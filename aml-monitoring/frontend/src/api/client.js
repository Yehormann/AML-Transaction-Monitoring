let _credentials = null;

export function setCredentials(username, password) {
  _credentials = btoa(username + ':' + password);
}

export function clearCredentials() {
  _credentials = null;
}

async function request(path, options = {}) {
  const headers = { ...options.headers };
  if (_credentials) {
    headers['Authorization'] = 'Basic ' + _credentials;
  }
  if (options.body && !(options.body instanceof Blob)) {
    headers['Content-Type'] = 'application/json';
  }

  const res = await fetch(path, { ...options, headers });

  if (res.status === 401) {
    throw new Error('Unauthorized');
  }
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || res.statusText);
  }
  if (res.status === 204 || res.headers.get('content-length') === '0') {
    return null;
  }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) {
    return res.json();
  }
  return res;
}

// --- Transactions ---
export function getTransactions() {
  return request('/api/transactions');
}

export function submitTransaction(data) {
  return request('/api/transactions', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// --- Alerts ---
export function getAlerts(status) {
  const q = status ? '?status=' + status : '';
  return request('/api/alerts' + q);
}

export function dismissAlert(id, note) {
  return request('/api/alerts/' + id + '/dismiss', {
    method: 'PATCH',
    body: JSON.stringify({ note }),
  });
}

export function escalateAlert(id) {
  return request('/api/alerts/' + id + '/escalate', {
    method: 'PATCH',
  });
}

// --- Dashboard ---
export function getDashboardStats() {
  return request('/api/dashboard/stats');
}

// --- SAR Reports ---
export function getSarReports() {
  return request('/api/reports/sar');
}

export function downloadSarPdf(id) {
  return request('/api/reports/sar/' + id);
}

// --- Audit Log ---
export function getAuditLog() {
  return request('/api/audit-log');
}
