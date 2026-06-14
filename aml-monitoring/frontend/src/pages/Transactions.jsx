import { useState } from 'react';
import rawTransactions from '../../../backend/src/main/resources/data/transactions.json';
import TransactionTable from '../components/TransactionTable/TransactionTable';
import './Transactions.css';

function buildTransactions(raw) {
  return raw.map((tx, i) => ({
    id: 'tx-' + String(i + 1).padStart(4, '0'),
    senderAccount: tx.senderAccount,
    receiverCountry: tx.receiverCountry,
    amount: tx.amount,
    riskScore: 0,
    status: 'PENDING',
    firedRules: [],
    createdAt: tx.timestamp,
  }));
}

const EMPTY_FORM = {
  senderAccount: '',
  senderCountry: '',
  receiverAccount: '',
  receiverCountry: '',
  amount: '',
  currency: 'EUR',
  receiverLastActive: '',
  timestamp: '',
};

function getCurrentDatetime() {
  const now = new Date();
  const offset = now.getTimezoneOffset();
  const local = new Date(now.getTime() - offset * 60000);
  return local.toISOString().slice(0, 16);
}

function Transactions({ role }) {
  const [transactions, setTransactions] = useState(() => buildTransactions(rawTransactions));
  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({ ...EMPTY_FORM, timestamp: getCurrentDatetime() });
  const [submitSuccess, setSubmitSuccess] = useState(false);
  const [formError, setFormError] = useState(null);

  const isAdmin = role === 'ADMIN';
  const focusClass = isAdmin ? 'form-input--focus-teal' : 'form-input--focus-purple';

  function openModal() {
    setFormData({ ...EMPTY_FORM, timestamp: getCurrentDatetime() });
    setFormError(null);
    setSubmitSuccess(false);
    setModalOpen(true);
  }

  function closeModal() {
    setModalOpen(false);
  }

  function handleChange(field, value) {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (formError) setFormError(null);
  }

  function handleSubmit(e) {
    e.preventDefault();

    const { senderAccount, senderCountry, receiverAccount, receiverCountry, amount, receiverLastActive, timestamp } = formData;
    if (!senderAccount || !senderCountry || !receiverAccount || !receiverCountry || !amount || !receiverLastActive || !timestamp) {
      setFormError('All fields are required.');
      return;
    }

    const riskScore = Math.floor(Math.random() * 101);
    const newTx = {
      id: 'tx-' + Date.now(),
      senderAccount,
      receiverCountry,
      amount: parseFloat(amount),
      riskScore,
      status: riskScore > 40 ? 'FLAGGED' : 'APPROVED',
      firedRules: [],
      createdAt: new Date().toISOString(),
    };

    setSubmitSuccess(true);
    setTimeout(() => {
      setTransactions((prev) => [newTx, ...prev]);
      setSubmitSuccess(false);
      setModalOpen(false);
    }, 1500);
  }

  return (
    <div className="transactions-page">
      <div className="transactions-header">
        <span className="transactions-title">All transactions</span>
        <button
          className={`submit-btn ${isAdmin ? 'submit-btn--admin' : 'submit-btn--analyst'}`}
          onClick={openModal}
        >
          Submit transaction
        </button>
      </div>

      <TransactionTable transactions={transactions} />

      {modalOpen && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            {submitSuccess ? (
              <span className="form-success">Transaction submitted successfully!</span>
            ) : (
              <>
                <div className="modal-header">
                  <h2>Submit transaction</h2>
                  <button className="modal-close" onClick={closeModal}>&times;</button>
                </div>
                <span className="modal-subtitle">Enter transaction details below</span>
                <form className="modal-form" onSubmit={handleSubmit}>
                  <div className="form-row">
                    <div className="form-field">
                      <label className="form-label">Sender account</label>
                      <input
                        className={`form-input ${focusClass}`}
                        type="text"
                        placeholder="e.g. ACC-1192"
                        value={formData.senderAccount}
                        onChange={(e) => handleChange('senderAccount', e.target.value)}
                      />
                    </div>
                    <div className="form-field">
                      <label className="form-label">Sender country</label>
                      <input
                        className={`form-input ${focusClass}`}
                        type="text"
                        placeholder="e.g. LU"
                        value={formData.senderCountry}
                        onChange={(e) => handleChange('senderCountry', e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-field">
                      <label className="form-label">Receiver account</label>
                      <input
                        className={`form-input ${focusClass}`}
                        type="text"
                        placeholder="e.g. ACC-4487"
                        value={formData.receiverAccount}
                        onChange={(e) => handleChange('receiverAccount', e.target.value)}
                      />
                    </div>
                    <div className="form-field">
                      <label className="form-label">Receiver country</label>
                      <input
                        className={`form-input ${focusClass}`}
                        type="text"
                        placeholder="e.g. IR"
                        value={formData.receiverCountry}
                        onChange={(e) => handleChange('receiverCountry', e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="form-field">
                    <label className="form-label">Amount</label>
                    <input
                      className={`form-input ${focusClass}`}
                      type="number"
                      placeholder="e.g. 14500"
                      min="0"
                      step="0.01"
                      value={formData.amount}
                      onChange={(e) => handleChange('amount', e.target.value)}
                    />
                  </div>

                  <div className="form-row">
                    <div className="form-field">
                      <label className="form-label">Currency</label>
                      <input
                        className="form-input form-input--disabled"
                        type="text"
                        value="EUR"
                        disabled
                      />
                    </div>
                    <div className="form-field">
                      <label className="form-label">Receiver last active</label>
                      <input
                        className={`form-input ${focusClass}`}
                        type="date"
                        value={formData.receiverLastActive}
                        onChange={(e) => handleChange('receiverLastActive', e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="form-field">
                    <label className="form-label">Timestamp</label>
                    <input
                      className={`form-input ${focusClass}`}
                      type="datetime-local"
                      value={formData.timestamp}
                      onChange={(e) => handleChange('timestamp', e.target.value)}
                    />
                  </div>

                  {formError && <span className="form-error">{formError}</span>}

                  <button
                    type="submit"
                    className={`form-submit ${isAdmin ? 'form-submit--admin' : 'form-submit--analyst'}`}
                  >
                    Submit transaction
                  </button>
                </form>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default Transactions;
