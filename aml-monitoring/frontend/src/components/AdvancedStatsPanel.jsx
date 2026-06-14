import './AdvancedStatsPanel.css';

function AdvancedStatsPanel({ stats }) {
  return (
    <div className="advanced-stats-wrapper">
      {stats.map((item) => (
        <div className="advanced-stats-cell" key={item.label}>
          <span className="advanced-stats-label">{item.label}</span>
          <span className="advanced-stats-value">{item.value}</span>
          <span className="advanced-stats-sub">{item.sub}</span>
        </div>
      ))}
    </div>
  );
}

export default AdvancedStatsPanel;
