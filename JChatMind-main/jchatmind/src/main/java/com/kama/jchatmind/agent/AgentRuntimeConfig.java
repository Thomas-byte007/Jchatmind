package com.kama.jchatmind.agent;

import com.kama.jchatmind.config.ChatClientRegistry;
import com.kama.jchatmind.converter.ChatMessageConverter;
import com.kama.jchatmind.model.dto.KnowledgeBaseDTO;
import com.kama.jchatmind.service.ChatMessageFacadeService;
import com.kama.jchatmind.service.SseService;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;

import java.util.List;

@Data
@Builder
public class AgentRuntimeConfig {

    private ChatClient chatClient;
    private ChatClientRegistry chatClientRegistry;
    private String primaryModelName;
    private List<Message> memory;
    private List<ToolCallback> toolCallbacks;
    private List<KnowledgeBaseDTO> knowledgeBases;
    private SseService sseService;
    private ChatMessageFacadeService chatMessageFacadeService;
    private ChatMessageConverter chatMessageConverter;

    public void validate() {
        Assert.notNull(chatClient, "chatClient ????");
        Assert.notNull(sseService, "sseService ????");
        Assert.notNull(chatMessageFacadeService, "chatMessageFacadeService ????");
        Assert.notNull(chatMessageConverter, "chatMessageConverter ????");
    }
}
