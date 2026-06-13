export default function AppLogo({ size = 28, className = '' }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 28 28"
      fill="none"
      className={className}
      aria-hidden="true"
    >
      <circle cx="14" cy="17" r="9" fill="#FFF0F3" stroke="#F4A7B3" strokeWidth="1.2"/>
      <ellipse cx="10" cy="9" rx="5" ry="8" fill="#E8637A" transform="rotate(-20 10 9)"/>
      <ellipse cx="14" cy="7" rx="5" ry="8.5" fill="#E8637A"/>
      <ellipse cx="18" cy="9" rx="5" ry="8" fill="#E8637A" transform="rotate(20 18 9)"/>
      <circle cx="14" cy="12" r="3" fill="#F4A7B3"/>
      <circle cx="11.5" cy="17" r="2" fill="#2D2D2D"/>
      <circle cx="16.5" cy="17" r="2" fill="#2D2D2D"/>
      <ellipse cx="9" cy="20" rx="3.5" ry="2" fill="#E8637A" opacity="0.25"/>
      <ellipse cx="19" cy="20" rx="3.5" ry="2" fill="#E8637A" opacity="0.25"/>
    </svg>
  );
}
