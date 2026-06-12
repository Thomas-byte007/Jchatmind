package com.kama.jchatmind.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ?????
 */
@Data
@AllArgsConstructor
public class ChatEvent {

    /**
     * ???ID
     */
    private String agentId;

    /**
     * ??ID
     */
    private String sessionId;

    /**
     * ????
     */
    private String userInput;
}
