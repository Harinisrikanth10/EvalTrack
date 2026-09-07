/* EvalTrack Main Application Controller */

document.addEventListener('DOMContentLoaded', () => {
  Router.init({
    'login': renderLoginView,
    'register': renderRegisterView,

    // Student Routes
    'student/dashboard': renderStudentDashboard,
    'student/exams': renderStudentExams,
    'student/exams/:id': renderStudentExamTake,
    'student/results': renderStudentResults,
    'student/assignments': renderStudentAssignments,
    'student/profile': renderStudentProfile,

    // Teacher Routes
    'teacher/dashboard': renderTeacherDashboard,
    'teacher/exams': renderTeacherExams,
    'teacher/exams/:id/attempts': renderTeacherExamAttempts,
    'teacher/attempts/:id': renderTeacherAttemptDetail,
    'teacher/assignments': renderTeacherAssignments,

    // Admin Routes
    'admin/dashboard': renderAdminDashboard,
    'admin/students': renderAdminStudents,
    'admin/teachers': renderAdminTeachers,
    'admin/subjects': renderAdminSubjects,
    'admin/attempts': renderAdminFlaggedAttempts
  });

  setupGlobalEvents();
});

function setupGlobalEvents() {
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
      e.preventDefault();
      Proctoring.stopProctoring();
      Auth.clear();
      showToast('Logged out successfully', 'info');
      Router.navigate('login');
    });
  }
}

function updateLayoutUI() {
  const authWrapper = document.getElementById('auth-wrapper');
  const appContainer = document.getElementById('app-container');
  const user = Auth.getUser();

  if (!Auth.isAuthenticated()) {
    authWrapper.classList.remove('hidden');
    appContainer.classList.add('hidden');
  } else {
    authWrapper.classList.add('hidden');
    appContainer.classList.remove('hidden');

    // Update Nav & User Badge
    document.getElementById('nav-user-name').innerText = user.name || user.email;
    document.getElementById('nav-user-role').innerText = user.role;
    document.getElementById('nav-user-avatar').innerText = (user.name || user.email).charAt(0).toUpperCase();

    // Render Role Sidebar
    renderSidebar(user.role);
  }
}

function renderSidebar(role) {
  const sidebarMenu = document.getElementById('sidebar-menu');
  let html = '';

  if (role === 'STUDENT') {
    html = `
      <li class="sidebar-item ${window.location.hash.includes('dashboard') ? 'active' : ''}"><a href="#student/dashboard">📊 Dashboard</a></li>
      <li class="sidebar-item ${window.location.hash.includes('exams') ? 'active' : ''}"><a href="#student/exams">📝 Examinations</a></li>
      <li class="sidebar-item ${window.location.hash.includes('results') ? 'active' : ''}"><a href="#student/results">🏆 My Results</a></li>
      <li class="sidebar-item ${window.location.hash.includes('assignments') ? 'active' : ''}"><a href="#student/assignments">📚 Assignments</a></li>
      <li class="sidebar-item ${window.location.hash.includes('profile') ? 'active' : ''}"><a href="#student/profile">👤 My Profile</a></li>
    `;
  } else if (role === 'TEACHER') {
    html = `
      <li class="sidebar-item ${window.location.hash.includes('dashboard') ? 'active' : ''}"><a href="#teacher/dashboard">📊 Dashboard</a></li>
      <li class="sidebar-item ${window.location.hash.includes('exams') ? 'active' : ''}"><a href="#teacher/exams">📝 Exam Management</a></li>
      <li class="sidebar-item ${window.location.hash.includes('assignments') ? 'active' : ''}"><a href="#teacher/assignments">📚 Weekly Assignments</a></li>
    `;
  } else if (role === 'ADMIN') {
    html = `
      <li class="sidebar-item ${window.location.hash.includes('dashboard') ? 'active' : ''}"><a href="#admin/dashboard">📊 Dashboard</a></li>
      <li class="sidebar-item ${window.location.hash.includes('students') ? 'active' : ''}"><a href="#admin/students">👨‍🎓 Manage Students</a></li>
      <li class="sidebar-item ${window.location.hash.includes('teachers') ? 'active' : ''}"><a href="#admin/teachers">👩‍🏫 Manage Teachers</a></li>
      <li class="sidebar-item ${window.location.hash.includes('subjects') ? 'active' : ''}"><a href="#admin/subjects">📖 Subjects & Courses</a></li>
      <li class="sidebar-item ${window.location.hash.includes('attempts') ? 'active' : ''}"><a href="#admin/attempts">🚨 Flagged Attempts</a></li>
    `;
  }
  sidebarMenu.innerHTML = html;
}

/* ==========================================================================
   AUTH VIEWS
   ========================================================================== */
function renderLoginView() {
  updateLayoutUI();
  const main = document.getElementById('auth-card-body');
  main.innerHTML = `
    <h2 style="font-size: 22px; font-weight: 700; margin-bottom: 8px;">Welcome Back</h2>
    <p style="color: var(--text-muted); font-size: 14px; margin-bottom: 24px;">Sign in to your EvalTrack account</p>
    
    <form id="login-form">
      <div class="form-group">
        <label>Email Address</label>
        <input type="email" id="login-email" class="form-control" placeholder="name@college.edu" required />
      </div>
      <div class="form-group">
        <label>Password</label>
        <input type="password" id="login-password" class="form-control" placeholder="••••••••" required />
      </div>
      <button type="submit" class="btn btn-primary" style="width: 100%;">Sign In</button>
    </form>
    
    <div style="margin-top: 24px; text-align: center; font-size: 14px; color: var(--text-muted);">
      Are you a student? <a href="#register" style="color: var(--primary); font-weight: 600; text-decoration: none;">Register account</a>
    </div>
  `;

  document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
      const res = await apiFetch('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });
      Auth.setAuth(res.token, res.profile);
      showToast('Welcome back, ' + (res.profile.name || 'User'), 'success');

      if (res.role === 'STUDENT') Router.navigate('student/dashboard');
      else if (res.role === 'TEACHER') Router.navigate('teacher/dashboard');
      else if (res.role === 'ADMIN') Router.navigate('admin/dashboard');
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

function renderRegisterView() {
  updateLayoutUI();
  const main = document.getElementById('auth-card-body');
  main.innerHTML = `
    <h2 style="font-size: 22px; font-weight: 700; margin-bottom: 8px;">Student Registration</h2>
    <p style="color: var(--text-muted); font-size: 14px; margin-bottom: 24px;">Create your student account</p>

    <form id="register-form">
      <div class="form-group">
        <label>Full Name</label>
        <input type="text" id="reg-name" class="form-control" placeholder="Alice Johnson" required />
      </div>
      <div class="form-group">
        <label>Roll Number</label>
        <input type="text" id="reg-roll" class="form-control" placeholder="CS2026-042" required />
      </div>
      <div class="form-group">
        <label>College Email</label>
        <input type="email" id="reg-email" class="form-control" placeholder="alice@college.edu" required />
      </div>
      <div class="form-group">
        <label>Department</label>
        <input type="text" id="reg-dept" class="form-control" placeholder="Computer Science" required />
      </div>
      <div class="form-group">
        <label>Age</label>
        <input type="number" id="reg-age" class="form-control" placeholder="20" required />
      </div>
      <div class="form-group">
        <label>Password</label>
        <input type="password" id="reg-password" class="form-control" placeholder="••••••••" required />
      </div>
      <button type="submit" class="btn btn-primary" style="width: 100%;">Create Account</button>
    </form>

    <div style="margin-top: 24px; text-align: center; font-size: 14px; color: var(--text-muted);">
      Already have an account? <a href="#login" style="color: var(--primary); font-weight: 600; text-decoration: none;">Sign in</a>
    </div>
  `;

  document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('reg-name').value;
    const rollNumber = document.getElementById('reg-roll').value;
    const email = document.getElementById('reg-email').value;
    const department = document.getElementById('reg-dept').value;
    const age = parseInt(document.getElementById('reg-age').value);
    const password = document.getElementById('reg-password').value;

    try {
      const res = await apiFetch('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ name, rollNumber, email, department, age, password })
      });
      Auth.setAuth(res.token, res.profile);
      showToast('Registration successful!', 'success');
      Router.navigate('student/dashboard');
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

/* ==========================================================================
   STUDENT VIEWS
   ========================================================================== */
async function renderStudentDashboard() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">Student Dashboard</h1>
    <p class="page-subtitle">Overview of your enrolled courses, upcoming assignments, and proctored exams.</p>
    
    <div class="grid-3">
      <div class="stat-card">
        <div class="stat-icon">📖</div>
        <div>
          <div class="stat-number" id="dash-exams-count">-</div>
          <div class="stat-label">Available Exams</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📚</div>
        <div>
          <div class="stat-number" id="dash-assign-count">-</div>
          <div class="stat-label">Weekly Assignments</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏆</div>
        <div>
          <div class="stat-number" id="dash-results-count">-</div>
          <div class="stat-label">Completed Attempts</div>
        </div>
      </div>
    </div>

    <div class="grid-2">
      <!-- This Week's Assignments Widget -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">⏳ This Week's Assignments (Due within 7 Days)</h2>
          <a href="#student/assignments" class="btn btn-sm btn-secondary">View All</a>
        </div>
        <div id="upcoming-assignments-list">Loading upcoming assignments...</div>
      </div>

      <!-- Available Examinations Widget -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">📝 Available Examinations</h2>
          <a href="#student/exams" class="btn btn-sm btn-secondary">View All</a>
        </div>
        <div id="upcoming-exams-list">Loading examinations...</div>
      </div>
    </div>
  `;

  try {
    const [exams, assignments, upcomingAssignments, results] = await Promise.all([
      apiFetch('/students/me/exams'),
      apiFetch('/students/me/assignments'),
      apiFetch('/students/me/assignments/upcoming'),
      apiFetch('/students/me/results')
    ]);

    document.getElementById('dash-exams-count').innerText = exams.length;
    document.getElementById('dash-assign-count').innerText = assignments.length;
    document.getElementById('dash-results-count').innerText = results.length;

    // Render Upcoming Assignments
    const assignContainer = document.getElementById('upcoming-assignments-list');
    if (upcomingAssignments.length === 0) {
      assignContainer.innerHTML = `<p style="color: var(--text-muted); font-size: 14px;">No assignments due in the next 7 days 🎉</p>`;
    } else {
      assignContainer.innerHTML = upcomingAssignments.map(a => {
        const due = new Date(a.dueAt);
        const isLate = new Date() > due;
        const statusBadge = a.studentSubmissionStatus === 'GRADED'
          ? `<span class="badge badge-success">Graded (${a.studentGrade})</span>`
          : a.studentSubmissionStatus === 'SUBMITTED' || a.studentSubmissionStatus === 'LATE'
          ? `<span class="badge badge-info">${a.studentSubmissionStatus}</span>`
          : `<span class="badge ${isLate ? 'badge-danger' : 'badge-warning'}">${isLate ? 'OVERDUE' : 'DUE SOON'}</span>`;

        return `
          <div style="padding: 14px; border: 1px solid var(--border-color); border-radius: var(--radius-sm); margin-bottom: 10px; display: flex; align-items: center; justify-content: space-between;">
            <div>
              <div style="font-weight: 600; font-size: 15px;">${a.title}</div>
              <div style="font-size: 13px; color: var(--text-muted);">${a.subjectName} • Week ${a.weekNumber}</div>
              <div style="font-size: 12px; color: var(--danger); font-weight: 500; margin-top: 4px;">Due: ${due.toLocaleString()}</div>
            </div>
            <div>${statusBadge}</div>
          </div>
        `;
      }).join('');
    }

    // Render Upcoming Exams
    const examContainer = document.getElementById('upcoming-exams-list');
    if (exams.length === 0) {
      examContainer.innerHTML = `<p style="color: var(--text-muted); font-size: 14px;">No proctored exams currently scheduled.</p>`;
    } else {
      examContainer.innerHTML = exams.map(e => `
        <div style="padding: 14px; border: 1px solid var(--border-color); border-radius: var(--radius-sm); margin-bottom: 10px; display: flex; align-items: center; justify-content: space-between;">
          <div>
            <div style="font-weight: 600; font-size: 15px;">${e.title}</div>
            <div style="font-size: 13px; color: var(--text-muted);">${e.subjectName} • ${e.durationMinutes} Mins • ${e.totalQuestions} Questions</div>
            ${e.requiresCamera ? '<span class="badge badge-info" style="margin-top: 4px;">📷 Webcam Required</span>' : ''}
          </div>
          <a href="#student/exams/${e.id}" class="btn btn-sm btn-primary">Start Exam</a>
        </div>
      `).join('');
    }

  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function renderStudentExams() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">Proctored Examinations</h1>
    <p class="page-subtitle">Select an exam to begin. Ensure your webcam is connected and working before starting.</p>
    <div class="card">
      <div id="student-exams-table-box">Loading examinations...</div>
    </div>
  `;

  try {
    const exams = await apiFetch('/students/me/exams');
    const box = document.getElementById('student-exams-table-box');

    if (exams.length === 0) {
      box.innerHTML = `<p style="color: var(--text-muted);">No exams available for your enrolled subjects.</p>`;
      return;
    }

    box.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Exam Title</th>
              <th>Subject</th>
              <th>Duration</th>
              <th>Questions</th>
              <th>Proctoring</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${exams.map(e => `
              <tr>
                <td style="font-weight: 600;">${e.title}</td>
                <td>${e.subjectName}</td>
                <td>${e.durationMinutes} Minutes</td>
                <td>${e.totalQuestions} MCQs</td>
                <td>${e.requiresCamera ? '<span class="badge badge-info">📷 Camera Required</span>' : '<span class="badge badge-secondary">Standard</span>'}</td>
                <td><a href="#student/exams/${e.id}" class="btn btn-sm btn-primary">Start Exam</a></td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

/* --------------------------------------------------------------------------
   EXAM TAKING WORKFLOW WITH CAMERA PRE-CHECK & PROCTORING
   -------------------------------------------------------------------------- */
async function renderStudentExamTake(params) {
  updateLayoutUI();
  const examId = params.id;
  const body = document.getElementById('page-body');

  body.innerHTML = `
    <div class="exam-container" id="exam-flow-container">
      <div class="card">
        <h2 style="font-size: 24px; font-weight: 700; margin-bottom: 12px;">Webcam Proctoring & Consent Agreement</h2>
        <p style="color: var(--text-muted); font-size: 14px; margin-bottom: 20px;">
          This examination requires continuous webcam proctoring and browser integrity monitoring.
        </p>

        <div style="background: #EFF6FF; border: 1px solid #BFDBFE; padding: 20px; border-radius: var(--radius-md); margin-bottom: 24px;">
          <h4 style="color: var(--primary); margin-bottom: 10px;">📋 Proctored Exam Rules:</h4>
          <ul style="font-size: 14px; color: var(--text-main); margin-left: 20px; line-height: 1.6;">
            <li>Periodic webcam snapshots will be recorded every 15 seconds.</li>
            <li>Tab switching or exiting browser window will log a <strong>TAB_BLUR</strong> integrity warning.</li>
            <li>Exiting fullscreen mode will log a <strong>FULLSCREEN_EXIT</strong> warning.</li>
            <li>3 or more warning/critical events will automatically flag your attempt for instructor review.</li>
            <li>Your camera feed must remain unobstructed with your face clearly visible throughout.</li>
          </ul>
        </div>

        <div style="margin-bottom: 24px;">
          <label style="display: flex; align-items: center; gap: 12px; font-size: 15px; font-weight: 600; cursor: pointer;">
            <input type="checkbox" id="proctor-consent-checkbox" style="width: 20px; height: 20px; accent-color: var(--primary);" />
            I agree to camera proctoring and browser integrity monitoring for academic integrity purposes.
          </label>
        </div>

        <div id="camera-precheck-box" class="hidden" style="margin-bottom: 24px;">
          <h4 style="margin-bottom: 12px;">📷 Camera Readiness Check</h4>
          <div class="camera-preview-box" style="width: 320px; height: 240px; margin-bottom: 12px;">
            <video id="precheck-video" autoplay playsinline muted></video>
          </div>
          <p style="font-size: 13px; color: var(--text-muted);" id="precheck-status">Requesting camera access...</p>
        </div>

        <div style="display: flex; gap: 16px;">
          <button id="btn-agree-consent" class="btn btn-secondary" disabled>I Agree & Enable Camera</button>
          <button id="btn-start-quiz" class="btn btn-success hidden">Enter Fullscreen & Start Exam</button>
        </div>
      </div>
    </div>
  `;

  const consentCheckbox = document.getElementById('proctor-consent-checkbox');
  const btnAgree = document.getElementById('btn-agree-consent');
  const btnStartQuiz = document.getElementById('btn-start-quiz');
  const precheckBox = document.getElementById('camera-precheck-box');
  const precheckVideo = document.getElementById('precheck-video');
  const precheckStatus = document.getElementById('precheck-status');

  consentCheckbox.addEventListener('change', () => {
    btnAgree.disabled = !consentCheckbox.checked;
    if (consentCheckbox.checked) {
      btnAgree.classList.remove('btn-secondary');
      btnAgree.classList.add('btn-primary');
    } else {
      btnAgree.classList.remove('btn-primary');
      btnAgree.classList.add('btn-secondary');
    }
  });

  btnAgree.addEventListener('click', async () => {
    precheckBox.classList.remove('hidden');
    try {
      await Proctoring.requestCamera();
      Proctoring.attachPreview(precheckVideo);
      precheckStatus.innerText = '✅ Camera active and ready! Click below to enter fullscreen and start.';
      precheckStatus.style.color = 'var(--success)';
      btnAgree.classList.add('hidden');
      btnStartQuiz.classList.remove('hidden');
    } catch (err) {
      precheckStatus.innerText = '❌ Camera Access Failed: ' + err.message;
      precheckStatus.style.color = 'var(--danger)';
    }
  });

  btnStartQuiz.addEventListener('click', async () => {
    try {
      // 1. Enter Fullscreen
      if (document.documentElement.requestFullscreen) {
        await document.documentElement.requestFullscreen().catch(() => {});
      }

      // 2. Start Attempt API Call
      const attempt = await apiFetch(`/exams/${examId}/attempts`, { method: 'POST' });
      const questions = await apiFetch(`/exams/${examId}/questions`);

      if (!questions || questions.length === 0) {
        showToast('This examination has no published questions yet. Please contact your instructor.', 'warning');
        Router.navigate('student/exams');
        return;
      }

      // 3. Render Quiz Engine
      renderActiveQuiz(attempt, questions);
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

function renderActiveQuiz(attempt, questions) {
  const container = document.getElementById('exam-flow-container');
  let currentQuestionIndex = 0;
  const userAnswers = {}; // questionId -> chosenOption (1-4)

  container.innerHTML = `
    <div class="warning-banner hidden" id="fullscreen-warning-banner">
      ⚠️ WARNING: You have exited fullscreen mode! Return to fullscreen immediately.
    </div>
    <div class="warning-banner hidden" id="camera-warning-banner"></div>

    <div class="proctoring-bar">
      <div>
        <div style="font-size: 12px; text-transform: uppercase; color: #94A3B8;">Proctored Attempt</div>
        <div style="font-weight: 700; font-size: 16px;">${attempt.exam.title}</div>
      </div>
      
      <div style="display: flex; align-items: center; gap: 20px;">
        <div class="camera-preview-box">
          <video id="exam-camera-video" autoplay playsinline muted></video>
        </div>
        <div>
          <div style="font-size: 12px; color: #94A3B8;">Time Remaining</div>
          <div style="font-size: 20px; font-weight: 800; color: #38BDF8;" id="quiz-timer">00:00</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px; color: var(--text-muted); font-size: 14px;">
        <span>Question <strong id="q-curr-num">1</strong> of ${questions.length}</span>
        <span class="badge badge-info">1 Point Each</span>
      </div>

      <h3 style="font-size: 18px; font-weight: 600; margin-bottom: 24px; line-height: 1.5;" id="q-text-box"></h3>

      <div id="q-options-box"></div>

      <div style="display: flex; justify-content: space-between; margin-top: 32px; padding-top: 20px; border-top: 1px solid var(--border-color);">
        <button id="btn-prev-q" class="btn btn-secondary" disabled>Previous</button>
        <button id="btn-next-q" class="btn btn-primary">Next Question</button>
        <button id="btn-submit-exam" class="btn btn-success hidden">Finish & Submit Exam</button>
      </div>
    </div>
  `;

  const examVideo = document.getElementById('exam-camera-video');
  Proctoring.startProctoring(attempt.id, examVideo);

  // Timer logic
  let durationSeconds = attempt.exam.durationMinutes * 60;
  const timerDisplay = document.getElementById('quiz-timer');
  const timerInterval = setInterval(() => {
    durationSeconds--;
    const mins = Math.floor(durationSeconds / 60);
    const secs = durationSeconds % 60;
    timerDisplay.innerText = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;

    if (durationSeconds <= 0) {
      clearInterval(timerInterval);
      showToast('Time expired! Submitting your exam...', 'warning');
      submitExamAction();
    }
  }, 1000);

  function loadQuestion(index) {
    currentQuestionIndex = index;
    const q = questions[index];

    document.getElementById('q-curr-num').innerText = index + 1;
    document.getElementById('q-text-box').innerText = q.text;

    const optionsBox = document.getElementById('q-options-box');
    const selectedOpt = userAnswers[q.id] || -1;

    optionsBox.innerHTML = [1, 2, 3, 4].map(optNum => {
      const optText = q[`option${optNum}`];
      if (!optText) return '';
      const isSelected = selectedOpt === optNum;

      return `
        <label class="quiz-option-label ${isSelected ? 'selected' : ''}">
          <input type="radio" name="quiz_opt" value="${optNum}" ${isSelected ? 'checked' : ''} />
          <span><strong>Option ${optNum}:</strong> ${optText}</span>
        </label>
      `;
    }).join('');

    // Attach radio select handler
    optionsBox.querySelectorAll('input[name="quiz_opt"]').forEach(radio => {
      radio.addEventListener('change', (e) => {
        userAnswers[q.id] = parseInt(e.target.value);
        loadQuestion(index);
      });
    });

    // Button states
    document.getElementById('btn-prev-q').disabled = index === 0;
    if (index === questions.length - 1) {
      document.getElementById('btn-next-q').classList.add('hidden');
      document.getElementById('btn-submit-exam').classList.remove('hidden');
    } else {
      document.getElementById('btn-next-q').classList.remove('hidden');
      document.getElementById('btn-submit-exam').classList.add('hidden');
    }
  }

  document.getElementById('btn-prev-q').addEventListener('click', () => {
    if (currentQuestionIndex > 0) loadQuestion(currentQuestionIndex - 1);
  });

  document.getElementById('btn-next-q').addEventListener('click', () => {
    if (currentQuestionIndex < questions.length - 1) loadQuestion(currentQuestionIndex + 1);
  });

  document.getElementById('btn-submit-exam').addEventListener('click', () => {
    if (confirm('Are you sure you want to submit your examination?')) {
      submitExamAction();
    }
  });

  window.autoSubmitExam = () => submitExamAction();

  async function submitExamAction() {
    clearInterval(timerInterval);
    Proctoring.stopProctoring();

    try {
      const result = await apiFetch(`/attempts/${attempt.id}/submit`, {
        method: 'POST',
        body: JSON.stringify({ answers: userAnswers })
      });

      renderExamResult(result);
    } catch (err) {
      showToast(err.message, 'danger');
    }
  }

  loadQuestion(0);
}

function renderExamResult(result) {
  const container = document.getElementById('exam-flow-container');
  const isPassed = result.percentage >= 40.0;
  const gradeBadgeClass = result.grade === 'A+' || result.grade === 'A' ? 'badge-success' : (result.grade === 'B' || result.grade === 'C' ? 'badge-info' : 'badge-danger');

  container.innerHTML = `
    <div class="card" style="text-align: center; padding: 48px;">
      <div style="font-size: 64px; margin-bottom: 16px;">${isPassed ? '🎉' : '⚠️'}</div>
      <h2 style="font-size: 28px; font-weight: 800; margin-bottom: 8px;">${result.examTitle}</h2>
      <p style="color: var(--text-muted); margin-bottom: 32px;">Subject: ${result.subjectName}</p>

      <div style="display: flex; justify-content: center; gap: 40px; margin-bottom: 32px;">
        <div>
          <div style="font-size: 14px; color: var(--text-muted);">Score Percentage</div>
          <div style="font-size: 36px; font-weight: 800; color: var(--primary);">${result.percentage.toFixed(1)}%</div>
        </div>
        <div>
          <div style="font-size: 14px; color: var(--text-muted);">Assigned Grade</div>
          <div style="font-size: 36px; font-weight: 800;"><span class="badge ${gradeBadgeClass}" style="font-size: 24px; padding: 8px 20px;">${result.grade}</span></div>
        </div>
        <div>
          <div style="font-size: 14px; color: var(--text-muted);">Correct Answers</div>
          <div style="font-size: 36px; font-weight: 800; color: var(--text-main);">${result.correctAnswers} / ${result.totalQuestions}</div>
        </div>
      </div>

      <div style="margin-bottom: 32px;">
        <span class="badge ${isPassed ? 'badge-success' : 'badge-danger'}" style="font-size: 14px; padding: 10px 24px;">
          ${isPassed ? 'PASSED (≥ 40%)' : 'FAILED (< 40%)'}
        </span>
        ${result.status === 'FLAGGED' ? '<br/><span class="badge badge-warning" style="margin-top: 12px;">🚨 Flagged for Proctoring Review</span>' : ''}
      </div>

      <a href="#student/results" class="btn btn-primary">View All Exam Results</a>
    </div>
  `;
}

async function renderStudentResults() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">My Exam Results</h1>
    <p class="page-subtitle">Historical performance and proctored examination scores.</p>
    <div class="card">
      <div id="student-results-table-box">Loading exam results...</div>
    </div>
  `;

  try {
    const results = await apiFetch('/students/me/results');
    const box = document.getElementById('student-results-table-box');

    if (results.length === 0) {
      box.innerHTML = `<p style="color: var(--text-muted);">You have not completed any exam attempts yet.</p>`;
      return;
    }

    box.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Exam Title</th>
              <th>Subject</th>
              <th>Submitted At</th>
              <th>Score</th>
              <th>Percentage</th>
              <th>Grade</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            ${results.map(r => `
              <tr>
                <td style="font-weight: 600;">${r.examTitle}</td>
                <td>${r.subjectName}</td>
                <td>${new Date(r.submittedAt).toLocaleString()}</td>
                <td>${r.correctAnswers} / ${r.totalQuestions}</td>
                <td style="font-weight: 700; color: var(--primary);">${r.percentage.toFixed(1)}%</td>
                <td><span class="badge ${r.grade === 'A+' || r.grade === 'A' ? 'badge-success' : (r.grade === 'Fail' ? 'badge-danger' : 'badge-info')}">${r.grade}</span></td>
                <td><span class="badge ${r.status === 'FLAGGED' ? 'badge-warning' : 'badge-success'}">${r.status}</span></td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function renderStudentAssignments() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">Weekly Coursework Assignments</h1>
    <p class="page-subtitle">Submit your weekly homework, lab reports, and assignment tasks.</p>
    <div class="card">
      <div id="student-assignments-list">Loading assignments...</div>
    </div>
  `;

  try {
    const assignments = await apiFetch('/students/me/assignments');
    const list = document.getElementById('student-assignments-list');

    if (assignments.length === 0) {
      list.innerHTML = `<p style="color: var(--text-muted);">No assignments posted for your subjects.</p>`;
      return;
    }

    list.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Week</th>
              <th>Title</th>
              <th>Subject</th>
              <th>Due Date</th>
              <th>Status</th>
              <th>Grade & Feedback</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${assignments.map(a => {
              const due = new Date(a.dueAt);
              const isOverdue = new Date() > due;
              const statusBadge = a.studentSubmissionStatus === 'GRADED'
                ? `<span class="badge badge-success">GRADED</span>`
                : a.studentSubmissionStatus === 'SUBMITTED' || a.studentSubmissionStatus === 'LATE'
                ? `<span class="badge badge-info">${a.studentSubmissionStatus}</span>`
                : `<span class="badge ${isOverdue ? 'badge-danger' : 'badge-warning'}">${isOverdue ? 'OVERDUE' : 'PENDING'}</span>`;

              return `
                <tr>
                  <td><strong>Week ${a.weekNumber || 1}</strong></td>
                  <td style="font-weight: 600;">${a.title}</td>
                  <td>${a.subjectName}</td>
                  <td>${due.toLocaleString()}</td>
                  <td>${statusBadge}</td>
                  <td>${a.studentGrade ? `<strong>${a.studentGrade}</strong><br/><small style="color:var(--text-muted);">${a.studentFeedback || ''}</small>` : '-'}</td>
                  <td>
                    <button class="btn btn-sm btn-primary" onclick="openStudentSubmitModal('${a.id}', '${escapeHtml(a.title)}', '${escapeHtml(a.description || '')}')">
                      ${a.studentSubmissionStatus === 'PENDING' ? 'Submit' : 'View / Resubmit'}
                    </button>
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

window.openStudentSubmitModal = function(id, title, description) {
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal-card">
      <div class="modal-header">
        <h3 class="modal-title">Submit Assignment: ${title}</h3>
        <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
      </div>
      <p style="font-size: 14px; color: var(--text-muted); margin-bottom: 20px;">${description}</p>
      
      <form id="submit-assignment-form">
        <div class="form-group">
          <label>Written Submission / Solution Text</label>
          <textarea id="sub-content" class="form-control" placeholder="Type your full submission text here..."></textarea>
        </div>
        <div class="form-group">
          <label>Attach File / Work Document</label>
          <input type="file" id="sub-file" class="form-control" />
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%;">Submit Work</button>
      </form>
    </div>
  `;
  document.body.appendChild(modal);

  document.getElementById('submit-assignment-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const contentText = document.getElementById('sub-content').value;
    const fileInput = document.getElementById('sub-file');
    let fileUrl = null;

    try {
      if (fileInput.files.length > 0) {
        const formData = new FormData();
        formData.append('file', fileInput.files[0]);
        const uploadRes = await apiFetch('/files/upload', {
          method: 'POST',
          body: formData
        });
        fileUrl = uploadRes.url;
      }

      await apiFetch(`/assignments/${id}/submit`, {
        method: 'POST',
        body: JSON.stringify({ contentText, fileUrl })
      });

      showToast('Assignment submitted successfully!', 'success');
      modal.remove();
      renderStudentAssignments();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
};

async function renderStudentProfile() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">My Profile</h1>
    <p class="page-subtitle">Manage your personal information and credentials.</p>
    
    <div class="grid-2">
      <div class="card">
        <h3 class="card-title" style="margin-bottom: 20px;">Student Details</h3>
        <form id="profile-update-form">
          <div class="form-group">
            <label>Roll Number (Read-only)</label>
            <input type="text" id="prof-roll" class="form-control" disabled />
          </div>
          <div class="form-group">
            <label>Email Address (Read-only)</label>
            <input type="email" id="prof-email" class="form-control" disabled />
          </div>
          <div class="form-group">
            <label>Full Name</label>
            <input type="text" id="prof-name" class="form-control" required />
          </div>
          <div class="form-group">
            <label>Department</label>
            <input type="text" id="prof-dept" class="form-control" required />
          </div>
          <div class="form-group">
            <label>Age</label>
            <input type="number" id="prof-age" class="form-control" required />
          </div>
          <button type="submit" class="btn btn-primary">Save Profile Changes</button>
        </form>
      </div>

      <div class="card">
        <h3 class="card-title" style="margin-bottom: 20px;">Security & Password</h3>
        <form id="password-change-form">
          <div class="form-group">
            <label>Current Password</label>
            <input type="password" id="pass-old" class="form-control" required />
          </div>
          <div class="form-group">
            <label>New Password</label>
            <input type="password" id="pass-new" class="form-control" required />
          </div>
          <button type="submit" class="btn btn-secondary">Update Password</button>
        </form>
      </div>
    </div>
  `;

  try {
    const student = await apiFetch('/students/me');
    document.getElementById('prof-roll').value = student.rollNumber;
    document.getElementById('prof-email').value = student.email;
    document.getElementById('prof-name').value = student.name;
    document.getElementById('prof-dept').value = student.department || '';
    document.getElementById('prof-age').value = student.age || '';

    document.getElementById('profile-update-form').addEventListener('submit', async (e) => {
      e.preventDefault();
      try {
        await apiFetch('/students/me', {
          method: 'PUT',
          body: JSON.stringify({
            name: document.getElementById('prof-name').value,
            department: document.getElementById('prof-dept').value,
            age: parseInt(document.getElementById('prof-age').value)
          })
        });
        showToast('Profile updated successfully!', 'success');
      } catch (err) {
        showToast(err.message, 'danger');
      }
    });

    document.getElementById('password-change-form').addEventListener('submit', async (e) => {
      e.preventDefault();
      try {
        await apiFetch('/students/me/change-password', {
          method: 'POST',
          body: JSON.stringify({
            oldPassword: document.getElementById('pass-old').value,
            newPassword: document.getElementById('pass-new').value
          })
        });
        showToast('Password changed successfully!', 'success');
        document.getElementById('password-change-form').reset();
      } catch (err) {
        showToast(err.message, 'danger');
      }
    });
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

/* ==========================================================================
   TEACHER VIEWS
   ========================================================================== */
async function renderTeacherDashboard() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">Teacher Dashboard</h1>
    <p class="page-subtitle">Manage exams, weekly assignments, and proctoring reviews for your assigned subjects.</p>
    
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">📖 Assigned Subjects</h2>
      </div>
      <div id="teacher-subjects-list">Loading subjects...</div>
    </div>
  `;

  try {
    const subjects = await apiFetch('/teacher/subjects');
    const box = document.getElementById('teacher-subjects-list');

    if (subjects.length === 0) {
      box.innerHTML = `<p style="color: var(--text-muted);">You have not been assigned to any subjects yet.</p>`;
      return;
    }

    box.innerHTML = `
      <div class="grid-3">
        ${subjects.map(s => `
          <div style="border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 20px; background: white;">
            <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 6px;">${s.name}</h3>
            <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 16px;">Department: ${s.department || 'General'}</p>
            <div style="font-size: 14px; font-weight: 600; color: var(--primary); margin-bottom: 16px;">👨‍🎓 Enrolled Students: ${s.enrolledCount}</div>
            <div style="display: flex; gap: 8px;">
              <a href="#teacher/exams?subjectId=${s.id}" class="btn btn-sm btn-primary">Exams</a>
              <a href="#teacher/assignments?subjectId=${s.id}" class="btn btn-sm btn-secondary">Assignments</a>
            </div>
          </div>
        `).join('')}
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function renderTeacherExams() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  const urlParams = new URLSearchParams(window.location.hash.split('?')[1] || '');
  const subjectId = urlParams.get('subjectId');

  body.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <div>
        <h1 class="page-title">Exam Management</h1>
        <p class="page-subtitle">Create and manage proctored examinations and MCQ question banks.</p>
      </div>
      <button class="btn btn-primary" id="btn-open-create-exam">+ Create New Exam</button>
    </div>
    
    <div class="card">
      <div id="teacher-exams-list">Select or loading exams...</div>
    </div>
  `;

  try {
    const subjects = await apiFetch('/teacher/subjects');
    if (subjects.length === 0) {
      document.getElementById('teacher-exams-list').innerHTML = `<p style="color: var(--text-muted);">No subjects assigned.</p>`;
      return;
    }

    const activeSubId = subjectId || subjects[0].id;
    const exams = await apiFetch(`/teacher/exams?subjectId=${activeSubId}`);
    const box = document.getElementById('teacher-exams-list');

    box.innerHTML = `
      <div style="margin-bottom: 20px;">
        <label style="font-weight: 600; font-size: 14px; margin-right: 12px;">Filter Subject:</label>
        <select class="form-control" style="width: auto; display: inline-block;" onchange="window.location.hash='#teacher/exams?subjectId=' + this.value">
          ${subjects.map(s => `<option value="${s.id}" ${s.id === activeSubId ? 'selected' : ''}>${s.name}</option>`).join('')}
        </select>
      </div>

      ${exams.length === 0 ? '<p style="color: var(--text-muted);">No exams created for this subject yet.</p>' : `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Duration</th>
                <th>Questions</th>
                <th>Camera Proctoring</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              ${exams.map(e => `
                <tr>
                  <td style="font-weight: 600;">${e.title}</td>
                  <td>${e.durationMinutes} Mins</td>
                  <td>${e.totalQuestions} MCQs</td>
                  <td>${e.requiresCamera ? '<span class="badge badge-info">Required</span>' : '<span class="badge badge-secondary">Disabled</span>'}</td>
                  <td>
                    <a href="#teacher/exams/${e.id}/attempts" class="btn btn-sm btn-primary">View Attempts</a>
                    <button class="btn btn-sm btn-danger" onclick="deleteExam('${e.id}')">Delete</button>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    `;

    document.getElementById('btn-open-create-exam').addEventListener('click', () => {
      openCreateExamModal(subjects, activeSubId);
    });
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

window.deleteExam = async function(id) {
  if (confirm('Are you sure you want to delete this exam?')) {
    try {
      await apiFetch(`/teacher/exams/${id}`, { method: 'DELETE' });
      showToast('Exam deleted', 'info');
      renderTeacherExams();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  }
};

function openCreateExamModal(subjects, defaultSubjectId) {
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal-card" style="max-width: 750px;">
      <div class="modal-header">
        <h3 class="modal-title">Create Proctored Examination</h3>
        <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
      </div>
      
      <form id="create-exam-form">
        <div class="form-group">
          <label>Subject</label>
          <select id="exam-subject-id" class="form-control">
            ${subjects.map(s => `<option value="${s.id}" ${s.id === defaultSubjectId ? 'selected' : ''}>${s.name}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label>Exam Title</label>
          <input type="text" id="exam-title" class="form-control" placeholder="Midterm OS Exam 2026" required />
        </div>
        <div class="grid-2">
          <div class="form-group">
            <label>Duration (Minutes)</label>
            <input type="number" id="exam-duration" class="form-control" value="30" required />
          </div>
          <div class="form-group" style="display: flex; align-items: center; margin-top: 28px;">
            <label style="display: flex; align-items: center; gap: 10px; cursor: pointer;">
              <input type="checkbox" id="exam-req-cam" checked style="width: 18px; height: 18px;" />
              Mandatory Webcam Proctoring
            </label>
          </div>
        </div>

        <hr style="margin: 20px 0; border: none; border-top: 1px solid var(--border-color);" />
        <h4 style="margin-bottom: 14px;">Questions (MCQs)</h4>
        <div id="questions-builder-box"></div>
        <button type="button" class="btn btn-sm btn-secondary" id="btn-add-q-builder" style="margin-bottom: 20px;">+ Add Question</button>

        <button type="submit" class="btn btn-primary" style="width: 100%;">Create Exam & Save Questions</button>
      </form>
    </div>
  `;
  document.body.appendChild(modal);

  const builderBox = document.getElementById('questions-builder-box');
  let qCount = 0;

  function addQuestionFields() {
    qCount++;
    const qDiv = document.createElement('div');
    qDiv.className = 'card';
    qDiv.style.background = '#F8FAFC';
    qDiv.innerHTML = `
      <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
        <strong>Question #${qCount}</strong>
        <button type="button" style="color: var(--danger); background: none; border: none; cursor: pointer;" onclick="this.closest('.card').remove()">&times; Remove</button>
      </div>
      <div class="form-group">
        <input type="text" class="form-control q-text" placeholder="Enter question text..." required />
      </div>
      <div class="grid-2">
        <div class="form-group"><input type="text" class="form-control q-opt1" placeholder="Option 1" required /></div>
        <div class="form-group"><input type="text" class="form-control q-opt2" placeholder="Option 2" required /></div>
        <div class="form-group"><input type="text" class="form-control q-opt3" placeholder="Option 3" required /></div>
        <div class="form-group"><input type="text" class="form-control q-opt4" placeholder="Option 4" required /></div>
      </div>
      <div class="form-group">
        <label>Correct Option (1-4)</label>
        <select class="form-control q-correct">
          <option value="1">Option 1</option>
          <option value="2">Option 2</option>
          <option value="3">Option 3</option>
          <option value="4">Option 4</option>
        </select>
      </div>
    `;
    builderBox.appendChild(qDiv);
  }

  addQuestionFields(); // default 1 question
  document.getElementById('btn-add-q-builder').addEventListener('click', addQuestionFields);

  document.getElementById('create-exam-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const subjectId = document.getElementById('exam-subject-id').value;
    const title = document.getElementById('exam-title').value;
    const durationMinutes = parseInt(document.getElementById('exam-duration').value);
    const requiresCamera = document.getElementById('exam-req-cam').checked;

    const questions = [];
    builderBox.querySelectorAll('.card').forEach(qCard => {
      questions.push({
        text: qCard.querySelector('.q-text').value,
        option1: qCard.querySelector('.q-opt1').value,
        option2: qCard.querySelector('.q-opt2').value,
        option3: qCard.querySelector('.q-opt3').value,
        option4: qCard.querySelector('.q-opt4').value,
        correctOption: parseInt(qCard.querySelector('.q-correct').value)
      });
    });

    try {
      await apiFetch('/teacher/exams', {
        method: 'POST',
        body: JSON.stringify({ subjectId, title, durationMinutes, requiresCamera, questions })
      });
      showToast('Exam created successfully!', 'success');
      modal.remove();
      renderTeacherExams();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

async function renderTeacherExamAttempts(params) {
  updateLayoutUI();
  const examId = params.id;
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">Student Exam Attempts</h1>
    <p class="page-subtitle">Review scores, grades, and proctoring integrity flags.</p>
    <div class="card">
      <div id="teacher-attempts-list">Loading attempts...</div>
    </div>
  `;

  try {
    const attempts = await apiFetch(`/teacher/exams/${examId}/attempts`);
    const box = document.getElementById('teacher-attempts-list');

    if (attempts.length === 0) {
      box.innerHTML = `<p style="color: var(--text-muted);">No students have attempted this exam yet.</p>`;
      return;
    }

    box.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Student Name</th>
              <th>Roll Number</th>
              <th>Started At</th>
              <th>Submitted At</th>
              <th>Score</th>
              <th>Percentage</th>
              <th>Grade</th>
              <th>Status</th>
              <th>Proctoring Timeline</th>
            </tr>
          </thead>
          <tbody>
            ${attempts.map(a => `
              <tr style="${a.status === 'FLAGGED' ? 'background: #FEF2F2;' : ''}">
                <td style="font-weight: 600;">${a.studentName}</td>
                <td>${a.studentRollNumber}</td>
                <td>${new Date(a.startedAt).toLocaleString()}</td>
                <td>${a.submittedAt ? new Date(a.submittedAt).toLocaleString() : 'In Progress'}</td>
                <td>${a.correctAnswers || 0} / ${a.totalQuestions || 0}</td>
                <td style="font-weight: 700;">${a.percentage != null ? a.percentage.toFixed(1) + '%' : '-'}</td>
                <td><span class="badge ${a.grade === 'Fail' ? 'badge-danger' : 'badge-success'}">${a.grade || '-'}</span></td>
                <td><span class="badge ${a.status === 'FLAGGED' ? 'badge-warning' : 'badge-info'}">${a.status}</span></td>
                <td><a href="#teacher/attempts/${a.attemptId}" class="btn btn-sm btn-secondary">Inspect Timeline</a></td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function renderTeacherAttemptDetail(params) {
  updateLayoutUI();
  const attemptId = params.id;
  const body = document.getElementById('page-body');

  body.innerHTML = `
    <h1 class="page-title">Proctoring Timeline & Attempt Review</h1>
    <p class="page-subtitle">Inspect captured snapshots, integrity events, and submit instructor decision.</p>
    <div id="attempt-detail-content">Loading attempt details...</div>
  `;

  try {
    const detail = await apiFetch(`/teacher/attempts/${attemptId}`);
    const box = document.getElementById('attempt-detail-content');

    box.innerHTML = `
      <div class="grid-2">
        <!-- Student & Score Summary -->
        <div class="card">
          <h3 class="card-title" style="margin-bottom: 16px;">Attempt Summary</h3>
          <div style="font-size: 14px; line-height: 1.8;">
            <div><strong>Student:</strong> ${detail.studentName} (${detail.studentRollNumber})</div>
            <div><strong>Exam:</strong> ${detail.examTitle} (${detail.subjectName})</div>
            <div><strong>Score:</strong> ${detail.correctAnswers || 0} / ${detail.totalQuestions || 0} (${detail.percentage != null ? detail.percentage.toFixed(1) + '%' : '-'})</div>
            <div><strong>Assigned Grade:</strong> <span class="badge badge-info">${detail.grade || '-'}</span></div>
            <div><strong>Current Status:</strong> <span class="badge ${detail.status === 'FLAGGED' ? 'badge-warning' : 'badge-success'}">${detail.status}</span></div>
          </div>

          <hr style="margin: 20px 0; border: none; border-top: 1px solid var(--border-color);" />
          <h4 style="margin-bottom: 12px;">Teacher Review & Decision</h4>
          <form id="review-attempt-form">
            <div class="form-group">
              <label>Decision</label>
              <select id="rev-decision" class="form-control">
                <option value="CLEARED" ${detail.reviewDecision === 'CLEARED' ? 'selected' : ''}>CLEARED (No Cheating Detected)</option>
                <option value="FLAGGED_FOR_ACTION" ${detail.reviewDecision === 'FLAGGED_FOR_ACTION' ? 'selected' : ''}>FLAGGED FOR ACTION (Integrity Violation)</option>
              </select>
            </div>
            <div class="form-group">
              <label>Review Notes / Explanation</label>
              <textarea id="rev-note" class="form-control" placeholder="Write rationale...">${detail.reviewNote || ''}</textarea>
            </div>
            <button type="submit" class="btn btn-primary">Save Review Decision</button>
          </form>
        </div>

        <!-- Proctoring Timeline -->
        <div class="card">
          <h3 class="card-title">Proctoring Events & Camera Snapshots (${detail.proctoringEvents.length})</h3>
          <div class="proctoring-timeline">
            ${detail.proctoringEvents.length === 0 ? '<p style="color: var(--text-muted);">No proctoring events recorded.</p>' : detail.proctoringEvents.map(e => `
              <div class="timeline-item">
                ${e.snapshotUrl ? `<img src="${e.snapshotUrl}" class="timeline-thumbnail" alt="Snapshot" />` : '<div class="timeline-thumbnail" style="display:flex;align-items:center;justify-content:center;color:white;font-size:12px;">No Image</div>'}
                <div class="timeline-info">
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                    <strong style="font-size: 14px;">${e.eventType}</strong>
                    <span class="badge ${e.severity === 'CRITICAL' ? 'badge-danger' : (e.severity === 'WARNING' ? 'badge-warning' : 'badge-info')}">${e.severity}</span>
                  </div>
                  <div style="font-size: 12px; color: var(--text-muted);">${new Date(e.occurredAt).toLocaleString()}</div>
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      </div>
    `;

    document.getElementById('review-attempt-form').addEventListener('submit', async (e) => {
      e.preventDefault();
      try {
        await apiFetch(`/teacher/attempts/${attemptId}/review`, {
          method: 'POST',
          body: JSON.stringify({
            decision: document.getElementById('rev-decision').value,
            note: document.getElementById('rev-note').value
          })
        });
        showToast('Review decision saved!', 'success');
        renderTeacherAttemptDetail(params);
      } catch (err) {
        showToast(err.message, 'danger');
      }
    });

  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function renderTeacherAssignments() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  const urlParams = new URLSearchParams(window.location.hash.split('?')[1] || '');
  const subjectId = urlParams.get('subjectId');

  body.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <div>
        <h1 class="page-title">Weekly Assignments Management</h1>
        <p class="page-subtitle">Post weekly coursework and grade student submissions.</p>
      </div>
      <button class="btn btn-primary" id="btn-open-create-assignment">+ Post Assignment</button>
    </div>

    <div class="card">
      <div id="teacher-assignments-list">Loading assignments...</div>
    </div>
  `;

  try {
    const subjects = await apiFetch('/teacher/subjects');
    if (subjects.length === 0) {
      document.getElementById('teacher-assignments-list').innerHTML = `<p style="color: var(--text-muted);">No subjects assigned.</p>`;
      return;
    }

    const activeSubId = subjectId || subjects[0].id;
    const assignments = await apiFetch(`/teacher/assignments?subjectId=${activeSubId}`);
    const box = document.getElementById('teacher-assignments-list');

    box.innerHTML = `
      <div style="margin-bottom: 20px;">
        <label style="font-weight: 600; font-size: 14px; margin-right: 12px;">Filter Subject:</label>
        <select class="form-control" style="width: auto; display: inline-block;" onchange="window.location.hash='#teacher/assignments?subjectId=' + this.value">
          ${subjects.map(s => `<option value="${s.id}" ${s.id === activeSubId ? 'selected' : ''}>${s.name}</option>`).join('')}
        </select>
      </div>

      ${assignments.length === 0 ? '<p style="color: var(--text-muted);">No assignments posted for this subject yet.</p>' : `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Week</th>
                <th>Title</th>
                <th>Due Date</th>
                <th>Submission Ratio</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${assignments.map(a => `
                <tr>
                  <td><strong>Week ${a.weekNumber || 1}</strong></td>
                  <td style="font-weight: 600;">${a.title}</td>
                  <td>${new Date(a.dueAt).toLocaleString()}</td>
                  <td><span class="badge badge-info" style="font-size: 13px;">${a.submittedCount} / ${a.enrolledCount} Submitted</span></td>
                  <td><button class="btn btn-sm btn-primary" onclick="openViewSubmissionsModal('${a.id}', '${escapeHtml(a.title)}')">Review & Grade</button></td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `}
    `;

    document.getElementById('btn-open-create-assignment').addEventListener('click', () => {
      openCreateAssignmentModal(subjects, activeSubId);
    });

  } catch (err) {
    showToast(err.message, 'danger');
  }
}

function openCreateAssignmentModal(subjects, defaultSubjectId) {
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal-card">
      <div class="modal-header">
        <h3 class="modal-title">Post Weekly Assignment</h3>
        <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
      </div>
      <form id="create-assignment-form">
        <div class="form-group">
          <label>Subject</label>
          <select id="asgn-subject-id" class="form-control">
            ${subjects.map(s => `<option value="${s.id}" ${s.id === defaultSubjectId ? 'selected' : ''}>${s.name}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label>Assignment Title</label>
          <input type="text" id="asgn-title" class="form-control" placeholder="Week 1: Process Scheduling" required />
        </div>
        <div class="form-group">
          <label>Description & Instructions</label>
          <textarea id="asgn-desc" class="form-control" placeholder="Detailed instructions..."></textarea>
        </div>
        <div class="grid-2">
          <div class="form-group">
            <label>Week Number</label>
            <input type="number" id="asgn-week" class="form-control" value="1" required />
          </div>
          <div class="form-group">
            <label>Due Date & Time</label>
            <input type="datetime-local" id="asgn-due" class="form-control" required />
          </div>
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%;">Post Assignment</button>
      </form>
    </div>
  `;
  document.body.appendChild(modal);

  document.getElementById('create-assignment-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      await apiFetch('/teacher/assignments', {
        method: 'POST',
        body: JSON.stringify({
          subjectId: document.getElementById('asgn-subject-id').value,
          title: document.getElementById('asgn-title').value,
          description: document.getElementById('asgn-desc').value,
          weekNumber: parseInt(document.getElementById('asgn-week').value),
          dueAt: document.getElementById('asgn-due').value
        })
      });
      showToast('Assignment posted!', 'success');
      modal.remove();
      renderTeacherAssignments();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

window.openViewSubmissionsModal = async function(assignmentId, title) {
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal-card" style="max-width: 850px;">
      <div class="modal-header">
        <h3 class="modal-title">Submissions: ${title}</h3>
        <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
      </div>
      <div id="submissions-modal-body">Loading submissions...</div>
    </div>
  `;
  document.body.appendChild(modal);

  try {
    const submissions = await apiFetch(`/teacher/assignments/${assignmentId}/submissions`);
    const box = document.getElementById('submissions-modal-body');

    box.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Student Name</th>
              <th>Roll Number</th>
              <th>Submitted At</th>
              <th>Status</th>
              <th>Grade</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${submissions.map(s => `
              <tr>
                <td style="font-weight: 600;">${s.studentName}</td>
                <td>${s.studentRollNumber}</td>
                <td>${s.submittedAt ? new Date(s.submittedAt).toLocaleString() : 'Not Submitted'}</td>
                <td><span class="badge ${s.status === 'GRADED' ? 'badge-success' : (s.status === 'LATE' ? 'badge-danger' : (s.status === 'SUBMITTED' ? 'badge-info' : 'badge-secondary'))}">${s.status}</span></td>
                <td><strong>${s.grade || '-'}</strong></td>
                <td>
                  ${s.submissionId ? `<button class="btn btn-sm btn-primary" onclick="openGradeModal('${s.submissionId}', '${escapeHtml(s.studentName)}')">Grade</button>` : '-'}
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'danger');
  }
};

window.openGradeModal = function(submissionId, studentName) {
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal-card">
      <div class="modal-header">
        <h3 class="modal-title">Grade Submission: ${studentName}</h3>
        <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
      </div>
      <form id="grade-sub-form">
        <div class="form-group">
          <label>Grade (e.g. 9/10 or A+)</label>
          <input type="text" id="grade-val" class="form-control" placeholder="9/10" required />
        </div>
        <div class="form-group">
          <label>Written Feedback</label>
          <textarea id="grade-feed" class="form-control" placeholder="Great work on algorithm complexity..."></textarea>
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%;">Submit Grade & Feedback</button>
      </form>
    </div>
  `;
  document.body.appendChild(modal);

  document.getElementById('grade-sub-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      await apiFetch(`/teacher/submissions/${submissionId}/grade`, {
        method: 'POST',
        body: JSON.stringify({
          grade: document.getElementById('grade-val').value,
          feedback: document.getElementById('grade-feed').value
        })
      });
      showToast('Grade submitted!', 'success');
      modal.remove();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
};

/* ==========================================================================
   ADMIN VIEWS
   ========================================================================== */
async function renderAdminDashboard() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">Institution Administration</h1>
    <p class="page-subtitle">College-wide statistics, user management, and academic integrity monitoring.</p>
    
    <div class="grid-3">
      <div class="stat-card">
        <div class="stat-icon">👨‍🎓</div>
        <div>
          <div class="stat-number" id="adm-students">-</div>
          <div class="stat-label">Total Students</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👩‍🏫</div>
        <div>
          <div class="stat-number" id="adm-teachers">-</div>
          <div class="stat-label">Faculty / Teachers</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📖</div>
        <div>
          <div class="stat-number" id="adm-subjects">-</div>
          <div class="stat-label">Active Subjects</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📝</div>
        <div>
          <div class="stat-number" id="adm-exams">-</div>
          <div class="stat-label">Exams Created</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div>
          <div class="stat-number" id="adm-attempts">-</div>
          <div class="stat-label">Total Exam Attempts</div>
        </div>
      </div>
      <div class="stat-card" style="border: 2px solid var(--danger);">
        <div class="stat-icon" style="background: #FEE2E2; color: var(--danger);">🚨</div>
        <div>
          <div class="stat-number" id="adm-flagged" style="color: var(--danger);">-</div>
          <div class="stat-label">Flagged Attempts</div>
        </div>
      </div>
    </div>
  `;

  try {
    const stats = await apiFetch('/admin/stats');
    document.getElementById('adm-students').innerText = stats.totalStudents;
    document.getElementById('adm-teachers').innerText = stats.totalTeachers;
    document.getElementById('adm-subjects').innerText = stats.totalSubjects;
    document.getElementById('adm-exams').innerText = stats.totalExams;
    document.getElementById('adm-attempts').innerText = stats.totalAttempts;
    document.getElementById('adm-flagged').innerText = stats.flaggedAttemptsCount;
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function renderAdminStudents() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">Manage Students</h1>
    <p class="page-subtitle">View enrolled students and assign subject enrollments.</p>
    <div class="card">
      <div id="admin-students-list">Loading students...</div>
    </div>
  `;

  try {
    const [students, subjects] = await Promise.all([
      apiFetch('/admin/students'),
      apiFetch('/admin/subjects')
    ]);

    const box = document.getElementById('admin-students-list');

    if (students.length === 0) {
      box.innerHTML = `<p style="color: var(--text-muted);">No students registered yet.</p>`;
      return;
    }

    box.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Roll Number</th>
              <th>Name</th>
              <th>Email</th>
              <th>Department</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${students.map(s => `
              <tr>
                <td><strong>${s.rollNumber}</strong></td>
                <td style="font-weight: 600;">${s.name}</td>
                <td>${s.email}</td>
                <td>${s.department || '-'}</td>
                <td>
                  <button class="btn btn-sm btn-primary" onclick="openEnrollStudentModal('${s.id}', '${escapeHtml(s.name)}')">Enroll in Subject</button>
                  <button class="btn btn-sm btn-danger" onclick="deleteStudent('${s.id}')">Delete</button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;

    window.openEnrollStudentModal = (studentId, studentName) => {
      const modal = document.createElement('div');
      modal.className = 'modal-overlay';
      modal.innerHTML = `
        <div class="modal-card">
          <div class="modal-header">
            <h3 class="modal-title">Enroll ${studentName}</h3>
            <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
          </div>
          <form id="enroll-form">
            <div class="form-group">
              <label>Select Subject</label>
              <select id="enr-sub-id" class="form-control">
                ${subjects.map(sub => `<option value="${sub.id}">${sub.name}</option>`).join('')}
              </select>
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%;">Confirm Enrollment</button>
          </form>
        </div>
      `;
      document.body.appendChild(modal);

      document.getElementById('enroll-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await apiFetch('/admin/enrollments', {
            method: 'POST',
            body: JSON.stringify({
              studentId,
              subjectId: document.getElementById('enr-sub-id').value
            })
          });
          showToast('Student enrolled successfully!', 'success');
          modal.remove();
        } catch (err) {
          showToast(err.message, 'danger');
        }
      });
    };

    window.deleteStudent = async (id) => {
      if (confirm('Delete student account?')) {
        try {
          await apiFetch(`/admin/students/${id}`, { method: 'DELETE' });
          showToast('Student deleted', 'info');
          renderAdminStudents();
        } catch (err) {
          showToast(err.message, 'danger');
        }
      }
    };
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function renderAdminTeachers() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <div>
        <h1 class="page-title">Manage Faculty & Teachers</h1>
        <p class="page-subtitle">Create teacher accounts and assign teaching subjects.</p>
      </div>
      <button class="btn btn-primary" id="btn-create-teacher">+ Create Teacher Account</button>
    </div>
    <div class="card">
      <div id="admin-teachers-list">Loading teachers...</div>
    </div>
  `;

  try {
    const [teachers, subjects] = await Promise.all([
      apiFetch('/admin/teachers'),
      apiFetch('/admin/subjects')
    ]);

    const box = document.getElementById('admin-teachers-list');

    if (teachers.length === 0) {
      box.innerHTML = `<p style="color: var(--text-muted);">No faculty accounts created yet.</p>`;
    } else {
      box.innerHTML = `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Faculty Name</th>
                <th>Department</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              ${teachers.map(t => `
                <tr>
                  <td style="font-weight: 600;">${t.name}</td>
                  <td>${t.department || '-'}</td>
                  <td>
                    <button class="btn btn-sm btn-primary" onclick="openAssignTeacherModal('${t.id}', '${escapeHtml(t.name)}')">Assign Subject</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteTeacher('${t.id}')">Delete</button>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    }

    document.getElementById('btn-create-teacher').addEventListener('click', () => {
      openCreateTeacherModal(subjects);
    });

    window.openAssignTeacherModal = (teacherId, teacherName) => {
      const modal = document.createElement('div');
      modal.className = 'modal-overlay';
      modal.innerHTML = `
        <div class="modal-card">
          <div class="modal-header">
            <h3 class="modal-title">Assign Subject to ${teacherName}</h3>
            <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
          </div>
          <form id="assign-ts-form">
            <div class="form-group">
              <label>Select Subject</label>
              <select id="ts-sub-id" class="form-control">
                ${subjects.map(sub => `<option value="${sub.id}">${sub.name}</option>`).join('')}
              </select>
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%;">Assign Subject</button>
          </form>
        </div>
      `;
      document.body.appendChild(modal);

      document.getElementById('assign-ts-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await apiFetch('/admin/teacher-subjects', {
            method: 'POST',
            body: JSON.stringify({
              teacherId,
              subjectId: document.getElementById('ts-sub-id').value
            })
          });
          showToast('Subject assigned to teacher!', 'success');
          modal.remove();
        } catch (err) {
          showToast(err.message, 'danger');
        }
      });
    };

    window.deleteTeacher = async (id) => {
      if (confirm('Delete teacher account?')) {
        try {
          await apiFetch(`/admin/teachers/${id}`, { method: 'DELETE' });
          showToast('Teacher deleted', 'info');
          renderAdminTeachers();
        } catch (err) {
          showToast(err.message, 'danger');
        }
      }
    };
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

function openCreateTeacherModal(subjects) {
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal-card">
      <div class="modal-header">
        <h3 class="modal-title">Create Teacher Account</h3>
        <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
      </div>
      <form id="create-t-form">
        <div class="form-group">
          <label>Teacher Name</label>
          <input type="text" id="ct-name" class="form-control" placeholder="Prof. Robert Miller" required />
        </div>
        <div class="form-group">
          <label>Email Address</label>
          <input type="email" id="ct-email" class="form-control" placeholder="robert@college.edu" required />
        </div>
        <div class="form-group">
          <label>Department</label>
          <input type="text" id="ct-dept" class="form-control" placeholder="Computer Science" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input type="password" id="ct-pass" class="form-control" placeholder="••••••••" required />
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%;">Create Teacher Account</button>
      </form>
    </div>
  `;
  document.body.appendChild(modal);

  document.getElementById('create-t-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      await apiFetch('/auth/admin/create-teacher', {
        method: 'POST',
        body: JSON.stringify({
          name: document.getElementById('ct-name').value,
          email: document.getElementById('ct-email').value,
          department: document.getElementById('ct-dept').value,
          password: document.getElementById('ct-pass').value
        })
      });
      showToast('Teacher account created successfully!', 'success');
      modal.remove();
      renderAdminTeachers();
    } catch (err) {
      showToast(err.message, 'danger');
    }
  });
}

async function renderAdminSubjects() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <div>
        <h1 class="page-title">Manage Subjects & Courses</h1>
        <p class="page-subtitle">Add college subjects and view enrollment metrics.</p>
      </div>
      <button class="btn btn-primary" id="btn-create-subject">+ Add Subject</button>
    </div>
    <div class="card">
      <div id="admin-subjects-list">Loading subjects...</div>
    </div>
  `;

  try {
    const subjects = await apiFetch('/admin/subjects');
    const box = document.getElementById('admin-subjects-list');

    if (subjects.length === 0) {
      box.innerHTML = `<p style="color: var(--text-muted);">No subjects created yet.</p>`;
    } else {
      box.innerHTML = `
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Subject Name</th>
                <th>Department</th>
                <th>Assigned Faculty</th>
                <th>Enrolled Students</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${subjects.map(s => `
                <tr>
                  <td style="font-weight: 600;">${s.name}</td>
                  <td>${s.department || '-'}</td>
                  <td>${s.teacherNames && s.teacherNames.length > 0 ? s.teacherNames.join(', ') : '<span style="color:var(--text-muted);">None</span>'}</td>
                  <td><span class="badge badge-info">${s.enrolledCount} Enrolled</span></td>
                  <td><button class="btn btn-sm btn-danger" onclick="deleteSubject('${s.id}')">Delete</button></td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    }

    document.getElementById('btn-create-subject').addEventListener('click', () => {
      const modal = document.createElement('div');
      modal.className = 'modal-overlay';
      modal.innerHTML = `
        <div class="modal-card">
          <div class="modal-header">
            <h3 class="modal-title">Add New Subject</h3>
            <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
          </div>
          <form id="create-sub-form">
            <div class="form-group">
              <label>Subject Name</label>
              <input type="text" id="cs-name" class="form-control" placeholder="CS301 - Operating Systems" required />
            </div>
            <div class="form-group">
              <label>Department</label>
              <input type="text" id="cs-dept" class="form-control" placeholder="Computer Science" required />
            </div>
            <button type="submit" class="btn btn-primary" style="width: 100%;">Create Subject</button>
          </form>
        </div>
      `;
      document.body.appendChild(modal);

      document.getElementById('create-sub-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
          await apiFetch('/admin/subjects', {
            method: 'POST',
            body: JSON.stringify({
              name: document.getElementById('cs-name').value,
              department: document.getElementById('cs-dept').value
            })
          });
          showToast('Subject created!', 'success');
          modal.remove();
          renderAdminSubjects();
        } catch (err) {
          showToast(err.message, 'danger');
        }
      });
    });

    window.deleteSubject = async (id) => {
      if (confirm('Delete subject?')) {
        try {
          await apiFetch(`/admin/subjects/${id}`, { method: 'DELETE' });
          showToast('Subject deleted', 'info');
          renderAdminSubjects();
        } catch (err) {
          showToast(err.message, 'danger');
        }
      }
    };

  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function renderAdminFlaggedAttempts() {
  updateLayoutUI();
  const body = document.getElementById('page-body');
  body.innerHTML = `
    <h1 class="page-title">College-Wide Flagged Attempt Queue</h1>
    <p class="page-subtitle">Academic integrity review queue of attempts flagged for suspicious activity.</p>
    <div class="card">
      <div id="admin-flagged-list">Loading flagged queue...</div>
    </div>
  `;

  try {
    const attempts = await apiFetch('/admin/attempts?flagged=true');
    const box = document.getElementById('admin-flagged-list');

    if (attempts.length === 0) {
      box.innerHTML = `<p style="color: var(--text-muted);">No flagged attempts requiring review. Academic integrity clear! 🎉</p>`;
      return;
    }

    box.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Student Name</th>
              <th>Roll Number</th>
              <th>Exam Title</th>
              <th>Subject</th>
              <th>Score</th>
              <th>Grade</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${attempts.map(a => `
              <tr style="background: #FEF2F2;">
                <td style="font-weight: 600;">${a.studentName}</td>
                <td>${a.studentRollNumber}</td>
                <td>${a.examTitle}</td>
                <td>${a.subjectName}</td>
                <td>${a.correctAnswers || 0} / ${a.totalQuestions || 0}</td>
                <td><span class="badge badge-danger">${a.grade || '-'}</span></td>
                <td><span class="badge badge-warning">🚨 FLAGGED</span></td>
                <td><a href="#teacher/attempts/${a.attemptId}" class="btn btn-sm btn-primary">Review Timeline</a></td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
