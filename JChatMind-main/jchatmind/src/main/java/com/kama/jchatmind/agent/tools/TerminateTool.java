package com.kama.jchatmind.agent.tools;

import org.springframework.stereotype.Component;

/**
 * ???? - Agent Loop?????
 */
@Component
public class TerminateTool implements Tool {

    @Override
    public String getName() {
        return "terminate";
    }

    @Override
    public String getDescription() {
        return "?? Agent Loop ???";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(name = "terminate", description = "???????????????????,?????????")
    public void terminate() {
    }
}
