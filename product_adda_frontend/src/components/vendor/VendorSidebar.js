import React from "react";

import {
  FaTachometerAlt,
  FaBoxOpen,
  FaShoppingCart,
  FaUserTie,
  FaCog,
  FaSignOutAlt,
} from "react-icons/fa";

function VendorSidebar({
  activeTab,
  setActiveTab,
}) {
  return (
    <div className="vendor-sidebar">

      <div className="vendor-logo">
        <h2>ProductAdda</h2>
        <p>Vendor Panel</p>
      </div>

      <ul>

        <li
          className={
            activeTab === "dashboard"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveTab("dashboard")
          }
        >
          <FaTachometerAlt />
          Dashboard
        </li>

        <li
          className={
            activeTab === "products"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveTab("products")
          }
        >
          <FaBoxOpen />
          Products
        </li>

        <li
          className={
            activeTab === "orders"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveTab("orders")
          }
        >
          <FaShoppingCart />
          Orders
        </li>

        <li
          className={
            activeTab === "profile"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveTab("profile")
          }
        >
          <FaUserTie />
          Profile
        </li>

        <li
          className={
            activeTab === "settings"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveTab("settings")
          }
        >
          <FaCog />
          Settings
        </li>

        <li className="logout">
          <FaSignOutAlt />
          Logout
        </li>

      </ul>

    </div>
  );
}

export default VendorSidebar;