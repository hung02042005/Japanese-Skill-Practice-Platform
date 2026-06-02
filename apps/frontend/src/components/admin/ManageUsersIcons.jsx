/* SVG icons used by ManageUsers page */

/* ── Stat card icons ── */
export function StatIconUsers() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <circle cx="11" cy="9.5" r="4.5" fill="currentColor" opacity="0.15"/>
      <circle cx="11" cy="9.5" r="3.2" stroke="currentColor" strokeWidth="1.8" fill="none"/>
      <path d="M3 24c0-4.418 3.582-8 8-8s8 3.582 8 8" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" fill="none"/>
      <circle cx="20" cy="10.5" r="2.8" fill="currentColor" opacity="0.08"/>
      <circle cx="20" cy="10.5" r="1.9" stroke="currentColor" strokeWidth="1.3" fill="none" opacity="0.5"/>
      <path d="M16.5 24c.3-3 2.2-5.5 4.5-6.3" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" fill="none" opacity="0.45"/>
      <ellipse cx="23.5" cy="3.5" rx="1.5" ry="2.5" fill="currentColor" opacity="0.2" transform="rotate(-20 23.5 3.5)"/>
    </svg>
  );
}

export function StatIconActive() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(0 14 14)"/>
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(72 14 14)"/>
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(144 14 14)"/>
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(216 14 14)"/>
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(288 14 14)"/>
      <circle cx="14" cy="14" r="5.5" fill="currentColor" opacity="0.13"/>
      <circle cx="14" cy="14" r="4" fill="currentColor" opacity="0.28"/>
      <path d="M10.5 14l2.5 2.5 4.5-5" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

export function StatIconSuspended() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <rect x="5" y="13" width="18" height="12" rx="3" fill="currentColor" opacity="0.1"/>
      <rect x="5" y="13" width="18" height="12" rx="3" stroke="currentColor" strokeWidth="1.8"/>
      <path d="M9.5 13V10a4.5 4.5 0 0 1 9 0v3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      <circle cx="14" cy="19.5" r="1.8" fill="currentColor" opacity="0.5"/>
      <path d="M14 19.5v1.8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      <ellipse cx="21.5" cy="4" rx="1.4" ry="2.4" fill="currentColor" opacity="0.18" transform="rotate(-18 21.5 4)"/>
    </svg>
  );
}

export function StatIconPending() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <circle cx="14" cy="14" r="10.5" fill="currentColor" opacity="0.07"/>
      <circle cx="14" cy="14" r="9.5" stroke="currentColor" strokeWidth="1.8"/>
      <path d="M14 7v7l4.5 2.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
      <circle cx="8"  cy="25.5" r="1.1" fill="currentColor" opacity="0.3"/>
      <circle cx="12" cy="26.8" r="1.1" fill="currentColor" opacity="0.2"/>
      <circle cx="16" cy="27.2" r="1.1" fill="currentColor" opacity="0.12"/>
    </svg>
  );
}

/* ── Row action icons ── */
export function IcBan() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2.2"/>
      <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" stroke="currentColor" strokeWidth="2.2"/>
    </svg>
  );
}

export function IcCheck() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

export function IcKey() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="7.5" cy="15.5" r="5.5" stroke="currentColor" strokeWidth="2"/>
      <path d="M21 2l-9.5 9.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      <path d="M19 4l-2 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      <path d="M16 7l-2 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
    </svg>
  );
}

export function IcSwap() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M7 16H3l4-4 4 4H7V8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M17 8h4l-4 4-4-4h4v8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

export function IcTrash() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6M10 11v6M14 11v6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

/* ── Header / toolbar icons ── */
export function IcAdminChip() {
  return (
    <svg width="13" height="13" viewBox="0 0 22 18" fill="none" aria-hidden="true">
      <path d="M2 14L5 5l4.5 4L11 2l1.5 7L17 5l3 9H2z" fill="currentColor" opacity="0.82" strokeLinejoin="round"/>
      <rect x="2" y="14.5" width="18" height="2.5" rx="1.25" fill="currentColor" opacity="0.82"/>
      <circle cx="11" cy="2" r="1.3" fill="currentColor"/>
    </svg>
  );
}

export function IcAddStaff() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 21v-8M12 13C9 13 5.5 10.5 4 6c3.5.5 6.5 3.5 8 7M12 13c3 0 6.5-2.5 8-7-3.5.5-6.5 3.5-8 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M12 7V2M9.5 4.5h5" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
    </svg>
  );
}

export function IcSearchGlass() {
  return (
    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="10.5" cy="10.5" r="7.5" stroke="currentColor" strokeWidth="2"/>
      <circle cx="10.5" cy="10.5" r="3" fill="currentColor" opacity="0.1"/>
      <ellipse cx="10.5" cy="7.5" rx="1.1" ry="1.8" fill="currentColor" opacity="0.18"/>
      <ellipse cx="10.5" cy="7.5" rx="1.1" ry="1.8" fill="currentColor" opacity="0.18" transform="rotate(90 10.5 10.5)"/>
      <path d="M16.5 16.5l4.2 4.2" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round"/>
    </svg>
  );
}

/* ── Tab icons ── */
function TabIconStudent() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 3L2 9l10 6 10-6-10-6z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" fill="currentColor" opacity="0.12"/>
      <path d="M6 12.5v4C6 18.43 8.686 20 12 20s6-1.57 6-3.5v-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      <path d="M20.5 9.5v5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      <ellipse cx="20.5" cy="15.5" rx="1.3" ry="2" fill="currentColor" opacity="0.45" transform="rotate(12 20.5 15.5)"/>
    </svg>
  );
}

function TabIconStaff() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="7" r="3.5" stroke="currentColor" strokeWidth="1.8"/>
      <path d="M4.5 21c0-4.14 3.36-7.5 7.5-7.5s7.5 3.36 7.5 7.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      <path d="M17.5 11c0-2.5 3-4.5 5.5-4.5C22.5 9 20.5 11 17.5 11z" fill="currentColor" opacity="0.38" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round"/>
      <path d="M17.5 11c.5-1.5 2.5-3 4-3" stroke="currentColor" strokeWidth="1" strokeLinecap="round" opacity="0.5"/>
    </svg>
  );
}

function TabIconAdmin() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 2l2.6 8.4H22l-6.3 4.6 2.4 8-6.1-4.4-6.1 4.4 2.4-8L2 10.4h7.4L12 2z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" strokeLinecap="round" fill="currentColor" opacity="0.13"/>
      <ellipse cx="12" cy="2.8" rx="1.1" ry="1.7" fill="currentColor" opacity="0.42"/>
    </svg>
  );
}

/* ── Modal confirm icon ── */
export function IcBloomCheck() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
      <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(0 12 12)"/>
      <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(72 12 12)"/>
      <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(144 12 12)"/>
      <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(216 12 12)"/>
      <ellipse cx="12" cy="7" rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(288 12 12)"/>
      <circle cx="12" cy="12" r="4.5" fill="var(--color-primary)" opacity="0.14"/>
      <path d="M9 12l2.2 2.2 4-4.5" stroke="var(--color-primary)" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

/* ── Pre-built icon maps ── */
export const TAB_ICONS = {
  student: <TabIconStudent />,
  staff:   <TabIconStaff />,
  admin:   <TabIconAdmin />,
};

export const STAT_ICONS = {
  total:     <StatIconUsers />,
  active:    <StatIconActive />,
  suspended: <StatIconSuspended />,
  pending:   <StatIconPending />,
};

/* ════════════════════════════════════
   NEW — Dashboard icons
════════════════════════════════════ */
export function IcChart() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="3"  y="12" width="4" height="9" rx="1.5" fill="currentColor" opacity="0.18"/>
      <rect x="10" y="6"  width="4" height="15" rx="1.5" fill="currentColor" opacity="0.18"/>
      <rect x="17" y="9"  width="4" height="12" rx="1.5" fill="currentColor" opacity="0.18"/>
      <rect x="3"  y="12" width="4" height="9"  rx="1.5" stroke="currentColor" strokeWidth="1.8"/>
      <rect x="10" y="6"  width="4" height="15" rx="1.5" stroke="currentColor" strokeWidth="1.8"/>
      <rect x="17" y="9"  width="4" height="12" rx="1.5" stroke="currentColor" strokeWidth="1.8"/>
      <path d="M2 21h20" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      <ellipse cx="22" cy="4" rx="1.2" ry="2" fill="currentColor" opacity="0.18" transform="rotate(-15 22 4)"/>
    </svg>
  );
}

export function IcSystemHealth() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M2 12h4l2-5 3 10 3-8 2 3h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <circle cx="9" cy="7" r="1.5" fill="currentColor" opacity="0.35"/>
      <ellipse cx="21" cy="4" rx="1.3" ry="2.2" fill="currentColor" opacity="0.15" transform="rotate(-20 21 4)"/>
    </svg>
  );
}

/* ════════════════════════════════════
   NEW — Settings tab icons
════════════════════════════════════ */
export function IcMail() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="2" y="4" width="20" height="16" rx="2.5" fill="currentColor" opacity="0.08"/>
      <rect x="2" y="4" width="20" height="16" rx="2.5" stroke="currentColor" strokeWidth="2"/>
      <path d="M2 7l10 7 10-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <ellipse cx="20.5" cy="3" rx="1" ry="1.8" fill="currentColor" opacity="0.20" transform="rotate(-15 20.5 3)"/>
    </svg>
  );
}

export function IcShield() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 2l8 3v7c0 4.5-3.5 8.5-8 10-4.5-1.5-8-5.5-8-10V5l8-3z"
            fill="currentColor" opacity="0.10"
            stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
      <path d="M8.5 12l2.5 2.5 5-5.5" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

export function IcWrench() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"
            fill="currentColor" opacity="0.08"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <ellipse cx="21" cy="4" rx="1" ry="1.8" fill="currentColor" opacity="0.18" transform="rotate(-30 21 4)"/>
    </svg>
  );
}

export function IcBell() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
            fill="currentColor" opacity="0.10"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      <circle cx="12" cy="3" r="1.5" fill="currentColor" opacity="0.30"/>
      <ellipse cx="20" cy="5" rx="1" ry="1.8" fill="currentColor" opacity="0.15" transform="rotate(15 20 5)"/>
    </svg>
  );
}

export function IcPlus() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
    </svg>
  );
}

export function IcEdit() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"
            fill="currentColor" opacity="0.12"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

export const SETTINGS_TAB_ICONS = {
  system:        <IcWrench />,
  email:         <IcMail />,
  security:      <IcShield />,
  notifications: <IcBell />,
};
