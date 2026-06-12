package com.kama.jchatmind.agent.examples;

import com.kama.jchatmind.agent.tools.test.CityTool;
import com.kama.jchatmind.agent.tools.test.DateTool;
import com.kama.jchatmind.agent.tools.test.WeatherTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JChatMindV2 ???
 * ????????(ReAct ??)
 */
@SpringBootTest
public class JChatMindV2Test {

    @Autowired
    @Qualifier("deepseek-chat")
    private ChatClient chatClient;

    @Autowired
    private CityTool cityTool;

    @Autowired
    private DateTool dateTool;

    @Autowired
    private WeatherTool weatherTool;

    @Test
    public void testToolCalling() {
        // ??????
        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(cityTool, dateTool, weatherTool)
                .build()
                .getToolCallbacks();

        // ?? V2 ??
        JChatMindV2 agent = new JChatMindV2(
                "test-agent-v2",
                "?? Agent V2",
                "????????,???????????????????",
                chatClient,
                20,
                "test-session-v2",
                Arrays.asList(toolCallbacks)
        );

        // ???????????
        String userInput = "?????????";
        String response = agent.chat(userInput);

        // ???????
        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("????: " + userInput);
        System.out.println("AI ??: " + response);
        System.out.println("??????: " + agent.getConversationHistory().size());
    }

    @Test
    public void testMultipleToolCalls() {
        // ??????
        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(cityTool, dateTool, weatherTool)
                .build()
                .getToolCallbacks();

        // ?? V2 ??
        JChatMindV2 agent = new JChatMindV2(
                "test-agent-v2",
                "?? Agent V2",
                "????????,???????????????????",
                chatClient,
                20,
                "test-session-v2-multi",
                Arrays.asList(toolCallbacks)
        );

        // ?????????????
        String userInput = "??????????????,??????????????";
        String response = agent.chat(userInput);

        // ???????
        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("????: " + userInput);
        System.out.println("AI ??: " + response);
        System.out.println("??????: " + agent.getConversationHistory().size());
    }

    @Test
    public void testConversationWithoutToolCalling() {
        // ?? V2 ??(?????)
        JChatMindV2 agent = new JChatMindV2(
                "test-agent-v2",
                "?? Agent V2",
                "??????????",
                chatClient,
                20,
                "test-session-v2-no-tool",
                List.of() // ?????
        );

        // ????????????
        String userInput = "??,?????????";
        String response = agent.chat(userInput);

        // ???????
        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("????: " + userInput);
        System.out.println("AI ??: " + response);
    }

    @Test
    public void testReActLoop() {
        // ??????
        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(cityTool, dateTool, weatherTool)
                .build()
                .getToolCallbacks();

        // ?? V2 ??
        JChatMindV2 agent = new JChatMindV2(
                "test-agent-v2",
                "?? Agent V2",
                "????????,???????????????????",
                chatClient,
                20,
                "test-session-v2-react",
                Arrays.asList(toolCallbacks)
        );

        // ?? ReAct ??(think-execute)
        String userInput = "?????????,???????????";
        String response = agent.chat(userInput);

        // ???????
        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("????: " + userInput);
        System.out.println("AI ??: " + response);
        System.out.println("??????: " + agent.getConversationHistory().size());
    }
}

