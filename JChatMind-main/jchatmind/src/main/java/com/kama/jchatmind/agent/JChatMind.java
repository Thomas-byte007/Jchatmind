package com.kama.jchatmind.agent;

import com.kama.jchatmind.config.ChatClientRegistry;
import com.kama.jchatmind.converter.ChatMessageConverter;
import com.kama.jchatmind.message.SseMessage;
import com.kama.jchatmind.model.dto.ChatMessageDTO;
import com.kama.jchatmind.model.dto.KnowledgeBaseDTO;
import com.kama.jchatmind.model.response.CreateChatMessageResponse;
import com.kama.jchatmind.model.vo.ChatMessageVO;
import com.kama.jchatmind.service.ChatMessageFacadeService;
import com.kama.jchatmind.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * Agent智能体核心运行时 —— 实现Think-Act循环
 *
 * 小白必读：
 * Agent Loop = Think(思考) + Act(执行) 循环，直到任务完成或达到MAX_STEPS上限
 * Think：调用AI分析 → 决定是否需要调用工具
 * Act：执行AI决定的工具（查数据库、发邮件等）→ 把结果反馈给AI
 * ChatMemory：对话记忆窗口，只保留最近N条消息
 * SSE：通过SseService实时推送AI生成的内容给前端
 */
@Slf4j
public class JChatMind {

    private String agentId;
    private String name;
    private String description;
    private String systemPrompt;            // 系统提示词，定义AI角色和行为
    private ChatClient chatClient;          // 与AI模型通信的客户端
    private ChatClientRegistry chatClientRegistry; // 模型注册表（用于运行时降级）
    private String primaryModelName;        // 初始请求的模型名
    private AgentState agentState;          // 当前状态：IDLE/THINKING/EXECUTING/FINISHED/ERROR
    private List<ToolCallback> availableTools;     // Agent可调用的所有工具
    private List<KnowledgeBaseDTO> availableKbs;   // Agent可访问的知识库
    private ToolCallingManager toolCallingManager; // 工具调用管理器
    private ChatMemory chatMemory;          // 对话记忆（只保留最近消息）
    private String chatSessionId;
    private static final Integer MAX_STEPS = 20;            // 最大循环次数，防无限循环
    private static final Integer DEFAULT_MAX_MESSAGES = 20; // 记忆窗口大小
    private ChatOptions chatOptions;        // AI模型选项，关闭自动工具执行
    private SseService sseService;          // SSE推送服务
    private ChatMessageConverter chatMessageConverter;
    private ChatMessageFacadeService chatMessageFacadeService;
    private ChatResponse lastChatResponse;  // 最近一次AI响应
    private final List<ChatMessageDTO> pendingChatMessages = new ArrayList<>(); // 待推送消息队列

    public JChatMind() {}

    public JChatMind(AgentConfig config, AgentRuntimeConfig runtimeConfig) {
        config.validate();
        runtimeConfig.validate();

        this.agentId = config.getAgentId();
        this.name = config.getName();
        this.description = config.getDescription();
        this.systemPrompt = config.getSystemPrompt();
        this.chatSessionId = config.getChatSessionId();

        this.chatClient = runtimeConfig.getChatClient();
        this.chatClientRegistry = runtimeConfig.getChatClientRegistry();
        this.primaryModelName = runtimeConfig.getPrimaryModelName();
        this.sseService = runtimeConfig.getSseService();
        this.chatMessageFacadeService = runtimeConfig.getChatMessageFacadeService();
        this.chatMessageConverter = runtimeConfig.getChatMessageConverter();
        this.availableTools = runtimeConfig.getToolCallbacks();
        this.availableKbs = runtimeConfig.getKnowledgeBases();

        this.agentState = AgentState.IDLE;
        Integer maxMessages = config.getMaxMessages() != null
                ? config.getMaxMessages() : DEFAULT_MAX_MESSAGES;
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(maxMessages).build();
        this.chatMemory.add(this.chatSessionId, runtimeConfig.getMemory());
        if (StringUtils.hasLength(this.systemPrompt))
            this.chatMemory.add(this.chatSessionId, new SystemMessage(this.systemPrompt));
        this.chatOptions = DefaultToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false).build();
        this.toolCallingManager = ToolCallingManager.builder().build();
    }

    // 格式化输出工具调用的名称和参数到日志
    private void logToolCalls(List<AssistantMessage.ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) { log.info("\n\n[ToolCalling] 无工具调用"); return; }
        String logMessage = IntStream.range(0, toolCalls.size())
                .mapToObj(i -> String.format("[ToolCalling #%d]\n- name      : %s\n- arguments : %s",
                        i + 1, toolCalls.get(i).name(), toolCalls.get(i).arguments()))
                .collect(Collectors.joining("\n\n"));
        log.info("\n\n========== Tool Calling ==========\n{}\n=================================\n", logMessage);
    }

    // 保存消息到数据库 + 加入待推送队列
    // 处理AssistantMessage(AI回复)和ToolResponseMessage(工具结果)
    // 将Spring AI类型转换为自定义MetaData类型，确保JSON序列化字段名与前端一致
    private void saveMessage(Message message) {
        ChatMessageDTO.ChatMessageDTOBuilder builder = ChatMessageDTO.builder();
        if (message instanceof AssistantMessage assistantMessage) {
            // 转换 Spring AI ToolCall → 自定义 ToolInvocation
            List<ChatMessageDTO.ToolInvocation> toolInvocations = null;
            if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
                toolInvocations = assistantMessage.getToolCalls().stream()
                        .map(tc -> ChatMessageDTO.ToolInvocation.builder()
                                .id(tc.id()).type(tc.type()).name(tc.name()).arguments(tc.arguments())
                                .build())
                        .toList();
            }
            ChatMessageDTO chatMessageDTO = builder
                    .role(ChatMessageDTO.RoleType.ASSISTANT).content(assistantMessage.getText())
                    .sessionId(this.chatSessionId)
                    .metadata(ChatMessageDTO.MetaData.builder().toolCalls(toolInvocations).build())
                    .build();
            CreateChatMessageResponse resp = chatMessageFacadeService.createChatMessage(chatMessageDTO);
            chatMessageDTO.setId(resp.getChatMessageId());
            pendingChatMessages.add(chatMessageDTO);
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
                // 转换 Spring AI ToolResponse → 自定义 ToolResult
                ChatMessageDTO.ToolResult toolResult = ChatMessageDTO.ToolResult.builder()
                        .id(toolResponse.id()).name(toolResponse.name())
                        .responseData(toolResponse.responseData())
                        .build();
                ChatMessageDTO chatMessageDTO = builder
                        .role(ChatMessageDTO.RoleType.TOOL).content(toolResponse.responseData())
                        .sessionId(this.chatSessionId)
                        .metadata(ChatMessageDTO.MetaData.builder().toolResponse(toolResult).build())
                        .build();
                CreateChatMessageResponse resp = chatMessageFacadeService.createChatMessage(chatMessageDTO);
                chatMessageDTO.setId(resp.getChatMessageId());
                pendingChatMessages.add(chatMessageDTO);
            }
        } else {
            throw new IllegalArgumentException("不支持的 Message 类型: " + message.getClass().getName());
        }
    }

    // 刷新待推送队列：转为ChatMessageVO → 构建SseMessage → SSE推送给前端
    private void refreshPendingMessages() {
        for (ChatMessageDTO message : pendingChatMessages) {
            ChatMessageVO vo = chatMessageConverter.toVO(message);
            SseMessage sseMessage = SseMessage.builder()
                    .type(SseMessage.Type.AI_GENERATED_CONTENT)
                    .payload(SseMessage.Payload.builder().message(vo).build())
                    .metadata(SseMessage.Metadata.builder().chatMessageId(message.getId()).build())
                    .build();
            sseService.send(this.chatSessionId, sseMessage);
        }
        pendingChatMessages.clear();
    }

    /*
     * Think（思考）阶段 —— Agent Loop的核心步骤
     * 加载对话记忆 → 注入决策提示词 → 调用AI → 解析工具调用 → 保存AI回复
     * 支持运行时模型降级：主模型调用失败时自动切换备用模型重试
     * @return true=需要执行工具，false=直接结束
     */
    private boolean think() {
        String thinkPrompt = """
                现在你是一个智能的的具体「决策模块」
                请根据当前对话上下文，决定下一步的动作。
                                
                【额外信息】
                - 你目前拥有的知识库列表以及描述：%s
                - 如果有缺失的上下文时，优先从知识库中进行搜索
                """.formatted(this.availableKbs);

        // 最多重试一次降级（主模型失败 → 备用模型）
        int maxAttempts = (chatClientRegistry != null) ? 2 : 1;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                Prompt prompt = Prompt.builder()
                        .chatOptions(this.chatOptions)
                        .messages(this.chatMemory.get(this.chatSessionId)).build();
                this.lastChatResponse = this.chatClient
                        .prompt(prompt).system(thinkPrompt)
                        .toolCallbacks(this.availableTools.toArray(new ToolCallback[0]))
                        .call().chatClientResponse().chatResponse();
                Assert.notNull(lastChatResponse, "Last chat client response cannot be null");
                AssistantMessage output = this.lastChatResponse.getResult().getOutput();
                List<AssistantMessage.ToolCall> toolCalls = output.getToolCalls();
                saveMessage(output);
                refreshPendingMessages();
                logToolCalls(toolCalls);
                return !toolCalls.isEmpty();
            } catch (Exception e) {
                if (attempt == 0 && chatClientRegistry != null) {
                    // 主模型失败，尝试降级到备用模型
                    String currentModel = chatClientRegistry.getResolvedModelName(primaryModelName, chatClient);
                    ChatClient fallback = tryGetFallbackClient(currentModel);
                    if (fallback != null) {
                        String fallbackModel = chatClientRegistry.getResolvedModelName(primaryModelName, fallback);
                        log.warn("Think阶段模型调用失败(model={})，降级到备用模型: {}", currentModel, fallbackModel);
                        this.chatClient = fallback;
                        // 通知前端模型降级
                        sseService.send(this.chatSessionId, SseMessage.builder()
                                .type(SseMessage.Type.AI_PLANNING)
                                .payload(SseMessage.Payload.builder()
                                        .statusText("模型降级：" + currentModel + " 调用失败，已切换到 " + fallbackModel)
                                        .build())
                                .build());
                        continue; // 重试
                    }
                }
                throw e; // 无备用模型或已是备用模型仍失败，抛出异常
            }
        }
        return false; // 不会到达，但编译器需要
    }

    /**
     * 获取备用模型ChatClient（按 FALLBACK_ORDER 优先级逐个尝试，排除已失败的模型）
     */
    private ChatClient tryGetFallbackClient(String failedModel) {
        if (chatClientRegistry == null) return null;
        for (String model : chatClientRegistry.getFallbackOrder()) {
            if (model.equals(failedModel)) continue;
            try {
                ChatClient candidate = chatClientRegistry.get(model);
                log.info("尝试备用模型: {}", model);
                return candidate;
            } catch (Exception e) {
                log.warn("备用模型 {} 不可用: {}", model, e.getMessage());
            }
        }
        return null;
    }

    // Execute（执行）阶段 —— 执行Think阶段决定的工具调用
    // 调用ToolCallingManager执行工具 → 收集结果 → 更新记忆 → 检查是否调用了terminate
    private void execute() {
        Assert.notNull(this.lastChatResponse, "Last chat client response cannot be null");
        if (!this.lastChatResponse.hasToolCalls()) return;
        Prompt prompt = Prompt.builder()
                .messages(this.chatMemory.get(this.chatSessionId))
                .chatOptions(this.chatOptions).build();
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, this.lastChatResponse);
        this.chatMemory.clear(this.chatSessionId);
        this.chatMemory.add(this.chatSessionId, toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult
                .conversationHistory().get(toolExecutionResult.conversationHistory().size() - 1);//从“工具执行结果”对象的对话历史里，翻到最后一条记录，把它当作“工具响应消息”取出来。
        log.info("工具调用结果：{}", toolResponseMessage.getResponses().stream()
                .map(resp -> "工具" + resp.name() + "的返回结果为：" + resp.responseData())
                .collect(Collectors.joining("\n")));
        saveMessage(toolResponseMessage);
        refreshPendingMessages();
        if (toolResponseMessage.getResponses().stream()
                .anyMatch(resp -> resp.name().equals("terminate"))) {
            this.agentState = AgentState.FINISHED;
            log.info("任务结束");
        }
    }

    // Step（单步执行） —— Think + Act，不需要工具则结束
    private void step() {
        if (think()) execute();
        else agentState = AgentState.FINISHED;
    }

    // Run（入口方法） —— 循环执行Step，最多MAX_STEPS次
    public void run() {
        if (agentState != AgentState.IDLE) throw new IllegalStateException("Agent is not idle");
        try {
            for (int i = 0; i < MAX_STEPS && agentState != AgentState.FINISHED; i++) {
                step();
                if (i + 1 >= MAX_STEPS) { agentState = AgentState.FINISHED; log.warn("Max steps reached"); }
            }
            agentState = AgentState.FINISHED;
            // 推送完成状态给前端
            SseMessage doneMessage = SseMessage.builder()
                    .type(SseMessage.Type.AI_DONE)
                    .payload(SseMessage.Payload.builder().done(true).statusText("任务完成").build())
                    .build();
            sseService.send(this.chatSessionId, doneMessage);
        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("Agent执行异常: {}", e.getMessage(), e);
            // 通过SSE推送错误信息给前端，让用户感知到问题
            try {
                SseMessage errorMessage = SseMessage.builder()
                        .type(SseMessage.Type.AI_ERROR)
                        .payload(SseMessage.Payload.builder()
                                .done(true)
                                .statusText("Agent执行出错: " + e.getMessage())
                                .build())
                        .build();
                sseService.send(this.chatSessionId, errorMessage);
            } catch (Exception sseError) {
                log.error("推送错误消息失败", sseError);
            }
            throw new RuntimeException("Error running agent", e);
        }
    }

    @Override
    public String toString() {
        return "JChatMind {name=" + name + ", description=" + description +
                ", agentId=" + agentId + ", systemPrompt=" + systemPrompt + "}";
    }
}