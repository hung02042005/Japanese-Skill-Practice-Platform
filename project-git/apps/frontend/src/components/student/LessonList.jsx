import LessonCard from './LessonCard';

function LessonList({ lessons }) {
  return (
    <div className="lesson-list">
      {lessons.map((lesson) => (
        <LessonCard key={lesson.id} lesson={lesson} />
      ))}
    </div>
  );
}

export default LessonList;
