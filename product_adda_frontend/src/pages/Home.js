import React from "react";

import Navbar from "../components/Navbar";
import HeroSection from "../components/HeroSection";
import Categories from "../components/Categories";
import FeaturedProducts from "../components/FeaturedProducts";
import OfferBanner from "../components/OfferBanner";
import TrendingProducts from "../components/TrendingProducts";
import WhyChooseUs from "../components/WhyChooseUs";
import Footer from "../components/Footer";

function Home() {
  return (
    <>
      <Navbar />
      <HeroSection />
      <Categories />
      <FeaturedProducts />
      <OfferBanner />
      <TrendingProducts />
      <WhyChooseUs />
      <Footer />
    </>
  );
}

export default Home;