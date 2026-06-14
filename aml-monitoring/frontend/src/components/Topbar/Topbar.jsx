import './Topbar.css';

function Topbar({ title, subtitle, showExportCsv, onExportCsv }) {
  return (
    <div className="topbar">
      <div className="topbar-left">
        <span className="topbar-title">{title}</span>
        <span className="topbar-subtitle">{subtitle}</span>
      </div>
      {showExportCsv && (
        <button className="topbar-export-btn" onClick={onExportCsv}>
          Export CSV
        </button>
      )}
    </div>
  );
}

export default Topbar;
