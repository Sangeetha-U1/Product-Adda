import { useState, useContext } from "react";
import { NavLink, useNavigate, Link } from "react-router-dom";
import { FaShoppingCart, FaUserCircle, FaTimes } from "react-icons/fa";
import "../styles/Navbar.css";
import logo from "../assets/logo.png";

import { CartContext } from "../context/CartContext";
import { SearchContext } from "../context/SearchContext";
import CartModal from "./CartModal";

function Navbar() {
  const [isNavbarOpen, setIsNavbarOpen] = useState(false);
  const [isCartOpen, setIsCartOpen] = useState(false);

  const { cart } = useContext(CartContext);
  const { searchQuery, setSearchQuery } = useContext(SearchContext);

  const navigate = useNavigate();

  const clearSearch = () => setSearchQuery("");

  const handleSearch = (e) => {
    if (e.key === "Enter") {
      navigate(`/products?search=${searchQuery}`);
      setIsNavbarOpen(false);
    }
  };

  return (
    <nav className="navbar navbar-expand-lg custom-navbar">
      <div className="container">

        {/* LOGO */}
        <NavLink className="navbar-brand" to="/">
          <img src={logo} alt="logo" className="logo" />
        </NavLink>

        {/* TOGGLER */}
        <button
          className="navbar-toggler"
          onClick={() => setIsNavbarOpen(!isNavbarOpen)}
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        {/* MENU */}
        <div className={`collapse navbar-collapse ${isNavbarOpen ? "show" : ""}`}>

          {/* LINKS */}
          <ul className="navbar-nav mx-auto">
            <li><NavLink className="nav-link" to="/">Home</NavLink></li>
            <li><NavLink className="nav-link" to="/products">Products</NavLink></li>
            <li><NavLink className="nav-link" to="/categories">Categories</NavLink></li>
            <li><NavLink className="nav-link" to="/contact">Contact</NavLink></li>
          </ul>

          {/* SEARCH */}
          <div className="search-box">
            <input
              type="text"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={handleSearch}
              className="search-input"
            />

            {searchQuery && (
              <FaTimes
                className="clear-icon"
                onClick={clearSearch}
              />
            )}
          </div>

          {/* ICONS */}
          <div className="icon-group">

            {/* CART */}
            <button
              className="icon-btn"
              onClick={() => setIsCartOpen(true)}
            >
              <FaShoppingCart />
              {cart?.length > 0 && (
                <span className="cart-badge">{cart.length}</span>
              )}
            </button>

            {/* PROFILE */}
            <Link to="/dashboard" className="icon-btn">
  <FaUserCircle />
</Link>

          </div>

          {/* LOGIN */}
          <button
            className="btn-login"
            onClick={() => navigate("/login")}
          >
            Login
          </button>

        </div>
      </div>

      {/* CART MODAL */}
      <CartModal isOpen={isCartOpen} setIsOpen={setIsCartOpen} />
    </nav>
  );
}

export default Navbar;