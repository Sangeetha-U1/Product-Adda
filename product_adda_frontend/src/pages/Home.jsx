import Card from "../components/layout/Card";
import Button from "../components/layout/Button";

function Home() {
  return (
    <div className="container mt-5">

      <h1>Welcome to ProductAdda</h1>

      <Card
        title="Electronics"
        description="Explore latest gadgets and devices."
      />

      <br />

      <Button text="Shop Now" />

    </div>
  );
}

export default Home;