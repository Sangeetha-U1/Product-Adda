import React, { useState } from "react";
import { Link } from "react-router-dom";
import "../styles/Register.css";

function Register() {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [mobile, setMobile] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    const cleanMobile = mobile.trim();

    // Mobile validation
    if (cleanMobile.length !== 10) {
      alert("Mobile number must contain exactly 10 digits");
      return;
    }

    // Password validation
    const passwordRegex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.#])[A-Za-z\d@$!%*?&.#]{8,}$/;

    if (!passwordRegex.test(password)) {
      alert(
        "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character."
      );
      return;
    }

    // Confirm password check
    if (password !== confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    // Save user data
    localStorage.setItem(
      "productAddaUser",
      JSON.stringify({
        email: email.trim(),
        mobile: cleanMobile,
        password,
      })
    );

    alert("Registration Successful");

    // Optional: clear form
    setEmail("");
    setMobile("");
    setPassword("");
    setConfirmPassword("");
  };

  return (
    <div className="register-page">
      <div className="register-card">

        {/* LEFT SIDE */}
        <div className="register-left">
                      <h1>Welcome to Our website</h1>

          <h1>Join ProductAdda</h1>

          <p>
            Create your account and explore thousands of products from trusted
            vendors across India.
          </p>

          <div className="register-features">
            <p>✓ Easy Registration</p>
            <p>✓ Secure Transactions</p>
            <p>✓ Multiple Vendors</p>
          </div>
        </div>

        {/* RIGHT SIDE */}
        <div className="register-right">
          <h2>Create Account</h2>

          <form onSubmit={handleSubmit}>

            <label>First Name</label>
            <input type="text" placeholder="Enter first name" required />

            <label>Last Name</label>
            <input type="text" placeholder="Enter last name" required />

            <label>Email Address</label>
            <input
              type="email"
              placeholder="Enter email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <label>Mobile Number</label>
            <input
              type="tel"
              maxLength="10"
              pattern="[0-9]{10}"
              placeholder="Enter mobile number"
              value={mobile}
              onChange={(e) => setMobile(e.target.value)}
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
                👁️
              </span>
            </div>

            <label>Confirm Password</label>
            <div className="password-box">
              <input
                type={showConfirmPassword ? "text" : "password"}
                placeholder="Confirm password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
              <span
                className="eye-icon"
                onClick={() =>
                  setShowConfirmPassword(!showConfirmPassword)
                }
              >
                👁️
              </span>
            </div>

            <button type="submit">Register</button>

            <p className="bottom-link">
              Already have an account? <Link to="/login">Login</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
}

export default Register;