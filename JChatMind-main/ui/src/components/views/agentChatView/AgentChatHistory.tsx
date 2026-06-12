import React, { useState, useRef, useEffect, useCallback } from "react";
import { Bubble, Actions } from "@ant-design/x";
import XMarkdown from "@ant-design/x-markdown";
import { useVirtualizer } from "@tanstack/react-virtual";
import { message as antdMessage } from "antd";
import {
  ToolOutlined,
  CheckCircleOutlined,
  RobotOutlined,
  DownOutlined,
  RightOutlined,
  CopyOutlined,
  RedoOutlined,
} from "@ant-design/icons";
import type { ChatMessageVO, SseMessageType, ToolCall, ToolResponse } from "../../../types";

interface AgentChatHistoryProps {
  messages: ChatMessageVO[];
  displayAgentStatus?: boolean;
  agentStatusText?: string;
  agentStatusType?: SseMessageType;
  hasMore?: boolean;
  loadingMore?: boolean;
  onLoadMore?: () => void;
  onRegenerate?: (message: ChatMessageVO) => void;
}

// 工具调用展示组件（简化版，用于 assistant 消息内）
const ToolCallDisplay: React.FC<{ toolCall: ToolCall }> = ({ toolCall }) => {
  let parsedArgs: Record<string, unknown> = {};
  try {
    parsedArgs = JSON.parse(toolCall.arguments) as Record<string, unknown>;
  } catch {
    // 如果解析失败，使用原始字符串
  }

  const argCount = Object.keys(parsedArgs).length;
  const argPreview = argCount > 0 
    ? Object.keys(parsedArgs).slice(0, 2).join(", ") + (argCount > 2 ? "..." : "")
    : toolCall.arguments.slice(0, 50) + (toolCall.arguments.length > 50 ? "..." : "");

  return (
    <div className="text-xs text-gray-500 dark:text-gray-400 flex items-center gap-1.5">
      <ToolOutlined className="text-blue-500" />
      <span className="font-mono text-blue-600 dark:text-blue-400">{toolCall.name}</span>
      {argPreview && (
        <>
          <span className="text-gray-400 dark:text-gray-500">·</span>
          <span className="text-gray-500 dark:text-gray-400 truncate max-w-[200px]">{argPreview}</span>
        </>
      )}
    </div>
  );
};

// 工具响应展示组件（可折叠）
const ToolResponseDisplay: React.FC<{ toolResponse: ToolResponse }> = ({
  toolResponse,
}) => {
  const [expanded, setExpanded] = useState(false);
  
  let parsedData: unknown = null;
  let isJson = false;
  let dataPreview = "";
  
  try {
    parsedData = JSON.parse(toolResponse.responseData);
    isJson = true;
    const jsonStr = JSON.stringify(parsedData);
    dataPreview = jsonStr.length > 100 ? jsonStr.slice(0, 100) + "..." : jsonStr;
  } catch {
    dataPreview = toolResponse.responseData.length > 100 
      ? toolResponse.responseData.slice(0, 100) + "..." 
      : toolResponse.responseData;
  }

  return (
    <div className="my-1.5 text-xs">
      <div 
        className="flex items-center gap-2 text-gray-500 dark:text-gray-400 cursor-pointer hover:text-gray-700 dark:hover:text-gray-300 transition-colors"
        onClick={() => setExpanded(!expanded)}
      >
        {expanded ? (
          <DownOutlined className="text-gray-400 dark:text-gray-500" />
        ) : (
          <RightOutlined className="text-gray-400 dark:text-gray-500" />
        )}
        <CheckCircleOutlined className="text-green-500" />
        <span className="font-mono text-green-600 dark:text-green-400">{toolResponse.name}</span>
        <span className="text-gray-400 dark:text-gray-500">·</span>
        <span className="text-gray-500 dark:text-gray-400 truncate flex-1">{dataPreview}</span>
      </div>
      {expanded && (
        <div className="ml-5 mt-1.5 p-2 bg-gray-50 dark:bg-gray-800 rounded border border-gray-200 dark:border-gray-700">
          <div className="text-xs text-gray-600 dark:text-gray-300 font-mono">
            {isJson ? (
              <pre className="whitespace-pre-wrap break-words overflow-x-auto max-h-60 overflow-y-auto">
                {JSON.stringify(parsedData, null, 2)}
              </pre>
            ) : (
              <div className="whitespace-pre-wrap break-words">
                {toolResponse.responseData}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

const AgentChatHistory: React.FC<AgentChatHistoryProps> = ({
  messages,
  displayAgentStatus = false,
  agentStatusText = "",
  agentStatusType,
  hasMore = false,
  loadingMore = false,
  onLoadMore,
  onRegenerate,
}) => {
  // 滚动容器引用
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  // 是否允许自动滚动（用户是否接近底部）
  const [isNearBottom, setIsNearBottom] = useState(true);
  // 容错阈值（像素）
  const SCROLL_THRESHOLD = 20;
  // 上一次消息数量，用于检测新消息
  const prevMessagesLengthRef = useRef(messages.length);
  // 最新消息 ID，用于入场动画
  const lastMessageIdRef = useRef<string>("");

  // 检查是否接近底部
  const checkIfNearBottom = useCallback(() => {
    const container = scrollContainerRef.current;
    if (!container) return false;

    const { scrollTop, clientHeight, scrollHeight } = container;
    const distanceFromBottom = scrollHeight - scrollTop - clientHeight;
    return distanceFromBottom <= SCROLL_THRESHOLD;
  }, []);

  // 滚动到底部
  const scrollToBottom = useCallback(() => {
    const container = scrollContainerRef.current;
    if (!container) return;

    // 使用 requestAnimationFrame 确保 DOM 更新完成后再滚动
    requestAnimationFrame(() => {
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    });
  }, []);

  // 处理滚动事件，实时更新是否接近底部的状态 + 向上滚动加载更多
  const handleScroll = useCallback(() => {
    const container = scrollContainerRef.current;
    if (!container) return;

    const nearBottom = checkIfNearBottom();
    setIsNearBottom(nearBottom);

    // 向上滚动到顶部时加载更多
    if (container.scrollTop <= 50 && hasMore && !loadingMore && onLoadMore) {
      // 记录当前 scrollHeight，加载完成后恢复位置
      const oldScrollHeight = container.scrollHeight;
      onLoadMore();
      // 加载完成后恢复滚动位置（使用 requestAnimationFrame 等待 DOM 更新）
      requestAnimationFrame(() => {
        if (scrollContainerRef.current) {
          const newScrollHeight = scrollContainerRef.current.scrollHeight;
          scrollContainerRef.current.scrollTop = newScrollHeight - oldScrollHeight;
        }
      });
    }
  }, [checkIfNearBottom, hasMore, loadingMore, onLoadMore]);

  // 监听滚动事件
  useEffect(() => {
    const container = scrollContainerRef.current;
    if (!container) return;

    // 初始化时检查是否在底部（延迟执行以避免同步 setState）
    const initTimer = setTimeout(() => {
      setIsNearBottom(checkIfNearBottom());
    }, 0);

    container.addEventListener("scroll", handleScroll, { passive: true });

    return () => {
      clearTimeout(initTimer);
      container.removeEventListener("scroll", handleScroll);
    };
  }, [handleScroll, checkIfNearBottom]);

  // 虚拟滚动：只渲染可视区域内的消息
  const virtualizer = useVirtualizer({
    count: messages.length,
    getScrollElement: () => scrollContainerRef.current,
    estimateSize: () => 80, // 估算每条消息高度
    overscan: 5, // 上下多渲染 5 条，减少滚动时空白
  });

  // 监听消息变化，决定是否自动滚动
  useEffect(() => {
    const hasNewMessage = messages.length > prevMessagesLengthRef.current;
    if (hasNewMessage && messages.length > 0) {
      lastMessageIdRef.current = messages[messages.length - 1].id;
    }
    prevMessagesLengthRef.current = messages.length;

    if (hasNewMessage && isNearBottom) {
      scrollToBottom();
    }
  }, [messages, isNearBottom, scrollToBottom]);

  // 当 displayAgentStatus 变化时，如果用户接近底部，也自动滚动
  useEffect(() => {
    if (displayAgentStatus && isNearBottom) {
      scrollToBottom();
    }
  }, [displayAgentStatus, isNearBottom, scrollToBottom]);

  // 获取状态标签
  const getStatusLabel = () => {
    switch (agentStatusType) {
      case "AI_PLANNING":
        return "规划中";
      case "AI_THINKING":
        return "思考中";
      case "AI_EXECUTING":
        return "执行中";
      default:
        return "处理中";
    }
  };

  // 渲染单条消息
  const renderMessage = (message: ChatMessageVO) => (
    <>
      {/* Assistant 消息 */}
      {message.role === "assistant" && (
        <Bubble
          content={
            <div className="w-full">
              {/* 工具调用展示 */}
              {message.metadata?.toolCalls &&
                message.metadata.toolCalls.length > 0 && (
                  <div className="mb-2 flex flex-wrap gap-2">
                    {message.metadata.toolCalls.map((toolCall) => (
                      <ToolCallDisplay key={toolCall.id} toolCall={toolCall} />
                    ))}
                  </div>
                )}
              {/* 消息内容 */}
              {message.content && (
                <div>
                  <XMarkdown
                    streaming={{ enableAnimation: true, hasNextChunk: true }}
                  >
                    {message.content}
                  </XMarkdown>
                </div>
              )}
            </div>
          }
          placement="start"
          footer={
            <Actions
              items={[
                {
                  key: "copy",
                  label: "复制",
                  icon: <CopyOutlined />,
                  onItemClick: () => {
                    navigator.clipboard.writeText(message.content || "")
                      .then(() => antdMessage.success("已复制到剪贴板"))
                      .catch(() => antdMessage.error("复制失败"));
                  },
                },
                {
                  key: "regenerate",
                  label: "重新生成",
                  icon: <RedoOutlined />,
                  onItemClick: () => onRegenerate?.(message),
                },
              ]}
              variant="borderless"
              fadeIn
            />
          }
          footerPlacement="outer-end"
        />
      )}

      {/* Tool 消息 - 简洁展示，不使用气泡 */}
      {message.role === "tool" && message.metadata?.toolResponse && (
        <div className="flex justify-start">
          <div className="max-w-[85%]">
            <ToolResponseDisplay toolResponse={message.metadata.toolResponse} />
          </div>
        </div>
      )}

      {/* User 消息 */}
      {message.role === "user" && (
        <Bubble content={message.content} placement="end" />
      )}

      {/* System 消息 */}
      {message.role === "system" && (
        <div className="flex justify-center">
          <div className="px-3 py-1 bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 text-xs rounded-full flex items-center gap-1">
            <RobotOutlined />
            <span>{message.content}</span>
          </div>
        </div>
      )}
    </>
  );

  return (
    <div
      ref={scrollContainerRef}
      className="flex-1 px-16 pt-4 overflow-y-scroll"
    >
      {/* 加载更多提示 */}
      {hasMore && (
        <div className="text-center py-2 text-xs text-gray-400 dark:text-gray-500">
          {loadingMore ? "加载中..." : "↑ 向上滚动加载更多消息"}
        </div>
      )}
      {/* 虚拟滚动容器 */}
      <div
        style={{
          height: virtualizer.getTotalSize(),
          width: "100%",
          position: "relative",
        }}
      >
        {virtualizer.getVirtualItems().map((virtualItem) => {
          const message = messages[virtualItem.index];
          const isNewMessage = message.id === lastMessageIdRef.current;
          return (
            <div
              key={message.id}
              style={{
                position: "absolute",
                top: 0,
                left: 0,
                width: "100%",
                transform: `translateY(${virtualItem.start}px)`,
                padding: "0 0 16px 0",
              }}
              data-index={virtualItem.index}
              ref={virtualizer.measureElement}
              className={isNewMessage ? "message-enter" : ""}
            >
              {renderMessage(message)}
            </div>
          );
        })}
      </div>
      {displayAgentStatus && (
        <div className="mb-3">
          <div
            className="animate-pulse"
            style={{
              animation: "pulse 0.8s cubic-bezier(0.4, 0, 0.6, 1) infinite",
              filter: "brightness(1.15)",
            }}
          >
            <Bubble
              content={
                <span className="flex items-center gap-2">
                  <span
                    className="font-semibold text-blue-600 dark:text-blue-400"
                    style={{
                      animation:
                        "pulse 0.7s cubic-bezier(0.4, 0, 0.6, 1) infinite",
                      textShadow:
                        "0 0 10px rgba(37, 99, 235, 1), 0 0 20px rgba(37, 99, 235, 0.8), 0 0 30px rgba(37, 99, 235, 0.5)",
                      filter: "brightness(1.3)",
                    }}
                  >
                    ✨ {getStatusLabel()}
                  </span>
                  <span className="text-gray-400 dark:text-gray-500">·</span>
                  <span className="text-gray-600 dark:text-gray-300">{agentStatusText}</span>
                </span>
              }
              placement="start"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default AgentChatHistory;
