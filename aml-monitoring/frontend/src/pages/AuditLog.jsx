import { useState } from 'react';
import { MOCK_AUDIT_LOG } from '../mockData';
import './AuditLog.css';

const ACTION_MAP = {
  ALERT_CREATED:   { className: 'audit-action-badge--created',   label: 'Alert created' },
  ALERT_DISMISSED: { className: 'audit-action-badge--dismissed', label: 'Alert dismissed' },
  ALERT_ESCALATED: { className: 'audit-action-badge--escalated', label: 'Alert escalated' },
  SAR_FILED:       { className: 'audit-action-badge--filed',     label: 'SAR filed' },
};

const FILTERS = [
  { key: 'ALL',              label: 'All' },
  { key: 'ALERT_CREATED',    label: 'Alert created' },
  { key: 'ALERT_DISMISSED',  label: 'Alert dismissed' },
  { key: 'ALERT_ESCALATED',  label: 'Alert escalated' },
  { key: 'SAR_FILED',        label: 'SAR filed' },
];

function formatDate(isoString) {
  const d = new Date(isoString);
  const day = String(d.getDate()).padStart(2, '0');
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  const month = months[d.getMonth()];
  const year = d.getFullYear();
  const hours = String(d.getHours()).padStart(2, '0');
  const mins = String(d.getMinutes()).padStart(2, '0');
  return day + ' ' + month + ' ' + year + ' ' + hours + ':' + mins;
}

function AuditLog({ role }) {
  const [auditLog] = useState([...MOCK_AUDIT_LOG]);
  const [actionFilter, setActionFilter] = useState('ALL');
  const [expandedId, setExpandedId] = useState(null);

  const isAdmin = role === 'ADMIN';
  const activeClass = isAdmin ? 'audit-filter-btn--active-admin' : 'audit-filter-btn--active-analyst';

  const filtered = actionFilter === 'ALL'
    ? auditLog
    : auditLog.filter((e) => e.action === actionFilter);

  const countCreated   = auditLog.filter((e) => e.action === 'ALERT_CREATED').length;
  const countEscalated = auditLog.filter((e) => e.action === 'ALERT_ESCALATED').length;
  const countFiled     = auditLog.filter((e) => e.action === 'SAR_FILED').length;

  const colCount = isAdmin ? 6 : 5;

  function toggleRow(id) {
    setExpandedId((prev) => (prev === id ? null : id));
  }

  function getPerformedByClass(performedBy) {
    if (performedBy === 'admin') return 'audit-performed-by--admin';
    if (performedBy === 'analyst') return 'audit-cell-performed-by--analyst';
    return 'audit-cell-performed-by--system';
  }

  return (
    <div className="audit-page">
      <div className="audit-header">
        <h2 className="audit-title">Audit Log</h2>
        <span className="audit-readonly-note">Read only — immutable record</span>
      </div>

      <div className="audit-stats">
        <div className="audit-stat-card">
          <span className="audit-stat-label">Total entries</span>
          <span className="audit-stat-value">{auditLog.length}</span>
        </div>
        <div className="audit-stat-card">
          <span className="audit-stat-label">Alerts created</span>
          <span className="audit-stat-value" style={{ color: 'var(--color-amber)' }}>{countCreated}</span>
        </div>
        <div className="audit-stat-card">
          <span className="audit-stat-label">Escalations</span>
          <span className="audit-stat-value" style={{ color: 'var(--color-red)' }}>{countEscalated}</span>
        </div>
        <div className="audit-stat-card">
          <span className="audit-stat-label">SARs filed</span>
          <span className="audit-stat-value" style={{ color: 'var(--color-purple)' }}>{countFiled}</span>
        </div>
      </div>

      <div className="audit-filters">
        {FILTERS.map((f) => (
          <button
            key={f.key}
            className={`audit-filter-btn ${actionFilter === f.key ? activeClass : ''}`}
            onClick={() => setActionFilter(f.key)}
          >
            {f.label}
          </button>
        ))}
      </div>

      <div className="audit-table-wrap">
        <table className="audit-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Action</th>
              <th>Entity type</th>
              <th>Entity ID</th>
              {isAdmin && <th>Performed by</th>}
              <th>Note</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 && (
              <tr>
                <td colSpan={colCount} className="audit-empty">
                  No audit entries for this filter
                </td>
              </tr>
            )}
            {filtered.map((entry) => {
              const isExpanded = expandedId === entry.id;
              const actionConfig = ACTION_MAP[entry.action];
              return (
                <tr
                  key={entry.id}
                  className={`audit-row ${isExpanded ? 'audit-row--expanded' : ''}`}
                  onClick={() => toggleRow(entry.id)}
                >
                  <td className="audit-cell-timestamp">{formatDate(entry.timestamp)}</td>
                  <td>
                    <span className={`audit-action-badge ${actionConfig.className}`}>
                      {actionConfig.label}
                    </span>
                  </td>
                  <td className="audit-cell-entity-type">{entry.entityType}</td>
                  <td className="audit-cell-entity-id">{entry.entityId}</td>
                  {isAdmin && (
                    <td className={getPerformedByClass(entry.performedBy)}>
                      {entry.performedBy}
                    </td>
                  )}
                  <td>
                    {isExpanded ? (
                      <div>
                        <div className="audit-note-full">{entry.note}</div>
                        <button
                          className="audit-collapse-link"
                          onClick={(e) => {
                            e.stopPropagation();
                            setExpandedId(null);
                          }}
                        >
                          collapse &uarr;
                        </button>
                      </div>
                    ) : (
                      <div className="audit-note-short">
                        {entry.note.length > 60 ? entry.note.slice(0, 60) + '...' : entry.note}
                      </div>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AuditLog;
