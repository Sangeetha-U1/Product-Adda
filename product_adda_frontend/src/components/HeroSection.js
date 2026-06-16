import React from "react";
import "../styles/HeroSection.css";

function HeroSection() {
  return (
    <section className="hero">
      <div className="container">
        <div className="row align-items-center">
          
          {/* Left Content */}
          <div className="col-lg-6 col-md-6">
            <span className="hero-tag">
  Multi Vendor Marketplace
</span>
            <h1 className="hero-title">
              Buy More, Pay Less
            </h1>

            <p className="hero-text">
              Discover thousands of products from trusted vendors with the 
              best prices, amazing deals, and a seamless shopping experience.
            </p>

            <div className="hero-buttons">
              <button className="shop-btn">
                Shop Now
              </button>

              <button className="category-btn">
                Explore Categories
              </button>
            </div>
          </div>


          {/* Right Banner Image */}
          <div className="col-lg-6 col-md-6 text-center">
            <img
              src="https://images.unsplash.com/photo-1607082349566-187342175e2f"
              alt="Online Shopping"
              className="hero-image"
            />
          </div>

        </div>
      </div>
    </section>
  );
}

export default HeroSection;