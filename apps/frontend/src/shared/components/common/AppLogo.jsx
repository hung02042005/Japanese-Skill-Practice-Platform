export default function AppLogo({ size = 28, className = '' }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      fill="none"
      className={className}
      aria-hidden="true"
    >
      <defs>
        <linearGradient id="applogo-petal-grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor="#FF85A1" />
          <stop offset="100%" stopColor="#E04768" />
        </linearGradient>
        <path
          id="applogo-sakura-petal"
          d="M 50 48 C 37 40 28 25 38 13 C 44 7 48 11 50 15 C 52 11 56 7 62 13 C 72 25 63 40 50 48 Z"
          fill="url(#applogo-petal-grad)"
        />
        <g id="applogo-stamen">
          <line x1="50" y1="50" x2="50" y2="31" stroke="#FFFFFF" strokeWidth="1.8" strokeLinecap="round" opacity="0.85" />
          <circle cx="50" cy="30" r="2.2" fill="#FFFFFF" opacity="0.9" />
        </g>
      </defs>
      
      <use href="#applogo-sakura-petal" transform="rotate(0 50 50)" />
      <use href="#applogo-sakura-petal" transform="rotate(72 50 50)" />
      <use href="#applogo-sakura-petal" transform="rotate(144 50 50)" />
      <use href="#applogo-sakura-petal" transform="rotate(216 50 50)" />
      <use href="#applogo-sakura-petal" transform="rotate(288 50 50)" />

      <use href="#applogo-stamen" transform="rotate(0 50 50)" />
      <use href="#applogo-stamen" transform="rotate(72 50 50)" />
      <use href="#applogo-stamen" transform="rotate(144 50 50)" />
      <use href="#applogo-stamen" transform="rotate(216 50 50)" />
      <use href="#applogo-stamen" transform="rotate(288 50 50)" />

      <circle cx="50" cy="50" r="3.5" fill="#FFFFFF" opacity="0.9" />
    </svg>
  );
}
