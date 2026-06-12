/*
 * ???????? -- ???????????(????)
 * ????:{basePath}/{kbId}/{documentId}/{UUID??????}.???
 * UUID?????????;StandardCopyOption.REPLACE_EXISTING??????
 * ????????????(documentDir??),??????
 */
package com.kama.jchatmind.service.impl;

import com.kama.jchatmind.service.DocumentStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class DocumentStorageServiceImpl implements DocumentStorageService {

    // ?????,?? ./data/documents,??yaml???
    @Value("${document.storage.base-path:./data/documents}")
    private String baseStoragePath;

    // ????:???? ? ??????? ? ???? ? ??????
    @Override
    public String saveFile(String kbId, String documentId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("???????");
        Path kbDir = Paths.get(baseStoragePath, kbId);
        Path documentDir = kbDir.resolve(documentId);
        Files.createDirectories(documentDir);  // ??????????
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;  // ? a1b2c3d4.pdf
        Path targetPath = documentDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        String relativePath = Paths.get(kbId, documentId, uniqueFilename).toString().replace("\\", "/");
        log.info("??????: kbId={}, documentId={}, filename={}, path={}", kbId, documentId, originalFilename, relativePath);
        return relativePath;
    }

    // ?????????????(??????????)
    @Override
    public void deleteFile(String filePath) throws IOException {
        Path fullPath = getFilePath(filePath);
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
            log.info("??????: {}", filePath);
            Path parentDir = fullPath.getParent();
            if (parentDir != null && Files.exists(parentDir)) {
                try {
                    Files.delete(parentDir);  // ????????
                    log.info("??????: {}", parentDir);
                } catch (IOException e) {
                    log.debug("??????(?????): {}", parentDir);
                }
            }
        } else {
            log.warn("?????,????: {}", filePath);
        }
    }

    // ??????:basePath + ????
    @Override
    public Path getFilePath(String filePath) {
        return Paths.get(baseStoragePath, filePath);
    }

    @Override
    public boolean fileExists(String filePath) {
        Path fullPath = getFilePath(filePath);
        return Files.exists(fullPath) && Files.isRegularFile(fullPath);
    }
}