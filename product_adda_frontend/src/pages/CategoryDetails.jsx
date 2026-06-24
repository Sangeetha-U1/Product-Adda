import React from "react";
import { useParams, useNavigate } from "react-router-dom";
import categories from "../data/categories";
import products from "../data/products";
import "./CategoryDetails.css";

function CategoryDetails() {
  const { name } = useParams();
  const navigate = useNavigate();

  const category = categories.find(
    (item) =>
      item.name.toLowerCase() ===
      name.toLowerCase()
  );

  if (!category) {
    return <h2>Category Not Found</h2>;
  }

  const categoryProducts = products.filter(
    (item) =>
      item.category.toLowerCase() ===
      category.name.toLowerCase()
  );

  return (
    <div className="category-details">

      <div className="breadcrumb">
        Home &gt; Categories &gt; {category.name}
      </div>

      <div className="details-container">

        <div className="image-section">
          <img
            src={category.image}
            alt={category.name}
          />
        </div>

        <div className="info-section">

          <h1>{category.name}</h1>

          <div className="category-badge">
            {categoryProducts.length} Products
          </div>

          <p>{category.description}</p>

          <div className="category-features">
            <span>✓ Best Quality</span>
            <span>✓ Fast Delivery</span>
            <span>✓ Latest Collection</span>
          </div>

          <button
            className="shop-btn"
            onClick={() =>
              navigate("/products")
            }
          >
            Shop Now
          </button>

        </div>

      </div>

      <div className="description-section">
        <h3>About Category</h3>

        <p>
          Explore the latest {category.name} products
          with the best prices and premium quality.
          Find your favorite items and enjoy shopping.
        </p>
      </div>

      <div className="related-section">

        <h3>Products in {category.name}</h3>

<div className="related-grid">
  {categoryProducts.map((item) => (
    <div className="product-card" key={item.id}>
      <img
        src={item.image}
        alt={item.name}
      />

      <h4>{item.name}</h4>

      <p>₹{item.price}</p>
    </div>
  ))}
</div>
      </div>

    </div>
  );
}

export default CategoryDetails;