import { useState } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { useToast, ToastContainer } from '../../components/common/Toast';
import SubmissionList from '../../components/staff/SubmissionList';
import GradingPanel from '../../components/staff/GradingPanel';
import './StaffGrading.css';

const MOCK_SUBMISSIONS = [
  { submissionId: 1, studentName: 'Nguyễn Văn A', jlptLevel: 'N3', durationSeconds: 89, submittedAt: '2026-06-03T14:30:00', status: 'ai_graded', aiOverallScore: 72.5 },
  { submissionId: 2, studentName: 'Trần Thị B', jlptLevel: 'N4', durationSeconds: 65, submittedAt: '2026-06-02T16:20:00', status: 'graded', aiOverallScore: 81.0, manualScore: 85.0, finalScore: 85.0 },
  { submissionId: 3, studentName: 'Lê Văn C', jlptLevel: 'N2', durationSeconds: 120, submittedAt: '2026-06-03T09:00:00', status: 'ai_graded', aiOverallScore: 68.0 },
  { submissionId: 4, studentName: 'Phạm Minh D', jlptLevel: 'N5', durationSeconds: 45, submittedAt: '2026-06-01T10:15:00', status: 'ai_graded', aiOverallScore: 55.0 },
  { submissionId: 5, studentName: 'Hoàng Thị E', jlptLevel: 'N4', durationSeconds: 78, submittedAt: '2026-05-31T14:00:00', status: 'graded', aiOverallScore: 76.0, manualScore: 80.0, finalScore: 80.0 },
];

const MOCK_DETAILS = {
  1: {
    submissionId: 1, studentName: 'Nguyễn Văn A', jlptLevel: 'N3',
    submittedAt: '2026-06-03T14:30:00', durationSeconds: 89, recordingUrl: null,
    aiOverallScore: 72.5, aiPronunciationScore: 68.0, aiFluencyScore: 77.0,
    aiHighlightedErrors: '"おはよう" — âm điệu kéo dài bất thường ở cuối từ; "ございます" — nhịp điệu chưa đều.',
    aiSuggestions: 'Chú ý ngắt âm dứt khoát hơn ở cuối câu. Luyện thêm nhịp điệu tự nhiên qua shadowing N3 Level 2.',
    manualScore: null, manualFeedback: null, finalScore: 72.5, status: 'ai_graded',
  },
  2: {
    submissionId: 2, studentName: 'Trần Thị B', jlptLevel: 'N4',
    submittedAt: '2026-06-02T16:20:00', durationSeconds: 65, recordingUrl: null,
    aiOverallScore: 81.0, aiPronunciationScore: 78.0, aiFluencyScore: 84.0,
    aiHighlightedErrors: 'Không phát hiện lỗi nghiêm trọng.',
    aiSuggestions: 'Phát âm tốt. Cần cải thiện thêm tốc độ nói để tự nhiên hơn.',
    manualScore: 85.0, manualFeedback: 'Bạn nói rất tự nhiên và lưu loát. Phát âm chuẩn, nhịp điệu đều. Cần chú ý thêm intonation ở câu hỏi. Tiếp tục cố gắng!',
    finalScore: 85.0, status: 'graded', gradedBy: 'Staff Minh', gradedAt: '2026-06-02T18:00:00',
  },
  3: {
    submissionId: 3, studentName: 'Lê Văn C', jlptLevel: 'N2',
    submittedAt: '2026-06-03T09:00:00', durationSeconds: 120, recordingUrl: null,
    aiOverallScore: 68.0, aiPronunciationScore: 65.0, aiFluencyScore: 71.0,
    aiHighlightedErrors: '"難しい" — phát âm sai thanh điệu; nhiều khoảng dừng quá dài.',
    aiSuggestions: 'Tập trung vào pitch accent của N2. Cố gắng giảm hesitation fillers (えー、あの).',
    manualScore: null, manualFeedback: null, finalScore: 68.0, status: 'ai_graded',
  },
  4: {
    submissionId: 4, studentName: 'Phạm Minh D', jlptLevel: 'N5',
    submittedAt: '2026-06-01T10:15:00', durationSeconds: 45, recordingUrl: null,
    aiOverallScore: 55.0, aiPronunciationScore: 52.0, aiFluencyScore: 58.0,
    aiHighlightedErrors: 'Nhiều âm không rõ. Tốc độ quá chậm.',
    aiSuggestions: 'Cần luyện phát âm cơ bản N5 thêm. Nghe và lặp lại nhiều hơn.',
    manualScore: null, manualFeedback: null, finalScore: 55.0, status: 'ai_graded',
  },
  5: {
    submissionId: 5, studentName: 'Hoàng Thị E', jlptLevel: 'N4',
    submittedAt: '2026-05-31T14:00:00', durationSeconds: 78, recordingUrl: null,
    aiOverallScore: 76.0, aiPronunciationScore: 74.0, aiFluencyScore: 78.0,
    aiHighlightedErrors: 'Một số từ phát âm chưa đủ rõ ở cuối câu.',
    aiSuggestions: 'Tăng cường luyện âm cuối câu.',
    manualScore: 80.0, manualFeedback: 'Phát âm khá tốt, lưu loát. Cần chú ý âm cuối câu rõ ràng hơn.',
    finalScore: 80.0, status: 'graded', gradedBy: 'Staff Lan', gradedAt: '2026-05-31T17:00:00',
  },
};

export default function StaffGrading() {
  const [submissions, setSubmissions] = useState(MOCK_SUBMISSIONS);
  const [details, setDetails] = useState(MOCK_DETAILS);
  const [selectedId, setSelected] = useState(null);
  const [statusTab, setTab] = useState('pending');
  const { toasts, addToast, removeToast } = useToast();

  const pendingCount = submissions.filter((s) => s.status === 'ai_graded' || s.status === 'pending').length;

  const filtered = statusTab === 'pending'
    ? submissions.filter((s) => s.status === 'ai_graded' || s.status === 'pending')
    : submissions.filter((s) => s.status === 'graded');

  const selectedSubmission = submissions.find((s) => s.submissionId === selectedId) ?? null;
  const selectedDetail = selectedId ? (details[selectedId] ?? null) : null;

  function handleGrade(submissionId, manualScore, feedback) {
    const aiScore = details[submissionId]?.aiOverallScore ?? 0;
    const finalScore = manualScore !== '' ? parseFloat(manualScore) : aiScore;

    setDetails((prev) => ({
      ...prev,
      [submissionId]: {
        ...prev[submissionId],
        status: 'graded',
        manualScore: manualScore !== '' ? parseFloat(manualScore) : null,
        manualFeedback: feedback,
        finalScore,
        gradedBy: 'Staff (Bạn)',
        gradedAt: new Date().toISOString(),
      },
    }));

    setSubmissions((prev) =>
      prev.map((s) =>
        s.submissionId === submissionId
          ? { ...s, status: 'graded', manualScore: finalScore, finalScore }
          : s
      )
    );

    addToast('success', 'Đã lưu điểm. Học viên sẽ nhận thông báo kết quả.');

    const nextPending = filtered.find(
      (s) => s.submissionId !== submissionId && (s.status === 'ai_graded' || s.status === 'pending')
    );
    if (nextPending) setSelected(nextPending.submissionId);
  }

  return (
    <div className="grd-page">
      <StaffTopNav activeTab="staff-grading" />

      <div className="grd-header-bar">
        <h1 className="grd-page-title">Chấm Bài Nói</h1>
        {pendingCount > 0 && (
          <span className="grd-pending-badge" aria-label={`${pendingCount} bài đang chờ chấm điểm`}>
            {pendingCount} chờ chấm
          </span>
        )}
      </div>

      <main className="grd-body">
        <SubmissionList
          submissions={filtered}
          selectedId={selectedId}
          statusTab={statusTab}
          pendingCount={pendingCount}
          onSelect={setSelected}
          onTabChange={setTab}
        />
        <GradingPanel
          submission={selectedSubmission}
          detail={selectedDetail}
          onGrade={handleGrade}
        />
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
