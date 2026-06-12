package com.kama.jchatmind.model.request;

import com.kama.jchatmind.model.dto.AgentDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * ?????????
 */
@Data
public class CreateAgentRequest {

    /**
     * ?????
     */
    @NotBlank(message = "?????????")
    private String name;

    /**
     * ?????
     */
    private String description;

    /**
     * ?????
     */
    private String systemPrompt;

    /**
     * ???AI??
     */
    @NotBlank(message = "AI??????")
    private String model;

    /**
     * ?????????
     */
    private List<String> allowedTools;

    /**
     * ??????????
     */
    private List<String> allowedKbs;

    /**
     * ??????
     */
    @NotNull(message = "????????")
    private AgentDTO.ChatOptions chatOptions;
}
