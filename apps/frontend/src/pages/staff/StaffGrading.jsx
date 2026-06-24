import { useState, useEffect, useCallback } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { useToast, ToastContainer } from '../../components/common/Toast';
import SubmissionList from '../../components/staff/SubmissionList';
import GradingPanel from '../../components/staff/GradingPanel';
import StaffPageHero from '../../components/staff/StaffPageHero';
import { getSpeakingSubmissions, getSubmissionDetail, gradeSubmission } from '../../api/staffService';
import './StaffGrading.css';

export default function StaffGrading() {
  const [submissions, setSubmissions] = useState([]);
  const [detail, setDetail] = useState(null);
  const [selectedId, setSelected] = useState(null);
  const [statusTab, setTab] = useState('pending');
  const [pendingCount, setPendingCount] = useState(0);
  const [isLoadingList, setLoadingList] = useState(true);
  const { toasts, addToast, removeToast } = useToast();

  const loadSubmissions = useCallback(async () => {
    setLoadingList(true);
    try {
      const status = statusTab === 'pending' ? 'ai_graded' : 'graded';
      const res = await getSpeakingSubmissions({ status });
      setSubmissions(res.content ?? []);
      if (statusTab === 'pending') setPendingCount((res.content ?? []).length);
    } catch {
      addToast('error', 'Không thể tải danh sách bài nộp.');
    } finally {
      setLoadingList(false);
    }
  }, [statusTab, addToast]);

  useEffect(() => {
    loadSubmissions();
  }, [loadSubmissions]);

  useEffect(() => {
    if (!selectedId) {
      setDetail(null);
      return;
    }
    let active = true;
    getSubmissionDetail(selectedId)
      .then((d) => {
        if (active) setDetail(d);
      })
      .catch(() => {
        if (active) addToast('error', 'Không thể tải chi tiết bài nộp.');
      });
    return () => {
      active = false;
    };
  }, [selectedId, addToast]);

  const selectedSubmission = submissions.find((s) => s.submissionId === selectedId) ?? null;

  async function handleGrade(submissionId, manualScore, feedback) {
    try {
      const scoreToSend = manualScore !== '' ? manualScore : detail?.aiOverallScore;
      await gradeSubmission(submissionId, { manualScore: scoreToSend, manualFeedback: feedback });
      const refreshed = await getSubmissionDetail(submissionId);
      setDetail(refreshed);
      setSubmissions((prev) => prev.filter((s) => s.submissionId !== submissionId));
      setPendingCount((prev) => Math.max(0, prev - 1));

      addToast('success', 'Đã lưu điểm. Học viên sẽ nhận thông báo kết quả.');

      const nextPending = submissions.find((s) => s.submissionId !== submissionId);
      setSelected(nextPending ? nextPending.submissionId : null);
    } catch {
      addToast('error', 'Không thể lưu điểm. Vui lòng thử lại.');
    }
  }

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

      <main className="grd-body">
        {isLoadingList ? (
          <div className="grd-empty-list">Đang tải...</div>
        ) : (
          <SubmissionList
            submissions={submissions}
            selectedId={selectedId}
            statusTab={statusTab}
            pendingCount={pendingCount}
            onSelect={setSelected}
            onTabChange={setTab}
          />
        )}
        <GradingPanel
          submission={selectedSubmission}
          detail={detail}
          onGrade={handleGrade}
        />
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
