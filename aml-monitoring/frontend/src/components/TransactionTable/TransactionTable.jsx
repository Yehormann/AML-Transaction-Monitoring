import StatusBadge from '../StatusBadge/StatusBadge';
import './TransactionTable.css';

function getRiskClass(score) {
  if (score >= 75) return 'risk-score--high';
  if (score >= 41) return 'risk-score--med';
  return 'risk-score--low';
}

function getRiskLabel(score) {
  if (score >= 75) return 'High';
  if (score >= 41) return 'Med';
  return 'Low';
}

function formatAmount(amount) {
  return `\u20AC${amount.toLocaleString()}`;
}

function TransactionTable({ transactions, limit }) {
  const rows = limit ? transactions.slice(0, limit) : transactions;

  return (
    <div className="transaction-table-wrapper">
      <table className="transaction-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Sender</th>
            <th>Amount</th>
            <th>Country</th>
            <th>Risk score</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((tx) => (
            <tr key={tx.id}>
              <td className="cell-mono">{tx.id}</td>
              <td className="cell-mono">{tx.senderAccount}</td>
              <td>{formatAmount(tx.amount)}</td>
              <td>{tx.receiverCountry}</td>
              <td className={getRiskClass(tx.riskScore)}>
                {tx.riskScore} &mdash; {getRiskLabel(tx.riskScore)}
              </td>
              <td><StatusBadge status={tx.status} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default TransactionTable;
