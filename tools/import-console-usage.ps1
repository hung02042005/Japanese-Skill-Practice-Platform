<#
.SYNOPSIS
    Nap so token THAT (tu file CSV xuat thu cong o Org Analytics Dashboard cua
    Claude Team/Enterprise) vao cac file usage-logs/*.csv cuc bo, phuc vu he
    thong "ManagementToken".

.DESCRIPTION
    KHONG doan mo hinh schema cua file CSV export tu Console (khong fetch duoc
    docs chinh thuc de xac nhan ten cot that). Script nay TU DO ten cot theo tu
    khoa (khong phan biet hoa thuong):
        - cot ngay: header chua "date" hoac "day"
        - cot nguoi dung: header chua "user", "email", hoac "member"
        - cot token: header chua "token"; neu tach rieng input/output (header
          chua "input" va "output") thi cong lai; neu chi 1 cot token duy nhat
          thi dung truc tiep

    Neu bat ky nhom nao khong tim duoc dung 1 cot (khong thay, hoac thay nhieu
    cot mo ho) - script DUNG LAI, in ro danh sach header that da doc duoc, va
    KHONG ghi gi ca. Day la hanh vi quan trong nhat cua script: tha con hon
    doan sai.

    Voi moi dong import khop duoc: tim file usage-logs/<user>-<thang>.csv tuong
    ung; neu co, ghi token_source=console_export + real_tokens vao MOT dong dai
    dien cua ngay do (uu tien dong da duoc danh dau console_export tu lan chay
    truoc, de idempotent khi chay lai; neu chua co thi chon dong co timestamp
    som nhat trong ngay). KHONG BAO GIO xoa est_tokens - giu lai de so sanh voi
    real_tokens ve sau.

    Neu user/ngay trong Console export khong khop file cuc bo nao, ghi vao
    usage-logs/_unmatched-console-rows.csv de xem lai thu cong, khong bo lang le.

.PARAMETER ConsoleCsvPath
    Duong dan file CSV xuat tu Org Analytics Dashboard (Team/Enterprise).

.PARAMETER LogsDir
    Thu muc chua cac file CSV usage-logs cuc bo (moi user 1 file/thang).

.EXAMPLE
    pwsh ./tools/import-console-usage.ps1 -ConsoleCsvPath C:\Downloads\console-usage-2026-07.csv
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ConsoleCsvPath,
    [string]$LogsDir = "docs/06-Management/skills/usage-logs"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ConsoleCsvPath)) {
    throw "Khong tim thay file: $ConsoleCsvPath"
}

$imported = Import-Csv -LiteralPath $ConsoleCsvPath
if (-not $imported -or $imported.Count -eq 0) {
    throw "File $ConsoleCsvPath khong co dong du lieu nao."
}

$headers = $imported[0].PSObject.Properties.Name
Write-Host "Header thuc te doc duoc tu $ConsoleCsvPath :"
Write-Host ("  " + ($headers -join ', '))


# QUAN TRONG: bat buoc boc @(...) - neu Where-Object chi tra ve 1 ket qua,
# PowerShell se "un-wrap" thanh 1 string thay vi array 1 phan tu, khien
# [0] sau nay index vao TUNG KY TU cua string do thay vi lay ca chuoi.
$dateCandidates = @($headers | Where-Object { $_ -match '(?i)date|day' })
$userCandidates = @($headers | Where-Object { $_ -match '(?i)user|email|member' })
$tokenCandidates = @($headers | Where-Object { $_ -match '(?i)token' })
$inputTokenCandidates = @($tokenCandidates | Where-Object { $_ -match '(?i)input' })
$outputTokenCandidates = @($tokenCandidates | Where-Object { $_ -match '(?i)output' })

$abort = $false
if ($dateCandidates.Count -ne 1) {
    Write-Warning "Khong xac dinh duoc DUY NHAT 1 cot ngay. Ung vien tim thay: $($dateCandidates -join ', ')"
    $abort = $true
}
if ($userCandidates.Count -ne 1) {
    Write-Warning "Khong xac dinh duoc DUY NHAT 1 cot nguoi dung. Ung vien tim thay: $($userCandidates -join ', ')"
    $abort = $true
}

$tokenMode = $null
$tokenColSingle = $null
$tokenColInput = $null
$tokenColOutput = $null
if ($inputTokenCandidates.Count -eq 1 -and $outputTokenCandidates.Count -eq 1) {
    $tokenMode = 'split'
    $tokenColInput = $inputTokenCandidates[0]
    $tokenColOutput = $outputTokenCandidates[0]
}
elseif ($tokenCandidates.Count -eq 1) {
    $tokenMode = 'single'
    $tokenColSingle = $tokenCandidates[0]
}
else {
    Write-Warning "Khong xac dinh duoc cot token ro rang. Ung vien tim thay: $($tokenCandidates -join ', ')"
    $abort = $true
}

if ($abort) {
    Write-Host ''
    Write-Host 'DUNG LAI - khong ghi gi ca. Hay doi ten cot trong file export (hoac sua script)' -ForegroundColor Yellow
    Write-Host 'de khop dung 1 cot cho moi nhom (ngay / nguoi dung / token) roi chay lai.'
    exit 1
}

Write-Host ''
Write-Host "Cot duoc dung: ngay='$($dateCandidates[0])', nguoi dung='$($userCandidates[0])', token=$tokenMode ($(if ($tokenMode -eq 'split') { "$tokenColInput + $tokenColOutput" } else { $tokenColSingle }))"
Write-Host ''

$dateCol = $dateCandidates[0]
$userCol = $userCandidates[0]

$matchedCount = 0
$unmatchedRows = New-Object 'System.Collections.Generic.List[object]'

foreach ($row in $imported) {
    [datetime]$parsedDate = [datetime]::MinValue
    if (-not [DateTime]::TryParse($row.$dateCol, [ref]$parsedDate)) {
        Write-Warning "Bo qua 1 dong: khong parse duoc ngay '$($row.$dateCol)'"
        continue
    }
    $dateStr = $parsedDate.ToString('yyyy-MM-dd')
    $monthTag = $parsedDate.ToString('yyyy-MM')

    $rawUser = [string]$row.$userCol
    $localPart = ($rawUser -split '@')[0]
    $safeUsername = ($localPart.ToLowerInvariant() -replace '[^a-z0-9\-]', '-')
    if ([string]::IsNullOrWhiteSpace($safeUsername)) { $safeUsername = 'unknown' }

    if ($tokenMode -eq 'split') {
        $tokenValue = 0.0
        $inVal = 0.0; $outVal = 0.0
        [void][double]::TryParse($row.$tokenColInput, [ref]$inVal)
        [void][double]::TryParse($row.$tokenColOutput, [ref]$outVal)
        $tokenValue = $inVal + $outVal
    }
    else {
        $tokenValue = 0.0
        [void][double]::TryParse($row.$tokenColSingle, [ref]$tokenValue)
    }

    $localCsvPath = Join-Path $LogsDir "$safeUsername-$monthTag.csv"
    if (-not (Test-Path $localCsvPath)) {
        $unmatchedRows.Add([pscustomobject]@{
            console_user  = $rawUser
            console_date  = $dateStr
            console_tokens = $tokenValue
            reason        = "khong tim thay $localCsvPath"
        })
        continue
    }

    $localRows = Import-Csv -LiteralPath $localCsvPath
    $dateRows = $localRows | Where-Object {
        [datetime]$d = [datetime]::MinValue
        [DateTime]::TryParse($_.timestamp_utc, [ref]$d) -and $d.ToString('yyyy-MM-dd') -eq $dateStr
    }

    if (-not $dateRows -or $dateRows.Count -eq 0) {
        $unmatchedRows.Add([pscustomobject]@{
            console_user  = $rawUser
            console_date  = $dateStr
            console_tokens = $tokenValue
            reason        = "co file $localCsvPath nhung khong co dong nao ngay $dateStr"
        })
        continue
    }

    $target = $dateRows | Where-Object { $_.token_source -eq 'console_export' } | Select-Object -First 1
    if (-not $target) {
        [datetime]$earliest = [datetime]::MaxValue
        foreach ($dr in $dateRows) {
            [datetime]$d = [datetime]::MinValue
            if ([DateTime]::TryParse($dr.timestamp_utc, [ref]$d) -and $d -lt $earliest) {
                $earliest = $d
                $target = $dr
            }
        }
    }

    $target.token_source = 'console_export'
    $target.real_tokens = [string][math]::Round($tokenValue, 0)
    $matchedCount++

    $localRows | Export-Csv -LiteralPath $localCsvPath -NoTypeInformation -Encoding UTF8
}

if ($unmatchedRows.Count -gt 0) {
    $unmatchedPath = Join-Path $LogsDir '_unmatched-console-rows.csv'
    $unmatchedRows | Export-Csv -LiteralPath $unmatchedPath -NoTypeInformation -Encoding UTF8 -Append:(Test-Path $unmatchedPath)
}

Write-Host "Hoan tat. Khop: $matchedCount dong. Khong khop: $($unmatchedRows.Count) dong."
if ($unmatchedRows.Count -gt 0) {
    Write-Host "Xem chi tiet cac dong khong khop tai: $(Join-Path $LogsDir '_unmatched-console-rows.csv')"
}
