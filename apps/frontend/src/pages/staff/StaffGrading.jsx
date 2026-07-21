import { useState, useEffect, useCallback } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { useToast, ToastContainer } from '../../components/common/Toast';
import SubmissionList from '../../components/staff/SubmissionList';
import GradingPanel from '../../components/staff/GradingPanel';
import StaffPageHero from '../../components/staff/StaffPageHero';
import {
  getSpeakingSubmissions,
  getSpeakingSubmissionDetail,
  gradeSpeakingSubmission,
} from '../../api/staffService';
import './StaffGrading.css';

export default function StaffGrading() {
  const [submissions, setSubmissions] = useState([]);
  const [details, setDetails] = useState({}); // cache: submissionId -> detail
  const [selectedId, setSelected] = useState(null);
  const [statusTab, setTab] = useState('pending');
  const [isLoading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isGrading, setGrading] = useState(false);
  const { toasts, addToast, removeToast } = useToast();

  // ── Tải danh sách bài nộp speaking ──
  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError('');
    getSpeakingSubmissions({ size: 100 })
      .then((data) => {
        if (alive) setSubmissions(data?.content ?? []);
      })
      .catch((err) => {
        if (alive) setError(err?.response?.data?.message ?? 'Không thể tải danh sách bài nộp.');
      })
      .finally(() => {
        if (alive) setLoading(false);
      });
    return () => {
      alive = false;
    };
  }, []);

  // ── Tải chi tiết khi chọn (nếu chưa có cache) ──
  useEffect(() => {
    if (selectedId == null || details[selectedId]) return;
    let alive = true;
    getSpeakingSubmissionDetail(selectedId)
      .then((data) => {
        if (alive) setDetails((prev) => ({ ...prev, [selectedId]: data }));
      })
      .catch((err) => {
        if (alive) addToast('error', err?.response?.data?.message ?? 'Không thể tải chi tiết bài nộp.');
      });
    return () => {
      alive = false;
    };
  }, [selectedId, details, addToast]);

  const pendingCount = submissions.filter((s) => s.status === 'ai_graded' || s.status === 'pending').length;

  const filtered =
    statusTab === 'pending'
      ? submissions.filter((s) => s.status === 'ai_graded' || s.status === 'pending')
      : submissions.filter((s) => s.status === 'graded');

  const selectedSubmission = submissions.find((s) => s.submissionId === selectedId) ?? null;
  const selectedDetail = selectedId != null ? (details[selectedId] ?? null) : null;

  const handleGrade = useCallback(
    async (submissionId, manualScoreStr, feedback) => {
      if (isGrading) return;
      const score = parseFloat(manualScoreStr);
      if (isNaN(score)) return; // GradingPanel đã bắt buộc nhập điểm hợp lệ

      setGrading(true);
      try {
        await gradeSpeakingSubmission(submissionId, { manualScore: score, manualFeedback: feedback });
        // Lấy lại chi tiết đầy đủ (GradeResponse thiếu vài trường AI/level) để panel readonly hiển thị đúng.
        const fresh = await getSpeakingSubmissionDetail(submissionId);
        setDetails((prev) => ({ ...prev, [submissionId]: fresh }));
        setSubmissions((prev) =>
          prev.map((s) =>
            s.submissionId === submissionId
              ? { ...s, status: fresh.status, manualScore: fresh.manualScore, finalScore: fresh.finalScore }
              : s
          )
        );
        addToast('success', 'Đã lưu điểm. Học viên sẽ nhận thông báo kết quả.');

        const nextPending = submissions.find(
          (s) => s.submissionId !== submissionId && (s.status === 'ai_graded' || s.status === 'pending')
        );
        setSelected(nextPending ? nextPending.submissionId : null);
      } catch (err) {
        addToast('error', err?.response?.data?.message ?? 'Không thể lưu điểm. Vui lòng thử lại.');
      } finally {
        setGrading(false);
      }
    },
    [isGrading, submissions, addToast]
  );

  return (
    <div className="grd-page">
      <StaffTopNav activeTab="staff-grading" />

      <StaffPageHero
        accent="gold"
        title="Chấm Bài Nói"
        subtitle="Xem xét và chấm điểm thủ công các bài luyện speaking đã được AI sơ chấm"
        icon={
          <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            {/* Quạt xếp (扇子) */}
            <line x1="24" y1="38" x2="6" y2="16"/>
            <line x1="24" y1="38" x2="13" y2="9"/>
            <line x1="24" y1="38" x2="24" y2="7"/>
            <line x1="24" y1="38" x2="35" y2="9"/>
            <line x1="24" y1="38" x2="42" y2="16"/>
            <path d="M6 16 Q15 7 24 7 Q33 7 42 16"/>
            <circle cx="24" cy="38" r="2.5" fill="currentColor"/>
            <line x1="24" y1="40.5" x2="24" y2="44"/>
          </svg>
        }
      />

      <div className="grd-header-bar">
        <h1 className="grd-page-title">Chấm Bài Nói</h1>
        {pendingCount > 0 && (
          <span className="grd-pending-badge" aria-label={`${pendingCount} bài đang chờ chấm điểm`}>
            {pendingCount} chờ chấm
          </span>
        )}
      </div>

      {error && (
        <div className="grd-error-bar" role="alert" style={{ margin: '0 24px 12px', color: 'var(--color-error)' }}>
          {error}
        </div>
      )}

      <main className="grd-body">
        <SubmissionList
          submissions={filtered}
          selectedId={selectedId}
          statusTab={statusTab}
          pendingCount={pendingCount}
          isLoading={isLoading}
          onSelect={setSelected}
          onTabChange={setTab}
        />
        <GradingPanel
          submission={selectedSubmission}
          detail={selectedDetail}
          onGrade={handleGrade}
          isGrading={isGrading}
        />
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
