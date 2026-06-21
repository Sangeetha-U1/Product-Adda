import React from "react";
import { FaPlus, FaEdit, FaTrash } from "react-icons/fa";

function VendorProducts() {
  const products = [
    {
      id: 1,
      name: "Smart Watch",
      category: "Electronics",
      price: 1999,
      stock: 20,
    },
    {
      id: 2,
      name: "Wireless Headphones",
      category: "Electronics",
      price: 1499,
      stock: 15,
    },
    {
      id: 3,
      name: "Sports Shoes",
      category: "Fashion",
      price: 999,
      stock: 30,
    },
  ];

  return (
    <div className="vendor-card">

      <div className="vendor-page-header">

        <div>
          <h2>My Products</h2>
          <p>Manage your products</p>
        </div>

        <button className="add-product-btn">
          <FaPlus />
          Add Product
        </button>

      </div>

      <div className="table-wrapper">

        <table className="vendor-table">

          <thead>
            <tr>
              <th>ID</th>
              <th>Product</th>
              <th>Category</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>

            {products.map((product) => (
              <tr key={product.id}>

                <td>{product.id}</td>

                <td>{product.name}</td>

                <td>{product.category}</td>

                <td>₹ {product.price}</td>

                <td>{product.stock}</td>

                <td>

                  <button className="edit-btn">
                    <FaEdit />
                  </button>

                  <button className="delete-btn">
                    <FaTrash />
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

export default VendorProducts;