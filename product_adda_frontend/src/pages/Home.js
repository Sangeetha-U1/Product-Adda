import React from "react";

import HeroSection from "../components/HeroSection";
import Categories from "../components/Categories";
import FeaturedProducts from "../components/FeaturedProducts";
import OfferBanner from "../components/OfferBanner";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";



function Home(){

return(
<>
<Navbar/>
<HeroSection/>
<Categories/>
<FeaturedProducts/>
 <OfferBanner />
 <Footer/>
</>
);
}

export default Home;