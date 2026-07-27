import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch } from '@/store/hooks';
import { setUser } from '@/features/auth/authSlice';
import AppLogo from '@/shared/components/common/AppLogo';
import SakuChan from '@/shared/components/common/SakuChan';
import { submitOnboarding } from '@/shared/api/studentService';
import { useToast } from '@/shared/context/ToastContext';
import { getErrorMessage } from '@/shared/utils/apiMessage';
import './Onboarding.css';

const JLPT_LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

const DAILY_OPTIONS = [
  { value: 5, label: '5 phút' },
  { value: 10, label: '10 phút', recommended: true },
  { value: 15, label: '15 phút' },
  { value: 20, label: '20 phút' },
];

const SKILL_OPTIONS = [
  { id: 'kanji', label: 'Kanji' },
  { id: 'vocabulary', label: 'Từ vựng' },
  { id: 'grammar', label: 'Ngữ pháp' },
  { id: 'all', label: 'Tất cả' },
];

export default function Onboarding() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { error: toastError } = useToast();

  const [step, setStep] = useState(1);
  const [jlptGoal, setJlpt] = useState('N5');
  const [dailyMinutes, setMins] = useState(10);
  const [focusSkills, setSkills] = useState(['all']);
  const [isSubmitting, setSubmit] = useState(false);

  function toggleSkill(id) {
    if (id === 'all') { setSkills(['all']); return; }
    setSkills((prev) => {
      const next = prev.filter((s) => s !== 'all');
      return next.includes(id) ? next.filter((s) => s !== id) : [...next, id];
    });
  }

  async function handleFinish() {
    if (focusSkills.length === 0) return;
    setSubmit(true);
    try {
      const updated = await submitOnboarding({ jlptGoal, dailyMinutes, focusSkills });
      // Đồng bộ user (currentJlptLevel mới + onboardingCompleted=true) để Dashboard hiển thị đúng cấp độ.
      dispatch(setUser(updated));
      navigate('/dashboard', { replace: true });
    } catch (err) {
      toastError(getErrorMessage(err, 'Không lưu được lựa chọn onboarding. Vui lòng thử lại.'));
    } finally {
      setSubmit(false);
    }
  }

  return (
    <div className="onb-page">
      <div className="onb-topbar">
        <AppLogo size={28} />
        <span className="onb-brand">SakuJi</span>
      </div>

      <div className="onb-card">
        <div className="onb-steps" aria-label={`Bước ${step} trên 3`}>
          {[1, 2, 3].map((s) => (
            <div
              key={s}
              className={`onb-dot${s === step ? ' onb-dot--active' : s < step ? ' onb-dot--done' : ''}`}
            />
          ))}
        </div>

        <div className="onb-mascot">
          <SakuChan variant={step === 3 ? 'celebrate' : 'happy'} size={120} />
        </div>

        {step === 1 && (
          <div className="onb-step">
            <h1 className="onb-title">Bạn muốn thi JLPT cấp độ nào?</h1>
            <p className="onb-subtitle">Chúng tôi sẽ cá nhân hoá lộ trình học cho bạn.</p>
            <div className="onb-level-grid" role="radiogroup" aria-label="Chọn JLPT level">
              {JLPT_LEVELS.map((lvl) => (
                <button
                  key={lvl}
                  type="button"
                  role="radio"
                  aria-checked={jlptGoal === lvl}
                  className={`onb-level-btn${jlptGoal === lvl ? ' onb-level-btn--active' : ''}`}
                  onClick={() => setJlpt(lvl)}
                >
                  {lvl}
                </button>
              ))}
            </div>
            <button className="onb-btn onb-btn--primary" onClick={() => setStep(2)}>
              Tiếp theo →
            </button>
          </div>
        )}

        {step === 2 && (
          <div className="onb-step">
            <h1 className="onb-title">Mỗi ngày bạn có bao nhiêu thời gian?</h1>
            <p className="onb-subtitle">Chúng tôi sẽ gợi ý số bài học phù hợp.</p>
            <div className="onb-time-grid" role="radiogroup" aria-label="Chọn thời gian học">
              {DAILY_OPTIONS.map((opt) => (
                <label
                  key={opt.value}
                  className={`onb-time-opt${dailyMinutes === opt.value ? ' onb-time-opt--active' : ''}`}
                >
                  <input
                    type="radio"
                    name="daily"
                    value={opt.value}
                    checked={dailyMinutes === opt.value}
                    onChange={() => setMins(opt.value)}
                    className="onb-sr-only"
                  />
                  {opt.label}
                  {opt.recommended && <span className="onb-recommend-badge">Đề xuất</span>}
                </label>
              ))}
            </div>
            <div className="onb-footer-btns">
              <button className="onb-btn onb-btn--ghost" onClick={() => setStep(1)}>← Quay lại</button>
              <button className="onb-btn onb-btn--primary" onClick={() => setStep(3)}>Tiếp theo →</button>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="onb-step">
            <h1 className="onb-title">Kỹ năng nào bạn muốn tập trung?</h1>
            <p className="onb-subtitle">Có thể chọn nhiều hoặc chọn &quot;Tất cả&quot;.</p>
            <div className="onb-skill-grid" role="group" aria-label="Chọn kỹ năng">
              {SKILL_OPTIONS.map((sk) => (
                <button
                  key={sk.id}
                  type="button"
                  aria-pressed={focusSkills.includes(sk.id)}
                  className={`onb-skill-btn${focusSkills.includes(sk.id) ? ' onb-skill-btn--active' : ''}`}
                  onClick={() => toggleSkill(sk.id)}
                >
                  {focusSkills.includes(sk.id) && <span aria-hidden="true">✓ </span>}
                  {sk.label}
                </button>
              ))}
            </div>
            <div className="onb-footer-btns">
              <button className="onb-btn onb-btn--ghost" onClick={() => setStep(2)}>← Quay lại</button>
              <button
                className="onb-btn onb-btn--primary"
                onClick={handleFinish}
                disabled={isSubmitting || focusSkills.length === 0}
                aria-busy={isSubmitting}
              >
                {isSubmitting && <span className="onb-spinner onb-spinner--white" aria-hidden="true" />}
                {isSubmitting ? 'Đang lưu…' : 'Bắt đầu học!'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
