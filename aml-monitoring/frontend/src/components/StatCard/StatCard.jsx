import './StatCard.css';

function StatCard({ label, value, valueColor, sub }) {
  return (
    <div className="stat-card">
      <span className="stat-card-label">{label}</span>
      <span className="stat-card-value" style={valueColor ? { color: valueColor } : undefined}>
        {value}
      </span>
      {sub && <span className="stat-card-sub">{sub}</span>}
    </div>
  );
}

export default StatCard;
