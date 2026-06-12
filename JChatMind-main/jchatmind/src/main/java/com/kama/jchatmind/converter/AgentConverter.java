package com.kama.jchatmind.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.model.dto.AgentDTO;
import com.kama.jchatmind.model.entity.Agent;
import com.kama.jchatmind.model.request.CreateAgentRequest;
import com.kama.jchatmind.model.request.UpdateAgentRequest;
import com.kama.jchatmind.model.vo.AgentVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

/*
 * Agent??? -- Entity ? DTO ? VO ? Request ?????????
 * Entity:?????;DTO:?????;VO:????;Request:????
 * ObjectMapper:Jackson?,??JSON ?? Java??(?allowedTools??????)
 */
@Component
@AllArgsConstructor
public class AgentConverter {

    private final ObjectMapper objectMapper;

    // DTO ? Entity:?List/??????JSON????????
    public Agent toEntity(AgentDTO agentDTO) throws JsonProcessingException {
        Assert.notNull(agentDTO, "AgentDTO cannot be null");
        Assert.notNull(agentDTO.getAllowedTools(), "Allowed tools cannot be null");
        Assert.notNull(agentDTO.getAllowedKbs(), "Allowed kbs cannot be null");
        Assert.notNull(agentDTO.getChatOptions(), "Chat options cannot be null");
        Assert.notNull(agentDTO.getModel(), "Model cannot be null");
        return Agent.builder()
                .id(agentDTO.getId()).name(agentDTO.getName())
                .description(agentDTO.getDescription()).systemPrompt(agentDTO.getSystemPrompt())
                .model(agentDTO.getModel().getModelName())
                .allowedTools(objectMapper.writeValueAsString(agentDTO.getAllowedTools()))
                .allowedKbs(objectMapper.writeValueAsString(agentDTO.getAllowedKbs()))
                .chatOptions(objectMapper.writeValueAsString(agentDTO.getChatOptions()))
                .createdAt(agentDTO.getCreatedAt()).updatedAt(agentDTO.getUpdatedAt())
                .build();
    }

    // Entity ? DTO:?JSON????????List/????????
    public AgentDTO toDTO(Agent agent) throws JsonProcessingException {
        Assert.notNull(agent, "Agent cannot be null");
        AgentDTO.ModelType model = AgentDTO.ModelType.fromModelName(agent.getModel());
        List<String> tools = objectMapper.readValue(agent.getAllowedTools(), new TypeReference<>() {});
        List<String> kbs = objectMapper.readValue(agent.getAllowedKbs(), new TypeReference<>() {});
        AgentDTO.ChatOptions options = objectMapper.readValue(agent.getChatOptions(), AgentDTO.ChatOptions.class);
        return AgentDTO.builder()
                .id(agent.getId()).name(agent.getName())
                .description(agent.getDescription()).systemPrompt(agent.getSystemPrompt())
                .model(model).allowedTools(tools).allowedKbs(kbs).chatOptions(options)
                .createdAt(agent.getCreatedAt()).updatedAt(agent.getUpdatedAt())
                .build();
    }

    // Entity ? VO:????,??DTO????????? toDTO() + toVO(DTO)
    public AgentVO toVO(Agent agent) throws JsonProcessingException {
        return toVO(toDTO(agent));
    }

    // DTO ? VO:??????????
    public AgentVO toVO(AgentDTO agentDTO) {
        Assert.notNull(agentDTO, "AgentDTO cannot be null");
        return AgentVO.builder()
                .id(agentDTO.getId()).name(agentDTO.getName())
                .description(agentDTO.getDescription()).systemPrompt(agentDTO.getSystemPrompt())
                .model(agentDTO.getModel()).allowedTools(agentDTO.getAllowedTools())
                .allowedKbs(agentDTO.getAllowedKbs()).chatOptions(agentDTO.getChatOptions())
                .createdAt(agentDTO.getCreatedAt()).updatedAt(agentDTO.getUpdatedAt())
                .build();
    }

    // CreateRequest ? DTO:??????????DTO??
    public AgentDTO toDTO(CreateAgentRequest request) {
        AgentDTO dto = new AgentDTO();
        dto.setName(request.getName());
        dto.setDescription(request.getDescription());
        dto.setSystemPrompt(request.getSystemPrompt());
        dto.setModel(AgentDTO.ModelType.fromModelName(request.getModel()));
        dto.setAllowedTools(request.getAllowedTools());
        dto.setAllowedKbs(request.getAllowedKbs());
        dto.setChatOptions(request.getChatOptions());
        return dto;
    }

    // ?UpdateRequest???????DTO,??????(PATCH??)
    public void updateDTOFromRequest(AgentDTO dto, UpdateAgentRequest request) {
        if (request.getName() != null) dto.setName(request.getName());
        if (request.getDescription() != null) dto.setDescription(request.getDescription());
        if (request.getSystemPrompt() != null) dto.setSystemPrompt(request.getSystemPrompt());
        if (request.getModel() != null) dto.setModel(AgentDTO.ModelType.fromModelName(request.getModel()));
        if (request.getAllowedTools() != null) dto.setAllowedTools(request.getAllowedTools());
        if (request.getAllowedKbs() != null) dto.setAllowedKbs(request.getAllowedKbs());
        if (request.getChatOptions() != null) dto.setChatOptions(request.getChatOptions());
    }
}