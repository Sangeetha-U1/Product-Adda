import React from "react";

function Home() {
  return (
    <div className="container mt-5">
      <div className="row mb-5">
        <div className="col-md-12 text-center py-4">
          <h1>Welcome to ProductAdda</h1>
          <p>Your One Stop Marketplace Platform</p>
        </div>
      </div>

      <div className="row mb-5">
        <div className="col-md-4">
          <div className="border p-4 text-center">
            <h3>Products</h3>
            <p>Browse thousands of products.</p>
          </div>
        </div>

        <div className="col-md-4">
          <div className="border p-4 text-center">
            <h3>Categories</h3>
            <p>Explore products by category.</p>
          </div>
        </div>

        <div className="col-md-4">
          <div className="border p-4 text-center">
            <h3>Offers</h3>
            <p>Get exciting discounts and deals.</p>
          </div>
        </div>
      </div>

      <div className="d-flex justify-content-center py-4">
        <button className="btn btn-primary">
          Explore Products
        </button>
      </div>
    </div>
  );
}

export default Home;