import React, { useState, useContext } from "react";
import { CartContext } from "../context/CartContext";
import ProductCard from "../components/ProductCard";
import "../styles/ProductList.css";
import { SearchContext } from "../context/SearchContext";

function ProductList() {
  const { addToCart } = useContext(CartContext);
  const { searchQuery } = useContext(SearchContext);

  const products = [
    {
      id: 1,
      title: "Smart Watch",
      price: 1999,
      category: "Electronics",
      image: "https://images.unsplash.com/photo-1523275335684-37898b6baf30",
      rating: 4.2,
    },
    {
      id: 2,
      title: "Shoes",
      price: 999,
      category: "Fashion",
      image: "https://images.unsplash.com/photo-1542291026-7eec264c27ff",
      rating: 3.8,
    },
    {
      id: 3,
      title: "Headphones",
      price: 1499,
      category: "Electronics",
      image: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
      rating: 4.5,
    },
    {
      id: 4,
      title: "Mobile",
      price: 9999,
      category: "Electronics",
      image: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9",
      rating: 4.7,
    },
  ];

  const [category, setCategory] = useState("All");
  const [maxPrice, setMaxPrice] = useState(20000);

  // ✅ NO STATE FILTERING (DIRECT COMPUTATION)
  const filtered = products
    .filter((item) =>
      searchQuery
        ? item.title.toLowerCase().includes(searchQuery.toLowerCase())
        : true
    )
    .filter((item) =>
      category !== "All" ? item.category === category : true
    )
    .filter((item) => item.price <= maxPrice);

  return (
    <div className="product-page">

      {/* FILTER BAR */}
      <div className="filter-bar">

        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
        >
          <option value="All">All Categories</option>
          <option value="Electronics">Electronics</option>
          <option value="Fashion">Fashion</option>
        </select>

        <div className="price-filter">
          <h4>Max Price: ₹{maxPrice}</h4>

          <input
            type="range"
            min="500"
            max="20000"
            value={maxPrice}
            onChange={(e) => setMaxPrice(Number(e.target.value))}
          />
        </div>

      </div>

      {/* PRODUCTS */}
      <div className="product-grid">

        {filtered.length > 0 ? (
          filtered.map((item) => (
            <ProductCard
              key={item.id}
              product={item}
              addToCart={addToCart}
            />
          ))
        ) : (
          <p>No products found 😔</p>
        )}

      </div>

    </div>
  );
}

export default ProductList;