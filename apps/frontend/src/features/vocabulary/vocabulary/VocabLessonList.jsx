import VocabLessonCard from './VocabLessonCard';

/**
 * VocabLessonList — danh sách bài học theo order_index (FR-VH-03).
 *
 * Props:
 *   lessons — mảng lesson đã sắp xếp
 *   onOpen  — (lesson) => void
 */
export default function VocabLessonList({ lessons = [], onOpen }) {
  return (
    <div className="vh-lesson-list">
      {lessons.map((lesson) => (
        <VocabLessonCard
          key={lesson.topicId}
          lesson={lesson}
          onOpen={onOpen}
        />
      ))}
    </div>
  );
}
