/*
 * RAG???? -- ??????(Retrieval-Augmented Generation)
 * RAG????:?? ? embed()??? ? ??pgvector ? similaritySearch()????? ? ??AI
 * ??WebClient????Ollama?bge-m3??????(Embedding)
 * Ollama??:http://localhost:11434,???? ollama pull bge-m3 ????
 */
package com.kama.jchatmind.service.impl;

import com.kama.jchatmind.mapper.ChunkBgeM3Mapper;
import com.kama.jchatmind.model.entity.ChunkBgeM3;
import com.kama.jchatmind.service.RagService;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class RagServiceImpl implements RagService {

    // WebClient:Spring?HTTP???,??????Ollama API
    private final WebClient webClient;
    // ChunkBgeM3Mapper:??????,???????
    private final ChunkBgeM3Mapper chunkBgeM3Mapper;

    public RagServiceImpl(WebClient.Builder builder, ChunkBgeM3Mapper chunkBgeM3Mapper) {
        this.webClient = builder.baseUrl("http://localhost:11434").build();
        this.chunkBgeM3Mapper = chunkBgeM3Mapper;
    }

    // Ollama API???JSON??:{"embedding": [0.1, 0.2, ...]}
    @Data
    private static class EmbeddingResponse {
        private float[] embedding;
    }

    // ??Ollama /api/embeddings??,?????????(float??)
    // block() = ????HTTP????,??RAG????????
    private float[] doEmbed(String text) {
        EmbeddingResponse resp = webClient.post()// ? ? HTTP POST
                .uri("/api/embeddings")// ? Ollama ???
                .bodyValue(Map.of("model", "bge-m3", "prompt", text))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)// ? ?? JSON
                .block();// ? ??????
        Assert.notNull(resp, "Embedding response cannot be null");
        return resp.getEmbedding();
    }

    @Override
    public float[] embed(String text) {
        return doEmbed(text);
    }

    // ?????:???????? ? ?pgvector?? ? ???????Top 3???chunk
    @Override
    public List<String> similaritySearch(String kbId, String title) {
        String queryEmbedding = toPgVector(doEmbed(title));
        List<ChunkBgeM3> chunks = chunkBgeM3Mapper.similaritySearch(kbId, queryEmbedding, 3);
        return chunks.stream().map(ChunkBgeM3::getContent).toList();
    }

    // float[] ? PostgreSQL pgvector??:? [0.1,0.2,0.3]
    private String toPgVector(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            sb.append(v[i]);
            if (i < v.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}