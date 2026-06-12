import React, { useCallback, useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { message as antdMessage, Skeleton, Space } from "antd";
import AgentChatHistory from "./agentChatView/AgentChatHistory.tsx";
import AgentChatInput from "./agentChatView/AgentChatInput.tsx";
import {
  createChatMessage,
  createChatSession,
  getChatMessagesBySessionId,
  getChatMessagesBySessionIdPaginated,
  getChatSession,
  deleteChatMessage,
} from "../../api/api.ts";
import { useAgents } from "../../hooks/useAgents.ts";
import { useChatSessions } from "../../hooks/useChatSessions.ts";
import EmptyAgentChatView from "./agentChatView/EmptyAgentChatView.tsx";
import type { ChatMessageVO, SseMessage, SseMessageType } from "../../types";

const AgentChatView: React.FC = () => {
  const { chatSessionId } = useParams<{ chatSessionId: string }>();
  const navigate = useNavigate();
  const { state } = useLocation();
  const [loading, setLoading] = useState(false);
  const { agents } = useAgents();
  const { refreshChatSessions } = useChatSessions();

  const [messages, setMessages] = useState<ChatMessageVO[]>([]);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [totalMessages, setTotalMessages] = useState(0);
  const PAGE_SIZE = 20;

  const addMessage = (message: ChatMessageVO) => {
    setMessages((prevMessages) => [...prevMessages, message]);
    setTotalMessages((prev) => prev + 1);
  };

  const [agentId, setAgentId] = useState<string>("");

  const getChatMessages = useCallback(async () => {
    if (!chatSessionId) {
      return;
    }
    setMessagesLoading(true);
    try {
      // ???????????,?? agentId ????
      const [messagesResp, sessionResp] = await Promise.all([
        getChatMessagesBySessionIdPaginated(chatSessionId, PAGE_SIZE, 0),
        getChatSession(chatSessionId),
      ]);
      setMessages(messagesResp.chatMessages);
      setTotalMessages(messagesResp.total);
      setHasMore(messagesResp.total > PAGE_SIZE);
      setAgentId(sessionResp.chatSession.agentId);
    } catch (error) {
      console.error("????????:", error);
      antdMessage.error("????????,?????");
    } finally {
      setMessagesLoading(false);
    }
  }, [chatSessionId]);

  // ????????????
  const loadMoreMessages = useCallback(async () => {
    if (!chatSessionId || loadingMore || !hasMore) return;
    setLoadingMore(true);
    try {
      const currentCount = messages.length;
      // ??????????(offset = total - currentCount - PAGE_SIZE)
      const offset = Math.max(0, totalMessages - currentCount - PAGE_SIZE);
      const resp = await getChatMessagesBySessionIdPaginated(chatSessionId, PAGE_SIZE, offset);
      // ????:??????????
      setMessages((prev) => [...resp.chatMessages, ...prev]);
      setHasMore(currentCount + resp.chatMessages.length < totalMessages);
    } catch (error) {
      console.error("????????:", error);
    } finally {
      setLoadingMore(false);
    }
  }, [chatSessionId, loadingMore, hasMore, messages.length, totalMessages]);

  useEffect(() => {
    if (!chatSessionId) {
      return;
    }
    getChatMessages().then();
  }, [chatSessionId, getChatMessages]);

  const handleSendMessage = async (value: string | { text: string }) => {
    // ?? Sender ???????????
    const message = typeof value === "string" ? value : value.text;

    console.log(message);

    if (!message || !message.trim()) return;

    // ???? chatSessionId,?????
    if (!chatSessionId) {
      if (!agentId) {
        antdMessage.warning("???????????");
        return;
      }
      setLoading(true);
      try {
        const response = await createChatSession({
          agentId: agentId,
          title: message.slice(0, 20),
        });
        // ????????
        await refreshChatSessions();
        // ?????????
        navigate(`/chat/${response.chatSessionId}`, {
          replace: true,
          state: {
            init: true,
            initMessage: message,
          },
        });
      } catch (error) {
        console.error("????????:", error);
        antdMessage.error("????????,???");
      } finally {
        setLoading(false);
      }
    } else {
      if (state?.init) {
        console.log("init message already handled by useEffect, skipping");
      } else {
        console.log("ask", message);
        await createChatMessage({
          agentId: agentId ?? "",
          sessionId: chatSessionId,
          role: "user",
          content: message,
        });
      }
      await getChatMessages();
    }
  };

  const [displayAgentStatus, setDisplayAgentStatus] = useState<boolean>(false);
  const [agentStatusText, setAgentStatusText] = useState("");
  const [agentStatusType, setAgentStatusType] = useState<
    SseMessageType | undefined
  >(undefined);

  const [sseConnected, setSseConnected] = useState(false);
  const [sseReconnecting, setSseReconnecting] = useState(false);

  /**
   * SSE ???? -- ??? SseMessage.Type ??????
   * ????????????????,??? console.warn
   */
  const handleSseMessage = useCallback((message: SseMessage) => {
    switch (message.type) {
      case "AI_GENERATED_CONTENT":
        addMessage(message.payload.message);
        break;
      case "AI_PLANNING":
      case "AI_THINKING":
      case "AI_EXECUTING":
        setDisplayAgentStatus(true);
        setAgentStatusText(message.payload.statusText);
        setAgentStatusType(message.type);
        break;
      case "AI_DONE":
        setDisplayAgentStatus(false);
        setAgentStatusText("");
        setAgentStatusType(undefined);
        break;
      case "AI_ERROR":
        setDisplayAgentStatus(true);
        setAgentStatusText(message.payload.statusText || "????");
        setAgentStatusType("AI_ERROR");
        console.error("Agent error:", message.payload.statusText);
        break;
      default:
        console.warn("Unknown SSE message type:", message.type);
    }
  }, []);

  // SSE ??:??????????
  useEffect(() => {
    if (!chatSessionId) {
      return;
    }
    const sseBaseUrl = import.meta.env.VITE_SSE_BASE_URL || "http://localhost:8080";
    const sseUrl = `${sseBaseUrl}/sse/connect/${chatSessionId}`;

    let es: EventSource | null = null;
    let retryCount = 0;
    const MAX_RETRIES = 5;
    const BASE_DELAY = 1000; // 1s, 2s, 4s, 8s, 16s
    let reconnectTimer: ReturnType<typeof setTimeout>;

    const connect = () => {
      es = new EventSource(sseUrl);

      es.addEventListener("init", () => {
        console.log("SSE connection established");
        setSseConnected(true);
        setSseReconnecting(false);
        retryCount = 0; // ??????
      });

      es.addEventListener("message", (event) => {
        const message = JSON.parse(event.data) as SseMessage;
        handleSseMessage(message);
      });

      es.onerror = () => {
        console.warn("SSE connection lost");
        setSseConnected(false);
        es?.close();
        es = null;

        // ??????
        if (retryCount < MAX_RETRIES) {
          const delay = BASE_DELAY * Math.pow(2, retryCount);
          retryCount++;
          setSseReconnecting(true);
          console.log(`SSE reconnecting in ${delay}ms (attempt ${retryCount}/${MAX_RETRIES})`);
          reconnectTimer = setTimeout(connect, delay);
        } else {
          console.error("SSE max retries reached, giving up");
          setSseReconnecting(false);
          antdMessage.error("??????,???????");
        }
      };
    };

    connect();

    return () => {
      clearTimeout(reconnectTimer);
      es?.close();
    };
  }, [chatSessionId, handleSseMessage]);

  // ? SSE ?????????????,????
  useEffect(() => {
    if (!chatSessionId || !sseConnected || !state?.init || !agentId) {
      return;
    }
    
    const sendInitMessage = async () => {
      console.log("Sending init message:", state.initMessage);
      await createChatMessage({
        agentId: agentId,
        sessionId: chatSessionId,
        role: "user",
        content: state.initMessage ?? "",
      });
      await getChatMessages();
    };
    
    sendInitMessage();
  }, [chatSessionId, sseConnected, state?.init, agentId, state?.initMessage]);

  // ???? chatSessionId,??????
  if (!chatSessionId) {
    return (
      <EmptyAgentChatView
        agents={agents}
        loading={loading}
        handleSendMessage={handleSendMessage}
      />
    );
  }

  // ??? chatSessionId,?????????
  return (
    <div className="flex flex-col h-full">
      {/* SSE ?????? */}
      {sseReconnecting && (
        <div className="px-4 py-1.5 bg-amber-50 dark:bg-amber-950 border-b border-amber-200 dark:border-amber-800 text-amber-700 dark:text-amber-300 text-xs text-center">
          ????,????????...
        </div>
      )}
      {!sseConnected && !sseReconnecting && chatSessionId && (
        <div className="px-4 py-1.5 bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400 text-xs text-center">
          ????????...
        </div>
      )}
      {messagesLoading ? (
        <div className="flex-1 p-6 space-y-4">
          <Space direction="vertical" className="w-full" size="middle">
            {[1, 2, 3].map((i) => (
              <div key={i} className={`flex ${i % 2 === 0 ? "justify-end" : "justify-start"}`}>
                <Skeleton.Avatar active style={{ marginRight: i % 2 !== 0 ? 8 : 0, marginLeft: i % 2 === 0 ? 8 : 0 }} />
                <Skeleton.Input active style={{ width: 200 + Math.random() * 200, height: 40 }} />
              </div>
            ))}
          </Space>
        </div>
      ) : (
        <AgentChatHistory
          messages={messages}
          displayAgentStatus={displayAgentStatus}
          agentStatusText={agentStatusText}
          agentStatusType={agentStatusType}
          hasMore={hasMore}
          loadingMore={loadingMore}
          onLoadMore={loadMoreMessages}
          onRegenerate={async (msg) => {
            const msgIndex = messages.findIndex((m) => m.id === msg.id);
            if (msgIndex < 0) return;

            // ??? assistant ????????? user ??
            const prevUserMsg = [...messages].slice(0, msgIndex).reverse().find((m) => m.role === "user");
            if (!prevUserMsg || !chatSessionId) return;

            // ??? assistant ??????????
            const messagesToDelete = messages.slice(msgIndex);
            for (const m of messagesToDelete) {
              try {
                await deleteChatMessage(m.id);
              } catch (e) {
                console.warn("??????:", m.id, e);
              }
            }

            // ??????:??? msgIndex ?????
            setMessages(messages.slice(0, msgIndex));

            // ????????(SSE ??????? AI ??)
            await createChatMessage({
              agentId: agentId ?? "",
              sessionId: chatSessionId,
              role: "user",
              content: prevUserMsg.content,
            });
          }}
        />
      )}
      <div className="border-t border-gray-200 dark:border-gray-700 p-4 bg-white dark:bg-gray-900">
        <AgentChatInput onSend={handleSendMessage} />
      </div>
    </div>
  );
};

export default AgentChatView;
