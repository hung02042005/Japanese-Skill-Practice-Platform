"""PNG rasterizer for the class/sequence diagram data used by build_all.py.

No draw.io CLI is available in this environment, so instead of exporting the
generated .drawio XML to PNG via drawio-desktop, this module renders the same
``boxes``/``edges`` (class diagram) and ``participants``/``messages`` (sequence
diagram) data structures directly to PNG with Pillow. Visual language mirrors
gen_diagrams.py: colored header per box, white body with method/attribute
list, arrows for associations; lifelines + labeled arrows for sequences.

Usage mirrors gen_diagrams.build_class_diagram / build_sequence_diagram:
    render_class_diagram_png(path, boxes=[...], edges=[...], page_w=.., page_h=..)
    render_sequence_diagram_png(path, participants=[...], messages=[...], frames=[...])
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

FONT_DIR = Path("C:/Windows/Fonts")
SCALE = 1.6  # drawio point -> PNG pixel

_font_cache: dict[tuple[str, int], ImageFont.FreeTypeFont] = {}


def _font(name: str, size: int) -> ImageFont.FreeTypeFont:
    key = (name, size)
    if key not in _font_cache:
        _font_cache[key] = ImageFont.truetype(str(FONT_DIR / name), size)
    return _font_cache[key]


def _f_regular(size=13):
    return _font("arial.ttf", size)


def _f_bold(size=14):
    return _font("arialbd.ttf", size)


def _f_mono(size=12):
    return _font("consola.ttf", size)


def _text_w(draw: ImageDraw.ImageDraw, text: str, font) -> float:
    return draw.textlength(text, font=font)


def _wrap(draw, text, font, max_w):
    words = text.split(" ")
    lines, cur = [], ""
    for w in words:
        cand = (cur + " " + w).strip()
        if _text_w(draw, cand, font) <= max_w or not cur:
            cur = cand
        else:
            lines.append(cur)
            cur = w
    if cur:
        lines.append(cur)
    return lines


# --------------------------------------------------------------------------- #
# Class diagram
# --------------------------------------------------------------------------- #

_HEADER_H = 34
_ROW_H = 22
_PAD = 10


def _wrap_mono(text, max_chars):
    """Word-boundary wrap; only hard-breaks a single word if it alone exceeds max_chars."""
    if not text:
        return [""]
    words = text.split(" ")
    lines, cur = [], ""
    for w in words:
        cand = (cur + " " + w).strip()
        if len(cand) <= max_chars or not cur:
            if len(w) > max_chars and not cur:
                # single overlong word/token: hard-split it
                for i in range(0, len(w), max_chars):
                    lines.append(w[i : i + max_chars])
                cur = ""
                continue
            cur = cand
        else:
            lines.append(cur)
            cur = w
    if cur:
        lines.append(cur)
    return lines or [""]


def _method_lines(methods, w):
    max_chars = max(1, int((w - 2 * _PAD) / 7.2))
    out = []
    for m in methods:
        out.extend(_wrap_mono(m, max_chars))
    return out


def _box_height(methods, w):
    lines = len(_method_lines(methods, w))
    return _HEADER_H + max(lines, 0) * _ROW_H + _PAD * 1.5


def render_class_diagram_png(path, boxes, edges, page_w=1400, page_h=900, scale=SCALE):
    W, H = int(page_w * scale), int(page_h * scale)
    img = Image.new("RGB", (W, H), "white")
    draw = ImageDraw.Draw(img)

    geo = {}
    for b in boxes:
        w = b.get("w", 260)
        h = _box_height(b.get("methods", []), w)
        geo[b["id"]] = (b["x"], b["y"], w, h)

    # edges first (so boxes sit on top)
    for e in edges:
        sx, sy, sw, sh = geo[e["src"]]
        tx, ty, tw, th = geo[e["tgt"]]
        scx, scy = sx + sw / 2, sy + sh / 2
        tcx, tcy = tx + tw / 2, ty + th / 2
        # connect at nearest box edge midpoints (simple 4-side routing)
        p1 = _edge_point(sx, sy, sw, sh, tcx, tcy)
        p2 = _edge_point(tx, ty, tw, th, scx, scy)
        p1 = (p1[0] * scale, p1[1] * scale)
        p2 = (p2[0] * scale, p2[1] * scale)
        color = "#333333"
        draw.line([p1, p2], fill=color, width=2)
        _arrowhead(draw, p1, p2, color, block=(e.get("arrow") == "block"))
        label = e.get("label")
        if label:
            mx, my = (p1[0] + p2[0]) / 2, (p1[1] + p2[1]) / 2
            draw.text((mx + 4, my - 14), label, font=_f_regular(11), fill="#555555")

    for b in boxes:
        x, y, w, h = geo[b["id"]]
        x, y, w, h = x * scale, y * scale, w * scale, h * scale
        header_h = _HEADER_H * scale
        header_color = b.get("header_color", "#dae8fc")
        draw.rectangle([x, y, x + w, y + h], outline="#333333", width=2, fill="white")
        draw.rectangle([x, y, x + w, y + header_h], outline="#333333", width=2, fill=header_color)
        title = b["name"] + (" «interface»" if b.get("interface") else "")
        tf = _f_bold(15)
        tw = _text_w(draw, title, tf)
        draw.text((x + (w - tw) / 2, y + header_h / 2 - 9), title, font=tf, fill="#1a1a1a")

        yy = y + header_h + _PAD * scale / 2
        mf = _f_mono(12)
        for chunk in _method_lines(b.get("methods", []), w / scale):
            draw.text((x + _PAD * scale / 2, yy), chunk, font=mf, fill="#222222")
            yy += _ROW_H * scale

    img.save(path)


def _edge_point(bx, by, bw, bh, toward_x, toward_y):
    """Return the point on the box border closest to (toward_x, toward_y), 4-side clamp."""
    cx, cy = bx + bw / 2, by + bh / 2
    dx, dy = toward_x - cx, toward_y - cy
    if dx == 0 and dy == 0:
        return (cx, cy)
    scale_x = (bw / 2) / abs(dx) if dx != 0 else float("inf")
    scale_y = (bh / 2) / abs(dy) if dy != 0 else float("inf")
    s = min(scale_x, scale_y)
    return (cx + dx * s, cy + dy * s)


def _arrowhead(draw, p1, p2, color, block=False, size=9):
    import math

    ang = math.atan2(p2[1] - p1[1], p2[0] - p1[0])
    x, y = p2
    a1 = ang + math.radians(150)
    a2 = ang - math.radians(150)
    pts = [
        (x, y),
        (x + size * math.cos(a1), y + size * math.sin(a1)),
        (x + size * math.cos(a2), y + size * math.sin(a2)),
    ]
    if block:
        draw.polygon(pts, outline=color, fill="white", width=2)
    else:
        draw.line([pts[0], pts[1]], fill=color, width=2)
        draw.line([pts[0], pts[2]], fill=color, width=2)


# --------------------------------------------------------------------------- #
# Sequence diagram
# --------------------------------------------------------------------------- #


def render_sequence_diagram_png(path, participants, messages, frames=None, page_w=1400, page_h=600, scale=SCALE):
    W, H = int(page_w * scale), int(page_h * scale)
    img = Image.new("RGB", (W, H), "white")
    draw = ImageDraw.Draw(img)

    head_y = 30 * scale
    head_h = 44 * scale
    life_bottom = (page_h - 20) * scale

    xpos = {}
    for pid, label, x in participants:
        xpos[pid] = x

    # frames (draw first, behind lifelines/messages)
    for fr in frames or []:
        x1, x2, y1, y2 = fr["x1"] * scale, fr["x2"] * scale, fr["y1"] * scale, fr["y2"] * scale
        draw.rectangle([x1, y1, x2, y2], outline="#b85450", width=2)
        draw.text((x1 + 6, y1 + 4), fr.get("label", ""), font=_f_regular(11), fill="#b85450")

    # lifelines + headers
    box_w = 170 * scale
    for pid, label, x in participants:
        bx = x * scale
        draw.rectangle([bx, head_y, bx + box_w, head_y + head_h], outline="#333333", width=2, fill="#dae8fc")
        lines = _wrap(draw, label, _f_bold(13), box_w - 10)
        ty = head_y + head_h / 2 - (len(lines) * 15) / 2
        for ln in lines:
            tw = _text_w(draw, ln, _f_bold(13))
            draw.text((bx + (box_w - tw) / 2, ty), ln, font=_f_bold(13), fill="#1a1a1a")
            ty += 15
        cx = bx + box_w / 2
        draw.line([(cx, head_y + head_h), (cx, life_bottom)], fill="#999999", width=2)
        xpos[pid] = cx

    # messages
    for m in messages:
        y = m["y"] * scale
        x1, x2 = xpos[m["src_id"]], xpos[m["tgt_id"]]
        dashed = m.get("dashed", False)
        color = "#333333"
        if dashed:
            _dashed_line(draw, (x1, y), (x2, y), color)
        else:
            draw.line([(x1, y), (x2, y)], fill=color, width=2)
        arrow_dir = 1 if x2 >= x1 else -1
        _h_arrowhead(draw, (x2, y), arrow_dir, color)
        label = m.get("label", "")
        lf = _f_regular(11)
        lw = _text_w(draw, label, lf)
        mid = (x1 + x2) / 2
        draw.text((mid - lw / 2, y - 16), label, font=lf, fill="#222222")

    img.save(path)


def _dashed_line(draw, p1, p2, color, dash=6, gap=4, width=2):
    import math

    x1, y1 = p1
    x2, y2 = p2
    dist = math.hypot(x2 - x1, y2 - y1)
    if dist == 0:
        return
    ux, uy = (x2 - x1) / dist, (y2 - y1) / dist
    n = int(dist // (dash + gap)) + 1
    for i in range(n):
        s = i * (dash + gap)
        e = min(s + dash, dist)
        draw.line([(x1 + ux * s, y1 + uy * s), (x1 + ux * e, y1 + uy * e)], fill=color, width=width)


def _h_arrowhead(draw, tip, direction, color, size=8):
    x, y = tip
    draw.polygon(
        [(x, y), (x - direction * size, y - size / 2), (x - direction * size, y + size / 2)],
        fill=color,
    )
