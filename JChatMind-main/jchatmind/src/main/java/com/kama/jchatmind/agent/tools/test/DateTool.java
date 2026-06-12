/*
 * ????(???) -- ???????????
 * FIXED??????,????Agent?????????????
 */
package com.kama.jchatmind.agent.tools.test;

import com.kama.jchatmind.agent.tools.Tool;
import com.kama.jchatmind.agent.tools.ToolType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTool implements Tool {

    @Override
    public String getName() { return "dateTool"; }

    @Override
    public String getDescription() { return "???????"; }

    @Override
    public ToolType getType() { return ToolType.FIXED; }

    @org.springframework.ai.tool.annotation.Tool(name = "getDate", description = "???????")
    public String getDate() {
        // LocalDateTime.now()??????,DateTimeFormatter?????? yyyy-MM-dd
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}