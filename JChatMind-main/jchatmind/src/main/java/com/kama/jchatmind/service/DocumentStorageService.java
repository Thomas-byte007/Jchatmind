package com.kama.jchatmind.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * ????????
 */
public interface DocumentStorageService {
    /**
     * ???????
     *
     * @param kbId       ???ID
     * @param documentId ??ID
     * @param file       ?????
     * @return ???????
     * @throws IOException ??????
     */
    String saveFile(String kbId, String documentId, MultipartFile file) throws IOException;

    /**
     * ????
     *
     * @param filePath ????
     * @throws IOException ??????
     */
    void deleteFile(String filePath) throws IOException;

    /**
     * ?????????
     *
     * @param filePath ??????
     * @return ??????
     */
    Path getFilePath(String filePath);

    /**
     * ????????
     *
     * @param filePath ????
     * @return ??????
     */
    boolean fileExists(String filePath);
}
