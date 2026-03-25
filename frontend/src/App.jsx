import SettingsForm from "./components/SettingsForm";
import "./App.css";
import logo from "./assets/logo.svg";

function App() {
  return (
    <div className="app-shell">
      <div className="page">
        <header className="page-header">
          <div className="title-row">
            <img src={logo} alt="ThreatWatch logo" className="logo" />
            <h1>ThreatWatch</h1>
          </div>

          <p>Self-hosted vulnerability monitoring settings</p>
        </header>

        <SettingsForm />
      </div>
    </div>
  );
}

export default App;