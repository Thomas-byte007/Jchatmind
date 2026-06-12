package com.kama.jchatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kama.jchatmind.converter.ChatMessageConverter;
import com.kama.jchatmind.event.ChatEvent;
import com.kama.jchatmind.exception.BizException;
import com.kama.jchatmind.mapper.ChatMessageMapper;
import com.kama.jchatmind.model.dto.ChatMessageDTO;
import com.kama.jchatmind.model.entity.ChatMessage;
import com.kama.jchatmind.model.request.CreateChatMessageRequest;
import com.kama.jchatmind.model.request.UpdateChatMessageRequest;
import com.kama.jchatmind.model.response.CreateChatMessageResponse;
import com.kama.jchatmind.model.response.GetChatMessagesResponse;
import com.kama.jchatmind.model.vo.ChatMessageVO;
import com.kama.jchatmind.service.ChatMessageFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * ???????? -- ?????????????????
 * ApplicationEventPublisher:Spring?????,?????????ChatEvent
 * ??Agent????????????,??????AI?????
 */
@Service
@AllArgsConstructor
public class ChatMessageFacadeServiceImpl implements ChatMessageFacadeService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageConverter chatMessageConverter;
    // ?????:publishEvent()????,@EventListener????
    private final ApplicationEventPublisher publisher;

    // ???????? ? ?VO???
    @Override
    public GetChatMessagesResponse getChatMessagesBySessionId(String sessionId) {
        List<ChatMessage> chatMessages = chatMessageMapper.selectBySessionId(sessionId);
        List<ChatMessageVO> result = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            try {
                result.add(chatMessageConverter.toVO(chatMessage));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetChatMessagesResponse.builder()
                .chatMessages(result.toArray(new ChatMessageVO[0]))
                .total(chatMessages.size()).build();
    }

    // ????????
    @Override
    public GetChatMessagesResponse getChatMessagesBySessionIdPaginated(String sessionId, int limit, int offset) {
        int total = chatMessageMapper.countBySessionId(sessionId);
        List<ChatMessage> chatMessages = chatMessageMapper.selectBySessionIdPaginated(sessionId, limit, offset);
        List<ChatMessageVO> result = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            try {
                result.add(chatMessageConverter.toVO(chatMessage));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetChatMessagesResponse.builder()
                .chatMessages(result.toArray(new ChatMessageVO[0]))
                .total(total).build();
    }

    // ????N??? ? Agent?????
    @Override
    public List<ChatMessageDTO> getChatMessagesBySessionIdRecently(String sessionId, int limit) {
        List<ChatMessage> chatMessages = chatMessageMapper.selectBySessionIdRecently(sessionId, limit);
        List<ChatMessageDTO> result = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            try {
                result.add(chatMessageConverter.toDTO(chatMessage));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    // ????? ? ????? ? ????(??Agent??)
    @Override
    public CreateChatMessageResponse createChatMessage(CreateChatMessageRequest request) {
        ChatMessage chatMessage = doCreateChatMessage(request);
        publisher.publishEvent(new ChatEvent(request.getAgentId(), chatMessage.getSessionId(), chatMessage.getContent()));
        return CreateChatMessageResponse.builder().chatMessageId(chatMessage.getId()).build();
    }

    // ?????(DTO??),????
    @Override
    public CreateChatMessageResponse createChatMessage(ChatMessageDTO dto) {
        ChatMessage chatMessage = doCreateChatMessage(dto);
        return CreateChatMessageResponse.builder().chatMessageId(chatMessage.getId()).build();
    }

    // Agent???? ? ????,??????
    @Override
    public CreateChatMessageResponse agentCreateChatMessage(CreateChatMessageRequest r) {
        ChatMessage chatMessage = doCreateChatMessage(r);
        return CreateChatMessageResponse.builder().chatMessageId(chatMessage.getId()).build();
    }

    // ??????:Request ? DTO ? Entity ? ???
    private ChatMessage doCreateChatMessage(CreateChatMessageRequest request) {
        return doCreateChatMessage(chatMessageConverter.toDTO(request));
    }

    private ChatMessage doCreateChatMessage(ChatMessageDTO chatMessageDTO) {
        try {
            ChatMessage chatMessage = chatMessageConverter.toEntity(chatMessageDTO);
            LocalDateTime now = LocalDateTime.now();
            chatMessage.setCreatedAt(now);
            chatMessage.setUpdatedAt(now);
            int result = chatMessageMapper.insert(chatMessage);
            if (result <= 0) throw new BizException("????????");
            return chatMessage;
        } catch (JsonProcessingException e) {
            throw new BizException("??????????????: " + e.getMessage());
        }
    }

    // ??????:????? ? ????? ? ?????
    // @Transactional ???????????????,????????????
    @Transactional
    @Override
    public CreateChatMessageResponse appendChatMessage(String msgId, String appendContent) {
        ChatMessage chatMessage = chatMessageMapper.selectById(msgId);
        if (chatMessage == null) throw new BizException("?????: " + msgId);
        String oldContent = chatMessage.getContent();
        chatMessage.setContent((oldContent == null ? "" : oldContent) + appendContent);
        chatMessage.setUpdatedAt(LocalDateTime.now());
        chatMessageMapper.updateById(chatMessage);
        return CreateChatMessageResponse.builder().chatMessageId(msgId).build();
    }

    @Override
    public void deleteChatMessage(String chatMessageId) {
        ChatMessage chatMessage = chatMessageMapper.selectById(chatMessageId);
        if (chatMessage == null) throw new BizException("?????: " + chatMessageId);
        int result = chatMessageMapper.deleteById(chatMessageId);
        if (result <= 0) throw new BizException("??????");
    }

    @Override
    public void updateChatMessage(String chatMessageId, UpdateChatMessageRequest request) {
        ChatMessage chatMessage = chatMessageMapper.selectById(chatMessageId);
        if (chatMessage == null) throw new BizException("?????: " + chatMessageId);
        chatMessage.setContent(request.getContent());
        chatMessage.setUpdatedAt(LocalDateTime.now());
        chatMessageMapper.updateById(chatMessage);
    }
}