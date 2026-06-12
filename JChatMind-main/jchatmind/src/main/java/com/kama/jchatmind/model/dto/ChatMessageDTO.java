package com.kama.jchatmind.model.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ??????????(DTO)
 */
@Data
@Builder
public class ChatMessageDTO {

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
     */
    private RoleType role;

    /**
     * ????
     */
    private String content;

    /**
     * ?????
     */
    private MetaData metadata;

    /**
     * ????
     */
    private LocalDateTime createdAt;

    /**
     * ????
     */
    private LocalDateTime updatedAt;

    /**
     * ????? -- ???????,?? JSON ???????????
     * ?????? Spring AI ? ToolResponse/ToolCall,????????
     */
    @Data
    @Builder
    public static class MetaData {
        /** ???? */
        private ToolResult toolResponse;
        /** ?????? */
        private List<ToolInvocation> toolCalls;
    }

    /** ?????? -- ???? ToolCall { id, type, name, arguments } */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToolInvocation {
        private String id;
        private String type;
        private String name;
        private String arguments;
    }

    /** ?????? -- ???? ToolResponse { id, name, responseData } */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToolResult {
        private String id;
        private String name;
        private String responseData;
    }

    /**
     * ??????
     */
    @Getter
    @AllArgsConstructor
    public enum RoleType {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system"),
        TOOL("tool");

        @JsonValue
        private final String role;

        public static RoleType fromRole(String role) {
            for (RoleType value : values()) {
                if (value.role.equals(role)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
