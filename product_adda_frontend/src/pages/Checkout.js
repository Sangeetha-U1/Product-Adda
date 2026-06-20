import React, { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import "../styles/Checkout.css";

function Checkout() {
  const navigate = useNavigate();
  const { cart } = useContext(CartContext);

  const [loading, setLoading] = useState(false);

  const [payment, setPayment] = useState("COD");

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    state: "",
    pincode: "",
  });

  const [errors, setErrors] = useState({});

  const total = cart.reduce(
    (sum, item) => sum + item.price * item.qty,
    0
  );

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });

    setErrors({
      ...errors,
      [e.target.name]: "",
    });
  };

  const validateForm = () => {
    let newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = "Full Name is required";
    }

    if (!formData.email.trim()) {
      newErrors.email = "Email is required";
    } else if (
      !/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i.test(
        formData.email
      )
    ) {
      newErrors.email = "Enter a valid email";
    }

    if (!formData.phone.trim()) {
      newErrors.phone = "Phone Number is required";
    } else if (!/^[0-9]{10}$/.test(formData.phone)) {
      newErrors.phone =
        "Enter a valid 10 digit phone number";
    }

    if (!formData.address.trim()) {
      newErrors.address = "Address is required";
    }

    if (!formData.city.trim()) {
      newErrors.city = "City is required";
    }

    if (!formData.state.trim()) {
      newErrors.state = "State is required";
    }

    if (!formData.pincode.trim()) {
      newErrors.pincode = "Pincode is required";
    } else if (!/^[0-9]{6}$/.test(formData.pincode)) {
      newErrors.pincode =
        "Enter a valid 6 digit pincode";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const handleOrder = () => {
    if (!validateForm()) return;

    setLoading(true);

    setTimeout(() => {
      navigate("/order-success");
    }, 1500);
  };

  return (
    <div className="checkout-container">

      {/* SHIPPING FORM */}
      <div className="checkout-left">

        <h2>Shipping Address</h2>

        <input
          type="text"
          name="name"
          placeholder="Full Name"
          value={formData.name}
          onChange={handleChange}
          className={errors.name ? "error" : ""}
        />
        {errors.name && (
          <small className="error-text">
            {errors.name}
          </small>
        )}

        <input
          type="email"
          name="email"
          placeholder="Email Address"
          value={formData.email}
          onChange={handleChange}
          className={errors.email ? "error" : ""}
        />
        {errors.email && (
          <small className="error-text">
            {errors.email}
          </small>
        )}

        <input
          type="text"
          name="phone"
          placeholder="Phone Number"
          value={formData.phone}
          onChange={handleChange}
          className={errors.phone ? "error" : ""}
        />
        {errors.phone && (
          <small className="error-text">
            {errors.phone}
          </small>
        )}

        <textarea
          name="address"
          placeholder="Full Address"
          value={formData.address}
          onChange={handleChange}
          className={errors.address ? "error" : ""}
        />
        {errors.address && (
          <small className="error-text">
            {errors.address}
          </small>
        )}

        <input
          type="text"
          name="city"
          placeholder="City"
          value={formData.city}
          onChange={handleChange}
          className={errors.city ? "error" : ""}
        />
        {errors.city && (
          <small className="error-text">
            {errors.city}
          </small>
        )}

        <input
          type="text"
          name="state"
          placeholder="State"
          value={formData.state}
          onChange={handleChange}
          className={errors.state ? "error" : ""}
        />
        {errors.state && (
          <small className="error-text">
            {errors.state}
          </small>
        )}

        <input
          type="text"
          name="pincode"
          placeholder="Pincode"
          value={formData.pincode}
          onChange={handleChange}
          className={errors.pincode ? "error" : ""}
        />
        {errors.pincode && (
          <small className="error-text">
            {errors.pincode}
          </small>
        )}

      </div>

      {/* ORDER SUMMARY */}
      <div className="checkout-right">

        <h2>Order Summary</h2>

        {cart.length === 0 ? (
          <p>No items in cart</p>
        ) : (
          cart.map((item) => (
            <div
              className="summary-item"
              key={item.id}
            >
              <span>
                {item.title} × {item.qty}
              </span>

              <span>
                ₹ {item.price * item.qty}
              </span>
            </div>
          ))
        )}

        <div className="order-total">
          <h3>Total: ₹ {total}</h3>
        </div>

        {/* PAYMENT */}
        <div className="payment-section">

          <h2>Payment Method</h2>

          <label className="payment-option">
            <input
              type="radio"
              checked={payment === "COD"}
              onChange={() =>
                setPayment("COD")
              }
            />
            Cash On Delivery
          </label>

          <label className="payment-option">
            <input
              type="radio"
              checked={payment === "UPI"}
              onChange={() =>
                setPayment("UPI")
              }
            />
            UPI Payment
          </label>

          <label className="payment-option">
            <input
              type="radio"
              checked={payment === "CARD"}
              onChange={() =>
                setPayment("CARD")
              }
            />
            Credit / Debit Card
          </label>

        </div>

        <button
          className="place-order-btn"
          onClick={handleOrder}
          disabled={loading}
        >
          {loading
            ? "Processing Order..."
            : "Place Order"}
        </button>

      </div>

    </div>
  );
}

export default Checkout;