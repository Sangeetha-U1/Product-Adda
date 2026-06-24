import { useState } from "react";
import categories from "../data/categories";
import CategoryCard from "../components/category/CategoryCard";
import "./Categories.css";

function Categories() {
  const [search, setSearch] = useState("");

  const filteredCategories = categories.filter((category) =>
    category.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="container py-5">
      <h1 className="section-title text-center mb-4">
        Categories
      </h1>

      <div className="search-box mb-4">
        <input
          type="text"
          className="form-control"
          placeholder="Search Category..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className="row">
        {filteredCategories.length > 0 ? (
          filteredCategories.map((category) => (
            <div
              className="col-lg-3 col-md-4 col-sm-6 mb-4"
              key={category.id}
            >
              <CategoryCard category={category} />
            </div>
          ))
        ) : (
          <h4 className="text-center">
            No Categories Found
          </h4>
        )}
      </div>
    </div>
  );
}

export default Categories;