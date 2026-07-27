from __future__ import annotations

import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DOC_ROOT = ROOT / "docs" / "02-SDD-Architecture" / "feat_flow"
JAVA_ROOT = ROOT / "apps" / "backend" / "src" / "main" / "java"
START = "<!-- BACKEND-METHOD-INVENTORY:START -->"
END = "<!-- BACKEND-METHOD-INVENTORY:END -->"

JAVA_PATH_RE = re.compile(
    r"apps/backend/src/main/java/[A-Za-z0-9_./-]+\.java"
)
TYPE_RE = re.compile(r"\b(class|interface|enum|record)\s+([A-Za-z_]\w*)")
METHOD_RE = re.compile(
    r"(?m)^[ \t]*"
    r"(?P<annotations>(?:@[A-Za-z_][\w.]*"
    r"(?:\((?:[^()\"']|\"[^\"]*\"|'[^']*')*\))?[ \t]*\r?\n[ \t]*)*)"
    r"(?P<mods>(?:(?:public|protected|private|static|default|abstract|final|"
    r"synchronized|native|strictfp)\s+)*)"
    r"(?P<ret>[A-Za-z_][\w.<>, ?\[\]]*)\s+"
    r"(?P<name>[A-Za-z_]\w*)\s*"
    r"\((?P<params>[^;{}()]*)\)"
    r"[ \t]*(?:throws[^{;]+)?(?P<end>[{;])"
)
MAPPING_RE = re.compile(
    r"@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping|RequestMapping)"
    r"(?:\((.*?)\))?"
)


def clean_signature(return_type: str, name: str, params: str) -> str:
    return_type = " ".join(return_type.split())
    params = " ".join(params.split())
    return f"{return_type} {name}({params})"


def endpoint_from_annotations(annotations: str) -> str:
    found = []
    for annotation, args in MAPPING_RE.findall(annotations):
        method = {
            "GetMapping": "GET",
            "PostMapping": "POST",
            "PutMapping": "PUT",
            "PatchMapping": "PATCH",
            "DeleteMapping": "DELETE",
            "RequestMapping": "REQUEST",
        }[annotation]
        quoted = re.search(r'"([^"]*)"', args or "")
        found.append(f"{method} {quoted.group(1) if quoted else ''}".strip())
    return ", ".join(found)


def purpose(name: str, annotations: str, type_name: str) -> str:
    endpoint = endpoint_from_annotations(annotations)
    lower = name.lower()
    spaced = re.sub(r"(?<!^)(?=[A-Z])", " ", name).lower()

    if endpoint:
        return f"Xử lý endpoint `{endpoint}`; thực hiện nghiệp vụ `{spaced}`."
    if lower.startswith(("find", "get", "load", "search", "list", "fetch")):
        return f"Đọc hoặc tra cứu dữ liệu phục vụ `{spaced}`."
    if lower.startswith("count"):
        return f"Đếm dữ liệu phục vụ thống kê `{spaced}`."
    if lower.startswith(("exists", "has", "is", "can", "validate", "verify", "check")):
        return f"Kiểm tra điều kiện/trạng thái phục vụ `{spaced}`."
    if lower.startswith(("create", "register", "add", "save", "store", "submit")):
        return f"Tạo hoặc ghi dữ liệu cho nghiệp vụ `{spaced}`."
    if lower.startswith(("update", "change", "edit", "activate", "suspend", "toggle", "mark")):
        return f"Cập nhật trạng thái/dữ liệu cho nghiệp vụ `{spaced}`."
    if lower.startswith(("delete", "remove", "revoke", "invalidate", "clear")):
        return f"Xóa mềm, thu hồi hoặc loại bỏ dữ liệu trong `{spaced}`."
    if lower.startswith(("send", "notify", "broadcast", "dispatch", "deliver")):
        return f"Gửi hoặc phân phối thông tin cho nghiệp vụ `{spaced}`."
    if lower.startswith(("build", "to", "map", "convert", "resolve", "calculate", "compute")):
        return f"Biến đổi/tổng hợp dữ liệu nội bộ cho `{spaced}`."
    if "Repository" in type_name:
        return f"Truy vấn persistence phục vụ `{spaced}`."
    return f"Thực hiện xử lý backend `{spaced}` trong `{type_name}`."


def extract_methods(java_file: Path) -> tuple[str, list[dict[str, object]]]:
    source = java_file.read_text(encoding="utf-8")
    type_match = TYPE_RE.search(source)
    type_name = type_match.group(2) if type_match else java_file.stem
    methods = []

    for match in METHOD_RE.finditer(source):
        name = match.group("name")
        return_type = " ".join(match.group("ret").split())
        # Constructor-like declarations are not backend functions.
        if name == type_name:
            continue
        # Avoid matching control-flow fragments that happen to resemble declarations.
        if return_type.split()[0] in {"return", "new", "throw", "if", "for", "while", "switch"}:
            continue
        line = source.count("\n", 0, match.start()) + 1
        annotations = match.group("annotations") or ""
        methods.append(
            {
                "line": line,
                "signature": clean_signature(return_type, name, match.group("params")),
                "endpoint": endpoint_from_annotations(annotations),
                "purpose": purpose(name, annotations, type_name),
            }
        )
    return type_name, methods


def source_link(java_file: Path, line: int | None = None) -> str:
    relative = java_file.relative_to(ROOT).as_posix()
    target = f"../../../{relative}"
    return f"{target}#L{line}" if line else target


def build_inventory(doc: Path, java_files: list[Path]) -> str:
    lines = [
        START,
        "",
        "## Phụ lục — Danh mục đầy đủ hàm backend",
        "",
        "> Phần này được đối chiếu trực tiếp từ source backend hiện tại. "
        "Chỉ liệt kê các hàm khai báo tường minh trong những file Java mà tài liệu này tham chiếu; "
        "các hàm do Lombok/JPA sinh tự động không xuất hiện trong source nên không liệt kê.",
        "",
    ]

    total = 0
    for java_file in java_files:
        type_name, methods = extract_methods(java_file)
        if not methods:
            continue
        total += len(methods)
        lines.extend(
            [
                f"### `{type_name}`",
                "",
                f"Nguồn: [{java_file.name}]({source_link(java_file)})",
                "",
                "| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |",
                "|---:|---|---|---|",
            ]
        )
        for index, method in enumerate(methods, 1):
            signature = str(method["signature"]).replace("|", r"\|")
            endpoint = str(method["endpoint"]).replace("|", r"\|") or "—"
            purpose_text = str(method["purpose"]).replace("|", r"\|")
            link = source_link(java_file, int(method["line"]))
            lines.append(
                f"| {index} | [`{signature}`]({link}) | `{endpoint}` | {purpose_text} |"
            )
        lines.append("")

    if total == 0:
        lines.extend(
            [
                "_Tài liệu hiện không tham chiếu file Java nào có hàm được khai báo tường minh._",
                "",
            ]
        )
    lines.extend(
        [
            f"**Tổng cộng:** `{total}` hàm backend trong `{len(java_files)}` file Java được tham chiếu.",
            "",
            END,
        ]
    )
    return "\n".join(lines)


def referenced_java_files(text: str) -> list[Path]:
    files = set()
    for raw_path in JAVA_PATH_RE.findall(text):
        candidate = ROOT / Path(raw_path)
        if candidate.is_file() and candidate.is_relative_to(JAVA_ROOT):
            files.add(candidate)
    return sorted(files, key=lambda path: path.as_posix().lower())


def main() -> None:
    updated = 0
    skipped = 0
    for doc in sorted(DOC_ROOT.rglob("*.md")):
        text = doc.read_text(encoding="utf-8")
        java_files = referenced_java_files(text)
        if not java_files:
            skipped += 1
            continue
        inventory = build_inventory(doc, java_files)
        if START in text and END in text:
            prefix, remainder = text.split(START, 1)
            _, suffix = remainder.split(END, 1)
            new_text = prefix.rstrip() + "\n\n" + inventory + suffix
        else:
            new_text = text.rstrip() + "\n\n" + inventory + "\n"
        doc.write_text(new_text, encoding="utf-8", newline="\n")
        updated += 1

    print(f"Updated Markdown files: {updated}")
    print(f"Skipped without backend Java references: {skipped}")


if __name__ == "__main__":
    main()
