import React from "react";
import {
  FaUser,
  FaShoppingBag,
  FaMapMarkerAlt,
  FaCog,
  FaSignOutAlt,
} from "react-icons/fa";

function Sidebar({ activeTab, setActiveTab }) {
  const menus = [
    {
      id: "profile",
      label: "My Profile",
      icon: <FaUser />,
    },
    {
      id: "orders",
      label: "My Orders",
      icon: <FaShoppingBag />,
    },
    {
      id: "address",
      label: "Addresses",
      icon: <FaMapMarkerAlt />,
    },
    {
      id: "settings",
      label: "Settings",
      icon: <FaCog />,
    },
  ];

  return (
    <div className="dashboard-sidebar">

      <div className="sidebar-header">
        <h3>My Account</h3>
      </div>

      <ul>

        {menus.map((menu) => (
          <li
            key={menu.id}
            className={
              activeTab === menu.id
                ? "active"
                : ""
            }
            onClick={() =>
              setActiveTab(menu.id)
            }
          >
            {menu.icon}
            <span>{menu.label}</span>
          </li>
        ))}

        <li className="logout-item">
          <FaSignOutAlt />
          <span>Logout</span>
        </li>

      </ul>

    </div>
  );
}

export default Sidebar;