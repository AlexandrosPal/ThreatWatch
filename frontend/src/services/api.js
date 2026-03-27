const BASE_URL = "http://localhost:8080/api/settings";

export async function getSettings() {
  const res = await fetch(BASE_URL);
  if (!res.ok) throw new Error("Failed to fetch settings");

  const json = await res.json();
  return json.response;
}

export async function patchSettings(data) {
  const res = await fetch(BASE_URL, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!res.ok) throw new Error("Failed to update settings");

  const json = await res.json();
  return json.response;
}