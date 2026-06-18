import RuleEnginePanel from '../components/RuleEnginePanel/RuleEnginePanel';

const RULES = [
  { name: 'Large amount', threshold: '> \u20AC10,000', score: '+40 pts' },
  { name: 'High-risk country', threshold: 'IR, KP, RU, BY', score: '+35 pts' },
  { name: 'Structuring', threshold: '5 txs / 7 days', score: '+35 pts' },
  { name: 'Velocity', threshold: '20+ txs in 2h', score: '+30 pts' },
  { name: 'Dormant account', threshold: 'inactive 2yr+', score: '+25 pts' },
  { name: 'Round trip', threshold: 'round amounts', score: '+20 pts' },
];

function RuleEngine() {
  return (
    <div style={{ padding: 20 }}>
      <h2 style={{ fontSize: 15, fontWeight: 500, marginBottom: 16 }}>Rule Engine Configuration</h2>
      <p style={{ fontSize: 12, color: '#888', marginBottom: 16 }}>
        Read-only view of active fraud detection rules. Score threshold: &gt;40 = FLAGGED, &gt;75 = auto-escalate + SAR.
      </p>
      <RuleEnginePanel rules={RULES} />
    </div>
  );
}

export default RuleEngine;
