import { useState, useEffect } from "react";
import { Routes, Route } from "react-router-dom";
import Layout from "../layout/Layout.tsx";
import Sidebar from "../layout/Sidebar.tsx";
import SideMenu from "./SideMenu.tsx";
import Content from "../layout/Content.tsx";
import AgentChatView from "./views/AgentChatView.tsx";
import KnowledgeBaseView from "./views/KnowledgeBaseView.tsx";

const SIDEBAR_BREAKPOINT = 768;

export default function JChatMindLayout() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [isSmallScreen, setIsSmallScreen] = useState(false);

  useEffect(() => {
    const checkWidth = () => {
      const small = window.innerWidth < SIDEBAR_BREAKPOINT;
      setIsSmallScreen(small);
      if (small) {
        setSidebarCollapsed(true);
      }
    };
    checkWidth();
    window.addEventListener("resize", checkWidth);
    return () => window.removeEventListener("resize", checkWidth);
  }, []);

  const effectiveCollapsed = sidebarCollapsed || isSmallScreen;

  return (
    <Layout>
      <Sidebar collapsed={effectiveCollapsed}>
        <SideMenu
          collapsed={effectiveCollapsed}
          onToggleCollapse={() => {
            if (!isSmallScreen) {
              setSidebarCollapsed(!sidebarCollapsed);
            }
          }}
        />
      </Sidebar>
      <Content>
        <Routes>
          <Route path="/" element={<AgentChatView />} />
          <Route path="/agent" element={<AgentChatView />} />
          <Route path="/chat" element={<AgentChatView />} />
          <Route path="/chat/:chatSessionId" element={<AgentChatView />} />
          <Route path="/knowledge-base" element={<KnowledgeBaseView />} />
          <Route
            path="/knowledge-base/:knowledgeBaseId"
            element={<KnowledgeBaseView />}
          />
        </Routes>
      </Content>
    </Layout>
  );
}
