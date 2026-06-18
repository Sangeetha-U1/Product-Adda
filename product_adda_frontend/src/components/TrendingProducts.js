import React from "react";
import "../styles/TrendingProducts.css";

function TrendingProducts() {

  const products = [
    {
      name: "Samsung Galaxy S25",
      price: "₹79,999",
      image: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9"
    },

    {
      name: "Nike Air Max",
      price: "₹5,999",
      image: "https://images.unsplash.com/photo-1542291026-7eec264c27ff"
    },

    {
      name: "Gaming Headset",
      price: "₹2,999",
      image: "https://images.unsplash.com/photo-1599669454699-248893623440"
    },

    {
      name: "Smart Watch",
      price: "₹3,999",
      image: "https://images.unsplash.com/photo-1523275335684-37898b6baf30"
    }
  ];

  return (
    <section className="trending">

      <div className="container">

        <span className="trending-tag">
          🔥 Trending Now
        </span>

        <h2 className="trending-title">
          Most Popular Products
        </h2>

        <div className="trending-grid">

          {products.map((item, index) => (
            <div className="trending-card" key={index}>

              <div className="image-box">
                <img src={item.image} alt={item.name} />
              </div>

              <div className="card-content">
                <h4>{item.name}</h4>

                <p className="price">
                  {item.price}
                </p>

                <button>
                  Buy Now
                </button>
              </div>

            </div>
          ))}

        </div>

      </div>

    </section>
  );
}

export default TrendingProducts;