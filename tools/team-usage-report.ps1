<#
.SYNOPSIS
    Tong hop cac file CSV do tools/hooks/record-codegraph-usage.ps1 ghi ra, sinh
    bao cao Markdown theo doi hoat dong CodeGraph/Graphify cua ca team ("ManagementToken").

.DESCRIPTION
    Doc toan bo docs/06-Management/skills/usage-logs/*.csv (moi user 1 file/thang,
    xem tools/hooks/record-codegraph-usage.ps1), gom nhom theo user + ngay, tinh:
      - So phien lam viec (session_id) va tong thoi luong (suy ra tu khoang thoi
        gian giua dong dau va dong cuoi cua tung session trong ngay do, KHONG phai
        do truc tiep - phien chi co 1 dong thi thoi luong = 0).
      - Token UOC LUONG (est_tokens, tu hook) va token THAT (real_tokens, chi co
        sau khi chay tools/import-console-usage.ps1) - hien thi rieng, khong bao
        gio cong lan vao nhau.
      - Danh sach "muc dich"/"thay doi gi" da duoc con nguoi dien tay (neu co).
    Neu chua co file log nao, bao cao se noi ro "chua co du lieu" thay vi bia so.

.PARAMETER LogsDir
    Thu muc chua cac file CSV theo tung nguoi dung.

.PARAMETER OutFile
    Noi ghi bao cao Markdown tong hop.

.PARAMETER MaxGapMinutes
    Neu khoang cach giua 2 dong lien tiep trong cung 1 session vuot qua so phut
    nay, danh dau "possible-idle-gap" thay vi tinh thang vao thoi luong lam viec.

.EXAMPLE
    pwsh ./tools/team-usage-report.ps1
#>
[CmdletBinding()]
param(
    [string]$LogsDir = "docs/06-Management/skills/usage-logs",
    [string]$OutFile = "docs/06-Management/skills/management-token-report.md",
    [int]$MaxGapMinutes = 240
)

$ErrorActionPreference = "Stop"

function ConvertTo-SafeDouble($value) {
    $result = 0.0
    if ($value -and [double]::TryParse($value, [ref]$result)) { return $result }
    return 0.0
}

Write-Host "Doc log tai: $LogsDir"
$allRows = New-Object 'System.Collections.Generic.List[object]'
$skippedFiles = New-Object 'System.Collections.Generic.List[string]'

if (Test-Path $LogsDir) {
    $csvFiles = Get-ChildItem -Path $LogsDir -Filter '*.csv' -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike '_unmatched-*' }
    foreach ($f in $csvFiles) {
        try {
            $rows = Import-Csv -LiteralPath $f.FullName -ErrorAction Stop
            foreach ($r in $rows) { $allRows.Add($r) }
        }
        catch {
            Write-Warning "Bo qua file loi: $($f.FullName) - $($_.Exception.Message)"
            [void]$skippedFiles.Add($f.Name)
        }
    }
}

$now = Get-Date -Format 'yyyy-MM-dd HH:mm'
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('# ManagementToken - Bao cao hoat dong CodeGraph/Graphify cua team')
[void]$sb.AppendLine()
[void]$sb.AppendLine("> Sinh tu dong boi tools/team-usage-report.ps1 luc $now. KHONG sua tay file nay.")
[void]$sb.AppendLine('> Tai lieu huong dan day du: [management-token-usage.md](./management-token-usage.md)')
[void]$sb.AppendLine()

if ($allRows.Count -eq 0) {
    [void]$sb.AppendLine('## Chua co du lieu')
    [void]$sb.AppendLine()
    [void]$sb.AppendLine("Khong tim thay dong log nao trong $LogsDir.")
    [void]$sb.AppendLine('Co the vi: (1) chua ai dung CodeGraph/Graphify tu khi hook duoc bat, hoac')
    [void]$sb.AppendLine('(2) cac thanh vien chua push file usage-logs/*.csv cua ho len remote -')
    [void]$sb.AppendLine('routine hang ngay chi thay du lieu da duoc push, khong thay file cuc bo chua push.')
    [void]$sb.AppendLine()
    $sb.ToString() | Out-File -FilePath $OutFile -Encoding utf8
    Write-Host "Da ghi bao cao (chua co du lieu): $OutFile"
    exit 0
}

# --- Gan cot 'date' (UTC, yyyy-MM-dd) cho tung dong ---
foreach ($r in $allRows) {
    [datetime]$dt = [datetime]::MinValue
    if ([DateTime]::TryParse($r.timestamp_utc, [ref]$dt)) {
        $r | Add-Member -NotePropertyName '_date' -NotePropertyValue $dt.ToString('yyyy-MM-dd') -Force
        $r | Add-Member -NotePropertyName '_dt' -NotePropertyValue $dt -Force
    }
    else {
        $r | Add-Member -NotePropertyName '_date' -NotePropertyValue 'unknown-date' -Force
        $r | Add-Member -NotePropertyName '_dt' -NotePropertyValue ([DateTime]::MinValue) -Force
    }
}

$groups = $allRows | Group-Object -Property user_email, _date

$reportRows = New-Object 'System.Collections.Generic.List[object]'
$needsManualFill = New-Object 'System.Collections.Generic.List[string]'

foreach ($g in $groups) {
    $rows = $g.Group
    $userEmail = $rows[0].user_email
    $date = $rows[0]._date

    $estTokensSum = ($rows | ForEach-Object { ConvertTo-SafeDouble $_.est_tokens } | Measure-Object -Sum).Sum
    # @() bat buoc: neu Where-Object chi tra ve 1 dong, PowerShell "un-wrap"
    # thanh 1 object don le (khong phai array) - luc do .Count se doc nham
    # property "Count" cua chinh object do (thuong la $null) thay vi so luong.
    $realRows = @($rows | Where-Object { $_.token_source -eq 'console_export' -and $_.real_tokens })
    $realTokensSum = ($realRows | ForEach-Object { ConvertTo-SafeDouble $_.real_tokens } | Measure-Object -Sum).Sum
    $hasReal = $realRows.Count -gt 0

    # @() bat buoc: neu chi co 1 session_id duy nhat, Group-Object tra ve 1
    # GroupInfo don le (khong phai array) - luc do .Count se doc nham thanh
    # so item BEN TRONG group do (khong phai so luong group).
    $sessionGroups = @($rows | Group-Object -Property session_id)
    $totalDurationMinutes = 0.0
    $idleGapNote = $false
    foreach ($sg in $sessionGroups) {
        $sorted = @($sg.Group | Sort-Object -Property _dt)
        if ($sorted.Count -le 1) { continue }
        $prev = $null
        foreach ($row in $sorted) {
            if ($prev) {
                $gapMinutes = ($row._dt - $prev._dt).TotalMinutes
                if ($gapMinutes -gt $MaxGapMinutes) { $idleGapNote = $true }
                else { $totalDurationMinutes += $gapMinutes }
            }
            $prev = $row
        }
    }

    $purposeList = ($rows | ForEach-Object { $_.purpose_note } | Where-Object { $_ -and $_.Trim() -ne '' } | Select-Object -Unique) -join '; '
    $changedList = ($rows | ForEach-Object { $_.changed_files_note } | Where-Object { $_ -and $_.Trim() -ne '' } | Select-Object -Unique) -join '; '

    if ([string]::IsNullOrWhiteSpace($purposeList) -or [string]::IsNullOrWhiteSpace($changedList)) {
        [void]$needsManualFill.Add("$userEmail / $date")
    }

    $tokenRealDisplay = if ($hasReal) { [string][math]::Round($realTokensSum, 0) } else { '-' }
    $purposeDisplay = if ($purposeList) { $purposeList } else { '(chua dien)' }
    $changedDisplay = if ($changedList) { $changedList } else { '(chua dien)' }
    $durationDisplay = [string][math]::Round($totalDurationMinutes, 1)
    if ($idleGapNote) { $durationDisplay += ' (co khoang idle bi loai tru)' }

    $reportRows.Add([pscustomobject]@{
        User            = $userEmail
        Date            = $date
        Sessions        = $sessionGroups.Count
        DurationMinutes = $durationDisplay
        Purpose         = $purposeDisplay
        Changed         = $changedDisplay
        EstTokens       = [string][math]::Round($estTokensSum, 0)
        RealTokens      = $tokenRealDisplay
    })
}


# @() bat buoc: cung ly do nhu tren - neu chi co 1 dong bao cao, Sort-Object
# se tra ve 1 object don le thay vi array, lam $reportRows.Count phia duoi bi sai/rong.
$reportRows = @($reportRows | Sort-Object -Property @{Expression = 'Date'; Descending = $true }, @{Expression = 'User'; Descending = $false })

[void]$sb.AppendLine('## Bang tong hop theo nguoi dung / ngay')
[void]$sb.AppendLine()
[void]$sb.AppendLine('| Ai | Ngay | So phien | Tong thoi luong (phut) | Muc dich (best-effort) | Thay doi gi (best-effort) | Token uoc luong | Token that (neu co) |')
[void]$sb.AppendLine('|---|---|---|---|---|---|---|---|')
foreach ($rr in $reportRows) {
    [void]$sb.AppendLine("| $($rr.User) | $($rr.Date) | $($rr.Sessions) | $($rr.DurationMinutes) | $($rr.Purpose) | $($rr.Changed) | $($rr.EstTokens) | $($rr.RealTokens) |")
}
[void]$sb.AppendLine()

[void]$sb.AppendLine('## Can dien thu cong')
[void]$sb.AppendLine()
if ($needsManualFill.Count -gt 0) {
    [void]$sb.AppendLine('Cac dong sau co purpose_note hoac changed_files_note con trong - mo file CSV')
    [void]$sb.AppendLine('tuong ung trong usage-logs/ de dien tay neu muon:')
    [void]$sb.AppendLine()
    foreach ($item in ($needsManualFill | Select-Object -Unique)) {
        [void]$sb.AppendLine("- $item")
    }
}
else {
    [void]$sb.AppendLine('Khong co dong nao thieu purpose_note/changed_files_note.')
}
[void]$sb.AppendLine()

if ($skippedFiles.Count -gt 0) {
    [void]$sb.AppendLine('## File log bi loi (da bo qua)')
    [void]$sb.AppendLine()
    foreach ($f in $skippedFiles) { [void]$sb.AppendLine("- $f") }
    [void]$sb.AppendLine()
}

[void]$sb.AppendLine('## Phuong phap')
[void]$sb.AppendLine()
[void]$sb.AppendLine('- Token uoc luong (est_tokens) = (input_chars + result_chars) / 4, ghi luc hook')
[void]$sb.AppendLine('  chay - khong phai so token that tu Anthropic.')
[void]$sb.AppendLine('- Token that (real_tokens) chi xuat hien sau khi chay tools/import-console-usage.ps1')
[void]$sb.AppendLine('  voi file CSV xuat thu cong tu Org Analytics Dashboard (Team/Enterprise). Cot nay')
[void]$sb.AppendLine('  hien "-" neu chua nap.')
[void]$sb.AppendLine('- Thoi luong la suy ra tu khoang cach thoi gian giua cac lan goi tool trong cung')
[void]$sb.AppendLine("  1 session_id, khong phai do truc tiep. Khoang cach vuot qua $MaxGapMinutes phut")
[void]$sb.AppendLine('  giua 2 lan goi lien tiep bi loai khoi tong thoi luong (coi la nghi giua chung,')
[void]$sb.AppendLine('  khong phai dang lam viec lien tuc) va duoc danh dau trong bang.')
[void]$sb.AppendLine('- Bao cao nay chi phan anh du lieu da duoc push len remote truoc thoi diem chay -')
[void]$sb.AppendLine('  xem management-token-usage.md muc "quy trinh push".')
[void]$sb.AppendLine()

$sb.ToString() | Out-File -FilePath $OutFile -Encoding utf8
Write-Host "Da ghi bao cao: $OutFile ($($reportRows.Count) dong, $($allRows.Count) su kien tho)"
