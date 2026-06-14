import { useState } from 'react';
import LoginModal from './components/LoginModal/LoginModal';
import Sidebar from './components/Sidebar/Sidebar';
import Topbar from './components/Topbar/Topbar';
import Dashboard from './pages/Dashboard';

function App() {
  const [role, setRole] = useState(null);
  const [activePage, setActivePage] = useState('dashboard');

  if (!role) {
    return <LoginModal onLogin={(r) => setRole(r)} />;
  }

  const subtitle = role === 'ADMIN' ? 'Administrator view' : 'Compliance analyst view';

  return (
    <div style={{ display: 'flex', flexDirection: 'row', height: '100vh', overflow: 'hidden' }}>
      <Sidebar role={role} activePage={activePage} onNavigate={setActivePage} alertCount={2} />
      <div style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
        <Topbar
          title="Dashboard"
          subtitle={subtitle}
          showExportCsv={role === 'ADMIN'}
          onExportCsv={() => window.alert('Exporting CSV...')}
        />
        <div style={{ flex: 1, overflowY: 'auto' }}>
          {activePage === 'dashboard' && <Dashboard role={role} />}
          {activePage === 'transactions' && <div style={{ padding: 20 }}>Transactions</div>}
          {activePage === 'alerts' && <div style={{ padding: 20 }}>Alerts</div>}
          {activePage === 'sar-reports' && <div style={{ padding: 20 }}>SAR Reports</div>}
          {activePage === 'audit-log' && <div style={{ padding: 20 }}>Audit Log</div>}
          {activePage === 'rule-engine' && <div style={{ padding: 20 }}>Rule Engine</div>}
          {activePage === 'advanced-stats' && <div style={{ padding: 20 }}>Advanced Stats</div>}
        </div>
      </div>
    </div>
  );
}

export default App;
