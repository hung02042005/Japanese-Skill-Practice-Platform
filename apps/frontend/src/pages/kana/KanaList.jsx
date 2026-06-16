import { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import KanaDetailModal from '../../components/student/KanaDetailModal';
import { getKanaList, markKanaComplete } from '../../api/studentService';
import './KanaList.css';

const SCRIPTS = [
  { id: 'hiragana', label: 'Hiragana' },
  { id: 'katakana', label: 'Katakana' },
];

export default function KanaList() {
  const [searchParams] = useSearchParams();
  const [script,    setScript]  = useState(searchParams.get('script') ?? 'hiragana');
  const [chars,     setChars]   = useState([]);
  const [stats,     setStats]   = useState({ completed: 0, total: 46 });
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');
  const [selected,  setSelected]= useState(null);
  const [isSaving,  setSaving]  = useState(false);

  useEffect(() => {
    setLoading(true);
    setError('');
    getKanaList(script)
      .then((data) => {
        setChars(data.characters);
        setStats({ completed: data.completedCount, total: data.totalCount });
      })
      .catch((err) => setError(err?.response?.data?.message ?? 'Không thể tải bảng chữ Kana.'))
      .finally(() => setLoading(false));
  }, [script]);

  const rows = useMemo(() => {
    const map = {};
    chars.forEach((c) => {
      const key = c.rowGroup ?? '1';
      if (!map[key]) map[key] = [];
      map[key].push(c);
    });
    return Object.entries(map);
  }, [chars]);

  const handleComplete = async (kana) => {
    if (kana.isCompleted || isSaving) return;
    setSaving(true);
    try {
      await markKanaComplete(kana.kanaId);
      setChars((prev) =>
        prev.map((c) => (c.kanaId === kana.kanaId ? { ...c, isCompleted: true } : c))
      );
      setStats((prev) => ({ ...prev, completed: prev.completed + 1 }));
      setSelected((prev) => (prev ? { ...prev, isCompleted: true } : null));
    } finally {
      setSaving(false);
    }
  };

  const progressPct = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <div className="kna-page">
      <TopNav activeTab="kana" />
      <main className="kna-body">
        <div className="kna-header">
          <h1 className="kna-title"><span lang="ja">かな</span> Kana</h1>
          <p className="kna-subtitle">Học bảng chữ Hiragana và Katakana cơ bản.</p>
        </div>

        <div className="kna-tabs" role="tablist" aria-label="Chọn bảng chữ">
          {SCRIPTS.map((s) => (
            <button
              key={s.id}
              role="tab"
              aria-selected={script === s.id}
              className={`kna-tab${script === s.id ? ' kna-tab--active' : ''}`}
              onClick={() => setScript(s.id)}
            >
              {s.label}
            </button>
          ))}
        </div>

        {!isLoading && (
          <div className="kna-progress-bar">
            <span className="kna-progress-text">
              Đã học <strong>{stats.completed}</strong> / {stats.total}
            </span>
            <div className="kna-progress-track">
              <ProgressBar value={progressPct} />
            </div>
            <span className="kna-progress-pct">{progressPct}%</span>
          </div>
        )}

        {error && (
          <div className="kna-error" role="alert">
            {error}
            <button className="kna-retry" onClick={() => setScript((s) => s)}>Thử lại</button>
          </div>
        )}

        {isLoading ? (
          <div className="kna-skel" aria-hidden="true">
            {Array.from({ length: 10 }).map((_, i) => (
              <div key={i} className="kna-skel-row" />
            ))}
          </div>
        ) : (
          <div className="kna-table">
            {rows.map(([rowName, cells]) => (
              <div key={rowName} className="kna-row">
                <span className="kna-row-label">{rowName}</span>
                <div className="kna-row-cells">
                  {cells.map((c) => (
                    <button
                      key={c.kanaId}
                      className={`kna-cell${c.isCompleted ? ' kna-cell--done' : ''}`}
                      onClick={() => setSelected(c)}
                      aria-label={`${c.character} (${c.romaji})${c.isCompleted ? ' — đã học' : ''}`}
                    >
                      <span className="kna-char" lang="ja">{c.character}</span>
                      <span className="kna-romaji">{c.romaji}</span>
                      {c.isCompleted && <span className="kna-tick" aria-hidden="true">✓</span>}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {selected && (
        <KanaDetailModal
          kana={selected}
          isSaving={isSaving}
          onComplete={handleComplete}
          onClose={() => setSelected(null)}
        />
      )}
    </div>
  );
}
