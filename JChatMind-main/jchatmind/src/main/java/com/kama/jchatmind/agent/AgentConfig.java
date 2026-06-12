package com.kama.jchatmind.agent;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.Assert;

@Data
@Builder
public class AgentConfig {

    private String agentId;
    private String name;
    private String description;
    private String systemPrompt;
    private Integer maxMessages;
    private String chatSessionId;

    public void validate() {
        Assert.hasText(agentId, "agentId ????");
        Assert.hasText(name, "name ????");
        Assert.notNull(chatSessionId, "chatSessionId ????");
        if (maxMessages != null) {
            Assert.isTrue(maxMessages > 0, "maxMessages ????0");
        }
    }
}
