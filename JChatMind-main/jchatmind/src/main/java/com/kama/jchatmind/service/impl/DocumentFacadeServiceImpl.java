package com.kama.jchatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kama.jchatmind.converter.DocumentConverter;
import com.kama.jchatmind.exception.BizException;
import com.kama.jchatmind.mapper.DocumentMapper;
import com.kama.jchatmind.model.dto.DocumentDTO;
import com.kama.jchatmind.model.entity.Document;
import com.kama.jchatmind.model.request.CreateDocumentRequest;
import com.kama.jchatmind.model.request.UpdateDocumentRequest;
import com.kama.jchatmind.model.response.CreateDocumentResponse;
import com.kama.jchatmind.model.response.GetDocumentsResponse;
import com.kama.jchatmind.model.vo.DocumentVO;
import com.kama.jchatmind.mapper.ChunkBgeM3Mapper;
import com.kama.jchatmind.model.entity.ChunkBgeM3;
import com.kama.jchatmind.service.DocumentFacadeService;
import com.kama.jchatmind.service.DocumentStorageService;
import com.kama.jchatmind.service.MarkdownParserService;
import com.kama.jchatmind.service.RagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * ???????? -- ????CRUD??????Markdown???RAG???
 * ????:???? ? ???? ? ???? ? Markdown?? ? ????chunks ? ??
 * @Slf4j:Lombok???? log ??,??????
 * MultipartFile:Spring???????,???FormData??
 */
@Service
@AllArgsConstructor
@Slf4j
public class DocumentFacadeServiceImpl implements DocumentFacadeService {

    private final DocumentMapper documentMapper;
    private final DocumentConverter documentConverter;
    private final DocumentStorageService documentStorageService;  // ????
    private final MarkdownParserService markdownParserService;    // Markdown??
    private final RagService ragService;                          // ?????
    private final ChunkBgeM3Mapper chunkBgeM3Mapper;              // ??????

    @Override
    public GetDocumentsResponse getDocuments() {
        List<Document> documents = documentMapper.selectAll();
        List<DocumentVO> result = new ArrayList<>();
        for (Document document : documents) {
            try { result.add(documentConverter.toVO(document)); }
            catch (JsonProcessingException e) { throw new RuntimeException(e); }
        }
        return GetDocumentsResponse.builder().documents(result.toArray(new DocumentVO[0])).build();
    }

    @Override
    public GetDocumentsResponse getDocumentsByKbId(String kbId) {
        List<Document> documents = documentMapper.selectByKbId(kbId);
        List<DocumentVO> result = new ArrayList<>();
        for (Document document : documents) {
            try { result.add(documentConverter.toVO(document)); }
            catch (JsonProcessingException e) { throw new RuntimeException(e); }
        }
        return GetDocumentsResponse.builder().documents(result.toArray(new DocumentVO[0])).build();
    }

    // ??????(?????)
    @Override
    public CreateDocumentResponse createDocument(CreateDocumentRequest request) {
        try {
            DocumentDTO dto = documentConverter.toDTO(request);
            Document document = documentConverter.toEntity(dto);
            LocalDateTime now = LocalDateTime.now();
            document.setCreatedAt(now);
            document.setUpdatedAt(now);
            if (documentMapper.insert(document) <= 0) throw new BizException("??????");
            return CreateDocumentResponse.builder().documentId(document.getId()).build();
        } catch (JsonProcessingException e) { throw new BizException("?????: " + e.getMessage()); }
    }

    /*
     * ????(????????)
     * 1. ?????? ? 2. ?????? ? 3. ?????????ID
     * ? 4. ??????? ? 5. ????????? ? 6. Markdown??+???
     */
    @Override
    public CreateDocumentResponse uploadDocument(String kbId, MultipartFile file) {
        try {
            if (file.isEmpty()) throw new BizException("???????");
            String originalFilename = file.getOriginalFilename();
            String filetype = getFileType(originalFilename);
            long fileSize = file.getSize();

            // ??????? documentId,????????ID
            Document document = documentConverter.toEntity(DocumentDTO.builder()
                    .kbId(kbId).filename(originalFilename).filetype(filetype).size(fileSize).build());
            LocalDateTime now = LocalDateTime.now();
            document.setCreatedAt(now); document.setUpdatedAt(now);
            if (documentMapper.insert(document) <= 0) throw new BizException("????????");
            String documentId = document.getId();

            // ????,????:kbId/documentId/???
            String filePath = documentStorageService.saveFile(kbId, documentId, file);

            // ?????????
            DocumentDTO.MetaData metadata = new DocumentDTO.MetaData();
            metadata.setFilePath(filePath);
            DocumentDTO dto = DocumentDTO.builder()
                    .id(documentId).kbId(kbId).filename(originalFilename)
                    .filetype(filetype).size(fileSize).metadata(metadata)
                    .createdAt(now).updatedAt(now).build();
            Document updated = documentConverter.toEntity(dto);
            updated.setId(documentId); updated.setCreatedAt(now); updated.setUpdatedAt(now);
            documentMapper.updateById(updated);

            log.info("??????: kbId={}, documentId={}, filename={}", kbId, documentId, originalFilename);

            // Markdown?????????RAG??chunks
            if ("md".equalsIgnoreCase(filetype) || "markdown".equalsIgnoreCase(filetype)) {
                processMarkdownDocument(kbId, documentId, filePath);
            } else if ("txt".equalsIgnoreCase(filetype)) {
                processTextDocument(kbId, documentId, filePath);
            } else {
                log.warn("??????????: {}", filetype);
            }
            return CreateDocumentResponse.builder().documentId(documentId).build();
        } catch (IOException e) { throw new BizException("??????: " + e.getMessage()); }
    }

    // ????:????(?????),???????
    @Override
    public void deleteDocument(String documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) throw new BizException("?????: " + documentId);
        try {
            DocumentDTO dto = documentConverter.toDTO(document);
            if (dto.getMetadata() != null && dto.getMetadata().getFilePath() != null) {
                documentStorageService.deleteFile(dto.getMetadata().getFilePath());
            }
        } catch (Exception e) {
            log.warn("??????,??????: documentId={}", documentId, e);
        }
        if (documentMapper.deleteById(documentId) <= 0) throw new BizException("??????");
    }

    /*
     * ??Markdown?????RAG??chunks
     * ??:???? ? ?????(Section) ? ????embedding ? ??ChunkBgeM3??
     * embedding?????(???????),????docId????
     */
    private void processMarkdownDocument(String kbId, String documentId, String filePath) {
        try {
            log.info("???? Markdown ??: documentId={}", documentId);
            Path path = documentStorageService.getFilePath(filePath);
            try (InputStream inputStream = Files.newInputStream(path)) {
                List<MarkdownParserService.MarkdownSection> sections = markdownParserService.parseMarkdown(inputStream);
                if (sections.isEmpty()) { log.warn("??????: documentId={}", documentId); return; }
                LocalDateTime now = LocalDateTime.now();
                int chunkCount = 0;
                for (MarkdownParserService.MarkdownSection section : sections) {
                    String title = section.getTitle();
                    if (title == null || title.trim().isEmpty()) continue;
                    float[] embedding = ragService.embed(title);  // ?????
                    ChunkBgeM3 chunk = ChunkBgeM3.builder()
                            .kbId(kbId).docId(documentId)
                            .content(section.getContent() != null ? section.getContent() : "")
                            .embedding(embedding).createdAt(now).updatedAt(now).build();
                    if (chunkBgeM3Mapper.insert(chunk) > 0) chunkCount++;
                }
                log.info("Markdown ????: documentId={}, ??{}?chunks", documentId, chunkCount);
            }
        } catch (Exception e) { log.error("??Markdown??: documentId={}", documentId, e); }
    }

    /*
     * ??????????RAG??chunks
     * ??:???? ? ????? ? ??????embedding ? ??ChunkBgeM3??
     */
    private void processTextDocument(String kbId, String documentId, String filePath) {
        try {
            log.info("?????????: documentId={}", documentId);
            Path path = documentStorageService.getFilePath(filePath);
            String content = Files.readString(path);
            if (content.isBlank()) { log.warn("??????: documentId={}", documentId); return; }

            // ???????
            String[] paragraphs = content.split("\\n\\s*\\n");
            LocalDateTime now = LocalDateTime.now();
            int chunkCount = 0;
            for (String paragraph : paragraphs) {
                String trimmed = paragraph.trim();
                if (trimmed.isEmpty()) continue;
                // ???? 200 ???? embedding ??(?????)
                String embedText = trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
                float[] embedding = ragService.embed(embedText);
                ChunkBgeM3 chunk = ChunkBgeM3.builder()
                        .kbId(kbId).docId(documentId)
                        .content(trimmed)
                        .embedding(embedding).createdAt(now).updatedAt(now).build();
                if (chunkBgeM3Mapper.insert(chunk) > 0) chunkCount++;
            }
            log.info("???????: documentId={}, ??{}?chunks", documentId, chunkCount);
        } catch (Exception e) { log.error("???????: documentId={}", documentId, e); }
    }

    // ?????????(??),? "README.md" ? "md"
    private String getFileType(String filename) {
        if (filename == null || !filename.contains(".")) return "unknown";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    // ????:???????,id/kbId/createdAt???
    @Override
    public void updateDocument(String documentId, UpdateDocumentRequest request) {
        try {
            Document existing = documentMapper.selectById(documentId);
            if (existing == null) throw new BizException("?????: " + documentId);
            DocumentDTO dto = documentConverter.toDTO(existing);
            documentConverter.updateDTOFromRequest(dto, request);
            Document updated = documentConverter.toEntity(dto);
            updated.setId(existing.getId());
            updated.setKbId(existing.getKbId());
            updated.setCreatedAt(existing.getCreatedAt());
            updated.setUpdatedAt(LocalDateTime.now());
            if (documentMapper.updateById(updated) <= 0) throw new BizException("??????");
        } catch (JsonProcessingException e) { throw new BizException("?????: " + e.getMessage()); }
    }
}