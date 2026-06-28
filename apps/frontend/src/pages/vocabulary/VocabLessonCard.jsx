import SakuChan from '../../components/auth/SakuChan';
import { ProgressBar } from '../../components/common/ProgressBar';

/* Arrow → */
function ArrowIcon({ size = 20 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 6l6 6-6 6" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

/**
 * VocabLessonCard — 1 bài học (FR-VH-06/08/14).
 * Truy cập tự do: không khoá VIP, mọi bài đều mở.
 *
 * Props:
 *   lesson  — { topicId, slug, titleJp, subtitleEn, status, thumbnail,
 *              totalWords, learnedCount, masteredCount }
 *   onOpen  — (lesson) => void  (mở phiên flashcard ôn tập của chủ đề)
 */
export default function VocabLessonCard({ lesson, onOpen }) {
  const {
    titleJp, subtitleEn, status, thumbnail,
    totalWords = 0, learnedCount = 0, masteredCount = 0,
  } = lesson;

  const isActive = status === 'active';
  const stateClass = isActive ? 'vh-lesson--active' : 'vh-lesson--available';
  const label = `Bài: ${titleJp ?? ''} — ${subtitleEn ?? ''}`;

  return (
    <button
      type="button"
      className={`vh-lesson ${stateClass}`}
      onClick={() => onOpen?.(lesson)}
      aria-current={isActive ? 'true' : undefined}
      aria-label={label}
    >
      <span className="vh-lesson-thumb" aria-hidden="true">
        {thumbnail === 'saku-mascot' ? (
          <SakuChan variant={isActive ? 'happy' : 'idle'} size={44} />
        ) : (
          <span className="vh-lesson-thumb-char" lang="ja">
            {thumbnail || (titleJp ? titleJp.charAt(0) : '語')}
          </span>
        )}
      </span>

      <span className="vh-lesson-content">
        {titleJp && <span className="vh-lesson-title" lang="ja">{titleJp}</span>}
        {subtitleEn && <span className="vh-lesson-sub">{subtitleEn}</span>}
        {totalWords > 0 && (
          <span className="vh-lesson-progress">
            <ProgressBar
              value={learnedCount}
              max={totalWords}
              height={6}
              label={`Đã học ${learnedCount}/${totalWords} từ`}
            />
            <span className="vh-lesson-progress-text">
              {learnedCount}/{totalWords} đã học
              {masteredCount > 0 && ` · ${masteredCount} thành thạo`}
            </span>
          </span>
        )}
      </span>

      <span className="vh-lesson-trail" aria-hidden="true">
        <ArrowIcon size={20} />
      </span>
    </button>
  );
}
