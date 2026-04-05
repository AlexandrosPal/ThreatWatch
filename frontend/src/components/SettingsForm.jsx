import { useEffect, useState } from "react";
import { testEmailConnection , getSettings, patchSettings, testNvdConnection } from "../services/api";


function formatHoursFromMinutes(minutes) {
  const num = Number(minutes);

  if (!num || isNaN(num)) return "";

  if (num < 60) return `= ${num} min`;

  const hours = num / 60;
  if (hours < 24) return `= ${hours.toFixed(2)} h`;

  const days = hours / 24;
  return `= ${days.toFixed(2)} d`;
}

function getSeverityMeta(score) {
  const num = Number(score);

  if (isNaN(num) || num <= 0) {
    return { label: "NONE", color: "#98a2b3" };
  }

  let label;
  if (num < 0) label = "UNKNOWN";
  else if (num < 4.0) label = "LOW";
  else if (num < 7.0) label = "MEDIUM";
  else if (num < 9.0) label = "HIGH";
  else label = "CRITICAL";

  const percent = num / 10;
  const hue = (1 - percent) * 120;

  const color = `hsl(${hue}, 70%, 45%)`;

  return { label, color };
}

export default function SettingsForm() {
  const [nvdApiKey, setNvdApiKey] = useState("");
  const [nvdApiKeyProvided, setNvdApiKeyProvided] = useState(false);
  const [savingNvdApiKey, setSavingNvdApiKey] = useState(false);
  const [nvdApiKeySaved, setNvdApiKeySaved] = useState(false);
  const [batchIntervalMinutes, setBatchIntervalMinutes] = useState("");
  const minBatchInterval = nvdApiKeyProvided ? 5 : 30;
  const isBatchInvalid =
    batchIntervalMinutes === "" ||
    Number(batchIntervalMinutes) < minBatchInterval;
  const [emails, setEmails] = useState([]);
  const [emailInput, setEmailInput] = useState("");
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const isEmailInvalid = emailInput.trim() === "" || !emailRegex.test(emailInput.trim());
  const [supportedProducts, setSupportedProducts] = useState([]);
  const [selectedProducts, setSelectedProducts] = useState([]);
  const [selectedProductInput, setSelectedProductInput] = useState("");
  const [loading, setLoading] = useState(true);
  const [savingBatch, setSavingBatch] = useState(false);
  const [savingEmail, setSavingEmail] = useState(false);
  const [savingProduct, setSavingProduct] = useState(false);
  const [enabled, setEnabled] = useState(false);
  const [savingEnabled, setSavingEnabled] = useState(false);
  const [minimumSeverityScore, setMinimumSeverityScore] = useState("7.0");
  const [savingSeverity, setSavingSeverity] = useState(false);
  const [batchSaved, setBatchSaved] = useState(false);
  const [severitySaved, setSeveritySaved] = useState(false);
  const [earlyAlerts, setEarlyAlerts] = useState(false);
  const [savingEarlyAlerts, setSavingEarlyAlerts] = useState(false);
  const [emailProviderHost, setEmailProviderHost] = useState("");
  const [emailProviderPort, setEmailProviderPort] = useState("");
  const [emailProviderUsername, setEmailProviderUsername] = useState("");
  const [emailProviderPassword, setEmailProviderPassword] = useState("");
  const [savingEmailProvider, setSavingEmailProvider] = useState(false);
  const [emailProviderSaved, setEmailProviderSaved] = useState(false);
  const isEmailProviderPortInvalid =
    emailProviderPort !== "" && !/^\d+$/.test(emailProviderPort);
  const [testingEmailConnection, setTestingEmailConnection] = useState(false);
  const [emailConnectionResult, setEmailConnectionResult] = useState(null);
  const [testingNvdConnection, setTestingNvdConnection] = useState(false);
  const [nvdConnectionResult, setNvdConnectionResult] = useState(null);

  const isEmailProviderInvalid =
    emailProviderHost.trim() === "" ||
    emailProviderPort.trim() === "" ||
    emailProviderUsername.trim() === "" ||
    emailProviderPassword.trim() === "" ||
    isEmailProviderPortInvalid;

  const percent = (minimumSeverityScore / 10) * 100;
  const color = getSeverityMeta(minimumSeverityScore).color;

  const availableProducts = supportedProducts.filter(
    (product) => !selectedProducts.includes(product)
  );
  const isProductInvalid = selectedProductInput.trim() === "" || availableProducts.length === 0;

  useEffect(() => {
    loadSettings();
  }, []);

  async function loadSettings(showPageLoader = false) {
    try {
      if (showPageLoader) {
        setLoading(true);
      }
      
      const data = await getSettings();

      const seconds = Number(data?.batchInterval || 0);
      const minutes = seconds ? Math.round(seconds / 60) : "";

      setBatchIntervalMinutes(minutes);
      setEmails(data?.emails || []);
      setSupportedProducts(data?.supportedProducts || []);
      setSelectedProducts(data?.productsSelected || []);
      setEnabled(String(data?.enabled).toLowerCase() === "true");
      setMinimumSeverityScore(data?.severityThreshold || "7.0");
      setEarlyAlerts(String(data?.earlyAlerts).toLowerCase() === "true");
      setEmailProviderHost(data?.emailProviderHost || "");
      setEmailProviderPort(data?.emailProviderPort || "");
      setEmailProviderUsername(data?.emailProviderUsername || "");
      setEmailProviderPassword("");
      
      const nvdProvided =
        String(data?.nvdApiKeyProvided).toLowerCase() === "true";
      
        setNvdApiKeyProvided(String(data?.nvdApiKeyProvided).toLowerCase() === "true");
      setNvdApiKey("");
      
      setNvdApiKeyProvided(nvdProvided);
      setNvdApiKey("");

      if (nvdProvided) {
        try {
          const result = await testNvdConnection();
          setNvdConnectionResult(result);
        } catch (err) {
          console.error(err);
          setNvdConnectionResult(false);
        }
      } else {
        setNvdConnectionResult(null);
      }
    } catch (err) {
      console.error(err);
      alert("Failed to load settings");
    } finally {
      setLoading(false);
    }
  }

  async function handleClearNvdApiKey() {
    try {
      setSavingNvdApiKey(true);

      await patchSettings({
        nvdApiKey: ""
      });

      setNvdApiKey("");
      setNvdApiKeyProvided(false);
      setNvdConnectionResult(null);
    } catch (err) {
      console.error(err);
      alert("Failed to clear NVD API key");
    } finally {
      setSavingNvdApiKey(false);
    }
  }

  async function handleTestEmailConnection() {
    try {
      setTestingEmailConnection(true);
      setEmailConnectionResult(null);

      const result = await testEmailConnection();
      setEmailConnectionResult(result);
    } catch (err) {
      console.error(err);
      setEmailConnectionResult(false);
    } finally {
      setTestingEmailConnection(false);
    }
  }

  function flashSaved(setter) {
    setter(true);
    setTimeout(() => setter(false), 1200);
   }

  async function handleToggleEnabled() {
    try {
        setSavingEnabled(true);

        await patchSettings({
        enabled: (!enabled).toString(),
        });

        setEnabled(!enabled);
    } catch (err) {
        console.error(err);
        alert("Failed to update enabled state");
    } finally {
        setSavingEnabled(false);
    }
    }

  async function handleSaveBatchInterval() {
    try {
      setSavingBatch(true);

      const minutes = Number(batchIntervalMinutes);
      const seconds = isNaN(minutes) ? 0 : minutes * 60;

      await patchSettings({
        batchInterval: String(seconds),
      });

      await loadSettings();
      flashSaved(setBatchSaved);

    } catch (err) {
      console.error(err);
      alert("Failed to save batch interval");
    } finally {
      setSavingBatch(false);
    }
  }

  async function handleAddEmail() {
    const trimmedEmail = emailInput.trim();
    if (!trimmedEmail) return;

    try {
      setSavingEmail(true);
      await patchSettings({
        email: trimmedEmail,
      });
      setEmailInput("");
      await loadSettings();
    } catch (err) {
      console.error(err);
      alert("Failed to add email");
    } finally {
      setSavingEmail(false);
    }
  }

  async function handleSaveEmailProvider() {
    try {
      setSavingEmailProvider(true);

      await patchSettings({
        emailProviderHost: emailProviderHost.trim(),
        emailProviderPort: emailProviderPort.trim(),
        emailProviderUsername: emailProviderUsername.trim(),
        emailProviderPassword: emailProviderPassword,
      });

      flashSaved(setEmailProviderSaved);
    } catch (err) {
      console.error(err);
      alert("Failed to save email provider settings");
    } finally {
      setSavingEmailProvider(false);
    }
  }

  async function handleRemoveEmail(emailToRemove) {
    try {
      setSavingEmail(true);
      await patchSettings({
        email: emailToRemove,
      });
      await loadSettings();
    } catch (err) {
      console.error(err);
      alert("Failed to remove email");
    } finally {
      setSavingEmail(false);
    }
  }

  async function handleAddProduct() {
    const product = selectedProductInput.trim();
    if (!product) return;

    try {
      setSavingProduct(true);
      await patchSettings({
        productAddition: product,
      });
      setSelectedProductInput("");
      await loadSettings();
    } catch (err) {
      console.error(err);
      alert("Failed to add product");
    } finally {
      setSavingProduct(false);
    }
  }

  async function handleRemoveProduct(productToRemove) {
    try {
      setSavingProduct(true);
      await patchSettings({
        productAddition: productToRemove,
      });
      await loadSettings();
    } catch (err) {
      console.error(err);
      alert("Failed to remove product");
    } finally {
      setSavingProduct(false);
    }
  }

  async function handleSaveSeverity() {
    try {
        setSavingSeverity(true);

        await patchSettings({
        severityThreshold: String(minimumSeverityScore),
        });

        await loadSettings();
        flashSaved(setSeveritySaved);
    } catch (err) {
        console.error(err);
        alert("Failed to save severity filter");
    } finally {
        setSavingSeverity(false);
    }
  }

  async function handleToggleEarlyAlerts() {
    try {
      setSavingEarlyAlerts(true);

      await patchSettings({
        earlyAlerts: (!earlyAlerts).toString(),
      });

      setEarlyAlerts(!earlyAlerts);
    } catch (err) {
      console.error(err);
      alert("Failed to update early alerts setting");
    } finally {
      setSavingEarlyAlerts(false);
    }
  }

  async function handleTestNvdConnection() {
    try {
      setTestingNvdConnection(true);
      setNvdConnectionResult(null);

      const result = await testNvdConnection();
      setNvdConnectionResult(result);
    } catch (err) {
      console.error(err);
      setNvdConnectionResult(false);
    } finally {
      setTestingNvdConnection(false);
    }
  }

  async function handleSaveNvdApiKey() {
    try {
      setSavingNvdApiKey(true);
      setTestingNvdConnection(true);
      setNvdConnectionResult(null);

      const trimmedKey = nvdApiKey.trim();

      if (!trimmedKey) {
        return;
      }

      await patchSettings({
        nvdApiKey: trimmedKey,
      });

      const result = await testNvdConnection();

      setNvdConnectionResult(result);
      setNvdApiKey("");
      setNvdApiKeyProvided(result);
      flashSaved(setNvdApiKeySaved);
    } catch (err) {
      console.error(err);
      setNvdConnectionResult(false);
      alert("Failed to save or validate NVD API key");
    } finally {
      setSavingNvdApiKey(false);
      setTestingNvdConnection(false);
    }
  }

  if (loading) {
    return <p className="loading-text">Loading settings...</p>;
  }

  return (
    <div className="settings-grid">
      <section className="card">
        <div className="card-header">
          <div>
            <h2>Batch Interval</h2>
            <p>How often the background job should run.</p>
          </div>
        </div>

        <div className="input-row input-row-top">
          <div className="field-block">
            <div className="input-with-suffix">
              <input
                className={`text-input ${isBatchInvalid ? "input-error" : ""}`}
                type="text"
                inputMode="numeric"
                value={batchIntervalMinutes}
                onChange={(e) => {
                  const value = e.target.value;

                  if (/^\d*$/.test(value)) {
                    setBatchIntervalMinutes(value);
                  }
                }}
                placeholder="Batch interval in minutes"
              />
              <span className="input-suffix">
                {formatHoursFromMinutes(batchIntervalMinutes)}
              </span>
            </div>

            {isBatchInvalid && (
              <p className="error-text">
                Minimum value is {minBatchInterval} minute{minBatchInterval > 1 ? "s" : ""}
              </p>
            )}
          </div>

          <div className="save-action-wrap">
            <button
              type="button"
              className="primary-button"
              onClick={handleSaveBatchInterval}
              disabled={savingBatch || isBatchInvalid}
            >
              {savingBatch ? "Saving..." : "Save"}
            </button>

            <span className={`save-check ${batchSaved ? "visible" : ""}`}>✓</span>
          </div>
        </div>
      </section>

      <section className="card">
        <div className="card-header">
          <div>
            <h2>Email Provider</h2>
            <p>Configure the SMTP server used to send vulnerability alerts.</p>
          </div>
        </div>

        <div className="provider-row">
          <input
            className="text-input"
            type="text"
            value={emailProviderHost}
            onChange={(e) => {
              setEmailProviderHost(e.target.value);
              setEmailConnectionResult(null);
            }}
            placeholder="SMTP host"
          />

          <input
            className="text-input provider-port-input"
            type="text"
            value={emailProviderPort}
            onChange={(e) => {
              setEmailProviderPort(e.target.value);
              setEmailConnectionResult(null);
            }}
            placeholder="Port"
          />
        </div>

        <div className="provider-row">
          <input
            className="text-input"
            type="text"
            value={emailProviderUsername}
            onChange={(e) => {
              setEmailProviderUsername(e.target.value);
              setEmailConnectionResult(null);
            }}
            placeholder="Username"
          />

          <input
            className="text-input"
            type="password"
            value={emailProviderPassword}
            onChange={(e) => {
              setEmailProviderPassword(e.target.value);
              setEmailConnectionResult(null);
            }}
            placeholder="Leave empty to keep current password if set"
          />
        </div>

        <div className="provider-actions-row">
          <div className="save-action-wrap">
            <button
              type="button"
              className="primary-button"
              onClick={handleSaveEmailProvider}
            >
              {savingEmailProvider ? "Saving..." : "Save"}
            </button>

            <span className={`save-check ${emailProviderSaved ? "visible" : ""}`}>✓</span>
          </div>

          <div className="connection-test-wrap">
            <button
              type="button"
              className="secondary-button"
              onClick={handleTestEmailConnection}
              disabled={testingEmailConnection}
            >
              {testingEmailConnection ? "Testing..." : "Test connection"}
            </button>

            {emailConnectionResult === true && (
              <span className="connection-result success">Connected</span>
            )}

            {emailConnectionResult === false && (
              <span className="connection-result error">Failed</span>
            )}
          </div>
        </div>
      </section>

      <section className="card">
        <div className="card-header">
          <div>
            <h2>Emails</h2>
            <p>Add or remove notification recipients.</p>
          </div>
        </div>

        <div className="input-row">
          <input
            className={`text-input ${isEmailInvalid && emailInput ? "input-error" : ""}`}
            value={emailInput}
            onChange={(e) => setEmailInput(e.target.value)}
            placeholder="name@example.com"
          />
          <button
            className="icon-button"
            onClick={handleAddEmail}
            disabled={savingEmail || isEmailInvalid}
            aria-label="Add email"
            title="Add email"
          >
            +
          </button>
          {isEmailInvalid && emailInput && (
            <p className="error-text">Enter a valid email address</p>
          )}
        </div>

        <div className="chips-wrap">
          {emails.length === 0 ? (
            <p className="muted-text">No emails added yet.</p>
          ) : (
            emails.map((email) => (
              <div key={email} className="chip">
                <span>{email}</span>
                <button
                  className="chip-remove"
                  onClick={() => handleRemoveEmail(email)}
                  disabled={savingEmail}
                  aria-label={`Remove ${email}`}
                  title={`Remove ${email}`}
                >
                  ×
                </button>
              </div>
            ))
          )}
        </div>
      </section>
      
      <section className="card">
        <div className="card-header">
          <div>
            <h2>Watchlist</h2>
            <p>Select the technologies you want to receive alerts for.</p>
          </div>
        </div>

        <div className="input-row">
          <select
            className="text-input"
            value={selectedProductInput}
            onChange={(e) => setSelectedProductInput(e.target.value)}
            disabled={availableProducts.length === 0}
          >
            <option value="">
              {availableProducts.length === 0 ? "No more items available" : "Select item"}
            </option>
            {availableProducts.map((product) => (
              <option key={product} value={product}>
                {product}
              </option>
            ))}
          </select>

          <button
            className="icon-button"
            onClick={handleAddProduct}
            disabled={savingProduct || isProductInvalid}
            aria-label="Add product"
            title="Add product"
          >
            +
          </button>
        </div>

        <div className="chips-wrap">
          {selectedProducts.length === 0 ? (
            <p className="muted-text">No products selected yet.</p>
          ) : (
            selectedProducts.map((product) => (
              <div key={product} className="chip">
                <span>{product}</span>
                <button
                  className="chip-remove"
                  onClick={() => handleRemoveProduct(product)}
                  disabled={savingProduct}
                  aria-label={`Remove ${product}`}
                  title={`Remove ${product}`}
                >
                  ×
                </button>
              </div>
            ))
          )}
        </div>
      </section>
      <section className="card">
        <div className="card-header">
            <div>
            <h2>Scheduler</h2>
            <p>Enable or disable automatic execution.</p>
            </div>
        </div>

        <div className="toggle-row">
            <span className="toggle-label">
            Scheduler
            </span>

            <button
            type="button"
            className={`toggle-switch ${enabled ? "active" : ""}`}
            onClick={handleToggleEnabled}
            disabled={savingEnabled}
            >
            <span className="toggle-thumb"></span>
            </button>
        </div>
        <div className="toggle-divider"></div>
        <div className="toggle-row">
        <div className="label-with-tooltip">
          <span className="toggle-label">
            Early alerts
          </span>

          <div className="tooltip-container">
            <span className="tooltip-icon">?</span>

            <div className="tooltip-text">
              Include CVEs without severity score (early-stage vulnerabilities).
            </div>
          </div>
        </div>

        <button
          type="button"
          className={`toggle-switch ${earlyAlerts ? "active" : ""}`}
          onClick={handleToggleEarlyAlerts}
          disabled={savingEarlyAlerts}
        >
          <span className="toggle-thumb"></span>
        </button>
      </div>
      </section>
      <section className="card">
        <div className="card-header">
            <div>
            <h2>Severity Threshold</h2>
            <p>Only send alerts above the selected CVSS threshold.</p>
            </div>
        </div>

        <div className="severity-card-content">
            <input
                type="range"
                min="0.1"
                max="10"
                step="0.1"
                value={minimumSeverityScore}
                onChange={(e) => setMinimumSeverityScore(e.target.value)}
                className="severity-slider"
                style={{
                    background: `linear-gradient(to right, ${color} 0%, ${color} ${percent}%, #e5e7eb ${percent}%, #e5e7eb 100%)`
                }}
            />

            <div className="severity-meta-row">
            <div
                className="severity-badge"
                style={{ backgroundColor: getSeverityMeta(minimumSeverityScore).color }}
            >
                {getSeverityMeta(minimumSeverityScore).label}
            </div>

            <span className="severity-score">
                {Number(minimumSeverityScore).toFixed(1)}
            </span>
            </div>

            <div className="severity-footer">
            <span className="muted-text">0.1</span>
            <span className="muted-text">10.0</span>
            </div>

            <div style={{ marginTop: "16px" }}>
            <div className="save-action-wrap">
                <button
                    type="button"
                    className="primary-button"
                    onClick={handleSaveSeverity}
                    disabled={savingSeverity}
                >
                    {savingSeverity ? "Saving..." : "Save"}
                </button>

                <span className={`save-check ${severitySaved ? "visible" : ""}`}>✓</span>
              </div>
            </div>
        </div>
      </section>
      <section className="card">
        <div className="card-header">
          <div>
            <h2>NVD API Key</h2>
            <p>
              Add an NVD API key to allow shorter batch intervals and higher request throughput.
            </p>
          </div>
        </div>

        <div className="provider-row">
          <input
            className="text-input"
            type="password"
            value={nvdApiKey}
            onChange={(e) => {
              setNvdApiKey(e.target.value);
              setNvdConnectionResult(null);
            }}
            placeholder="Leave empty to keep current key"
          />
        </div>

        <div className="provider-actions-row">
          <div className="save-action-wrap">
            <button
              type="button"
              className="primary-button"
              onClick={handleSaveNvdApiKey}
              disabled={savingNvdApiKey || nvdApiKey.trim() === ""}
            >
              {savingNvdApiKey ? "Saving..." : "Save"}
            </button>

            <span className={`save-check ${nvdApiKeySaved ? "visible" : ""}`}>✓</span>
          </div>

          <div className="connection-test-wrap">
            {nvdApiKeyProvided && (
              <button
                type="button"
                className="secondary-button danger-button"
                onClick={handleClearNvdApiKey}
                disabled={savingNvdApiKey}
              >
                Clear key
              </button>
            )}

            {testingNvdConnection && (
              <span className="connection-result">Testing...</span>
            )}

            {!testingNvdConnection && nvdConnectionResult === true && (
              <span className="connection-result success">Configured</span>
            )}

            {!testingNvdConnection && nvdConnectionResult === false && (
              <span className="connection-result error">Invalid API key</span>
            )}

            {!testingNvdConnection && nvdConnectionResult === null && (
              <span className={`connection-result ${nvdApiKeyProvided ? "success" : "error"}`}>
                {nvdApiKeyProvided ? "API key configured" : "No API key configured"}
              </span>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}