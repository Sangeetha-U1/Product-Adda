import React from "react";
import CategoryCard from "./CategoryCard";
import "../styles/Categories.css";

function Categories() {

 const categories = [
  {
    name: "Electronics",
    image:
    "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9"
  },

  {
    name: "Fashion",
    image:
    "https://images.unsplash.com/photo-1441986300917-64674bd600d8"
  },

  {
    name: "Home & Kitchen",
    image:
    "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85"
  },

  {
    name: "Beauty",
    image:
    "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9"
  },

  {
    name: "Grocery",
    image:
    "https://images.unsplash.com/photo-1542838132-92c53300491e"
  },

  {
    name: "Sports",
    image:
    "https://images.unsplash.com/photo-1517836357463-d25dfeac3438"
  }
];


return (
<section className="categories">

<div className="container">

<h2 className="section-title">
Shop By Categories
</h2>


<div className="category-grid">

{
categories.map((item,index)=>(
<CategoryCard
key={index}
image={item.image}
name={item.name}
/>
))
}

</div>
</div>
</section>
);

}

export default Categories;