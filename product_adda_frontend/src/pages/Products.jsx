import React, { useState } from "react";
import { useLocation } from "react-router-dom";
import products from "../data/products";
import "./Products.css";

function Products() {

  const location = useLocation();

  const params = new URLSearchParams(location.search);

  const searchTerm = params.get("search") || "";

  const filteredProducts = products.filter((product) =>
    product.name.toLowerCase().includes(searchTerm.toLowerCase())
  );
const [loading] = useState(false);
if (loading) {
  return <h3>Loading Products...</h3>;
}
  return (
    <div className="container products-page">

      <h2 className="products-title">
        Products
      </h2>

      {searchTerm && (
        <>
          <h4>
            Search Result For: {searchTerm}
          </h4>

          <p className="result-count">
            {filteredProducts.length} Product(s) Found
          </p>
        </>
      )}

      <div className="row">

        {filteredProducts.length > 0 ? (

          filteredProducts.map((product) => (

            <div
              className="col-lg-4 col-md-6 mb-4"
              key={product.id}
            >

              <div className="product-card">

                <img
                  src={product.image}
                  alt={product.name}
                  className="product-image"
                />

                <div className="product-content">

                  <h4>{product.name}</h4>

                  <p className="category">
                    {product.category}
                  </p>

                  <p className="price">
                    ₹{product.price}
                  </p>

                </div>

              </div>

            </div>

          ))

        ) : (

<div className="text-center mt-5">
  <p className="result-count">
  Total Products Found: {filteredProducts.length}
</p>
  <h3>No Products Found</h3>
  <p>Try searching with another keyword.</p>
</div>
        )}

      </div>

    </div>
  );
}

export default Products;