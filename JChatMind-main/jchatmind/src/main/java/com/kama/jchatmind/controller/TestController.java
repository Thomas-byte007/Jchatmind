package com.kama.jchatmind.controller;

import com.kama.jchatmind.service.SseService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * ????? -- ??????????
 *
 * ????:
 * /health:????,???????????
 * /sse-test:??SSE??????
 */
@RestController
public class TestController {

    private final SseService sseService;
    private final ChatClient chatClient;

    public TestController(SseService sseService,
                          @Qualifier("deepseek-chat") ChatClient chatClient) {
        this.sseService = sseService;
        this.chatClient = chatClient;
    }


    // ???? GET /health ? "ok" = ????
    @RequestMapping("/health")
    public String health() {
        return "ok";
    }

    // SSE???? GET /sse-test
    @GetMapping("/sse-test")
    public String sseTest() {
        return "ok";
    }


    @GetMapping("/test/chat")
    public String testChat(@RequestParam String question,@RequestParam String abc) {
        // ?? Agent??? think/execute??? chatMemory??? ToolCalling--
        // ??????"?????????,?????"
        return chatClient.prompt(abc)
                .user(question)
                .call()
                .content();
    }
}