<#
.SYNOPSIS
    Do that so sanh so token thuc te giua "cach cu" (grep + doc full source) va
    "cach moi" (Graphify: truy van do thi con da duoc xay san tai apps/graphify-out/graph.json).

.DESCRIPTION
    Day la mot cong cu DO THUC TE, khong phai uoc luong tay:
      - "OLD" = tong so ky tu cua toan bo noi dung that cua cac file lien quan
        (target node + hang xom N-hop trong do thi phu thuoc), doc truc tiep tu dia.
      - "NEW" = so ky tu cua subgraph JSON toi gian (id/label/source_file + cac edge)
        cho dung nhung node/edge do — day chinh la thu ma `graphify query` /
        `graphify path` tra ve thay vi bat AI phai doc lai toan bo file nguon.
      - Token duoc UOC LUONG bang heuristic pho bien "~4 ky tu / token" (khong co
        tokenizer that trong moi truong nay) — luon hien ca so ky tu that lan
        token uoc luong de nguoi doc tu kiem chung.

    CodeGraph (MCP tool) KHONG duoc do trong script nay vi index `.codegraph/`
    trong repo hien dang RONG va khong co MCP tool song trong moi truong chay
    script — script se canh bao ro thay vi bia so lieu. Phuong phap o day ap
    dung duoc cho CodeGraph ngay khi index duoc xay (xem ghi chu cuoi report).

.PARAMETER GraphPath
    Duong dan toi graph.json cua Graphify.

.PARAMETER SearchRoots
    Cac thu muc goc de tim file that tren dia theo ten file (basename), vi
    source_file trong graph.json co the bi lech thu muc con neu code da refactor
    sau khi graph duoc build (xem muc "Phat hien phu" trong report).

.PARAMETER OutFile
    Noi ghi bao cao Markdown.

.EXAMPLE
    pwsh ./tools/codegraph-token-savings.ps1
#>
[CmdletBinding()]
param(
    [string]$GraphPath = "apps/graphify-out/graph.json",
    [string[]]$SearchRoots = @("apps/backend/src", "apps/frontend/src"),
    [string]$OutFile = "docs/06-Management/skills/codegraph-token-savings-report.md",
    [int]$TopN = 10
)

$ErrorActionPreference = "Stop"
# Script gia dinh duoc chay tu thu muc goc repo (vi du: pwsh ./tools/codegraph-token-savings.ps1)

function Estimate-Tokens([long]$chars) {
    # Heuristic pho bien: ~4 ky tu/token cho van ban tieng Anh/code (OpenAI/Anthropic
    # cookbook). Day la UOC LUONG, khong phai dem tokenizer that.
    return [math]::Ceiling($chars / 4.0)
}

function Format-Num([long]$n) {
    return ('{0:N0}' -f $n)
}

Write-Host "Loading graph: $GraphPath"
if (-not (Test-Path $GraphPath)) {
    throw "Khong tim thay graph.json tai '$GraphPath'. Chay 'graphify update .' truoc."
}
$graph = Get-Content -Raw $GraphPath | ConvertFrom-Json
$nodes = $graph.nodes
$edges = $graph.edges
Write-Host "  nodes=$($nodes.Count) edges=$($edges.Count)"

# --- Xay index tra cuu node theo id, va tim file that tren dia theo basename ---
$nodeById = @{}
foreach ($n in $nodes) { $nodeById[$n.id] = $n }

Write-Host "Indexing real files under: $($SearchRoots -join ', ')"
$diskFiles = Get-ChildItem -Path $SearchRoots -Recurse -File -ErrorAction SilentlyContinue
$fileByBasename = @{}
foreach ($f in $diskFiles) {
    if (-not $fileByBasename.ContainsKey($f.Name)) { $fileByBasename[$f.Name] = @() }
    $fileByBasename[$f.Name] += $f.FullName
}

$unresolvedCount = 0
$staleDirCount = 0
foreach ($n in $nodes) {
    if ($n.file_type -ne 'code' -or -not $n.source_file) { continue }
    $bn = Split-Path $n.source_file -Leaf
    if (-not $fileByBasename.ContainsKey($bn)) { $unresolvedCount++; continue }
    $real = $fileByBasename[$bn][0]
    $normalizedReal = ($real -replace '\\', '/')
    if ($normalizedReal -notmatch [regex]::Escape($n.source_file)) { $staleDirCount++ }
}
Write-Host "  Node source_file khong resolve duoc tren dia: $unresolvedCount"
Write-Host "  Node source_file lech thu muc so voi vi tri that hien tai (graph cu): $staleDirCount"

function Get-RealFilePath($node) {
    if ($node.file_type -ne 'code' -or -not $node.source_file) { return $null }
    $bn = Split-Path $node.source_file -Leaf
    if ($fileByBasename.ContainsKey($bn)) { return $fileByBasename[$bn][0] }
    return $null
}

# --- BFS lay hang xom N-hop cua 1 node trong do thi (khong phan biet chieu) ---
function Get-Neighborhood($startId, [int]$hops) {
    $visited = New-Object 'System.Collections.Generic.HashSet[string]'
    [void]$visited.Add($startId)
    $frontier = @($startId)
    $usedEdges = New-Object 'System.Collections.Generic.List[object]'
    for ($h = 0; $h -lt $hops; $h++) {
        $next = @()
        foreach ($e in $edges) {
            if ($frontier -contains $e.source -and -not $visited.Contains($e.target)) {
                [void]$visited.Add($e.target); $next += $e.target
            }
            if ($frontier -contains $e.target -and -not $visited.Contains($e.source)) {
                [void]$visited.Add($e.source); $next += $e.source
            }
        }
        $frontier = $next
    }
    foreach ($e in $edges) {
        if ($visited.Contains($e.source) -and $visited.Contains($e.target)) { $usedEdges.Add($e) }
    }
    return [pscustomobject]@{
        NodeIds = @($visited)
        Edges   = $usedEdges
    }
}

# --- Do "OLD WAY": doc full source that cua tap file lien quan ---
function Measure-OldWay([string[]]$nodeIds) {
    $totalChars = 0L
    $fileCount = 0
    $seen = New-Object 'System.Collections.Generic.HashSet[string]'
    foreach ($id in $nodeIds) {
        $n = $nodeById[$id]
        if (-not $n) { continue }
        $path = Get-RealFilePath $n
        if (-not $path -or $seen.Contains($path)) { continue }
        [void]$seen.Add($path)
        $content = Get-Content -Raw -LiteralPath $path -ErrorAction SilentlyContinue
        if ($null -ne $content) {
            $totalChars += $content.Length
            $fileCount++
        }
    }
    return [pscustomobject]@{ Chars = $totalChars; Files = $fileCount }
}

# --- Do "NEW WAY": kich thuoc subgraph JSON toi gian (dung nhu graphify query/path tra ve) ---
function Measure-NewWay($nodeIds, $edgeList) {
    $compactNodes = foreach ($id in $nodeIds) {
        $n = $nodeById[$id]
        if ($n) { [pscustomobject]@{ id = $n.id; label = $n.label; source_file = $n.source_file } }
    }
    $compactEdges = foreach ($e in $edgeList) {
        [pscustomobject]@{ source = $e.source; target = $e.target; relation = $e.relation }
    }
    $payload = [pscustomobject]@{ nodes = $compactNodes; edges = $compactEdges }
    $json = $payload | ConvertTo-Json -Depth 5 -Compress
    return [pscustomobject]@{ Chars = $json.Length; Nodes = $compactNodes.Count; Edges = $compactEdges.Count }
}

function Run-Scenario([string]$name, [string]$startId, [int]$hops) {
    if (-not $nodeById.ContainsKey($startId)) {
        Write-Warning "Bo qua kich ban '$name': khong tim thay node id '$startId' trong graph.json"
        return $null
    }
    $nbh = Get-Neighborhood -startId $startId -hops $hops
    $old = Measure-OldWay -nodeIds $nbh.NodeIds
    $new = Measure-NewWay -nodeIds $nbh.NodeIds -edgeList $nbh.Edges
    $reduction = 0
    if ($old.Chars -gt 0) { $reduction = [math]::Round((1 - ($new.Chars / [double]$old.Chars)) * 100, 1) }
    return [pscustomobject]@{
        Scenario       = $name
        Hops           = $hops
        FilesOldWay    = $old.Files
        OldChars       = $old.Chars
        OldTokensEst   = Estimate-Tokens $old.Chars
        NewChars       = $new.Chars
        NewTokensEst   = Estimate-Tokens $new.Chars
        ReductionPct   = $reduction
    }
}

Write-Host "`nRunning scenarios..."
$results = New-Object 'System.Collections.Generic.List[object]'

$r1 = Run-Scenario "Impact analysis: ContentReviewService (1-hop)" "backend_src_main_java_com_jlpt_feature_contentreview_contentreviewservice" 1
if ($r1) { $results.Add($r1) }

$r2 = Run-Scenario "Trace Controller->Service: ManagerReviewController (1-hop)" "backend_src_main_java_com_jlpt_feature_contentreview_managerreviewcontroller" 1
if ($r2) { $results.Add($r2) }

$r3 = Run-Scenario "Hieu toan bo tinh nang (end-to-end): ContentReviewService (2-hop)" "backend_src_main_java_com_jlpt_feature_contentreview_contentreviewservice" 2
if ($r3) { $results.Add($r3) }

# --- Kich ban rieng: "God Nodes" / cau hoi cau truc toan module ---
$deg = @{}
foreach ($e in $edges) {
    $deg[$e.source] = 1 + [int]($deg[$e.source])
    $deg[$e.target] = 1 + [int]($deg[$e.target])
}
$allCodeNodeIds = $nodes | Where-Object { $_.file_type -eq 'code' } | ForEach-Object { $_.id }
$oldAll = Measure-OldWay -nodeIds $allCodeNodeIds
$uniqueRealFileCount = $oldAll.Files
$topGod = $deg.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First $TopN |
    ForEach-Object {
        $n = $nodeById[$_.Key]
        [pscustomobject]@{ label = $(if ($n) { $n.label } else { $_.Key }); degree = $_.Value }
    }
$godJson = ($topGod | ConvertTo-Json -Depth 3 -Compress)
$godNodesResult = [pscustomobject]@{
    Scenario     = "God Nodes / cau truc toan module contentreview (top $TopN, do da tinh san)"
    Hops         = "N/A (toan module, $uniqueRealFileCount file that)"
    FilesOldWay  = $oldAll.Files
    OldChars     = $oldAll.Chars
    OldTokensEst = Estimate-Tokens $oldAll.Chars
    NewChars     = $godJson.Length
    NewTokensEst = Estimate-Tokens $godJson.Length
    ReductionPct = [math]::Round((1 - ($godJson.Length / [double]$oldAll.Chars)) * 100, 1)
}
$results.Add($godNodesResult)

Write-Host "`n=== KET QUA (so lieu that, do truc tiep tu repo hien tai) ===`n"
$results | Format-Table Scenario, Hops, FilesOldWay, OldChars, OldTokensEst, NewChars, NewTokensEst, ReductionPct -AutoSize

# --- Xuat bao cao Markdown ---
$now = Get-Date -Format "yyyy-MM-dd HH:mm"
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('# Thong ke thuc te: Graphify tiet kiem token bao nhieu so voi cach cu')
[void]$sb.AppendLine()
[void]$sb.AppendLine("> Sinh tu dong boi tools/codegraph-token-savings.ps1 luc $now.")
[void]$sb.AppendLine('> Chay lai script bat cu luc nao sau khi "graphify update ." de co so lieu moi nhat - KHONG sua tay file nay.')
[void]$sb.AppendLine('> Tai lieu dinh tinh lien quan: [codegraph-graphify-loi-ich.md](./codegraph-graphify-loi-ich.md)')
[void]$sb.AppendLine()
[void]$sb.AppendLine('## Phuong phap do')
[void]$sb.AppendLine()
[void]$sb.AppendLine('- **OLD (cach cu)**: doc toan bo noi dung that cua file muc tieu + cac file hang xom')
[void]$sb.AppendLine('  N-hop trong do thi phu thuoc (import/su dung), lay truc tiep tu dia - mo phong dung')
[void]$sb.AppendLine('  viec mot AI agent/dev phai grep roi mo tung file de tu suy ra quan he.')
[void]$sb.AppendLine('- **NEW (Graphify)**: kich thuoc subgraph JSON toi gian (chi id/label/source_file +')
[void]$sb.AppendLine('  danh sach edge source/target/relation) cho dung tap node/edge do - day la dang')
[void]$sb.AppendLine('  du lieu ma "graphify query" / "graphify path" thuc su tra ve thay vi bat AI doc lai source.')
[void]$sb.AppendLine('- **Token uoc luong** = so ky tu / 4 (heuristic pho bien cho van ban Anh/code). Day la')
[void]$sb.AppendLine('  UOC LUONG vi moi truong chay script khong co tokenizer that (khong co Node/Python kha dung')
[void]$sb.AppendLine('  de goi tiktoken) - luon doi chieu voi cot so ky tu that (OldChars/NewChars) o tren.')
[void]$sb.AppendLine("- Du lieu do thi: apps/graphify-out/graph.json - hien chi phu module contentreview")
[void]$sb.AppendLine("  ($($nodes.Count) node do thi / $($edges.Count) edge, anh xa toi $uniqueRealFileCount file .java that tren dia), khong phai toan repo.")
[void]$sb.AppendLine('  Muon co so lieu cho module khac, chay "graphify update ." tren toan repo roi chay lai script nay.')
[void]$sb.AppendLine()
[void]$sb.AppendLine('## Ket qua')
[void]$sb.AppendLine()
[void]$sb.AppendLine('| Kich ban | So file phai doc (cach cu) | OLD - ky tu that | OLD - token uoc luong | NEW - ky tu that | NEW - token uoc luong | Giam |')
[void]$sb.AppendLine('|---|---|---|---|---|---|---|')
foreach ($r in $results) {
    [void]$sb.AppendLine("| $($r.Scenario) | $($r.FilesOldWay) | $(Format-Num $r.OldChars) | $(Format-Num $r.OldTokensEst) | $(Format-Num $r.NewChars) | $(Format-Num $r.NewTokensEst) | **$($r.ReductionPct)%** |")
}
[void]$sb.AppendLine()
[void]$sb.AppendLine('## Phat hien phu (tu chinh du lieu that)')
[void]$sb.AppendLine()
[void]$sb.AppendLine("- **$unresolvedCount** node trong graph.json khong tim thay file tren dia theo ten (co the da bi xoa/doi ten sau khi graph duoc build).")
[void]$sb.AppendLine("- **$staleDirCount** node co source_file ghi sai thu muc con so voi vi tri that hien tai tren dia")
[void]$sb.AppendLine('  (vd. file da duoc di chuyen vao service/, controller/... sau khi graph duoc build lan dau).')
[void]$sb.AppendLine('  -> Xac nhan dung diem "Gioi han can biet" trong codegraph-graphify-loi-ich.md: do thi la anh chup,')
[void]$sb.AppendLine('  can chay "graphify update ." thuong xuyen de khong bi lech thuc te.')
[void]$sb.AppendLine()
[void]$sb.AppendLine('## Ve CodeGraph (chua co so lieu that trong moi truong nay)')
[void]$sb.AppendLine()
[void]$sb.AppendLine('Script nay KHONG bao gom so lieu do thuc cho CodeGraph vi trong moi truong chay script:')
[void]$sb.AppendLine('- thu muc index docs/06-Management/skills/.codegraph/ dang RONG (chua chay "codegraph init"), va')
[void]$sb.AppendLine('- khong co MCP tool codegraph_explore song de goi truy van that.')
[void]$sb.AppendLine()
[void]$sb.AppendLine('Phuong phap do o tren (OLD = doc full source hang xom N-hop, NEW = kich thuoc payload tra ve) ap dung')
[void]$sb.AppendLine('duoc y het cho CodeGraph - chi can thay NEW bang do dai response that cua codegraph_explore cho cung')
[void]$sb.AppendLine('mot tap file. Khi index da duoc xay (codegraph init roi explore vai truy van trong codegraph_prompts.md),')
[void]$sb.AppendLine('co the bo sung cot CodeGraph vao bang tren bang so do thuc - khong nen dien so uoc doan vao day.')
[void]$sb.AppendLine()

$sb.ToString() | Out-File -FilePath $OutFile -Encoding utf8
Write-Host "`nDa ghi bao cao: $OutFile"
