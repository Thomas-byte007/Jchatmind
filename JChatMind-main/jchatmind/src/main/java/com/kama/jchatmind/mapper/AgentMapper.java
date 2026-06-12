package com.kama.jchatmind.mapper;

import com.kama.jchatmind.model.entity.Agent;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/*
 * Agent?????? -- MyBatis???????
 * @Mapper:???MyBatis Mapper,Spring????????????
 * insert??????,selectById??????,selectAll???
 */
@Mapper
public interface AgentMapper {
    int insert(Agent agent);                    // INSERT INTO agent
    Agent selectById(String id);                // SELECT * FROM agent WHERE id=?
    List<Agent> selectAll();                    // SELECT * FROM agent
    int deleteById(String id);                  // DELETE FROM agent WHERE id=?
    int updateById(Agent agent);                // UPDATE agent SET ... WHERE id=?
    List<Agent> selectByIdBatch(List<String> ids); // SELECT * FROM agent WHERE id IN (...)
}