import './Sidebar.css';

const COMMON_NAV = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'transactions', label: 'Transactions' },
  { key: 'alerts', label: 'Alerts', showBadge: true },
  { key: 'sar-reports', label: 'SAR reports' },
  { key: 'audit-log', label: 'Audit log' },
];

const ADMIN_NAV = [
  { key: 'rule-engine', label: 'Rule engine' },
  { key: 'advanced-stats', label: 'Advanced stats' },
];

function Sidebar({ role, activePage, onNavigate, alertCount }) {
  const isAdmin = role === 'ADMIN';
  const accentClass = isAdmin ? 'accent-teal' : 'accent-purple';

  return (
    <div className="sidebar">
      <div className="sidebar-logo">AML Monitor</div>
      <div className={`sidebar-role ${isAdmin ? 'sidebar-role--admin' : 'sidebar-role--analyst'}`}>
        {isAdmin ? 'Administrator' : 'Compliance analyst'}
      </div>

      <nav className="sidebar-nav">
        {COMMON_NAV.map((item) => (
          <button
            key={item.key}
            className={`sidebar-nav-item ${activePage === item.key ? `active ${accentClass}` : ''}`}
            onClick={() => onNavigate(item.key)}
          >
            <span className="sidebar-nav-label">{item.label}</span>
            {item.showBadge && alertCount > 0 && (
              <span className="sidebar-nav-badge">{alertCount}</span>
            )}
          </button>
        ))}

        {isAdmin && (
          <>
            <div className="sidebar-nav-section">Admin only</div>
            {ADMIN_NAV.map((item) => (
              <button
                key={item.key}
                className={`sidebar-nav-item sidebar-nav-item--teal ${activePage === item.key ? `active ${accentClass}` : ''}`}
                onClick={() => onNavigate(item.key)}
              >
                <span className="sidebar-nav-label">{item.label}</span>
              </button>
            ))}
          </>
        )}
      </nav>
    </div>
  );
}

export default Sidebar;
