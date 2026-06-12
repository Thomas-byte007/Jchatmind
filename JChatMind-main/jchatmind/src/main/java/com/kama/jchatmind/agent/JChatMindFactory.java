/*
 * Agent?? -- ??????JChatMind??(???? + ????)
 * ????:???? ? ???? ? ?????&?? ? ???? ? ??Agent
 * ???????????,Spring??????? agentFactory.create(agentId, sessionId) ???????Agent
 */
package com.kama.jchatmind.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kama.jchatmind.agent.tools.Tool;
import com.kama.jchatmind.config.ChatClientRegistry;
import com.kama.jchatmind.converter.AgentConverter;
import com.kama.jchatmind.converter.ChatMessageConverter;
import com.kama.jchatmind.converter.KnowledgeBaseConverter;
import com.kama.jchatmind.mapper.AgentMapper;
import com.kama.jchatmind.mapper.KnowledgeBaseMapper;
import com.kama.jchatmind.model.dto.AgentDTO;
import com.kama.jchatmind.model.dto.ChatMessageDTO;
import com.kama.jchatmind.model.dto.KnowledgeBaseDTO;
import com.kama.jchatmind.model.entity.Agent;
import com.kama.jchatmind.model.entity.KnowledgeBase;
import com.kama.jchatmind.service.ChatMessageFacadeService;
import com.kama.jchatmind.service.SseService;
import com.kama.jchatmind.service.ToolFacadeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ???????JChatMind Agent??,?????????????
 * @Component:Spring?????Bean,?????@Autowired????
 */
@Component
public class JChatMindFactory {

    // ????? -- ????????,????
    private static final Logger log = LoggerFactory.getLogger(JChatMindFactory.class);

    // ChatClient??? -- ??????????AI????
    private final ChatClientRegistry chatClientRegistry;
    // SSE?????? -- ???????AI?????
    private final SseService sseService;
    // Agent????? -- ??Agent????
    private final AgentMapper agentMapper;
    // Agent??? -- Entity?DTO??,??JSON??????
    private final AgentConverter agentConverter;
    // ???????? -- ????Agent??????
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    // ?????? -- Entity?DTO??
    private final KnowledgeBaseConverter knowledgeBaseConverter;
    // ?????? -- ??????(FIXED)?????(OPTIONAL)?Agent??
    private final ToolFacadeService toolFacadeService;
    // ???? -- ????????
    private final ChatMessageFacadeService chatMessageFacadeService;
    // ????? -- DTO?VO??
    private final ChatMessageConverter chatMessageConverter;

    /**
     * ????? -- Spring????????,final???????
     */
    public JChatMindFactory(
            ChatClientRegistry chatClientRegistry,
            SseService sseService,
            AgentMapper agentMapper,
            AgentConverter agentConverter,
            KnowledgeBaseMapper knowledgeBaseMapper,
            KnowledgeBaseConverter knowledgeBaseConverter,
            ToolFacadeService toolFacadeService,
            ChatMessageFacadeService chatMessageFacadeService,
            ChatMessageConverter chatMessageConverter
    ) {
        this.chatClientRegistry = chatClientRegistry;
        this.sseService = sseService;
        this.agentMapper = agentMapper;
        this.agentConverter = agentConverter;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeBaseConverter = knowledgeBaseConverter;
        this.toolFacadeService = toolFacadeService;
        this.chatMessageFacadeService = chatMessageFacadeService;
        this.chatMessageConverter = chatMessageConverter;
    }

    // =========================================================================
    // ??Agent??(?????)
    // =========================================================================

    /**
     * ????? -- ?????????Agent??
     * 7???:???? ? ???? ? ???? ? ????? ? ???? ? ???? ? ?????
     * @param agentId Agent???ID
     * @param chatSessionId ??ID,??????
     * @return ???JChatMind??,?? .run() ????
     */
    public JChatMind create(String agentId, String chatSessionId) {
        Agent agent = loadAgent(agentId);
        AgentDTO agentConfig = toAgentConfig(agent);
        List<Message> memory = loadMemory(chatSessionId, agentConfig);
        List<KnowledgeBaseDTO> knowledgeBases = resolveRuntimeKnowledgeBases(agentConfig);
        List<Tool> runtimeTools = resolveRuntimeTools(agentConfig);
        List<ToolCallback> toolCallbacks = buildToolCallbacks(runtimeTools);
        return buildAgentRuntime(agent, agentConfig, memory, knowledgeBases, toolCallbacks, chatSessionId);
    }

    // =========================================================================
    // ????:????????
    // =========================================================================

    // ??????Agent??,selectById?MyBatis-Plus????
    private Agent loadAgent(String agentId) {
        return agentMapper.selectById(agentId);
    }

    // ????????????DTO,??????JSON????
    private AgentDTO toAgentConfig(Agent agent) {
        try {
            return agentConverter.toDTO(agent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("?? Agent ????", e);
        }
    }

    /*
     * ??????????,???Spring AI?Message??
     * ??4???:SYSTEM(?????)/USER(????)/ASSISTANT(AI??)/TOOL(????)
     * ??????????,??AI??????????;TOOL?????????ID
     */
    private List<Message> loadMemory(String chatSessionId, AgentDTO agentConfig) {
        int messageLength = agentConfig.getChatOptions().getMessageLength();
        List<ChatMessageDTO> chatMessages =
                chatMessageFacadeService.getChatMessagesBySessionIdRecently(chatSessionId, messageLength);
        List<Message> memory = new ArrayList<>();
        for (ChatMessageDTO chatMessageDTO : chatMessages) {
            switch (chatMessageDTO.getRole()) {
                case SYSTEM:
                    if (!StringUtils.hasLength(chatMessageDTO.getContent())) continue;
                    memory.add(0, new SystemMessage(chatMessageDTO.getContent()));
                    break;
                case USER:
                    if (!StringUtils.hasLength(chatMessageDTO.getContent())) continue;
                    memory.add(new UserMessage(chatMessageDTO.getContent()));
                    break;
                case ASSISTANT:
                    // ??? ToolInvocation ? Spring AI ToolCall
                    List<AssistantMessage.ToolCall> springToolCalls = null;
                    if (chatMessageDTO.getMetadata() != null && chatMessageDTO.getMetadata().getToolCalls() != null) {
                        springToolCalls = chatMessageDTO.getMetadata().getToolCalls().stream()
                                .map(ti -> new AssistantMessage.ToolCall(ti.getId(), ti.getType(), ti.getName(), ti.getArguments()))
                                .toList();
                    }
                    memory.add(AssistantMessage.builder()
                            .content(chatMessageDTO.getContent())
                            .toolCalls(springToolCalls)
                            .build());
                    break;
                case TOOL:
                    // ??? ToolResult ? Spring AI ToolResponse
                    ToolResponseMessage.ToolResponse springToolResponse = null;
                    if (chatMessageDTO.getMetadata() != null && chatMessageDTO.getMetadata().getToolResponse() != null) {
                        ChatMessageDTO.ToolResult tr = chatMessageDTO.getMetadata().getToolResponse();
                        springToolResponse = new ToolResponseMessage.ToolResponse(tr.getId(), tr.getName(), tr.getResponseData());
                    }
                    memory.add(ToolResponseMessage.builder()
                            .responses(springToolResponse != null ? List.of(springToolResponse) : List.of())
                            .build());
                    break;
                default:
                    log.error("???? Message ??: {}, content = {}",
                            chatMessageDTO.getRole().getRole(), chatMessageDTO.getContent());
                    throw new IllegalStateException("???? Message ??");
            }
        }
        return memory;
    }

    /*
     * ??????allowedKbs??,????Agent???????
     * ?????RAG(??????):AI??????????????,?????
     */
    private List<KnowledgeBaseDTO> resolveRuntimeKnowledgeBases(AgentDTO agentConfig) {
        List<String> allowedKbIds = agentConfig.getAllowedKbs();
        if (allowedKbIds == null || allowedKbIds.isEmpty()) return Collections.emptyList();
        List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectByIdBatch(allowedKbIds);
        if (knowledgeBases.isEmpty()) return Collections.emptyList();
        List<KnowledgeBaseDTO> kbDTOs = new ArrayList<>();
        try {
            for (KnowledgeBase knowledgeBase : knowledgeBases) {
                kbDTOs.add(knowledgeBaseConverter.toDTO(knowledgeBase));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return kbDTOs;
    }

    /*
     * ??????Agent???????
     * ????(FIXED):??Agent????(?directAnswer?terminate)
     * ????(OPTIONAL):??Agent???allowedTools????(???????????)
     */
    private List<Tool> resolveRuntimeTools(AgentDTO agentConfig) {
        List<Tool> runtimeTools = new ArrayList<>(toolFacadeService.getFixedTools());
        List<String> allowedToolNames = agentConfig.getAllowedTools();
        if (allowedToolNames == null || allowedToolNames.isEmpty()) return runtimeTools;
        Map<String, Tool> optionalToolMap = toolFacadeService.getOptionalTools()
                .stream().collect(Collectors.toMap(Tool::getName, Function.identity()));
        for (String toolName : allowedToolNames) {
            Tool tool = optionalToolMap.get(toolName);
            if (tool != null) runtimeTools.add(tool);
        }
        return runtimeTools;
    }

    /*
     * ?Tool?????Spring AI????ToolCallback
     * Tool????????,ToolCallback?Spring AI?????
     * ??MethodToolCallbackProvider??@Tool????,??????
     */
    private List<ToolCallback> buildToolCallbacks(List<Tool> runtimeTools) {
        List<ToolCallback> callbacks = new ArrayList<>();
        for (Tool tool : runtimeTools) {
            Object target = resolveToolTarget(tool);//??"??"?? :?????????,?????????;??????,?????
            ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                    .toolObjects(target).build().getToolCallbacks();
            callbacks.addAll(Arrays.asList(toolCallbacks));
        }
        return callbacks;
    }

    /*
     * ???????????,??AOP??(??"??"?? :?????????,?????????;??????,?????)
     * Spring?@Service/@Component??????,????????????@Tool??
     */
    private Object resolveToolTarget(Tool tool) {
        try {
            if (AopUtils.isAopProxy(tool)) {
                // ???????(Instance),??????(Class)
                return ((Advised) tool).getTargetSource().getTarget();
            }
            return tool;
        } catch (Exception e) {
            throw new IllegalStateException("??????????: " + tool.getName(), e);
        }
    }

    // ??JChatMind??,?????????????
    private JChatMind buildAgentRuntime(
            Agent agent,
            AgentDTO agentConfig,
            List<Message> memory,
            List<KnowledgeBaseDTO> knowledgeBases,
            List<ToolCallback> toolCallbacks,
            String chatSessionId
    ) {
        // ??????:?????????fallback?????
        ChatClient chatClient = chatClientRegistry.getWithFallback(agent.getModel());
        String resolvedModel = chatClientRegistry.getResolvedModelName(agent.getModel(), chatClient);

        // ???????,?????????
        if (!resolvedModel.equals(agent.getModel())) {
            log.warn("????:Agent '{}' ???? '{}' ???,???? '{}'",
                    agent.getName(), agent.getModel(), resolvedModel);
            sseService.send(chatSessionId, com.kama.jchatmind.message.SseMessage.builder()
                    .type(com.kama.jchatmind.message.SseMessage.Type.AI_PLANNING)
                    .payload(com.kama.jchatmind.message.SseMessage.Payload.builder()
                            .statusText("????:??? " + agent.getModel() + " ???,???? " + resolvedModel)
                            .build())
                    .build());
        }

        AgentConfig config = AgentConfig.builder()
                .agentId(agent.getId())
                .name(agent.getName())
                .description(agent.getDescription())
                .systemPrompt(agent.getSystemPrompt())
                .maxMessages(agentConfig.getChatOptions().getMessageLength())
                .chatSessionId(chatSessionId)
                .build();

        AgentRuntimeConfig runtimeConfig = AgentRuntimeConfig.builder()
                .chatClient(chatClient)
                .chatClientRegistry(chatClientRegistry)
                .primaryModelName(agent.getModel())
                .memory(memory)
                .toolCallbacks(toolCallbacks)
                .knowledgeBases(knowledgeBases)
                .sseService(sseService)
                .chatMessageFacadeService(chatMessageFacadeService)
                .chatMessageConverter(chatMessageConverter)
                .build();

        return new JChatMind(config, runtimeConfig);
    }
}