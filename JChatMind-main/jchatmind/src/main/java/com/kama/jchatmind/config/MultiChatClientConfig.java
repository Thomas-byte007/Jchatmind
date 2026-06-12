package com.kama.jchatmind.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * ????????? -- ???AI???????ChatClient
 * @Configuration:???Spring???,???????
 * @Bean:?????????Spring????Bean,bean?=???
 *
 * ????:Spring AI ? api-key ?????? "DUMMY_KEY_FOR_PROXY",
 * ?? API Key ??????? (jchatmind-proxy) ??????,
 * Agent ???????????
 */
@Configuration
public class MultiChatClientConfig {

    // ??DeepSeek???
    @Bean("deepseek-chat")
    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel) {
        return ChatClient.create(deepSeekChatModel);
    }

    // ????AI(GLM-5.1)???
    @Bean("glm-5.1")
    public ChatClient glm51ChatClient(ZhiPuAiChatModel zhiPuAiChatModel) {
        return ChatClient.create(zhiPuAiChatModel);
    }
}
