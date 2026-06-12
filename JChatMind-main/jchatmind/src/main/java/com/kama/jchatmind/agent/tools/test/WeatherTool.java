/*
 * 天气工具（测试用） —— 模拟查询天气的固定工具
 * 返回固定数据（非真实API调用），用于演示Agent如何调用带参数的工具
 */
package com.kama.jchatmind.agent.tools.test;

import com.kama.jchatmind.agent.tools.Tool;
import com.kama.jchatmind.agent.tools.ToolType;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool implements Tool {

    @Override
    public String getName() { return "weatherTool"; }

    @Override
    public String getDescription() { return "获取天气"; }

    @Override
    public ToolType getType() { return ToolType.FIXED; }

    // 接收city和date两个参数，模拟返回该城市该日期的天气（实际是写死的数据）
    @org.springframework.ai.tool.annotation.Tool(name = "weather", description = "获取天气")
    public String getWeather(String city, String date) {
        return city + date + "的天气查询结果：晴转多云，温度 25°C，湿度 60%";
    }
}