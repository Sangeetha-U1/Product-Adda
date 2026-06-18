import React from "react";
import "../styles/WhyChooseUs.css";

function WhyChooseUs() {
  return (
    <section className="why-choose">
      <div className="container">

        <h2 className="section-title">
          Why Choose ProductAdda?
        </h2>

        <div className="why-grid">

          <div className="why-card">
            <div className="icon">🚚</div>
            <h3>Fast Delivery</h3>
            <p>
              Get products delivered quickly and safely
              to your doorstep.
            </p>
          </div>

          <div className="why-card">
            <div className="icon">🔒</div>
            <h3>Secure Payment</h3>
            <p>
              Multiple secure payment methods with
              complete protection.
            </p>
          </div>

          <div className="why-card">
            <div className="icon">⭐</div>
            <h3>Quality Products</h3>
            <p>
              Verified vendors offering high-quality
              genuine products.
            </p>
          </div>

          <div className="why-card">
            <div className="icon">💬</div>
            <h3>24/7 Support</h3>
            <p>
              Dedicated customer support whenever
              you need assistance.
            </p>
          </div>

        </div>
      </div>
    </section>
  );
}

export default WhyChooseUs;