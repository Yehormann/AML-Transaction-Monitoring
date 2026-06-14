export const MOCK_STATS_ANALYST = {
  totalTransactions: 1284,
  openAlerts: 4,
  sarsFiled: 7,
  avgRiskScore: 32,
};

export const MOCK_STATS_ADMIN = {
  totalTransactions: 1284,
  totalSub: '+12 today',
  openAlerts: 4,
  openAlertsSub: '2 unassigned',
  sarsFiled: 7,
  sarsFiledSub: 'this month',
  overrideActions: 3,
  overrideActionsSub: 'by you',
};

export const MOCK_TRANSACTIONS = [
  { id: 'tx-0041', senderAccount: 'ACC-1192', receiverCountry: 'LU', amount: 14500, riskScore: 75, status: 'FLAGGED', firedRules: [{ name: 'LargeAmountRule', score: 40 }, { name: 'StructuringRule', score: 35 }] },
  { id: 'tx-0040', senderAccount: 'ACC-0033', receiverCountry: 'RU', amount: 9800, riskScore: 65, status: 'FLAGGED', firedRules: [{ name: 'HighRiskCountryRule', score: 35 }, { name: 'StructuringRule', score: 35 }] },
  { id: 'tx-0039', senderAccount: 'ACC-2210', receiverCountry: 'DE', amount: 1200, riskScore: 18, status: 'APPROVED', firedRules: [] },
  { id: 'tx-0038', senderAccount: 'ACC-7743', receiverCountry: 'IR', amount: 50000, riskScore: 100, status: 'FLAGGED', firedRules: [{ name: 'LargeAmountRule', score: 40 }, { name: 'HighRiskCountryRule', score: 35 }, { name: 'VelocityRule', score: 30 }] },
];

export const MOCK_ALERTS = [
  {
    id: 'ALT-0018', status: 'OPEN', riskScoreSnapshot: 75, analystNote: null,
    transaction: { id: 'tx-0041', senderAccount: 'ACC-1192', receiverCountry: 'LU', amount: 14500 },
    firedRules: [{ name: 'LargeAmountRule', score: 40 }, { name: 'StructuringRule', score: 35 }],
  },
  {
    id: 'ALT-0019', status: 'ESCALATED', riskScoreSnapshot: 100, analystNote: null,
    transaction: { id: 'tx-0038', senderAccount: 'ACC-7743', receiverCountry: 'IR', amount: 50000 },
    firedRules: [{ name: 'LargeAmountRule', score: 40 }, { name: 'HighRiskCountryRule', score: 35 }],
  },
  {
    id: 'ALT-0015', status: 'DISMISSED', riskScoreSnapshot: 45,
    analystNote: 'Known internal transfer, no suspicious activity.',
    transaction: { id: 'tx-0035', senderAccount: 'ACC-0091', receiverCountry: 'DE', amount: 9900 },
    firedRules: [{ name: 'StructuringRule', score: 35 }],
  },
];

export const MOCK_RULES = [
  { name: 'Large amount', threshold: '> \u20AC10,000', score: '+40 pts' },
  { name: 'High-risk country', threshold: 'IR, KP, RU, BY', score: '+35 pts' },
  { name: 'Structuring', threshold: '5 txs / 7 days', score: '+35 pts' },
  { name: 'Velocity', threshold: '20+ txs in 2h', score: '+30 pts' },
  { name: 'Dormant account', threshold: 'inactive 2yr+', score: '+25 pts' },
  { name: 'Round trip', threshold: 'round amounts', score: '+20 pts' },
];

export const MOCK_ADVANCED_STATS = [
  { label: 'SAR filing rate', value: '5.4%', sub: 'of all flagged alerts' },
  { label: 'Top risky account', value: 'ACC-7743', sub: 'score 100 \u00B7 3 alerts' },
  { label: 'Most triggered rule', value: 'LargeAmount', sub: 'fired 38 times' },
  { label: 'Overrides this month', value: '3', sub: '2 reopened \u00B7 1 reversed' },
];
