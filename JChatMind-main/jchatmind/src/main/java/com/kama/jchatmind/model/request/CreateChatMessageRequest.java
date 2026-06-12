package com.kama.jchatmind.model.request;

import com.kama.jchatmind.model.dto.ChatMessageDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * ??????????
 */
@Data
@Builder
public class CreateChatMessageRequest {

    /**
     * ?????ID
     */
    @NotBlank(message = "???ID????")
    private String agentId;

    /**
     * ????ID
     */
    @NotBlank(message = "??ID????")
    private String sessionId;

    /**
     * ????
     */
    @NotNull(message = "????????")
    private ChatMessageDTO.RoleType role;

    /**
     * ????
     */
    @NotBlank(message = "????????")
    private String content;

    /**
     * ?????
     */
    private ChatMessageDTO.MetaData metadata;
}
