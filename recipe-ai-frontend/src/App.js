import React, { useState } from "react";

function App() {
  const [ingredients, setIngredients] = useState("");
  const [styleOrDiet, setStyleOrDiet] = useState("");
  const [recipes, setRecipes] = useState([]); // 多筆食譜
  const [loading, setLoading] = useState(false);
  const [darkMode, setDarkMode] = useState(false);

  const handleGenerate = async () => {
    setLoading(true);
    setRecipes([]);

    try {
      const response = await fetch("http://localhost:8080/api/recipe/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ingredients, styleOrDiet }),
      });

      const data = await response.json();
      // 假設後端回傳的是陣列
      setRecipes(Array.isArray(data) ? data : [data]);
    } catch (error) {
      console.error("Error generating recipe:", error);
      alert("無法生成食譜，請稍後再試。");
    } finally {
      setLoading(false);
    }
  };

  const themeStyles = {
    background: darkMode ? "#1e1e1e" : "linear-gradient(to right, #f8f4f0, #fff1e6)",
    color: darkMode ? "#f5f5f5" : "#333",
    cardBg: darkMode ? "#2a2a2a" : "#fff",
    inputBg: darkMode ? "#3a3a3a" : "#fff",
    inputColor: darkMode ? "#f5f5f5" : "#333",
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        padding: "3rem 1rem",
        fontFamily: "'Poppins', sans-serif",
        background: themeStyles.background,
        color: themeStyles.color,
        transition: "all 0.3s ease",
      }}
    >
      <div style={{ textAlign: "center", marginBottom: "2rem" }}>
        <h1 style={{ color: "#ff6f61" }}>🍳 AI 食譜推薦系統</h1>
        <button
          onClick={() => setDarkMode(!darkMode)}
          style={{
            marginTop: "0.5rem",
            padding: "0.5rem 1rem",
            borderRadius: "12px",
            border: "none",
            cursor: "pointer",
            background: darkMode
              ? "linear-gradient(90deg, #ff9472, #ff6f61)"
              : "linear-gradient(90deg, #6a11cb, #2575fc)",
            color: "white",
            fontWeight: "600",
            transition: "all 0.3s",
          }}
        >
          {darkMode ? "切換亮色模式" : "切換深色模式"}
        </button>
      </div>

      {/* 表單區 */}
      <div
        style={{
          maxWidth: "600px",
          margin: "0 auto 2rem auto",
          padding: "2rem",
          borderRadius: "16px",
          background: themeStyles.cardBg,
          boxShadow: "0 8px 20px rgba(0,0,0,0.1)",
          display: "flex",
          flexDirection: "column",
          gap: "1.5rem",
          transition: "all 0.3s",
        }}
      >
        <div style={{ display: "flex", flexDirection: "column" }}>
          <label style={{ fontWeight: "600", marginBottom: "0.5rem" }}>食材</label>
          <input
            type="text"
            value={ingredients}
            onChange={(e) => setIngredients(e.target.value)}
            placeholder="例如：雞肉, 洋蔥, 胡椒粉"
            style={{
              padding: "0.75rem 1rem",
              borderRadius: "12px",
              border: "1px solid #555",
              background: themeStyles.inputBg,
              color: themeStyles.inputColor,
              outline: "none",
              fontSize: "1rem",
              transition: "all 0.3s",
            }}
          />
        </div>

        <div style={{ display: "flex", flexDirection: "column" }}>
          <label style={{ fontWeight: "600", marginBottom: "0.5rem" }}>料理風格/飲食需求</label>
          <input
            type="text"
            value={styleOrDiet}
            onChange={(e) => setStyleOrDiet(e.target.value)}
            placeholder="例如：低碳、泰式、素食"
            style={{
              padding: "0.75rem 1rem",
              borderRadius: "12px",
              border: "1px solid #555",
              background: themeStyles.inputBg,
              color: themeStyles.inputColor,
              outline: "none",
              fontSize: "1rem",
              transition: "all 0.3s",
            }}
          />
        </div>

        <button
          onClick={handleGenerate}
          disabled={loading}
          style={{
            padding: "1rem",
            fontSize: "1.1rem",
            fontWeight: "600",
            color: "white",
            borderRadius: "14px",
            border: "none",
            cursor: loading ? "not-allowed" : "pointer",
            background: loading
              ? "gray"
              : "linear-gradient(90deg, #ff6f61, #ff9472)",
            boxShadow: "0 6px 15px rgba(255,111,97,0.4)",
            transition: "all 0.3s ease",
          }}
        >
          {loading ? "生成中..." : "生成食譜"}
        </button>
      </div>

      {/* 食譜卡片區 */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
          gap: "2rem",
          maxWidth: "1200px",
          margin: "0 auto",
        }}
      >
        {recipes.map((recipe, index) => (
          <div
            key={index}
            style={{
              background: themeStyles.cardBg,
              borderRadius: "16px",
              boxShadow: "0 10px 30px rgba(0,0,0,0.1)",
              padding: "1.5rem",
              transition: "transform 0.3s, box-shadow 0.3s",
              cursor: "pointer",
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = "translateY(-5px) scale(1.02)";
              e.currentTarget.style.boxShadow = "0 15px 35px rgba(0,0,0,0.2)";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = "translateY(0) scale(1)";
              e.currentTarget.style.boxShadow = "0 10px 30px rgba(0,0,0,0.1)";
            }}
          >
            <h2 style={{ color: "#ff6f61", marginBottom: "1rem" }}>{recipe.title}</h2>

            <h3>📝 食材清單</h3>
            <ul style={{ paddingLeft: "1.2rem", marginBottom: "1rem" }}>
              {recipe.ingredients.map((item, idx) => (
                <li key={idx} style={{ marginBottom: "0.3rem" }}>
                  {item}
                </li>
              ))}
            </ul>

            <h3>👩‍🍳 料理步驟</h3>
            <ol style={{ paddingLeft: "1.2rem", marginBottom: "1rem" }}>
              {recipe.steps.map((step, idx) => (
                <li key={idx} style={{ marginBottom: "0.3rem" }}>
                  {step}
                </li>
              ))}
            </ol>

            {recipe.imageUrl && (
              <div style={{ textAlign: "center" }}>
                <img
                  src={recipe.imageUrl}
                  alt="食譜圖片"
                  style={{
                    maxWidth: "100%",
                    borderRadius: "16px",
                    boxShadow: "0 6px 20px rgba(0,0,0,0.2)",
                    transition: "transform 0.3s",
                  }}
                  onMouseOver={(e) => (e.currentTarget.style.transform = "scale(1.05)")}
                  onMouseOut={(e) => (e.currentTarget.style.transform = "scale(1)")}
                />
              </div>
            )}
          </div>
        ))}
      </div>

      <style>
        {`
          @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
          }
        `}
      </style>
    </div>
  );
}

export default App;
