import './Pagination.css';

/**
 * Props:
 *   currentPage  — 1-based
 *   totalPages
 *   onChange(page)
 */
export function Pagination({ currentPage, totalPages, onChange }) {
  if (totalPages <= 1) return null;

  const visibleCount = Math.min(totalPages, 7);
  const pages = Array.from({ length: visibleCount }, (_, i) => {
    if (totalPages <= 7)            return i + 1;
    if (currentPage <= 4)           return i + 1;
    if (currentPage >= totalPages - 3) return totalPages - 6 + i;
    return currentPage - 3 + i;
  });

  return (
    <div className="pagination">
      <button
        type="button"
        className="pg-btn"
        onClick={() => onChange(Math.max(1, currentPage - 1))}
        disabled={currentPage === 1}
        aria-label="Trang trước"
      >
        ‹
      </button>

      {pages.map((n) => (
        <button
          key={n}
          type="button"
          className={`pg-btn${n === currentPage ? ' pg-btn--on' : ''}`}
          onClick={() => onChange(n)}
          aria-current={n === currentPage ? 'page' : undefined}
        >
          {n}
        </button>
      ))}

      <button
        type="button"
        className="pg-btn"
        onClick={() => onChange(Math.min(totalPages, currentPage + 1))}
        disabled={currentPage === totalPages}
        aria-label="Trang sau"
      >
        ›
      </button>
    </div>
  );
}
