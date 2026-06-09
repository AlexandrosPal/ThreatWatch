import { useEffect, useState } from "react";
import SettingsForm from "./components/SettingsForm";
import PastExecutions from "./components/PastExecutions";
import { scanNow } from "./services/api";
import "./App.css";
import logo from "./assets/logo.svg";
import { retrieveVersionCheck } from "./services/api";

function App() {
  const [isScanning, setIsScanning] = useState(false);
  const [scanSuccess, setScanSuccess] = useState(false);
  const [activeTab, setActiveTab] = useState("basic");

  const [versionInfo, setVersionInfo] = useState(null);

  useEffect(() => {
    async function loadVersionInfo() {
      try {
        const response = await retrieveVersionCheck();
        setVersionInfo(response);
      } catch (err) {
        console.error(err);
      }
    }

    loadVersionInfo();
  }, []);

  const handleManualScan = async () => {
    try {
      setIsScanning(true);
      await scanNow();
      setScanSuccess(true);
      setTimeout(() => setScanSuccess(false), 1200);
    } catch (err) {
      console.error(err);
      alert("Failed to trigger scan");
    } finally {
      setIsScanning(false);
    }
  };

  return (
    <div className="app-shell">
      {
        versionInfo?.updateAvailable && (
          <div className="update-banner">

            <span>
              🚀 ThreatWatch {versionInfo.latestVersion} is available.
              You're currently running {versionInfo.currentVersion}.
            </span>

            <a
              href="https://github.com/AlexandrosPal/ThreatWatch/releases"
              target="_blank"
              rel="noopener noreferrer"
            >
              View release notes
            </a>

          </div>
        )
      }
      <div className="page">
        <header className="page-header">
          <div className="title-row">
            <img src={logo} alt="ThreatWatch logo" className="logo" />
            <h1>ThreatWatch</h1>
          </div>

          <div className="header-bottom">
            <p>Self-hosted vulnerability monitoring settings</p>

            <div className="header-actions">
              <button
                className="primary-button"
                onClick={handleManualScan}
                disabled={isScanning}
              >
                {isScanning ? "Scanning..." : "Scan now"}
              </button>

              <span className={`save-check ${scanSuccess ? "visible" : ""}`}>✓</span>
            </div>
          </div>
        </header>

        <nav className="settings-tabs">
          <button
            type="button"
            className={`tab-button ${activeTab === "basic" ? "active" : ""}`}
            onClick={() => setActiveTab("basic")}
          >
            Basic configuration
          </button>

          <button
            type="button"
            className={`tab-button ${activeTab === "notifications" ? "active" : ""}`}
            onClick={() => setActiveTab("notifications")}
          >
            Notifications
          </button>

          <button
            type="button"
            className={`tab-button ${activeTab === "executions" ? "active" : ""}`}
            onClick={() => setActiveTab("executions")}
          >
            Executions
          </button>
        </nav>

        <SettingsForm activeTab={activeTab} />
        {activeTab === "executions" && (
          <PastExecutions />
        )}
        
        <footer className="app-footer">
          Found a bug or have a feature request?{" "}
          <a
            href="https://github.com/AlexandrosPal/ThreatWatch/issues"
            target="_blank"
            rel="noopener noreferrer"
          >
            Open an issue on GitHub
          </a>
        </footer>
      </div>
    </div>
  );
}

export default App;