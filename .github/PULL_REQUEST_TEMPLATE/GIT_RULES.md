# LUẬT SỬ DỤNG GIT CHO HỆ THỐNG (GIT RULES)

## Nguyên tắc cốt lõi
**CHỈ CẬP NHẬT VÀ CHIA SẺ CODE LOGIC - KHÔNG ĐƯỢC PHÉP ĐẨY/TẢI FILE MÔI TRƯỜNG HAY CẤU HÌNH CÁ NHÂN.**

Mỗi thành viên trong dự án đều có thiết lập môi trường phát triển (OS, IDE, đường dẫn thư mục) và thông tin bảo mật (Database credentials, JWT secret, API keys) riêng biệt. Việc đẩy các file này lên repository chung sẽ gây xung đột và làm hỏng môi trường của người khác, đồng thời tiềm ẩn rủi ro lộ lọt dữ liệu.

Do đó, toàn bộ thành viên **bắt buộc tuân thủ** các quy định dưới đây:

---

### 1. QUY ĐỊNH VỀ FILE MÔI TRƯỜNG (`.env`, `application.yml`)
- **TUYỆT ĐỐI KHÔNG** commit các file `.env`, `.env.local`, `.env.development`, `.env.production` lên Git.
- Đối với Backend (Spring Boot), nếu mỗi người dùng cấu hình DB riêng trong `application-dev.yml`, thư mục/file đó không được push lên (hoặc chỉ push template).
- **Cách xử lý đúng:** Nếu dự án cần thêm một biến môi trường mới (ví dụ: `JWT_SECRET`), bạn **chỉ được phép cập nhật biến đó vào file `.env.example`** (file mẫu, không chứa giá trị thật). Sau đó, thông báo cho team để mọi người tự cập nhật vào file `.env` cá nhân ở máy của họ.

---

### 2. QUY ĐỊNH VỀ FILE CẤU HÌNH MÁY / IDE
- **Không đẩy (push)** các thư mục/file do phần mềm IDE tự sinh ra như: `.vscode/`, `.idea/`, `*.iml`, `.eclipse/`.
- **Không đẩy** các file hệ thống do hệ điều hành tự tạo: `.DS_Store` (macOS), `Thumbs.db` (Windows).
- Nếu IDE của bạn tự động thay đổi formatting, thụt lề (indentation) trên toàn bộ file không liên quan đến task của bạn, hãy cẩn thận loại bỏ những file đó ra khỏi commit.

---

### 3. QUY TRÌNH COMMIT & PUSH (UP CODE)
- **Kiểm tra kỹ trước khi add:** LUÔN LUÔN chạy `git status` trước khi thực hiện `git add .`. Hãy chắc chắn rằng bạn chỉ đang thêm các file mã nguồn liên quan đến chức năng bạn đang làm.
- **Review Diff:** Dùng `git diff` để xem lại các dòng code vừa thay đổi. Nếu thấy dính cả phần sửa đổi cấu hình cá nhân, phải bỏ ra ngay.
- **Kiểm tra .gitignore:** Bất kỳ file rác, file build, file thư viện (node_modules, target, dist) hay cấu hình cá nhân nào xuất hiện, hãy cho chúng vào `.gitignore` trước khi commit.

---

### 4. QUY TRÌNH PULL (TẢI CODE)
- Khi thực hiện `git pull`, hệ thống chỉ được phép lấy mã nguồn về.
- Nếu Git báo lỗi conflict (xung đột) do file môi trường, điều đó có nghĩa là đã có người làm sai luật số 1. Ngay lập tức thông báo trên nhóm để người đó gỡ bỏ file môi trường khỏi repo.
- Không bao giờ dùng cờ ép buộc (force) ghi đè cấu hình cá nhân của bạn bằng cấu hình từ máy người khác.

---

### 5. XỬ LÝ SỰ CỐ KHI LỠ COMMIT NHẦM
Nếu bạn vô tình đẩy `.env` hoặc `.vscode/` lên remote repository:
1. **Không được** xóa file thủ công rồi commit, vì lịch sử Git vẫn giữ các thông tin cấu hình nhạy cảm.
2. Mở Terminal và sử dụng lệnh xóa khỏi bộ nhớ đệm (cache) của Git nhưng vẫn giữ trên máy:
   - Xóa file: `git rm --cached .env`
   - Xóa thư mục: `git rm -r --cached .vscode/`
3. Thêm thay đổi, commit và push lại: `git commit -m "chore: remove sensitive env files"`
4. Nếu file đó chứa mật khẩu quan trọng, hãy đổi mật khẩu/API key đó ngay lập tức!

---

### 6. QUY ĐỊNH ĐẶT TÊN BRANCH

**Cấu trúc:** `<type>/<ticket-id>-<mo-ta-ngan>`

| Type | Khi nào dùng | Ví dụ |
|------|--------------|-------|
| `feature/` | Tính năng mới | `feature/JLPT-42-quiz-submission` |
| `fix/` | Sửa lỗi thông thường | `fix/JLPT-55-jwt-token-expiry` |
| `hotfix/` | Sửa lỗi khẩn cấp trên production | `hotfix/JLPT-60-login-crash` |
| `chore/` | Config, build, deps không ảnh hưởng logic | `chore/update-spring-boot-3.4` |
| `refactor/` | Tái cấu trúc code (không thêm tính năng) | `refactor/quiz-service-cleanup` |
| `docs/` | Chỉ sửa tài liệu | `docs/update-api-readme` |

**Quy tắc:**
- Chỉ dùng chữ thường và dấu gạch ngang `-`
- Không dùng dấu cách, dấu gạch dưới `_`, hay ký tự đặc biệt
- Tối đa 50 ký tự sau phần `type/`
- Branch `main` là **protected** — không được push trực tiếp, chỉ merge qua Pull Request

---

### 7. QUY ƯỚC COMMIT MESSAGE (Conventional Commits)

**Cấu trúc bắt buộc:**
```
<type>(<scope>): <mô tả ngắn>

[body — tùy chọn, giải thích WHY nếu cần]

[footer — tùy chọn, ví dụ: Closes #42]
```

**Các `type` hợp lệ:**

| Type | Ý nghĩa |
|------|---------|
| `feat` | Thêm tính năng mới |
| `fix` | Sửa lỗi |
| `chore` | Build, config, deps (không ảnh hưởng code logic) |
| `refactor` | Tái cấu trúc (không thêm tính năng, không sửa lỗi) |
| `style` | Chỉ format, whitespace, dấu chấm phẩy (không đổi logic) |
| `test` | Thêm hoặc sửa test |
| `docs` | Chỉ sửa tài liệu |
| `perf` | Cải thiện hiệu năng |
| `ci` | Thay đổi CI/CD config |
| `revert` | Hoàn tác commit trước |

**Các `scope` phổ biến trong dự án:**
`auth`, `student`, `staff`, `admin`, `quiz`, `course`, `lesson`, `ocr`, `speech`, `flashcard`, `exam`, `user`, `subscription`, `ui`, `api`, `db`, `config`

**Ví dụ:**
```
feat(quiz): add server-side time validation on submission

fix(auth): handle expired refresh token returning 500 instead of 401

chore(deps): upgrade Spring Boot to 3.4.0

refactor(quiz-service): extract score calculation into separate method

test(auth): add integration test for JWT blacklist on logout

docs(api): document OCR async job polling endpoint
```

**Quy tắc:**
- Dòng subject tối đa **72 ký tự**
- Dùng **tiếng Anh**, thì hiện tại (imperative mood): "add", không phải "added" hay "adds"
- Không viết hoa chữ đầu sau dấu `:` (hoặc nhất quán toàn team)
- Không dấu chấm `.` ở cuối subject

---

### 8. QUY TRÌNH PULL REQUEST (PR) & CODE REVIEW

**Khi nào tạo PR:**
- Khi branch của bạn đã hoàn thành task và sẵn sàng merge vào `main`

**Quy trình:**
1. Sync code mới nhất trước khi tạo PR:
   ```bash
   git fetch origin
   git rebase origin/main
   ```
2. Tạo PR trên GitHub với:
   - **Title:** Theo format Conventional Commits (ví dụ: `feat(quiz): server-side score calculation`)
   - **Description:** Mô tả WHAT + WHY, link tới ticket nếu có
   - **Checklist:** Tự review diff của mình trước
3. **Yêu cầu review:** Ít nhất **1 thành viên khác** approve trước khi merge
4. **Merge strategy:** Dùng **Squash and Merge** để giữ lịch sử `main` sạch
5. Sau khi merge: **Xóa branch** đã merge

**Reviewer cần kiểm tra:**
- Không có file `.env`, `.vscode/`, `.idea/` bị commit nhầm
- Logic đúng, không có security vulnerability rõ ràng
- Không return Entity trực tiếp (phải qua DTO)
- Soft delete đúng cách (không `DELETE FROM`)

---

### 9. QUY TRÌNH HOTFIX (SỬA LỖI KHẨN CẤP)

Khi có lỗi nghiêm trọng cần fix ngay:
```bash
# 1. Tạo branch hotfix từ main
git checkout main
git pull origin main
git checkout -b hotfix/JLPT-XX-mo-ta-loi

# 2. Fix lỗi, commit
git commit -m "fix(scope): mô tả lỗi đã sửa"

# 3. Tạo PR vào main (vẫn cần ít nhất 1 review)
# 4. Merge và xóa branch
```
