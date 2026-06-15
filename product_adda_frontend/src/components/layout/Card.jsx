function Card({ title, description }) {
  return (
    <div className="card shadow p-3">
      <h4>{title}</h4>

      <p>{description}</p>
    </div>
  );
}

export default Card;