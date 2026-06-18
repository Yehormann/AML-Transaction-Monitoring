import { useState, useEffect } from 'react';
import { getDashboardStats, getAlerts } from '../api/client';
import AdvancedStatsPanel from '../components/AdvancedStatsPanel/AdvancedStatsPanel';

function AdvancedStats() {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    Promise.all([getDashboardStats(), getAlerts()])
      .then(([dashboard, alerts]) => {
        const totalFlagged = dashboard.flaggedTransactions || 0;
        const sarsFiled = dashboard.totalSarsFiled || 0;
        const sarRate = totalFlagged > 0
          ? ((sarsFiled / totalFlagged) * 100).toFixed(1) + '%'
          : '0%';

        const escalated = alerts.filter((a) => a.status === 'ESCALATED').length;
        const dismissed = alerts.filter((a) => a.status === 'DISMISSED').length;

        setStats([
          { label: 'SAR filing rate', value: sarRate, sub: 'of all flagged transactions' },
          { label: 'Total alerts', value: String(alerts.length), sub: `${escalated} escalated \u00B7 ${dismissed} dismissed` },
          { label: 'Flagged transactions', value: String(totalFlagged), sub: `${dashboard.flaggedPercentage.toFixed(1)}% of total` },
          { label: 'SARs filed', value: String(sarsFiled), sub: 'auto + manual' },
        ]);
      })
      .catch(() => {});
  }, []);

  return (
    <div style={{ padding: 20 }}>
      <h2 style={{ fontSize: 15, fontWeight: 500, marginBottom: 16 }}>Advanced Statistics</h2>
      {stats ? (
        <AdvancedStatsPanel stats={stats} />
      ) : (
        <div style={{ color: '#888', fontSize: 12 }}>Loading...</div>
      )}
    </div>
  );
}

export default AdvancedStats;
