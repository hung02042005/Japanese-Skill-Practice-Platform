import { useState, useEffect }                    from 'react';
import { useLocation, useNavigate }               from 'react-router-dom';
import AdminTopNav                                from '../../components/layout/AdminTopNav';
import { AdminPageHeader }                        from '../../components/admin/AdminPageHeader';
import { ToastContainer, useToast }              from '../../components/common/Toast';
import { IcWrench, IcMail, IcShield, IcBell }   from '../../components/admin/ManageUsersIcons';
import { SystemTab }        from '../../components/admin/settings/SystemTab';
import { EmailTab }         from '../../components/admin/settings/EmailTab';
import { SecurityTab }      from '../../components/admin/settings/SecurityTab';
import { NotificationsTab } from '../../components/admin/settings/NotificationsTab';
import './AdminSettings.css';

const TABS = [
  { id: 'system',        label: 'Hệ thống',  icon: <IcWrench /> },
  { id: 'email',         label: 'Email',      icon: <IcMail />   },
  { id: 'security',      label: 'Bảo mật',   icon: <IcShield /> },
  { id: 'notifications', label: 'Thông báo', icon: <IcBell />   },
];

export default function AdminSettings() {
  const location = useLocation();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const getTab = () => new URLSearchParams(location.search).get('tab') ?? 'system';
  const [activeTab, setActiveTab] = useState(getTab);

  useEffect(() => { setActiveTab(getTab()); }, [location.search]); // eslint-disable-line

  function switchTab(id) {
    setActiveTab(id);
    navigate(`/admin/settings?tab=${id}`, { replace: true });
  }

  return (
    <div className="ast-page">
      <AdminTopNav activeTab="settings" />

      <AdminPageHeader
        chipIcon={<IcWrench />}
        chipLabel="Cài đặt"
        title="Cài Đặt Hệ Thống"
        subtitle="Cấu hình SMTP, bảo mật, bảo trì và quy tắc thông báo tự động"
        mascotVariant="idle"
        mascotSize={100}
      />

      <main className="ast-body">
        {/* Tab bar */}
        <nav className="ast-tabs" role="tablist" aria-label="Phân mục cài đặt">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              role="tab"
              aria-selected={activeTab === tab.id}
              aria-controls={`ast-panel-${tab.id}`}
              className={`ast-tab${activeTab === tab.id ? ' ast-tab--active' : ''}`}
              onClick={() => switchTab(tab.id)}
            >
              {tab.icon}
              <span>{tab.label}</span>
            </button>
          ))}
        </nav>

        {/* Tab panels — mount chỉ tab đang active */}
        {activeTab === 'system'        && <SystemTab        addToast={addToast} />}
        {activeTab === 'email'         && <EmailTab         addToast={addToast} />}
        {activeTab === 'security'      && <SecurityTab      addToast={addToast} />}
        {activeTab === 'notifications' && <NotificationsTab addToast={addToast} />}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
