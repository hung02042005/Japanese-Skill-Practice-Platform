export default function KanjiInfo({ kanji }) {
  return (
    <div className="kd-info">
      <div className="kd-info-char-block">
        <span className="kd-info-char" lang="ja">{kanji.characterValue}</span>
        <span className="kd-info-stroke-badge">{kanji.strokeCount} nét</span>
      </div>

      <div className="kd-info-readings">
        {kanji.onyomi && (
          <div className="kd-info-row">
            <span className="kd-info-rl">On&apos;yomi</span>
            <span className="kd-info-rv" lang="ja">{kanji.onyomi}</span>
          </div>
        )}
        {kanji.kunyomi && (
          <div className="kd-info-row">
            <span className="kd-info-rl">Kun&apos;yomi</span>
            <span className="kd-info-rv" lang="ja">{kanji.kunyomi}</span>
          </div>
        )}
        <div className="kd-info-row">
          <span className="kd-info-rl">Nghĩa</span>
          <span className="kd-info-rv kd-info-rv--meaning">{kanji.meaning}</span>
        </div>
      </div>

      {kanji.exampleWord && (
        <div className="kd-info-example">
          <span className="kd-info-ex-label">Ví dụ:</span>
          <span className="kd-info-ex-word" lang="ja">{kanji.exampleWord}</span>
          {kanji.exampleReading && (
            <span className="kd-info-ex-reading" lang="ja">({kanji.exampleReading})</span>
          )}
          {kanji.exampleMeaning && (
            <span className="kd-info-ex-meaning">— {kanji.exampleMeaning}</span>
          )}
        </div>
      )}
    </div>
  );
}
