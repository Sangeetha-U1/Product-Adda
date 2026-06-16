import { NavLink } from "react-router-dom";
import "./Navbar.css";
import logo from "../../assets/logo.png";
function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg navbar-dark custom-navbar">
      <div className="container">

<NavLink className="navbar-brand" to="/">
  <img
    src={logo}
    alt="ProductAdda Logo"
    className="logo"
  />
</NavLink>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarNav">

          <ul className="navbar-nav mx-auto">

            <li className="nav-item">
              <NavLink className="nav-link" to="/">
                Home
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className="nav-link" to="/products">
                Products
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className="nav-link" to="/categories">
                Categories
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className="nav-link" to="/cart">
                Cart
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className="nav-link" to="/contact">
                Contact
              </NavLink>
            </li>

          </ul>

          <form className="d-flex me-3">
            <input
              className="form-control"
              type="search"
              placeholder="Search Products"
            />
          </form>

<button className="btn-login me-2">
  Login
</button>

<button className="btn-register">
  Register
</button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;