# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r".skill/docx-report/scripts")
from docx_tools import *
from docx.oxml.ns import qn

doc = Document("docs/07-Release-Documents/RDS_Document.docx")

def del_range_before(doc, start_substr, end_substr):
    children = list(doc.element.body.iterchildren())
    i1 = find_paragraph_index(doc, start_substr)
    i2 = find_paragraph_index(doc, end_substr)
    for c in children[i1:i2]:
        c.getparent().remove(c)

# 1) Xoa noi dung vi du GAMS con lai trong Muc III: "1.2 System Access" -> het truoc "IV. Appendix"
del_range_before(doc, "1.2 System Access", "IV. Appendix")

# 2) Doi ten heading "1. <<Feature Name>>" (Muc III)
children = list(doc.element.body.iterchildren())
i_feat = find_paragraph_index(doc, "1. <<Feature Name>>")
set_paragraph_text(Paragraph(children[i_feat], doc), "1. Đặc Tả Chi Tiết Màn Hình (24/46 màn hình có tài liệu nguồn)")

# 3) Xoa toan bo doan huong dan dang "[...]" con sot (khong phai text that cua du an)
#    Chi xoa neu ca doan bat dau bang "[" va ket thuc bang "]" (tranh xoa nham text that co dung dau ngoac)
removed = 0
for p in list(doc.paragraphs):
    t = p.text.strip()
    if t.startswith("[") and t.endswith("]") and len(t) > 5:
        set_paragraph_text(p, "")
        removed += 1
print("Removed instructional bracket paragraphs:", removed)

doc.save("docs/07-Release-Documents/RDS_Document.docx")
print("Cleanup done.")
print(summarize(doc))
leftovers = find_leftover_markers(doc)
print("Remaining leftover markers:", len(leftovers))
with open("docs/07-Release-Documents/_leftovers.txt", "w", encoding="utf-8") as f:
    for l in leftovers:
        f.write(l[:200] + "\n---\n")
