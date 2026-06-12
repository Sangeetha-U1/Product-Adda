function Home() {
  return (
    <div>

      {/* Hero Section */}
      <div className="container-fluid bg-dark text-white text-center p-5">
        <h1 className="display-4">Welcome to ProductAdda</h1>
        <p className="lead">
          Your One Stop Destination for Shopping
        </p>
        <button className="btn btn-warning btn-lg mt-3">
          Shop Now
        </button>
      </div>


      {/* Features Section */}
      <div className="container mt-5">
        <div className="row text-center">

          <div className="col-md-4">
            <h3>🚚 Free Delivery</h3>
            <p>
              Get fast and free delivery on selected products.
            </p>
          </div>

          <div className="col-md-4">
            <h3>💳 Secure Payment</h3>
            <p>
              100% safe and secure payment options available.
            </p>
          </div>

          <div className="col-md-4">
            <h3>🎧 24/7 Support</h3>
            <p>
              Our team is always ready to help you.
            </p>
          </div>

        </div>
      </div>


      {/* Categories Section */}
      <div className="container mt-5">
        <h2 className="text-center text-primary">
          Shop By Categories
        </h2>

        <div className="row mt-4">

          <div className="col-md-3">
            <div className="card p-3 text-center">
              <h5>📱 Electronics</h5>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card p-3 text-center">
              <h5>👕 Fashion</h5>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card p-3 text-center">
              <h5>🏠 Home Appliances</h5>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card p-3 text-center">
              <h5>📚 Books</h5>
            </div>
          </div>

        </div>
      </div>


      {/* Offer Banner */}
      <div className="container mt-5">
        <div className="bg-warning text-center p-4 rounded">
          <h2>🔥 Mega Sale</h2>
          <p>
            Up to 50% OFF on selected products.
          </p>
          <button className="btn btn-dark">
            Grab Offer
          </button>
        </div>
      </div>


      {/* Footer */}
      <footer className="bg-dark text-white text-center p-3 mt-5">
        <p>
          © 2026 ProductAdda. All Rights Reserved.
        </p>
      </footer>

    </div>
  );
}

export default Home;