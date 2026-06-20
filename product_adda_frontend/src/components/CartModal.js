import React, { useContext } from "react";
import { CartContext } from "../context/CartContext";
import "../styles/CartModal.css";
import { useNavigate } from "react-router-dom";

function CartModal({ isOpen, setIsOpen }) {
  const navigate = useNavigate();
  const { cart, removeFromCart, addToCart, decreaseQty } =
    useContext(CartContext);

  const total = cart.reduce(
    (sum, item) => sum + item.price * item.qty,
    0
  );

  return (
    <>
      <div
        className={`cart-backdrop ${isOpen ? "show" : ""}`}
        onClick={() => setIsOpen(false)}
      ></div>

      <div className={`cart-drawer ${isOpen ? "open" : ""}`}>

        <div className="cart-header">
          <h3>My Cart</h3>
          <button onClick={() => setIsOpen(false)}>✖</button>
        </div>

        <div className="cart-body">

          {cart.length === 0 ? (
            <p className="empty">Cart is empty</p>
          ) : (
            cart.map((item) => (
              <div className="cart-item" key={item.id}>

                <img src={item.image} alt={item.title} />

                <div>
                  <h4>{item.title}</h4>
                  <p>₹ {item.price}</p>

                  {/* QUANTITY CONTROLS */}
                  <div className="qty-box">

                    <button onClick={() => decreaseQty(item.id)}>
                      -
                    </button>

                    <span>{item.qty}</span>

                    <button onClick={() => addToCart(item)}>
                      +
                    </button>

                  </div>
                </div>

                <button
                  className="remove"
                  onClick={() => removeFromCart(item.id)}
                >
                  ✖
                </button>

              </div>
            ))
          )}

        </div>

        <div className="cart-footer">
          <h3>Total: ₹ {total}</h3>
<button
  className="checkout"
  onClick={() => {
    setIsOpen(false);
    navigate("/checkout");
  }}
>
  Checkout
</button>        </div>

      </div>
    </>
  );
}

export default CartModal;