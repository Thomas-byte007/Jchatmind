package com.kama.jchatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kama.jchatmind.converter.AgentConverter;
import com.kama.jchatmind.exception.BizException;
import com.kama.jchatmind.mapper.AgentMapper;
import com.kama.jchatmind.model.dto.AgentDTO;
import com.kama.jchatmind.model.entity.Agent;
import com.kama.jchatmind.model.request.CreateAgentRequest;
import com.kama.jchatmind.model.request.UpdateAgentRequest;
import com.kama.jchatmind.model.response.CreateAgentResponse;
import com.kama.jchatmind.model.response.GetAgentsResponse;
import com.kama.jchatmind.model.vo.AgentVO;
import com.kama.jchatmind.service.AgentFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Agent?????? -- ???????Mapper+Converter,?Controller??????
 * @Service:???Service???,Spring????????
 * AgentMapper:MyBatis????,??????
 * AgentConverter:???DTO?VO??????
 */
@Service
@AllArgsConstructor
public class AgentFacadeServiceImpl implements AgentFacadeService {

    private final AgentMapper agentMapper;
    private final AgentConverter agentConverter;

    // ????Agent,Entity?? ? VO??
    @Override
    public GetAgentsResponse getAgents() {
        List<Agent> agents = agentMapper.selectAll();
        List<AgentVO> result = new ArrayList<>();
        for (Agent agent : agents) {
            try {
                result.add(agentConverter.toVO(agent));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetAgentsResponse.builder().agents(result.toArray(new AgentVO[0])).build();
    }

    // ??Agent:Request ? DTO ? Entity ? ?????
    @Override
    public CreateAgentResponse createAgent(CreateAgentRequest request) {
        try {
            AgentDTO agentDTO = agentConverter.toDTO(request);
            Agent agent = agentConverter.toEntity(agentDTO);
            LocalDateTime now = LocalDateTime.now();
            agent.setCreatedAt(now);
            agent.setUpdatedAt(now);
            int result = agentMapper.insert(agent);
            if (result <= 0) {
                throw new BizException("?? agent ??");
            }
            return CreateAgentResponse.builder().agentId(agent.getId()).build();
        } catch (JsonProcessingException e) {
            throw new BizException("?? agent ????????: " + e.getMessage());
        }
    }

    // ??Agent:???????,???
    @Override
    public void deleteAgent(String agentId) {
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new BizException("Agent ???: " + agentId);
        }
        int result = agentMapper.deleteById(agentId);
        if (result <= 0) {
            throw new BizException("?? agent ??");
        }
    }

    // ??Agent:?????,?Request???????????
    // createdAt?id???,updatedAt????
    @Override
    public void updateAgent(String agentId, UpdateAgentRequest request) {
        try {
            Agent existingAgent = agentMapper.selectById(agentId);
            if (existingAgent == null) {
                throw new BizException("Agent ???: " + agentId);
            }
            AgentDTO agentDTO = agentConverter.toDTO(existingAgent);
            agentConverter.updateDTOFromRequest(agentDTO, request);
            Agent updatedAgent = agentConverter.toEntity(agentDTO);
            updatedAgent.setId(existingAgent.getId());
            updatedAgent.setCreatedAt(existingAgent.getCreatedAt());
            updatedAgent.setUpdatedAt(LocalDateTime.now());
            int result = agentMapper.updateById(updatedAgent);
            if (result <= 0) {
                throw new BizException("?? agent ??");
            }
        } catch (JsonProcessingException e) {
            throw new BizException("?? agent ????????: " + e.getMessage());
        }
    }
}