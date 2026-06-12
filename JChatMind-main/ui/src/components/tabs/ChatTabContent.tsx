import React, { useMemo, useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button, Divider, Popconfirm, Skeleton, Input } from "antd";
import {
  PlusOutlined,
  MessageOutlined,
  DeleteOutlined,
  EditOutlined,
} from "@ant-design/icons";
import { useChatSessions } from "../../hooks/useChatSessions.ts";
import { useAgents } from "../../hooks/useAgents.ts";
import type { ChatSessionVO } from "../../api/api.ts";

// ??????
interface SessionGroup {
  label: string;
  sessions: ChatSessionVO[];
}

function groupByTime(sessions: ChatSessionVO[]): SessionGroup[] {
  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterdayStart = new Date(todayStart.getTime() - 86400000);
  const weekStart = new Date(todayStart.getTime() - todayStart.getDay() * 86400000);

  const groups: SessionGroup[] = [
    { label: "??", sessions: [] },
    { label: "??", sessions: [] },
    { label: "??", sessions: [] },
    { label: "??", sessions: [] },
  ];

  for (const session of sessions) {
    const time = session.updatedAt ? new Date(session.updatedAt) : null;
    if (!time || isNaN(time.getTime())) {
      groups[3].sessions.push(session); // ???????"??"
    } else if (time >= todayStart) {
      groups[0].sessions.push(session);
    } else if (time >= yesterdayStart) {
      groups[1].sessions.push(session);
    } else if (time >= weekStart) {
      groups[2].sessions.push(session);
    } else {
      groups[3].sessions.push(session);
    }
  }

  // ?????????
  return groups.filter((g) => g.sessions.length > 0);
}

const ChatTabContent: React.FC = () => {
  const navigate = useNavigate();
  const { chatSessions, loading, deleteChatSession, updateChatSession } = useChatSessions();
  const { agents } = useAgents();

  // inline ????
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editingTitle, setEditingTitle] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (editingId && inputRef.current) {
      inputRef.current.focus();
      inputRef.current.select();
    }
  }, [editingId]);

  // ?? agentId ? agent ???
  const agentMap = useMemo(() => {
    const map = new Map<string, string>();
    agents.forEach((agent) => {
      map.set(agent.id, agent.name);
    });
    return map;
  }, [agents]);

  // ?????
  const sessionGroups = useMemo(() => groupByTime(chatSessions), [chatSessions]);

  const handleCreateNewChat = () => {
    navigate("/chat");
  };

  const handleSelectChatSession = (chatSessionId: string) => {
    navigate(`/chat/${chatSessionId}`);
  };

  const handleDeleteChatSession = async (chatSessionId: string) => {
    await deleteChatSession(chatSessionId);
  };

  const handleStartEdit = (sessionId: string, currentTitle: string) => {
    setEditingId(sessionId);
    setEditingTitle(currentTitle);
  };

  const handleFinishEdit = async () => {
    if (editingId && editingTitle.trim()) {
      await updateChatSession(editingId, { title: editingTitle.trim() });
    }
    setEditingId(null);
    setEditingTitle("");
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditingTitle("");
  };

  // ???????
  const getDisplayTitle = (session: { title?: string; agentId: string }) => {
    if (session.title) {
      return session.title;
    }
    const agentName = agentMap.get(session.agentId);
    return agentName ? `? ${agentName} ???` : "???";
  };

  // ???????
  const renderSession = (session: ChatSessionVO) => (
    <div
      key={session.id}
      onClick={() => {
        if (editingId !== session.id) {
          handleSelectChatSession(session.id);
        }
      }}
      className="w-full px-3 py-2.5 rounded-lg bg-white dark:bg-gray-900 cursor-pointer transition-all hover:bg-gray-100 dark:hover:bg-gray-800 hover:shadow-sm group relative"
    >
      <div className="flex items-start gap-3">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-200 to-purple-200 dark:from-blue-900 dark:to-purple-900 flex items-center justify-center shrink-0 text-lg mt-0.5">
          <MessageOutlined />
        </div>
        <div className="flex-1 min-w-0">
          {editingId === session.id ? (
            <Input
              ref={inputRef as React.Ref<any>}
              size="small"
              value={editingTitle}
              onChange={(e) => setEditingTitle(e.target.value)}
              onPressEnter={handleFinishEdit}
              onBlur={handleFinishEdit}
              onKeyDown={(e) => {
                if (e.key === "Escape") handleCancelEdit();
              }}
              onClick={(e) => e.stopPropagation()}
            />
          ) : (
            <div className="font-medium text-gray-900 dark:text-gray-100 truncate">
              {getDisplayTitle(session)}
            </div>
          )}
        </div>
        {editingId !== session.id && (
          <div onClick={(e) => e.stopPropagation()}>
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              className="opacity-0 group-hover:opacity-100 transition-opacity shrink-0"
              onClick={() => handleStartEdit(session.id, getDisplayTitle(session))}
            />
            <Popconfirm
              title="?????????????"
              description="????????"
              onConfirm={() => handleDeleteChatSession(session.id)}
              okText="??"
              cancelText="??"
            >
              <Button
                type="text"
                size="small"
                icon={<DeleteOutlined />}
                className="opacity-0 group-hover:opacity-100 transition-opacity shrink-0"
                onClick={(e) => e.stopPropagation()}
                danger
              />
            </Popconfirm>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="flex flex-col h-full">
      <Button
        color="geekblue"
        variant="filled"
        icon={<PlusOutlined />}
        onClick={handleCreateNewChat}
        className="w-full"
      >
        ???
      </Button>
      <Divider />
      <div className="flex-1 min-h-0 overflow-y-auto bg-gray-50 dark:bg-gray-800 rounded-lg">
        {loading ? (
          <div className="p-2 space-y-3">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="flex items-center gap-3 px-2">
                <Skeleton.Avatar active shape="square" size={32} />
                <div className="flex-1">
                  <Skeleton.Input active block size="small" style={{ height: 16 }} />
                </div>
              </div>
            ))}
          </div>
        ) : sessionGroups.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-400 dark:text-gray-500">
            <MessageOutlined className="text-4xl mb-2" />
            <p className="text-sm">??????</p>
            <p className="text-xs mt-1">???????????</p>
          </div>
        ) : (
          <div className="p-1.5">
            {sessionGroups.map((group) => (
              <div key={group.label} className="mb-3">
                <div className="px-3 py-1.5 text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider">
                  {group.label}
                </div>
                <div className="space-y-1.5">
                  {group.sessions.map((session) => renderSession(session))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatTabContent;
