import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import FlashcardCard from '../../components/student/FlashcardCard';
import {
  fetchDueCardsThunk,
  revealCardThunk,
  rateCardThunk,
  resetSession,
} from '../../store/slices/flashcardSlice';
import './Review.css';

export default function Review() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { toasts, addToast, removeToast } = useToast();

  const {
    reviewQueue,
    reviewStatus,
    currentReviewIdx,
    isFlipped,
    backContent,
    revealStatus,
    rateStatus,
    isSessionDone,
    nextReviewDate,
    error,
  } = useSelector((s) => s.flashcard);

  useEffect(() => {
    dispatch(resetSession());
    dispatch(fetchDueCardsThunk(50));
  }, [dispatch]);

  async function handleFlip() {
    if (isFlipped) return;
    const card = reviewQueue[currentReviewIdx];
    const res = await dispatch(revealCardThunk(card.flashcardId));
    if (revealCardThunk.rejected.match(res)) addToast('error', res.payload);
  }

  async function handleRate(rating) {
    if (rateStatus === 'loading') return;
    const card = reviewQueue[currentReviewIdx];
    const isLastCardInSession = currentReviewIdx === reviewQueue.length - 1;
    const res = await dispatch(rateCardThunk({ flashcardId: card.flashcardId, rating, isLastCardInSession }));
    if (rateCardThunk.rejected.match(res)) addToast('error', res.payload);
  }

  const totalStart  = reviewQueue.length;
  const progress    = totalStart > 0 ? Math.round((currentReviewIdx / totalStart) * 100) : 0;
  const remaining   = totalStart - currentReviewIdx;
  const currentCard = reviewQueue[currentReviewIdx];
  const isLoading   = reviewStatus === 'loading';
  const isFetching  = revealStatus === 'loading';
  const isRating    = rateStatus === 'loading';

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

        {!isLoading && error && reviewStatus === 'failed' && (
          <div className="rev-error-banner" role="alert">
            <span>{error}</span>
            <button className="rev-retry-btn" onClick={() => dispatch(fetchDueCardsThunk(50))}>
              Thử lại
            </button>
          </div>
        )}

        {!isLoading && !error && !isSessionDone && reviewQueue.length === 0 && (
          <EmptyState
            title="Chưa có thẻ nào để ôn tập"
            subtitle="Thêm từ vựng hoặc Kanji vào Flashcard từ bài học để bắt đầu ôn tập."
            mascotVariant="idle"
            mascotSize={160}
          >
            <button className="rev-btn rev-btn--primary" onClick={() => navigate('/flashcard')}>
              Quản lý bộ thẻ
            </button>
            <button className="rev-btn rev-btn--ghost" onClick={() => navigate('/learn')}>
              Học bài mới
            </button>
          </EmptyState>
        )}

        {!isLoading && isSessionDone && totalStart > 0 && (
          <EmptyState
            title="Tuyệt vời! Hết thẻ hôm nay rồi 🎉"
            subtitle={
              nextReviewDate
                ? `Lần ôn tập tiếp theo: ${new Date(nextReviewDate).toLocaleDateString('vi-VN')}`
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

        {!isLoading && !isSessionDone && !error && currentCard && (
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
                  onClick={() => handleRate('WRONG')}
                  disabled={isRating}
                  aria-label="Không nhớ"
                >
                  <span className="rev-rate-icon" aria-hidden="true">✗</span>
                  Không nhớ
                </button>
                <button
                  className="rev-rate-btn rev-rate-btn--hard"
                  onClick={() => handleRate('HARD')}
                  disabled={isRating}
                  aria-label="Khó, nhớ mờ"
                >
                  <span className="rev-rate-icon" aria-hidden="true">△</span>
                  Khó
                </button>
                <button
                  className="rev-rate-btn rev-rate-btn--easy"
                  onClick={() => handleRate('EASY')}
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
