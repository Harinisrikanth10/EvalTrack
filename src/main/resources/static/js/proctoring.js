/* EvalTrack Camera Proctoring & Integrity Engine */

const Proctoring = {
  stream: null,
  attemptId: null,
  snapshotInterval: null,
  reconnectTimer: null,
  isExamActive: false,

  async requestCamera() {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 320 }, height: { ideal: 240 } },
        audio: false
      });
      return this.stream;
    } catch (err) {
      console.error('Camera access error:', err);
      throw new Error('Webcam access was denied or no camera device was found. Camera proctoring is mandatory for this examination.');
    }
  },

  attachPreview(videoElement) {
    if (this.stream && videoElement) {
      videoElement.srcObject = this.stream;
      videoElement.play().catch(err => console.warn('Video preview play warning:', err));
    }
  },

  async startProctoring(attemptId, videoElement) {
    this.attemptId = attemptId;
    this.isExamActive = true;

    if (videoElement && this.stream) {
      videoElement.srcObject = this.stream;
      videoElement.play().catch(err => console.warn('Video exam play warning:', err));
    }

    // 1. Listen for Camera disconnection / track end
    if (this.stream) {
      const videoTrack = this.stream.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.onended = () => this.handleCameraLoss();
      }
    }

    // 2. Periodic Snapshots every 15 seconds
    this.snapshotInterval = setInterval(() => {
      this.captureAndUploadSnapshot(videoElement);
    }, 15000);

    // Initial snapshot after 3s
    setTimeout(() => {
      this.captureAndUploadSnapshot(videoElement);
    }, 3000);

    // 3. Event Listener: Tab Blur / Window Switch
    window.onblur = () => {
      if (this.isExamActive) {
        this.logEvent('TAB_BLUR', 'WARNING');
        showToast('Warning: Tab switch or focus loss detected!', 'danger');
      }
    };

    document.onvisibilitychange = () => {
      if (document.hidden && this.isExamActive) {
        this.logEvent('TAB_BLUR', 'WARNING');
      }
    };

    // 4. Event Listener: Fullscreen Exit
    document.onfullscreenchange = () => {
      if (!document.fullscreenElement && this.isExamActive) {
        this.logEvent('FULLSCREEN_EXIT', 'WARNING');
        const banner = document.getElementById('fullscreen-warning-banner');
        if (banner) banner.classList.remove('hidden');
        showToast('ALERT: Fullscreen mode exited! Return to fullscreen immediately.', 'danger');
      } else {
        const banner = document.getElementById('fullscreen-warning-banner');
        if (banner) banner.classList.add('hidden');
      }
    };
  },

  async captureAndUploadSnapshot(videoElement) {
    if (!this.isExamActive || !videoElement || !this.stream) return;

    try {
      const canvas = document.createElement('canvas');
      canvas.width = 320;
      canvas.height = 240;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(videoElement, 0, 0, canvas.width, canvas.height);

      const base64Data = canvas.toDataURL('image/jpeg', 0.6);

      await apiFetch(`/attempts/${this.attemptId}/proctoring-events`, {
        method: 'POST',
        body: JSON.stringify({
          eventType: 'SNAPSHOT',
          snapshotBase64: base64Data,
          severity: 'INFO'
        })
      });
    } catch (err) {
      console.error('Failed to upload proctoring snapshot:', err);
    }
  },

  async logEvent(eventType, severity = 'WARNING') {
    if (!this.attemptId) return;
    try {
      await apiFetch(`/attempts/${this.attemptId}/proctoring-events`, {
        method: 'POST',
        body: JSON.stringify({
          eventType,
          severity
        })
      });
    } catch (err) {
      console.error('Failed to log proctoring event:', err);
    }
  },

  handleCameraLoss() {
    if (!this.isExamActive) return;
    this.logEvent('CAMERA_LOST', 'CRITICAL');
    showToast('CRITICAL WARNING: Camera connection lost! Reconnect within 60 seconds.', 'danger');

    let secondsLeft = 60;
    const banner = document.getElementById('camera-warning-banner');
    if (banner) {
      banner.classList.remove('hidden');
      banner.innerText = `Camera disconnected! Please reconnect your webcam. Auto-submitting in ${secondsLeft} seconds...`;
    }

    this.reconnectTimer = setInterval(async () => {
      secondsLeft--;
      if (banner) {
        banner.innerText = `Camera disconnected! Please reconnect your webcam. Auto-submitting in ${secondsLeft} seconds...`;
      }

      try {
        const newStream = await navigator.mediaDevices.getUserMedia({ video: true });
        if (newStream) {
          clearInterval(this.reconnectTimer);
          this.stream = newStream;
          if (banner) banner.classList.add('hidden');
          showToast('Camera reconnected successfully!', 'success');
          return;
        }
      } catch (e) {}

      if (secondsLeft <= 0) {
        clearInterval(this.reconnectTimer);
        this.stopProctoring();
        if (window.autoSubmitExam) {
          window.autoSubmitExam('AUTO_SUBMITTED');
        }
      }
    }, 1000);
  },

  stopProctoring() {
    this.isExamActive = false;
    if (this.snapshotInterval) clearInterval(this.snapshotInterval);
    if (this.reconnectTimer) clearInterval(this.reconnectTimer);

    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }

    window.onblur = null;
    document.onvisibilitychange = null;
    document.onfullscreenchange = null;

    if (document.fullscreenElement) {
      document.exitFullscreen().catch(() => {});
    }
  }
};
