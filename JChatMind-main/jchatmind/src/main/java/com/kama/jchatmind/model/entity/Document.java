package com.kama.jchatmind.model.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * ?????
 * ??????? document ?
 */
@Data
@Builder
public class Document {

    /**
     * ??????
     */
    private String id;

    /**
     * ?????ID
     */
    private String kbId;

    /**
     * ?????
     */
    private String filename;

    /**
     * ????
     */
    private String filetype;

    /**
     * ????(??)
     */
    private Long size;

    /**
     * ?????(JSON????)
     */
    private String metadata;

    /**
     * ????
     */
    private LocalDateTime createdAt;

    /**
     * ????
     */
    private LocalDateTime updatedAt;
}
