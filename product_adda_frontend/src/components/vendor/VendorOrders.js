import React from "react";

function VendorOrders() {
  const orders = [
    {
      id: "PA1001",
      customer: "Rahul",
      amount: 2500,
      status: "Delivered",
    },
    {
      id: "PA1002",
      customer: "Priya",
      amount: 1800,
      status: "Pending",
    },
    {
      id: "PA1003",
      customer: "Arjun",
      amount: 4500,
      status: "Shipped",
    },
  ];

  return (
    <div className="vendor-card">

      <div className="vendor-page-header">
        <div>
          <h2>Orders</h2>
          <p>Track customer orders</p>
        </div>
      </div>

      <div className="table-wrapper">

        <table className="vendor-table">

          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Amount</th>
              <th>Status</th>
            </tr>
          </thead>

          <tbody>

            {orders.map((order) => (
              <tr key={order.id}>

                <td>{order.id}</td>

                <td>{order.customer}</td>

                <td>₹ {order.amount}</td>

                <td>

                  <span
                    className={`status-badge ${order.status.toLowerCase()}`}
                  >
                    {order.status}
                  </span>

                </td>

              </tr>
            ))}

          </tbody>

        </table>

      </div>

    </div>
  );
}

export default VendorOrders;