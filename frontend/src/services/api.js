const BASE_URL_BATCH = "http://localhost:8080/api/batch/run";
const BASE_URL_SETTINGS = "http://localhost:8080/api/settings";
const BASE_URL_EMAIL = "http://localhost:8080/api/settings/email/connection";
const BASE_URL_NVD_CONNECTION = "http://localhost:8080/api/settings/nvd/connection"
const BASE_URL_PAST_EXECUTIONS = "http://localhost:8080/api/past-executions"

export async function scanNow() {
  const res = await fetch(BASE_URL_BATCH, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(),
  });

  if (!res.ok) throw new Error("Failed to start scan");

  const json = await res.json();
  return json.response;
}

export async function testEmailConnection() {
  const response = await fetch(BASE_URL_EMAIL);

  if (!response.ok) {
    throw new Error("Failed to test email connection");
  }

  const data = await response.json();
  return data.response;
}

export async function getSettings() {
  const res = await fetch(BASE_URL_SETTINGS);
  if (!res.ok) throw new Error("Failed to fetch settings");

  const json = await res.json();
  return json.response;
}

export async function patchSettings(data) {
  const res = await fetch(BASE_URL_SETTINGS, {
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

export async function testNvdConnection() {
  const response = await fetch(BASE_URL_NVD_CONNECTION);

  if (!response.ok) {
    throw new Error("Failed to test NVD connection");
  }

  const data = await response.json();
  return data.response === true;
}

export async function getPastExecutions(limit = 5) {
  const response = await fetch(
    `${BASE_URL_PAST_EXECUTIONS}?limit=${limit}`
  );

  if (!response.ok) {
    throw new Error("Failed to fetch past executions");
  }

  const data = await response.json();
  return data.response;
}

export async function retrieveVersionCheck() {
  const response = await fetch("http://localhost:8080/api/version/check");
  const data = await response.json();

  return data.response;
}