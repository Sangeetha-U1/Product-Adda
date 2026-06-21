import React from "react";

import {
  FaBoxOpen,
  FaShoppingBag,
  FaRupeeSign,
  FaClock,
} from "react-icons/fa";

function VendorHome() {
  return (
    <div>

      <div className="vendor-header">
        <h2>Vendor Dashboard</h2>
        <p>
          Welcome back! Here's your
          business overview.
        </p>
      </div>

      <div className="stats-grid">

        <div className="stat-card">
          <FaBoxOpen className="stat-icon" />

          <div>
            <h3>125</h3>
            <p>Total Products</p>
          </div>
        </div>

        <div className="stat-card">
          <FaShoppingBag className="stat-icon" />

          <div>
            <h3>567</h3>
            <p>Total Orders</p>
          </div>
        </div>

        <div className="stat-card">
          <FaRupeeSign className="stat-icon" />

          <div>
            <h3>₹1,25,000</h3>
            <p>Total Revenue</p>
          </div>
        </div>

        <div className="stat-card">
          <FaClock className="stat-icon" />

          <div>
            <h3>18</h3>
            <p>Pending Orders</p>
          </div>
        </div>

      </div>

      <div className="recent-orders">

        <div className="section-title">
          <h3>Recent Orders</h3>
        </div>

        <table>

          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Amount</th>
              <th>Status</th>
            </tr>
          </thead>

          <tbody>

            <tr>
              <td>#PA1001</td>
              <td>Rahul</td>
              <td>₹2500</td>
              <td>
                <span className="completed">
                  Delivered
                </span>
              </td>
            </tr>

            <tr>
              <td>#PA1002</td>
              <td>Priya</td>
              <td>₹1800</td>
              <td>
                <span className="pending">
                  Pending
                </span>
              </td>
            </tr>

            <tr>
              <td>#PA1003</td>
              <td>Arjun</td>
              <td>₹4500</td>
              <td>
                <span className="shipping">
                  Shipped
                </span>
              </td>
            </tr>

          </tbody>

        </table>

      </div>

    </div>
  );
}

export default VendorHome;