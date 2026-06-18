import { useState, useEffect } from 'react';
import { getDashboardStats, getTransactions, getAlerts, escalateAlert, dismissAlert } from '../api/client';

const RULES = [
  { name: 'Large amount', threshold: '> \u20AC10,000', score: '+40 pts' },
  { name: 'High-risk country', threshold: 'IR, KP, RU, BY', score: '+35 pts' },
  { name: 'Structuring', threshold: '5 txs / 7 days', score: '+35 pts' },
  { name: 'Velocity', threshold: '20+ txs in 2h', score: '+30 pts' },
  { name: 'Dormant account', threshold: 'inactive 2yr+', score: '+25 pts' },
  { name: 'Round trip', threshold: 'round amounts', score: '+20 pts' },
];

const ADVANCED_STATS = [
  { label: 'SAR filing rate', value: '5.4%', sub: 'of all flagged alerts' },
  { label: 'Top risky account', value: 'ACC-7743', sub: 'score 100 \u00B7 3 alerts' },
  { label: 'Most triggered rule', value: 'LargeAmount', sub: 'fired 38 times' },
  { label: 'Overrides this month', value: '3', sub: '2 reopened \u00B7 1 reversed' },
];
import StatCard from '../components/StatCard/StatCard';
import TransactionTable from '../components/TransactionTable/TransactionTable';
import AlertCard from '../components/AlertCard/AlertCard';
import RuleEnginePanel from '../components/RuleEnginePanel/RuleEnginePanel';
import AdvancedStatsPanel from '../components/AdvancedStatsPanel/AdvancedStatsPanel';
import './Dashboard.css';

function parseFiredRules(raw) {
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try { return JSON.parse(raw); } catch { return []; }
  }
  return [];
}

function mapTransaction(tx) {
  return {
    ...tx,
    firedRules: parseFiredRules(tx.firedRules).map((r) => ({ name: r.ruleName || r.name, score: r.score })),
  };
}

function mapAlert(a) {
  return {
    ...a,
    firedRules: parseFiredRules(a.firedRules).map((r) => ({ name: r.ruleName || r.name, score: r.score })),
  };
}

function Dashboard({ role }) {
  const [stats, setStats] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    getDashboardStats().then(setStats).catch(() => {});
    getTransactions().then((data) => setTransactions(data.map(mapTransaction))).catch(() => {});
    getAlerts().then((data) => setAlerts(data.map(mapAlert))).catch(() => {});
  }, []);

  async function handleEscalate(id) {
    if (!window.confirm(`Escalate alert ${id}?`)) return;
    try {
      const updated = await escalateAlert(id);
      setAlerts((prev) => prev.map((a) => (a.id === id ? mapAlert(updated) : a)));
      getDashboardStats().then(setStats).catch(() => {});
    } catch (err) {
      window.alert('Escalate failed: ' + err.message);
    }
  }

  async function handleDismiss(id) {
    const note = window.prompt(`Dismiss alert ${id}. Enter analyst note:`);
    if (note === null) return;
    try {
      const updated = await dismissAlert(id, note);
      setAlerts((prev) => prev.map((a) => (a.id === id ? mapAlert(updated) : a)));
      getDashboardStats().then(setStats).catch(() => {});
    } catch (err) {
      window.alert('Dismiss failed: ' + err.message);
    }
  }

  function handleOverrideReopen() {
    window.alert('Override/reopen is not supported by the backend yet.');
  }

  function handleFileSar() {
    window.alert('SAR is auto-filed on escalation by the backend.');
  }

  if (!stats) {
    return <div className="dashboard" style={{ padding: 32 }}>Loading...</div>;
  }

  if (role === 'ADMIN') {
    return (
      <div className="dashboard">
        <div className="dashboard-stats-row">
          <StatCard label="Total transactions" value={stats.totalTransactions} />
          <StatCard label="Open alerts" value={stats.openAlerts} valueColor="var(--color-amber)" />
          <StatCard label="SARs filed" value={stats.totalSarsFiled} valueColor="var(--color-red)" />
          <StatCard label="Flagged %" value={stats.flaggedPercentage.toFixed(1) + '%'} valueColor="var(--color-teal)" />
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
              {alerts.length === 0 && <div style={{ padding: 16, color: '#888' }}>No alerts</div>}
            </div>
          </div>

          <div className="dashboard-col">
            <div className="dashboard-section-label-row">
              <h2 className="dashboard-section-title" style={{ marginBottom: 0 }}>Rule engine config</h2>
              <span className="dashboard-admin-badge">read only</span>
            </div>
            <RuleEnginePanel rules={RULES} />
          </div>
        </div>

        <div>
          <div className="dashboard-section-label-row">
            <h2 className="dashboard-section-title" style={{ marginBottom: 0 }}>Advanced stats</h2>
            <span className="dashboard-admin-badge">admin only</span>
          </div>
          <AdvancedStatsPanel stats={ADVANCED_STATS} />
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <div className="dashboard-stats-row">
        <StatCard label="Total transactions" value={stats.totalTransactions} />
        <StatCard label="Open alerts" value={stats.openAlerts} valueColor="var(--color-amber)" />
        <StatCard label="SARs filed" value={stats.totalSarsFiled} valueColor="var(--color-red)" />
        <StatCard label="Flagged %" value={stats.flaggedPercentage.toFixed(1) + '%'} />
      </div>

      <div>
        <h2 className="dashboard-section-title">Recent transactions</h2>
        <TransactionTable transactions={transactions} limit={4} />
      </div>

      <div>
        <h2 className="dashboard-section-title">Open alerts</h2>
        <div className="dashboard-alerts-grid">
          {alerts.filter((a) => a.status === 'OPEN').map((alert) => (
            <AlertCard
              key={alert.id}
              alert={alert}
              role="ANALYST"
              onEscalate={handleEscalate}
              onDismiss={handleDismiss}
            />
          ))}
          {alerts.filter((a) => a.status === 'OPEN').length === 0 && (
            <span style={{ color: '#888' }}>No open alerts</span>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
