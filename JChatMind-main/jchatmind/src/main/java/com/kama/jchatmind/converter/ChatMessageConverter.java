package com.kama.jchatmind.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.model.dto.ChatMessageDTO;
import com.kama.jchatmind.model.entity.ChatMessage;
import com.kama.jchatmind.model.request.CreateChatMessageRequest;
import com.kama.jchatmind.model.request.UpdateChatMessageRequest;
import com.kama.jchatmind.model.vo.ChatMessageVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/*
 * ??????? -- Entity ? DTO ? VO ? Request ?????
 * ObjectMapper:??metadata???JSON???(?????????)
 */
@Component
@AllArgsConstructor
public class ChatMessageConverter {

    private final ObjectMapper objectMapper;

    // Entity ? DTO:?JSON???????
    public ChatMessageDTO toDTO(ChatMessage chatMessage) throws JsonProcessingException {
        Assert.notNull(chatMessage, "ChatMessage cannot be null");
        ChatMessageDTO.MetaData metaData = chatMessage.getMetadata() != null
                ? objectMapper.readValue(chatMessage.getMetadata(), ChatMessageDTO.MetaData.class) : null;
        return ChatMessageDTO.builder()
                .id(chatMessage.getId()).sessionId(chatMessage.getSessionId())
                .role(ChatMessageDTO.RoleType.fromRole(chatMessage.getRole()))
                .content(chatMessage.getContent()).metadata(metaData).build();
    }

    // DTO ? Entity:????????JSON????????
    public ChatMessage toEntity(ChatMessageDTO dto) throws JsonProcessingException {
        Assert.notNull(dto, "ChatMessageDTO cannot be null");
        Assert.notNull(dto.getRole(), "Role cannot be null");
        String metaData = dto.getMetadata() != null ? objectMapper.writeValueAsString(dto.getMetadata()) : null;
        return ChatMessage.builder()
                .id(dto.getId()).sessionId(dto.getSessionId())
                .role(dto.getRole().getRole()).content(dto.getContent()).metadata(metaData).build();
    }

    // DTO ? VO:??????,?????????
    public ChatMessageVO toVO(ChatMessageDTO dto) {
        return ChatMessageVO.builder()
                .id(dto.getId()).sessionId(dto.getSessionId())
                .role(dto.getRole()).content(dto.getContent()).metadata(dto.getMetadata()).build();
    }

    // Entity ? VO:????
    public ChatMessageVO toVO(ChatMessage chatMessage) throws JsonProcessingException {
        return toVO(toDTO(chatMessage));
    }

    // CreateRequest ? DTO:??????????
    public ChatMessageDTO toDTO(CreateChatMessageRequest request) {
        Assert.notNull(request, "CreateChatMessageRequest cannot be null");
        Assert.notNull(request.getSessionId(), "SessionId cannot be null");
        Assert.notNull(request.getRole(), "Role cannot be null");
        return ChatMessageDTO.builder()
                .sessionId(request.getSessionId()).role(request.getRole())
                .content(request.getContent()).metadata(request.getMetadata()).build();
    }

    // ????????null??(??????content?metadata)
    public void updateDTOFromRequest(ChatMessageDTO dto, UpdateChatMessageRequest request) {
        Assert.notNull(dto, "ChatMessageDTO cannot be null");
        Assert.notNull(request, "UpdateChatMessageRequest cannot be null");
        if (request.getContent() != null) dto.setContent(request.getContent());
        if (request.getMetadata() != null) dto.setMetadata(request.getMetadata());
    }
}