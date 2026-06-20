import React, { useState } from "react";

import Sidebar from "../components/dashboard/Sidebar";
import ProfileSection from "../components/dashboard/ProfileSection";
import OrdersSection from "../components/dashboard/OrdersSection";
import AddressSection from "../components/dashboard/AddressSection";
import SettingsSection from "../components/dashboard/SettingsSection";

import "../styles/Dashboard.css";

function Dashboard() {
  const [activeTab, setActiveTab] =
    useState("profile");

  const renderContent = () => {
    switch (activeTab) {
      case "profile":
        return <ProfileSection />;

      case "orders":
        return <OrdersSection />;

      case "address":
        return <AddressSection />;

      case "settings":
        return <SettingsSection />;

      default:
        return <ProfileSection />;
    }
  };

  return (
    <div className="dashboard-layout">

      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      <div className="dashboard-content">
        {renderContent()}
      </div>

    </div>
  );
}

export default Dashboard;