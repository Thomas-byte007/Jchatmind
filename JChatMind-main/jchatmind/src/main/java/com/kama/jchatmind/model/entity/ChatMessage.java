package com.kama.jchatmind.model.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * ???????
 * ??????? chat_message ?
 */
@Data
@Builder
public class ChatMessage {

    /**
     * ??????
     */
    private String id;

    /**
     * ????ID
     */
    private String sessionId;

    /**
     * ????
     * user/assistant/system/tool
     */
    private String role;

    /**
     * ????
     */
    private String content;

    /**
     * ?????(JSON????)
     */
    private String metadata;

    /**
     * ????
     */
    private LocalDateTime createdAt;

    /**
     * ????
     */
    private LocalDateTime updatedAt;
}
