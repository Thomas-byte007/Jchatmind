import React from "react";

interface SidebarProps {
  children: React.ReactNode;
  collapsed?: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ children, collapsed = false }) => {
  return (
    <div
      className="h-full bg-slate-50 dark:bg-slate-900 transition-all duration-300 ease-in-out overflow-hidden"
      style={{
        width: collapsed ? "60px" : "320px",
        minWidth: collapsed ? "60px" : "320px",
      }}
    >
      {children}
    </div>
  );
};

export default Sidebar;
