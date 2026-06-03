import { useState } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { useToast, ToastContainer } from '../../components/common/Toast';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './StaffAssessments.css';

const MOCK_QUIZZES = [
  { id: 1, title: 'Quiz Từ Vựng N5 Bài 1', jlptLevel: 'N5', questionCount: 10, status: 'published', updatedAt: '01/06/2026' },
  { id: 2, title: 'Quiz Ngữ Pháp N4 — ～て形', jlptLevel: 'N4', questionCount: 15, status: 'draft', updatedAt: '03/06/2026' },
  { id: 3, title: 'Quiz Kanji N5 Tổng hợp', jlptLevel: 'N5', questionCount: 20, status: 'pending_review', updatedAt: '02/06/2026' },
  { id: 4, title: 'Quiz Nghe N3 — Hội thoại ngắn', jlptLevel: 'N3', questionCount: 8, status: 'draft', updatedAt: '03/06/2026' },
  { id: 5, title: 'Quiz Đọc hiểu N4 Bài 2', jlptLevel: 'N4', questionCount: 12, status: 'published', updatedAt: '28/05/2026' },
];

const MOCK_EXAMS = [
  { id: 1, title: 'Mock Test JLPT N5 — Đề 01', jlptLevel: 'N5', questionCount: 55, duration: 90, status: 'published', updatedAt: '28/05/2026' },
  { id: 2, title: 'Mock Test JLPT N4 Vol.2', jlptLevel: 'N4', questionCount: 60, duration: 105, status: 'pending_review', updatedAt: '02/06/2026' },
  { id: 3, title: 'Mock Test JLPT N3 — Thử nghiệm', jlptLevel: 'N3', questionCount: 65, duration: 120, status: 'draft', updatedAt: '03/06/2026' },
  { id: 4, title: 'Mock Test JLPT N5 — Đề 02', jlptLevel: 'N5', questionCount: 55, duration: 90, status: 'published', updatedAt: '25/05/2026' },
];

const STATUS_META = {
  draft:          { label: 'Nháp',         bg: '#F0EDEB',                        color: 'var(--color-text-sub)' },
  pending_review: { label: 'Chờ duyệt',    bg: 'var(--color-accent-bg)',          color: 'var(--color-warning)' },
  published:      { label: 'Đã xuất bản',  bg: 'var(--color-secondary-bg)',       color: 'var(--color-secondary)' },
  rejected:       { label: 'Từ chối',      bg: '#FFEAEA',                         color: 'var(--color-error)' },
};

function StatusBadge({ status }) {
  const m = STATUS_META[status] ?? STATUS_META.draft;
  return (
    <span className="sfa-status" style={{ background: m.bg, color: m.color }}>
      {m.label}
    </span>
  );
}

const TABS = [
  { id: 'quiz', label: 'Quiz' },
  { id: 'exam', label: 'Đề thi' },
];

export default function StaffAssessments() {
  const [activeTab, setActiveTab] = useState('quiz');
  const [quizzes, setQuizzes] = useState(MOCK_QUIZZES);
  const [exams, setExams] = useState(MOCK_EXAMS);
  const { toasts, addToast, removeToast } = useToast();

  const items = activeTab === 'quiz' ? quizzes : exams;

  function handleCreate() {
    addToast('info', 'Tính năng tạo đề thi sẽ sớm được ra mắt!');
  }

  function handleSubmit(item) {
    const setter = activeTab === 'quiz' ? setQuizzes : setExams;
    setter((prev) => prev.map((i) => i.id === item.id ? { ...i, status: 'pending_review' } : i));
    addToast('success', `Đã gửi duyệt: ${item.title}`);
  }

  return (
    <div className="sfa-page">
      <StaffTopNav activeTab="staff-assessments" />

      <main className="sfa-body">
        <StaffPageHero
          accent="pink"
          title="Đề Thi & Quiz"
          subtitle="Tạo và quản lý đề thi JLPT mock và quiz theo chủ đề cho học viên"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              {/* Giấy thi với dấu tích */}
              <path d="M10 6 h20 l8 8 v28 a2 2 0 0 1-2 2 H12 a2 2 0 0 1-2-2 V8 a2 2 0 0 1 2-2 z"/>
              <polyline points="30,6 30,14 38,14"/>
              <polyline points="15,26 21,32 33,20"/>
              <line x1="15" y1="38" x2="28" y2="38"/>
              <line x1="15" y1="43" x2="24" y2="43"/>
            </svg>
          }
        />

        <div className="sfa-page-header">
          <h1 className="sfa-page-title">Đề Thi & Quiz</h1>
          <button className="sfa-btn-create" onClick={handleCreate}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
            </svg>
            Tạo mới
          </button>
        </div>

        <div className="sfa-tabs" role="tablist">
          {TABS.map((t) => (
            <button
              key={t.id}
              role="tab"
              aria-selected={activeTab === t.id}
              className={`sfa-tab${activeTab === t.id ? ' sfa-tab--active' : ''}`}
              onClick={() => setActiveTab(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>

        {items.length === 0 ? (
          <EmptyState
            title="Chưa có nội dung"
            subtitle="Bắt đầu tạo quiz hoặc đề thi đầu tiên."
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <div className="sfa-table-wrap">
            <table className="sfa-table">
              <caption className="sfa-visually-hidden">
                {activeTab === 'quiz' ? 'Danh sách quiz' : 'Danh sách đề thi'}
              </caption>
              <thead>
                <tr>
                  <th>Tiêu đề</th>
                  <th>Level</th>
                  <th>Số câu hỏi</th>
                  {activeTab === 'exam' && <th>Thời gian (phút)</th>}
                  <th>Trạng thái</th>
                  <th>Cập nhật</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    <td className="sfa-title-cell">{item.title}</td>
                    <td><JlptBadge level={item.jlptLevel} /></td>
                    <td>{item.questionCount} câu</td>
                    {activeTab === 'exam' && <td>{item.duration} phút</td>}
                    <td><StatusBadge status={item.status} /></td>
                    <td>{item.updatedAt}</td>
                    <td>
                      <div className="sfa-actions">
                        <button className="sfa-btn-icon" aria-label={`Xem ${item.title}`} title="Xem">
                          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
                          </svg>
                        </button>
                        {(item.status === 'draft' || item.status === 'rejected') && (
                          <>
                            <button className="sfa-btn-icon" aria-label={`Sửa ${item.title}`} title="Sửa">
                              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                              </svg>
                            </button>
                            <button
                              className="sfa-btn-submit"
                              onClick={() => handleSubmit(item)}
                              aria-label={`Gửi duyệt ${item.title}`}
                            >
                              Gửi duyệt
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
