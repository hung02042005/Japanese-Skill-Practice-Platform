import { useState, useMemo } from 'react';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import './Grammar.css';

const LEVELS = ['All', 'N5', 'N4', 'N3', 'N2', 'N1'];

const MOCK_GRAMMAR = [
  { id: 1,  jlptLevel: 'N5', structure: '〜は〜です',        formula: 'N は N です',              meaning: 'A là B — câu khẳng định đơn giản',               exampleJp: '私は学生です。',               exampleVi: 'Tôi là học sinh.' },
  { id: 2,  jlptLevel: 'N5', structure: '〜ます / 〜ません', formula: 'V-stem + ます / ません',     meaning: 'Hành động lịch sự (khẳng định / phủ định)',      exampleJp: '毎日日本語を勉強します。',       exampleVi: 'Tôi học tiếng Nhật mỗi ngày.' },
  { id: 3,  jlptLevel: 'N5', structure: '〜が好きです',       formula: 'N が 好き / 嫌い です',     meaning: 'Thích / ghét cái gì đó',                        exampleJp: '日本語が好きです。',             exampleVi: 'Tôi thích tiếng Nhật.' },
  { id: 4,  jlptLevel: 'N5', structure: '〜たい',            formula: 'V-stem + たい',             meaning: 'Muốn làm gì đó (nguyện vọng người nói)',        exampleJp: '日本に行きたいです。',           exampleVi: 'Tôi muốn đến Nhật.' },
  { id: 5,  jlptLevel: 'N5', structure: '〜てください',       formula: 'V-te + ください',           meaning: 'Yêu cầu lịch sự (Hãy làm...)',                  exampleJp: 'ちょっと待ってください。',       exampleVi: 'Xin hãy đợi một chút.' },
  { id: 6,  jlptLevel: 'N4', structure: '〜ている',           formula: 'V-te + いる',               meaning: 'Đang làm hành động / trạng thái kết quả',       exampleJp: '今、ご飯を食べています。',       exampleVi: 'Bây giờ tôi đang ăn cơm.' },
  { id: 7,  jlptLevel: 'N4', structure: '〜たことがある',     formula: 'V-ta + ことがある',          meaning: 'Từng có kinh nghiệm làm gì',                    exampleJp: '富士山に登ったことがあります。', exampleVi: 'Tôi đã từng leo núi Phú Sĩ.' },
  { id: 8,  jlptLevel: 'N4', structure: '〜てもいいです',     formula: 'V-te + もいいです / もいいか', meaning: 'Được phép làm gì / Xin phép',                  exampleJp: 'ここで写真を撮ってもいいですか。', exampleVi: 'Tôi có thể chụp ảnh ở đây không?' },
  { id: 9,  jlptLevel: 'N3', structure: '〜ばかり',          formula: 'V-ta + ばかり / N + ばかり', meaning: 'Vừa mới... / chỉ toàn...',                      exampleJp: '今来たばかりです。',             exampleVi: 'Vừa mới đến.' },
  { id: 10, jlptLevel: 'N3', structure: '〜ながら',          formula: 'V-stem + ながら',            meaning: 'Vừa làm A vừa làm B đồng thời',                 exampleJp: '音楽を聴きながら勉強します。',   exampleVi: 'Vừa học vừa nghe nhạc.' },
  { id: 11, jlptLevel: 'N3', structure: '〜ようにする',       formula: 'V-plain + ようにする',       meaning: 'Cố gắng làm / tập thói quen',                   exampleJp: '毎日運動するようにしています。', exampleVi: 'Tôi cố gắng tập thể dục mỗi ngày.' },
  { id: 12, jlptLevel: 'N2', structure: '〜に違いない',       formula: 'V / A / N + に違いない',     meaning: 'Chắc chắn là... (suy đoán rất mạnh)',           exampleJp: '彼は疲れているに違いない。',     exampleVi: 'Chắc chắn anh ấy đang mệt.' },
  { id: 13, jlptLevel: 'N2', structure: '〜わけにはいかない', formula: 'V-plain + わけにはいかない', meaning: 'Không thể làm... (vì lý do đạo đức / xã hội)',  exampleJp: '約束したから行かないわけにはいかない。', exampleVi: 'Đã hứa rồi nên không thể không đến.' },
  { id: 14, jlptLevel: 'N1', structure: '〜ざるを得ない',     formula: 'V-neg-stem + ざるを得ない',  meaning: 'Buộc phải làm... / không thể không...',          exampleJp: '残業せざるを得ない状況だ。',     exampleVi: 'Buộc phải ở lại làm thêm giờ.' },
  { id: 15, jlptLevel: 'N1', structure: '〜にもかかわらず',   formula: 'V / A / N + にもかかわらず', meaning: 'Mặc dù... nhưng vẫn... (nghịch lý, văn viết)',  exampleJp: '雨にもかかわらず試合が続いた。', exampleVi: 'Mặc dù trời mưa, trận đấu vẫn tiếp tục.' },
];

export default function Grammar() {
  const [level,    setLevel]    = useState('All');
  const [search,   setSearch]   = useState('');
  const [expanded, setExpanded] = useState(null);

  const filtered = useMemo(() => {
    let list = MOCK_GRAMMAR;
    if (level !== 'All') list = list.filter((g) => g.jlptLevel === level);
    const q = search.trim().toLowerCase();
    if (q) {
      list = list.filter(
        (g) =>
          g.structure.toLowerCase().includes(q) ||
          g.meaning.toLowerCase().includes(q) ||
          g.formula.toLowerCase().includes(q) ||
          g.exampleJp.includes(q)
      );
    }
    return list;
  }, [level, search]);

  function toggle(id) {
    setExpanded((prev) => (prev === id ? null : id));
  }

  return (
    <div className="grm-page">
      <TopNav activeTab="grammar" />
      <main className="grm-body">
        <div className="grm-header">
          <h1 className="grm-title">Ngữ Pháp</h1>
          <p className="grm-subtitle">Tổng hợp cấu trúc ngữ pháp theo cấp độ JLPT</p>
        </div>

        <div className="grm-controls">
          <div className="grm-levels">
            {LEVELS.map((l) => (
              <button
                key={l}
                className={`grm-lvl-btn${level === l ? ' grm-lvl-btn--active' : ''}`}
                onClick={() => setLevel(l)}
              >
                {l}
              </button>
            ))}
          </div>
          <div className="grm-search-wrap">
            <svg className="grm-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2" />
              <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
            <input
              className="grm-search"
              type="search"
              placeholder="Tìm cấu trúc, ý nghĩa..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        <p className="grm-count-label">{filtered.length} cấu trúc</p>

        {filtered.length === 0 ? (
          <EmptyState
            title="Không tìm thấy cấu trúc nào"
            subtitle="Thử tìm kiếm với từ khóa khác hoặc đổi cấp độ."
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <div className="grm-list">
            {filtered.map((g) => (
              <div
                key={g.id}
                className={`grm-card${expanded === g.id ? ' grm-card--open' : ''}`}
                onClick={() => toggle(g.id)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => e.key === 'Enter' && toggle(g.id)}
                aria-expanded={expanded === g.id}
              >
                <div className="grm-card-main">
                  <div className="grm-card-left">
                    <JlptBadge level={g.jlptLevel} />
                    <span className="grm-structure">{g.structure}</span>
                  </div>
                  <div className="grm-card-right">
                    <span className="grm-meaning">{g.meaning}</span>
                    <svg
                      className={`grm-chevron${expanded === g.id ? ' grm-chevron--up' : ''}`}
                      width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"
                    >
                      <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  </div>
                </div>

                {expanded === g.id && (
                  <div className="grm-card-detail" onClick={(e) => e.stopPropagation()}>
                    <div className="grm-formula-row">
                      <span className="grm-formula-label">Công thức:</span>
                      <code className="grm-formula">{g.formula}</code>
                    </div>
                    <div className="grm-example">
                      <p className="grm-example-jp">{g.exampleJp}</p>
                      <p className="grm-example-vi">{g.exampleVi}</p>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
