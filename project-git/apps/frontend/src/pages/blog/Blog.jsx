import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '../home/sections/TopBar';
import FooterSection from '../home/sections/FooterSection';
import BlogCard from './BlogCard';
import './Blog.css';

const CATEGORIES = ['Tất cả', 'Kanji', 'Ngữ pháp', 'Từ vựng', 'Luyện thi', 'Mẹo học'];

const BLOG_POSTS = [
  {
    id: 1,
    category: 'Kanji',
    emoji: '漢',
    emojiColor: '#E8637A',
    emojiBg: '#FFF0F3',
    title: '10 Kanji N5 quan trọng nhất bạn cần biết',
    excerpt:
      'Nếu bạn mới bắt đầu học tiếng Nhật, đây là 10 chữ Kanji N5 xuất hiện nhiều nhất trong cuộc sống hàng ngày và kỳ thi JLPT mà bạn không thể bỏ qua.',
    author: 'Saku-chan',
    date: '28 tháng 5, 2026',
    readTime: '5 phút',
    featured: true,
  },
  {
    id: 2,
    category: 'Mẹo học',
    emoji: '💡',
    emojiColor: '#B45309',
    emojiBg: '#FFFDE7',
    title: 'Spaced Repetition — Bí quyết nhớ 1000 từ vựng',
    excerpt:
      'Thuật toán Spaced Repetition (SR) là cách khoa học nhất để ghi nhớ từ vựng dài hạn. Cùng khám phá cách SakuJi áp dụng SR để tối ưu thời gian học của bạn.',
    author: 'Sensei Minh',
    date: '22 tháng 5, 2026',
    readTime: '8 phút',
  },
  {
    id: 3,
    category: 'Ngữ pháp',
    emoji: '📐',
    emojiColor: '#3730A3',
    emojiBg: '#EEF2FF',
    title: 'Phân biệt は và が — Hướng dẫn chi tiết',
    excerpt:
      'Một trong những điểm ngữ pháp gây nhầm lẫn nhất là sự khác biệt giữa trợ từ は (wa) và が (ga). Bài viết này sẽ giải thích rõ ràng bằng ví dụ thực tế.',
    author: 'Sensei Hoa',
    date: '18 tháng 5, 2026',
    readTime: '10 phút',
  },
  {
    id: 4,
    category: 'Luyện thi',
    emoji: '📝',
    emojiColor: '#C2410C',
    emojiBg: '#FFF7ED',
    title: 'Chiến lược ôn thi JLPT N3 trong 3 tháng',
    excerpt:
      'Bạn muốn đạt JLPT N3 sau 3 tháng? Đây là lộ trình học chi tiết theo từng tuần mà hơn 500 học viên SakuJi đã áp dụng thành công trong kỳ thi gần nhất.',
    author: 'Saku-chan',
    date: '14 tháng 5, 2026',
    readTime: '12 phút',
  },
  {
    id: 5,
    category: 'Từ vựng',
    emoji: '🍱',
    emojiColor: '#6B21A8',
    emojiBg: '#F3E8FF',
    title: 'Từ vựng chủ đề ăn uống tiếng Nhật (N5–N4)',
    excerpt:
      'Hôm nay cùng học từ vựng về ăn uống trong tiếng Nhật — chủ đề quen thuộc và thường xuất hiện trong kỳ thi JLPT N5 và N4 với tần suất rất cao.',
    author: 'Sensei Lan',
    date: '10 tháng 5, 2026',
    readTime: '6 phút',
  },
  {
    id: 6,
    category: 'Mẹo học',
    emoji: '🎯',
    emojiColor: '#0F766E',
    emojiBg: '#F0FDFA',
    title: 'Shadowing — Phương pháp luyện nói hiệu quả nhất',
    excerpt:
      'Shadowing là kỹ thuật luyện nghe và nói bằng cách bắt chước ngay lập tức người bản ngữ. Đây là phương pháp học phát âm tiếng Nhật nhanh nhất hiện nay.',
    author: 'Sensei Minh',
    date: '5 tháng 5, 2026',
    readTime: '7 phút',
  },
];

function Blog() {
  const navigate = useNavigate();
  const [activeCategory, setActiveCategory] = useState('Tất cả');
  const [email, setEmail] = useState('');

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 }
    );
    document.querySelectorAll('.reveal').forEach((el) => observer.observe(el));
    return () => observer.disconnect();
  }, []);

  const filtered =
    activeCategory === 'Tất cả'
      ? BLOG_POSTS
      : BLOG_POSTS.filter((p) => p.category === activeCategory);

  const [featuredPost, ...restPosts] = filtered;

  return (
    <div className="blog-root">
      <TopBar />
      <main>
        <section className="blog-hero">
          <div className="blog-hero-inner">
            <span className="blog-hero-badge">📖 Blog</span>
            <h1 className="blog-hero-title">
              Mẹo học tiếng Nhật
              <br />
              <span className="blog-hero-accent">từ cộng đồng SakuJi</span>
            </h1>
            <p className="blog-hero-sub">
              Bài viết về Kanji, ngữ pháp, từ vựng và bí quyết luyện thi JLPT
              do đội ngũ giảng viên và học viên SakuJi chia sẻ mỗi tuần.
            </p>
          </div>
        </section>

        <section className="blog-content">
          <div className="blog-content-inner">
            <div
              className="blog-categories"
              role="tablist"
              aria-label="Lọc bài viết theo danh mục"
            >
              {CATEGORIES.map((cat) => (
                <button
                  key={cat}
                  role="tab"
                  aria-selected={activeCategory === cat}
                  className={`blog-cat-btn ${
                    activeCategory === cat ? 'blog-cat-btn--active' : ''
                  }`}
                  onClick={() => setActiveCategory(cat)}
                >
                  {cat}
                </button>
              ))}
            </div>

            {filtered.length === 0 ? (
              <div className="blog-empty" role="status">
                <span className="blog-empty-icon" aria-hidden="true">🌸</span>
                <p>Chưa có bài viết trong danh mục này. Hãy quay lại sớm nhé!</p>
              </div>
            ) : (
              <>
                {featuredPost && (
                  <div className="blog-featured reveal">
                    <BlogCard post={featuredPost} featured />
                  </div>
                )}
                {restPosts.length > 0 && (
                  <div className="blog-grid">
                    {restPosts.map((post, i) => (
                      <div
                        key={post.id}
                        className="reveal"
                        style={{ transitionDelay: `${i * 0.07}s` }}
                      >
                        <BlogCard post={post} />
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        </section>

        <section className="blog-newsletter">
          <div className="blog-newsletter-inner reveal">
            <div className="blog-newsletter-mascot" aria-hidden="true">
              <svg width="64" height="64" viewBox="0 0 80 80" fill="none">
                <circle cx="40" cy="46" r="26" fill="#FFF0F3" stroke="#F4A7B3" strokeWidth="1.5"/>
                <ellipse cx="28" cy="19" rx="10" ry="16" fill="#E8637A" transform="rotate(-22 28 19)"/>
                <ellipse cx="40" cy="15" rx="10" ry="17" fill="#E8637A"/>
                <ellipse cx="52" cy="19" rx="10" ry="16" fill="#E8637A" transform="rotate(22 52 19)"/>
                <circle cx="40" cy="27" r="6" fill="#F4A7B3"/>
                <circle cx="33" cy="45" r="2.5" fill="#2D2D2D"/>
                <circle cx="47" cy="45" r="2.5" fill="#2D2D2D"/>
                <path d="M34 53 Q40 58 46 53" stroke="#2D2D2D" strokeWidth="2" strokeLinecap="round" fill="none"/>
              </svg>
            </div>
            <h2 className="blog-newsletter-title">Nhận bài viết mới nhất</h2>
            <p className="blog-newsletter-sub">
              Đăng ký nhận mẹo học tiếng Nhật miễn phí mỗi tuần từ Saku-chan.
            </p>
            <form
              className="blog-newsletter-form"
              onSubmit={(e) => {
                e.preventDefault();
                navigate('/register');
              }}
              aria-label="Form đăng ký nhận bản tin"
            >
              <input
                type="email"
                className="blog-newsletter-input"
                placeholder="Email của bạn..."
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                aria-label="Địa chỉ email đăng ký nhận bản tin"
              />
              <button className="blog-newsletter-btn" type="submit">
                Đăng ký
              </button>
            </form>
          </div>
        </section>
      </main>
      <FooterSection />
    </div>
  );
}

export default Blog;
