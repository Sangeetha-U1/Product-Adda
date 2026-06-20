import React from "react";
import { useNavigate } from "react-router-dom";
import {
  FaCheckCircle,
  FaHome,
  FaShoppingBag,
} from "react-icons/fa";

import "../styles/OrderSuccess.css";

function OrderSuccess() {
  const navigate = useNavigate();

  const orderId =
    "PA" + Math.floor(Math.random() * 100000);

  return (
    <div className="success-page">

      <div className="success-card">

        <FaCheckCircle className="success-icon" />

        <h1>Order Placed Successfully!</h1>

        <p>
          Thank you for shopping with ProductAdda.
        </p>

        <div className="order-info">
          <h3>Order ID: {orderId}</h3>

          <p>
            Estimated Delivery:
            <strong> 3 - 5 Days</strong>
          </p>

          <p>
            Payment Status:
            <strong> Confirmed</strong>
          </p>
        </div>

        <div className="success-actions">

          <button
            onClick={() => navigate("/")}
          >
            <FaHome />
            Home
          </button>

          <button
            onClick={() => navigate("/products")}
          >
            <FaShoppingBag />
            Shop More
          </button>

        </div>

      </div>

    </div>
  );
}

export default OrderSuccess;