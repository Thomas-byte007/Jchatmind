package com.kama.jchatmind.service;

import com.kama.jchatmind.model.dto.ChatMessageDTO;
import com.kama.jchatmind.model.request.CreateChatMessageRequest;
import com.kama.jchatmind.model.request.UpdateChatMessageRequest;
import com.kama.jchatmind.model.response.CreateChatMessageResponse;
import com.kama.jchatmind.model.response.GetChatMessagesResponse;
import java.util.List;

/*
 * ?????????? -- ?????????????
 * ????:USER(??)?ASSISTANT(AI)?TOOL(????)
 * getChatMessagesBySessionIdRecently():????N?,????Agent??
 * appendChatMessage():?????????,??????
 */
public interface ChatMessageFacadeService {
    GetChatMessagesResponse getChatMessagesBySessionId(String sessionId);
    GetChatMessagesResponse getChatMessagesBySessionIdPaginated(String sessionId, int limit, int offset);
    List<ChatMessageDTO> getChatMessagesBySessionIdRecently(String sessionId, int limit);
    CreateChatMessageResponse createChatMessage(CreateChatMessageRequest request);
    CreateChatMessageResponse createChatMessage(ChatMessageDTO dto);
    CreateChatMessageResponse agentCreateChatMessage(CreateChatMessageRequest r);
    CreateChatMessageResponse appendChatMessage(String msgId, String appendContent);
    void deleteChatMessage(String chatMessageId);
    void updateChatMessage(String chatMessageId, UpdateChatMessageRequest r);
}