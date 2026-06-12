package com.kama.jchatmind.agent.examples;

import com.kama.jchatmind.agent.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * V2 ??:????????(ReAct ??)
 * ? V1 ???,??????,?? ReAct(Reasoning + Acting)??
 * 
 * ????:
 * - ?? V1 ?????
 * - ??????(Tool Calling)
 * - ?? think-execute ??
 * - ???????????
 */
@Slf4j
public class JChatMindV2 extends JChatMindV1 {
    
    // ???????
    protected List<ToolCallback> availableTools;
    
    // ???????
    protected ToolCallingManager toolCallingManager;
    
    // ChatOptions
    protected ChatOptions chatOptions;
    
    // ????? ChatResponse
    protected ChatResponse lastChatResponse;
    
    // ??????
    private static final Integer MAX_STEPS = 20;
    
    public JChatMindV2() {
        super();
    }
    
    public JChatMindV2(String name,
                      String description,
                      String systemPrompt,
                      org.springframework.ai.chat.client.ChatClient chatClient,
                      Integer maxMessages,
                      String sessionId,
                      List<ToolCallback> availableTools) {
        super(name, description, systemPrompt, chatClient, maxMessages, sessionId);
        this.availableTools = availableTools;
        
        // ?? SpringAI ???????????????
        // ?????????????????
        this.chatOptions = DefaultToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
        
        // ??????????
        this.toolCallingManager = ToolCallingManager.builder().build();
    }
    
    /**
     * ????????
     */
    protected void logToolCalls(List<AssistantMessage.ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            log.info("\n\n[ToolCalling] ?????");
            return;
        }
        String logMessage = IntStream.range(0, toolCalls.size())
                .mapToObj(i -> {
                    AssistantMessage.ToolCall call = toolCalls.get(i);
                    return String.format(
                            "[ToolCalling #%d]\n- name      : %s\n- arguments : %s",
                            i + 1,
                            call.name(),
                            call.arguments()
                    );
                })
                .collect(Collectors.joining("\n\n"));
        log.info("\n\n========== Tool Calling ==========\n{}\n=================================\n", logMessage);
    }
    
    /**
     * Think ??:?????????????
     * @return ???????,?? true;???? false
     */
    protected boolean think() {
        String thinkPrompt = """
                ????????????????
                ??????????,?????????
                ?????????????,?????????
                """;
        
        // ?????
        Prompt prompt = Prompt.builder()
                .chatOptions(this.chatOptions)
                .messages(chatMemory.get(sessionId))
                .build();
        
        // ?? LLM,??????
        this.lastChatResponse = chatClient
                .prompt(prompt)
                .system(thinkPrompt)
                .toolCallbacks(availableTools != null ? availableTools.toArray(new ToolCallback[0]) : new ToolCallback[0])
                .call()
                .chatClientResponse()
                .chatResponse();
        
        Assert.notNull(lastChatResponse, "Last chat response cannot be null");
        
        AssistantMessage output = this.lastChatResponse
                .getResult()
                .getOutput();
        
        List<AssistantMessage.ToolCall> toolCalls = output.getToolCalls();
        
        // ????????
        logToolCalls(toolCalls);
        
        // ????????,? AI ???????
        // ???????,?? execute() ?????,????????????
        if (toolCalls.isEmpty()) {
            chatMemory.add(sessionId, output);
        }
        
        // ?????????,???????
        return !toolCalls.isEmpty();
    }
    
    /**
     * Execute ??:??????
     */
    protected void execute() {
        Assert.notNull(this.lastChatResponse, "Last chat response cannot be null");
        
        if (!this.lastChatResponse.hasToolCalls()) {
            return;
        }
        
        // ?????,?????????
        // ??:?? chatMemory ?????? tool_calls ? AssistantMessage
        // ToolCallingManager.executeToolCalls() ?? lastChatResponse ??? AssistantMessage
        // ???????????
        Prompt prompt = Prompt.builder()
                .messages(chatMemory.get(sessionId))
                .chatOptions(this.chatOptions)
                .build();
        
        // ??????
        // ToolCallingManager.executeToolCalls() ?:
        // 1. ? prompt ??????????
        // 2. ? lastChatResponse ????? tool_calls ? AssistantMessage,????????
        // 3. ??????
        // 4. ?? ToolResponseMessage ?????
        // toolExecutionResult.conversationHistory() ??????????:
        // - ???????
        // - ?? tool_calls ? AssistantMessage
        // - ToolResponseMessage(??????)
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, this.lastChatResponse);
        
        // ????:?????,????????(????????)
        // ?????????,?? tool_calls ? AssistantMessage ???????? ToolResponseMessage
        chatMemory.clear(sessionId);
        chatMemory.add(sessionId, toolExecutionResult.conversationHistory());
        
        // ????????
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult
                .conversationHistory()
                .get(toolExecutionResult.conversationHistory().size() - 1);
        
        // ????????
        String result = toolResponseMessage.getResponses()
                .stream()
                .map(resp -> "?? " + resp.name() + " ??????:" + resp.responseData())
                .collect(Collectors.joining("\n"));
        
//        log.info("??????:{}", result);
        
        // ???????????
        if (toolResponseMessage.getResponses()
                .stream()
                .anyMatch(resp -> resp.name().equals("terminate"))) {
            this.agentState = AgentState.FINISHED;
            log.info("????");
        }
    }
    
    /**
     * ????:think -> execute(????)
     */
    protected void step() {
        if (think()) {
            // ?????,????
            execute();
        } else {
            // ??????,????
            agentState = AgentState.FINISHED;
        }
    }
    
    /**
     * ?? Agent:??????,?? think-execute ??
     */
    @Override
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
            
            // ?? think-execute ??
            for (int i = 0; i < MAX_STEPS && agentState != AgentState.FINISHED; i++) {
                step();
                if (i >= MAX_STEPS - 1) {
                    agentState = AgentState.FINISHED;
                    log.warn("???????,?? Agent");
                }
            }
            
            // ????? AI ??
            List<Message> history = chatMemory.get(sessionId);
            String aiResponse = "";
            for (int i = history.size() - 1; i >= 0; i--) {
                Message msg = history.get(i);
                if (msg instanceof AssistantMessage) {
                    aiResponse = ((AssistantMessage) msg).getText();
                    break;
                }
            }
            
//            log.info("????: {}", userInput);
//            log.info("AI ??: {}", aiResponse);
            
            agentState = AgentState.FINISHED;

            return aiResponse;
        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("Agent ?????????", e);
            throw new RuntimeException("Agent ?????????", e);
        } finally {
            // ??????????
            agentState = AgentState.IDLE;
        }
    }
}
