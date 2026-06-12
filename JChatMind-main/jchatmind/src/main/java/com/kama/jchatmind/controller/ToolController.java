package com.kama.jchatmind.controller;

import com.kama.jchatmind.agent.tools.Tool;
import com.kama.jchatmind.model.common.ApiResponse;
import com.kama.jchatmind.model.dto.ToolDTO;
import com.kama.jchatmind.service.ToolFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/*
 * ????? -- ???????????
 *
 * ????:
 * ??(Tool) = Agent??????,???????????
 * ????:??Agent????(??????????)
 * ????:???Agent????????
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ToolController {

    private final ToolFacadeService toolFacadeService;

    // ???????? GET /api/tools
    @GetMapping("/tools")
    public ApiResponse<List<ToolDTO>> getOptionalTools() {
        List<ToolDTO> tools = toolFacadeService.getOptionalTools().stream()
                .map(ToolDTO::from).toList();
        return ApiResponse.success(tools);
    }
}