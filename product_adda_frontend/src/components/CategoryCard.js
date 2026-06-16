import React from "react";

function CategoryCard({ image, name }) {
  return (
    <div className="category-card">
      <img src={image} alt={name} className="category-image" />
      <h5>{name}</h5>
    </div>
  );
}

export default CategoryCard;