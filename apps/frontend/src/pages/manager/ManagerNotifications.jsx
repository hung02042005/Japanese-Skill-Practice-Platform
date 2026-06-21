import { useState } from 'react';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import { useToast, ToastContainer } from '../../components/common/Toast';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './ManagerNotifications.css';

const MOCK_SENT = [
  { id: 1, title: 'Thông báo bảo trì hệ thống', content: 'Hệ thống sẽ bảo trì vào 02:00 ngày 05/06/2026.', targetLevel: 'all', channel: 'both', sentAt: '03/06/2026 09:00', sentCount: 1250 },
  { id: 2, title: 'Tài liệu N4 mới đã được cập nhật', content: 'Bộ flashcard N4 Vol.3 đã được thêm vào hệ thống.', targetLevel: 'N4', channel: 'in-app', sentAt: '01/06/2026 14:30', sentCount: 340 },
  { id: 3, title: 'Nhắc nhở streak — Đừng để streak bị gián đoạn!', content: 'Bạn chưa học hôm nay. Hãy giữ streak của mình nhé!', targetLevel: 'all', channel: 'email', sentAt: '30/05/2026 20:00', sentCount: 980 },
  { id: 4, title: 'Kết quả thi JLPT tháng 7/2026 đã có', content: 'Xem kết quả kỳ thi JLPT tháng 7 ngay bây giờ.', targetLevel: 'N3', channel: 'both', sentAt: '28/05/2026 10:00', sentCount: 215 },
];

const CHANNEL_LABELS = { 'in-app': 'In-app', 'email': 'Email', 'both': 'Cả hai' };
const LEVEL_OPTIONS = ['all', 'N5', 'N4', 'N3', 'N2', 'N1'];
const CHANNEL_OPTIONS = [
  { value: 'in-app', label: 'In-app' },
  { value: 'email', label: 'Email' },
  { value: 'both', label: 'Cả hai' },
];

export default function ManagerNotifications() {
  const [sent, setSent] = useState(MOCK_SENT);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [targetLevel, setLevel] = useState('all');
  const [channel, setChannel] = useState('in-app');
  const [errors, setErrors] = useState({});
  const [sending, setSending] = useState(false);
  const { toasts, addToast, removeToast } = useToast();

  function validate() {
    const errs = {};
    if (!title.trim()) errs.title = 'Tiêu đề không được để trống.';
    if (!content.trim()) errs.content = 'Nội dung không được để trống.';
    return errs;
  }

  function handleSend() {
    const errs = validate();
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }
    setErrors({});
    setSending(true);
    setTimeout(() => {
      const newItem = {
        id: Date.now(),
        title,
        content,
        targetLevel,
        channel,
        sentAt: new Date().toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }),
        sentCount: Math.floor(Math.random() * 500) + 50,
      };
      setSent((prev) => [newItem, ...prev]);
      setTitle('');
      setContent('');
      setLevel('all');
      setChannel('in-app');
      setSending(false);
      addToast('success', 'Thông báo đã được gửi thành công!');
    }, 600);
  }

  return (
    <div className="nfs-page">
      <ManagerTopNav activeTab="manager-notifications" />

      <main className="nfs-body">
        <StaffPageHero
          accent="pink"
          title="Gửi Thông Báo"
          subtitle="Soạn và gửi thông báo hệ thống đến học viên theo cấp độ và kênh truyền thông"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              {/* Chuông gió Nhật (風鈴) */}
              <line x1="24" y1="4" x2="24" y2="8"/>
              <path d="M16 20 C16 13 20 8 24 8 C28 8 32 13 32 20 L34 28 L14 28 Z"/>
              <path d="M14 28 Q24 33 34 28"/>
              <line x1="24" y1="33" x2="24" y2="37"/>
              <circle cx="24" cy="39" r="2.5" fill="currentColor"/>
              <line x1="24" y1="41.5" x2="24" y2="44"/>
              <line x1="21" y1="44" x2="27" y2="44"/>
              <line x1="24" y1="44" x2="24" y2="47"/>
            </svg>
          }
        />

        {/* Compose form */}
        <section className="nfs-card">
          <h2 className="nfs-section-title">Soạn thông báo mới</h2>

          <div className="nfs-form">
            <div className="nfs-field">
              <label className="nfs-field-label" htmlFor="nfs-title">
                Tiêu đề <span className="nfs-req">*</span>
              </label>
              <input
                id="nfs-title"
                className={`nfs-input${errors.title ? ' nfs-input--err' : ''}`}
                type="text"
                value={title}
                onChange={(e) => { setTitle(e.target.value); setErrors((p) => ({ ...p, title: '' })); }}
                placeholder="Nhập tiêu đề thông báo..."
              />
              {errors.title && <span className="nfs-field-error">{errors.title}</span>}
            </div>

            <div className="nfs-field">
              <label className="nfs-field-label" htmlFor="nfs-content">
                Nội dung <span className="nfs-req">*</span>
              </label>
              <textarea
                id="nfs-content"
                className={`nfs-textarea${errors.content ? ' nfs-input--err' : ''}`}
                rows={4}
                value={content}
                onChange={(e) => { setContent(e.target.value); setErrors((p) => ({ ...p, content: '' })); }}
                placeholder="Nhập nội dung thông báo..."
              />
              {errors.content && <span className="nfs-field-error">{errors.content}</span>}
            </div>

            <div className="nfs-row">
              <div className="nfs-field">
                <label className="nfs-field-label" htmlFor="nfs-level">Đối tượng</label>
                <select id="nfs-level" className="nfs-select" value={targetLevel} onChange={(e) => setLevel(e.target.value)}>
                  <option value="all">Tất cả học viên</option>
                  {['N5','N4','N3','N2','N1'].map((l) => (
                    <option key={l} value={l}>Học viên {l}</option>
                  ))}
                </select>
              </div>

              <div className="nfs-field">
                <label className="nfs-field-label" htmlFor="nfs-channel">Kênh gửi</label>
                <select id="nfs-channel" className="nfs-select" value={channel} onChange={(e) => setChannel(e.target.value)}>
                  {CHANNEL_OPTIONS.map((c) => (
                    <option key={c.value} value={c.value}>{c.label}</option>
                  ))}
                </select>
              </div>

              <div className="nfs-field">
                <label className="nfs-field-label" htmlFor="nfs-schedule">Thời gian gửi</label>
                <select id="nfs-schedule" className="nfs-select" defaultValue="now">
                  <option value="now">Gửi ngay</option>
                  <option value="schedule" disabled>Hẹn giờ (sắp ra mắt)</option>
                </select>
              </div>
            </div>

            <div className="nfs-form-footer">
              <button className="nfs-btn-send" onClick={handleSend} disabled={sending}>
                {sending ? (
                  <>
                    <span className="nfs-spinner" aria-hidden="true" />
                    Đang gửi...
                  </>
                ) : (
                  <>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M22 2L11 13" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      <path d="M22 2L15 22 11 13 2 9l20-7z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    Gửi thông báo
                  </>
                )}
              </button>
            </div>
          </div>
        </section>

        {/* Sent history */}
        <section className="nfs-card">
          <h2 className="nfs-section-title">Lịch sử đã gửi ({sent.length})</h2>
          <div className="nfs-table-wrap">
            <table className="nfs-table">
              <thead>
                <tr>
                  <th>Tiêu đề</th>
                  <th>Đối tượng</th>
                  <th>Kênh</th>
                  <th>Đã gửi</th>
                  <th>Thời gian</th>
                </tr>
              </thead>
              <tbody>
                {sent.map((item) => (
                  <tr key={item.id}>
                    <td className="nfs-title-cell">
                      <div className="nfs-sent-title">{item.title}</div>
                      <div className="nfs-sent-content">{item.content}</div>
                    </td>
                    <td>
                      <span className="nfs-level-tag">
                        {item.targetLevel === 'all' ? 'Tất cả' : item.targetLevel}
                      </span>
                    </td>
                    <td>{CHANNEL_LABELS[item.channel]}</td>
                    <td>
                      <span className="nfs-count">{item.sentCount.toLocaleString()} người</span>
                    </td>
                    <td className="nfs-date">{item.sentAt}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
