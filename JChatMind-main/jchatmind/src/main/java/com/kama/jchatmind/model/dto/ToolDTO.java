package com.kama.jchatmind.model.dto;

import com.kama.jchatmind.agent.tools.Tool;
import com.kama.jchatmind.agent.tools.ToolType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ????DTO -- ?????????(????????),?????????
 */
@Data
@AllArgsConstructor
public class ToolDTO {

    private String name;
    private String description;
    private ToolType type;

    public static ToolDTO from(Tool tool) {
        return new ToolDTO(tool.getName(), tool.getDescription(), tool.getType());
    }
}
