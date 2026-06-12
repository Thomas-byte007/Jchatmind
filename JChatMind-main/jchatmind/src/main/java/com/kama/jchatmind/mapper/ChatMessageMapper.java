package com.kama.jchatmind.mapper;

import com.kama.jchatmind.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/*
 * ???????? -- MyBatis Mapper??
 * selectBySessionId:??????????(?????)
 * selectBySessionIdRecently:???N?,Agent?????
 */
@Mapper
public interface ChatMessageMapper {
    int insert(ChatMessage chatMessage);
    ChatMessage selectById(String id);
    int deleteById(String id);
    int updateById(ChatMessage chatMessage);
    List<ChatMessage> selectBySessionId(String sessionId);
    List<ChatMessage> selectBySessionIdRecently(String sessionId, int limit);
    List<ChatMessage> selectBySessionIdPaginated(String sessionId, int limit, int offset);
    int countBySessionId(String sessionId);
}