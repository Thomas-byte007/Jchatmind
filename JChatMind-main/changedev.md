# 前端代码变更日志

## 2026-06-10 14:30:00 — 模型配置更新：glm-4.6 → glm-5.1

| 文件 | 修改内容 |
|------|---------|
| `ui/src/api/api.ts` | `ModelType` 类型：`"glm-4.6"` → `"glm-5.1"` |
| `ui/src/components/modals/AddAgentModal.tsx` | 模型下拉选项：glm-4.6 → glm-5.1 |

---

## 2026-06-10 15:00:00 — 前端 SSE AI_ERROR 类型未处理修复

| 文件 | 修改内容 |
|------|---------|
| `ui/src/components/views/AgentChatView.tsx` | 新增 `AI_ERROR` 分支处理，显示错误状态文本，不再 throw |
| `ui/src/types/index.ts` | `SseMessageType` 新增 `"AI_ERROR"` |

---

## 2026-06-10 17:00:00 — SSE 消息类型规范化

| 文件 | 修改内容 |
|------|---------|
| `ui/src/components/views/AgentChatView.tsx` | 将 if-else 链重构为 `handleSseMessage` 函数 + switch-case，合并 PLANNING/THINKING/EXECUTING 三种状态类型处理，未知类型走 console.warn 不崩溃 |

---

## 2026-06-10 18:00:00 — 知识库拖拽上传 + 多文件 + 进度条

| 文件 | 修改内容 |
|------|---------|
| `ui/src/components/views/KnowledgeBaseView.tsx` | Upload 按钮替换为 `Upload.Dragger` 拖拽区域；支持 `multiple` 多文件批量上传；新增 `UploadingFile` 状态追踪每个文件的上传进度；`accept` 扩展为 `.md,.txt,.markdown` |

---

## 2026-06-10 19:00:00 — 修复历史对话不显示（前端部分）

| 文件 | 修改内容 |
|------|---------|
| `ui/src/components/views/AgentChatView.tsx` | `getChatMessages()` 加 try-catch 错误处理；消息和会话信息改为 `Promise.all` 并行加载，解决 agentId 时序问题 |
| `ui/src/types/index.ts` | 新增 `ToolCall`（id/type/name/arguments）、`ToolResponse`（id/name/responseData）、`ChatMessageVOMetadata` 接口定义，与后端 `ChatMessageDTO` 自定义 MetaData 类型对应 |

---

## 2026-06-12 11:00:00 — P0 前端动态 UI 优化（第一批）

| 文件 | 修改内容 |
|------|---------|
| `ui/.env` | 新增 `VITE_SSE_BASE_URL` 环境变量，SSE 地址不再硬编码 |
| `ui/src/components/views/AgentChatView.tsx` | SSE 地址改用 `import.meta.env.VITE_SSE_BASE_URL`；新增 `messagesLoading` 状态，加载时显示 Skeleton 骨架屏替代空白 |
| `ui/src/components/views/agentChatView/AgentChatHistory.tsx` | 引入 `@tanstack/react-virtual` 实现虚拟滚动，只渲染可视区域消息（overscan=5），长对话性能优化；提取 `renderMessage()` 函数 |
| `ui/package.json` | 新增依赖 `@tanstack/react-virtual` |

---

## 2026-06-12 11:30:00 — P1 前端动态 UI 优化（第二批）

| 文件 | 修改内容 |
|------|---------|
| `ui/src/components/views/agentChatView/AgentChatHistory.tsx` | XMarkdown `enableAnimation` 从 `false` 改为 `true`，启用打字动画效果 |
| `ui/src/components/views/AgentChatView.tsx` | SSE 连接增加指数退避自动重连（MAX_RETRIES=5, BASE_DELAY=1s）；新增 `sseReconnecting` 状态；连接状态提示条（重连中/连接中）；超过最大重试次数时提示用户刷新 |

---

## 2026-06-12 11:45:00 — P1 前端动态 UI 优化（第二批·续）— 暗色模式基础设施

| 文件 | 修改内容 |
|------|---------|
| `ui/src/main.tsx` | ConfigProvider 添加 `theme.algorithm`，根据 `document.documentElement.classList.contains("dark")` 切换 `darkAlgorithm` / `defaultAlgorithm` |
| `ui/src/components/SideMenu.tsx` | 新增 `isDark` 状态 + `toggleDark` 函数 + `localStorage` 持久化 + `SunOutlined`/`MoonOutlined` 切换按钮 |
| `ui/tailwind.config.js` | 添加 `darkMode: "class"` 配置 |
| `ui/src/index.css` | 添加 `.dark` 和 `.dark body` 基础样式（color-scheme + 背景色 + 文字色） |

---

## 2026-06-12 12:30:00 — P2 前端动态 UI 优化（第三批）

| 文件 | 修改内容 |
|------|---------|
| `ui/src/components/SideMenu.tsx` | Tabs 组件添加 `animated={{ inkBar: true, tabPane: true }}` 启用标签页切换过渡动画 |
| `ui/src/components/tabs/ChatTabContent.tsx` | 加载状态从纯文字"加载中..."替换为 Skeleton 骨架屏（4 行 Avatar + Input 组合） |
| `ui/src/layout/Sidebar.tsx` | 新增 `collapsed` 属性，折叠时宽度 60px / 展开 320px，添加 `transition-all duration-300` 过渡动画 |
| `ui/src/components/JChatMindLayout.tsx` | 新增 `sidebarCollapsed` 状态管理，传递 `collapsed` 和 `onToggleCollapse` 给 Sidebar 和 SideMenu |
| `ui/src/components/SideMenu.tsx` | 新增 `collapsed` / `onToggleCollapse` props；折叠模式显示图标导航（TeamOutlined/MessageOutlined/BookOutlined）+ Tooltip；展开模式顶部新增 MenuFoldOutlined 折叠按钮 |
| `ui/src/index.css` | 新增 `@keyframes messageFadeIn` 动画（opacity 0→1 + translateY 8px→0）和 `.message-enter` class |
| `ui/src/components/views/agentChatView/AgentChatHistory.tsx` | 新增 `lastMessageIdRef` 追踪最新消息；虚拟滚动渲染中最新消息添加 `message-enter` 入场动画 class |

---

## 2026-06-12 13:00:00 — P3 前端动态 UI 优化（第四批）

| 文件 | 修改内容 |
|------|---------|
| `ui/src/components/views/agentChatView/AgentChatHistory.tsx` | 引入 `Actions` 组件和 `antdMessage`；assistant 消息 Bubble 添加 `footer` 操作按钮（复制 + 重新生成），`footerPlacement="outer-end"`；新增 `onRegenerate` prop 回调 |
| `ui/src/components/views/AgentChatView.tsx` | AgentChatHistory 传入 `onRegenerate` 回调：查找 assistant 消息前的 user 消息并重新发送 |
| `ui/src/components/JChatMindLayout.tsx` | 新增 `isSmallScreen` 状态 + `resize` 事件监听（breakpoint 768px）；小屏幕自动折叠侧边栏；`effectiveCollapsed` 合并用户操作和响应式状态 |
| `ui/src/components/modals/AddAgentModal.tsx` | Modal `width` 改为响应式：`<768px` 时 `95vw`，否则 `800` |
| `ui/src/components/modals/AddKnowledgeBaseModal.tsx` | Modal `width` 改为响应式：`<768px` 时 `95vw`，否则 `600` |

---

## 2026-06-12 14:30:00 — P4 前端动态 UI 优化（第五批）

| 文件 | 修改内容 |
|------|---------|
| `ui/src/contexts/ThemeContext.tsx` | **新建**；ThemeContext/ThemeProvider/useTheme；mode/isDark/toggleTheme/setTheme/antdAlgorithm；localStorage 持久化 + prefers-color-scheme 检测 |
| `ui/src/main.tsx` | ThemeProvider 包裹 App；ThemedApp 组件从 useTheme() 读取 antdAlgorithm 传给 ConfigProvider；消除硬编码 DOM 读取 |
| `ui/src/components/SideMenu.tsx` | 删除本地 isDark/toggleDark/window.location.reload()；改用 useTheme() 的 isDark/toggleTheme；删除 useEffect 初始化暗色；3 处 dark: 变体（border-gray-700/text-indigo-400/text-gray-100） |
| `ui/src/components/views/AgentChatView.tsx` | 3 处 dark: 变体：amber 重连提示（bg-amber-950/border-amber-800/text-amber-300）、gray 连接提示（bg-gray-800/border-gray-700/text-gray-400）、输入区（border-gray-700/bg-gray-900） |
| `ui/src/components/views/agentChatView/AgentChatHistory.tsx` | ~15 处 dark: 变体：ToolCallDisplay（text-gray-400/text-blue-400）、ToolResponseDisplay（hover:text-gray-300/bg-gray-800/border-gray-700/text-gray-300）、system 消息（bg-gray-800/text-gray-300）、状态指示（text-blue-400/text-gray-500/text-gray-300） |
| `ui/src/components/views/KnowledgeBaseView.tsx` | 8 处 dark: 变体：删除按钮（text-red-400/hover:text-red-300）、知识库图标渐变（from-blue-900/to-purple-900）、描述文字（text-gray-300）、上传区（text-blue-300/text-gray-300/text-gray-500）、上传进度（bg-gray-800/text-gray-400） |
| `ui/src/components/tabs/ChatTabContent.tsx` | 5 处 dark: 变体：列表背景（bg-gray-800）、空状态（text-gray-500）、会话卡片（bg-gray-900/hover:bg-gray-800）、图标渐变（from-blue-900/to-purple-900）、标题（text-gray-100） |
| `ui/src/components/tabs/AgentTabContent.tsx` | 8 处 dark: 变体：列表背景（bg-gray-800）、空状态（text-gray-500）、Agent 卡片（bg-gray-900/hover:bg-gray-800）、图标渐变（from-yellow-900/to-orange-900）、标题（text-gray-100）、描述（text-gray-400）、时间（text-gray-500）、菜单按钮（text-gray-500/hover:text-gray-300） |
| `ui/src/components/views/agentChatView/EmptyAgentChatView.tsx` | 6 处 dark: 变体：顶部栏（border-gray-700/bg-gray-900）、下拉箭头（text-gray-500）、功能卡片渐变（from-blue-700/to-purple-700, from-green-700/to-teal-700, from-orange-700/to-red-700）、底部输入区（border-gray-700/bg-gray-900） |
| `ui/src/layout/Sidebar.tsx` | 1 处 dark: 变体：bg-slate-900 |
| `ui/src/index.css` | 新增 Ant Design 暗色覆盖：.dark .ant-card（#1f1f1f/#303030）、.dark .ant-table（#1f1f1f）、.dark .ant-upload-drag（#1f1f1f/#434343） |

---

## 2026-06-12 15:30:00 — P5 前端优化（会话管理 + 重新生成修复）

| 文件 | 修改内容 |
|------|---------|
| `ui/src/components/views/AgentChatView.tsx` | 引入 `deleteChatMessage` API；重写 `onRegenerate`：先删除该 assistant 消息及后续所有消息（逐条调用 deleteChatMessage），更新本地 messages 状态，再重新发送用户消息（SSE 自动推送新回复） |
| `ui/src/contexts/ChatSessionsContext.tsx` | 引入 `UpdateChatSessionRequest`/`updateChatSession`；接口新增 `updateChatSession` 方法；Provider 新增 `updateChatSessionHandle`（调用 API 后刷新列表） |
| `ui/src/components/tabs/ChatTabContent.tsx` | 引入 `Input`/`EditOutlined`；新增 `editingId`/`editingTitle` 状态 + `inputRef`；新增 `handleStartEdit`/`handleFinishEdit`/`handleCancelEdit`；编辑中标题替换为 Input（Enter 提交/ESC 取消/Blur 提交）；hover 时显示 EditOutlined 编辑按钮（与删除按钮并列）；编辑中阻止 onClick 导航 |

---

## 2026-06-12 16:00:00 — P5-2 会话按时间分组展示

| 文件 | 修改内容 |
|------|---------|
| `ui/src/api/api.ts` | `ChatSessionVO` 接口新增 `createdAt?: string` 和 `updatedAt?: string` |
| `ui/src/components/tabs/ChatTabContent.tsx` | 新增 `SessionGroup` 接口和 `groupByTime()` 函数（今天/昨天/本周/更早四组）；`sessionGroups` useMemo 缓存分组结果；渲染改为分组标题 + 分组内会话列表；提取 `renderSession()` 复用渲染逻辑 |

---

## 2026-06-12 17:00:00 — P5-3 消息分页加载

| 文件 | 修改内容 |
|------|---------|
| `ui/src/api/api.ts` | `GetChatMessagesResponse` 新增 `total: number`；新增 `getChatMessagesBySessionIdPaginated(sessionId, limit, offset)` 函数 |
| `ui/src/components/views/AgentChatView.tsx` | 引入 `getChatMessagesBySessionIdPaginated`；新增 `hasMore`/`loadingMore`/`totalMessages`/`PAGE_SIZE` 状态；`getChatMessages` 改为分页加载（初始加载最新 20 条）；新增 `loadMoreMessages` 回调（前置加载历史消息）；传递 `hasMore`/`loadingMore`/`onLoadMore` 给 AgentChatHistory |
| `ui/src/components/views/agentChatView/AgentChatHistory.tsx` | Props 新增 `hasMore`/`loadingMore`/`onLoadMore`；`handleScroll` 增加 scrollTop <= 50 时触发加载更多（保持滚动位置）；顶部新增"加载更多"提示文字 |
