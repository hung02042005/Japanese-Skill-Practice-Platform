import GoogleLogo from './GoogleLogo';

export default function GoogleButton({ onClick, children, ariaLabel }) {
  return (
    <button
      className="btn-google"
      type="button"
      onClick={onClick}
      aria-label={ariaLabel}
    >
      <GoogleLogo />
      {children}
    </button>
  );
}
