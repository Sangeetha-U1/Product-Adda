import { NavLink } from "react-router-dom";
import "../styles/Footer.css";
import { FaFacebook, FaInstagram, FaLinkedin, FaTwitter } from "react-icons/fa";
import logo from "../assets/logo.png";

function Footer() {
  return (
    <footer className="footer">
      <div className="container">

        <div className="row">

          {/* Company Info */}
          <div className="col-md-4 mb-4">
<div className="footer-logo-box">
    <img
        src={logo}
        alt="ProductAdda Logo"
        className="footer-logo"
    />
</div>            <p>
              Your One Stop Shopping Destination.
              Discover products, categories, and amazing deals.
            </p>
          </div>

          {/* Quick Links */}
          <div className="col-md-4 mb-4">
            <h4>Quick Links</h4>

            <ul className="footer-links">
              <li><a href="/">Home</a></li>
              <li><a href="/products">Products</a></li>
              <li><a href="/categories">Categories</a></li>
              <li><a href="/cart">Cart</a></li>
              <li><a href="/contact">Contact</a></li>
            </ul>
          </div>

          {/* Contact Info */}
          <div className="col-md-4 mb-4">
            <h4>Contact Us</h4>

            <p>Email: support@productadda.com</p>
            <p>Phone: +91 9876543210</p>
            <p>Location: Hyderabad, India</p>

            <div className="social-icons">
              <a href="#"><FaFacebook /></a>
              <a href="#"><FaInstagram /></a>
              <a href="#"><FaLinkedin /></a>
              <a href="#"><FaTwitter /></a>
            </div>
          </div>

        </div>

        <hr />

        <div className="footer-bottom">
          <p>© 2026 ProductAdda. All Rights Reserved.</p>
        </div>

      </div>
    </footer>
  );
}

export default Footer;