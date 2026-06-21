import React from "react";
import "../styles/HeroSection.css";
import { toast } from "react-toastify";
import { NavLink, useNavigate, Link } from "react-router-dom";

function HeroSection() {
    const navigate = useNavigate();

  return (
    <section className="hero">
      <div className="hero-content">

       <span className="offer-tag">
  Multi Vendor Marketplace
</span>
        <h1 className="hero-title">
          Discover Amazing Products <br />
          From Trusted Sellers
        </h1>

        <p className="hero-text">
          Shop thousands of products across fashion, electronics,
          beauty, home essentials and much more with exciting offers.
        </p>
  
        <div className="hero-buttons">
  <Link to="/products" className="shop-btn">
    Shop Now
  </Link>

  <Link to="/products" className="explore-btn">
    Explore Products
  </Link>
</div>
 <img 
            src="https://img.freepik.com/free-vector/online-shopping-concept-illustration_114360-1084.jpg"
            alt="Online Shopping"
            className="mobile-hero-image"
        />
      </div>
     
    </section>
  );
}


export default HeroSection;