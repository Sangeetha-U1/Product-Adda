function Footer() {
  return (
    <footer className="bg-dark text-white text-center p-4 mt-5">
      <div className="container">
        <h5>ProductAdda Marketplace</h5>
        <p>Your One Stop Shopping Destination</p>

        <div>
          <a href="/" className="text-white me-3">
            Home
          </a>

          <a href="/products" className="text-white me-3">
            Products
          </a>

          <a href="/contact" className="text-white">
            Contact
          </a>
        </div>

        <hr />

        <p> 2026 ProductAdda. All Rights Reserved.</p>
      </div>
    </footer>
  );
}

export default Footer;