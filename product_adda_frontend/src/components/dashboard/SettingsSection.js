import React, { useState } from "react";

function SettingsSection() {
  const [formData, setFormData] =
    useState({
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    });

  const [errors, setErrors] =
    useState({});

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]:
        e.target.value,
    });
  };

  const validateForm = () => {
    let newErrors = {};

    if (!formData.currentPassword) {
      newErrors.currentPassword =
        "Current password is required";
    }

    if (!formData.newPassword) {
      newErrors.newPassword =
        "New password is required";
    }

    if (
      formData.newPassword.length < 6
    ) {
      newErrors.newPassword =
        "Minimum 6 characters required";
    }

    if (
      formData.confirmPassword !==
      formData.newPassword
    ) {
      newErrors.confirmPassword =
        "Passwords do not match";
    }

    setErrors(newErrors);

    return (
      Object.keys(newErrors).length ===
      0
    );
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (validateForm()) {
      alert(
        "Password Updated Successfully"
      );

      setFormData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    }
  };

  return (
    <div className="dashboard-section">

      <div className="section-header">
        <h2>Settings</h2>
        <p>
          Manage your account settings
        </p>
      </div>

      <form
        className="settings-form"
        onSubmit={handleSubmit}
      >

        <div className="form-group">
          <label>
            Current Password
          </label>

          <input
            type="password"
            name="currentPassword"
            value={
              formData.currentPassword
            }
            onChange={handleChange}
          />

          {errors.currentPassword && (
            <small className="error-text">
              {
                errors.currentPassword
              }
            </small>
          )}
        </div>

        <div className="form-group">
          <label>
            New Password
          </label>

          <input
            type="password"
            name="newPassword"
            value={
              formData.newPassword
            }
            onChange={handleChange}
          />

          {errors.newPassword && (
            <small className="error-text">
              {errors.newPassword}
            </small>
          )}
        </div>

        <div className="form-group">
          <label>
            Confirm Password
          </label>

          <input
            type="password"
            name="confirmPassword"
            value={
              formData.confirmPassword
            }
            onChange={handleChange}
          />

          {errors.confirmPassword && (
            <small className="error-text">
              {
                errors.confirmPassword
              }
            </small>
          )}
        </div>

        <button
          type="submit"
          className="save-btn"
        >
          Save Changes
        </button>

      </form>

    </div>
  );
}

export default SettingsSection;