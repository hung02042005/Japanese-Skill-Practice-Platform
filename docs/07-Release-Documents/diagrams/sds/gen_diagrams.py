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


def box_height(methods, w):
    total_lines = sum(_wrapped_lines(m, w) for m in methods) if methods else 1
    method_h = max(24, 18 * total_lines + 12)
    return 26 + method_h


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


def _side_points(sx, sy, sw, sh, tx, ty, tw, th, offset=0.0):
    """Pick exit/entry perimeter points so the edge leaves/enters at a clean
    side (bottom/top or left/right) instead of mxGraph's default floating
    connection, which tends to cut through a tall box's own text when the
    target sits almost directly below/beside it.
    `offset` (-0.3..0.3) nudges the point away from dead-center so several
    edges leaving the same box don't all stack on the same pixel."""
    scx, scy = sx + sw / 2, sy + sh / 2
    tcx, tcy = tx + tw / 2, ty + th / 2
    dx, dy = tcx - scx, tcy - scy
    half = 0.5 + offset
    half = min(max(half, 0.15), 0.85)
    if abs(dy) >= abs(dx):
        if dy >= 0:
            return (half, 1), (half, 0)  # exit bottom, enter top
        return (half, 0), (half, 1)  # exit top, enter bottom
    if dx >= 0:
        return (1, half), (0, half)  # exit right, enter left
    return (0, half), (1, half)  # exit left, enter right


def edge(eid, src, tgt, label="", dashed=False, arrow="open", exitp=None, entryp=None):
    dash = "dashed=1;" if dashed else ""
    conn = ""
    if exitp:
        conn += f"exitX={exitp[0]};exitY={exitp[1]};exitDx=0;exitDy=0;"
    if entryp:
        conn += f"entryX={entryp[0]};entryY={entryp[1]};entryDx=0;entryDy=0;"
    return f"""
        <mxCell id="{eid}" value="{esc(label)}" style="edgeStyle=orthogonalEdgeStyle;html=1;endArrow={arrow};{dash}fontSize=10;labelBackgroundColor=#ffffff;rounded=0;{conn}" edge="1" parent="1" source="{src}" target="{tgt}">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>"""


def build_class_diagram(path, boxes, edges, page_w=1400, page_h=900):
    """boxes: list of dict(id,name,methods,x,y,w?,interface?,header_color?)
    edges: list of dict(src,tgt,label?,dashed?,arrow?)"""
    cells = []
    geom = {}
    for b in boxes:
        w = b.get("w", 260)
        h = box_height(b["methods"], w)
        geom[b["id"]] = (b["x"], b["y"], w, h)
        c, _ = class_box(b["id"], b["name"], b["methods"], b["x"], b["y"],
                          w=w, interface=b.get("interface", False),
                          header_color=b.get("header_color", "#dae8fc"))
        cells.append(c)

    # count how many edges share the same source/target box, to fan out
    # their connection points instead of stacking them on one pixel
    from collections import defaultdict
    src_count = defaultdict(int)
    tgt_count = defaultdict(int)
    for e in edges:
        src_count[e["src"]] += 1
        tgt_count[e["tgt"]] += 1
    src_seen = defaultdict(int)
    tgt_seen = defaultdict(int)

    eid = 900
    for e in edges:
        sx, sy, sw, sh = geom[e["src"]]
        tx, ty, tw, th = geom[e["tgt"]]
        n_src = src_count[e["src"]]
        i_src = src_seen[e["src"]]
        src_seen[e["src"]] += 1
        n_tgt = tgt_count[e["tgt"]]
        i_tgt = tgt_seen[e["tgt"]]
        tgt_seen[e["tgt"]] += 1
        src_offset = ((i_src - (n_src - 1) / 2) * 0.12) if n_src > 1 else 0.0
        tgt_offset = ((i_tgt - (n_tgt - 1) / 2) * 0.12) if n_tgt > 1 else 0.0
        exitp, entryp = _side_points(sx, sy, sw, sh, tx, ty, tw, th, offset=src_offset)
        # recompute entry offset independently so multiple sources into one
        # target also fan out, using the target-side offset instead
        _, entryp2 = _side_points(sx, sy, sw, sh, tx, ty, tw, th, offset=tgt_offset)
        entryp = entryp2 if n_tgt > 1 else entryp
        # manual override for edges that would otherwise cut through a box
        # sitting geometrically between source and target (same column/row)
        if "exit" in e:
            exitp = e["exit"]
        if "entry" in e:
            entryp = e["entry"]
        cells.append(edge(f"e{eid}", e["src"], e["tgt"], e.get("label", ""),
                           e.get("dashed", False), e.get("arrow", "open"),
                           exitp=exitp, entryp=entryp))
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
        src_x = pos[m["src_id"]]
        tgt_x = pos[m["tgt_id"]]
        y = m["y"]
        if m["src_id"] == m["tgt_id"]:
            # Self-call: source==target gives a zero-width, invisible line by
            # default. Draw the standard UML self-message loop instead (out
            # to the right, down, back left into the same lifeline) so the
            # arrowhead is actually visible.
            loop_w, loop_h = 60, 26
            cells.append(f"""
        <mxCell id="msg_{i}" value="{esc(m['label'])}" style="html=1;endArrow={arrow};{dash}fontSize=11;rounded=0;" edge="1" parent="1">
          <mxGeometry relative="1" as="geometry">
            <mxPoint x="{src_x}" y="{y}" as="sourcePoint" />
            <mxPoint x="{src_x}" y="{y + loop_h}" as="targetPoint" />
            <Array as="points">
              <mxPoint x="{src_x + loop_w}" y="{y}" />
              <mxPoint x="{src_x + loop_w}" y="{y + loop_h}" />
            </Array>
          </mxGeometry>
        </mxCell>""")
        else:
            cells.append(f"""
        <mxCell id="msg_{i}" value="{esc(m['label'])}" style="html=1;endArrow={arrow};{dash}fontSize=11;" edge="1" parent="1">
          <mxGeometry relative="1" as="geometry">
            <mxPoint x="{src_x}" y="{y}" as="sourcePoint" />
            <mxPoint x="{tgt_x}" y="{y}" as="targetPoint" />
          </mxGeometry>
        </mxCell>""")

    xml = FRAME.format(page_w=page_w, page_h=page_h, cells="".join(cells))
    with open(path, "w", encoding="utf-8") as f:
        f.write(xml)
