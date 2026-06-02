import { useState, useEffect, useCallback } from 'react';
import { EmptyState }                        from '../../common/EmptyState';
import { IcPlus, IcEdit, IcTrash }           from '../ManageUsersIcons';
import { RulePanelModal, MILESTONES }        from './RulePanelModal';
import {
  getNotificationRules,
  deleteNotificationRule,
} from '../../../api/adminService';

function milestoneLabel(m) {
  return MILESTONES.find((x) => x.value === m)?.label ?? m;
}
function channelLabel(ch) {
  return ch === 'email' ? 'Email' : 'Trong app';
}

function RuleCard({ rule, onEdit, onDelete, isDeleting }) {
  return (
    <div className="ast-rule-card">
      <div className="ast-rule-body">
        <div className="ast-rule-chips">
          <span className="ast-chip ast-chip--milestone">{milestoneLabel(rule.milestone)}</span>
          <span className={`ast-chip ast-chip--channel ast-chip--${rule.channel ?? 'in_app'}`}>
            {channelLabel(rule.channel)}
          </span>
        </div>
        <p className="ast-rule-title">{rule.templateTitle}</p>
        <p className="ast-rule-content">{rule.templateContent}</p>
      </div>

      <div className="ast-rule-actions">
        <button
          className="ast-rule-btn ast-rule-btn--edit"
          onClick={() => onEdit(rule)}
          aria-label={`Chỉnh sửa: ${rule.templateTitle}`}
        >
          <IcEdit />
        </button>
        <button
          className="ast-rule-btn ast-rule-btn--delete"
          onClick={() => onDelete(rule.ruleId)}
          disabled={isDeleting}
          aria-label={`Xóa: ${rule.templateTitle}`}
        >
          {isDeleting
            ? <span className="ast-spinner ast-spinner--small" aria-hidden="true" />
            : <IcTrash />}
        </button>
      </div>
    </div>
  );
}

function RuleSkeleton() {
  return (
    <div className="ast-rule-list">
      {[1, 2, 3].map((i) => (
        <div key={i} className="ast-rule-skel" aria-hidden="true" />
      ))}
    </div>
  );
}

export function NotificationsTab({ addToast }) {
  const [rules,     setRules]    = useState([]);
  const [isLoading, setLoading]  = useState(true);
  const [panelRule, setPanelRule]= useState(null); // null | 'new' | ruleObj
  const [deleting,  setDeleting] = useState(null); // ruleId being deleted

  const load = useCallback(() => {
    setLoading(true);
    getNotificationRules()
      .then((data) => setRules(Array.isArray(data) ? data : data?.content ?? []))
      .catch(() => setRules([]))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  async function handleDelete(ruleId) {
    if (!window.confirm('Bạn có chắc muốn xóa quy tắc này?')) return;
    setDeleting(ruleId);
    try {
      await deleteNotificationRule(ruleId);
      setRules((prev) => prev.filter((r) => r.ruleId !== ruleId));
      addToast('success', 'Đã xóa quy tắc thông báo');
    } catch {
      addToast('error', 'Xóa thất bại');
    } finally {
      setDeleting(null);
    }
  }

  function handleSaved(rule, isEdit) {
    setRules((prev) =>
      isEdit ? prev.map((r) => (r.ruleId === rule.ruleId ? rule : r)) : [...prev, rule]
    );
    setPanelRule(null);
    addToast('success', isEdit ? 'Đã cập nhật quy tắc' : 'Đã tạo quy tắc thông báo mới');
  }

  return (
    <div className="ast-notif">
      {/* Header */}
      <div className="ast-notif-header">
        <div>
          <h3 className="ast-section-title">Quy tắc thông báo tự động</h3>
          <p className="ast-section-desc">
            Tự động gửi thông báo đến học viên khi đạt mốc học tập.
          </p>
        </div>
        <button className="ast-btn ast-btn--primary" onClick={() => setPanelRule('new')}>
          <IcPlus />
          Tạo quy tắc mới
        </button>
      </div>

      {/* List */}
      {isLoading ? (
        <RuleSkeleton />
      ) : rules.length === 0 ? (
        <EmptyState
          title="Chưa có quy tắc thông báo"
          subtitle="Tạo quy tắc đầu tiên để tự động gửi thông báo đến học viên."
          mascotVariant="thinking"
          mascotSize={120}
        >
          <button
            className="ast-btn ast-btn--primary"
            style={{ marginTop: 16 }}
            onClick={() => setPanelRule('new')}
          >
            <IcPlus /> Tạo quy tắc đầu tiên
          </button>
        </EmptyState>
      ) : (
        <div className="ast-rule-list">
          {rules.map((rule) => (
            <RuleCard
              key={rule.ruleId}
              rule={rule}
              onEdit={setPanelRule}
              onDelete={handleDelete}
              isDeleting={deleting === rule.ruleId}
            />
          ))}
        </div>
      )}

      {/* Modal */}
      {panelRule !== null && (
        <RulePanelModal
          rule={panelRule === 'new' ? null : panelRule}
          onClose={() => setPanelRule(null)}
          onSaved={handleSaved}
          addToast={addToast}
        />
      )}
    </div>
  );
}
