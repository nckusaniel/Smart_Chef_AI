export async function generateRecipe(ingredients, style) {
  const response = await fetch("http://localhost:8080/api/recipe/generate", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ingredients, style })
  });
  return response.json();
}
