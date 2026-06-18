import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";import "../styles/Navbar.css";
import logo from "../assets/logo.png";
import { FaTimes } from "react-icons/fa";
import { toast } from "react-toastify";

function Navbar() {
const [open, setOpen] = useState(false);
const [search, setSearch] = useState("");
const clearSearch = () => {
  setSearch("");
  navigate("/products");
};
const navigate = useNavigate();
const handleSearch = (e) => {
  e.preventDefault();

  if (search.trim() !== "") {
    navigate(`/products?search=${search}`);
  }
};
  return (
    <nav className="navbar navbar-expand-lg navbar-light custom-navbar">
      <div className="container">

        <NavLink className="navbar-brand" to="/">
          <img src={logo} alt="ProductAdda Logo" className="logo" />
        </NavLink>

        <button
          className="navbar-toggler"
          type="button"
          onClick={() => setOpen(!open)}
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className={`collapse navbar-collapse ${open ? "show" : ""}`}>

          <ul className="navbar-nav mx-auto">

            <li className="nav-item">
              <NavLink className="nav-link" to="/" onClick={() => setOpen(false)}>
                Home
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className="nav-link" to="/products" onClick={() => setOpen(false)}>
                Products
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className="nav-link" to="/categories" onClick={() => setOpen(false)}>
                Categories
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className="nav-link" to="/cart" onClick={() => setOpen(false)}>
                Cart
              </NavLink>
            </li>

            <li className="nav-item">
              <NavLink className="nav-link" to="/contact" onClick={() => setOpen(false)}>
                Contact
              </NavLink>
            </li>

          </ul>
<form className="d-flex me-3" onSubmit={handleSearch}>
  <div className="search-box">

    <input
      className="form-control"
      type="search"
      placeholder="Search Products"
      value={search}
      onChange={(e) => setSearch(e.target.value)}
    />

    {search && (
      <span
        className="clear-icon"
        onClick={() => setSearch("")}
      />
    )}

  </div>

  <button
    type="submit"
    className="btn btn-primary ms-2"
  >
    Search
  </button>
</form>
<button className="btn-login me-2" onClick={() => navigate("/login")}>
  Login
</button>

<button className="btn-register" onClick={() => navigate("/register")}>
  Register
</button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;