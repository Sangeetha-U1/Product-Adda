import { Routes, Route } from "react-router-dom";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import ProductList from "./pages/ProductList";

import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
                <Route path="/products" element={<ProductList />} />

        <Route path="/forgot-password" element={<ForgotPassword />} />
      </Routes>

      {/* ✅ ADD THIS */}
      <ToastContainer position="top-right" autoClose={2000} />
      <ToastContainer
  position="top-right"
  autoClose={2000}
  hideProgressBar={false}
  newestOnTop
  closeOnClick
  pauseOnHover
  draggable
  theme="colored"
/>
    </>
  );
}

export default App;