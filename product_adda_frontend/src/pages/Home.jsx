import Card from "../components/layout/Card";
import Button from "../components/layout/Button";

function Home() {
  return (
    <div className="container section">

      <h1>Welcome to ProductAdda</h1>

      <Card
        title="Electronics"
        description="Explore latest gadgets and devices."
      />

      <div className="mt-4">
        <Button text="Shop Now" />
      </div>

    </div>
  );
}

export default Home;