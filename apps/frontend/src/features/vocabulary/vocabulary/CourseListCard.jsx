/**
 * CourseListCard — shortcut tới /courses (FR-VH-09, SPEC §8.2).
 *
 * Props:
 *   onClick — handler điều hướng tới /courses.
 */
function ShelfIcon({ size = 22 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="3" y="4" width="4" height="14" rx="1.2" fill="currentColor" opacity="0.85" />
      <rect x="8.5" y="4" width="4" height="14" rx="1.2" fill="currentColor" opacity="0.55" />
      <rect x="13" y="6" width="4" height="12" rx="1.2" fill="currentColor" opacity="0.85"
        transform="rotate(12 15 12)" />
      <line x1="2" y1="19" x2="22" y2="19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

function ArrowIcon({ size = 20 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 6l6 6-6 6" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export default function CourseListCard({ onClick }) {
  return (
    <button type="button" className="vh-courselist" onClick={onClick} aria-label="Mở danh sách khoá học">
      <span className="vh-courselist-icon" aria-hidden="true">
        <ShelfIcon size={22} />
      </span>
      <span className="vh-courselist-label">Course List</span>
      <span className="vh-courselist-arrow" aria-hidden="true">
        <ArrowIcon size={20} />
      </span>
    </button>
  );
}
