import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/ProductCard.css";

function ProductCard({ product, addToCart }) {
  const navigate = useNavigate();

  if (!product) {
    console.log("ProductCard received:", product);
    return null;
  }

  const renderStars = (rating = 0) => {
    const stars = [];

    for (let i = 1; i <= 5; i++) {
      stars.push(
        <span
          key={i}
          style={{
            color: i <= Math.floor(rating)
              ? "#ffb400"
              : "#ddd",
            fontSize: "16px",
          }}
        >
          ★
        </span>
      );
    }

    return stars;
  };

  return (
    <div className="product-card">
      <img
        src={product.image}
        alt={product.title}
        onClick={() =>
          navigate(`/product/${product.id}`)
        }
      />

      <h3
        onClick={() =>
          navigate(`/product/${product.id}`)
        }
      >
        {product.title}
      </h3>

      <p className="price">₹ {product.price}</p>

      <div className="rating">
        {renderStars(product.rating)}
        <span> ({product.rating})</span>
      </div>

      <button onClick={() => addToCart(product)}>
        Add To Cart
      </button>
    </div>
  );
}

export default ProductCard;