package com.kama.jchatmind.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.*;

/*
 * ChatClient??? -- ??????AI???ChatClient
 * Spring?????ChatClient???Bean???Map?:key=bean?,value=??
 * get("deepseek-chat") = DeepSeek???,get("glm-5.1") = ??AI???
 *
 * ??????:???????,??fallback?????
 */
@Component
public class ChatClientRegistry {

    private final Map<String, ChatClient> chatClients;

    // ???????:?????,?????????
    private static final List<String> FALLBACK_ORDER = List.of("deepseek-chat", "glm-5.1");

    // Spring????:????ChatClient Bean ? ??Map
    public ChatClientRegistry(Map<String, ChatClient> chatClients) {
        this.chatClients = chatClients;
    }

    // ??????ChatClient,??????????
    public ChatClient get(String name) {
        ChatClient client = chatClients.get(name);
        if (client == null) {
            throw new IllegalStateException(
                    "????? '" + name + "' ? ChatClient,??????: " + chatClients.keySet());
        }
        return client;
    }

    /**
     * ?????,?????????????????
     * @param primaryModel ?????
     * @return ???ChatClient
     */
    public ChatClient getWithFallback(String primaryModel) {
        // 1. ?????
        ChatClient primary = chatClients.get(primaryModel);
        if (primary != null) {
            return primary;
        }

        // 2. ??????,???????????
        for (String fallbackModel : FALLBACK_ORDER) {
            if (fallbackModel.equals(primaryModel)) continue; // ?????????
            ChatClient fallback = chatClients.get(fallbackModel);
            if (fallback != null) {
                return fallback;
            }
        }

        // 3. ????????
        throw new IllegalStateException(
                "??? '" + primaryModel + "' ???,??????,??????: " + chatClients.keySet());
    }

    /** ???????????(?????????) */
    public String getResolvedModelName(String requestedModel, ChatClient actualClient) {
        for (Map.Entry<String, ChatClient> entry : chatClients.entrySet()) {
            if (entry.getValue() == actualClient) {
                return entry.getKey();
            }
        }
        return requestedModel;
    }

    /** ????????? */
    public Set<String> getAvailableModels() {
        return Collections.unmodifiableSet(chatClients.keySet());
    }

    /** ????????? */
    public List<String> getFallbackOrder() {
        return FALLBACK_ORDER;
    }
}