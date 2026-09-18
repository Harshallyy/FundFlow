/* FundFlow frontend API client.
   Every page includes this before its own script. Relies on window.FF_BASE
   (set per-page: "" at the site root, "../" one folder deep) to build
   relative links/asset paths consistently regardless of folder depth. */

const FF_API_BASE = "http://localhost:8080/api";

const FundFlowApi = {
  // ---- token/session storage ----
  getToken() {
    return localStorage.getItem("ff_token");
  },
  getUser() {
    const raw = localStorage.getItem("ff_user");
    return raw ? JSON.parse(raw) : null;
  },
  isLoggedIn() {
    return !!this.getToken();
  },
  setSession(authResponse) {
    localStorage.setItem("ff_token", authResponse.token);
    localStorage.setItem("ff_user", JSON.stringify({
      userId: authResponse.userId,
      fullName: authResponse.fullName,
      email: authResponse.email,
      role: authResponse.role,
    }));
  },
  logout() {
    localStorage.removeItem("ff_token");
    localStorage.removeItem("ff_user");
    window.location.href = (window.FF_BASE || "") + "index.html";
  },

  // ---- guards used at the top of role-specific pages ----
  requireAuth() {
    if (!this.isLoggedIn()) {
      window.location.href = (window.FF_BASE || "") + "login.html";
    }
  },
  requireRole(role) {
    this.requireAuth();
    const user = this.getUser();
    if (!user || user.role !== "ROLE_" + role) {
      window.location.href = (window.FF_BASE || "") + "index.html";
    }
  },

  // ---- core request helper ----
  async request(path, { method = "GET", body = null, auth = true } = {}) {
    const headers = { "Content-Type": "application/json" };
    if (auth && this.getToken()) {
      headers["Authorization"] = "Bearer " + this.getToken();
    }

    let response;
    try {
      response = await fetch(FF_API_BASE + path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
      });
    } catch (networkErr) {
      throw new Error("Could not reach the FundFlow server. Is the backend running on localhost:8080?");
    }

    if (response.status === 204) return null;

    let data = null;
    try { data = await response.json(); } catch (_) { /* empty body */ }

    if (!response.ok) {
      const message = (data && data.message) ? data.message : ("Request failed (" + response.status + ")");
      const err = new Error(message);
      err.status = response.status;
      err.fieldErrors = data ? data.fieldErrors : null;
      throw err;
    }
    return data;
  },

  get(path) { return this.request(path, { method: "GET" }); },
  post(path, body) { return this.request(path, { method: "POST", body }); },
  put(path, body) { return this.request(path, { method: "PUT", body }); },
  del(path) { return this.request(path, { method: "DELETE" }); },
};
