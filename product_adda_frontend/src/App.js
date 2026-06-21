import { Routes, Route } from "react-router-dom";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import ProductList from "./pages/ProductList";
import CartPage from "./pages/CartPage";
import Footer from "./components/Footer";

import Navbar from "./components/Navbar";

import { CartProvider } from "./context/CartContext";
import { SearchProvider } from "./context/SearchContext";

import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import ProductDetails from "./pages/ProductDetails";
import Checkout from "./pages/Checkout";
import OrderSuccess from "./pages/OrderSuccess";

import Dashboard from "./pages/Dashboard";

import VendorDashboard from "./pages/VendorDashboard";
import Error404 from "./pages/Error404";

function App() {
  return (
    <SearchProvider>
    <CartProvider>
      <Navbar />
      <Routes>
        <Route path="/product/:id" element={<ProductDetails />} />

        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        {/* PRODUCTS */}
        <Route path="/products" element={<ProductList />} />

        {/* CART */}
        <Route path="/cart" element={<CartPage />} />
        <Route path="/checkout" element={<Checkout />} />

<Route
  path="/order-success"
  element={<OrderSuccess />}
/>
<Route
  path="/dashboard"
  element={<Dashboard />}
/>
<Route
  path="/vendor-dashboard"
  element={<VendorDashboard />}
/>
<Route path="*" element={<Error404 />} />
      </Routes>

      {/* TOAST NOTIFICATION */}
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
      <Footer />
    </CartProvider>
    </SearchProvider>
  );
}

export default App;