# Thong ke thuc te: Graphify tiet kiem token bao nhieu so voi cach cu

> Sinh tu dong boi tools/codegraph-token-savings.ps1 luc 2026-07-29 09:40.
> Chay lai script bat cu luc nao sau khi "graphify update ." de co so lieu moi nhat - KHONG sua tay file nay.
> Tai lieu dinh tinh lien quan: [codegraph-graphify-loi-ich.md](./codegraph-graphify-loi-ich.md)

## Phuong phap do

- **OLD (cach cu)**: doc toan bo noi dung that cua file muc tieu + cac file hang xom
  N-hop trong do thi phu thuoc (import/su dung), lay truc tiep tu dia - mo phong dung
  viec mot AI agent/dev phai grep roi mo tung file de tu suy ra quan he.
- **NEW (Graphify)**: kich thuoc subgraph JSON toi gian (chi id/label/source_file +
  danh sach edge source/target/relation) cho dung tap node/edge do - day la dang
  du lieu ma "graphify query" / "graphify path" thuc su tra ve thay vi bat AI doc lai source.
- **Token uoc luong** = so ky tu / 4 (heuristic pho bien cho van ban Anh/code). Day la
  UOC LUONG vi moi truong chay script khong co tokenizer that (khong co Node/Python kha dung
  de goi tiktoken) - luon doi chieu voi cot so ky tu that (OldChars/NewChars) o tren.
- Du lieu do thi: apps/graphify-out/graph.json - hien chi phu module contentreview
  (297 node do thi / 832 edge, anh xa toi 31 file .java that tren dia), khong phai toan repo.
  Muon co so lieu cho module khac, chay "graphify update ." tren toan repo roi chay lai script nay.

## Ket qua

| Kich ban | So file phai doc (cach cu) | OLD - ky tu that | OLD - token uoc luong | NEW - ky tu that | NEW - token uoc luong | Giam |
|---|---|---|---|---|---|---|
| Impact analysis: ContentReviewService (1-hop) | 8 | 18,384 | 4,596 | 8,989 | 2,248 | **51.1%** |
| Trace Controller->Service: ManagerReviewController (1-hop) | 6 | 7,930 | 1,983 | 6,923 | 1,731 | **12.7%** |
| Hieu toan bo tinh nang (end-to-end): ContentReviewService (2-hop) | 31 | 75,431 | 18,858 | 72,583 | 18,146 | **3.8%** |
| God Nodes / cau truc toan module contentreview (top 10, do da tinh san) | 31 | 75,431 | 18,858 | 445 | 112 | **99.4%** |

## Phat hien phu (tu chinh du lieu that)

- **0** node trong graph.json khong tim thay file tren dia theo ten (co the da bi xoa/doi ten sau khi graph duoc build).
- **44** node co source_file ghi sai thu muc con so voi vi tri that hien tai tren dia
  (vd. file da duoc di chuyen vao service/, controller/... sau khi graph duoc build lan dau).
  -> Xac nhan dung diem "Gioi han can biet" trong codegraph-graphify-loi-ich.md: do thi la anh chup,
  can chay "graphify update ." thuong xuyen de khong bi lech thuc te.

## Ve CodeGraph (chua co so lieu that trong moi truong nay)

Script nay KHONG bao gom so lieu do thuc cho CodeGraph vi trong moi truong chay script:
- thu muc index docs/06-Management/skills/.codegraph/ dang RONG (chua chay "codegraph init"), va
- khong co MCP tool codegraph_explore song de goi truy van that.

Phuong phap do o tren (OLD = doc full source hang xom N-hop, NEW = kich thuoc payload tra ve) ap dung
duoc y het cho CodeGraph - chi can thay NEW bang do dai response that cua codegraph_explore cho cung
mot tap file. Khi index da duoc xay (codegraph init roi explore vai truy van trong codegraph_prompts.md),
co the bo sung cot CodeGraph vao bang tren bang so do thuc - khong nen dien so uoc doan vao day.


