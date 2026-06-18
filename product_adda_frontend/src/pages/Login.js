import React, { useState } from "react";
import "../styles/Login.css";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

function Login() {
  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

const handleSubmit = (e) => {
  e.preventDefault();

  const savedUser = JSON.parse(
    localStorage.getItem("productAddaUser")
  );

  if (!savedUser) {
    toast.error("No registered user found. Please register first.");
    return;
  }

  if (
    email === savedUser.email &&
    password === savedUser.password
  ) {
toast.success("Login Successful 🎉");

  } else {
    toast.error("Invalid Email or Password");
  }
};
  return (
    <div className="login-page">
      <div className="login-card">

        <div className="login-left">
          <h1>Welcome Back!</h1>

          <p>
            Login to access your ProductAdda account and continue shopping
            from thousands of products.
          </p>

          <div className="login-features">
            <p>✓ Secure Shopping</p>
            <p>✓ Trusted Vendors</p>
            <p>✓ Fast Delivery</p>
          </div>
        </div>

        <div className="login-right">

          <h2>Login</h2>

          <form onSubmit={handleSubmit}>

            <label>Email Address</label>

            <input
              type="email"
              placeholder="Enter email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <label>Password</label>

            <div className="password-box">

              <input
                type={showPassword ? "text" : "password"}
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />

              <span
  className="eye-icon"
  onClick={() => setShowPassword(!showPassword)}
>
  
</span>

            </div>

            <Link
              to="/forgot-password"
              className="forgot-link"
            >
              Forgot Password?
            </Link>

            <button type="submit">
              Login
            </button>

            <p className="bottom-link">
              Don't have an account?
              <Link to="/register"> Register</Link>
            </p>

          </form>

        </div>

      </div>
    </div>
  );
}

export default Login;