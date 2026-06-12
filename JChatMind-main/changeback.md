# ????????

## 2026-06-11 10:00:00 - DataBaseTools.query() ??????

| ?? | ???? |
|------|---------|
| `jchatmind/.../agent/tools/DataBaseTools.java` | ? `query()` ??????????????:`validateSql()`?`executeQuery()`?`processResultSet()`?`extractTableData()`?`formatAsTable()`?`formatTableRow()`?`formatTableSeparator()`?`formatEmptyRow()`?`formatQueryResult()`?`calculateDataRowCount()`;????? `TableData` |

---

## 2026-06-10 14:30:00 - ??????:glm-4.6 ? glm-5.1

| ?? | ???? |
|------|---------|
| `jchatmind/src/main/resources/application.yaml` | `zhipuai.chat.options.model`: glm-4.6 ? glm-5.1 |
| `jchatmind/.../config/MultiChatClientConfig.java` | Bean ? `"glm-4.6"` ? `"glm-5.1"`,??? `glm46ChatClient` ? `glm51ChatClient`;DeepSeek Bean ???? `@Bean("deepseek-chat")` |
| `jchatmind/.../controller/TestController.java` | `@Qualifier("deepSeekChatClient")` ? `@Qualifier("deepseek-chat")` |
| `jchatmind/.../model/dto/AgentDTO.java` | ?? `GLM_4_6("glm-4.6")` ? `GLM_5_1("glm-5.1")` |
| `jchatmind/.../config/ChatClientRegistry.java` | ??? glm-4.6 ? glm-5.1 |

---

## 2026-06-10 15:30:00 - ?? LLM API 404:base-url ?? /v1 ??

| ?? | ???? |
|------|---------|
| `jchatmind/src/main/resources/application.yaml` | `base-url` ? `http://localhost:18080` ?? `http://localhost:18080/v1`(deepseek ? zhipuai ???) |

---

## 2026-06-10 16:00:00 - ?????? 401

| ?? | ???? |
|------|---------|
| `jchatmind-proxy/proxy-config.yaml` | `proxy.auth-token` ??????? `"jchatmind-local-dev-token"` |
| `jchatmind/src/main/resources/application.yaml` | ?? `credential-proxy.auth-token` ??;`api-key` ? `DUMMY_KEY_FOR_PROXY` ?? `jchatmind-local-dev-token` |
| `jchatmind/.../config/ProxyCredentialFetcher.java` | ?? `authToken` ??,????? `X-Proxy-Token` ?;?? `restTemplate.exchange()` ?? `getForObject()` |
| `jchatmind-proxy/.../LocalhostOnlyFilter.java` | ?? `Authorization: Bearer` ??? `X-Proxy-Token` ??????? |

---

## 2026-06-10 16:30:00 - ?????????(Fallback)

| ?? | ???? |
|------|---------|
| `jchatmind/.../config/ChatClientRegistry.java` | ?? `getWithFallback()`?`getResolvedModelName()`?`getAvailableModels()` ?? |
| `jchatmind/.../agent/AgentRuntimeConfig.java` | ?? `chatClientRegistry` ? `primaryModelName` ?? |
| `jchatmind/.../agent/JChatMindFactory.java` | `buildAgentRuntime()` ?? `getWithFallback()`,????? SSE ???? |
| `jchatmind/.../agent/JChatMind.java` | `think()` ?????????:???????????????????,?? `tryGetFallbackClient()` ?? |

---

## 2026-06-10 17:00:00 - ?? /api/tools 500 ??

| ?? | ???? |
|------|---------|
| `jchatmind/.../model/dto/ToolDTO.java` | ???? DTO,??? name/description/type ???? |
| `jchatmind/.../controller/ToolController.java` | ????? `List<Tool>` ?? `List<ToolDTO>`,?? `ToolDTO::from` ?? |

---

## 2026-06-10 17:30:00 - ?????

| ?? | ???? |
|------|---------|
| `jchatmind/src/main/resources/application.yaml` | `api-key`?`base-url`?`auth-token` ?? `${ENV_VAR:default}` ??,???????? |
| `jchatmind-proxy/src/main/resources/application.yaml` | `proxy.auth-token` ?? `${PROXY_AUTH_TOKEN:jchatmind-local-dev-token}` |

---

## 2026-06-10 18:00:00 - ??? .txt ????(????)

| ?? | ???? |
|------|---------|
| `jchatmind/.../service/impl/DocumentFacadeServiceImpl.java` | ?? `processTextDocument()` ??:???????,?? 200 ??? embedding,?? RAG ?? chunks;`uploadDocument()` ?? `.txt` ??????? |

---

## 2026-06-10 19:00:00 - ?????????(????)

| ?? | ???? |
|------|---------|
| `jchatmind/.../model/dto/ChatMessageDTO.java` | `MetaData` ??????? Spring AI ??,????? `ToolInvocation`(id/type/name/arguments)? `ToolResult`(id/name/responseData);?? `import lombok.NoArgsConstructor` |
| `jchatmind/.../agent/JChatMind.java` | `saveMessage()` ?? Spring AI ? `ToolCall`/`ToolResponse` ?????? `ToolInvocation`/`ToolResult` ??? MetaData |
| `jchatmind/.../agent/JChatMindFactory.java` | `loadChatMemory()` ????? `ToolInvocation`/`ToolResult` ?? Spring AI ? `ToolCall`/`ToolResponse` ? Agent ?? |

---

## 2026-06-10 20:00:00 - ?? GLM-5.1 ????(??? DeepSeek)

| ?? | ???? |
|------|---------|
| `jchatmind-proxy/.../LlmProxyController.java` | `@RequestMapping` ? `/v1` ?? `{"/v1", "/v4"}`,???? OpenAI ??? API ?? |
| `jchatmind/src/main/resources/application.yaml` | zhipuai ? `base-url` ? `http://localhost:18080/v1` ?? `http://localhost:18080`(?? `/v1`,?? ZhiPuAi starter ???? `/v4/chat/completions`) |

---

## 2026-06-12 14:30:00 - P4 ????(??????? + ?? bug ??)

| ?? | ???? |
|------|---------|
| `jchatmind/.../model/common/ApiResponse.java` | `ApiCode` ???? `BAD_REQUEST(400, "bad request")` ? `NOT_FOUND(404, "not found")` |
| `jchatmind/.../exception/GlobalExceptionHandler.java` | ?? `IllegalArgumentException` ? 400 ??;?? `IllegalStateException` ? 409 ??;`handle404` ???? `ApiResponse` ??(??? body);???????????? `e.getClass().getSimpleName()`;BizException ??????? `ApiCode.BAD_REQUEST` |
| `jchatmind/.../config/ChatClientRegistry.java` | ?? `getFallbackOrder()` ??,?? `FALLBACK_ORDER` ?? |
| `jchatmind/.../agent/JChatMind.java` | ?? `tryGetFallbackClient()`:? `FALLBACK_ORDER` ???????(??? `getAvailableModels()` ?? Set ???);catch ? continue ?????????;?? `log.info/warn` ?? |

---

## 2026-06-12 16:00:00 - P5-2 ?????????(??)

| ?? | ???? |
|------|---------|
| `jchatmind/.../model/vo/ChatSessionVO.java` | ?? `createdAt`(LocalDateTime)? `updatedAt`(LocalDateTime)?? |
| `jchatmind/.../converter/ChatSessionConverter.java` | `toVO(ChatSessionDTO)` ???? `createdAt` ? `updatedAt` ??? |

---

## 2026-06-12 17:00:00 - P5-3 ??????(??)

| ?? | ???? |
|------|---------|
| `jchatmind/.../mapper/ChatMessageMapper.java` | ?? `selectBySessionIdPaginated(sessionId, limit, offset)` ? `countBySessionId(sessionId)` ?? |
| `jchatmind/.../mapper/ChatMessageMapper.xml` | ?? `selectBySessionIdPaginated` SQL(LIMIT/OFFSET ??)? `countBySessionId` SQL |
| `jchatmind/.../model/response/GetChatMessagesResponse.java` | ?? `total` ?? |
| `jchatmind/.../service/ChatMessageFacadeService.java` | ?? `getChatMessagesBySessionIdPaginated(sessionId, limit, offset)` ???? |
| `jchatmind/.../service/impl/ChatMessageFacadeServiceImpl.java` | ?? `getChatMessagesBySessionIdPaginated`(? count ??,?????);? `getChatMessagesBySessionId` ??? total |
| `jchatmind/.../controller/ChatMessageController.java` | ?? `GET /api/chat-messages/session/{sessionId}/paginated?limit=20&offset=0` ?? |
