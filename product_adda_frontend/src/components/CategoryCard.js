import React from "react";

function CategoryCard({ image, name }) {
  return (
    <div className="category-card">
      <img src={image} alt={name} />
      <h5>{name}</h5>
    </div>
  );
}

export default CategoryCard;