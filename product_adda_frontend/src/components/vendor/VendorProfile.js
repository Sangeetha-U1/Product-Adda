import React from "react";
import {
  FaUserTie,
  FaEnvelope,
  FaPhone,
  FaMapMarkerAlt,
  FaBuilding,
} from "react-icons/fa";

function VendorProfile() {
  return (
    <div className="vendor-card">

      <div className="profile-header-center">

        <h2>Vendor Profile</h2>

        <p>
          Manage your business information
        </p>

        <img
          src="https://i.pravatar.cc/200?img=15"
          alt="vendor"
          className="vendor-profile-img"
        />

        <button className="vendor-edit-btn">
          Edit Profile
        </button>

      </div>

      <div className="vendor-profile-grid">

        <div className="vendor-info-card">
          <FaUserTie className="vendor-icon" />

          <div>
            <label>Vendor Name</label>
            <h4>Sangeetha</h4>
          </div>
        </div>

        <div className="vendor-info-card">
          <FaBuilding className="vendor-icon" />

          <div>
            <label>Business Name</label>
            <h4>ProductAdda Store</h4>
          </div>
        </div>

        <div className="vendor-info-card">
          <FaEnvelope className="vendor-icon" />

          <div>
            <label>Email</label>
            <h4>sangeetha@gmail.com</h4>
          </div>
        </div>

        <div className="vendor-info-card">
          <FaPhone className="vendor-icon" />

          <div>
            <label>Phone</label>
            <h4>9876543210</h4>
          </div>
        </div>

        <div className="vendor-info-card">
          <FaMapMarkerAlt className="vendor-icon" />

          <div>
            <label>Address</label>
            <h4>
              Hyderabad, Telangana,
              India
            </h4>
          </div>
        </div>

        <div className="vendor-info-card">
          <FaBuilding className="vendor-icon" />

          <div>
            <label>GST Number</label>
            <h4>
              29ABCDE1234F1Z5
            </h4>
          </div>
        </div>

      </div>

    </div>
  );
}

export default VendorProfile;