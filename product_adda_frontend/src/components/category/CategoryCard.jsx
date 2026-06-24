import { useNavigate } from "react-router-dom";
import "./CategoryCard.css";

function CategoryCard({ category }) {
  const navigate = useNavigate();

  return (
    <div
      className="category-card"
      onClick={() =>
        navigate(`/categories/${category.name}`)
      }
    >
      <img
        src={category.image}
        alt={category.name}
        className="category-image"
      />

      <div className="category-content">
        <h3>{category.name}</h3>
        <p>{category.description}</p>
      </div>
    </div>
  );
}

export default CategoryCard;