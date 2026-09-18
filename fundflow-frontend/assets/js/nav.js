/* Renders the shared navbar into <div id="ff-nav"></div>. Include after api.js.
   Uses window.FF_BASE for relative paths so it works from any folder depth. */

(function () {
  const base = window.FF_BASE || "";
  const user = FundFlowApi.getUser();

  function guestLinks() {
    return `
      <a class="nav-link" href="${base}explore.html">Explore</a>
      <a class="nav-link" href="${base}about.html">How it works</a>
      <a class="nav-link" href="${base}ai/assistant.html">Help assistant</a>
      <a class="btn btn-ff-outline btn-sm ms-2" href="${base}login.html">Log in</a>
      <a class="btn btn-ff-primary btn-sm ms-2" href="${base}register.html">Sign up</a>
    `;
  }

  function roleLinks() {
    if (user.role === "ROLE_DONOR") {
      return `
        <a class="nav-link" href="${base}explore.html">Explore</a>
        <a class="nav-link" href="${base}donor/dashboard.html">Dashboard</a>
        <a class="nav-link" href="${base}donor/donations.html">My Donations</a>
        <a class="nav-link" href="${base}donor/saved.html">Saved</a>
      `;
    }
    if (user.role === "ROLE_ORGANIZER") {
      return `
        <a class="nav-link" href="${base}explore.html">Explore</a>
        <a class="nav-link" href="${base}organizer/dashboard.html">Dashboard</a>
        <a class="nav-link" href="${base}organizer/campaigns.html">My Campaigns</a>
        <a class="nav-link" href="${base}organizer/create-campaign.html">New Campaign</a>
      `;
    }
    if (user.role === "ROLE_ADMIN") {
      return `
        <a class="nav-link" href="${base}admin/dashboard.html">Dashboard</a>
        <a class="nav-link" href="${base}admin/campaigns.html">Campaigns</a>
        <a class="nav-link" href="${base}admin/users.html">Users</a>
        <a class="nav-link" href="${base}admin/donations.html">Donations</a>
      `;
    }
    return "";
  }

  function accountLinks() {
    return `
      <div class="dropdown d-inline-block ms-2 position-relative">
        <button class="btn btn-ff-outline btn-sm position-relative" id="ffBellBtn" type="button">
          🔔<span id="ffBellDot" class="ff-bell-dot d-none"></span>
        </button>
        <div id="ffBellMenu" class="dropdown-menu dropdown-menu-end p-2 d-none" style="width: 320px;">
          <div id="ffBellList" class="small">Loading…</div>
          <hr class="my-2">
          <a href="${base}notifications.html" class="small">View all</a>
        </div>
      </div>
      <div class="dropdown d-inline-block ms-2">
        <button class="btn btn-ff-outline btn-sm dropdown-toggle" data-bs-toggle="dropdown">${user.fullName}</button>
        <ul class="dropdown-menu dropdown-menu-end">
          <li><a class="dropdown-item" href="${base}profile.html">Profile</a></li>
          <li><a class="dropdown-item" href="#" id="ffLogoutBtn">Log out</a></li>
        </ul>
      </div>
    `;
  }

  const nav = document.getElementById("ff-nav");
  if (!nav) return;

  nav.innerHTML = `
    <nav class="navbar navbar-expand-lg ff-navbar py-2">
      <div class="container">
        <a class="navbar-brand" href="${base}index.html"><img src="${base}assets/logo/logo.svg" alt="FundFlow" height="28"></a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#ffNavCollapse">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="ffNavCollapse">
          <div class="navbar-nav me-auto"></div>
          <div class="d-flex align-items-center ff-account-slot"></div>
        </div>
      </div>
    </nav>
  `;

  nav.querySelector(".navbar-nav").innerHTML = user ? roleLinks() : guestLinks();
  if (user) {
    nav.querySelector(".ff-account-slot").innerHTML = accountLinks();

    document.getElementById("ffLogoutBtn").addEventListener("click", (e) => {
      e.preventDefault();
      FundFlowApi.logout();
    });

    const bellBtn = document.getElementById("ffBellBtn");
    const bellMenu = document.getElementById("ffBellMenu");
    bellBtn.addEventListener("click", async () => {
      bellMenu.classList.toggle("d-none");
      if (bellMenu.classList.contains("d-none")) return;
      try {
        const notifications = await FundFlowApi.get("/notifications");
        const list = document.getElementById("ffBellList");
        if (!notifications.length) {
          list.innerHTML = '<p class="text-muted mb-0">No notifications yet.</p>';
        } else {
          list.innerHTML = notifications.slice(0, 6).map(n => `
            <div class="mb-2 pb-2 border-bottom">
              <div>${n.message}</div>
              <div class="text-muted" style="font-size:.75rem">${new Date(n.createdAt).toLocaleString()}</div>
            </div>
          `).join("");
        }
      } catch (err) {
        document.getElementById("ffBellList").innerHTML = '<p class="text-danger mb-0">Could not load notifications.</p>';
      }
    });

    FundFlowApi.get("/notifications").then(notifications => {
      if (notifications.some(n => !n.read)) {
        document.getElementById("ffBellDot").classList.remove("d-none");
      }
    }).catch(() => {});
  }
})();
