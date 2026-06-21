import { Link } from "react-router-dom";
import "../styles/Error404.css";

function Error404() {
  return (
    <div className="error-page">

      <h1>404</h1>

      <h2>Page Not Found</h2>

      <p>
        The page you are looking for does not exist.
      </p>

      <Link to="/" className="home-btn">
        Back To Home
      </Link>

    </div>
  );
}

export default Error404;