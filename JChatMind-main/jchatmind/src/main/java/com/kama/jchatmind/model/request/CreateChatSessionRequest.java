package com.kama.jchatmind.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ??????????
 */
@Data
public class CreateChatSessionRequest {

    /**
     * ??????ID
     */
    @NotBlank(message = "???ID????")
    private String agentId;

    /**
     * ????
     */
    private String title;
}
