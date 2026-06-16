import React from "react";
import "../styles/ProductCard.css";

function ProductCard({ image, name, price }) {
  return (
    <div className="product-card">
      <img src={image} alt={name} />

      <h4>{name}</h4>
      <p className="price">{price}</p>

      <div className="rating">⭐⭐⭐⭐⭐</div>

      <button className="btn-cart">Add To Cart</button>
    </div>
  );
}

export default ProductCard;