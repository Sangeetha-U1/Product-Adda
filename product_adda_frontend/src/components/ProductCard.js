import React from "react";
import "../styles/ProductCard.css";

function ProductCard({ product }) {
  if (!product) return null;
// ⭐ function to render stars
  const renderStars = (rating) => {
    const stars = [];

    for (let i = 1; i <= 5; i++) {
      if (i <= Math.floor(rating)) {
        stars.push("⭐"); // full star
      } else {
        stars.push("☆"); // empty star
      }
    }

    return stars.join(" ");
  };
  return (
    <div className="product-card">
      
      <img
        src={product?.image}
        alt={product?.title}
        className="product-image"
      />

      <div className="product-info">
        <h3>{product?.title}</h3>

        <p className="price">₹{product?.price}</p>

        <p className="category">{product?.category}</p>
   {/* ⭐ STAR RATING */}
        <p className="rating">
          {renderStars(product?.rating)} ({product?.rating})
        </p>
      </div>

    </div>
  );
}

export default ProductCard;