import React, { useContext, useState } from "react";
import { useParams } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import products from "../data/Products";
import ProductCard from "../components/ProductCard";
import "../styles/ProductDetails.css";

function ProductDetails() {
  const { id } = useParams();
  const { addToCart } = useContext(CartContext);

  const product = products.find(
    (item) => item.id === Number(id)
  );

  const [qty, setQty] = useState(1);

  if (!product) {
    return <h2>Product Not Found</h2>;
  }

  const relatedProducts = products.filter(
    (item) =>
      item.category === product.category &&
      item.id !== product.id
  );

  return (
    <div className="product-details">

      <div className="breadcrumb">
        Home &gt; Products &gt; {product.title}
      </div>

      <div className="details-container">

        <div className="image-section">
          <img src={product.image} alt={product.title} />
        </div>

        <div className="info-section">

          <h1>{product.title}</h1>

          <div className="rating">
            ⭐ {product.rating}
          </div>

          <h2>₹ {product.price}</h2>

          <p>{product.description}</p>

          <div className="qty-box">
            <button
              onClick={() =>
                setQty(qty > 1 ? qty - 1 : 1)
              }
            >
              -
            </button>

            <span>{qty}</span>

            <button
              onClick={() => setQty(qty + 1)}
            >
              +
            </button>
          </div>

          <div className="action-buttons">
            <button
              className="cart-btn"
              onClick={() => addToCart(product)}
            >
              Add To Cart
            </button>

            <button className="buy-btn">
              Buy Now
            </button>
          </div>

        </div>

      </div>

      <div className="description-section">
        <h3>Description</h3>
        <p>{product.description}</p>
      </div>

      <div className="review-section">
        <h3>Customer Reviews</h3>

        <div className="review">
          ⭐⭐⭐⭐⭐ Excellent Product
        </div>

        <div className="review">
          ⭐⭐⭐⭐ Worth the Price
        </div>
      </div>

      <div className="related-section">
        <h3>Related Products</h3>

        <div className="related-grid">
          {relatedProducts.map((item) => (
            <ProductCard
              key={item.id}
              product={item}
              addToCart={addToCart}
            />
          ))}
        </div>
      </div>

    </div>
  );
}

export default ProductDetails;