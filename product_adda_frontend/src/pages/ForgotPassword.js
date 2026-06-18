import React from "react";
import { Link } from "react-router-dom";
import "../styles/ForgotPassword.css";
import { toast } from "react-toastify";
function ForgotPassword() {

  const handleSubmit = (e) => {
  e.preventDefault();

  toast.success("Reset link sent 📩");

  setTimeout(() => {
    toast.info("Redirecting to Login...");
  }, 1000);
};

  return (
    <div className="forgot-page">

      <div className="forgot-card">

        <h2>Forgot Password?</h2>

        <p>
          Enter your registered email address and
          we'll send you a password reset link.
        </p>

        <form onSubmit={handleSubmit}>

          <label>Email Address</label>

          <input
            type="email"
            placeholder="Enter your email"
            required
          />

          <button type="submit">
            Send Reset Link
          </button>

        </form>

        <p className="back-login">
          Back to
          <Link to="/login"> Login</Link>
        </p>

      </div>

    </div>
  );
}

export default ForgotPassword;