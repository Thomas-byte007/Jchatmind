package com.kama.jchatmind.service;

import com.kama.jchatmind.model.request.CreateAgentRequest;
import com.kama.jchatmind.model.request.UpdateAgentRequest;
import com.kama.jchatmind.model.response.CreateAgentResponse;
import com.kama.jchatmind.model.response.GetAgentsResponse;

/*
 * Agent?????? -- ??Agent(???)?????????
 * ????:?Controller??????,????Mapper?Converter???
 * ? AgentFacadeServiceImpl ????
 */
public interface AgentFacadeService {
    GetAgentsResponse getAgents();                          // ????Agent
    CreateAgentResponse createAgent(CreateAgentRequest r);  // ??Agent
    void deleteAgent(String agentId);                       // ??Agent
    void updateAgent(String agentId, UpdateAgentRequest r); // ????Agent
}