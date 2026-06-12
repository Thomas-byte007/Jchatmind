package com.kama.jchatmind.controller;

import com.kama.jchatmind.model.common.ApiResponse;
import com.kama.jchatmind.model.request.CreateChatMessageRequest;
import com.kama.jchatmind.model.request.UpdateChatMessageRequest;
import com.kama.jchatmind.model.response.CreateChatMessageResponse;
import com.kama.jchatmind.model.response.GetChatMessagesResponse;
import com.kama.jchatmind.service.ChatMessageFacadeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/*
 * ??????? -- ?????CRUD
 *
 * ????:
 * ????:USER(????) / ASSISTANT(AI??) / TOOL(???????)
 * ????????? sessionId(??ID),??????????????
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ChatMessageController {

    private final ChatMessageFacadeService chatMessageFacadeService;

    /*
     * ????????????
     * GET /api/chat-messages/session/{sessionId}
     */
    @GetMapping("/chat-messages/session/{sessionId}")
    public ApiResponse<GetChatMessagesResponse> getChatMessagesBySessionId(
            @PathVariable String sessionId) {
        return ApiResponse.success(
                chatMessageFacadeService.getChatMessagesBySessionId(sessionId));
    }

    /*
     * ????????????
     * GET /api/chat-messages/session/{sessionId}/paginated?limit=20&offset=0
     */
    @GetMapping("/chat-messages/session/{sessionId}/paginated")
    public ApiResponse<GetChatMessagesResponse> getChatMessagesBySessionIdPaginated(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.success(
                chatMessageFacadeService.getChatMessagesBySessionIdPaginated(sessionId, limit, offset));
    }

    /*
     * ??????(????????)
     * POST /api/chat-messages
     */
    @PostMapping("/chat-messages")
    public ApiResponse<CreateChatMessageResponse> createChatMessage(
            @Valid @RequestBody CreateChatMessageRequest request) {
        return ApiResponse.success(
                chatMessageFacadeService.createChatMessage(request));
    }

    /*
     * ????
     * DELETE /api/chat-messages/{chatMessageId}
     */
    @DeleteMapping("/chat-messages/{chatMessageId}")
    public ApiResponse<Void> deleteChatMessage(@PathVariable String chatMessageId) {
        chatMessageFacadeService.deleteChatMessage(chatMessageId);
        return ApiResponse.success();
    }

    /*
     * ??????
     * PATCH /api/chat-messages/{chatMessageId}
     */
    @PatchMapping("/chat-messages/{chatMessageId}")
    public ApiResponse<Void> updateChatMessage(
            @PathVariable String chatMessageId,
            @RequestBody UpdateChatMessageRequest request) {
        chatMessageFacadeService.updateChatMessage(chatMessageId, request);
        return ApiResponse.success();
    }
}