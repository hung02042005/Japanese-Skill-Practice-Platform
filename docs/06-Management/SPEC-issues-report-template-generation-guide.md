# SPEC — Hướng dẫn Generate Issues Report (Template4) dạng Markdown

> **Nguồn**: `Temp_Document/Template4_Issues Report.pdf` (bản in của `Template4_Issues Report.xlsx` — FPT University template)
> **Xem cùng**: [`SPEC-sds-template-generation-guide.md`](../02-SDD-Architecture/SPEC-sds-template-generation-guide.md) (spec tương tự cho Template3/SDS)
> **Đối tượng dùng**: Dev hoặc AI Agent cần xuất báo cáo issue/tiến độ định kỳ (theo sprint/iteration) cho dự án này.

---

## 1. Mục đích

Template4 gốc là 1 bảng Excel export từ issue tracker (demo trong file dùng GitLab), dùng để báo cáo cho giảng viên/khách hàng: mỗi dòng là 1 issue (feature/task/defect), gắn với 1 màn hình/chức năng cụ thể. Spec này quy định cách generate ra `.md` **điền dữ liệu issue thật** của dự án (không phải bảng mẫu rỗng), vì:

- Dự án này host trên **GitHub** (`hung02042005/Japanese-Skill-Practice-Platform`), không phải GitLab như demo trong template — cột `URL` phải trỏ đúng GitHub.
- Tại thời điểm viết spec này, repo **chưa dùng GitHub Issues** (chỉ có Pull Request #2–#8, xem `git log --all --grep="#[0-9]"`) — spec phải nêu rõ cách xử lý khi chưa có Issues thật, không được bịa issue giả.
- Format đồng nhất, đặt đúng thư mục quản trị dự án `docs/06-Management/`.

---

## 2. Cấu trúc gốc của Template4 (.xlsx/.pdf)

File gốc là **1 bảng phẳng duy nhất**, PDF chỉ tách thành 3 khối cột do khổ giấy in ngang không đủ chỗ:

| Khối | Cột | Ý nghĩa gốc (theo demo GitLab) |
|---|---|---|
| 1 | `Title` | Tên ngắn gọn của issue |
| 1 | `Description` | Mô tả chi tiết issue (yêu cầu, phạm vi) |
| 1 | `Issue ID` | Số thứ tự issue trên tracker (vd `1`, `2`...) |
| 2 | `URL` | Link trực tiếp tới issue trên GitLab/GitHub |
| 2 | `State` | `Open` / `Closed` |
| 2 | `Assignee` | Người được giao (username) |
| 2 | `Created At` | Ngày tạo issue |
| 2 | `Due Date` | Hạn hoàn thành |
| 3 | `Milestone` | Sprint/iteration (vd `iter1`, `iter2`) |
| 3 | `Labels` | 2 nhãn: **loại** (`WP`/`Task`/`Defect`) + **trạng thái** (`1_To Do`/`2_Doing`/`3_Done`) |
| 3 | `Functions/Screens` | Màn hình/chức năng issue đó thuộc về (vd `User Login`, `Home Page`) |

Khi generate `.md`, gộp lại thành **1 bảng duy nhất theo đúng 11 cột trên** (không tách khối như bản PDF in).

---

## 3. Ánh xạ cột → nguồn dữ liệu thật trong repo

| Cột | Lấy dữ liệu thật ở đâu | Ghi chú |
|---|---|---|
| `Issue ID` | Số Pull Request GitHub (`#2`–`#8`...) nếu chưa dùng Issues, hoặc số GitHub Issue thật nếu team đã bật Issues | Repo hiện tại **không có GitHub Issues**, chỉ có PR — xem Mục 5 để biết cách xử lý |
| `Title` | Tiêu đề PR/Issue (`gh pr list --json number,title` hoặc `gh issue list --json number,title`) | Nếu lấy từ commit lẻ (không qua PR), dùng dòng đầu commit message |
| `Description` | Mô tả PR/Issue, hoặc phần thân commit message (sau dòng tiêu đề) | Không tự diễn giải thêm — copy nguyên văn, chỉ dịch nếu cần |
| `URL` | `https://github.com/hung02042005/Japanese-Skill-Practice-Platform/pull/<n>` hoặc `.../issues/<n>` | Không dùng link GitLab như bản demo gốc |
| `State` | `Open`/`Closed`/`Merged` — lấy từ `gh pr view <n> --json state` | GitHub PR có thêm state `Merged`, khác GitLab (chỉ `Open`/`Closed`) — giữ nguyên giá trị thật, không ép về 2 giá trị |
| `Assignee` | Tác giả commit/PR (`git log --format='%an'` hoặc `gh pr view <n> --json author`) | Đối chiếu với `Git user` thật của từng thành viên trong repo (xem `git shortlog -sn`) |
| `Created At` | Ngày tạo PR/Issue (`gh pr view <n> --json createdAt`) hoặc ngày commit đầu tiên của nhánh | Format `YYYY-MM-DD` |
| `Due Date` | Không có trường tương ứng trên GitHub mặc định — lấy từ Milestone `due_on` nếu có cấu hình, hoặc để trống và ghi chú `chưa đặt hạn` | Không tự bịa ngày |
| `Milestone` | GitHub Milestone thật (`gh api repos/.../milestones`) nếu đã tạo; nếu chưa có, dùng quy ước sprint nội bộ của nhóm (vd theo tuần: `sprint-2026-W29`) | Phải nêu rõ nguồn milestone là GitHub thật hay quy ước tự đặt |
| `Labels` | 2 phần: **loại** suy ra từ prefix branch/commit theo `GIT_RULES.md § 6` (`feature/`→`WP` hoặc `Task`, `fix/`/`hotfix/`→`Defect`, `chore/`/`refactor/`/`docs/`→`Task`); **trạng thái** suy ra từ PR state (`Merged`→`3_Done`, `Open` có review→`2_Doing`, `Open` chưa review→`1_To Do`) | Xem bảng quy tắc chi tiết ở Mục 4 |
| `Functions/Screens` | Tên package backend (`apps/backend/.../feature/<name>`) hoặc tên trang frontend (`apps/frontend/src/pages/<name>`) bị đổi trong PR đó — lấy từ `git show --stat <sha>` | Dùng đúng tên package/page thật, khớp cách đặt tên trong `SDS-Japanese-Skill-Practice-Platform.md` § Code Packages |

### Bảng quy tắc suy luận `Labels` từ branch/commit prefix

| Branch/commit prefix (`GIT_RULES.md`) | Label loại | Ví dụ |
|---|---|---|
| `feature/`, `feat(...)`/`feat:` | `WP` (nếu là cả 1 màn hình/luồng lớn) hoặc `Task` (nếu là 1 phần việc nhỏ trong luồng) | `feat(speaking): hoàn thiện luồng luyện nói...` → `WP` |
| `fix/`, `hotfix/`, `fix(...)`/`fix:` | `Defect` | `fix(smtp): allow non-email usernames...` → `Defect` |
| `chore/`, `refactor/`, `docs/`, `style:` | `Task` | `chore: remove empty ManagerVocabularyTopicController stub` → `Task` |

---

## 4. Quy ước đặt tên & vị trí file

- **Vị trí**: `docs/06-Management/Issues-Report-<phạm-vi>.md`
  - Báo cáo theo sprint/iteration: `Issues-Report-iter<N>.md` (vd `Issues-Report-iter2.md`)
  - Báo cáo theo mốc nộp bài (AI Usage Report đi kèm Project Tracking): `Issues-Report-<yyyy-mm-dd>.md`
- **Không tạo `.xlsx`/`.pdf`** trừ khi cần nộp bản chính thức — mặc định chỉ tạo `.md` (dễ diff/review); nếu cần `.xlsx` để nộp, convert từ `.md` ở bước cuối cùng, không soạn tay 2 bản song song.
- Đầu file luôn ghi rõ **khoảng thời gian** báo cáo (từ ngày → đến ngày) và **nguồn dữ liệu** (PR GitHub / GitHub Issues / quy ước nội bộ) để người đọc biết mức độ tin cậy của số liệu.

---

## 5. Quy trình từng bước để generate 1 Issues Report mới

1. **Xác định phạm vi thời gian** (1 sprint/iteration, hoặc từ lần report trước đến nay).
2. **Kiểm tra xem repo đã bật GitHub Issues chưa**:
   - Nếu đã có Issues thật → dùng `gh issue list --repo hung02042005/Japanese-Skill-Practice-Platform --state all --json number,title,body,url,state,assignees,createdAt,labels,milestone` làm nguồn chính, mapping 1-1 theo Mục 3.
   - Nếu **chưa có Issues** (tình trạng hiện tại) → dùng Pull Request đã merge làm surrogate: `git log --oneline --merges` hoặc `gh pr list --state all --json number,title,body,url,state,author,createdAt,mergedAt` trong khoảng thời gian đang report. Ghi rõ trong file là "surrogate từ PR, không phải GitHub Issue thật".
3. **Với mỗi PR/Issue lấy được**, tra thêm:
   - `Functions/Screens`: `git show --stat <merge-sha>` → lấy thư mục package/page bị đổi nhiều nhất.
   - `Labels` loại: áp bảng quy tắc ở Mục 3.
   - `Labels` trạng thái: PR merged → `3_Done`; PR còn mở → `2_Doing` hoặc `1_To Do` tùy đã có review chưa.
4. **Không tự bịa** `Assignee`, `Due Date`, `Milestone` nếu không tra được — để trống và ghi chú rõ trong phần đầu file, không điền giá trị đoán mò.
5. **Viết bảng** theo đúng 11 cột ở Mục 2, 1 dòng/issue, sắp theo `Created At` tăng dần (giống thứ tự trong file mẫu gốc).
6. **Review lại**: mỗi `URL` trong bảng phải mở được thật (đúng số PR/Issue tồn tại trên GitHub), mỗi `Functions/Screens` phải khớp tên package/page thật trong repo tại thời điểm đó.

---

## 6. Checklist trước khi coi là hoàn thành

- [ ] Đầu file nêu rõ khoảng thời gian report + nguồn dữ liệu (PR surrogate hay GitHub Issue thật).
- [ ] Đủ 11 cột đúng thứ tự: `Title, Description, Issue ID, URL, State, Assignee, Created At, Due Date, Milestone, Labels, Functions/Screens`.
- [ ] Mỗi `URL` là link GitHub thật (`github.com/hung02042005/Japanese-Skill-Practice-Platform/...`), không phải link GitLab demo còn sót từ template gốc.
- [ ] `State` dùng đúng giá trị GitHub thật (`Open`/`Closed`/`Merged`), không ép cứng về 2 giá trị của GitLab.
- [ ] Không có `Assignee`/`Due Date`/`Milestone` bị bịa — ô nào không tra được thì để trống + ghi chú.
- [ ] `Functions/Screens` khớp tên package backend hoặc page frontend thật tại thời điểm PR đó.
- [ ] `Labels` áp đúng bảng quy tắc suy luận ở Mục 3, không gán tùy tiện.
- [ ] File đặt đúng `docs/06-Management/`, đặt tên theo Mục 4.

---

## 7. Ghi chú tình trạng hiện tại của repo

Tại thời điểm viết spec này, repo **chưa bật GitHub Issues** — toàn bộ lịch sử công việc nằm ở Pull Request (`#2`–`#8` đã merge, xem `git log --all --grep="#[0-9]" --oneline`). Vì vậy **chưa có ví dụ Issues Report thật** để tham chiếu như file `SDS-Japanese-Skill-Practice-Platform.md` làm mẫu cho Template3. Khi cần bản báo cáo đầu tiên, dùng nhánh "surrogate từ PR" ở Mục 5 bước 2, và cân nhắc đề xuất team bật GitHub Issues cho các sprint tiếp theo để dữ liệu chuẩn hơn.
