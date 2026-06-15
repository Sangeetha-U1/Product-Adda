function Home() {
  return (
    <div>

      {/* Hero Section */}
      <div className="hero-section">
        <div>
          <h1>Welcome to ProductAdda</h1>
          <p>Your One-Stop Multi Vendor Marketplace Platform</p>
          <button className="hero-btn">
            Explore Products
          </button>
        </div>
      </div>

      {/* Features Section */}
      <div className="container features">
        <div className="row">

          <div className="col-md-4 mb-4">
            <div className="feature-card">
              <h3> Fast Delivery</h3>
              <p>Get products delivered quickly and safely.</p>
            </div>
          </div>

          <div className="col-md-4 mb-4">
            <div className="feature-card">
              <h3>Secure Payment</h3>
              <p>100% secure and trusted payment methods.</p>
            </div>
          </div>

          <div className="col-md-4 mb-4">
            <div className="feature-card">
              <h3> Quality Products</h3>
              <p>Shop from verified vendors with quality assurance.</p>
            </div>
          </div>

        </div>
      </div>

    </div>
  );
}

export default Home;