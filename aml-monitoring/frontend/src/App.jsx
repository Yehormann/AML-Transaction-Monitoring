import { useState, useEffect } from 'react';
import { getDashboardStats, getTransactions } from './api/client';
import LoginModal from './components/LoginModal/LoginModal';
import Sidebar from './components/Sidebar/Sidebar';
import Topbar from './components/Topbar/Topbar';
import Dashboard from './pages/Dashboard';
import Transactions from './pages/Transactions';
import Alerts from './pages/Alerts';
import SarReports from './pages/SarReports';
import AuditLog from './pages/AuditLog';
import RuleEngine from './pages/RuleEngine';
import AdvancedStats from './pages/AdvancedStats';

function exportCsv(transactions) {
  const headers = ['ID','Sender','SenderCountry','Receiver',
                   'ReceiverCountry','Amount','Currency',
                   'RiskScore','Status','Timestamp'];
  const rows = transactions.map(t => [
    t.id, t.senderAccount, t.senderCountry,
    t.receiverAccount, t.receiverCountry,
    t.amount, t.currency, t.riskScore, t.status, t.timestamp
  ]);
  const csv = [headers, ...rows].map(r => r.join(',')).join('\n');
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'transactions.csv';
  a.click();
  URL.revokeObjectURL(url);
}

function App() {
  const [role, setRole] = useState(null);
  const [activePage, setActivePage] = useState('dashboard');
  const [openAlertCount, setOpenAlertCount] = useState(0);

  useEffect(() => {
    if (!role) return;
    getDashboardStats()
      .then((stats) => setOpenAlertCount(stats.openAlerts || 0))
      .catch(() => {});
  }, [role, activePage]);

  if (!role) {
    return <LoginModal onLogin={(r) => setRole(r)} />;
  }

  function handleExportCsv() {
    getTransactions()
      .then(exportCsv)
      .catch(() => window.alert('Failed to export CSV'));
  }

  const subtitle = role === 'ADMIN' ? 'Administrator view' : 'Compliance analyst view';

  return (
    <div style={{ display: 'flex', flexDirection: 'row', height: '100vh', overflow: 'hidden' }}>
      <Sidebar role={role} activePage={activePage} onNavigate={setActivePage} alertCount={openAlertCount} />
      <div style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
        <Topbar
          title="Dashboard"
          subtitle={subtitle}
          showExportCsv={role === 'ADMIN'}
          onExportCsv={handleExportCsv}
        />
        <div style={{ flex: 1, overflowY: 'auto' }}>
          {activePage === 'dashboard' && <Dashboard role={role} />}
          {activePage === 'transactions' && <Transactions role={role} />}
          {activePage === 'alerts' && <Alerts role={role} />}
          {activePage === 'sar-reports' && <SarReports role={role} />}
          {activePage === 'audit-log' && <AuditLog role={role} />}
          {activePage === 'rule-engine' && <RuleEngine />}
          {activePage === 'advanced-stats' && <AdvancedStats />}
        </div>
      </div>
    </div>
  );
}

export default App;
