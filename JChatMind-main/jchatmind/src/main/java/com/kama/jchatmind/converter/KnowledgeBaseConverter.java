package com.kama.jchatmind.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.model.dto.KnowledgeBaseDTO;
import com.kama.jchatmind.model.entity.KnowledgeBase;
import com.kama.jchatmind.model.request.CreateKnowledgeBaseRequest;
import com.kama.jchatmind.model.request.UpdateKnowledgeBaseRequest;
import com.kama.jchatmind.model.vo.KnowledgeBaseVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/*
 * ?????? -- Entity ? DTO ? VO ? Request ?????
 * ??AgentConverter???,??????????,????Converter????
 * ObjectMapper:??metadata??(JSON) ? Java??????
 */
@Component
@AllArgsConstructor
public class KnowledgeBaseConverter {

    private final ObjectMapper objectMapper;

    // DTO ? Entity:?????????JSON???
    public KnowledgeBase toEntity(KnowledgeBaseDTO dto) throws JsonProcessingException {
        Assert.notNull(dto, "KnowledgeBaseDTO cannot be null");
        return KnowledgeBase.builder()
                .id(dto.getId()).name(dto.getName()).description(dto.getDescription())
                .metadata(dto.getMetadata() != null ? objectMapper.writeValueAsString(dto.getMetadata()) : null)
                .createdAt(dto.getCreatedAt()).updatedAt(dto.getUpdatedAt()).build();
    }

    // Entity ? DTO:JSON?????????????
    public KnowledgeBaseDTO toDTO(KnowledgeBase entity) throws JsonProcessingException {
        Assert.notNull(entity, "KnowledgeBase cannot be null");
        return KnowledgeBaseDTO.builder()
                .id(entity.getId()).name(entity.getName()).description(entity.getDescription())
                .metadata(entity.getMetadata() != null
                        ? objectMapper.readValue(entity.getMetadata(), KnowledgeBaseDTO.MetaData.class) : null)
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    // DTO ? VO:??????????(id?name?description)
    public KnowledgeBaseVO toVO(KnowledgeBaseDTO dto) {
        return KnowledgeBaseVO.builder()
                .id(dto.getId()).name(dto.getName()).description(dto.getDescription()).build();
    }

    // Entity ? VO:????
    public KnowledgeBaseVO toVO(KnowledgeBase entity) throws JsonProcessingException {
        return toVO(toDTO(entity));
    }

    // CreateRequest ? DTO:??????????????
    public KnowledgeBaseDTO toDTO(CreateKnowledgeBaseRequest request) {
        Assert.notNull(request, "CreateKnowledgeBaseRequest cannot be null");
        return KnowledgeBaseDTO.builder()
                .name(request.getName()).description(request.getDescription()).build();
    }

    // ????null??:name?description?metadata
    public void updateDTOFromRequest(KnowledgeBaseDTO dto, UpdateKnowledgeBaseRequest request) {
        Assert.notNull(dto, "KnowledgeBaseDTO cannot be null");
        Assert.notNull(request, "UpdateKnowledgeBaseRequest cannot be null");
        if (request.getName() != null) dto.setName(request.getName());
        if (request.getDescription() != null) dto.setDescription(request.getDescription());
    }
}