import React from "react";
import Header from "./Header";
import Navbar from "./Navbar";

function MainLayout({ children }) {
  return (
    <>
      <Header />
      <Navbar />

      <main className="container my-5">
        {children}
      </main>

    </>
  );
}

export default MainLayout;