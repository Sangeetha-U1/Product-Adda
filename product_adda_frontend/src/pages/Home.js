import React from "react";

import HeroSection from "../components/HeroSection";
import Categories from "../components/Categories";
import FeaturedProducts from "../components/FeaturedProducts";
import OfferBanner from "../components/OfferBanner";


function Home(){

return(
<>
<HeroSection/>
<Categories/>
<FeaturedProducts/>
 <OfferBanner />
</>
);
}

export default Home;