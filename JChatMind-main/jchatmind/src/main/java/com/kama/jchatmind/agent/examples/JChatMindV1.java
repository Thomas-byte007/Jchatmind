package com.kama.jchatmind.agent.examples;

import com.kama.jchatmind.agent.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * V1 ??:??????
 * ??:?? LLM,??????
 * 
 * ????:
 * - ?? ChatClient ? LLM ??
 * - ?? ChatMemory ??????
 * - ???????
 * - ??????? -> AI ????
 */
@Slf4j
public class JChatMindV1 {
    // ??
    protected String name;
    
    // ??
    protected String description;
    
    // ?????
    protected String systemPrompt;
    
    // ChatClient ??
    protected ChatClient chatClient;
    
    // ????
    protected ChatMemory chatMemory;
    
    // ??
    protected AgentState agentState;
    
    // ?? ID(??????)
    protected String sessionId;
    
    private static final Integer DEFAULT_MAX_MESSAGES = 20;
    
    public JChatMindV1() {}
    
    public JChatMindV1(String name,
                      String description,
                      String systemPrompt,
                      ChatClient chatClient,
                      Integer maxMessages,
                      String sessionId) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.chatClient = chatClient;
        this.sessionId = sessionId != null ? sessionId : "default-session";
        this.agentState = AgentState.IDLE;
        
        // ???????
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(maxMessages != null ? maxMessages : DEFAULT_MAX_MESSAGES)
                .build();
        
        // ???????
        if (StringUtils.hasLength(systemPrompt)) {
            this.chatMemory.add(this.sessionId, new SystemMessage(systemPrompt));
        }
    }
    
    /**
     * ????????? AI ??
     */
    public String chat(String userInput) {
        Assert.notNull(userInput, "????????");
        
        if (agentState != AgentState.IDLE) {
            throw new IllegalStateException("Agent ???? IDLE,????:" + agentState);
        }
        
        try {
            agentState = AgentState.THINKING;
            
            // ?????????
            UserMessage userMessage = new UserMessage(userInput);
            chatMemory.add(sessionId, userMessage);
            
            // ?????
            Prompt prompt = Prompt.builder()
                    .messages(chatMemory.get(sessionId))
                    .build();
            
            // ?? LLM
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            Assert.notNull(response, "ChatResponse ????");
            
            AssistantMessage assistantMessage = response.getResult().getOutput();
            String aiResponse = assistantMessage.getText();
            
            // ? AI ???????
            chatMemory.add(sessionId, assistantMessage);
            
            agentState = AgentState.FINISHED;
            
            return aiResponse;
            
        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("?????????", e);
            throw new RuntimeException("?????????", e);
        } finally {
            // ??????????
            agentState = AgentState.IDLE;
        }
    }
    
    /**
     * ????????
     */
    public List<Message> getConversationHistory() {
        return chatMemory.get(sessionId);
    }
    
    /**
     * ??????
     */
    public void reset() {
        chatMemory.clear(sessionId);
        if (StringUtils.hasLength(systemPrompt)) {
            chatMemory.add(sessionId, new SystemMessage(systemPrompt));
        }
        agentState = AgentState.IDLE;
    }
    
    @Override
    public String toString() {
        return "JChatMindV1 {" +
                "name = " + name + ",\n" +
                "description = " + description + ",\n" +
                "systemPrompt = " + systemPrompt + "}";
    }
}
