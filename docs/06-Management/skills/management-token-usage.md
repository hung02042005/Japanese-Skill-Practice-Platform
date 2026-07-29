# ManagementToken — Theo dõi hoạt động CodeGraph/Graphify của team

> **Mục đích tài liệu**: Giải thích hệ thống theo dõi tự động "ai dùng CodeGraph/Graphify, khi nào, dùng bao lâu, để làm gì, thay đổi gì, tốn bao nhiêu token" cho cả team, cập nhật hằng ngày.
> **Tài liệu liên quan**: [`codegraph-graphify-loi-ich.md`](./codegraph-graphify-loi-ich.md) · [`codegraph-token-savings-report.md`](./codegraph-token-savings-report.md) · [`management-token-report.md`](./management-token-report.md) (báo cáo tự sinh, không sửa tay)

---

## 1. Kiến trúc tổng quan

```
Máy mỗi thành viên                          Repo (git)                    Cloud routine (hằng ngày)
┌─────────────────────┐                    ┌──────────────────┐         ┌──────────────────────────┐
│ Claude Code chạy     │  PostToolUse hook  │ usage-logs/       │  push   │ Clone repo (bản mới nhất) │
│ Bash "graphify ..."  │ ─────────────────► │  <ban>-<thang>.csv│ ──────► │ chạy team-usage-report.ps1│
│ hoặc MCP codegraph   │  ghi 1 dòng CSV    │  (1 file/ng/thg)  │         │ mở PR cập nhật report     │
└─────────────────────┘                    └──────────────────┘         └──────────────────────────┘
```

Ba mảnh ghép:
1. **Hook cục bộ** (`tools/hooks/record-codegraph-usage.ps1`) — tự động ghi lại *ai/khi nào/gọi gì* mỗi khi ai đó dùng CodeGraph hoặc Graphify trong Claude Code.
2. **Bộ tổng hợp** (`tools/team-usage-report.ps1`) — gộp tất cả file CSV thành 1 báo cáo Markdown, chạy bởi routine hằng ngày.
3. **Bộ nạp token thật** (`tools/import-console-usage.ps1`) — chạy thủ công khi có CSV thật từ Org Analytics Dashboard (Team/Enterprise).

## 2. Giới hạn quan trọng cần biết

- **Số token trong log KHÔNG phải số thật** trừ khi đã chạy bước 3 ở trên. Docs chính thức của Claude Code nói rõ hook không truy cập được số token thật (transcript nội bộ, có thể vỡ giữa các bản) — nên hook chỉ ghi **ước lượng** `est_tokens = (input_chars + result_chars) / 4`, đánh dấu `token_source = estimate`.
- **"Mục đích" và "thay đổi gì" không tự động được** — hook để trống 2 cột này, mỗi người có thể tự mở file CSV của mình trong `usage-logs/` để điền tay nếu muốn (không bắt buộc).
- **Routine hằng ngày chỉ thấy dữ liệu đã được `git push`** — nó chạy trên bản clone mới từ remote, không thấy file cục bộ trên máy ai đó chưa push. Xem mục 4.
- **Matcher MCP `mcp__codegraph.*` trong `.claude/settings.json` là placeholder** — repo hiện chưa đăng ký MCP server `codegraph` nào (`docs/06-Management/skills/.codegraph/` rỗng), nên tên tool thật chưa được xác nhận. Khi MCP server được cấu hình, cần kiểm tra lại và sửa matcher cho khớp tên tool thật.

## 3. Schema file CSV (`usage-logs/<safe-username>-<yyyy-MM>.csv`)

| Cột | Ý nghĩa | Ai điền |
|---|---|---|
| `timestamp_utc` | Thời điểm gọi tool (UTC, ISO 8601) | hook |
| `user_email` | Danh tính (git email/name/username, fallback theo thứ tự đó) | hook |
| `session_id` | ID phiên Claude Code — dùng để gom nhóm tính thời lượng | hook |
| `tool_name`, `matched_pattern` | Tool nào được gọi, khớp rule nào | hook |
| `target_or_query` | Lệnh/câu hỏi thật (cắt 300 ký tự) | hook |
| `purpose_note` | Mục đích sử dụng | **điền tay (tùy chọn)** |
| `changed_files_note` | Kết quả/thay đổi gì | **điền tay (tùy chọn)** |
| `input_chars`, `result_chars`, `est_tokens`, `token_source` | Số liệu ước lượng | hook |
| `real_tokens` | Số token thật | **`import-console-usage.ps1`** |
| `tool_use_id` | ID gọi tool (dedup) | hook |

## 4. Quy trình bắt buộc: push log của bạn

Vì routine hằng ngày chạy trên bản clone mới, **báo cáo tổng hợp sẽ không thấy hoạt động của bạn cho tới khi bạn push** file `usage-logs/<ban>-<thang>.csv` lên remote. Khuyến nghị: push định kỳ (cuối ngày làm việc, hoặc gộp cùng lần push code bình thường):

```powershell
git add docs/06-Management/skills/usage-logs/
git commit -m "chore: cap nhat usage log ManagementToken"
git push
```

## 5. Cách xem báo cáo tổng hợp

Chạy thủ công bất cứ lúc nào (không cần đợi routine hằng ngày):

```powershell
pwsh ./tools/team-usage-report.ps1
```

Kết quả ghi vào [`management-token-report.md`](./management-token-report.md) — bảng theo `user/ngày`: số phiên, tổng thời lượng (suy ra từ khoảng cách timestamp trong cùng session, không phải đo trực tiếp), mục đích/thay đổi gì (nếu đã điền tay), token ước lượng, và token thật (nếu đã nạp).

## 6. Cách nạp số token thật (khi có CSV từ Org Admin)

1. Một Org Admin đăng nhập Console/claude.ai, xuất CSV usage theo ngày/thành viên từ Org Analytics Dashboard (Team/Enterprise).
2. Chạy:
   ```powershell
   pwsh ./tools/import-console-usage.ps1 -ConsoleCsvPath "C:\đường\dẫn\file-export.csv"
   ```
3. Script **tự dò tên cột** theo từ khóa (ngày/user/token) — nếu không chắc chắn khớp đúng cột, nó **dừng lại và in ra toàn bộ header thật** thay vì đoán bừa. Đọc cảnh báo, đổi tên cột nếu cần, chạy lại.
4. Sau khi chạy xong, chạy lại `tools/team-usage-report.ps1` để báo cáo hiển thị số token thật.

## 7. Vì sao `.claude/settings.json` được commit lên git (ngoại lệ)

`GIT_RULES.md` quy định không commit cấu hình IDE/tool cá nhân (như `.vscode/`, `.idea/`). `.claude/settings.json` là **ngoại lệ có chủ đích**: đây là cấu hình hook dùng chung cho cả team (không phải cấu hình cá nhân), nên cần được chia sẻ qua git để mọi người `git pull` về là có sẵn. `.gitignore` đã được sửa thành `.claude/*` + `!.claude/settings.json` — các file khác trong `.claude/` (vd. `settings.local.json`, cache) vẫn bị ignore như cũ.
