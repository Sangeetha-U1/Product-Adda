import React from "react";
import {
  FaMapMarkerAlt,
  FaEdit,
  FaTrash,
  FaPlus,
} from "react-icons/fa";

function AddressSection() {
  const addresses = [
    {
      id: 1,
      type: "Home",
      address:
        "Flat No 101, Kukatpally, Hyderabad, Telangana - 500072",
    },
    {
      id: 2,
      type: "Office",
      address:
        "Miweb Technologies, Hitech City, Hyderabad",
    },
  ];

  return (
    <div className="dashboard-section">

      <div className="address-header">

        <div>
          <h2>Saved Addresses</h2>
          <p>
            Manage your delivery addresses
          </p>
        </div>

        <button className="add-address-btn">
          <FaPlus />
          Add Address
        </button>

      </div>

      <div className="address-grid">

        {addresses.map((item) => (
          <div
            className="address-card"
            key={item.id}
          >

            <FaMapMarkerAlt className="address-icon" />

            <h4>{item.type}</h4>

            <p>{item.address}</p>

            <div className="address-actions">

              <button>
                <FaEdit />
                Edit
              </button>

              <button className="delete-btn">
                <FaTrash />
                Delete
              </button>

            </div>

          </div>
        ))}

      </div>

    </div>
  );
}

export default AddressSection;