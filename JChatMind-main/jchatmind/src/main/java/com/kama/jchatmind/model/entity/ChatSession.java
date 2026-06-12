package com.kama.jchatmind.model.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * ???????
 * ??????? chat_session ?
 */
@Data
@Builder
public class ChatSession {

    /**
     * ??????
     */
    private String id;

    /**
     * ??????ID
     */
    private String agentId;

    /**
     * ????
     */
    private String title;

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
