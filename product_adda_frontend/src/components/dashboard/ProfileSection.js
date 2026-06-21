import React from "react";
import {
  FaEnvelope,
  FaPhone,
  FaMapMarkerAlt,
  FaUser,
} from "react-icons/fa";
import { useNavigate } from "react-router-dom";
function ProfileSection() {
  const navigate = useNavigate();
  return (
    <div className="dashboard-section">

      <div className="section-header profile-header">
        <h2>My Profile</h2>
        <p>Manage your personal information</p>
      </div>

      <div className="profile-top">

        <img
          src="https://i.pravatar.cc/200?img=12"
          alt="profile"
          className="profile-image"
        />

        <button className="edit-profile-btn">
          Edit Profile
        </button>

      </div>

      <div className="profile-details">

        <div className="info-card">
          <FaUser className="info-icon" />
          <div>
            <label>Full Name</label>
            <h4>Sangeetha</h4>
          </div>
        </div>

        <div className="info-card">
          <FaEnvelope className="info-icon" />
          <div>
            <label>Email Address</label>
            <h4>sangeetha@gmail.com</h4>
          </div>
        </div>

        <div className="info-card">
          <FaPhone className="info-icon" />
          <div>
            <label>Mobile Number</label>
            <h4>9876543210</h4>
          </div>
        </div>

        <div className="info-card">
          <FaMapMarkerAlt className="info-icon" />
          <div>
            <label>Address</label>
            <h4>Hyderabad, Telangana, India</h4>
          </div>
        </div>
      </div>
<div className="vendor-panel-section">

  <h3>Seller Account</h3>

  <p>
    Want to manage products, orders and
    business details? Open Vendor Dashboard.
  </p>

  <button
    className="vendor-panel-btn"
    onClick={() =>
      navigate("/vendor-dashboard")
    }
  >
    Open Vendor Dashboard
  </button>

</div>

    </div>
    
  );
}

export default ProfileSection;