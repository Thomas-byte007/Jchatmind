import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from "react";
import { theme as antdTheme } from "antd";

type ThemeMode = "light" | "dark";

interface ThemeContextValue {
  mode: ThemeMode;
  isDark: boolean;
  toggleTheme: () => void;
  setTheme: (mode: ThemeMode) => void;
  antdAlgorithm: typeof antdTheme.darkAlgorithm | typeof antdTheme.defaultAlgorithm;
}

const ThemeContext = createContext<ThemeContextValue | null>(null);

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error("useTheme must be used within ThemeProvider");
  return ctx;
}

function getInitialMode(): ThemeMode {
  const saved = localStorage.getItem("theme");
  if (saved === "dark" || saved === "light") return saved;
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<ThemeMode>(getInitialMode);

  useEffect(() => {
    document.documentElement.classList.toggle("dark", mode === "dark");
    localStorage.setItem("theme", mode);
  }, [mode]);

  const toggleTheme = useCallback(() => {
    setMode((prev) => (prev === "dark" ? "light" : "dark"));
  }, []);

  const isDark = mode === "dark";
  const antdAlgorithm = isDark ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm;

  return (
    <ThemeContext.Provider value={{ mode, isDark, toggleTheme, setTheme: setMode, antdAlgorithm }}>
      {children}
    </ThemeContext.Provider>
  );
}
