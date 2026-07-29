<#
.SYNOPSIS
    Claude Code PostToolUse hook: ghi lai moi lan goi CodeGraph/Graphify vao CSV
    theo tung nguoi dung, phuc vu he thong "ManagementToken" theo doi hoat dong team.

.DESCRIPTION
    Duoc dang ky trong .claude/settings.json (PostToolUse, matcher "Bash" va
    "mcp__codegraph.*"). Nhan JSON cua hook qua stdin, tu loc ra nhung lan goi
    thuc su lien quan (Bash chua "graphify", hoac MCP tool ten "codegraph*"),
    roi ghi 1 dong vao:

        docs/06-Management/skills/usage-logs/<safe-username>-<yyyy-MM>.csv

    QUAN TRONG - hook phai "fail-soft" tuyet doi: khong bao gio duoc throw loi
    hay block tool call cua nguoi dung. Moi loi bat duoc deu roi vao catch va
    exit 0 trong im lang.

    KHONG the lay so token that tu hook nay (transcript JSONL la dinh dang noi
    bo, Anthropic khuyen khong parse). Cot est_tokens chi la UOC LUONG
    (chars/4, cung cong thuc voi tools/codegraph-token-savings.ps1), danh dau
    token_source=estimate. So token THAT chi duoc nap vao qua
    tools/import-console-usage.ps1 tu file CSV xuat thu cong tu Org Analytics
    Dashboard (Team/Enterprise).

.NOTES
    Tham khao phong cach tu .github/modernize/java-upgrade/hooks/scripts/recordToolUse.ps1
    (doc stdin, append UTF8-no-BOM). Placeholder "mcp__codegraph.*" can xac
    nhan lai ten MCP tool that khi codegraph MCP server duoc dang ky trong repo.
#>

try {
    $raw = [Console]::In.ReadToEnd()
    if ([string]::IsNullOrWhiteSpace($raw)) { exit 0 }

    $payload = $raw | ConvertFrom-Json -ErrorAction Stop

    $toolName = [string]$payload.tool_name
    $toolInput = $payload.tool_input
    $toolResult = $payload.tool_result
    $sessionId = [string]$payload.session_id
    $toolUseId = [string]$payload.tool_use_id
    $cwd = [string]$payload.cwd
    if ([string]::IsNullOrWhiteSpace($cwd)) { $cwd = (Get-Location).Path }

    # --- Xac dinh co khop khong ---
    $matchedPattern = $null
    $targetOrQuery = $null

    if ($toolName -eq 'Bash' -and $toolInput -and $toolInput.command -match 'graphify') {
        $matchedPattern = 'graphify-bash'
        $targetOrQuery = [string]$toolInput.command
    }
    elseif ($toolName -match '^mcp__codegraph') {
        # Placeholder: chua co MCP server "codegraph" nao duoc dang ky trong repo
        # nay de xac nhan ten tool that - can cap nhat pattern nay khi co.
        $matchedPattern = 'codegraph-mcp'
        if ($toolInput.query) { $targetOrQuery = [string]$toolInput.query }
        elseif ($toolInput.projectPath) { $targetOrQuery = [string]$toolInput.projectPath }
        else { $targetOrQuery = ($toolInput | ConvertTo-Json -Compress -Depth 5) }
    }
    else {
        exit 0
    }

    # --- Xac dinh danh tinh ---
    $userEmail = $null
    try { $userEmail = (& git -C $cwd config user.email 2>$null) } catch {}
    if ([string]::IsNullOrWhiteSpace($userEmail)) {
        try { $userEmail = (& git -C $cwd config user.name 2>$null) } catch {}
    }
    if ([string]::IsNullOrWhiteSpace($userEmail)) { $userEmail = $env:USERNAME }
    if ([string]::IsNullOrWhiteSpace($userEmail)) { $userEmail = 'unknown' }

    $localPart = ($userEmail -split '@')[0]
    $safeUsername = ($localPart.ToLowerInvariant() -replace '[^a-z0-9\-]', '-')
    if ([string]::IsNullOrWhiteSpace($safeUsername)) { $safeUsername = 'unknown' }

    # --- Duong dan file CSV dich (rotate theo thang) ---
    $now = Get-Date
    $monthTag = $now.ToString('yyyy-MM')
    $logsDir = Join-Path $cwd 'docs/06-Management/skills/usage-logs'
    if (-not (Test-Path $logsDir)) { New-Item -ItemType Directory -Path $logsDir -Force | Out-Null }
    $csvPath = Join-Path $logsDir "$safeUsername-$monthTag.csv"

    # --- Chuan bi cac gia tri ---
    $inputChars = 0
    if ($toolInput) { $inputChars = ($toolInput | ConvertTo-Json -Compress -Depth 10).Length }
    $resultChars = 0
    if ($toolResult) { $resultChars = ($toolResult | ConvertTo-Json -Compress -Depth 10).Length }
    $estTokens = [math]::Ceiling(($inputChars + $resultChars) / 4.0)

    $targetOrQueryClean = ($targetOrQuery -replace '[\r\n]+', ' ').Trim()
    if ($targetOrQueryClean.Length -gt 300) { $targetOrQueryClean = $targetOrQueryClean.Substring(0, 300) }

    $timestampUtc = $now.ToUniversalTime().ToString('o')

    function ConvertTo-CsvField([string]$value) {
        if ($null -eq $value) { $value = '' }
        return '"' + ($value -replace '"', '""') + '"'
    }

    $header = 'timestamp_utc,user_email,session_id,tool_name,matched_pattern,target_or_query,purpose_note,changed_files_note,input_chars,result_chars,est_tokens,token_source,real_tokens,tool_use_id'

    $row = @(
        (ConvertTo-CsvField $timestampUtc),
        (ConvertTo-CsvField $userEmail),
        (ConvertTo-CsvField $sessionId),
        (ConvertTo-CsvField $toolName),
        (ConvertTo-CsvField $matchedPattern),
        (ConvertTo-CsvField $targetOrQueryClean),
        (ConvertTo-CsvField ''),
        (ConvertTo-CsvField ''),
        (ConvertTo-CsvField $inputChars),
        (ConvertTo-CsvField $resultChars),
        (ConvertTo-CsvField $estTokens),
        (ConvertTo-CsvField 'estimate'),
        (ConvertTo-CsvField ''),
        (ConvertTo-CsvField $toolUseId)
    ) -join ','

    $utf8NoBom = [System.Text.UTF8Encoding]::new($false)
    if (-not (Test-Path $csvPath)) {
        [System.IO.File]::AppendAllText($csvPath, $header + "`n", $utf8NoBom)
    }
    [System.IO.File]::AppendAllText($csvPath, $row + "`n", $utf8NoBom)

    exit 0
}
catch {
    # Fail-soft tuyet doi: khong bao gio de loi cua hook lam gian doan tool call
    exit 0
}
