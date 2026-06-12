package com.kama.jchatmind.service.impl;

import com.kama.jchatmind.agent.tools.Tool;
import com.kama.jchatmind.agent.tools.ToolType;
import com.kama.jchatmind.service.ToolFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/*
 * 工具管理实现 —— 维护Agent可调用的所有工具
 * Spring自动将所有实现了Tool接口的Bean注入到List<Tool>中，无需手动注册
 * 固定工具(FIXED)：每个Agent自动拥有，如直接回答、终止任务
 * 可选工具(OPTIONAL)：需在Agent配置中勾选，如查数据库、发邮件
 */
@Service
@AllArgsConstructor
public class ToolFacadeServiceImpl implements ToolFacadeService {

    // Spring注入：收集所有Tool接口实现类
    private final List<Tool> tools;

    @Override
    public List<Tool> getFixedTools() {
        return getToolsByType(ToolType.FIXED);
    }

    @Override
    public List<Tool> getOptionalTools() {
        return getToolsByType(ToolType.OPTIONAL);
    }

    // 按类型筛选工具：stream().filter() 类似 SQL的WHERE子句
    private List<Tool> getToolsByType(ToolType type) {
        return tools.stream().filter(tool -> tool.getType().equals(type)).toList();
    }
}