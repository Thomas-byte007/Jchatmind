package com.kama.jchatmind.agent.examples;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JChatMindV1 ???
 * ????????
 */
@SpringBootTest
public class JChatMindV1Test {

    @Autowired
    @Qualifier("deepseek-chat")
    private ChatClient chatClient;

    @Test
    public void testBasicChat() {
        // ?? V1 ??
        JChatMindV1 agent = new JChatMindV1(
                "test-agent-v1",
                "?? Agent V1",
                "??????????",
                chatClient,
                20,
                "test-session-v1"
        );

        // ??????
        String userInput = "??,?????????";
        String response = agent.chat(userInput);

        // ???????
        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("????: " + userInput);
        System.out.println("AI ??: " + response);
        System.out.println("??????: " + agent.getConversationHistory().size());
    }

    @Test
    public void testMultiTurnConversation() {
        // ?? V1 ??
        JChatMindV1 agent = new JChatMindV1(
                "test-agent-v1",
                "?? Agent V1",
                "",
                chatClient,
                20,
                "test-session-v1-multi"
        );

        // ?????
        String response1 = agent.chat("????????");
        assertNotNull(response1);
        System.out.println("??? - [??]: ?????????");
        System.out.println("??? - [AI]: " + response1);

        // ?????(???????)
        String response2 = agent.chat("?????????");
        assertNotNull(response2);
        System.out.println("??? - [??]: ?????????");
        System.out.println("??? - [AI]: " + response2);

        // ????????????
        assertTrue(agent.getConversationHistory().size() >= 4); // ????:???? + ????1 + AI??1 + ????2 + AI??2
    }

    @Test
    public void testResetConversation() {
        // ?? V1 ??
        JChatMindV1 agent = new JChatMindV1(
                "test-agent-v1",
                "?? Agent V1",
                "???????",
                chatClient,
                20,
                "test-session-v1-reset"
        );

        // ????
        agent.chat("??");
        int historySizeBeforeReset = agent.getConversationHistory().size();

        // ????
        agent.reset();

        // ?????????(???????)
        int historySizeAfterReset = agent.getConversationHistory().size();
        assertTrue(historySizeAfterReset < historySizeBeforeReset);
        assertTrue(historySizeAfterReset <= 1); // ??????

        System.out.println("???????: " + historySizeBeforeReset);
        System.out.println("???????: " + historySizeAfterReset);
    }
}

