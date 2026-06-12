package com.kama.jchatmind.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.model.dto.ChatSessionDTO;
import com.kama.jchatmind.model.entity.ChatSession;
import com.kama.jchatmind.model.request.CreateChatSessionRequest;
import com.kama.jchatmind.model.request.UpdateChatSessionRequest;
import com.kama.jchatmind.model.vo.ChatSessionVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/*
 * ??????? -- Entity ? DTO ? VO ? Request ?????
 * ????????,????Agent????,?????Converter
 * metadata???ObjectMapper??JSON???
 * ?????agentId???,?title???
 */
@Component
@AllArgsConstructor
public class ChatSessionConverter {

    private final ObjectMapper objectMapper;

    // DTO ? Entity
    public ChatSession toEntity(ChatSessionDTO dto) throws JsonProcessingException {
        Assert.notNull(dto, "ChatSessionDTO cannot be null");
        return ChatSession.builder()
                .id(dto.getId()).agentId(dto.getAgentId()).title(dto.getTitle())
                .metadata(dto.getMetadata() != null ? objectMapper.writeValueAsString(dto.getMetadata()) : null)
                .createdAt(dto.getCreatedAt()).updatedAt(dto.getUpdatedAt()).build();
    }

    // Entity ? DTO
    public ChatSessionDTO toDTO(ChatSession entity) throws JsonProcessingException {
        Assert.notNull(entity, "ChatSession cannot be null");
        return ChatSessionDTO.builder()
                .id(entity.getId()).agentId(entity.getAgentId()).title(entity.getTitle())
                .metadata(entity.getMetadata() != null
                        ? objectMapper.readValue(entity.getMetadata(), ChatSessionDTO.MetaData.class) : null)
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    // DTO ? VO:??????????
    public ChatSessionVO toVO(ChatSessionDTO dto) {
        return ChatSessionVO.builder()
                .id(dto.getId()).agentId(dto.getAgentId()).title(dto.getTitle())
                .createdAt(dto.getCreatedAt()).updatedAt(dto.getUpdatedAt()).build();
    }

    // Entity ? VO:????
    public ChatSessionVO toVO(ChatSession entity) throws JsonProcessingException {
        return toVO(toDTO(entity));
    }

    // CreateRequest ? DTO
    public ChatSessionDTO toDTO(CreateChatSessionRequest request) {
        Assert.notNull(request, "CreateChatSessionRequest cannot be null");
        Assert.notNull(request.getAgentId(), "AgentId cannot be null");
        return ChatSessionDTO.builder()
                .agentId(request.getAgentId()).title(request.getTitle()).build();
    }

    // ???title???,agentId?id???
    public void updateDTOFromRequest(ChatSessionDTO dto, UpdateChatSessionRequest request) {
        Assert.notNull(dto, "ChatSessionDTO cannot be null");
        Assert.notNull(request, "UpdateChatSessionRequest cannot be null");
        if (request.getTitle() != null) dto.setTitle(request.getTitle());
    }
}