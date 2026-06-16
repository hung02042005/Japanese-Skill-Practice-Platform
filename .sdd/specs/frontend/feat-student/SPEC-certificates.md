# SPEC — Chứng chỉ (`/certificates`)
> **Sprint:** 4 — Monetization & Retention
> **Prefix:** `crt-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §12.5`
> **Yêu cầu:** VIP hoặc đã hoàn thành level

---

## 1. MÔ TẢ TRANG

Trang chứng chỉ cá nhân. Hiển thị danh sách chứng chỉ đã đạt được và progress các level chưa hoàn thành. Cho phép tải PDF và chia sẻ.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                           │
├──────────────────────────────────────────────────────────────────┤
│  Chứng Chỉ                                                      │
│                                                                  │
│  ─── Đã đạt ───────────────────────────────────────────────── │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  🌸  Chứng nhận Hoàn thành          [N5]                  │ │
│  │      Nguyễn Văn A                                          │ │
│  │      Hoàn thành: 15/05/2026                               │ │
│  │                                                            │ │
│  │      [📥 Tải PDF]   [LinkedIn]   [Facebook]               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ─── Đang tiến hành ─────────────────────────────────────────  │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  [N4]  Chứng nhận Hoàn thành N4                           │ │
│  │  [███████░░░░░░░░░░] 45% · 36/80 bài hoàn thành          │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  [N3]  Chứng nhận Hoàn thành N3                           │ │
│  │  [███░░░░░░░░░░░░░░] 20% · 24/120 bài hoàn thành         │ │
│  └────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/certificates/
├── Certificates.jsx
└── Certificates.css
```

---

## 4. STATE

```js
const [earned,    setEarned]  = useState([]);    // đã đạt
const [progress,  setProgress]= useState([]);    // đang tiến hành
const [isLoading, setLoading] = useState(true);
const [error,     setError]   = useState('');
const [downloading, setDown]  = useState(null);  // certId đang download

const { user } = useAppSelector((s) => s.auth);
const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// GET /api/certificates/me
// Response: [{ certId, jlptLevel, earnedAt, studentName, pdfUrl }]

// GET /api/certificates/me/progress
// Response: [{ jlptLevel, completedLessons, totalLessons, progressPercent }]

// GET /api/certificates/:certId/download
// Response: { data: { pdfUrl } }
// → window.open(pdfUrl, '_blank')
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { getMyCertificates, getCertificateProgress, downloadCertificate } from '../../api/studentService';
import './Certificates.css';

export default function Certificates() {
  const { user }  = useAppSelector((s) => s.auth);
  const { toasts, addToast, removeToast } = useToast();

  const [earned,    setEarned]  = useState([]);
  const [progress,  setProgress]= useState([]);
  const [isLoading, setLoading] = useState(true);
  const [downloading, setDown]  = useState(null);

  useEffect(() => {
    Promise.all([getMyCertificates(), getCertificateProgress()])
      .then(([certs, prog]) => { setEarned(certs); setProgress(prog); })
      .catch(() => addToast('error', 'Không thể tải chứng chỉ.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleDownload(certId) {
    setDown(certId);
    try {
      const { pdfUrl } = await downloadCertificate(certId);
      window.open(pdfUrl, '_blank', 'noopener,noreferrer');
    } catch {
      addToast('error', 'Không thể tải chứng chỉ. Thử lại sau.');
    } finally {
      setDown(null);
    }
  }

  function shareLinkedIn(cert) {
    const url = encodeURIComponent(`https://sakuji.vn/certificates/${cert.certId}`);
    const title = encodeURIComponent(`Tôi vừa hoàn thành JLPT ${cert.jlptLevel} trên SakuJi!`);
    window.open(`https://www.linkedin.com/sharing/share-offsite/?url=${url}&title=${title}`, '_blank', 'noopener,noreferrer');
  }

  return (
    <div className="crt-page">
      <TopNav activeTab="" />
      <main className="crt-body">
        <h1 className="crt-title">Chứng Chỉ</h1>

        {isLoading ? (
          <>{[1,2].map((i) => <div key={i} className="crt-skel" aria-hidden="true" />)}</>
        ) : (
          <>
            {/* Earned */}
            {earned.length > 0 && (
              <section>
                <h2 className="crt-section-title">Đã đạt được</h2>
                <div className="crt-earned-list">
                  {earned.map((cert) => (
                    <div key={cert.certId} className="crt-card crt-card--earned">
                      <div className="crt-card-left">
                        <div className="crt-bloom-icon" aria-hidden="true">🌸</div>
                        <div className="crt-card-info">
                          <div className="crt-cert-name">
                            <JlptBadge level={cert.jlptLevel} />
                            <span className="crt-cert-title">Chứng nhận Hoàn thành</span>
                          </div>
                          <div className="crt-cert-owner">{cert.studentName}</div>
                          <div className="crt-cert-date">Hoàn thành: {new Date(cert.earnedAt).toLocaleDateString('vi-VN')}</div>
                        </div>
                      </div>
                      <div className="crt-card-actions">
                        <button
                          className="crt-dl-btn"
                          onClick={() => handleDownload(cert.certId)}
                          disabled={downloading === cert.certId}
                          aria-label={`Tải chứng chỉ ${cert.jlptLevel} dạng PDF`}
                        >
                          {downloading === cert.certId
                            ? <span className="crt-spinner" aria-hidden="true" />
                            : <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                          }
                          Tải PDF
                        </button>
                        <button
                          className="crt-share-btn crt-share-btn--linkedin"
                          onClick={() => shareLinkedIn(cert)}
                          aria-label={`Chia sẻ chứng chỉ ${cert.jlptLevel} lên LinkedIn`}
                        >
                          LinkedIn
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {/* In progress */}
            {progress.length > 0 && (
              <section>
                <h2 className="crt-section-title">Đang tiến hành</h2>
                <div className="crt-progress-list">
                  {progress.map((p) => (
                    <div key={p.jlptLevel} className="crt-card crt-card--progress">
                      <div className="crt-prog-header">
                        <JlptBadge level={p.jlptLevel} />
                        <span className="crt-prog-name">Chứng nhận Hoàn thành {p.jlptLevel}</span>
                      </div>
                      <div className="crt-prog-bar-row">
                        <ProgressBar value={p.progressPercent} />
                        <span className="crt-prog-pct">{p.progressPercent}%</span>
                      </div>
                      <span className="crt-prog-count">{p.completedLessons}/{p.totalLessons} bài hoàn thành</span>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {/* Empty */}
            {earned.length === 0 && progress.length === 0 && (
              <EmptyState
                title="Chưa có chứng chỉ nào"
                subtitle="Hoàn thành toàn bộ bài học của một cấp độ JLPT để nhận chứng chỉ!"
                mascotVariant="idle"
                mascotSize={160}
              >
                <a href="/learn/new" className="crt-cta-btn">Học bài ngay →</a>
              </EmptyState>
            )}
          </>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Certificates (SakuJi Hanami Theme) ===== */
.crt-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.crt-body { flex: 1; max-width: 860px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 24px; box-sizing: border-box; }
.crt-title { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.crt-section-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0 0 12px; }
.crt-earned-list, .crt-progress-list { display: flex; flex-direction: column; gap: 14px; }
.crt-card { background: var(--color-card); border-radius: var(--radius-xl); box-shadow: var(--shadow-sm); padding: 20px 24px; }
.crt-card--earned { border-top: 3px solid var(--color-primary); display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.crt-card--progress { display: flex; flex-direction: column; gap: 10px; }
.crt-card-left { display: flex; align-items: center; gap: 16px; }
.crt-bloom-icon { font-size: 36px; }
.crt-card-info { display: flex; flex-direction: column; gap: 4px; }
.crt-cert-name { display: flex; align-items: center; gap: 8px; }
.crt-cert-title { font-size: 16px; font-weight: 700; color: var(--color-text); }
.crt-cert-owner { font-size: 14px; color: var(--color-text-sub); }
.crt-cert-date  { font-size: 13px; color: var(--color-text-disabled); }
.crt-card-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.crt-dl-btn { display: inline-flex; align-items: center; gap: 6px; height: 36px; padding: 0 16px; background: var(--color-primary-bg); border: 1.5px solid var(--color-primary); color: var(--color-primary); border-radius: var(--radius-full); font-size: 13px; font-weight: 700; cursor: pointer; transition: background var(--transition); }
.crt-dl-btn:hover:not(:disabled) { background: var(--color-primary); color: white; }
.crt-dl-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.crt-share-btn { height: 36px; padding: 0 14px; border: 1.5px solid var(--color-border); background: transparent; color: var(--color-text-sub); border-radius: var(--radius-full); font-size: 13px; font-weight: 600; cursor: pointer; transition: background var(--transition), color var(--transition); }
.crt-share-btn--linkedin:hover { background: #0077B5; color: white; border-color: #0077B5; }
.crt-prog-header { display: flex; align-items: center; gap: 10px; }
.crt-prog-name  { font-size: 16px; font-weight: 700; color: var(--color-text); }
.crt-prog-bar-row { display: flex; align-items: center; gap: 10px; }
.crt-prog-pct   { font-size: 13px; font-weight: 700; color: var(--color-text); white-space: nowrap; }
.crt-prog-count { font-size: 13px; color: var(--color-text-sub); }
.crt-skel { height: 100px; border-radius: var(--radius-xl); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.crt-cta-btn { display: inline-flex; align-items: center; height: 42px; padding: 0 24px; background: var(--color-secondary); color: white; border-radius: var(--radius-full); text-decoration: none; font-size: 14px; font-weight: 700; }
.crt-spinner { display: inline-block; width: 13px; height: 13px; border: 2px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 767px) { .crt-body { padding: 16px 16px 32px; } .crt-card--earned { flex-direction: column; align-items: flex-start; } .crt-card-actions { width: 100%; } .crt-dl-btn, .crt-share-btn { flex: 1; justify-content: center; } }
@media (prefers-reduced-motion: reduce) { .crt-page * { animation: none !important; transition-duration: 0ms !important; } }
```
