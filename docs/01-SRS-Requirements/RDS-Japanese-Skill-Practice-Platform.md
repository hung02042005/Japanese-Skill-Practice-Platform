# Japanese Skill Practice Platform

## Requirement & Design Specification (RDS)

**Version: 1.0**

> Dựa trên `Temp_Document/Template2_RDS Document.docx` (FPT University template, mẫu GAMS), generate theo [`SPEC-rds-template-generation-guide.md`](SPEC-rds-template-generation-guide.md).
> **File này là bản "master/index"** theo đúng mục lục Template2 — mỗi mục chỉ tóm tắt ngắn + link sang nguồn thật (`use-cases/`, `constraints/`, `SDS-*.md`, `Overall-Functionalities.md`, `RDS-User-Login.md`), **không copy lại nội dung** để tránh 2 nguồn sự thật lệch nhau (xem `SPEC-rds-template-generation-guide.md § 4`).

---

## RECORD OF CHANGES

| Version | Date | A*/M,D | In charge | Change Description |
|---|---|---|---|---|
| 1.0 | 2026-07-22 | A | AI Agent (theo yêu cầu user) | Khởi tạo bản master RDS theo Template2, tổng hợp + link toàn bộ nội dung đã có (`use-cases/`, `constraints/`, `SDS-*.md`, `Overall-Functionalities.md`, `RDS-User-Login.md`) |

*A - Added, M - Modified, D - Deleted*

---

## Table of Contents

- [I. Overview](#i-overview)
  - [1. User Requirements](#1-user-requirements)
  - [2. Overall Functionalities](#2-overall-functionalities)
  - [3. System High Level Design](#3-system-high-level-design)
- [II. Requirement Specifications](#ii-requirement-specifications)
- [III. Design Specifications](#iii-design-specifications)
- [IV. Appendix](#iv-appendix)

---

## I. Overview

### 1. User Requirements

#### 1.1 Actors

4 nhóm tác nhân — **Student** (20 UC), **Staff** (12 UC), **StaffManager** (2 UC, thực chất là `Staff` với `staff_role = staff_manager`), **Admin** (6 UC). Chi tiết mô tả từng actor: [`use-cases/Bao_cao_dac_ta_Use_Case.md § "1.3. Phân loại tác nhân"`](use-cases/Bao_cao_dac_ta_Use_Case.md).

#### 1.2 Use Cases

**a. Diagram(s)** — sơ đồ actor-UC tổng quan theo 4 role: [`Overall-Functionalities.md § "I.1.2.a — Use Case Diagram"`](Overall-Functionalities.md#i12a--use-case-diagram).

**b. Descriptions** — 40 UC đầy đủ, chia 4 nhóm:

| Nhóm | Số UC | Mã UC | Chi tiết |
|---|---|---|---|
| Student (Học viên) | 20 | UC-01 → UC-20 | [`use-cases/... § "2.1. Nhóm Student"`](use-cases/Bao_cao_dac_ta_Use_Case.md) |
| Staff (Nhân viên) | 12 | UC-21 → UC-32 | [`use-cases/... § "2.2. Nhóm Staff"`](use-cases/Bao_cao_dac_ta_Use_Case.md) |
| StaffManager (Quản lý nội dung) | 2 | UC-33 → UC-34 | [`use-cases/... § "2.3. Nhóm StaffManager"`](use-cases/Bao_cao_dac_ta_Use_Case.md) |
| Admin (Quản trị viên) | 6 | UC-35 → UC-40 | [`use-cases/... § "2.4. Nhóm Admin"`](use-cases/Bao_cao_dac_ta_Use_Case.md) |

> ⚠️ Cả 40 UC đều có mô tả (actor/trigger/luồng cơ bản/luồng thay thế/hậu điều kiện), nhưng **chỉ UC-01 (User Login)** đã được nâng cấp lên đúng rigor "Functional Description Template" đầy đủ của Template2 (đánh số flow, Priority, Exception...) — xem [`RDS-User-Login.md`](RDS-User-Login.md). 39 UC còn lại vẫn ở bản rút gọn.

### 2. Overall Functionalities

| Mục Template2 | Nội dung | Chi tiết |
|---|---|---|
| 2.1 Screens Flow | Sơ đồ luồng màn hình theo route thật (`App.jsx`) | [`Overall-Functionalities.md § I.2.1`](Overall-Functionalities.md#i21--screens-flow) |
| 2.2 Screen Descriptions | 14 dòng: Feature / Screen (Route) / Description | [`Overall-Functionalities.md § I.2.2`](Overall-Functionalities.md#i22--screen-descriptions) |
| 2.3 Screen Authorization | Ma trận Role × Khu vực màn hình, đối chiếu route-guard Frontend lẫn `@PreAuthorize` Backend | [`Overall-Functionalities.md § I.2.3`](Overall-Functionalities.md#i23--screen-authorization) |
| 2.4 Non-UI Functions | 5 job/event thật (`SpeakingAsyncProcessor`, `AuthEventListener` x2, `NotificationDispatcher` x2) | [`Overall-Functionalities.md § I.2.4`](Overall-Functionalities.md#i24--non-ui-functions) |

### 3. System High Level Design

| Mục Template2 | Chi tiết |
|---|---|
| 3.1 Database Design | [`database-design/JLPT_database.md`](../02-SDD-Architecture/database-design/JLPT_database.md) — 23+ bảng, ERD, quan hệ |
| 3.2 Code Packages | [`SDS-Japanese-Skill-Practice-Platform.md § "I.1 Code Packages"`](../02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md) — 13 feature package backend |

---

## II. Requirement Specifications

### 1. Authentication

#### 1.1 UC-01_User Login (Đăng nhập)

Functional Description đầy đủ (Trigger/Precondition/Postcondition/Normal Flow "1.0"/Alternative Flow "1.1"/Exception "1.0.E1"–"E5"/Priority/Business Rules) — xem **[`RDS-User-Login.md § II`](RDS-User-Login.md)**.

### 2. Các Use Case còn lại (39 UC)

Chưa nâng cấp lên rigor Template2 đầy đủ — mô tả hiện có (Trigger gộp trong Mô tả, luồng không đánh số) tại [`use-cases/Bao_cao_dac_ta_Use_Case.md`](use-cases/Bao_cao_dac_ta_Use_Case.md), theo đúng 4 nhóm ở bảng Mục I.1.2.b. Khi cần nâng cấp 1 UC cụ thể lên đúng format Template2, làm theo `SPEC-rds-template-generation-guide.md § 5` (dùng UC-01 làm mẫu).

Business Rules áp dụng cho các UC: xem [`constraints/business.md`](constraints/business.md) (8 nhóm, ~30 rule có ID `BIZ-*`) và bảng ánh xạ Category tại [`Overall-Functionalities.md § IV.3`](Overall-Functionalities.md#iv3--business-rules-định-dạng-template2-id--category--rule-definition).

---

## III. Design Specifications

### 1. Authentication

#### 1.1 User Login Screen

UI Design (field-table) + Database Access (CRUD + SQL) đầy đủ theo đúng granularity Template2 (1 màn hình/file) — xem **[`RDS-User-Login.md § III`](RDS-User-Login.md)**.

### 2. Các màn hình khác (39 màn hình còn lại)

Chưa có bản Template2-style (UI field-table + SQL riêng từng màn hình). Thiết kế hiện có ở granularity **feature/luồng nghiệp vụ** (Class Diagram + Sequence Diagram + SQL gộp nhiều màn hình), tại [`SDS-Japanese-Skill-Practice-Platform.md`](../02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md):

| Feature (SDS) | Màn hình liên quan | UC liên quan |
|---|---|---|
| Authentication & Login | Login, Register, Forgot/Reset Password | UC-01 → UC-03 |
| Quiz Submission (Assessment) | Quiz, Mock Test (list/attempt/results) | UC-10, UC-11 |
| Flashcard SRS | Notebook, Flashcard Session | UC-12 |
| Kanji Writing Evaluation (OCR/DTW) | Kanji List, Kanji Practice | UC-07, UC-20 |
| Speaking Submission Grading | Speaking Page, Staff Grading | UC-13, UC-31 |
| Content Review | Manager Review Queue, Content Pipeline | UC-33, UC-34 |

> Khi cần đúng format Template2 (UI field-table + SQL riêng) cho 1 màn hình cụ thể trong danh sách trên, tạo file mới `docs/01-SRS-Requirements/RDS-<Screen-Name>.md` theo mẫu `RDS-User-Login.md` (`SPEC-rds-template-generation-guide.md § 5`, bước 3–4).

---

## IV. Appendix

### 1. Assumptions & Dependencies

Bản nháp đầu tiên (cần team xác nhận) — xem [`Overall-Functionalities.md § IV.1`](Overall-Functionalities.md#iv1--assumptions--dependencies).

### 2. Limitations & Exclusions

Xem [`Overall-Functionalities.md § IV.2`](Overall-Functionalities.md#iv2--limitations--exclusions). **Lưu ý quan trọng**: engine chấm điểm phát âm (UC-13/UC-31) hiện là `StubSpeechRecognitionEngine` — mô phỏng tất định, **chưa tích hợp ASR/AI thật**.

### 3. Business Rules

~30 rule, 8 nhóm, đầy đủ ID — xem [`constraints/business.md`](constraints/business.md), bảng ánh xạ `Category` theo format Template2 tại [`Overall-Functionalities.md § IV.3`](Overall-Functionalities.md#iv3--business-rules-định-dạng-template2-id--category--rule-definition).
