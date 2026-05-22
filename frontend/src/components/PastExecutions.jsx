import { useEffect, useState } from "react";
import { getPastExecutions } from "../services/api";

function PastExecutions() {

  const [executions, setExecutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedExecution, setExpandedExecution] = useState(null);
  const [limit, setLimit] = useState(5);

  useEffect(() => {

    async function loadExecutions() {

      try {

        const data = await getPastExecutions(limit);
        setExecutions(data);

      } catch (err) {

        console.error(err);

      } finally {

        setLoading(false);
      }
    }

    loadExecutions();

  }, [limit]);

  function getSeverityCounts(cves) {

    const counts = {
      CRITICAL: 0,
      HIGH: 0,
      MEDIUM: 0,
      LOW: 0,
      UNKNOWN: 0,
    };

    cves.forEach((cve) => {

      if (counts[cve.severity] !== undefined) {
        counts[cve.severity]++;
      } else {
        counts.UNKNOWN++;
      }
    });

    return counts;
  }

  function getProducts(cves) {

    const products = {};

    cves.forEach((cve) => {

      if (!products[cve.product]) {
        products[cve.product] = 0;
      }

      products[cve.product]++;
    });

    return products;
  }

  if (loading) {
    return (
      <div className="card">
        <p className="loading-text">
          Loading past executions...
        </p>
      </div>
    );
  }

  if (executions.length === 0) {
    return (
      <div className="card">
        <p className="muted-text">
          No executions found yet.
        </p>
      </div>
    );
  }

  return (
    <>
      <div className="execution-toolbar">
        <div className="execution-toolbar-left">
          <h2>Recent executions</h2>
        </div>

        <div className="execution-toolbar-right">

          <label htmlFor="execution-limit">
            Show
          </label>

          <select
            id="execution-limit"
            value={limit}
            onChange={(e) =>
              setLimit(Number(e.target.value))
            }
            className="execution-limit-select"
          >

            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={15}>15</option>
            <option value={20}>20</option>

          </select>

        </div>

      </div>
      <div className="execution-list">

        {executions.map((execution, index) => {

          const severityCounts =
            getSeverityCounts(execution.cves);

          const products =
            getProducts(execution.cves);

          const isExpanded =
            expandedExecution === index;

          return (

            <div
              key={execution.timestamp}
              className="card execution-card"
            >

              <div className="execution-header">

                <div>

                  <h2>
                    {execution.totalCves} vulnerabilities detected
                  </h2>

                  <p>
                    {new Date(execution.timestamp)
                      .toLocaleString()}
                  </p>

                </div>

              </div>

              <div className="execution-badges">

                {severityCounts.CRITICAL > 0 && (
                  <span className="severity-pill critical">
                    Critical {severityCounts.CRITICAL}
                  </span>
                )}

                {severityCounts.HIGH > 0 && (
                  <span className="severity-pill high">
                    High {severityCounts.HIGH}
                  </span>
                )}

                {severityCounts.MEDIUM > 0 && (
                  <span className="severity-pill medium">
                    Medium {severityCounts.MEDIUM}
                  </span>
                )}

                {severityCounts.LOW > 0 && (
                  <span className="severity-pill low">
                    Low {severityCounts.LOW}
                  </span>
                )}

              </div>

              <div className="execution-products">

                {Object.entries(products).map(([product, count]) => (

                  <div
                    key={product}
                    className="execution-product"
                  >
                    <strong>{product}</strong>
                    <span>{count} CVEs</span>
                  </div>

                ))}

              </div>

              <div className="execution-footer">

                {execution.cves.length === 0 ? (

                  <div className="clean-scan-badge">
                    ✓ Clean scan
                  </div>

                ) : (

                  <button
                    className="secondary-button"
                    onClick={() =>
                      setExpandedExecution(
                        isExpanded ? null : index
                      )
                    }
                  >
                    {isExpanded
                      ? "Hide CVEs"
                      : `View CVEs (${execution.cves.length})`}
                  </button>

                )}

              </div>

              {isExpanded && (

                <div className="execution-cves">

                  {execution.cves.map((cve) => (

                    <div
                      key={cve.id}
                      className="execution-cve-card"
                    >

                      <div className="execution-cve-top">

                        <div>

                          <h3>
                            {cve.product} | {cve.id}
                          </h3>

                          <p>
                            {cve.published}
                          </p>

                        </div>

                        <span
                          className={`severity-pill ${cve.severity.toLowerCase()}`}
                        >
                          {cve.severity}
                        </span>

                      </div>

                      <p className="execution-cve-description">
                        {cve.description}
                      </p>

                      <a
                        href={`https://nvd.nist.gov/vuln/detail/${cve.id}`}
                        target="_blank"
                        rel="noreferrer"
                      >
                        View on NVD →
                      </a>

                    </div>

                  ))}

                </div>

              )}

            </div>
          );
        })}

      </div>
    </>
  );
}

export default PastExecutions;