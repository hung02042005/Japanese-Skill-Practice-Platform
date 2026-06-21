import { JlptBadge } from '../common/Badges';

/**
 * Chi tiết một mẫu ngữ pháp từ kết quả tra cứu — SPEC-dictionary §6/§7.
 * Dùng inline (không điều hướng) vì payload search đã đủ field: cấu trúc, nghĩa, công thức.
 * Tránh 404 do route /grammar/:id chưa tồn tại.
 */
export default function DictGrammarPanel({ item, onBack }) {
  const { structure, meaning, formula, jlptLevel } = item;

  return (
    <section className="dct-detail" aria-label={`Chi tiết ${structure}`}>
      <button className="dct-detail-back" onClick={onBack}>← Kết quả</button>

      <div className="dct-detail-head">
        <div className="dct-detail-word-wrap">
          {jlptLevel && <JlptBadge level={jlptLevel} />}
          <span className="dct-detail-word" lang="ja">{structure}</span>
        </div>
      </div>

      {meaning && <p className="dct-detail-meaning">{meaning}</p>}

      {formula && (
        <div className="dct-detail-example">
          <p className="dct-detail-formula-label">Công thức</p>
          <p lang="ja">{formula}</p>
        </div>
      )}
    </section>
  );
}
