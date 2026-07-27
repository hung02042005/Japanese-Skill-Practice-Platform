import { useState, useRef, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { setSelectedLevel } from '@/features/dashboard/studentSlice';
import { SakuraIcon } from '@/shared/components/common/AppIcons';
import './CoursePickerBanner.css';

/* ─────────────────────────────────────────────
   Level icons — flat-vector, uses currentColor
   so they inherit the JLPT card's text color
───────────────────────────────────────────── */

/** N5 — Sprout: first steps out of the soil */
function SproutIcon() {
  return (
    <svg width="40" height="40" viewBox="0 0 36 36" fill="none" aria-hidden="true">
      {/* Soil mound */}
      <ellipse cx="18" cy="30.5" rx="11" ry="4" fill="currentColor" opacity="0.18" />
      {/* Stem */}
      <path d="M18 30 L18 19" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" />
      {/* Left leaf */}
      <ellipse cx="12.5" cy="22" rx="6.5" ry="3" fill="currentColor" opacity="0.65"
        transform="rotate(-38 12.5 22)" />
      {/* Right leaf */}
      <ellipse cx="23.5" cy="22" rx="6.5" ry="3" fill="currentColor"
        transform="rotate(38 23.5 22)" />
      {/* Sprout tip */}
      <ellipse cx="18" cy="16.5" rx="2.6" ry="3.8" fill="currentColor" opacity="0.88" />
    </svg>
  );
}

/** N4 — Open book: deepening the foundation */
function BookIcon() {
  return (
    <svg width="40" height="40" viewBox="0 0 36 36" fill="none" aria-hidden="true">
      {/* Left page fill */}
      <path d="M6 12 Q6 8 10 8 L18 9 L18 28 Q12 26 6 28 Z"
        fill="currentColor" opacity="0.18" />
      {/* Right page fill */}
      <path d="M18 9 L26 8 Q30 8 30 12 L30 28 Q24 26 18 28 Z"
        fill="currentColor" opacity="0.10" />
      {/* Outer border */}
      <path d="M6 12 Q6 8 10 8 L18 9 L26 8 Q30 8 30 12 L30 28 Q24 26 18 28 Q12 26 6 28 Z"
        stroke="currentColor" strokeWidth="1.6" fill="none" strokeLinejoin="round" />
      {/* Spine */}
      <line x1="18" y1="9" x2="18" y2="28"
        stroke="currentColor" strokeWidth="1.2" strokeOpacity="0.55" strokeLinecap="round" />
      {/* Left text lines */}
      <line x1="9"  y1="14" x2="16.5" y2="13.5" stroke="currentColor" strokeWidth="1.2"
        strokeOpacity="0.45" strokeLinecap="round" />
      <line x1="9"  y1="18" x2="16.5" y2="17.5" stroke="currentColor" strokeWidth="1.2"
        strokeOpacity="0.45" strokeLinecap="round" />
      <line x1="9"  y1="22" x2="14"   y2="21.7" stroke="currentColor" strokeWidth="1.2"
        strokeOpacity="0.45" strokeLinecap="round" />
      {/* Right text lines */}
      <line x1="19.5" y1="13.5" x2="27" y2="14" stroke="currentColor" strokeWidth="1.2"
        strokeOpacity="0.45" strokeLinecap="round" />
      <line x1="19.5" y1="17.5" x2="27" y2="18" stroke="currentColor" strokeWidth="1.2"
        strokeOpacity="0.45" strokeLinecap="round" />
    </svg>
  );
}

/** N3 — Torii gate: crossing into deeper territory */
function ToriiIcon() {
  return (
    <svg width="40" height="40" viewBox="0 0 36 36" fill="none" aria-hidden="true">
      {/* Curved top kasagi beam */}
      <path d="M3 14 Q18 8 33 14"
        stroke="currentColor" strokeWidth="3" strokeLinecap="round" fill="none" />
      {/* End-cap circles */}
      <circle cx="3.5"  cy="14" r="2.2" fill="currentColor" />
      <circle cx="32.5" cy="14" r="2.2" fill="currentColor" />
      {/* Nuki crossbar */}
      <line x1="8" y1="19" x2="28" y2="19"
        stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
      {/* Left pillar */}
      <line x1="12" y1="12" x2="12" y2="34"
        stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
      {/* Right pillar */}
      <line x1="24" y1="12" x2="24" y2="34"
        stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
      {/* Ground line */}
      <line x1="6" y1="34" x2="30" y2="34"
        stroke="currentColor" strokeWidth="1.5" strokeOpacity="0.35" strokeLinecap="round" />
    </svg>
  );
}

/** N2 — Cherry blossom: full bloom */
function BlossomIcon() {
  return (
    <svg width="40" height="40" viewBox="0 0 36 36" fill="none" aria-hidden="true">
      {/* 5 petals — each is an ellipse rotated 72° around the center (18,18) */}
      <ellipse cx="18" cy="11" rx="4.8" ry="8" fill="currentColor" opacity="0.75" />
      <ellipse cx="18" cy="11" rx="4.8" ry="8" fill="currentColor" opacity="0.75"
        transform="rotate(72 18 18)" />
      <ellipse cx="18" cy="11" rx="4.8" ry="8" fill="currentColor" opacity="0.75"
        transform="rotate(144 18 18)" />
      <ellipse cx="18" cy="11" rx="4.8" ry="8" fill="currentColor" opacity="0.75"
        transform="rotate(216 18 18)" />
      <ellipse cx="18" cy="11" rx="4.8" ry="8" fill="currentColor" opacity="0.75"
        transform="rotate(288 18 18)" />
      {/* White centre disc */}
      <circle cx="18" cy="18" r="6" fill="white" />
      {/* Inner centre tint */}
      <circle cx="18" cy="18" r="3.2" fill="currentColor" opacity="0.22" />
      {/* Stamens */}
      <circle cx="18"   cy="14.2" r="1.1" fill="currentColor" opacity="0.8" />
      <circle cx="21.5" cy="20.5" r="1.1" fill="currentColor" opacity="0.8" />
      <circle cx="14.5" cy="20.5" r="1.1" fill="currentColor" opacity="0.8" />
    </svg>
  );
}

/** N1 — Crown: pinnacle mastery */
function CrownIcon() {
  return (
    <svg width="40" height="40" viewBox="0 0 36 36" fill="none" aria-hidden="true">
      {/* Crown body */}
      <path d="M4 28 L4 16 L11 23 L18 8 L25 23 L32 16 L32 28 Z"
        fill="currentColor" opacity="0.82" />
      {/* Base band */}
      <rect x="4" y="25" width="28" height="6" rx="3" fill="currentColor" />
      {/* Top-peak gem */}
      <circle cx="18" cy="9"  r="3.2" fill="white" opacity="0.90" />
      {/* Side-peak gems */}
      <circle cx="4.5"  cy="17" r="2.4" fill="white" opacity="0.72" />
      <circle cx="31.5" cy="17" r="2.4" fill="white" opacity="0.72" />
      {/* Band decorations */}
      <circle cx="12" cy="28" r="1.6" fill="white" opacity="0.70" />
      <circle cx="18" cy="28" r="1.6" fill="white" opacity="0.70" />
      <circle cx="24" cy="28" r="1.6" fill="white" opacity="0.70" />
    </svg>
  );
}

/* ─────────────────────────────────────────────
   JLPT level data
───────────────────────────────────────────── */
const JLPT_LEVELS = [
  {
    key: 'N5',
    Icon: SproutIcon,
    desc: 'Sơ cấp',
    detail: 'Hiragana, Katakana & 800 từ vựng nền tảng',
    kanji: 'あ',
    bg: '#E8F5E9',
    color: '#2E7D32',
    glow: 'rgba(46,125,50,0.16)',
  },
  {
    key: 'N4',
    Icon: BookIcon,
    desc: 'Cơ bản',
    detail: '1.500 từ vựng & giao tiếp hàng ngày',
    kanji: '字',
    bg: '#E3F2FD',
    color: '#1565C0',
    glow: 'rgba(21,101,192,0.16)',
  },
  {
    key: 'N3',
    Icon: ToriiIcon,
    desc: 'Trung cấp',
    detail: '3.000 từ vựng & đọc hiểu văn bản thực tế',
    kanji: '語',
    bg: '#FFF3E0',
    color: '#E65100',
    glow: 'rgba(230,81,0,0.16)',
  },
  {
    key: 'N2',
    Icon: BlossomIcon,
    desc: 'Cao cấp',
    detail: '6.000 từ vựng & phân tích văn bản phức tạp',
    kanji: '道',
    bg: '#F3E5F5',
    color: '#6A1B9A',
    glow: 'rgba(106,27,154,0.16)',
  },
  {
    key: 'N1',
    Icon: CrownIcon,
    desc: 'Thành thạo',
    detail: 'Trình độ bản ngữ — báo chí, học thuật',
    kanji: '極',
    bg: '#FCE4EC',
    color: '#C62828',
    glow: 'rgba(198,40,40,0.16)',
  },
];

/* ─────────────────────────────────────────────
   Bookshelf illustration — hero figure (back view)
   walking into a sakura-pink glow between books
───────────────────────────────────────────── */
function BookshelfSVG() {
  return (
    <svg
      width="112"
      height="90"
      viewBox="0 0 112 90"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
      className="cpb-shelf-svg"
    >
      <defs>
        <radialGradient id="cpb-pg" cx="50%" cy="55%" r="50%">
          <stop offset="0%"   stopColor="#FFFFFF" />
          <stop offset="48%"  stopColor="#FFF0F3" />
          <stop offset="100%" stopColor="#F7CBD4" stopOpacity="0" />
        </radialGradient>
      </defs>

      {/* Shelf plank */}
      <rect x="3" y="70" width="106" height="8" rx="3" fill="#C49A6C" />
      <rect x="3" y="70" width="106" height="3" rx="2" fill="#A0785A" />

      {/* Portal glow — rendered first so books overlay edges */}
      <ellipse cx="56" cy="47" rx="19" ry="25" fill="url(#cpb-pg)" />

      {/* ── Left books ── */}
      {/* L1 — Sakura Pink, tall */}
      <rect x="5"  y="22" width="14" height="49" rx="2.5" fill="#E89AAA" />
      <rect x="5"  y="22" width="4"  height="49" rx="2"   fill="#D47A8A" />
      <line x1="7"  y1="34" x2="18" y2="34" stroke="white" strokeWidth="0.9" strokeOpacity="0.45" />
      <line x1="7"  y1="46" x2="18" y2="46" stroke="white" strokeWidth="0.9" strokeOpacity="0.45" />

      {/* L2 — Gold, medium */}
      <rect x="21" y="36" width="11" height="35" rx="2.5" fill="#F7C948" />
      <rect x="21" y="36" width="3.5" height="35" rx="2"  fill="#DDA830" />
      <line x1="23" y1="48" x2="31" y2="48" stroke="white" strokeWidth="0.9" strokeOpacity="0.45" />

      {/* L3 — Emerald, short */}
      <rect x="34" y="48" width="10" height="23" rx="2.5" fill="#5DBB69" />
      <rect x="34" y="48" width="3"  height="23" rx="2"   fill="#48A053" />

      {/* ── Right books ── */}
      {/* R1 — Wisteria, tall */}
      <rect x="68" y="20" width="14" height="51" rx="2.5" fill="#CE93D8" />
      <rect x="68" y="20" width="4"  height="51" rx="2"   fill="#B478C2" />
      <line x1="70" y1="33" x2="81" y2="33" stroke="white" strokeWidth="0.9" strokeOpacity="0.45" />
      <line x1="70" y1="46" x2="81" y2="46" stroke="white" strokeWidth="0.9" strokeOpacity="0.45" />

      {/* R2 — Coral, medium */}
      <rect x="84" y="37" width="11" height="34" rx="2.5" fill="#F4A261" />
      <rect x="84" y="37" width="3.5" height="34" rx="2"  fill="#DD8A4C" />
      <line x1="86" y1="49" x2="94" y2="49" stroke="white" strokeWidth="0.9" strokeOpacity="0.45" />

      {/* R3 — Sakura, short */}
      <rect x="97" y="49" width="11" height="22" rx="2.5" fill="#E89AAA" opacity="0.78" />
      <rect x="97" y="49" width="3.5" height="22" rx="2"  fill="#D47A8A" opacity="0.78" />

      {/* ── Hero figure — back view, stepping into the portal ── */}
      {/* Ground shadow */}
      <ellipse cx="56" cy="71" rx="8" ry="2.2" fill="#D84F68" opacity="0.13" />

      {/* Legs — slight stride */}
      <rect x="52"   y="62" width="4.5" height="9"  rx="2.2" fill="#C8A07A" />
      <rect x="57.5" y="61" width="4.5" height="10" rx="2.2" fill="#B8906A" />

      {/* Body — sakura-pink shirt */}
      <rect x="49.5" y="50" width="13" height="13" rx="3.5" fill="#E89AAA" />

      {/* Backpack (randoseru style) */}
      <rect x="53.5" y="50.5" width="7.5" height="10.5" rx="2.5" fill="#D84F68" />
      <rect x="55.5" y="54"   width="3.5" height="3.5"  rx="1.2" fill="#C03060" opacity="0.55" />
      <line x1="53.5" y1="52" x2="51.5" y2="55.5"
        stroke="#C03060" strokeWidth="1.2" strokeOpacity="0.45" strokeLinecap="round" />
      <line x1="61"   y1="52" x2="63"   y2="55.5"
        stroke="#C03060" strokeWidth="1.2" strokeOpacity="0.45" strokeLinecap="round" />

      {/* Arms */}
      <ellipse cx="48.5" cy="55" rx="2.5" ry="4.5"
        fill="#E89AAA" transform="rotate(-7 48.5 55)" />
      <ellipse cx="63.5" cy="55" rx="2.5" ry="4.5"
        fill="#E89AAA" transform="rotate(7 63.5 55)" />

      {/* Neck */}
      <rect x="54" y="46" width="4" height="5" rx="2" fill="#C8A07A" />

      {/* Head */}
      <circle cx="56" cy="41" r="6" fill="#C8A07A" />

      {/* Hair — back-of-head cap */}
      <path d="M50 39 Q51 32 56 31 Q61 32 62 39 L62 42 Q59 40.5 56 40.5 Q53 40.5 50 42 Z"
        fill="#3D2B1F" />

      {/* Floating sakura petals */}
      <ellipse cx="44" cy="27" rx="3"   ry="1.7"
        fill="#E89AAA" opacity="0.60" transform="rotate(-22 44 27)" />
      <ellipse cx="68" cy="20" rx="2.3" ry="1.4"
        fill="#F7CBD4" opacity="0.70" transform="rotate(16 68 20)" />
      <ellipse cx="40" cy="58" rx="2"   ry="1.2"
        fill="#E89AAA" opacity="0.45" transform="rotate(-38 40 58)" />
      <ellipse cx="73" cy="54" rx="1.9" ry="1.1"
        fill="#F7CBD4" opacity="0.55" transform="rotate(28 73 54)" />
      <ellipse cx="63" cy="31" rx="1.8" ry="1.1"
        fill="#F7CBD4" opacity="0.50" transform="rotate(-12 63 31)" />
    </svg>
  );
}

/* ─────────────────────────────────────────────
   Main component
───────────────────────────────────────────── */
function CoursePickerBanner() {
  const dispatch      = useAppDispatch();
  const selectedLevel = useAppSelector((s) => s.student.selectedLevel);
  const [isOpen, setIsOpen] = useState(false);
  const wrapperRef = useRef(null);

  const selected = JLPT_LEVELS.find((l) => l.key === selectedLevel) ?? JLPT_LEVELS[0];

  useEffect(() => {
    if (!isOpen) return;
    function onOutside(e) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) setIsOpen(false);
    }
    document.addEventListener('mousedown', onOutside);
    return () => document.removeEventListener('mousedown', onOutside);
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) return;
    function onKey(e) { if (e.key === 'Escape') setIsOpen(false); }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [isOpen]);

  function handleSelect(levelKey) {
    dispatch(setSelectedLevel(levelKey));
    setIsOpen(false);
  }

  return (
    <div className="cpb-wrapper" ref={wrapperRef}>
      {/* ── Main card ── */}
      <div className={`cpb-card${isOpen ? ' cpb-card--open' : ''}`}>
        <span className="cpb-petal cpb-petal--1" aria-hidden="true"><SakuraIcon size={16} /></span>
        <span className="cpb-petal cpb-petal--2" aria-hidden="true"><SakuraIcon size={16} /></span>

        <div className="cpb-icon">
          <BookshelfSVG />
        </div>

        <div className="cpb-body">
          <p className="cpb-eyebrow">Đang học</p>
          <h2 className="cpb-title">DANH SÁCH KHÓA HỌC</h2>
          <div className="cpb-level-row">
            <span
              className="cpb-level-badge"
              style={{ background: selected.bg, color: selected.color }}
            >
              {selected.key}
            </span>
            <span className="cpb-level-name">{selected.desc}</span>
            <span className="cpb-level-detail">— {selected.detail}</span>
          </div>
        </div>

        <button
          type="button"
          className={`cpb-arrow-btn${isOpen ? ' cpb-arrow-btn--open' : ''}`}
          onClick={() => setIsOpen((v) => !v)}
          aria-expanded={isOpen}
          aria-haspopup="listbox"
          aria-label={isOpen ? 'Đóng danh sách cấp độ' : 'Mở danh sách cấp độ'}
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M5 9l7 6 7-6" stroke="white" strokeWidth="2.5"
              strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
      </div>

      {/* ── Dropdown panel ── */}
      {isOpen && (
        <div className="cpb-dropdown" role="listbox" aria-label="Chọn cấp độ JLPT">
          <span className="cpb-dp-petal cpb-dp-petal--1" aria-hidden="true"><SakuraIcon size={16} /></span>
          <span className="cpb-dp-petal cpb-dp-petal--2" aria-hidden="true"><SakuraIcon size={16} /></span>
          <span className="cpb-dp-petal cpb-dp-petal--3" aria-hidden="true"><SakuraIcon size={16} /></span>

          <div className="cpb-dp-header">
            <span className="cpb-dp-header-icon" aria-hidden="true"><SakuraIcon size={16} /></span>
            <span className="cpb-dp-header-title">Chọn lộ trình học của bạn</span>
            <div className="cpb-dp-header-line" aria-hidden="true" />
          </div>

          <div className="cpb-dropdown-grid">
            {JLPT_LEVELS.map((level) => {
              const isActive = selectedLevel === level.key;
              return (
                <button
                  key={level.key}
                  type="button"
                  role="option"
                  aria-selected={isActive}
                  className={`cpb-lv-card${isActive ? ' cpb-lv-card--active' : ''}`}
                  style={{
                    '--lv-bg':    level.bg,
                    '--lv-color': level.color,
                    '--lv-glow':  level.glow,
                  }}
                  onClick={() => handleSelect(level.key)}
                >
                  {/* Kanji watermark */}
                  <span className="cpb-lv-watermark" aria-hidden="true">{level.kanji}</span>

                  {/* Active chip */}
                  {isActive && (
                    <span className="cpb-lv-active-chip" aria-hidden="true">
                      <svg width="9" height="9" viewBox="0 0 10 10" fill="none">
                        <path d="M2 5l2.5 2.5L8 3" stroke="currentColor" strokeWidth="1.8"
                          strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                      Đang học
                    </span>
                  )}

                  {/* SVG icon — inherits currentColor from the card */}
                  <span className="cpb-lv-icon" aria-hidden="true">
                    <level.Icon />
                  </span>

                  <span className="cpb-lv-key">{level.key}</span>
                  <span className="cpb-lv-badge-pill">{level.desc}</span>
                  <span className="cpb-lv-detail">{level.detail}</span>
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

export default CoursePickerBanner;
