import React from "react";

function ProductCard({ image, name, price }) {

return(

<div className="product-card">

<img src={image} alt={name}/>

<h4>{name}</h4>

<p className="price">{price}</p>

<div className="rating">
⭐⭐⭐⭐⭐
</div>

<button>
Add To Cart
</button>

</div>

);

}

export default ProductCard;