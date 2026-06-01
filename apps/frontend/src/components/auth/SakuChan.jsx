export default function SakuChan({ variant = 'idle', size = 80 }) {
  return (
    <div className={`saku-chan${variant !== 'idle' ? ` saku-chan--${variant}` : ''}`} aria-hidden="true">
      <svg width={size} height={size} viewBox="0 0 80 80" fill="none">
        <ellipse cx="26" cy="19" rx="7" ry="11" fill="#E8637A" transform="rotate(-28 26 19)"/>
        <ellipse cx="40" cy="14" rx="7" ry="11" fill="#E8637A"/>
        <ellipse cx="54" cy="19" rx="7" ry="11" fill="#E8637A" transform="rotate(28 54 19)"/>
        <circle  cx="40" cy="26" r="6" fill="#F4A7B3"/>
        <circle cx="40" cy="51" r="26" fill="#FFF0F3"/>
        <circle cx="40" cy="51" r="26" stroke="#F4A7B3" strokeWidth="1.5"/>
        <ellipse cx="25" cy="57" rx="7" ry="5" fill="#F4A7B3" opacity="0.55"/>
        <ellipse cx="55" cy="57" rx="7" ry="5" fill="#F4A7B3" opacity="0.55"/>
        <circle cx="32" cy="49" r="3.5" fill="#2D2D2D"/>
        <circle cx="48" cy="49" r="3.5" fill="#2D2D2D"/>
        <circle cx="33.8" cy="47.4" r="1.2" fill="white"/>
        <circle cx="49.8" cy="47.4" r="1.2" fill="white"/>
        <path d="M34 58 Q40 64 46 58" stroke="#E8637A" strokeWidth="1.8" strokeLinecap="round" fill="none"/>
      </svg>
    </div>
  );
}
