import React, { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import "../styles/CartPage.css";

function CartPage() {
  const navigate = useNavigate();

  const {
    cart,
    removeFromCart,
    addToCart,
    decreaseQty,
  } = useContext(CartContext);

  const total = cart.reduce(
    (sum, item) => sum + item.price * item.qty,
    0
  );

  return (
    <div className="cart-container">

      <h2 className="cart-title">🛒 My Cart</h2>

      {cart.length === 0 ? (
        <div className="empty-cart">
          <h3>Your cart is empty 😔</h3>

          <button
            className="shop-btn"
            onClick={() => navigate("/products")}
          >
            Continue Shopping
          </button>
        </div>
      ) : (
        <div className="cart-layout">

          {/* LEFT SIDE */}
          <div className="cart-items">

            {cart.map((item) => (
              <div className="cart-card" key={item.id}>

                <img
                  src={item.image}
                  alt={item.title}
                />

                <div className="cart-info">

                  <h4>{item.title}</h4>

                  <p className="price">
                    ₹ {item.price}
                  </p>

                  {/* QUANTITY */}
                  <div className="qty-box">

                    <button
                      onClick={() =>
                        decreaseQty(item.id)
                      }
                    >
                      -
                    </button>

                    <span>{item.qty}</span>

                    <button
                      onClick={() =>
                        addToCart(item)
                      }
                    >
                      +
                    </button>

                  </div>

                </div>

                <button
                  className="remove-btn"
                  onClick={() =>
                    removeFromCart(item.id)
                  }
                >
                  Remove
                </button>

              </div>
            ))}

          </div>

          {/* RIGHT SIDE */}
          <div className="cart-summary">

            <h3>Price Details</h3>

            <hr />

            <p>
              Total Items :
              <strong> {cart.length}</strong>
            </p>

            <p>
              Delivery :
              <strong> Free</strong>
            </p>

            <hr />

            <h2>Total: ₹ {total}</h2>

            <button
  className="checkout-btn"
  onClick={() => navigate("/checkout")}
>
  Proceed to Checkout
</button>

          </div>

        </div>
      )}

    </div>
  );
}

export default CartPage;