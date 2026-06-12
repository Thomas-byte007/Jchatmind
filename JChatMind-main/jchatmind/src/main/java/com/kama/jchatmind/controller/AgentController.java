package com.kama.jchatmind.controller;

import com.kama.jchatmind.model.common.ApiResponse;
import com.kama.jchatmind.model.request.CreateAgentRequest;
import com.kama.jchatmind.model.request.UpdateAgentRequest;
import com.kama.jchatmind.model.response.CreateAgentResponse;
import com.kama.jchatmind.model.response.GetAgentsResponse;
import com.kama.jchatmind.service.AgentFacadeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/*
 * Agent(???)?????
 *
 * ????:
 * @RestController = ??????HTTP?????
 * @RequestMapping("/api") = ???????? /api ??
 * @AllArgsConstructor = Lombok????????,?????? final ? service
 * CRUD = ?(Create)?(Delete)?(Update)?(Read)
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AgentController {

    // AgentFacadeService:??Agent????????
    private final AgentFacadeService agentFacadeService;

    /*
     * ????Agent
     * GET /api/agents ? ????Agent?JSON??
     */
    @GetMapping("/agents")
    public ApiResponse<GetAgentsResponse> getAgents() {
        return ApiResponse.success(agentFacadeService.getAgents());
    }

    /*
     * ?????Agent
     * POST /api/agents
     * @RequestBody:??????JSON???? CreateAgentRequest ??
     */
    @PostMapping("/agents")
    public ApiResponse<CreateAgentResponse> createAgent(@Valid @RequestBody CreateAgentRequest request) {
        return ApiResponse.success(agentFacadeService.createAgent(request));
    }

    /*
     * ????Agent
     * DELETE /api/agents/{agentId}
     * @PathVariable:?URL????? {agentId} ??
     */
    @DeleteMapping("/agents/{agentId}")
    public ApiResponse<Void> deleteAgent(@PathVariable String agentId) {
        agentFacadeService.deleteAgent(agentId);
        return ApiResponse.success();
    }

    /*
     * ??Agent?????(?????????)
     * PATCH /api/agents/{agentId}
     * ?PUT???:PATCH????????,PUT???????
     */
    @PatchMapping("/agents/{agentId}")
    public ApiResponse<Void> updateAgent(
            @PathVariable String agentId,
            @RequestBody UpdateAgentRequest request
    ) {
        agentFacadeService.updateAgent(agentId, request);
        return ApiResponse.success();
    }
}