package com.kama.jchatmind.mapper;

import com.kama.jchatmind.model.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/*
 * ??????? -- MyBatis Mapper??
 * selectByIdBatch:????,????Agent?????????
 */
@Mapper
public interface KnowledgeBaseMapper {
    int insert(KnowledgeBase knowledgeBase);
    KnowledgeBase selectById(String id);
    List<KnowledgeBase> selectAll();
    int deleteById(String id);
    int updateById(KnowledgeBase knowledgeBase);
    List<KnowledgeBase> selectByIdBatch(List<String> ids);
}