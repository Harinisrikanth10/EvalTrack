/* EvalTrack API & Authentication Client */

const API_BASE = '/api';

const Auth = {
  getToken() {
    return sessionStorage.getItem('evaltrack_token') || localStorage.getItem('evaltrack_token');
  },
  getUser() {
    const raw = sessionStorage.getItem('evaltrack_user') || localStorage.getItem('evaltrack_user');
    return raw ? JSON.parse(raw) : null;
  },
  setAuth(token, user, remember = true) {
    const storage = remember ? localStorage : sessionStorage;
    storage.setItem('evaltrack_token', token);
    storage.setItem('evaltrack_user', JSON.stringify(user));
  },
  clear() {
    sessionStorage.removeItem('evaltrack_token');
    sessionStorage.removeItem('evaltrack_user');
    localStorage.removeItem('evaltrack_token');
    localStorage.removeItem('evaltrack_user');
  },
  isAuthenticated() {
    return !!this.getToken();
  },
  getRole() {
    const user = this.getUser();
    return user ? user.role : null;
  }
};

async function apiFetch(endpoint, options = {}) {
  const token = Auth.getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Remove Content-Type if FormData is used
  if (options.body instanceof FormData) {
    delete headers['Content-Type'];
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers
  });

  if (response.status === 401) {
    Auth.clear();
    window.location.hash = '#login';
    throw new Error('Session expired. Please log in again.');
  }

  if (!response.ok) {
    let errorMsg = 'An error occurred';
    try {
      const errorData = await response.json();
      errorMsg = errorData.message || errorData.error || errorMsg;
    } catch (e) {
      errorMsg = await response.text();
    }
    throw new Error(errorMsg || `HTTP ${response.status}`);
  }

  // Handle empty responses
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerText = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 4000);
}
