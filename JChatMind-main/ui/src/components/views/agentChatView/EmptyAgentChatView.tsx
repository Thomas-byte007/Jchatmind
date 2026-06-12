import React, { useState, useMemo } from "react";
import { Card, Space, Typography, Select } from "antd";
import {
  BulbOutlined,
  MessageOutlined,
  RobotOutlined,
  DownOutlined,
} from "@ant-design/icons";
import { Sender } from "@ant-design/x";
import { useNavigate } from "react-router-dom";
import {
  type AgentVO,
  createChatSession,
} from "../../../api/api.ts";
import { getAgentEmoji } from "../../../utils";
import { useChatSessions } from "../../../hooks/useChatSessions.ts";

const { Title, Text } = Typography;

interface DefaultAgentChatViewProps {
  handleSendMessage: (message: string) => void;
  loading: boolean;
  agents: AgentVO[];
}

const EmptyAgentChatView: React.FC<DefaultAgentChatViewProps> = ({
  loading,
  agents,
}) => {
  const [message, setMessage] = useState("");
  const [selectedAgentId, setSelectedAgentId] = useState<string | null>(null);

  const navigate = useNavigate();
  const { refreshChatSessions } = useChatSessions();

  // ??? agent ?? emoji
  const agentsWithEmoji = useMemo(() => {
    return agents.map((agent) => ({
      ...agent,
      emoji: getAgentEmoji(agent.id),
    }));
  }, [agents]);

  // ??????? agent ID(????????,?????????)
  const effectiveAgentId = useMemo(() => {
    if (selectedAgentId) {
      return selectedAgentId;
    }
    return agents.length > 0 ? agents[0].id : null;
  }, [selectedAgentId, agents]);

  return (
    <div className="flex flex-col h-full">
      {/* Agent ??? - ?? */}
      {agents.length > 0 && (
        <div className="border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 px-4 py-3">
          <div className="flex items-center justify-start">
            <Select
              value={effectiveAgentId}
              onChange={(value) => setSelectedAgentId(value)}
              style={{ width: 200 }}
              className="agent-selector"
              suffixIcon={<DownOutlined className="text-gray-400 dark:text-gray-500" />}
              placeholder="???????"
              optionRender={(option) => (
                <div className="flex items-center gap-2">
                  <span className="text-lg">
                    {agentsWithEmoji.find((a) => a.id === option.value)?.emoji}
                  </span>
                  <span className="text-sm">{option.label}</span>
                </div>
              )}
              options={agentsWithEmoji.map((agent) => ({
                value: agent.id,
                label: agent.name,
              }))}
            />
          </div>
        </div>
      )}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="max-w-2xl w-full space-y-6">
          <div className="text-center mb-8">
            <Title level={2} className="mb-2">
              ??????
            </Title>
            <Text type="secondary" className="text-base">
              ?????????????,????????????
            </Text>
          </div>
          <Space orientation="vertical" size="large" className="w-full">
            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-400 dark:from-blue-700 dark:to-purple-700 flex items-center justify-center">
                  <RobotOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    ????
                  </Title>
                  <Text type="secondary">
                    ? AI ????????,???????
                  </Text>
                </div>
              </Space>
            </Card>

            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-green-400 to-teal-400 dark:from-green-700 dark:to-teal-700 flex items-center justify-center">
                  <BulbOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    ????
                  </Title>
                  <Text type="secondary">
                    ?????????,???????
                  </Text>
                </div>
              </Space>
            </Card>

            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-orange-400 to-red-400 dark:from-orange-700 dark:to-red-700 flex items-center justify-center">
                  <MessageOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    ????
                  </Title>
                  <Text type="secondary">
                    ??????????,??????
                  </Text>
                </div>
              </Space>
            </Card>
          </Space>
        </div>
      </div>
      <div className="border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900">
        {/* ??? */}
        <div className="px-4 pb-4 pt-4">
          <Sender
            onSubmit={async () => {
              if (!effectiveAgentId) return;
              console.log("????", message);
              const response = await createChatSession({
                agentId: effectiveAgentId,
                title: message.slice(0, 20),
              });
              // ????????
              await refreshChatSessions();
              setMessage("");
              navigate(
                `/chat/${response.chatSessionId}`,
                {
                  state: {
                    init: true,
                    initMessage: message,
                  },
                },
              );
            }}
            value={message}
            loading={loading}
            placeholder="????????..."
            onChange={(value) => {
              setMessage(value);
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default EmptyAgentChatView;
