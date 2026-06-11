import React from "react";
import Header from "./Header";
import Navbar from "./Navbar";
import Footer from "./Footer";

function MainLayout({ children }) {
  return (
    <>
      <Header />
      <Navbar />

      <main className="container my-5">
        {children}
      </main>

      <Footer />
    </>
  );
}

export default MainLayout;