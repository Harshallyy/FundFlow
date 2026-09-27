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
      <div class="dropdown d-inline-block ms-2 position-relative ff-notification-wrap">
        <button class="btn btn-ff-outline btn-sm position-relative ff-notification-btn" id="ffBellBtn" type="button" aria-label="Notifications" aria-expanded="false" aria-controls="ffBellMenu">
          <svg class="ff-bell-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg><span class="ff-notification-label">Notifications</span><span id="ffBellDot" class="ff-bell-dot d-none"></span>
        </button>
        <div id="ffBellMenu" class="dropdown-menu dropdown-menu-end p-2 d-none ff-notification-panel" role="dialog" aria-label="Notifications">
          <div class="d-flex justify-content-between align-items-center px-2 pb-2">
            <strong>Notifications</strong><span id="ffBellCount" class="text-secondary small"></span>
          </div>
          <div id="ffBellList" class="small" aria-live="polite">Loading…</div>
          <hr class="my-2">
          <a href="${base}notifications.html" class="small">View all</a>
        </div>
      </div>
      <div class="dropdown d-inline-block ms-2 ff-profile-dropdown">
        <button class="btn btn-ff-outline btn-sm dropdown-toggle" id="ffProfileBtn" type="button" aria-expanded="false" aria-controls="ffProfileMenu">${user.fullName}</button>
        <ul class="dropdown-menu dropdown-menu-end" id="ffProfileMenu">
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
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#ffNavCollapse" aria-controls="ffNavCollapse" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="ffNavCollapse">
          <div class="navbar-nav me-auto"></div>
          <div class="d-flex align-items-center ff-account-slot"></div>
        </div>
      </div>
    </nav>
  `;

  nav.querySelector(".navbar-nav").innerHTML = user
    ? roleLinks()
    : guestLinks();
  nav.querySelectorAll(".navbar-nav a").forEach((link) => {
    if (new URL(link.href).pathname === window.location.pathname) {
      link.classList.add("active");
      link.setAttribute("aria-current", "page");
    }
  });
  if (user) {
    nav.querySelector(".ff-account-slot").innerHTML = accountLinks();

    document.getElementById("ffLogoutBtn").addEventListener("click", (e) => {
      e.preventDefault();
      FundFlowApi.logout();
    });

    const bellBtn = document.getElementById("ffBellBtn");
    const bellMenu = document.getElementById("ffBellMenu");
    let notificationRequest;
    function setBellOpen(open) {
      bellMenu.classList.toggle("d-none", !open);
      bellMenu.classList.toggle("show", open);
      bellBtn.setAttribute("aria-expanded", String(open));
    }

    async function loadNotifications() {
      const list = document.getElementById("ffBellList");
      try {
        notificationRequest ??= FundFlowApi.get("/notifications");
        const notifications = await notificationRequest;
        document.getElementById("ffBellCount").textContent =
          notifications.length ? `${notifications.length} total` : "All clear";
        list.replaceChildren();
        if (!notifications.length) {
          const empty = document.createElement("div");
          empty.className = "ff-notification-empty";
          empty.innerHTML =
            '<span class="ff-empty-icon" aria-hidden="true">&#10003;</span><strong>No notifications for now</strong><span>You\'re all caught up.</span>';
          list.appendChild(empty);
          return notifications;
        }
        notifications.slice(0, 6).forEach((n) => {
          const item = document.createElement("div");
          item.className = `ff-notification-item ${n.read ? "" : "is-unread"}`;
          const message = document.createElement("div");
          message.textContent = FundFlowApi.formatNotificationMessage(
            n.message,
          );
          const meta = document.createElement("div");
          meta.className = "text-secondary";
          meta.textContent = `${new Date(n.createdAt).toLocaleString()} · ${(n.type || "NOTICE").replaceAll("_", " ")}`;
          item.append(message, meta);
          list.appendChild(item);
        });
        return notifications;
      } catch (err) {
        notificationRequest = null;
        list.textContent = "Could not load notifications.";
        return [];
      }
    }

    bellBtn.addEventListener("click", async () => {
      const opening = bellMenu.classList.contains("d-none");
      setBellOpen(opening);
      if (!opening) return;
      await loadNotifications();
    });
    document.addEventListener("click", (event) => {
      if (!bellMenu.contains(event.target) && !bellBtn.contains(event.target))
        setBellOpen(false);
    });
    document.addEventListener("keydown", (event) => {
      if (event.key === "Escape") setBellOpen(false);
    });

    loadNotifications()
      .then((notifications) => {
        if (notifications.some((n) => !n.read)) {
          document.getElementById("ffBellDot").classList.remove("d-none");
        }
      })
      .catch(() => {});

    const profile = nav.querySelector(".ff-profile-dropdown");
    const profileButton = document.getElementById("ffProfileBtn");
    const profileMenu = document.getElementById("ffProfileMenu");
    let profileCloseTimer;
    let hoverOpenedProfile = false;
    function setProfileOpen(open) {
      profileMenu.classList.toggle("show", open);
      profileButton.setAttribute("aria-expanded", String(open));
    }
    profileButton.addEventListener("click", () => {
      if (hoverOpenedProfile) {
        hoverOpenedProfile = false;
        return;
      }
      setProfileOpen(!profileMenu.classList.contains("show"));
    });
    if (window.matchMedia("(hover: hover) and (min-width: 992px)").matches) {
      profile.addEventListener("pointerenter", () => {
        window.clearTimeout(profileCloseTimer);
        hoverOpenedProfile = true;
        setProfileOpen(true);
      });
      profile.addEventListener("pointerleave", () => {
        hoverOpenedProfile = false;
        profileCloseTimer = window.setTimeout(() => {
          if (!profileMenu.contains(document.activeElement))
            setProfileOpen(false);
        }, 140);
      });
    }
    document.addEventListener("click", (event) => {
      if (!profile.contains(event.target)) setProfileOpen(false);
    });
    document.addEventListener("keydown", (event) => {
      if (event.key === "Escape" && profileMenu.classList.contains("show")) {
        setProfileOpen(false);
        profileButton.focus();
      }
    });
  }
})();
