/* ============================================================
   V8 — Chuẩn hoá lại giá trị enum chữ thường trong dữ liệu mock cũ (V2)
   ------------------------------------------------------------
   Lý do: V2__mock_data.sql insert các cột enum (assessment_type,
   status, question_type, skill, parent_type) bằng giá trị chữ
   thường (ví dụ 'quiz', 'published', 'multiple_choice'...), nhưng
   entity Java dùng @Enumerated(EnumType.STRING) với tên hằng số
   chữ HOA (QUIZ, PUBLISHED...). Khi đọc qua JPA/Hibernate sẽ ném
   IllegalArgumentException "No enum constant ...AssessmentType.quiz".
   UPPER() tự thân là idempotent (UPPER(UPPER(x)) = UPPER(x)) nên
   không cần điều kiện IF NOT EXISTS, an toàn khi chạy lại nhiều lần.
   Chỉ chỉnh các bảng mà feature assessment (UC-11) thực sự đọc qua
   JPA — không đụng tới kanji/lesson/student_content_progress (nằm
   ngoài phạm vi UC-11, cần được rà soát riêng nếu cũng bị ảnh hưởng).
   ============================================================ */

UPDATE assessments SET assessment_type = UPPER(assessment_type);
UPDATE assessments SET status = UPPER(status);
GO

UPDATE questions SET question_type = UPPER(question_type);
UPDATE questions SET skill = UPPER(skill);
UPDATE questions SET status = UPPER(status);
GO

UPDATE question_assignments SET parent_type = UPPER(parent_type);
GO
