import React from "react";

function OrdersSection() {
  const orders = [
    {
      id: "PA10234",
      date: "20 Jun 2026",
      amount: 2999,
      status: "Delivered",
    },
    {
      id: "PA10235",
      date: "18 Jun 2026",
      amount: 1499,
      status: "Processing",
    },
    {
      id: "PA10236",
      date: "15 Jun 2026",
      amount: 3999,
      status: "Shipped",
    },
    {
      id: "PA10237",
      date: "12 Jun 2026",
      amount: 999,
      status: "Cancelled",
    },
  ];

  return (
    <div className="dashboard-section">
      <div className="section-header">
        <h2>My Orders</h2>
        <p>Track and manage your orders</p>
      </div>

      <div className="orders-table-wrapper">
        <table className="orders-table">

          <thead>
            <tr>
              <th>Order ID</th>
              <th>Date</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>

            {orders.map((order) => (
              <tr key={order.id}>

                <td>{order.id}</td>

                <td>{order.date}</td>

                <td>₹ {order.amount}</td>

                <td>
                  <span
                    className={`status-badge ${order.status.toLowerCase()}`}
                  >
                    {order.status}
                  </span>
                </td>

                <td>
                  <button className="view-btn">
                    View Details
                  </button>
                </td>

              </tr>
            ))}

          </tbody>

        </table>
      </div>
    </div>
  );
}

export default OrdersSection;