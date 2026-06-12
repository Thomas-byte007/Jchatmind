package com.kama.jchatmind.controller;

import com.kama.jchatmind.model.common.ApiResponse;
import com.kama.jchatmind.model.request.CreateChatSessionRequest;
import com.kama.jchatmind.model.request.UpdateChatSessionRequest;
import com.kama.jchatmind.model.response.CreateChatSessionResponse;
import com.kama.jchatmind.model.response.GetChatSessionResponse;
import com.kama.jchatmind.model.response.GetChatSessionsResponse;
import com.kama.jchatmind.service.ChatSessionFacadeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/*
 * ??????? -- ??????????????????
 *
 * ????:
 * ??(Session) = ?????????,??????
 * ????????Agent(???),?? agentId ??
 * ?????:???????? ? ???? ? ?????? ? ??
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ChatSessionController {

    private final ChatSessionFacadeService chatSessionFacadeService;

    // ?????? GET /api/chat-sessions
    @GetMapping("/chat-sessions")
    public ApiResponse<GetChatSessionsResponse> getChatSessions() {
        return ApiResponse.success(chatSessionFacadeService.getChatSessions());
    }

    // ???????? GET /api/chat-sessions/{chatSessionId}
    @GetMapping("/chat-sessions/{chatSessionId}")
    public ApiResponse<GetChatSessionResponse> getChatSession(
            @PathVariable String chatSessionId) {
        return ApiResponse.success(chatSessionFacadeService.getChatSession(chatSessionId));
    }

    // ????Agent?????? GET /api/chat-sessions/agent/{agentId}
    @GetMapping("/chat-sessions/agent/{agentId}")
    public ApiResponse<GetChatSessionsResponse> getChatSessionsByAgentId(
            @PathVariable String agentId) {
        return ApiResponse.success(chatSessionFacadeService.getChatSessionsByAgentId(agentId));
    }

    // ????? POST /api/chat-sessions
    @PostMapping("/chat-sessions")
    public ApiResponse<CreateChatSessionResponse> createChatSession(
            @Valid @RequestBody CreateChatSessionRequest request) {
        return ApiResponse.success(chatSessionFacadeService.createChatSession(request));
    }

    // ????(???????????) DELETE /api/chat-sessions/{chatSessionId}
    @DeleteMapping("/chat-sessions/{chatSessionId}")
    public ApiResponse<Void> deleteChatSession(@PathVariable String chatSessionId) {
        chatSessionFacadeService.deleteChatSession(chatSessionId);
        return ApiResponse.success();
    }

    // ?????? PATCH /api/chat-sessions/{chatSessionId}
    @PatchMapping("/chat-sessions/{chatSessionId}")
    public ApiResponse<Void> updateChatSession(
            @PathVariable String chatSessionId,
            @RequestBody UpdateChatSessionRequest request) {
        chatSessionFacadeService.updateChatSession(chatSessionId, request);
        return ApiResponse.success();
    }
}