import './FooterSection.css';

const FOOTER_LINKS = {
  'Sản phẩm': ['Flashcard', 'Kanji', 'Từ điển'],
  'Tài nguyên': ['Blog', 'Tài liệu', 'API docs'],
  'Công ty': ['Về chúng tôi', 'Tuyển dụng', 'Liên hệ'],
};

const SOCIAL_ICONS = [
  { label: 'Facebook', path: 'M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z' },
  { label: 'YouTube', path: 'M22.54 6.42a2.78 2.78 0 0 0-1.95-1.96C18.88 4 12 4 12 4s-6.88 0-8.59.46a2.78 2.78 0 0 0-1.95 1.96A29 29 0 0 0 1 12a29 29 0 0 0 .46 5.58A2.78 2.78 0 0 0 3.41 19.54C5.12 20 12 20 12 20s6.88 0 8.59-.46a2.78 2.78 0 0 0 1.95-1.96A29 29 0 0 0 23 12a29 29 0 0 0-.46-5.58zM9.75 15.02V8.98L15.5 12l-5.75 3.02z' },
  { label: 'TikTok', path: 'M19.59 6.69a4.83 4.83 0 0 1-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 0 1-2.88 2.5 2.89 2.89 0 0 1-2.89-2.89 2.89 2.89 0 0 1 2.89-2.89c.28 0 .54.04.79.1V9.01a6.3 6.3 0 0 0-.79-.05 6.34 6.34 0 0 0-6.34 6.34 6.34 6.34 0 0 0 6.34 6.34 6.34 6.34 0 0 0 6.33-6.34V8.69a8.18 8.18 0 0 0 4.78 1.52V6.78a4.85 4.85 0 0 1-1.01-.09z' },
  { label: 'Discord', path: 'M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057.1 18.079.11 18.1.12 18.113a19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03z' },
];

function FooterSection() {
  return (
    <footer className="footer">
      <div className="footer-top">
        <div className="footer-brand">
          <a href="/" className="footer-logo">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none" aria-hidden="true">
              <circle cx="14" cy="17" r="9" fill="#FFF0F3" stroke="#F4A7B3" strokeWidth="1.2"/>
              <ellipse cx="10" cy="9" rx="5" ry="8" fill="#E8637A" transform="rotate(-20 10 9)"/>
              <ellipse cx="14" cy="7" rx="5" ry="8.5" fill="#E8637A"/>
              <ellipse cx="18" cy="9" rx="5" ry="8" fill="#E8637A" transform="rotate(20 18 9)"/>
              <circle cx="14" cy="12" r="3" fill="#F4A7B3"/>
              <circle cx="11.5" cy="17" r="2" fill="#2D2D2D"/>
              <circle cx="16.5" cy="17" r="2" fill="#2D2D2D"/>
            </svg>
            <span className="footer-logo-text">
              <span className="footer-logo-saku">Saku</span>Ji
            </span>
          </a>

          <p className="footer-tagline">
            Hành trình tiếng Nhật của bạn bắt đầu từ một cánh hoa.
          </p>

          <div className="footer-socials" aria-label="Mạng xã hội">
            {SOCIAL_ICONS.map((icon) => (
              <a
                key={icon.label}
                href="#"
                className="footer-social-btn"
                aria-label={icon.label}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                  <path d={icon.path} />
                </svg>
              </a>
            ))}
          </div>

          <div className="footer-sleeping-saku" aria-label="Saku-chan đang ngủ">
            <svg width="64" height="64" viewBox="0 0 80 80" fill="none">
              <ellipse cx="40" cy="58" rx="28" ry="18" fill="#FFF0F3" stroke="#F4A7B3" strokeWidth="1.5"/>
              <circle cx="40" cy="38" r="24" fill="#FFF0F3" stroke="#F4A7B3" strokeWidth="1.5"/>
              <ellipse cx="28" cy="16" rx="10" ry="16" fill="#E8637A" transform="rotate(-22 28 16)"/>
              <ellipse cx="40" cy="12" rx="10" ry="17" fill="#E8637A"/>
              <ellipse cx="52" cy="16" rx="10" ry="16" fill="#E8637A" transform="rotate(22 52 16)"/>
              <circle cx="40" cy="22" r="6" fill="#F4A7B3"/>
              <path d="M29 38 Q36 44 43 38" stroke="#2D2D2D" strokeWidth="2" strokeLinecap="round" fill="none"/>
              <path d="M45 38 Q49 42 53 38" stroke="#2D2D2D" strokeWidth="2" strokeLinecap="round" fill="none"/>
              <ellipse cx="26" cy="44" rx="10" ry="6" fill="#E8637A" opacity="0.22"/>
              <ellipse cx="54" cy="44" rx="10" ry="6" fill="#E8637A" opacity="0.22"/>
              <text x="58" y="24" fontFamily="Nunito, sans-serif" fontSize="10" fontWeight="700" fill="rgba(255,255,255,0.5)">z</text>
              <text x="64" y="16" fontFamily="Nunito, sans-serif" fontSize="13" fontWeight="700" fill="rgba(255,255,255,0.4)">Z</text>
            </svg>
          </div>
        </div>

        {Object.entries(FOOTER_LINKS).map(([group, links]) => (
          <div key={group} className="footer-links-col">
            <h4 className="footer-links-heading">{group}</h4>
            <ul className="footer-links-list" aria-label={group}>
              {links.map((link) => (
                <li key={link}>
                  <a href="#" className="footer-link">{link}</a>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      <div className="footer-bottom">
        <p className="footer-copy">© 2026 SakuJi. Mọi quyền được bảo lưu.</p>
        <button className="footer-lang" aria-label="Chọn ngôn ngữ">
          🇻🇳 Tiếng Việt ▾
        </button>
      </div>
    </footer>
  );
}

export default FooterSection;
