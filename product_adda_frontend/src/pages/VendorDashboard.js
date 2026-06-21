import React, { useState } from "react";

import VendorSidebar from "../components/vendor/VendorSidebar";
import VendorHome from "../components/vendor/VendorHome";
import VendorProducts from "../components/vendor/VendorProducts";
import VendorOrders from "../components/vendor/VendorOrders";
import VendorProfile from "../components/vendor/VendorProfile";
import VendorSettings from "../components/vendor/VendorSettings";
import { useNavigate } from "react-router-dom";
import { FaArrowLeft } from "react-icons/fa";
import "../styles/VendorDashboard.css";

function VendorDashboard() {
    const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("dashboard");

  const renderContent = () => {
    switch (activeTab) {
      case "dashboard":
        return <VendorHome />;

      case "products":
        return <VendorProducts />;

      case "orders":
        return <VendorOrders />;

      case "profile":
        return <VendorProfile />;

      case "settings":
        return <VendorSettings />;

      default:
        return <VendorHome />;
    }
  };

  return (
    <div className="vendor-dashboard">

      <VendorSidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      <div className="vendor-content">
        <div className="back-dashboard">
  <button
    className="back-btn"
    onClick={() => navigate("/dashboard")}
  >
    <FaArrowLeft />
    Back to User Dashboard
  </button>
</div>
        {renderContent()}
      </div>

    </div>
  );
}

export default VendorDashboard;