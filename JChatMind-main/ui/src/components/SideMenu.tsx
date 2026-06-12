import React, { useState } from "react";
import {
  RobotOutlined,
  SunOutlined,
  MoonOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  TeamOutlined,
  MessageOutlined,
  BookOutlined,
} from "@ant-design/icons";
import { Tabs, type TabsProps, Button, Tooltip } from "antd";
import { useNavigate } from "react-router-dom";
import AgentTabContent from "./tabs/AgentTabContent.tsx";
import AddAgentModal from "./modals/AddAgentModal.tsx";
import ChatTabContent from "./tabs/ChatTabContent.tsx";
import KnowledgeBaseTabContent from "./tabs/KnowledgeBaseTabContent.tsx";
import AddKnowledgeBaseModal from "./modals/AddKnowledgeBaseModal.tsx";
import { useAgents } from "../hooks/useAgents.ts";
import { useKnowledgeBases } from "../hooks/useKnowledgeBases.ts";
import { useTheme } from "../contexts/ThemeContext.tsx";

interface SideMenuProps {
  collapsed?: boolean;
  onToggleCollapse?: () => void;
}

const SideMenu: React.FC<SideMenuProps> = ({ collapsed = false, onToggleCollapse }) => {
  const navigate = useNavigate();
  const { isDark, toggleTheme } = useTheme();

  const [isAddAgentModalOpen, setIsAddAgentModalOpen] = useState(false);
  const toggleAddAgentModal = () => {
    setIsAddAgentModalOpen(!isAddAgentModalOpen);
    setEditingAgent(null);
  };

  const [editingAgent, setEditingAgent] = useState<
    import("../api/api.ts").AgentVO | null
  >(null);

  /**
   * ??????????
   */
  const [isAddKnowledgeBaseModalOpen, setIsAddKnowledgeBaseModalOpen] =
    useState(false);
  const toggleAddKnowledgeBaseModal = () => {
    setIsAddKnowledgeBaseModalOpen(!isAddKnowledgeBaseModalOpen);
  };
  const { agents, createAgentHandle, deleteAgentHandle, updateAgentHandle } =
    useAgents();

  const [activeKey, setActiveKey] = useState(() => {
    if (location.pathname.startsWith("/agent")) return "agent";
    if (location.pathname.startsWith("/knowledge-base")) return "knowledgeBase";
    if (location.pathname.startsWith("/chat")) return "chat";
    return "agent";
  });

  const { knowledgeBases, createKnowledgeBaseHandle } = useKnowledgeBases();

  // ???????
  const handleTabChange = (key: string) => {
    setActiveKey(key);
  };

  const items: TabsProps["items"] = [
    {
      key: "agent",
      label: <span className="select-none">?????</span>,
      children: (
        <AgentTabContent
          agents={agents}
          onSelectAgent={() => {}}
          onCreateAgentClick={toggleAddAgentModal}
          onEditAgent={(agent) => {
            setEditingAgent(agent);
            setIsAddAgentModalOpen(true);
          }}
          onDeleteAgent={deleteAgentHandle}
        />
      ),
    },
    {
      key: "chat",
      label: <span className="select-none">????</span>,
      children: <ChatTabContent />,
    },
    {
      key: "knowledgeBase",
      label: <span className="select-none">???</span>,
      children: (
        <KnowledgeBaseTabContent
          knowledgeBases={knowledgeBases}
          onCreateKnowledgeBaseClick={toggleAddKnowledgeBaseModal}
          onSelectKnowledgeBase={(knowledgeBaseId) => {
            navigate(`/knowledge-base/${knowledgeBaseId}`);
          }}
        />
      ),
    },
  ];

  // ????:???????
  if (collapsed) {
    const navItems = [
      { key: "agent", icon: <TeamOutlined />, label: "?????" },
      { key: "chat", icon: <MessageOutlined />, label: "????" },
      { key: "knowledgeBase", icon: <BookOutlined />, label: "???" },
    ];

    return (
      <div className="flex flex-col h-full items-center py-3">
        <Tooltip title="?????" placement="right">
          <Button
            type="text"
            size="small"
            icon={<MenuUnfoldOutlined />}
            onClick={onToggleCollapse}
            className="mb-3"
          />
        </Tooltip>
        <div className="flex flex-col items-center gap-1 flex-1">
          {navItems.map((item) => (
            <Tooltip key={item.key} title={item.label} placement="right">
              <Button
                type={activeKey === item.key ? "primary" : "text"}
                icon={item.icon}
                onClick={() => setActiveKey(item.key)}
                className="w-10 h-10 flex items-center justify-center"
              />
            </Tooltip>
          ))}
        </div>
        <div className="flex flex-col items-center gap-1 mt-auto">
          <Tooltip title={isDark ? "??????" : "??????"} placement="right">
            <Button
              type="text"
              size="small"
              icon={isDark ? <SunOutlined /> : <MoonOutlined />}
              onClick={toggleTheme}
            />
          </Tooltip>
        </div>
      </div>
    );
  }

  // ????:?????
  return (
    <div className="px-4 flex flex-col h-full">
      <div className="h-14 w-full flex items-center justify-between border-b border-gray-200 dark:border-gray-700">
        <div className="flex items-center gap-2.5 mx-4">
          <RobotOutlined className="text-xl text-indigo-600 dark:text-indigo-400" />
          <div className="text-lg font-semibold select-none text-gray-900 dark:text-gray-100">
            JChatMind
          </div>
        </div>
        <div className="flex items-center gap-1 mr-4">
          <Tooltip title="?????">
            <Button
              type="text"
              size="small"
              icon={<MenuFoldOutlined />}
              onClick={onToggleCollapse}
            />
          </Tooltip>
          <Tooltip title={isDark ? "??????" : "??????"}>
            <Button
              type="text"
              size="small"
              icon={isDark ? <SunOutlined /> : <MoonOutlined />}
              onClick={toggleTheme}
            />
          </Tooltip>
        </div>
      </div>
      <div className="flex-1 min-h-0 flex flex-col">
        <Tabs
          activeKey={activeKey}
          onChange={handleTabChange}
          items={items}
          animated={{ inkBar: true, tabPane: true }}
        />
      </div>
      <AddAgentModal
        open={isAddAgentModalOpen}
        onClose={toggleAddAgentModal}
        createAgentHandle={createAgentHandle}
        updateAgentHandle={updateAgentHandle}
        editingAgent={editingAgent}
      />
      <AddKnowledgeBaseModal
        open={isAddKnowledgeBaseModalOpen}
        onClose={toggleAddKnowledgeBaseModal}
        createKnowledgeBaseHandle={createKnowledgeBaseHandle}
      />
    </div>
  );
};

export default SideMenu;
