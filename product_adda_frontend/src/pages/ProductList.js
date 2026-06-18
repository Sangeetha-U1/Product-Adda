import React, { useState, useEffect } from "react";
import "../styles/ProductList.css";
import ProductCard from "../components/ProductCard";
import { useSearchParams } from "react-router-dom";

function ProductList() {
  const [products, setProducts] = useState([]);
  const [filteredProducts, setFilteredProducts] = useState([]);

  const [searchParams] = useSearchParams();
  const searchQuery = searchParams.get("search") || "";

  const [category, setCategory] = useState("All");
  const [maxPrice, setMaxPrice] = useState(100000);

  useEffect(() => {
    const dummyProducts = [
      {
        id: 1,
        title: "iPhone 15 Pro",
        price: 134999,
        category: "Electronics",
        image:
          "https://images.unsplash.com/photo-1592750475338-74b7b21085ab",
        rating: 4.5,
      },
      {
        id: 2,
        title: "Nike Running Shoes",
        price: 4999,
        category: "Fashion",
        image:
          "https://images.unsplash.com/photo-1606813907291-d86efa9b94db",
        rating: 4.2,
      },
      {
        id: 3,
        title: "Smart Watch",
        price: 2999,
        category: "Electronics",
        image:
          "https://images.unsplash.com/photo-1523275335684-37898b6baf30",
        rating: 4.0,
      },
      {
        id: 4,
        title: "Leather Jacket",
        price: 7999,
        category: "Fashion",
        image:
          "https://images.unsplash.com/photo-1520975954732-35dd22299614",
        rating: 4.6,
      },
    ];

    setProducts(dummyProducts);
    setFilteredProducts(dummyProducts);
  }, []);

  useEffect(() => {
    let result = [...products];

    if (searchQuery) {
      result = result.filter((p) =>
        p.title.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    if (category !== "All") {
      result = result.filter((p) => p.category === category);
    }

    result = result.filter((p) => p.price <= maxPrice);

    setFilteredProducts(result);
  }, [searchQuery, category, maxPrice, products]);

  return (
    <div className="product-page">

      {/* FILTER BAR */}
      <div className="filter-bar">
        <select onChange={(e) => setCategory(e.target.value)}>
          <option value="All">All Categories</option>
          <option value="Electronics">Electronics</option>
          <option value="Fashion">Fashion</option>
        </select>

        <input
          type="range"
          min="1000"
          max="150000"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
        />

        <span>Max Price: ₹{maxPrice}</span>
      </div>

      {/* PRODUCT GRID */}
      <div className="product-grid">
        {filteredProducts.length > 0 ? (
          filteredProducts.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))
        ) : (
          <div className="no-products">No products found 😔</div>
        )}
      </div>

    </div>
  );
}

export default ProductList;