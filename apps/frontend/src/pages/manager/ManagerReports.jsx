import ManagerTopNav from '../../components/layout/ManagerTopNav';
import { EmptyState } from '../../components/common/EmptyState';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './ManagerReports.css';

export default function ManagerReports() {
  return (
    <div className="mrp-page">
      <ManagerTopNav activeTab="manager-reports" />

      <main className="mrp-body">
        <StaffPageHero
          accent="gold"
          title="Báo Cáo & Phân Tích"
          subtitle="Tổng quan hiệu quả nội dung, năng suất nhóm và phân bố theo cấp độ JLPT"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M10 6h22l8 8v28a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z"/>
              <polyline points="32 6 32 14 40 14"/>
              <polyline points="16 30 20 26 24.5 30 32 22"/>
              <line x1="16" y1="35" x2="28" y2="35"/>
            </svg>
          }
        />

        <EmptyState
          title="Chưa có dữ liệu báo cáo"
          subtitle="Backend chưa cung cấp API thống kê nội dung/Staff theo thời gian."
          mascotVariant="thinking"
        />
      </main>
    </div>
  );
}
