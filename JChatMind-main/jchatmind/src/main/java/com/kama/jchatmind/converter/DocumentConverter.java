package com.kama.jchatmind.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.model.dto.DocumentDTO;
import com.kama.jchatmind.model.entity.Document;
import com.kama.jchatmind.model.request.CreateDocumentRequest;
import com.kama.jchatmind.model.request.UpdateDocumentRequest;
import com.kama.jchatmind.model.vo.DocumentVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/*
 * ????? -- Entity ? DTO ? VO ? Request ?????
 * Document????????(??????????),?????????????
 * metadata???ObjectMapper??JSON ?? Java??????
 */
@Component
@AllArgsConstructor
public class DocumentConverter {

    private final ObjectMapper objectMapper;

    // DTO ? Entity:?????????JSON???
    public Document toEntity(DocumentDTO dto) throws JsonProcessingException {
        Assert.notNull(dto, "DocumentDTO cannot be null");
        return Document.builder()
                .id(dto.getId()).kbId(dto.getKbId()).filename(dto.getFilename())
                .filetype(dto.getFiletype()).size(dto.getSize())
                .metadata(dto.getMetadata() != null ? objectMapper.writeValueAsString(dto.getMetadata()) : null)
                .createdAt(dto.getCreatedAt()).updatedAt(dto.getUpdatedAt()).build();
    }

    // Entity ? DTO:JSON?????????????
    public DocumentDTO toDTO(Document entity) throws JsonProcessingException {
        Assert.notNull(entity, "Document cannot be null");
        return DocumentDTO.builder()
                .id(entity.getId()).kbId(entity.getKbId()).filename(entity.getFilename())
                .filetype(entity.getFiletype()).size(entity.getSize())
                .metadata(entity.getMetadata() != null
                        ? objectMapper.readValue(entity.getMetadata(), DocumentDTO.MetaData.class) : null)
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    // DTO ? VO:?????,??metadata????
    public DocumentVO toVO(DocumentDTO dto) {
        return DocumentVO.builder()
                .id(dto.getId()).kbId(dto.getKbId()).filename(dto.getFilename())
                .filetype(dto.getFiletype()).size(dto.getSize()).build();
    }

    // Entity ? VO:????
    public DocumentVO toVO(Document entity) throws JsonProcessingException {
        return toVO(toDTO(entity));
    }

    // CreateRequest ? DTO:??kbId??????????
    public DocumentDTO toDTO(CreateDocumentRequest request) {
        Assert.notNull(request, "CreateDocumentRequest cannot be null");
        Assert.notNull(request.getKbId(), "KbId cannot be null");
        return DocumentDTO.builder()
                .kbId(request.getKbId()).filename(request.getFilename())
                .filetype(request.getFiletype()).size(request.getSize()).build();
    }

    // ????null??:filename?filetype?size?id?kbId???
    public void updateDTOFromRequest(DocumentDTO dto, UpdateDocumentRequest request) {
        Assert.notNull(dto, "DocumentDTO cannot be null");
        Assert.notNull(request, "UpdateDocumentRequest cannot be null");
        if (request.getFilename() != null) dto.setFilename(request.getFilename());
        if (request.getFiletype() != null) dto.setFiletype(request.getFiletype());
        if (request.getSize() != null) dto.setSize(request.getSize());
    }
}