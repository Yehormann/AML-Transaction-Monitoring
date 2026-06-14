import './RuleEnginePanel.css';

function RuleEnginePanel({ rules }) {
  return (
    <div className="rule-engine-card">
      <div className="rule-engine-header">
        Active rules &middot; {rules.length} total
      </div>
      {rules.map((rule) => (
        <div className="rule-engine-row" key={rule.name}>
          <span className="rule-engine-name">{rule.name}</span>
          <span className="rule-engine-threshold">{rule.threshold}</span>
          <span className="rule-engine-score">{rule.score}</span>
        </div>
      ))}
    </div>
  );
}

export default RuleEnginePanel;
