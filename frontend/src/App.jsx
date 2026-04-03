import { useState } from "react";
import SettingsForm from "./components/SettingsForm";
import { scanNow } from "./services/api";
import "./App.css";
import logo from "./assets/logo.svg";

function App() {
  const [isScanning, setIsScanning] = useState(false);
  const [scanSuccess, setScanSuccess] = useState(false);

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

              <span className={`save-check ${scanSuccess ? "visible" : ""}`}>
                ✓
              </span>
            </div>
          </div>

        </header>

        <SettingsForm />
      </div>
    </div>
  );
}

export default App;