import React from "react";
import ProductCard from "./ProductCard";
import "../styles/ProductCard.css";
import "../styles/FeaturedProducts.css";

import { toast } from "react-toastify";
function FeaturedProducts(){

const products=[
{
title:"iPhone 16",
price:"₹89,999",
image:"https://images.unsplash.com/photo-1592899677977-9c10ca588bbd"
},

{
title:"Laptop",
price:"₹59,999",
image:"https://images.unsplash.com/photo-1496181133206-80ce9b88a853"
},

{
title:"Smart Watch",
price:"₹5,999",
image:"https://images.unsplash.com/photo-1523275335684-37898b6baf30"
},

{
title:"Shoes",
price:"₹2,499",
image:"https://images.unsplash.com/photo-1542291026-7eec264c27ff"
}

];


return(

<section className="products">

<div className="container">

<h2 className="section-title">
Featured Products
</h2>


<div className="product-grid">

{
products.map((item, index) => (
  <ProductCard
    key={index}
    product={{
      id: index,
      title: item.title,   // ✅ FIXED
      price: item.price,
      image: item.image,
      rating: 4.5,
    }}
  />

))
}

</div>

</div>

</section>

);

}

export default FeaturedProducts;