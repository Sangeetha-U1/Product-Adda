function Login() {
  return (
    <div className="login-section">
      <div className="login-card">

        <h2 className="login-title">
          ProductAdda Login
        </h2>

        <form>

          <input
            type="email"
            className="login-input"
            placeholder="Enter your email"
          />

          <input
            type="password"
            className="login-input"
            placeholder="Enter your password"
          />

          <button className="login-btn">
            Login
          </button>

          <p style={{ textAlign: "center", marginTop: "15px" }}>
            Don't have an account? 
            <a href="/" style={{ color: "#007bff" }}>
              Register
            </a>
          </p>

        </form>

      </div>
    </div>
  );
}

export default Login;