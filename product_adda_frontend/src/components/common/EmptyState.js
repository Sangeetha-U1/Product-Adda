function EmptyState({
  title,
  description
}) {
  return (
    <div className="text-center p-5">

      <h3>{title}</h3>

      <p>{description}</p>

    </div>
  );
}

export default EmptyState;