/* EvalTrack Client-side Hash Router & Role Guards */

const Router = {
  routes: {},

  init(routes) {
    this.routes = routes;
    window.addEventListener('hashchange', () => this.handleRoute());
    window.addEventListener('load', () => this.handleRoute());
  },

  navigate(hash) {
    window.location.hash = hash;
  },

  handleRoute() {
    let hash = window.location.hash.slice(1) || 'login';
    const isAuthenticated = Auth.isAuthenticated();
    const userRole = Auth.getRole();

    // Default route redirects if logged in
    if (!isAuthenticated) {
      if (hash !== 'login' && hash !== 'register') {
        hash = 'login';
        window.location.hash = '#login';
      }
    } else {
      if (hash === 'login' || hash === 'register' || hash === '') {
        if (userRole === 'STUDENT') hash = 'student/dashboard';
        else if (userRole === 'TEACHER') hash = 'teacher/dashboard';
        else if (userRole === 'ADMIN') hash = 'admin/dashboard';
        window.location.hash = `#${hash}`;
        return;
      }
    }

    // Role-based Access Guards
    if (hash.startsWith('student/') && userRole !== 'STUDENT' && userRole !== 'ADMIN') {
      showToast('Access denied: Student area only', 'danger');
      window.location.hash = userRole === 'TEACHER' ? '#teacher/dashboard' : '#login';
      return;
    }

    if (hash.startsWith('teacher/') && userRole !== 'TEACHER' && userRole !== 'ADMIN') {
      showToast('Access denied: Teacher area only', 'danger');
      window.location.hash = userRole === 'STUDENT' ? '#student/dashboard' : '#login';
      return;
    }

    if (hash.startsWith('admin/') && userRole !== 'ADMIN') {
      showToast('Access denied: Administrator area only', 'danger');
      window.location.hash = userRole === 'STUDENT' ? '#student/dashboard' : (userRole === 'TEACHER' ? '#teacher/dashboard' : '#login');
      return;
    }

    // Match Route Handler
    let matchedHandler = null;
    let params = {};

    for (const routePattern in this.routes) {
      const patternParts = routePattern.split('/');
      const hashParts = hash.split('/');

      if (patternParts.length === hashParts.length) {
        let match = true;
        let p = {};
        for (let i = 0; i < patternParts.length; i++) {
          if (patternParts[i].startsWith(':')) {
            p[patternParts[i].slice(1)] = hashParts[i];
          } else if (patternParts[i] !== hashParts[i]) {
            match = false;
            break;
          }
        }
        if (match) {
          matchedHandler = this.routes[routePattern];
          params = p;
          break;
        }
      }
    }

    if (matchedHandler) {
      matchedHandler(params);
    } else {
      console.warn(`No route matched for #${hash}`);
      if (isAuthenticated) {
        if (userRole === 'STUDENT') window.location.hash = '#student/dashboard';
        else if (userRole === 'TEACHER') window.location.hash = '#teacher/dashboard';
        else if (userRole === 'ADMIN') window.location.hash = '#admin/dashboard';
      } else {
        window.location.hash = '#login';
      }
    }
  }
};
