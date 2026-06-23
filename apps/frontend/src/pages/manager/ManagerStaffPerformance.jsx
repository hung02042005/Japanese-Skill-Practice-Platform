import ManagerTopNav from '../../components/layout/ManagerTopNav';
import { EmptyState } from '../../components/common/EmptyState';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './ManagerStaffPerformance.css';

export default function ManagerStaffPerformance() {
  return (
    <div className="msp-page">
      <ManagerTopNav activeTab="manager-staff-performance" />

      <main className="msp-body">
        <StaffPageHero
          accent="green"
          title="Hiệu Suất Staff"
          subtitle="Theo dõi năng suất, tỷ lệ duyệt và hoạt động của từng thành viên trong nhóm"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <rect x="4" y="26" width="8" height="18" rx="2"/>
              <rect x="19" y="16" width="8" height="28" rx="2"/>
              <rect x="34" y="6" width="8" height="38" rx="2"/>
              <polyline points="6 24 23 14 38 5" strokeDasharray="3 2"/>
            </svg>
          }
        />

        <EmptyState
          title="Chưa có dữ liệu hiệu suất Staff"
          subtitle="Backend chưa cung cấp API thống kê hiệu suất theo từng Staff."
          mascotVariant="thinking"
        />
      </main>
    </div>
  );
}
