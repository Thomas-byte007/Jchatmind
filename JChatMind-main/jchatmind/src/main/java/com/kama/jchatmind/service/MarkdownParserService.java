package com.kama.jchatmind.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.InputStream;
import java.util.List;

/**
 * Markdown ??????
 */
public interface MarkdownParserService {
    /**
     * ?? Markdown ??,??????????
     *
     * @param inputStream Markdown ?????
     * @return ????????,????????????????
     */
    List<MarkdownSection> parseMarkdown(InputStream inputStream);
    
    /**
     * Markdown ?????
     */
    @Data
    @AllArgsConstructor
    @ToString
    class MarkdownSection {
        private String title;
        private String content;
    }
}
