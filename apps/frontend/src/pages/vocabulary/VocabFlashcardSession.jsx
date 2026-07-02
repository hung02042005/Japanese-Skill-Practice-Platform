import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ProgressBar } from '../../components/common/ProgressBar';
import { EmptyState } from '../../components/common/EmptyState';
import { NotebookIcon } from '../../components/student/StudentIcons';
import { SproutIcon, RepeatIcon, SpeakerIcon } from '../../components/common/AppIcons';
import { ToastContainer, useToast } from '../../components/common/Toast';
import {
  getVocabFlashcardSession,
  submitFlashcardReview,
  addWrongWordsToReviewDeck,
} from '../../api/studentService';
import './VocabFlashcardSession.css';

/**
 * Phiên học từ vựng dạng Flashcard SRS (§3.6/§3.7) — thay cho danh sách phẳng.
 * Thẻ MỚI 2 mặt: mặt trước = từ + furigana, lật ra mặt sau = nghĩa + ví dụ + audio,
 * rồi bấm "Tiếp theo" để học từ kế (không tự đánh giá dễ/khó). Thẻ ÔN TẬP = trắc nghiệm chọn nghĩa.
 * Bấm "Tiếp theo" → thẻ kế luôn bắt đầu ở mặt trước.
 */
export default function VocabFlashcardSession() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAppSelector((s) => s.auth);
  const { toasts, addToast, removeToast } = useToast();

  const deckId  = searchParams.get('deckId');
  const topicId = searchParams.get('topicId');
  const level   = searchParams.get('level') ?? user?.jlptLevel ?? 'N5';

  const [status,  setStatus]  = useState('loading'); // loading | ready | error
  const [error,   setError]   = useState('');
  const [session, setSession] = useState(null);
  const [idx,     setIdx]     = useState(0);

  // Trạng thái 1 thẻ
  const [revealed,   setRevealed]   = useState(false); // thẻ từ: đã lật xem mặt sau chưa
  const [selected,   setSelected]   = useState(null);  // thẻ bài tập: optionId đã chọn
  const [result,     setResult]     = useState(null);  // ReviewResult sau khi đánh giá
  const [submitting, setSubmitting] = useState(false);

  // Tổng kết phiên
  const [done,        setDone]        = useState(false);
  const [correctCnt,  setCorrectCnt]  = useState(0);
  const [finalResult, setFinalResult] = useState(null);
  const [addingWrong, setAddingWrong] = useState(false);
  const [wrongAdded,  setWrongAdded]  = useState(false);

  const loadSession = useCallback(async () => {
    setStatus('loading');
    setError('');
    try {
      const data = await getVocabFlashcardSession(
        deckId ? { deckId: Number(deckId) } : { topicId: Number(topicId) },
      );
      setSession(data);
      setIdx(0);
      setRevealed(false);
      setSelected(null);
      setResult(null);
      setDone(false);
      setCorrectCnt(0);
      setFinalResult(null);
      setWrongAdded(false);
      setStatus('ready');
    } catch (err) {
      if (err?.response?.status === 401) { navigate('/login'); return; }
      setError(
        err?.response?.status === 403 || err?.response?.status === 422
          ? 'Cấp độ này chưa mở khóa hoặc cần nâng cấp tài khoản.'
          : (err?.response?.data?.message ?? 'Không thể tải phiên học flashcard.'),
      );
      setStatus('error');
    }
  }, [deckId, topicId, navigate]);

  useEffect(() => { loadSession(); }, [loadSession]);

  // Thứ tự thẻ do backend quyết định — học 2–3 thẻ MỚI rồi kiểm tra ngay các từ đó (§3.6).
  const queue   = session?.queue ?? [];
  const total   = queue.length;
  // Mỗi từ xuất hiện 2 lần (học + kiểm tra); điểm tính trên các thẻ ÔN TẬP (trắc nghiệm).
  const quizTotal = queue.filter((c) => c.stage !== 'NEW').length;
  const card    = queue[idx];
  const isLast  = idx === total - 1;
  const isNew   = card?.stage === 'NEW';

  // Thẻ từ (NEW): chỉ lật học nghĩa rồi đi tiếp — KHÔNG chấm điểm/ghi SM-2 ở đây.
  // Việc ghi nhận đúng/sai do thẻ ÔN TẬP (trắc nghiệm) của cùng từ đó (ngay sau) đảm nhiệm.

  // Thẻ bài tập (REVIEW): chọn 1 trong 2 đáp án → server chấm đúng/sai.
  async function handleAnswer(optionId) {
    if (submitting || result) return;
    setSelected(optionId);
    setSubmitting(true);
    try {
      const res = await submitFlashcardReview(card.flashcardId, {
        selectedOptionId: optionId,
        isLastCardInSession: isLast,
        sessionId: session?.sessionId,   // V17: đóng dấu phiên để gom đúng từ sai cuối phiên
      });
      setResult(res);
      if (res.correct) setCorrectCnt((c) => c + 1);
      if (isLast) setFinalResult(res);
    } catch (err) {
      setSelected(null);
      addToast('error', err?.response?.data?.message ?? 'Không thể lưu kết quả. Thử lại nhé.');
    } finally {
      setSubmitting(false);
    }
  }

  function handleNext() {
    if (isLast) { setDone(true); return; }
    setIdx((i) => i + 1);
    setRevealed(false);   // thẻ kế luôn bắt đầu ở mặt trước
    setSelected(null);
    setResult(null);
  }

  async function handleAddWrong() {
    const words = finalResult?.wrongWords ?? [];
    if (words.length === 0) return;
    setAddingWrong(true);
    try {
      await addWrongWordsToReviewDeck(
        words.map((w) => ({ contentType: 'VOCABULARY', contentId: w.contentId })),
      );
      setWrongAdded(true);
      addToast('success', 'Đã thêm vào sổ "Từ cần ôn lại"!');
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Không thể thêm từ. Thử lại nhé.');
    } finally {
      setAddingWrong(false);
    }
  }

  function playAudio(url) {
    if (url) new Audio(url).play().catch(() => {});
  }

  const backToHub = () =>
    navigate(deckId ? '/notebook' : `/vocabulary?level=${level}`);

  return (
    <div className="vfs-page">
      <TopNav activeTab={deckId ? '' : 'vocabulary'} />
      <main className="vfs-body">
        <button type="button" className="vfs-back" onClick={backToHub}>
          {deckId ? '← Sổ tay' : '← Lộ trình từ vựng'}
        </button>

        <div className="vfs-head">
          <h1 className="vfs-title">
            {deckId ? (
              <span><NotebookIcon size={20} /> Từ cần ôn lại</span>
            ) : (
              <>
                <JlptBadge level={session?.level ?? level} />
                <span lang="ja">{session?.topicTitle || 'Từ vựng'}</span>
              </>
            )}
          </h1>
          {!deckId && (
            <a
              className="vfs-listlink"
              href={`/vocabulary?level=${level}&view=list`}
            >
              Xem danh sách từ
            </a>
          )}
        </div>

        {/* ── Loading ── */}
        {status === 'loading' && (
          <div className="vfs-loading" role="status" aria-label="Đang tải phiên học">
            <div className="vfs-spinner-lg" />
            <span>Đang chuẩn bị bộ thẻ...</span>
          </div>
        )}

        {/* ── Error ── */}
        {status === 'error' && (
          <div className="vfs-error" role="alert">
            <span>{error}</span>
            <button className="vfs-retry" onClick={loadSession}>Thử lại</button>
          </div>
        )}

        {/* ── Empty ── */}
        {status === 'ready' && total === 0 && (
          <EmptyState
            title="Chưa có từ để học hôm nay"
            subtitle="Chủ đề này chưa có từ vựng, hoặc bạn đã ôn hết các thẻ đến hạn. Quay lại sau nhé!"
            mascotVariant="idle"
            mascotSize={150}
          >
            <button className="vfs-btn vfs-btn--primary" onClick={backToHub}>Về lộ trình</button>
          </EmptyState>
        )}

        {/* ── Summary ── */}
        {status === 'ready' && total > 0 && done && (
          <div className="vfs-summary">
            <EmptyState
              title="Hoàn thành phiên học!"
              subtitle={`Bạn trả lời đúng ${correctCnt}/${quizTotal} câu.`}
              mascotVariant="celebrate"
              mascotSize={170}
            />
            {finalResult?.suggestAddToReviewDeck && (finalResult?.wrongWords?.length ?? 0) > 0 && (
              <div className="vfs-wrong-box">
                <p className="vfs-wrong-title">
                  Có {finalResult.wrongWords.length} từ bạn còn nhớ chưa chắc:
                </p>
                <div className="vfs-wrong-list" lang="ja">
                  {finalResult.wrongWords.map((w) => (
                    <span key={w.contentId} className="vfs-wrong-chip">{w.word}</span>
                  ))}
                </div>
                <button
                  className="vfs-btn vfs-btn--accent"
                  onClick={handleAddWrong}
                  disabled={addingWrong || wrongAdded}
                  aria-busy={addingWrong}
                >
                  {wrongAdded ? '✓ Đã thêm vào "Từ cần ôn lại"' : addingWrong ? 'Đang thêm…' : 'Thêm vào "Từ cần ôn lại"'}
                </button>
              </div>
            )}
            <div className="vfs-summary-actions">
              <button className="vfs-btn vfs-btn--primary" onClick={loadSession}>Học lại</button>
              <button className="vfs-btn vfs-btn--ghost" onClick={backToHub}>Về lộ trình</button>
            </div>
          </div>
        )}

        {/* ── Card ── */}
        {status === 'ready' && total > 0 && !done && card && (
          <>
            <div className="vfs-progress">
              <ProgressBar value={Math.round((idx / total) * 100)} />
              <div className="vfs-progress-meta">
                <span>{idx + 1} / {total}</span>
                <span className={`vfs-stage vfs-stage--${isNew ? 'new' : 'review'}`}>
                  {isNew ? <><SproutIcon size={14} /> Từ mới</> : <><RepeatIcon size={14} /> Ôn tập</>}
                </span>
              </div>
            </div>

            {isNew ? (
              /* ════ Thẻ từ: lật 3D kiểu Quizlet (mặt trước = từ, mặt sau = nghĩa) ════ */
              <>
                <div className="vfs-scene">
                  <div
                    className={`vfs-flip${revealed ? ' vfs-flip--flipped' : ''}`}
                    role="button"
                    tabIndex={0}
                    onClick={() => !result && setRevealed((r) => !r)}
                    onKeyDown={(e) => {
                      if (!result && (e.key === 'Enter' || e.key === ' ')) {
                        e.preventDefault();
                        setRevealed((r) => !r);
                      }
                    }}
                    aria-label={revealed ? 'Lật về mặt từ' : 'Lật thẻ xem nghĩa'}
                  >
                    {/* Mặt trước */}
                    <div className="vfs-face vfs-face--front" aria-hidden={revealed}>
                      <span className="vfs-word" lang="ja">{card.front.word}</span>
                      {card.front.furigana && (
                        <span className="vfs-furigana" lang="ja">{card.front.furigana}</span>
                      )}
                      <span className="vfs-fliphint">Chạm để lật thẻ ↻</span>
                    </div>

                    {/* Mặt sau */}
                    <div className="vfs-face vfs-face--back" aria-hidden={!revealed}>
                      {card.learn && (
                        <>
                          <div className="vfs-back-term">
                            <span className="vfs-back-word" lang="ja">{card.front.word}</span>
                            {card.front.furigana && (
                              <span className="vfs-back-reading" lang="ja">{card.front.furigana}</span>
                            )}
                          </div>
                          <p className="vfs-meaning">{card.learn.meaning}</p>
                          {card.learn.exampleJp && (
                            <div className="vfs-example">
                              <p lang="ja">{card.learn.exampleJp}</p>
                              {card.learn.exampleVi && (
                                <p className="vfs-example-vi">{card.learn.exampleVi}</p>
                              )}
                            </div>
                          )}
                          {card.learn.audioUrl && (
                            <button
                              type="button"
                              className="vfs-audio"
                              onClick={(e) => { e.stopPropagation(); playAudio(card.learn.audioUrl); }}
                              aria-label="Nghe phát âm"
                            >
                              <SpeakerIcon size={16} /> Phát âm
                            </button>
                          )}
                        </>
                      )}
                    </div>
                  </div>
                </div>

                {/* Sau khi lật: chỉ học nghĩa rồi đi tiếp — thẻ ÔN TẬP của từ này sẽ tới ngay sau */}
                {revealed && (
                  <div className="vfs-next-row">
                    <button className="vfs-btn vfs-btn--primary" onClick={handleNext}>
                      Tiếp theo →
                    </button>
                  </div>
                )}
              </>
            ) : (
              /* ════ Thẻ bài tập: hiện từ + 2 đáp án bên dưới ════ */
              <div className="vfs-card">
                <div className="vfs-front">
                  <span className="vfs-word" lang="ja">{card.front.word}</span>
                  {card.front.furigana && (
                    <span className="vfs-furigana" lang="ja">{card.front.furigana}</span>
                  )}
                </div>

                {card.quiz?.options?.length > 0 && (
                  <div className="vfs-quiz" role="group" aria-label="Chọn nghĩa đúng">
                    <p className="vfs-quiz-prompt">Chọn nghĩa đúng:</p>
                    {card.quiz.options.map((opt) => {
                      const isPicked  = selected === opt.optionId;
                      const isCorrect = result && opt.optionId === result.correctOptionId;
                      const isWrong   = result && isPicked && !result.correct;
                      return (
                        <button
                          key={opt.optionId}
                          className={`vfs-option${isCorrect ? ' vfs-option--correct' : ''}${isWrong ? ' vfs-option--wrong' : ''}`}
                          onClick={() => handleAnswer(opt.optionId)}
                          disabled={submitting || !!result}
                        >
                          {opt.meaning}
                          {isCorrect && <span className="vfs-mark" aria-hidden="true"> ✓</span>}
                          {isWrong && <span className="vfs-mark" aria-hidden="true"> ✗</span>}
                        </button>
                      );
                    })}
                  </div>
                )}

                {result && (
                  <div className={`vfs-feedback${result.correct ? ' vfs-feedback--ok' : ' vfs-feedback--no'}`} aria-live="polite">
                    <span className="vfs-feedback-text">
                      {result.correct ? 'Chính xác!' : `Chưa đúng — nghĩa đúng: ${result.correctMeaning ?? ''}`}
                    </span>
                    <button className="vfs-btn vfs-btn--primary" onClick={handleNext}>
                      {isLast ? 'Hoàn thành →' : 'Tiếp theo →'}
                    </button>
                  </div>
                )}
              </div>
            )}
          </>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
