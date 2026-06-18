import React from "react";
import "../styles/OfferBanner.css";
import { toast } from "react-toastify";
function OfferBanner() {
  return (
    <section className="offer-banner">

      <h2>
        Mega Sale Up To 70% OFF
      </h2>

      <p>
        Shop your favorite products at amazing prices.
      </p>

      <button>
        Shop Now
      </button>

    </section>
  );
}

export default OfferBanner;