package com.kama.jchatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kama.jchatmind.converter.ChatSessionConverter;
import com.kama.jchatmind.exception.BizException;
import com.kama.jchatmind.mapper.ChatSessionMapper;
import com.kama.jchatmind.model.dto.ChatSessionDTO;
import com.kama.jchatmind.model.entity.ChatSession;
import com.kama.jchatmind.model.request.CreateChatSessionRequest;
import com.kama.jchatmind.model.request.UpdateChatSessionRequest;
import com.kama.jchatmind.model.response.CreateChatSessionResponse;
import com.kama.jchatmind.model.response.GetChatSessionResponse;
import com.kama.jchatmind.model.response.GetChatSessionsResponse;
import com.kama.jchatmind.model.vo.ChatSessionVO;
import com.kama.jchatmind.service.ChatSessionFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * ???????? -- ???????????
 * ChatSession:????,????Agent,??????
 * ChatSessionDTO/ChatSessionVO:????/????????
 */
@Service
@AllArgsConstructor
public class ChatSessionFacadeServiceImpl implements ChatSessionFacadeService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatSessionConverter chatSessionConverter;

    @Override
    public GetChatSessionsResponse getChatSessions() {
        List<ChatSession> sessions = chatSessionMapper.selectAll();
        List<ChatSessionVO> result = new ArrayList<>();
        for (ChatSession session : sessions) {
            try { result.add(chatSessionConverter.toVO(session)); }
            catch (JsonProcessingException e) { throw new RuntimeException(e); }
        }
        return GetChatSessionsResponse.builder().chatSessions(result.toArray(new ChatSessionVO[0])).build();
    }

    @Override
    public GetChatSessionResponse getChatSession(String chatSessionId) {
        ChatSession session = chatSessionMapper.selectById(chatSessionId);
        if (session == null) throw new BizException("?????: " + chatSessionId);
        try { return GetChatSessionResponse.builder().chatSession(chatSessionConverter.toVO(session)).build(); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }

    // ????Agent??????
    @Override
    public GetChatSessionsResponse getChatSessionsByAgentId(String agentId) {
        List<ChatSession> sessions = chatSessionMapper.selectByAgentId(agentId);
        List<ChatSessionVO> result = new ArrayList<>();
        for (ChatSession session : sessions) {
            try { result.add(chatSessionConverter.toVO(session)); }
            catch (JsonProcessingException e) { throw new RuntimeException(e); }
        }
        return GetChatSessionsResponse.builder().chatSessions(result.toArray(new ChatSessionVO[0])).build();
    }

    // ????:Request ? DTO ? Entity ? ???
    @Override
    public CreateChatSessionResponse createChatSession(CreateChatSessionRequest request) {
        try {
            ChatSessionDTO dto = chatSessionConverter.toDTO(request);
            ChatSession session = chatSessionConverter.toEntity(dto);
            LocalDateTime now = LocalDateTime.now();
            session.setCreatedAt(now);
            session.setUpdatedAt(now);
            if (chatSessionMapper.insert(session) <= 0) throw new BizException("??????");
            return CreateChatSessionResponse.builder().chatSessionId(session.getId()).build();
        } catch (JsonProcessingException e) { throw new BizException("?????: " + e.getMessage()); }
    }

    @Override
    public void deleteChatSession(String chatSessionId) {
        ChatSession session = chatSessionMapper.selectById(chatSessionId);
        if (session == null) throw new BizException("?????: " + chatSessionId);
        if (chatSessionMapper.deleteById(chatSessionId) <= 0) throw new BizException("??????");
    }

    // ????:???????(id/agentId/createdAt),???????
    @Override
    public void updateChatSession(String chatSessionId, UpdateChatSessionRequest request) {
        try {
            ChatSession existing = chatSessionMapper.selectById(chatSessionId);
            if (existing == null) throw new BizException("?????: " + chatSessionId);
            ChatSessionDTO dto = chatSessionConverter.toDTO(existing);
            chatSessionConverter.updateDTOFromRequest(dto, request);
            ChatSession updated = chatSessionConverter.toEntity(dto);
            updated.setId(existing.getId());
            updated.setAgentId(existing.getAgentId());
            updated.setCreatedAt(existing.getCreatedAt());
            updated.setUpdatedAt(LocalDateTime.now());
            if (chatSessionMapper.updateById(updated) <= 0) throw new BizException("??????");
        } catch (JsonProcessingException e) { throw new BizException("?????: " + e.getMessage()); }
    }
}