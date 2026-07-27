# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, r".skill/docx-report/scripts")
from docx_tools import *
from docx.oxml.ns import qn

doc = Document("docs/07-Release-Documents/RDS_Document.docx")

imgs = find_image_paragraphs(doc)
print("image paragraphs found:", len(imgs))
# Thu tu thuc te trong Muc I: [0]=logo bia (giu nguyen), [1]=UC diagram, [2]=Screens Flow, [3]=ER Schema, [4]=Package diagram
assert len(imgs) == 5, f"expected 5 (logo+4), got {len(imgs)}"

mapping = {
    1: "docs/07-Release-Documents/diagrams/rds/uc-diagram.png",
    2: "docs/07-Release-Documents/diagrams/rds/screens-flow.png",
    3: "docs/07-Release-Documents/diagrams/rds/er-diagram.png",
    4: "docs/07-Release-Documents/diagrams/rds/package-diagram.png",
}
for i, path in mapping.items():
    replace_image_in_paragraph(doc, imgs[i], path, width_in=6.5)

doc.save("docs/07-Release-Documents/RDS_Document.docx")
print("Images embedded.")
print(summarize(doc))
