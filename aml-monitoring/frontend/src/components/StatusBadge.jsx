import './StatusBadge.css';

const STATUS_MAP = {
  APPROVED: { label: 'Approved', className: 'status-badge--approved' },
  FLAGGED: { label: 'Flagged', className: 'status-badge--flagged' },
  OPEN: { label: 'Open', className: 'status-badge--open' },
  ESCALATED: { label: 'Escalated', className: 'status-badge--escalated' },
  DISMISSED: { label: 'Dismissed', className: 'status-badge--dismissed' },
};

function StatusBadge({ status }) {
  const config = STATUS_MAP[status] || STATUS_MAP.DISMISSED;
  return (
    <span className={`status-badge ${config.className}`}>
      {config.label}
    </span>
  );
}

export default StatusBadge;
