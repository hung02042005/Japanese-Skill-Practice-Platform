import { useState, useRef, useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  fetchGrammarsThunk,
  submitGrammarReviewThunk,
  createGrammarThunk,
  updateGrammarThunk,
} from "../../store/slices/staffGrammarSlice";
import {
  fetchLessonsThunk,
  fetchVocabularyThunk,
  fetchKanjiThunk,
} from "../../store/slices/staffLearningSlice";
import {
  createStaffLesson,
  updateStaffLesson,
  createStaffVocabulary,
  updateStaffVocabulary,
  createStaffKanji,
  updateStaffKanji,
  submitAssessmentForReview,
  getContentReviewFeedback,
} from "../../api/staffService";
import StaffTopNav from "../../components/layout/StaffTopNav";
import { EmptyState } from "../../components/common/EmptyState";
import { Pagination } from "../../components/common/Pagination";
import { JlptBadge } from "../../components/common/Badges";
import { useToast, ToastContainer } from "../../components/common/Toast";
import ContentStatusActions from "../../components/staff/ContentStatusActions";
import ContentFormModal from "../../components/staff/ContentFormModal";
import ContentPreviewDrawer from "../../components/staff/ContentPreviewDrawer";
import StaffPageHero from "../../components/staff/StaffPageHero";
import ReviewFeedbackModal from "../../components/common/ReviewFeedbackModal";
import "./StaffContent.css";

const LESSON_TYPE_LABELS = {
  lesson: "Bài học",
  reading: "Đọc hiểu",
};

const STATUS_META = {
  draft: { label: "Nháp", cls: "sfc-status--draft" },
  pending_review: { label: "Chờ duyệt", cls: "sfc-status--pending_review" },
  published: { label: "Đã xuất bản", cls: "sfc-status--published" },
  rejected: { label: "Từ chối", cls: "sfc-status--rejected" },
  archived: { label: "Lưu trữ", cls: "sfc-status--archived" },
};

const CONTENT_TABS = [
  { id: "course", label: "Khóa học" },
  { id: "lesson", label: "Bài học" },
  { id: "vocabulary", label: "Từ vựng" },
  { id: "grammar", label: "Ngữ pháp" },
  { id: "kanji", label: "Kanji" },
];

const PAGE_SIZE = 10;

function StatusBadge({ status }) {
  const m = STATUS_META[status] ?? STATUS_META.draft;
  return <span className={`sfc-status ${m.cls}`}>{m.label}</span>;
}

function formatDate(value) {
  if (!value) return "";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString("vi-VN");
}

export default function StaffContent() {
  const dispatch = useDispatch();
  const grammarState = useSelector((state) => state.staffGrammar);
  const learnState = useSelector((state) => state.staffLearning);

  const [activeContentTab, setActiveTab] = useState("course");
  const [search, setSearch] = useState("");
  const [levelFilter, setLevel] = useState("");
  const [statusFilter, setStatus] = useState("");
  const [createType, setCreateType] = useState(null);
  const [editItem, setEditItem] = useState(null);
  const [previewItem, setPreviewItem] = useState(null);
  const [showModal, setModal] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [feedbackModal, setFeedbackModal] = useState({ open: false, feedback: null, actionType: null, reviewedAt: null });
  const dropdownRef = useRef(null);
  const { toasts, addToast, removeToast } = useToast();
  const searchTimerRef = useRef(null);


  // ─── State helpers ──────────────────────────────────────────────────

  function getTabState() {
    switch (activeContentTab) {
      case "course":
      case "lesson":
        return {
          items: learnState.lessons,
          totalPages: learnState.lessonsTotalPages,
          status: learnState.lessonsStatus,
          error: learnState.lessonsError,
        };
      case "vocabulary":
        return {
          items: learnState.vocabulary,
          totalPages: learnState.vocabularyTotalPages,
          status: learnState.vocabularyStatus,
          error: learnState.vocabularyError,
        };
      case "grammar":
        return {
          items: grammarState.items,
          totalPages: grammarState.totalPages,
          status: grammarState.status,
          error: grammarState.error,
        };
      case "kanji":
        return {
          items: learnState.kanji,
          totalPages: learnState.kanjiTotalPages,
          status: learnState.kanjiStatus,
          error: learnState.kanjiError,
        };
      default:
        return { items: [], totalPages: 0, status: "idle", error: null };
    }
  }

  const { items, totalPages, status, error } = getTabState();
  const loading = status === "loading" || status === "idle";

  // ─── Fetch data ─────────────────────────────────────────────────────

  const fetchData = useCallback(() => {
    const opts = {
      q: search || undefined,
      jlptLevel: levelFilter || undefined,
      status: statusFilter || undefined,
      page: currentPage - 1,
      size: PAGE_SIZE,
    };

    switch (activeContentTab) {
      case "course":
        return dispatch(fetchLessonsThunk({ ...opts, lessonType: undefined }));
      case "lesson":
        return dispatch(fetchLessonsThunk(opts));
      case "vocabulary":
        return dispatch(fetchVocabularyThunk(opts));
      case "grammar":
        return dispatch(
          fetchGrammarsThunk({
            jlptLevel: levelFilter || undefined,
            status: statusFilter || undefined,
          })
        );
      case "kanji":
        return dispatch(fetchKanjiThunk(opts));
      default:
        return undefined;
    }
  }, [dispatch, activeContentTab, search, levelFilter, statusFilter, currentPage]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // ─── Filter handlers ────────────────────────────────────────────────

  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
    setSearch("");
    setLevel("");
    setStatus("");
    setCurrentPage(1);
    setPreviewItem(null);
  };

  const handleSearchChange = (e) => {
    const v = e.target.value;
    setSearch(v);
    clearTimeout(searchTimerRef.current);
    searchTimerRef.current = setTimeout(() => setCurrentPage(1), 400);
  };

  const handleFilterChange = (setter) => (e) => {
    setter(e.target.value);
    setCurrentPage(1);
  };

  const handleLevelChange = handleFilterChange(setLevel);
  const handleStatusChange = handleFilterChange(setStatus);

  // ─── Modal handlers ─────────────────────────────────────────────────

  const handleOpenCreate = (contentType) => {
    setCreateType(contentType);
    setEditItem(null);
    setModal(true);
    setShowDropdown(false);
  };

  const handleEdit = (item) => {
    setCreateType(activeContentTab);
    setEditItem(item);
    setModal(true);
  };

  const handleView = (item) => {
    setPreviewItem(item);
  };

  const handleModalClose = () => {
    setModal(false);
    setCreateType(null);
    setEditItem(null);
  };

  const handleSubmitForReview = async (item) => {
    try {
      let contentType;
      switch (activeContentTab) {
        case "course":
        case "lesson":
          contentType = "lesson";
          break;
        case "vocabulary":
          contentType = "vocabulary";
          break;
        case "grammar":
          await dispatch(submitGrammarReviewThunk(item.grammarId || item.id)).unwrap();
          addToast({ type: "success", message: "Đã gửi duyệt ngữ pháp thành công!" });
          fetchData();
          return;
        case "kanji":
          contentType = "kanji";
          break;
        default:
          return;
      }
      const contentId = item.lessonId ?? item.vocabularyId ?? item.kanjiId ?? item.id;
      await submitAssessmentForReview(contentType, contentId);
      addToast({ type: "success", message: `Đã gửi duyệt thành công!` });
      fetchData();
    } catch (err) {
      addToast({ type: "error", message: err?.message || err || "Lỗi khi gửi duyệt" });
    }
  };

  const handleViewFeedback = async (item) => {
    let contentType;
    let contentId;
    switch (activeContentTab) {
      case "course":
      case "lesson":
        contentType = "lesson";
        contentId = item.lessonId ?? item.id;
        break;
      case "vocabulary":
        contentType = "vocabulary";
        contentId = item.vocabularyId ?? item.id;
        break;
      case "grammar":
        contentType = "grammar";
        contentId = item.grammarId ?? item.id;
        break;
      case "kanji":
        contentType = "kanji";
        contentId = item.kanjiId ?? item.id;
        break;
      default:
        return;
    }
    try {
      const data = await getContentReviewFeedback(contentId, contentType);
      setFeedbackModal({ open: true, feedback: data.feedback, actionType: data.actionType, reviewedAt: data.reviewedAt });
    } catch (err) {
      if (err?.response?.status === 404) {
        setFeedbackModal({ open: true, feedback: null, actionType: null, reviewedAt: null });
      } else {
        addToast({ type: "error", message: "Không thể tải phản hồi. Vui lòng thử lại." });
      }
    }
  };

  const handleSave = async (formData) => {
    const ct = formData.contentType;
    try {
      if (ct === "grammar") {
        if (editItem) {
          const grammarId = editItem.grammarId || editItem.id;
          await dispatch(updateGrammarThunk({ grammarId, payload: formData })).unwrap();
          if (formData.status === "pending_review") {
            await dispatch(submitGrammarReviewThunk(grammarId)).unwrap();
            addToast({ type: "success", message: "Đã cập nhật và gửi duyệt ngữ pháp thành công!" });
          } else {
            addToast({ type: "success", message: "Đã cập nhật ngữ pháp thành công!" });
          }
        } else {
          const created = await dispatch(createGrammarThunk(formData)).unwrap();
          const grammarId = created.grammarId || created.id;
          if (formData.status === "pending_review" && grammarId) {
            await dispatch(submitGrammarReviewThunk(grammarId)).unwrap();
            addToast({ type: "success", message: "Đã tạo và gửi duyệt ngữ pháp thành công!" });
          } else {
            addToast({ type: "success", message: "Đã tạo ngữ pháp thành công!" });
          }
        }
      } else if (ct === "course" || ct === "lesson") {
        const payload = ct === "course"
          ? { ...formData, lessonType: formData.lessonType || "lesson" }
          : formData;
        if (editItem) {
          const lessonId = editItem.lessonId || editItem.id;
          const res = await updateStaffLesson(lessonId, payload);
          if (res.status !== 200) throw new Error(res.message || "Lỗi cập nhật");
          if (formData.status === "pending_review") {
            await submitAssessmentForReview("lesson", lessonId);
            addToast({ type: "success", message: "Đã cập nhật và gửi duyệt học liệu thành công!" });
          } else {
            addToast({ type: "success", message: "Đã cập nhật học liệu thành công!" });
          }
        } else {
          const res = await createStaffLesson(payload);
          if (res.status !== 201) throw new Error(res.message || "Lỗi tạo học liệu");
          const lessonId = res.data?.lessonId || res.data?.id;
          if (formData.status === "pending_review" && lessonId) {
            await submitAssessmentForReview("lesson", lessonId);
            addToast({ type: "success", message: "Đã tạo và gửi duyệt học liệu thành công!" });
          } else {
            addToast({ type: "success", message: "Đã tạo học liệu thành công!" });
          }
        }
      } else if (ct === "vocabulary") {
        if (editItem) {
          const vocabId = editItem.vocabularyId || editItem.id;
          await updateStaffVocabulary(vocabId, formData);
          if (formData.status === "pending_review") {
            await submitAssessmentForReview("vocabulary", vocabId);
            addToast({ type: "success", message: "Đã cập nhật và gửi duyệt từ vựng thành công!" });
          } else {
            addToast({ type: "success", message: "Đã cập nhật từ vựng thành công!" });
          }
        } else {
          const res = await createStaffVocabulary(formData);
          if (res.status && res.status !== 201) throw new Error(res.message || "Lỗi tạo từ vựng");
          const vocabId = res.data?.vocabularyId || res.data?.id;
          if (formData.status === "pending_review" && vocabId) {
            await submitAssessmentForReview("vocabulary", vocabId);
            addToast({ type: "success", message: "Đã tạo và gửi duyệt từ vựng thành công!" });
          } else {
            addToast({ type: "success", message: "Đã tạo từ vựng thành công!" });
          }
        }
      } else if (ct === "kanji") {
        if (editItem) {
          const kanjiId = editItem.kanjiId || editItem.id;
          await updateStaffKanji(kanjiId, formData);
          if (formData.status === "pending_review") {
            await submitAssessmentForReview("kanji", kanjiId);
            addToast({ type: "success", message: "Đã cập nhật và gửi duyệt Kanji thành công!" });
          } else {
            addToast({ type: "success", message: "Đã cập nhật Kanji thành công!" });
          }
        } else {
          const res = await createStaffKanji(formData);
          if (res.status && res.status !== 201) throw new Error(res.message || "Lỗi tạo Kanji");
          const kanjiId = res.data?.kanjiId || res.data?.id;
          if (formData.status === "pending_review" && kanjiId) {
            await submitAssessmentForReview("kanji", kanjiId);
            addToast({ type: "success", message: "Đã tạo và gửi duyệt Kanji thành công!" });
          } else {
            addToast({ type: "success", message: "Đã tạo Kanji thành công!" });
          }
        }
      }
      handleModalClose();
      fetchData();
    } catch (err) {
      addToast({ type: "error", message: err?.message || err || "Lỗi khi lưu" });
    }
  };

  // ─── Dropdown outside click ─────────────────────────────────────────

  useEffect(() => {
    function handleClick(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  // ─── Pagination ─────────────────────────────────────────────────────

  const safePage = currentPage > 1 && currentPage > totalPages ? 1 : currentPage;

  // ─── Render helpers ─────────────────────────────────────────────────

  function renderLessonRow(item) {
    return (
      <tr key={item.lessonId ?? item.id}>
        <td style={{ fontWeight: 500 }}>{item.title}</td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13 }}>
          {item.lessonType ? (LESSON_TYPE_LABELS[item.lessonType] ?? item.lessonType) : ""}
        </td>
        <td><JlptBadge level={item.jlptLevel} /></td>
        <td><StatusBadge status={item.status} /></td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13 }}>{formatDate(item.updatedAt)}</td>
        <td>
          <ContentStatusActions item={item} onEdit={handleEdit} onSubmit={handleSubmitForReview} onView={handleView} onFeedback={handleViewFeedback} />
        </td>
      </tr>
    );
  }

  function renderVocabRow(item) {
    return (
      <tr key={item.vocabularyId ?? item.id}>
        <td style={{ fontWeight: 600, fontFamily: "monospace", fontSize: 15 }}>
          {item.word}
          {item.furigana ? <span style={{ marginLeft: 6, fontWeight: 400, fontSize: 13, color: "var(--color-text-sub)" }}>({item.furigana})</span> : null}
        </td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13, maxWidth: 200 }}>{item.meaning}</td>
        <td><JlptBadge level={item.jlptLevel} /></td>
        <td><StatusBadge status={item.status} /></td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13 }}>{formatDate(item.updatedAt)}</td>
        <td>
          <ContentStatusActions item={item} onEdit={handleEdit} onSubmit={handleSubmitForReview} onView={handleView} onFeedback={handleViewFeedback} />
        </td>
      </tr>
    );
  }

  function renderGrammarRow(item) {
    return (
      <tr key={item.grammarId ?? item.id}>
        <td style={{ fontWeight: 600, fontFamily: "monospace", fontSize: 15 }}>{item.structure || item.pattern}</td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13, maxWidth: 220 }}>{item.meaning}</td>
        <td><JlptBadge level={item.jlptLevel} /></td>
        <td><StatusBadge status={item.status} /></td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13 }}>{formatDate(item.updatedAt)}</td>
        <td>
          <ContentStatusActions item={item} onEdit={handleEdit} onSubmit={handleSubmitForReview} onView={handleView} onFeedback={handleViewFeedback} />
        </td>
      </tr>
    );
  }

  function renderKanjiRow(item) {
    return (
      <tr key={item.kanjiId ?? item.id}>
        <td><span className="sfc-kanji-char">{item.characterValue}</span></td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13 }}>{item.onyomi}</td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13 }}>{item.kunyomi}</td>
        <td><JlptBadge level={item.jlptLevel} /></td>
        <td><StatusBadge status={item.status} /></td>
        <td style={{ color: "var(--color-text-sub)", fontSize: 13 }}>{formatDate(item.updatedAt)}</td>
        <td>
          <ContentStatusActions item={item} onEdit={handleEdit} onSubmit={handleSubmitForReview} onView={handleView} onFeedback={handleViewFeedback} />
        </td>
      </tr>
    );
  }

  function renderTable() {
    if (loading) {
      return (
        <div className="sfc-empty-wrap">
          <EmptyState title="Đang tải…" subtitle="Vui lòng chờ trong giây lát." mascotVariant="thinking" mascotSize={80} />
        </div>
      );
    }
    if (error) {
      return (
        <div className="sfc-empty-wrap">
          <EmptyState title="Lỗi tải dữ liệu" subtitle={error} mascotVariant="thinking" mascotSize={80} />
        </div>
      );
    }
    if (!items || items.length === 0) {
      return (
        <div className="sfc-empty-wrap">
          <EmptyState
            title="Không có nội dung nào"
            subtitle="Hãy tạo nội dung mới hoặc thử thay đổi bộ lọc"
            mascotVariant="empty"
            mascotSize={80}
          />
        </div>
      );
    }

    const headerMap = {
      course: ["Tiêu đề", "Loại", "Cấp độ", "Trạng thái", "Cập nhật", ""],
      lesson: ["Tiêu đề", "Loại", "Cấp độ", "Trạng thái", "Cập nhật", ""],
      vocabulary: ["Từ vựng", "Nghĩa", "Cấp độ", "Trạng thái", "Cập nhật", ""],
      grammar: ["Cấu trúc", "Ý nghĩa", "Cấp độ", "Trạng thái", "Cập nhật", ""],
      kanji: ["Chữ Hán", "Âm On", "Âm Kun", "Cấp độ", "Trạng thái", "Cập nhật", ""],
    };
    const headers = headerMap[activeContentTab] ?? headerMap.lesson;

    const rowMap = {
      course: renderLessonRow,
      lesson: renderLessonRow,
      vocabulary: renderVocabRow,
      grammar: renderGrammarRow,
      kanji: renderKanjiRow,
    };
    const renderRow = rowMap[activeContentTab] ?? renderLessonRow;

    return (
      <table className="sfc-table" aria-label={`Danh sách ${CONTENT_TABS.find((t) => t.id === activeContentTab)?.label ?? ""}`}>
        <thead>
          <tr>{headers.map((h, i) => (h ? <th key={i}>{h}</th> : <th key={i}></th>))}</tr>
        </thead>
        <tbody>{items.map(renderRow)}</tbody>
      </table>
    );
  }

  return (
    <div className="sfc-page">
      <StaffTopNav activeTab="staff-content" />

      <main className="sfc-body">
        <h1 className="sfc-visually-hidden">Quản Lý Học Liệu</h1>

        <StaffPageHero
          accent="gold"
          title="Quản Lý Học Liệu"
          subtitle="Soạn thảo khóa học, bài học, từ vựng, ngữ pháp và Kanji theo từng cấp độ JLPT"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <rect x="10" y="14" width="28" height="20" rx="1.5" />
              <ellipse cx="10" cy="24" rx="3.5" ry="10" />
              <ellipse cx="38" cy="24" rx="3.5" ry="10" />
              <line x1="16" y1="20" x2="32" y2="20" />
              <line x1="16" y1="25" x2="28" y2="25" />
              <line x1="16" y1="30" x2="24" y2="30" />
            </svg>
          }
        />

        <div className="sfc-page-header">
          <p className="sfc-page-title">Quản Lý Học Liệu</p>
          <div className="sfc-create-wrap" ref={dropdownRef}>
            <button
              className="sfc-btn-create"
              onClick={() => setShowDropdown((v) => !v)}
              aria-haspopup="true"
              aria-expanded={showDropdown}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
              </svg>
              Tạo mới
              <svg
                width="12"
                height="12"
                viewBox="0 0 24 24"
                fill="none"
                aria-hidden="true"
                style={{ transition: "transform var(--transition)", transform: showDropdown ? "rotate(180deg)" : "rotate(0deg)" }}
              >
                <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
            {showDropdown && (
              <div className="sfc-dropdown" role="menu">
                {CONTENT_TABS.map((tab) => (
                  <button
                    key={tab.id}
                    className="sfc-dropdown-item"
                    role="menuitem"
                    onClick={() => handleOpenCreate(tab.id)}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="sfc-content-tabs" role="tablist" aria-label="Loại học liệu">
          {CONTENT_TABS.map((tab) => (
            <button
              key={tab.id}
              className={`sfc-content-tab${activeContentTab === tab.id ? " sfc-content-tab--active" : ""}`}
              role="tab"
              aria-selected={activeContentTab === tab.id}
              onClick={() => handleTabChange(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="sfc-filter-bar">
          <input
            className="sfc-search"
            type="search"
            placeholder="Tìm kiếm..."
            value={search}
            onChange={handleSearchChange}
            aria-label="Tìm kiếm nội dung"
          />
          <select className="sfc-select" value={levelFilter} onChange={handleLevelChange} aria-label="Lọc theo cấp độ">
            <option value="">Tất cả cấp độ</option>
            {["N5", "N4", "N3", "N2", "N1"].map((l) => (
              <option key={l} value={l}>{l}</option>
            ))}
          </select>
          <select className="sfc-select" value={statusFilter} onChange={handleStatusChange} aria-label="Lọc theo trạng thái">
            <option value="">Tất cả trạng thái</option>
            <option value="draft">Nháp</option>
            <option value="pending_review">Chờ duyệt</option>
            <option value="published">Đã xuất bản</option>
            <option value="rejected">Từ chối</option>
            <option value="archived">Lưu trữ</option>
          </select>
        </div>

        <div className="sfc-table-wrap" role="tabpanel">
          {renderTable()}
        </div>

        {totalPages > 1 && (
          <Pagination currentPage={safePage} totalPages={totalPages} onChange={(p) => setCurrentPage(p)} />
        )}
      </main>

      <ContentFormModal
        isOpen={showModal}
        contentType={createType}
        editItem={editItem}
        onClose={handleModalClose}
        onSave={handleSave}
      />

      <ContentPreviewDrawer item={previewItem} contentType={activeContentTab} onClose={() => setPreviewItem(null)} />

      <ReviewFeedbackModal
        isOpen={feedbackModal.open}
        onClose={() => setFeedbackModal({ open: false, feedback: null, actionType: null, reviewedAt: null })}
        feedback={feedbackModal.feedback}
        actionType={feedbackModal.actionType}
        reviewedAt={feedbackModal.reviewedAt}
      />

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
