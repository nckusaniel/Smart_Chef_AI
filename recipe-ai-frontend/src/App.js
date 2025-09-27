import { useState } from "react";
import { generateRecipe } from "./services/api";

function App() {
  const [ingredients, setIngredients] = useState("");
  const [style, setStyle] = useState("");
  const [recipe, setRecipe] = useState(null);

  const handleGenerate = async () => {
    const data = await generateRecipe(ingredients, style);
    setRecipe(data);
  };

  return (
    <div>
      <h1>AI 食譜推薦系統</h1>
      <input
        type="text"
        placeholder="輸入食材"
        value={ingredients}
        onChange={(e) => setIngredients(e.target.value)}
      />
      <input
        type="text"
        placeholder="料理風格/飲食需求"
        value={style}
        onChange={(e) => setStyle(e.target.value)}
      />
      <button onClick={handleGenerate}>生成食譜</button>

      {recipe && (
        <div>
          <h2>{recipe.name}</h2>
          <ul>
            {recipe.ingredients.map((item, i) => <li key={i}>{item}</li>)}
          </ul>
          <ol>
            {recipe.steps.map((step, i) => <li key={i}>{step}</li>)}
          </ol>
          <img src={recipe.imageUrl} alt="食譜圖片" />
        </div>
      )}
    </div>
  );
}

export default App;
