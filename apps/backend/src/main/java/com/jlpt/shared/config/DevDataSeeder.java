/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.config;

import com.jlpt.feature.admin.AdminUser;
import com.jlpt.feature.admin.AdminUserRepository;
import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.assessment.AssessmentRepository;
import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.assessment.QuestionAssignment;
import com.jlpt.feature.assessment.QuestionAssignmentRepository;
import com.jlpt.feature.assessment.QuestionRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DevDataSeeder — chỉ chạy khi profile là "default" hoặc "dev" (H2 in-memory).
 *
 * <p>Tạo tài khoản mẫu khi DB trống để có thể test ngay mà không cần chạy SQL thủ công.
 *
 * <pre>
 *   ADMIN   : admin@sakuji.com    / Admin@123456   (two_factor_enabled = false — bỏ qua TOTP)
 *   STUDENT : student1@sakuji.com / Student@123456
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!prod") // Không chạy ở môi trường production
public class DevDataSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final StudentUserRepository studentUserRepository;
    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedStudent();
        seedQuizzes();
        seedMockExam();
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    private void seedAdmin() {
        String email = "admin@sakuji.com";
        if (adminUserRepository.existsByEmail(email)) {
            log.info("[DevDataSeeder] Admin đã tồn tại — bỏ qua.");
            return;
        }

        AdminUser admin = AdminUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Admin@123456"))
                .fullName("Quản Trị Viên")
                .status(AdminUser.AdminStatus.ACTIVE)
                .loginAttempts(0)
                .build();

        adminUserRepository.save(admin);
        log.info("[DevDataSeeder] ✅ Admin seed thành công: {} / Admin@123456", email);
    }

    // ─── Student mẫu ─────────────────────────────────────────────────────────

    private void seedStudent() {
        String email = "student1@sakuji.com";
        if (studentUserRepository.existsByEmail(email)) {
            log.info("[DevDataSeeder] Student đã tồn tại — bỏ qua.");
            return;
        }

        StudentUser student = StudentUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Student@123456"))
                .fullName("Nguyễn Minh Anh")
                .status(StudentUser.StudentStatus.ACTIVE)
                .emailVerifiedAt(LocalDateTime.now())
                .loginAttempts(0)
                .build();

        studentUserRepository.save(student);
        log.info("[DevDataSeeder] ✅ Student seed thành công: {} / Student@123456", email);
    }

    // ─── Quiz mẫu (mỗi level N5→N1 một bài 5 câu) ──────────────────────────────

    private record QuizQuestionSeed(
            String text, String a, String b, String c, String d, String correct, String explanation) {}

    private void seedQuizzes() {
        seedQuizLevel(
                StudentUser.JlptLevel.N5,
                "N5 - Từ vựng cơ bản (Demo)",
                "vocabulary",
                Question.Skill.VOCABULARY,
                List.of(
                        new QuizQuestionSeed("「猫」の意味は？", "Mèo", "Chó", "Chim", "Cá", "A", "猫(ねこ) = con mèo."),
                        new QuizQuestionSeed("「水」の意味は？", "Lửa", "Nước", "Đất", "Gió", "B", "水(みず) = nước."),
                        new QuizQuestionSeed(
                                "「学校」の意味は？",
                                "Bệnh viện",
                                "Nhà hàng",
                                "Trường học",
                                "Công viên",
                                "C",
                                "学校(がっこう) = trường học."),
                        new QuizQuestionSeed("「食べる」の意味は？", "Uống", "Ăn", "Ngủ", "Chạy", "B", "食べる(たべる) = ăn."),
                        new QuizQuestionSeed("「大きい」の意味は？", "Nhỏ", "Đẹp", "Lớn", "Rẻ", "C", "大きい(おおきい) = lớn, to.")));

        seedQuizLevel(
                StudentUser.JlptLevel.N4,
                "N4 - Ngữ pháp cơ bản (Demo)",
                "grammar",
                Question.Skill.GRAMMAR,
                List.of(
                        new QuizQuestionSeed("明日、友達＿＿＿映画を見ます。", "に", "と", "の", "へ", "B", "～と = làm gì cùng với ai đó."),
                        new QuizQuestionSeed("この本は＿＿＿読みやすいです。", "とても", "あまり", "全然", "少しも", "A", "とても = rất."),
                        new QuizQuestionSeed(
                                "雨が降っている＿＿＿、出かけました。", "ので", "のに", "から", "まで", "B", "～のに = mặc dù... (nhưng vẫn)."),
                        new QuizQuestionSeed("田中さんは今、会議＿＿＿出席しています。", "を", "に", "が", "と", "B", "～に出席する = tham dự..."),
                        new QuizQuestionSeed("宿題をする＿＿＿に、テレビを見てしまった。", "前", "後", "間", "時", "A", "～前に = trước khi.")));

        seedQuizLevel(
                StudentUser.JlptLevel.N3,
                "N3 - Ngữ pháp trung cấp (Demo)",
                "grammar",
                Question.Skill.GRAMMAR,
                List.of(
                        new QuizQuestionSeed("会議の＿＿＿、電話が鳴った。", "最中に", "間", "時", "後で", "A", "～最中に = đang giữa lúc."),
                        new QuizQuestionSeed(
                                "これは学生＿＿＿難しい問題だ。", "にとって", "について", "に対して", "によって", "A", "～にとって = đối với."),
                        new QuizQuestionSeed("彼は怒っている＿＿＿、何も言わなかった。", "くせに", "わりに", "のに", "ものの", "C", "～のに = mặc dù."),
                        new QuizQuestionSeed(
                                "経済の発展＿＿＿、環境問題が深刻になった。",
                                "において",
                                "に伴って",
                                "に関して",
                                "をもとに",
                                "B",
                                "～に伴って = đi kèm với, theo sau."),
                        new QuizQuestionSeed("この映画は感動的だ＿＿＿、少し長すぎる。", "が", "ので", "から", "のに", "A", "～が = nhưng.")));

        seedQuizLevel(
                StudentUser.JlptLevel.N2,
                "N2 - Ngữ pháp nâng cao (Demo)",
                "grammar",
                Question.Skill.GRAMMAR,
                List.of(
                        new QuizQuestionSeed(
                                "何度も注意した＿＿＿、彼は同じ間違いを繰り返した。",
                                "にもかかわらず",
                                "に応じて",
                                "に沿って",
                                "に基づいて",
                                "A",
                                "～にもかかわらず = mặc dù."),
                        new QuizQuestionSeed(
                                "無理なダイエットは健康を損ない＿＿＿。",
                                "かねない",
                                "きれない",
                                "かまわない",
                                "でしかない",
                                "A",
                                "～かねない = có thể dẫn đến điều không tốt."),
                        new QuizQuestionSeed(
                                "彼は一日中悩んだ＿＿＿、結局何も決められなかった。",
                                "あげく",
                                "ところ",
                                "とたん",
                                "次第",
                                "A",
                                "～あげく = rốt cuộc (kết quả không tốt)."),
                        new QuizQuestionSeed(
                                "このレストランの料理は値段が高い＿＿＿、味はまあまあだ。",
                                "くせに",
                                "わりに",
                                "ところ",
                                "末に",
                                "B",
                                "～わりに = so với mức... thì."),
                        new QuizQuestionSeed("彼の言い分は信じ＿＿＿。", "がたい", "かねる", "あぐねる", "きれる", "A", "～がたい = khó mà...")));

        seedQuizLevel(
                StudentUser.JlptLevel.N1,
                "N1 - Ngữ pháp cao cấp (Demo)",
                "grammar",
                Question.Skill.GRAMMAR,
                List.of(
                        new QuizQuestionSeed(
                                "この問題を解決できるのは彼＿＿＿いない。",
                                "をおいて",
                                "にひいて",
                                "にとって",
                                "に応じて",
                                "A",
                                "～をおいて = không ai khác ngoài."),
                        new QuizQuestionSeed(
                                "完璧とは言え＿＿＿、かなり良い出来だ。",
                                "ないまでも",
                                "ないかぎり",
                                "ないことには",
                                "ないでもない",
                                "A",
                                "～ないまでも = tuy không... nhưng."),
                        new QuizQuestionSeed(
                                "社長＿＿＿なれば、責任は重くなる。",
                                "ともなれば",
                                "であれば",
                                "にしたら",
                                "としたら",
                                "A",
                                "～ともなれば = khi đã ở vị trí/mức độ đó."),
                        new QuizQuestionSeed(
                                "その光景は見る＿＿＿耐えない。", "に", "のに", "から", "まで", "A", "～に耐えない = không chịu được khi..."),
                        new QuizQuestionSeed(
                                "彼女は生まれ＿＿＿、芸術家の才能を持っていた。",
                                "ながらに",
                                "つつ",
                                "がてら",
                                "ばかりに",
                                "A",
                                "～ながらに = ngay từ khi sinh ra đã...")));
    }

    private void seedQuizLevel(
            StudentUser.JlptLevel level,
            String title,
            String topic,
            Question.Skill skill,
            List<QuizQuestionSeed> seeds) {
        if (assessmentRepository.existsByTitle(title)) {
            log.info("[DevDataSeeder] Quiz '{}' đã tồn tại — bỏ qua.", title);
            return;
        }

        Assessment assessment = assessmentRepository.save(Assessment.builder()
                .assessmentType(Assessment.AssessmentType.QUIZ)
                .title(title)
                .topic(topic)
                .jlptLevel(level)
                .durationMin(10)
                .passScore(6)
                .totalScore(seeds.size() * 2)
                .status(Kanji.ContentStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build());

        int displayOrder = 1;
        for (QuizQuestionSeed seed : seeds) {
            Question question = questionRepository.save(Question.builder()
                    .questionText(seed.text())
                    .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                    .skill(skill)
                    .jlptLevel(level)
                    .optionA(seed.a())
                    .optionB(seed.b())
                    .optionC(seed.c())
                    .optionD(seed.d())
                    .correctOption(seed.correct())
                    .explanation(seed.explanation())
                    .status(Question.ContentStatus.PUBLISHED)
                    .publishedAt(LocalDateTime.now())
                    .build());

            questionAssignmentRepository.save(QuestionAssignment.builder()
                    .parentType(QuestionAssignment.ParentType.ASSESSMENT)
                    .parentId(assessment.getId())
                    .question(question)
                    .sectionName("default")
                    .score(BigDecimal.valueOf(2))
                    .displayOrder(displayOrder++)
                    .build());
        }

        log.info("[DevDataSeeder] ✅ Quiz seed thành công: {} ({} câu)", title, seeds.size());
    }

    // ─── Mock Exam mẫu (N3, 3 section: language_knowledge / reading / listening) ──────────────

    private void seedMockExam() {
        String title = "N3 - Đề thi thử JLPT (Demo)";
        if (assessmentRepository.existsByTitle(title)) {
            log.info("[DevDataSeeder] Mock exam '{}' đã tồn tại — bỏ qua.", title);
            return;
        }

        Assessment exam = assessmentRepository.save(Assessment.builder()
                .assessmentType(Assessment.AssessmentType.EXAM)
                .title(title)
                .jlptLevel(StudentUser.JlptLevel.N3)
                .durationMin(90)
                .passScore(60)
                .totalScore(100)
                .status(Kanji.ContentStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build());

        seedExamSection(
                exam,
                "language_knowledge",
                Question.Skill.GRAMMAR,
                List.of(
                        new QuizQuestionSeed(
                                "彼は＿＿＿、会議に遅れた。", "にもかかわらず", "に応じて", "に沿って", "に基づいて", "A", "～にもかかわらず = mặc dù."),
                        new QuizQuestionSeed(
                                "この薬は健康を損ない＿＿＿。",
                                "かねない",
                                "きれない",
                                "かまわない",
                                "でしかない",
                                "A",
                                "～かねない = có thể dẫn đến điều không tốt.")));

        seedExamSection(
                exam,
                "vocabulary",
                Question.Skill.VOCABULARY,
                List.of(
                        new QuizQuestionSeed(
                                "「勉強」の読み方はどれか。",
                                "べんきょう",
                                "めんきょう",
                                "けんきょう",
                                "せんきょう",
                                "A",
                                "勉強 = べんきょう."),
                        new QuizQuestionSeed(
                                "「友達」の読み方はどれか。", "ともだち", "ゆうだち", "しんだち", "はんだち", "A", "友達 = ともだち.")));

        seedExamSection(
                exam,
                "listening",
                Question.Skill.LISTENING,
                List.of(new QuizQuestionSeed(
                        "会話を聞いて、正しい答えを選びなさい。",
                        "答え1",
                        "答え2",
                        "答え3",
                        "答え4",
                        "D",
                        "Nghe kỹ đoạn hội thoại trước khi chọn.")));

        log.info("[DevDataSeeder] ✅ Mock exam seed thành công: {}", title);
    }

    private void seedExamSection(
            Assessment exam, String sectionName, Question.Skill skill, List<QuizQuestionSeed> seeds) {
        int displayOrder = 1;
        for (QuizQuestionSeed seed : seeds) {
            Question question = questionRepository.save(Question.builder()
                    .questionText(seed.text())
                    .questionType(Question.QuestionType.MULTIPLE_CHOICE)
                    .skill(skill)
                    .jlptLevel(StudentUser.JlptLevel.N3)
                    .optionA(seed.a())
                    .optionB(seed.b())
                    .optionC(seed.c())
                    .optionD(seed.d())
                    .correctOption(seed.correct())
                    .explanation(seed.explanation())
                    .status(Question.ContentStatus.PUBLISHED)
                    .publishedAt(LocalDateTime.now())
                    .build());

            questionAssignmentRepository.save(QuestionAssignment.builder()
                    .parentType(QuestionAssignment.ParentType.ASSESSMENT)
                    .parentId(exam.getId())
                    .question(question)
                    .sectionName(sectionName)
                    .score(BigDecimal.valueOf(20))
                    .displayOrder(displayOrder++)
                    .build());
        }
    }
}
