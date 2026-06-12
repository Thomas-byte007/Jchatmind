package com.kama.jchatmind.model.request;

import com.kama.jchatmind.model.dto.AgentDTO;
import lombok.Data;

import java.util.List;

/**
 * ?????????
 */
@Data
public class UpdateAgentRequest {

    /**
     * ?????
     */
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
    private AgentDTO.ChatOptions chatOptions;
}
