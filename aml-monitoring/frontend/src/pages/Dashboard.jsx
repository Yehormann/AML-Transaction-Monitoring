import { useState } from 'react';
import {
  MOCK_STATS_ANALYST,
  MOCK_STATS_ADMIN,
  MOCK_TRANSACTIONS,
  MOCK_ALERTS,
  MOCK_RULES,
  MOCK_ADVANCED_STATS,
} from '../mockData';
import StatCard from '../components/StatCard/StatCard';
import TransactionTable from '../components/TransactionTable/TransactionTable';
import AlertCard from '../components/AlertCard/AlertCard';
import RuleEnginePanel from '../components/RuleEnginePanel/RuleEnginePanel';
import AdvancedStatsPanel from '../components/AdvancedStatsPanel/AdvancedStatsPanel';
import './Dashboard.css';

function Dashboard({ role }) {
  const [alerts, setAlerts] = useState(MOCK_ALERTS);

  function handleEscalate(id) {
    if (window.confirm(`Escalate alert ${id}?`)) {
      setAlerts((prev) =>
        prev.map((a) => (a.id === id ? { ...a, status: 'ESCALATED' } : a))
      );
    }
  }

  function handleDismiss(id) {
    const note = window.prompt(`Dismiss alert ${id}. Enter analyst note:`);
    if (note !== null) {
      setAlerts((prev) =>
        prev.map((a) =>
          a.id === id ? { ...a, status: 'DISMISSED', analystNote: note } : a
        )
      );
    }
  }

  function handleOverrideReopen(id) {
    setAlerts((prev) =>
      prev.map((a) =>
        a.id === id ? { ...a, status: 'OPEN', analystNote: null } : a
      )
    );
  }

  function handleFileSar(id) {
    window.alert('SAR filed for ' + id);
  }

  if (role === 'ADMIN') {
    const s = MOCK_STATS_ADMIN;
    return (
      <div className="dashboard">
        <div className="dashboard-stats-row">
          <StatCard label="Total transactions" value={s.totalTransactions} sub={s.totalSub} />
          <StatCard label="Open alerts" value={s.openAlerts} valueColor="var(--color-amber)" sub={s.openAlertsSub} />
          <StatCard label="SARs filed" value={s.sarsFiled} valueColor="var(--color-red)" sub={s.sarsFiledSub} />
          <StatCard label="Override actions" value={s.overrideActions} valueColor="var(--color-teal)" sub={s.overrideActionsSub} />
        </div>

        <div className="dashboard-two-col">
          <div className="dashboard-col">
            <div className="dashboard-section-label-row">
              <h2 className="dashboard-section-title" style={{ marginBottom: 0 }}>Alert management</h2>
              <span className="dashboard-admin-badge">admin controls</span>
            </div>
            <div className="dashboard-admin-card">
              <div className="dashboard-admin-card-header">Open alerts &mdash; admin view</div>
              {alerts.map((alert) => (
                <AlertCard
                  key={alert.id}
                  alert={alert}
                  role="ADMIN"
                  onEscalate={handleEscalate}
                  onDismiss={handleDismiss}
                  onOverrideReopen={handleOverrideReopen}
                  onFileSar={handleFileSar}
                  inline
                />
              ))}
            </div>
          </div>

          <div className="dashboard-col">
            <div className="dashboard-section-label-row">
              <h2 className="dashboard-section-title" style={{ marginBottom: 0 }}>Rule engine config</h2>
              <span className="dashboard-admin-badge">read only</span>
            </div>
            <RuleEnginePanel rules={MOCK_RULES} />
          </div>
        </div>

        <div>
          <div className="dashboard-section-label-row">
            <h2 className="dashboard-section-title" style={{ marginBottom: 0 }}>Advanced stats</h2>
            <span className="dashboard-admin-badge">admin only</span>
          </div>
          <AdvancedStatsPanel stats={MOCK_ADVANCED_STATS} />
        </div>
      </div>
    );
  }

  const s = MOCK_STATS_ANALYST;
  return (
    <div className="dashboard">
      <div className="dashboard-stats-row">
        <StatCard label="Total transactions" value={s.totalTransactions} />
        <StatCard label="Open alerts" value={s.openAlerts} valueColor="var(--color-amber)" />
        <StatCard label="SARs filed" value={s.sarsFiled} valueColor="var(--color-red)" />
        <StatCard label="Avg risk score" value={s.avgRiskScore} />
      </div>

      <div>
        <h2 className="dashboard-section-title">Recent transactions</h2>
        <TransactionTable transactions={MOCK_TRANSACTIONS} limit={4} />
      </div>

      <div>
        <h2 className="dashboard-section-title">Open alerts</h2>
        <div className="dashboard-alerts-grid">
          {alerts.map((alert) => (
            <AlertCard
              key={alert.id}
              alert={alert}
              role="ANALYST"
              onEscalate={handleEscalate}
              onDismiss={handleDismiss}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
