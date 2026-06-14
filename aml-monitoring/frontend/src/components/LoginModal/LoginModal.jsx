import { useState } from 'react';
import './LoginModal.css';

function LoginModal({ onLogin }) {
  const [selectedRole, setSelectedRole] = useState('ANALYST');

  return (
    <div className="login-overlay">
      <div className="login-card">
        <div className="login-header">
          <h2>Log in</h2>
          <p>Select your role and enter password to continue</p>
        </div>

        <div className="login-field">
          <label>Role</label>
          <select value={selectedRole} onChange={(e) => setSelectedRole(e.target.value)}>
            <option value="ANALYST">Compliance analyst</option>
            <option value="ADMIN">Compliance administrator</option>
          </select>
        </div>

        <div className="login-field">
          <label>Password</label>
          {/* Password is mock — not validated, not sent anywhere */}
          <input type="password" placeholder="Enter password" />
        </div>

        <button className="login-submit" onClick={() => onLogin(selectedRole)}>
          Log in
        </button>
      </div>
    </div>
  );
}

export default LoginModal;
