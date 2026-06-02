import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import FlashcardCard from '../../components/student/FlashcardCard';
import { getFlashcardsDue, revealFlashcard, rateFlashcard } from '../../api/studentService';
import './Review.css';

export default function Review() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [queue,      setQueue]    = useState([]);
  const [currentIdx, setIdx]      = useState(0);
  const [isFlipped,  setFlipped]  = useState(false);
  const [backContent,setBack]     = useState(null);
  const [isLoading,  setLoading]  = useState(true);
  const [isFetching, setFetch]    = useState(false);
  const [isRating,   setRating]   = useState(false);
  const [isDone,     setDone]     = useState(false);
  const [nextReview, setNext]     = useState(null);
  const [error,      setError]    = useState('');
  const [totalStart, setTotal]    = useState(0);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res   = await getFlashcardsDue(50);
        const cards = res.content ?? [];
        setQueue(cards);
        setTotal(cards.length);
        if (cards.length === 0) setDone(true);
      } catch {
        setError('Không thể tải bộ thẻ. Thử lại sau.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  async function handleFlip() {
    if (isFlipped) return;
    setFetch(true);
    try {
      const card = queue[currentIdx];
      const back = await revealFlashcard(card.flashcardId);
      setBack(back.backContent);
      setFlipped(true);
    } catch {
      addToast('error', 'Không thể tải nội dung thẻ. Thử lại.');
    } finally {
      setFetch(false);
    }
  }

  async function handleRate(rating) {
    if (isRating) return;
    setRating(true);
    try {
      const card = queue[currentIdx];
      const res  = await rateFlashcard(card.flashcardId, rating);
      const nextIdx = currentIdx + 1;
      if (nextIdx >= queue.length) {
        setDone(true);
        setNext(res.nextReviewDate ?? null);
      } else {
        setIdx(nextIdx);
        setFlipped(false);
        setBack(null);
      }
    } catch {
      addToast('error', 'Không thể lưu đánh giá. Thử lại.');
    } finally {
      setRating(false);
    }
  }

  const progress    = totalStart > 0 ? Math.round((currentIdx / totalStart) * 100) : 0;
  const remaining   = queue.length - currentIdx;
  const currentCard = queue[currentIdx];

  return (
    <div className="rev-page">
      <TopNav activeTab="review" />

      <main className="rev-body">
        {isLoading && (
          <div className="rev-loading" role="status" aria-label="Đang tải bộ thẻ">
            <div className="rev-spinner-lg" />
            <span>Đang tải bộ thẻ ôn tập...</span>
          </div>
        )}

        {!isLoading && error && (
          <div className="rev-error-banner" role="alert">
            <span>{error}</span>
            <button className="rev-retry-btn" onClick={() => window.location.reload()}>Thử lại</button>
          </div>
        )}

        {!isLoading && !error && !isDone && queue.length === 0 && (
          <EmptyState
            title="Chưa có thẻ nào để ôn tập"
            subtitle="Thêm từ vựng hoặc Kanji vào Flashcard từ bài học để bắt đầu ôn tập."
            mascotVariant="idle"
            mascotSize={160}
          >
            <button className="rev-btn rev-btn--primary" onClick={() => navigate('/flashcard')}>
              Quản lý bộ thẻ
            </button>
            <button className="rev-btn rev-btn--ghost" onClick={() => navigate('/learn/new')}>
              Học bài mới
            </button>
          </EmptyState>
        )}

        {!isLoading && isDone && totalStart > 0 && (
          <EmptyState
            title="Tuyệt vời! Hết thẻ hôm nay rồi 🎉"
            subtitle={
              nextReview
                ? `Lần ôn tập tiếp theo: ${new Date(nextReview).toLocaleDateString('vi-VN')}`
                : 'Bạn đã ôn tập xong bộ thẻ hôm nay!'
            }
            mascotVariant="celebrate"
            mascotSize={180}
          >
            <button className="rev-btn rev-btn--primary" onClick={() => navigate('/dashboard')}>
              Về Dashboard
            </button>
            <button className="rev-btn rev-btn--ghost" onClick={() => navigate('/flashcard')}>
              Xem bộ thẻ
            </button>
          </EmptyState>
        )}

        {!isLoading && !isDone && !error && currentCard && (
          <>
            <div className="rev-header">
              <span className="rev-title">Ôn tập hôm nay</span>
              <span className="rev-counter" aria-live="polite">{remaining} thẻ còn lại</span>
            </div>

            <ProgressBar value={progress} />

            <FlashcardCard
              card={currentCard}
              isFlipped={isFlipped}
              backContent={backContent}
              isFetching={isFetching}
              onFlip={handleFlip}
            />

            {isFlipped && (
              <div className="rev-rating-row" aria-label="Đánh giá mức nhớ">
                <button
                  className="rev-rate-btn rev-rate-btn--wrong"
                  onClick={() => handleRate('wrong')}
                  disabled={isRating}
                  aria-label="Không nhớ"
                >
                  <span className="rev-rate-icon" aria-hidden="true">✗</span>
                  Không nhớ
                </button>
                <button
                  className="rev-rate-btn rev-rate-btn--hard"
                  onClick={() => handleRate('hard')}
                  disabled={isRating}
                  aria-label="Khó, nhớ mờ"
                >
                  <span className="rev-rate-icon" aria-hidden="true">△</span>
                  Khó
                </button>
                <button
                  className="rev-rate-btn rev-rate-btn--easy"
                  onClick={() => handleRate('easy')}
                  disabled={isRating}
                  aria-label="Dễ, nhớ rõ"
                >
                  <span className="rev-rate-icon" aria-hidden="true">✓</span>
                  Dễ
                </button>
              </div>
            )}
          </>
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
