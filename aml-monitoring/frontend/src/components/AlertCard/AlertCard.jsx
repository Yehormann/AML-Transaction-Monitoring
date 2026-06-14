import StatusBadge from '../StatusBadge/StatusBadge';
import RuleTag from '../RuleTag/RuleTag';
import './AlertCard.css';

function getAccentClass(status) {
  if (status === 'OPEN') return 'alert-card-accent--open';
  if (status === 'ESCALATED') return 'alert-card-accent--escalated';
  return 'alert-card-accent--dismissed';
}

function formatAmount(amount) {
  return `\u20AC${amount.toLocaleString()}`;
}

function AlertCard({ alert, role, onEscalate, onDismiss, onOverrideReopen, onFileSar, inline }) {
  const { id, status, transaction, firedRules, analystNote } = alert;
  const isAnalyst = role === 'ANALYST';
  const isAdmin = role === 'ADMIN';

  return (
    <div className={`alert-card ${inline ? 'alert-card--inline' : ''}`}>
      <div className={`alert-card-accent ${getAccentClass(status)}`} />

      <div className="alert-card-header">
        <span className="alert-card-id">{id}</span>
        <StatusBadge status={status} />
      </div>

      <span className="alert-card-meta">
        {transaction.id} &middot; {transaction.senderAccount} &middot; {formatAmount(transaction.amount)} &middot; {transaction.receiverCountry}
      </span>

      <div className="alert-card-rules">
        {firedRules.map((rule) => (
          <RuleTag key={rule.name} name={rule.name} score={rule.score} />
        ))}
      </div>

      {isAnalyst && status === 'OPEN' && (
        <div className="alert-card-actions">
          <button className="alert-card-btn alert-card-btn--escalate" onClick={() => onEscalate(id)}>
            Escalate
          </button>
          <button className="alert-card-btn alert-card-btn--dismiss" onClick={() => onDismiss(id)}>
            Dismiss
          </button>
        </div>
      )}

      {isAnalyst && status === 'ESCALATED' && (
        <>
          <div className="alert-card-actions">
            <button className="alert-card-btn alert-card-btn--escalate alert-card-btn--disabled" disabled>
              Escalate
            </button>
            <button className="alert-card-btn alert-card-btn--dismiss alert-card-btn--disabled" disabled>
              Dismiss
            </button>
          </div>
          <span className="alert-card-escalated-note">Escalated &mdash; admin action required</span>
        </>
      )}

      {isAnalyst && status === 'DISMISSED' && analystNote && (
        <span className="alert-card-analyst-note">{analystNote}</span>
      )}

      {isAdmin && status === 'OPEN' && (
        <div className="alert-card-actions">
          <button className="alert-card-btn alert-card-btn--teal" onClick={() => onEscalate(id)}>
            Escalate
          </button>
          <button className="alert-card-btn alert-card-btn--teal" onClick={() => onDismiss(id)}>
            Dismiss
          </button>
          <button className="alert-card-btn alert-card-btn--teal" onClick={() => onFileSar(id)}>
            File SAR manually
          </button>
        </div>
      )}

      {isAdmin && status === 'ESCALATED' && (
        <div className="alert-card-actions">
          <button className="alert-card-btn alert-card-btn--teal" onClick={() => onOverrideReopen(id)}>
            Override &mdash; reopen
          </button>
          <button className="alert-card-btn alert-card-btn--teal" onClick={() => onFileSar(id)}>
            File SAR manually
          </button>
        </div>
      )}

      {isAdmin && status === 'DISMISSED' && (
        <div className="alert-card-actions">
          <button className="alert-card-btn alert-card-btn--teal" onClick={() => onOverrideReopen(id)}>
            Override &mdash; reopen
          </button>
        </div>
      )}
    </div>
  );
}

export default AlertCard;
