import React, { useState } from "react";

function App() {
  const [ingredients, setIngredients] = useState("");
  const [styleOrDiet, setStyleOrDiet] = useState("");
  const [recipe, setRecipe] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleGenerate = async () => {
    setLoading(true);
    setRecipe(null);

    try {
      const response = await fetch("http://localhost:8080/api/recipe/generate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ ingredients, styleOrDiet }),
      });

      const data = await response.json();
      setRecipe(data);
    } catch (error) {
      console.error("Error generating recipe:", error);
      alert("無法生成食譜，請稍後再試。");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: "700px", margin: "0 auto", padding: "2rem" }}>
      <h1>AI 食譜推薦系統</h1>

      <div style={{ marginBottom: "1rem" }}>
        <label>食材：</label>
        <input
          type="text"
          value={ingredients}
          onChange={(e) => setIngredients(e.target.value)}
          style={{ width: "100%", padding: "0.5rem", marginTop: "0.5rem" }}
          placeholder="例如：雞肉, 洋蔥, 胡椒粉"
        />
      </div>

      <div style={{ marginBottom: "1rem" }}>
        <label>料理風格/飲食需求：</label>
        <input
          type="text"
          value={styleOrDiet}
          onChange={(e) => setStyleOrDiet(e.target.value)}
          style={{ width: "100%", padding: "0.5rem", marginTop: "0.5rem" }}
          placeholder="例如：低碳、泰式、素食"
        />
      </div>

      <button
        onClick={handleGenerate}
        disabled={loading}
        style={{
          padding: "0.75rem 1.5rem",
          backgroundColor: "#4CAF50",
          color: "white",
          border: "none",
          borderRadius: "5px",
          cursor: loading ? "not-allowed" : "pointer",
        }}
      >
        {loading ? "生成中..." : "生成食譜"}
      </button>

      {recipe && (
        <div style={{ marginTop: "2rem" }}>
          <h2>{recipe.title}</h2>

          <h3>食材清單</h3>
          <ul>
            {recipe.ingredients.map((item, index) => (
              <li key={index}>{item}</li>
            ))}
          </ul>

          <h3>料理步驟</h3>
          <ol>
            {recipe.steps.map((step, index) => (
              <li key={index}>{step}</li>
            ))}
          </ol>

          {recipe.imageUrl && (
            <div>
              <h3>成品圖片</h3>
              <img
                src={recipe.imageUrl}
                alt="食譜圖片"
                style={{ maxWidth: "100%", borderRadius: "8px" }}
              />
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default App;
