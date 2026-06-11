import React from "react";

function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <a className="navbar-brand" href="/">
          ProductAdda
        </a>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarContent"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarContent">
          <ul className="navbar-nav ms-auto">
            <li className="nav-item">
              <a className="nav-link" href="/">
                Home
              </a>
            </li>

            <li className="nav-item">
              <a className="nav-link" href="/">
                Products
              </a>
            </li>

            <li className="nav-item">
              <a className="nav-link" href="/">
                Categories
              </a>
            </li>

            <li className="nav-item">
              <a className="nav-link" href="/">
                Cart
              </a>
            </li>

            <li className="nav-item">
              <a className="nav-link" href="/">
                Contact
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;