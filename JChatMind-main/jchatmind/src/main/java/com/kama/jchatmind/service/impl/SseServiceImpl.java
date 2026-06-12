package com.kama.jchatmind.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.message.SseMessage;
import com.kama.jchatmind.service.SseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * SSE????? -- ???????????????
 * ConcurrentHashMap:?????Map,???????????
 * SseEmitter:Spring?SSE??,??????????,??30??????
 */
@Service
@Slf4j
@AllArgsConstructor
public class SseServiceImpl implements SseService {

    // chatSessionId ? SseEmitter:????????SSE??
    private final ConcurrentMap<String, SseEmitter> clients = new ConcurrentHashMap<>();
    // Jackson?????,?Java????JSON???????
    private final ObjectMapper objectMapper;

    /*
     * ??SSE??
     * @param chatSessionId ??ID
     * @return SseEmitter ??? EventSource ????
     */
    @Override
    public SseEmitter connect(String chatSessionId) {
        // 30????
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        clients.put(chatSessionId, emitter);
        try {
            emitter.send(SseEmitter.event().name("init").data("connected"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // ?????????Map???,??????
        emitter.onCompletion(() -> clients.remove(chatSessionId));
        emitter.onTimeout(() -> clients.remove(chatSessionId));
        emitter.onError((error) -> clients.remove(chatSessionId));
        return emitter;
    }

    /*
     * ???????????
     * ???????JSON,???emitter??
     * ???????????????
     */
    @Override
    public void send(String chatSessionId, SseMessage message) {
        try {
            SseEmitter emitter = clients.get(chatSessionId);
            if (emitter == null || emitter.getTimeout() == null) {
                log.warn("SSE?????????,chatSessionId: {}", chatSessionId);
                return;
            }
            String json = objectMapper.writeValueAsString(message);
            emitter.send(json);
        } catch (Exception e) {
            log.warn("SSE??????,chatSessionId: {}, ??: {}", chatSessionId, e.getMessage());
            clients.remove(chatSessionId);
        }
    }
}