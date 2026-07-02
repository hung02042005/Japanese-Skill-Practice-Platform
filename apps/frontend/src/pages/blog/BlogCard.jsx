import './Blog.css';

function BlogCard({ post, featured }) {
  return (
    <article
      className={`blog-card ${featured ? 'blog-card--featured' : ''}`}
      aria-label={post.title}
    >
      <div
        className="blog-card-cover"
        style={{ background: post.emojiBg }}
        aria-hidden="true"
      >
        <span
          className="blog-card-cover-emoji"
          style={{ color: post.emojiColor }}
        >
          {post.Icon ? <post.Icon size={44} /> : post.emoji}
        </span>
      </div>
      <div className="blog-card-body">
        <span className="blog-card-category">{post.category}</span>
        <h3 className="blog-card-title">{post.title}</h3>
        <p className="blog-card-excerpt">{post.excerpt}</p>
        <div className="blog-card-meta">
          <span className="blog-card-author">{post.author}</span>
          <span className="blog-card-dot" aria-hidden="true">·</span>
          <time className="blog-card-date">{post.date}</time>
          <span className="blog-card-dot" aria-hidden="true">·</span>
          <span className="blog-card-read">{post.readTime} đọc</span>
        </div>
      </div>
    </article>
  );
}

export default BlogCard;
