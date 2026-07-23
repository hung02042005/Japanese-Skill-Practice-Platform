"""Small generator for the 6 SDS class diagrams + 6 sequence diagrams (drawio XML).

Not a general-purpose library — just enough to avoid hand-writing 12 near-identical
mxGraph XML files by hand. Run once per diagram set, then export with draw.io CLI.
"""
import xml.sax.saxutils as sx

FRAME = """<mxfile host="Electron" version="24.0.0">
  <diagram name="Page-1" id="page1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1"
        connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{page_w}"
        pageHeight="{page_h}" math="0" shadow="0">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
{cells}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
"""


def esc(s):
    return sx.escape(str(s), {'"': "&quot;"})


def _wrapped_lines(text, w):
    """Rough estimate of how many visual lines `text` wraps to inside a box of
    width `w` at fontSize=11 (~6.2px/char average, minus padding)."""
    chars_per_line = max(10, int((w - 12) / 6.2))
    n = max(1, -(-len(text) // chars_per_line))  # ceil
    return n


def class_box(cid, name, methods, x, y, w=260, interface=False, header_color="#dae8fc"):
    total_lines = sum(_wrapped_lines(m, w) for m in methods) if methods else 1
    method_h = max(24, 18 * total_lines + 12)
    total_h = 26 + method_h
    label = ("&lt;&lt;interface&gt;&gt;\n" + name) if interface else name
    methods_text = "&#10;".join(esc(m) for m in methods)
    cells = f"""
        <mxCell id="{cid}" value="{esc(label)}" style="swimlane;fontStyle=1;childLayout=stackLayout;horizontal=1;startSize=26;horizontalStack=0;resizeParent=0;resizeParentMax=0;collapsible=0;marginBottom=0;html=1;fillColor={header_color};" vertex="1" parent="1">
          <mxGeometry x="{x}" y="{y}" width="{w}" height="{total_h}" as="geometry" />
        </mxCell>
        <mxCell id="{cid}_m" value="{methods_text}" style="text;html=1;align=left;verticalAlign=top;spacingLeft=6;spacingRight=4;spacingTop=4;overflow=hidden;whiteSpace=wrap;fontSize=11;" vertex="1" parent="{cid}">
          <mxGeometry y="26" width="{w}" height="{method_h}" as="geometry" />
        </mxCell>"""
    return cells, total_h


def edge(eid, src, tgt, label="", dashed=False, arrow="open", enum_edge=False):
    dash = "dashed=1;" if dashed else ""
    return f"""
        <mxCell id="{eid}" value="{esc(label)}" style="edgeStyle=orthogonalEdgeStyle;html=1;endArrow={arrow};{dash}fontSize=10;" edge="1" parent="1" source="{src}" target="{tgt}">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>"""


def build_class_diagram(path, boxes, edges, page_w=1400, page_h=900):
    """boxes: list of dict(id,name,methods,x,y,w?,interface?,header_color?)
    edges: list of dict(src,tgt,label?,dashed?,arrow?)"""
    cells = []
    for b in boxes:
        c, _ = class_box(b["id"], b["name"], b["methods"], b["x"], b["y"],
                          w=b.get("w", 260), interface=b.get("interface", False),
                          header_color=b.get("header_color", "#dae8fc"))
        cells.append(c)
    eid = 900
    for e in edges:
        cells.append(edge(f"e{eid}", e["src"], e["tgt"], e.get("label", ""),
                           e.get("dashed", False), e.get("arrow", "open")))
        eid += 1
    xml = FRAME.format(page_w=page_w, page_h=page_h, cells="".join(cells))
    with open(path, "w", encoding="utf-8") as f:
        f.write(xml)


# --------------------------------------------------------------------------- #
# Sequence diagram
# --------------------------------------------------------------------------- #

def build_sequence_diagram(path, participants, messages, frames=None, page_w=1400, page_h=900):
    """participants: list of (id, label, x)
    messages: list of dict(src_id, tgt_id, y, label, dashed=False, arrow='block')
    frames: list of dict(label, x1, x2, y1, y2) -> alt/opt/loop dashed rectangle
    """
    cells = []
    lifeline_bottom = max(m["y"] for m in messages) + 60 if messages else 300
    for pid, label, x in participants:
        cells.append(f"""
        <mxCell id="p_{pid}" value="{esc(label)}" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#f5f5f5;fontStyle=1;" vertex="1" parent="1">
          <mxGeometry x="{x}" y="40" width="150" height="30" as="geometry" />
        </mxCell>""")
        cells.append(f"""
        <mxCell id="ll_{pid}" style="html=1;endArrow=none;dashed=1;" edge="1" parent="1">
          <mxGeometry relative="1" as="geometry">
            <mxPoint x="{x + 75}" y="70" as="sourcePoint" />
            <mxPoint x="{x + 75}" y="{lifeline_bottom}" as="targetPoint" />
          </mxGeometry>
        </mxCell>""")

    pos = {pid: x + 75 for pid, _, x in participants}

    if frames:
        for i, fr in enumerate(frames):
            cells.append(f"""
        <mxCell id="frame_{i}" value="" style="rounded=0;whiteSpace=wrap;html=1;dashed=1;fillColor=none;verticalAlign=top;" vertex="1" parent="1">
          <mxGeometry x="{fr['x1']}" y="{fr['y1']}" width="{fr['x2'] - fr['x1']}" height="{fr['y2'] - fr['y1']}" as="geometry" />
        </mxCell>""")
            cells.append(f"""
        <mxCell id="frame_lbl_{i}" value="{esc(fr['label'])}" style="text;html=1;align=left;verticalAlign=top;fontStyle=2;fontSize=10;fontColor=#B85450;" vertex="1" parent="1">
          <mxGeometry x="{fr['x1'] + 4}" y="{fr['y1'] - 18}" width="{fr['x2'] - fr['x1'] - 8}" height="16" as="geometry" />
        </mxCell>""")

    for i, m in enumerate(messages):
        dash = "dashed=1;" if m.get("dashed") else ""
        arrow = m.get("arrow", "block")
        cells.append(f"""
        <mxCell id="msg_{i}" value="{esc(m['label'])}" style="html=1;endArrow={arrow};{dash}fontSize=11;" edge="1" parent="1">
          <mxGeometry relative="1" as="geometry">
            <mxPoint x="{pos[m['src_id']]}" y="{m['y']}" as="sourcePoint" />
            <mxPoint x="{pos[m['tgt_id']]}" y="{m['y']}" as="targetPoint" />
          </mxGeometry>
        </mxCell>""")

    xml = FRAME.format(page_w=page_w, page_h=page_h, cells="".join(cells))
    with open(path, "w", encoding="utf-8") as f:
        f.write(xml)
