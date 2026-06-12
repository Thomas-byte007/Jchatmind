import { createRoot } from "react-dom/client";
import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import App from "./App.tsx";
import { ThemeProvider, useTheme } from "./contexts/ThemeContext.tsx";
import "./index.css";

function ThemedApp() {
  const { antdAlgorithm } = useTheme();
  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        algorithm: antdAlgorithm,
      }}
    >
      <App />
    </ConfigProvider>
  );
}

createRoot(document.getElementById("root")!).render(
  <ThemeProvider>
    <ThemedApp />
  </ThemeProvider>
);
