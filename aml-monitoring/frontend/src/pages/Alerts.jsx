import { useState, useEffect } from 'react';
import { getAlerts, dismissAlert, escalateAlert } from '../api/client';
import StatusBadge from '../components/StatusBadge/StatusBadge';
import RuleTag from '../components/RuleTag/RuleTag';
import './Alerts.css';

const FILTERS = ['ALL', 'OPEN', 'DISMISSED', 'ESCALATED'];

function parseFiredRules(raw) {
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try { return JSON.parse(raw); } catch { return []; }
  }
  return [];
}

function mapAlert(a) {
  return {
    ...a,
    firedRules: parseFiredRules(a.firedRules).map((r) => ({ name: r.ruleName || r.name, score: r.score })),
  };
}

function formatAmount(amount) {
  return '\u20AC' + Number(amount).toLocaleString();
}

function getAccentClass(status) {
  if (status === 'OPEN') return 'alert-card__accent--open';
  if (status === 'ESCALATED') return 'alert-card__accent--escalated';
  return 'alert-card__accent--dismissed';
}

function Alerts({ role }) {
  const [alerts, setAlerts] = useState([]);
  const [activeFilter, setActiveFilter] = useState('ALL');
  const [dismissTarget, setDismissTarget] = useState(null);
  const [dismissNote, setDismissNote] = useState('');
  const [dismissError, setDismissError] = useState(null);

  useEffect(() => {
    getAlerts()
      .then((data) => setAlerts(data.map(mapAlert)))
      .catch(() => {});
  }, []);

  const isAdmin = role === 'ADMIN';
  const activeTabClass = isAdmin ? 'filter-tab--active-admin' : 'filter-tab--active-analyst';
  const focusClass = isAdmin ? 'dismiss-textarea--focus-teal' : 'dismiss-textarea--focus-purple';

  const filtered = activeFilter === 'ALL'
    ? alerts
    : alerts.filter((a) => a.status === activeFilter);

  function getCount(filter) {
    if (filter === 'ALL') return alerts.length;
    return alerts.filter((a) => a.status === filter).length;
  }

  async function handleEscalate(id) {
    const msg = isAdmin
      ? `Escalate ${id} as administrator?`
      : `Escalate ${id}? This will trigger SAR generation.`;
    if (!window.confirm(msg)) return;
    try {
      const updated = await escalateAlert(id);
      setAlerts((prev) => prev.map((a) => (a.id === id ? mapAlert(updated) : a)));
    } catch (err) {
      window.alert('Escalate failed: ' + err.message);
    }
  }

  function handleDismissOpen(id) {
    setDismissTarget(id);
    setDismissNote('');
    setDismissError(null);
  }

  async function handleDismissConfirm() {
    if (!dismissNote.trim()) {
      setDismissError('Note is required before dismissing');
      return;
    }
    try {
      const updated = await dismissAlert(dismissTarget, dismissNote);
      setAlerts((prev) => prev.map((a) => (a.id === dismissTarget ? mapAlert(updated) : a)));
      setDismissTarget(null);
      setDismissNote('');
    } catch (err) {
      setDismissError('Dismiss failed: ' + err.message);
    }
  }

  function handleDismissCancel() {
    setDismissTarget(null);
    setDismissNote('');
    setDismissError(null);
  }

  function handleOverrideReopen() {
    window.alert('Override/reopen is not supported by the backend yet.');
  }

  function handleFileSar() {
    window.alert('SAR is auto-filed on escalation by the backend.');
  }

  return (
    <div className="alerts-page">
      <h2 className="alerts-title">Alerts</h2>

      <div className="alerts-filters">
        {FILTERS.map((f) => (
          <button
            key={f}
            className={`filter-tab ${activeFilter === f ? activeTabClass : ''}`}
            onClick={() => setActiveFilter(f)}
          >
            {f.charAt(0) + f.slice(1).toLowerCase()} ({getCount(f)})
          </button>
        ))}
      </div>

      <div className="alerts-grid">
        {filtered.length === 0 && (
          <span className="alerts-empty">No alerts in this category</span>
        )}

        {filtered.map((alert) => {
          const tx = alert.transaction || {};
          return (
            <div className="alert-card" key={alert.id}>
              <div className={`alert-card__accent ${getAccentClass(alert.status)}`} />

              <div className="alert-card__header">
                <span className="alert-card__id">{alert.id}</span>
                <StatusBadge status={alert.status} />
              </div>

              <span className="alert-card__meta">
                {tx.id ? tx.id.substring(0, 8) : '—'} &middot; {tx.senderAccount || '—'} &middot; {formatAmount(tx.amount || 0)} &middot; {tx.receiverCountry || '—'}
              </span>

              <span className="alert-card__score">Score: {alert.riskScoreSnapshot}</span>

              <div className="alert-card__rules">
                {alert.firedRules.map((rule, i) => (
                  <RuleTag key={rule.name + i} name={rule.name} score={rule.score} />
                ))}
              </div>

              {/* ANALYST + OPEN */}
              {!isAdmin && alert.status === 'OPEN' && (
                <div className="alert-card__actions">
                  <button
                    className="alert-card__action-btn alert-card__action-btn--analyst-escalate"
                    onClick={() => handleEscalate(alert.id)}
                  >
                    Escalate
                  </button>
                  <button
                    className="alert-card__action-btn alert-card__action-btn--analyst-dismiss"
                    onClick={() => handleDismissOpen(alert.id)}
                  >
                    Dismiss
                  </button>
                </div>
              )}

              {/* ANALYST + ESCALATED */}
              {!isAdmin && alert.status === 'ESCALATED' && (
                <>
                  <div className="alert-card__actions">
                    <button className="alert-card__action-btn alert-card__action-btn--analyst-escalate alert-card__action-btn--disabled" disabled>
                      Escalate
                    </button>
                    <button className="alert-card__action-btn alert-card__action-btn--analyst-dismiss alert-card__action-btn--disabled" disabled>
                      Dismiss
                    </button>
                  </div>
                  <span className="alert-card__escalated-note">Escalated — admin action required</span>
                </>
              )}

              {/* ANALYST + DISMISSED */}
              {!isAdmin && alert.status === 'DISMISSED' && alert.analystNote && (
                <span className="alert-card__analyst-note">{alert.analystNote}</span>
              )}

              {/* ADMIN + OPEN */}
              {isAdmin && alert.status === 'OPEN' && (
                <div className="alert-card__actions">
                  <button
                    className="alert-card__action-btn alert-card__action-btn--admin"
                    onClick={() => handleEscalate(alert.id)}
                  >
                    Escalate
                  </button>
                  <button
                    className="alert-card__action-btn alert-card__action-btn--admin"
                    onClick={() => handleDismissOpen(alert.id)}
                  >
                    Dismiss
                  </button>
                  <button
                    className="alert-card__action-btn alert-card__action-btn--admin"
                    onClick={() => handleFileSar(alert.id)}
                  >
                    File SAR manually
                  </button>
                </div>
              )}

              {/* ADMIN + ESCALATED */}
              {isAdmin && alert.status === 'ESCALATED' && (
                <div className="alert-card__actions">
                  <button
                    className="alert-card__action-btn alert-card__action-btn--admin"
                    onClick={() => handleOverrideReopen(alert.id)}
                  >
                    Override — reopen
                  </button>
                  <button
                    className="alert-card__action-btn alert-card__action-btn--admin"
                    onClick={() => handleFileSar(alert.id)}
                  >
                    File SAR manually
                  </button>
                </div>
              )}

              {/* ADMIN + DISMISSED */}
              {isAdmin && alert.status === 'DISMISSED' && (
                <>
                  <div className="alert-card__actions">
                    <button
                      className="alert-card__action-btn alert-card__action-btn--admin"
                      onClick={() => handleOverrideReopen(alert.id)}
                    >
                      Override — reopen
                    </button>
                  </div>
                  {alert.analystNote && (
                    <span className="alert-card__analyst-note">{alert.analystNote}</span>
                  )}
                </>
              )}
            </div>
          );
        })}
      </div>

      {dismissTarget && (
        <div className="dismiss-overlay" onClick={handleDismissCancel}>
          <div className="dismiss-card" onClick={(e) => e.stopPropagation()}>
            <div className="dismiss-header">
              <h3>Dismiss alert</h3>
              <button className="dismiss-close" onClick={handleDismissCancel}>&times;</button>
            </div>
            <span className="dismiss-subtitle">
              Add an investigation note explaining why this alert is being dismissed. This note will be recorded in the audit log.
            </span>
            <textarea
              className={`dismiss-textarea ${focusClass}`}
              placeholder="Describe your investigation findings..."
              value={dismissNote}
              onChange={(e) => {
                setDismissNote(e.target.value);
                if (dismissError) setDismissError(null);
              }}
            />
            {dismissError && <span className="dismiss-error">{dismissError}</span>}
            <div className="dismiss-buttons">
              <button className="dismiss-btn-cancel" onClick={handleDismissCancel}>Cancel</button>
              <button
                className={`dismiss-btn-confirm ${isAdmin ? 'dismiss-btn-confirm--admin' : 'dismiss-btn-confirm--analyst'}`}
                onClick={handleDismissConfirm}
              >
                Confirm dismiss
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Alerts;
