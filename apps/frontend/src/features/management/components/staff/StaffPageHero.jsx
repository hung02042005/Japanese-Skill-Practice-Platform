import './StaffPageHero.css';

export default function StaffPageHero({ title, subtitle, icon, accent = 'pink' }) {
  return (
    <div className={`sph-hero sph-hero--${accent}`}>
      <div className="sph-icon-wrap">
        {icon}
      </div>
      <div className="sph-text">
        <h1 className="sph-title">{title}</h1>
        {subtitle && <p className="sph-subtitle">{subtitle}</p>}
      </div>
      {/* Floating petal decorations */}
      <svg className="sph-deco" viewBox="0 0 240 88" preserveAspectRatio="xMaxYMid meet" aria-hidden="true" focusable="false">
        <ellipse cx="172" cy="20" rx="9" ry="14" transform="rotate(-22 172 20)" fill="currentColor" opacity="0.13"/>
        <ellipse cx="198" cy="42" rx="7" ry="12" transform="rotate(28 198 42)" fill="currentColor" opacity="0.09"/>
        <ellipse cx="215" cy="12" rx="6" ry="10" transform="rotate(58 215 12)" fill="currentColor" opacity="0.07"/>
        <ellipse cx="160" cy="64" rx="6" ry="11" transform="rotate(-42 160 64)" fill="currentColor" opacity="0.10"/>
        <ellipse cx="230" cy="62" rx="5" ry="9" transform="rotate(12 230 62)" fill="currentColor" opacity="0.07"/>
        <ellipse cx="185" cy="74" rx="5" ry="8" transform="rotate(-58 185 74)" fill="currentColor" opacity="0.06"/>
        <circle cx="178" cy="38" r="2.5" fill="currentColor" opacity="0.17"/>
        <circle cx="208" cy="56" r="2" fill="currentColor" opacity="0.12"/>
        <circle cx="222" cy="28" r="1.8" fill="currentColor" opacity="0.10"/>
      </svg>
    </div>
  );
}
