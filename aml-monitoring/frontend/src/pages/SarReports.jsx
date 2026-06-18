import { useState, useEffect } from 'react';
import { getSarReports, downloadSarPdf } from '../api/client';
import './SarReports.css';

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

function shortId(uuid) {
  if (!uuid) return '—';
  return uuid.length > 8 ? uuid.substring(0, 8) : uuid;
}

function SarReports({ role }) {
  const [sarReports, setSarReports] = useState([]);
  const [previewId, setPreviewId] = useState(null);

  useEffect(() => {
    getSarReports()
      .then(setSarReports)
      .catch(() => {});
  }, []);

  const isAdmin = role === 'ADMIN';

  const totalFiled = sarReports.length;
  const filedThisMonth = sarReports.filter((s) => {
    const d = new Date(s.filedAt);
    const now = new Date();
    return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
  }).length;

  async function handleDownload(id) {
    try {
      const res = await downloadSarPdf(id);
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'SAR-' + id + '.pdf';
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      window.alert('PDF download failed — report may not have a generated PDF yet.');
    }
  }

  const previewSar = previewId ? sarReports.find((s) => s.id === previewId) : null;

  return (
    <div className="sar-page">
      <div className="sar-header">
        <h2 className="sar-title">SAR Reports</h2>
        {isAdmin && (
          <span className="sar-summary-pill">{filedThisMonth} filed this month</span>
        )}
      </div>

      <div className="sar-stats">
        <div className="sar-stat-card">
          <span className="sar-stat-label">Total SARs filed</span>
          <span className="sar-stat-value">{totalFiled}</span>
        </div>
        <div className="sar-stat-card">
          <span className="sar-stat-label">Filed this month</span>
          <span className="sar-stat-value" style={{ color: 'var(--color-amber)' }}>{filedThisMonth}</span>
        </div>
      </div>

      {sarReports.length === 0 ? (
        <div style={{ padding: 24, color: '#888' }}>No SAR reports filed yet.</div>
      ) : (
        <div className="sar-table-wrap">
          <table className="sar-table">
            <thead>
              <tr>
                <th>SAR ID</th>
                <th>Alert ID</th>
                <th>Transaction</th>
                <th>Amount</th>
                <th>Filed date</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {sarReports.map((sar) => (
                <tr key={sar.id}>
                  <td className="sar-cell-mono">{shortId(sar.id)}</td>
                  <td className="sar-cell-mono">{shortId(sar.alertId)}</td>
                  <td className="sar-cell-mono">{shortId(sar.transactionId)}</td>
                  <td className="sar-cell-amount">{sar.currency === 'USD' ? '$' : '\u20AC'}{Number(sar.amount).toLocaleString()}</td>
                  <td className="sar-cell-date">{formatDate(sar.filedAt)}</td>
                  <td>
                    <div className="sar-action-cell">
                      <button className="sar-download-btn" onClick={() => handleDownload(sar.id)}>
                        Download PDF
                      </button>
                      {isAdmin && (
                        <button className="sar-preview-btn" onClick={() => setPreviewId(sar.id)}>
                          Preview
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {previewSar && (
        <>
          <div className="sar-overlay" onClick={() => setPreviewId(null)} />
          <div className="sar-panel">
            <div className="sar-panel__header">
              <h3>SAR Preview</h3>
              <button className="sar-panel__close" onClick={() => setPreviewId(null)}>&times;</button>
            </div>
            <div className="sar-panel__content">
              <div>
                <div className="sar-panel__section-title">Report details</div>
                <div className="sar-panel__detail-row">
                  <span className="sar-panel__detail-label">SAR ID</span>
                  <span className="sar-panel__detail-value">{previewSar.id}</span>
                </div>
                <div className="sar-panel__detail-row">
                  <span className="sar-panel__detail-label">Alert ID</span>
                  <span className="sar-panel__detail-value">{previewSar.alertId}</span>
                </div>
                <div className="sar-panel__detail-row">
                  <span className="sar-panel__detail-label">Transaction</span>
                  <span className="sar-panel__detail-value">{previewSar.transactionId}</span>
                </div>
                <div className="sar-panel__detail-row">
                  <span className="sar-panel__detail-label">Amount</span>
                  <span className="sar-panel__detail-value">{previewSar.currency === 'USD' ? '$' : '\u20AC'}{Number(previewSar.amount).toLocaleString()}</span>
                </div>
                <div className="sar-panel__detail-row">
                  <span className="sar-panel__detail-label">Filed date</span>
                  <span className="sar-panel__detail-value">{formatDate(previewSar.filedAt)}</span>
                </div>
                <div className="sar-panel__detail-row">
                  <span className="sar-panel__detail-label">Status</span>
                  <span className="sar-status-pill">Filed</span>
                </div>
              </div>

              <div>
                <div className="sar-panel__section-title">PDF document</div>
                <div className="sar-panel__pdf-box">
                  <div className="sar-panel__pdf-icon">PDF</div>
                  <span className="sar-panel__pdf-name">SAR-{shortId(previewSar.id)}.pdf</span>
                  <span className="sar-panel__pdf-note">Generated automatically on escalation</span>
                </div>
                <button className="sar-panel__pdf-download" onClick={() => handleDownload(previewSar.id)}>
                  Download PDF
                </button>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export default SarReports;
