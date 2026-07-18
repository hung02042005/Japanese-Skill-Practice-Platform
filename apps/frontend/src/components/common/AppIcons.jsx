/**
 * AppIcons.jsx — barrel re-export cho bộ icon SVG dùng chung (thay cho emoji làm icon).
 *
 * File gốc (554 dòng) đã được tách theo nhóm vào thư mục `./icons/` để tuân thủ giới hạn
 * 300 dòng/file trong CLAUDE.md. Giữ lại barrel này để toàn bộ import site hiện có
 * (`import { X } from '.../AppIcons'`) không phải sửa lại.
 */

export * from './icons/BadgeIcons';
export * from './icons/MediaIcons';
export * from './icons/ObjectIcons';
export * from './icons/StatusIcons';
export * from './icons/FeedbackIcons';
