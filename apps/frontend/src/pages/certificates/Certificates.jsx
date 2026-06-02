import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { getMyCertificates, getCertificateProgress, downloadCertificate } from '../../api/studentService';
import './Certificates.css';

export default function Certificates() {
  const { toasts, addToast, removeToast } = useToast();

  const [earned,     setEarned]   = useState([]);
  const [progress,   setProgress] = useState([]);
  const [isLoading,  setLoading]  = useState(true);
  const [downloading,setDown]     = useState(null);

  useEffect(() => {
    Promise.all([getMyCertificates(), getCertificateProgress()])
      .then(([certs, prog]) => { setEarned(certs ?? []); setProgress(prog ?? []); })
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
    const url   = encodeURIComponent(`https://sakuji.vn/certificates/${cert.certId}`);
    const title = encodeURIComponent(`Tôi vừa hoàn thành JLPT ${cert.jlptLevel} trên SakuJi!`);
    window.open(
      `https://www.linkedin.com/sharing/share-offsite/?url=${url}&title=${title}`,
      '_blank',
      'noopener,noreferrer'
    );
  }

  return (
    <div className="crt-page">
      <TopNav activeTab="" />
      <main className="crt-body">
        <h1 className="crt-title">Chứng Chỉ</h1>

        {isLoading ? (
          <>{[1, 2].map((i) => <div key={i} className="crt-skel" aria-hidden="true" />)}</>
        ) : (
          <>
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
                          <div className="crt-cert-date">
                            Hoàn thành: {new Date(cert.earnedAt).toLocaleDateString('vi-VN')}
                          </div>
                        </div>
                      </div>
                      <div className="crt-card-actions">
                        <button
                          className="crt-dl-btn"
                          onClick={() => handleDownload(cert.certId)}
                          disabled={downloading === cert.certId}
                          aria-label={`Tải chứng chỉ ${cert.jlptLevel} dạng PDF`}
                        >
                          {downloading === cert.certId ? (
                            <span className="crt-spinner" aria-hidden="true" />
                          ) : (
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                          )}
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
                      <span className="crt-prog-count">
                        {p.completedLessons}/{p.totalLessons} bài hoàn thành
                      </span>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {earned.length === 0 && progress.length === 0 && (
              <EmptyState
                title="Chưa có chứng chỉ nào"
                subtitle="Hoàn thành toàn bộ bài học của một cấp độ JLPT để nhận chứng chỉ!"
                mascotVariant="idle"
                mascotSize={160}
              >
                <Link to="/learn/new" className="crt-cta-btn">Học bài ngay →</Link>
              </EmptyState>
            )}
          </>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
