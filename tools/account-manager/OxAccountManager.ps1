#Requires -Version 5.1
# 0x Account Manager - manage accounts before launch (creds + proxy + MAC identity).
# Same role as x64's account-manager.ps1; accounts live under %USERPROFILE%\.oxrl.\accounts\
# Run: Add-Account.bat   or   powershell -STA -ExecutionPolicy Bypass -File this.ps1
param([switch]$NoShow)

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
[System.Windows.Forms.Application]::EnableVisualStyles()

. "$PSScriptRoot\..\identity\ox-identity.ps1"
Ensure-OxDirs

# ── colours ────────────────────────────────────────────────────────────────────
$Purple   = [System.Drawing.Color]::FromArgb(180, 77, 255)
$Bg       = [System.Drawing.Color]::FromArgb(24, 24, 30)
$Panel    = [System.Drawing.Color]::FromArgb(34, 34, 42)
$FgColor  = [System.Drawing.Color]::FromArgb(235, 235, 245)
$Muted    = [System.Drawing.Color]::FromArgb(145, 145, 160)
$Ok       = [System.Drawing.Color]::FromArgb(80, 190, 120)
$Warn     = [System.Drawing.Color]::FromArgb(220, 90, 90)

function Parse-ProxyRaw([string]$raw) {
    $p = @{ host = ""; port = ""; user = ""; pass = "" }
    if (-not $raw) { return $p }
    $parts = $raw.Split(":")
    if ($parts.Count -ge 1) { $p.host = $parts[0] }
    if ($parts.Count -ge 2) { $p.port = $parts[1] }
    if ($parts.Count -ge 4) { $p.user = $parts[2]; $p.pass = $parts[3] }
    return $p
}

function Build-ProxyRaw($proxyHost, $proxyPort, $proxyUser, $proxyPass) {
    $h = ([string]$proxyHost).Trim(); $po = ([string]$proxyPort).Trim()
    $u = ([string]$proxyUser).Trim(); $pw = ([string]$proxyPass).Trim()
    if ([string]::IsNullOrWhiteSpace($h)) { return "" }
    if ($u -and $pw) { return "${h}:${po}:${u}:${pw}" }
    return "${h}:${po}"
}

function Test-OxClientRunning {
    try {
        foreach ($p in (Get-Process java -ErrorAction SilentlyContinue)) {
            $cmd = $null
            try { $cmd = (Get-CimInstance Win32_Process -Filter "ProcessId=$($p.Id)" -ErrorAction SilentlyContinue).CommandLine } catch {}
            if ($cmd -and ($cmd -match '0x-client|OxLauncher|ox\.OxLauncher')) { return $true }
        }
    } catch {}
    return $false
}

# ═══════════════════════════════════════════════════════════════════════════════
# Form
# ═══════════════════════════════════════════════════════════════════════════════
$form = New-Object System.Windows.Forms.Form
$form.Text = "0x Account Manager"
$form.Size = New-Object System.Drawing.Size(920, 660)
$form.StartPosition = "CenterScreen"
$form.FormBorderStyle = "FixedDialog"
$form.MaximizeBox = $false
$form.BackColor = $Bg
$form.ForeColor = $FgColor
$form.Font = New-Object System.Drawing.Font("Segoe UI", 9.5)

function Style-TextBox($t) {
    $t.BackColor = $Panel
    $t.ForeColor = $FgColor
    $t.BorderStyle = "FixedSingle"
}

function New-Label($text, $x, $y, $w = 120, $h = 20) {
    $l = New-Object System.Windows.Forms.Label
    $l.Text = $text
    $l.Location = New-Object System.Drawing.Point($x, $y)
    $l.Size = New-Object System.Drawing.Size($w, $h)
    $l.ForeColor = $Muted
    $form.Controls.Add($l)
    return $l
}

function New-Text($x, $y, $w = 200) {
    $t = New-Object System.Windows.Forms.TextBox
    $t.Location = New-Object System.Drawing.Point($x, $y)
    $t.Size = New-Object System.Drawing.Size($w, 24)
    Style-TextBox $t
    $form.Controls.Add($t)
    return $t
}

function New-GLabel($parent, $text, $x, $y, $w = 70) {
    $l = New-Object System.Windows.Forms.Label
    $l.Text = $text
    $l.Location = New-Object System.Drawing.Point($x, $y)
    $l.Size = New-Object System.Drawing.Size($w, 20)
    $l.ForeColor = $Muted
    $parent.Controls.Add($l)
    return $l
}

function New-GText($parent, $x, $y, $w) {
    $t = New-Object System.Windows.Forms.TextBox
    $t.Location = New-Object System.Drawing.Point($x, $y)
    $t.Size = New-Object System.Drawing.Size($w, 22)
    Style-TextBox $t
    $parent.Controls.Add($t)
    return $t
}

function New-PurpleBtn($text, $x, $y, $w, $h = 34) {
    $b = New-Object System.Windows.Forms.Button
    $b.Text = $text
    $b.Location = New-Object System.Drawing.Point($x, $y)
    $b.Size = New-Object System.Drawing.Size($w, $h)
    $b.FlatStyle = "Flat"
    $b.BackColor = $Panel
    $b.ForeColor = $FgColor
    $b.FlatAppearance.BorderColor = $Purple
    $b.Cursor = [System.Windows.Forms.Cursors]::Hand
    return $b
}

# Header
$hdr = New-Object System.Windows.Forms.Label
$hdr.Text = "0x Account Manager"
$hdr.Font = New-Object System.Drawing.Font("Segoe UI Semibold", 14)
$hdr.ForeColor = $Purple
$hdr.Location = New-Object System.Drawing.Point(12, 10)
$hdr.AutoSize = $true
$form.Controls.Add($hdr)

$sub = New-Object System.Windows.Forms.Label
$sub.Text = "Add accounts here first. Then Launch-0x.bat and pick a number. Each account = own MAC + proxy."
$sub.ForeColor = $Muted
$sub.Location = New-Object System.Drawing.Point(230, 16)
$sub.Size = New-Object System.Drawing.Size(660, 20)
$form.Controls.Add($sub)

# Left: account list
New-Label "Accounts:" 12 48 200 | Out-Null
$lst = New-Object System.Windows.Forms.ListBox
$lst.Location = New-Object System.Drawing.Point(12, 70)
$lst.Size = New-Object System.Drawing.Size(320, 480)
$lst.Font = New-Object System.Drawing.Font("Consolas", 9)
$lst.BackColor = $Panel
$lst.ForeColor = $FgColor
$lst.BorderStyle = "FixedSingle"
$form.Controls.Add($lst)

# Right: editor
$rx = 350
$lblTitle = New-Object System.Windows.Forms.Label
$lblTitle.Location = New-Object System.Drawing.Point($rx, 48)
$lblTitle.Size = New-Object System.Drawing.Size(540, 24)
$lblTitle.Font = New-Object System.Drawing.Font("Segoe UI", 11, [System.Drawing.FontStyle]::Bold)
$lblTitle.ForeColor = $FgColor
$lblTitle.Text = "New account"
$form.Controls.Add($lblTitle)

New-Label "Display name:" $rx 88 100 | Out-Null
$txtName = New-Text ($rx + 105) 86 300

New-Label "Username:" $rx 122 100 | Out-Null
$txtUser = New-Text ($rx + 105) 120 300

New-Label "Password:" $rx 156 100 | Out-Null
$txtPass = New-Text ($rx + 105) 154 300
$txtPass.UseSystemPasswordChar = $true
$chkShow = New-Object System.Windows.Forms.CheckBox
$chkShow.Text = "show"
$chkShow.Location = New-Object System.Drawing.Point(($rx + 415), 154)
$chkShow.Size = New-Object System.Drawing.Size(70, 24)
$chkShow.ForeColor = $Muted
$chkShow.FlatStyle = "Flat"
$form.Controls.Add($chkShow)

# Proxy group
$grpProxy = New-Object System.Windows.Forms.GroupBox
$grpProxy.Text = "Proxy  (leave Host blank for DIRECT)"
$grpProxy.Location = New-Object System.Drawing.Point($rx, 195)
$grpProxy.Size = New-Object System.Drawing.Size(540, 120)
$grpProxy.ForeColor = $Purple
$grpProxy.BackColor = $Bg
$form.Controls.Add($grpProxy)

New-GLabel $grpProxy "Host / IP:" 12 28 | Out-Null
$txtHost = New-GText $grpProxy 90 26 210
New-GLabel $grpProxy "Port:" 315 28 40 | Out-Null
$txtPort = New-GText $grpProxy 360 26 80
New-GLabel $grpProxy "User:" 12 62 | Out-Null
$txtPUser = New-GText $grpProxy 90 60 210
New-GLabel $grpProxy "Pass:" 315 62 40 | Out-Null
$txtPPass = New-GText $grpProxy 360 60 150
$txtPPass.UseSystemPasswordChar = $true
New-GLabel $grpProxy "host:port or host:port:user:pass - one proxy per account" 12 92 500 | Out-Null

# Identity group
$grpId = New-Object System.Windows.Forms.GroupBox
$grpId.Text = "Device identity (frozen MAC / HWID - auto-created on save)"
$grpId.Location = New-Object System.Drawing.Point($rx, 325)
$grpId.Size = New-Object System.Drawing.Size(540, 175)
$grpId.ForeColor = $Purple
$grpId.BackColor = $Bg
$form.Controls.Add($grpId)

$lblId = New-Object System.Windows.Forms.Label
$lblId.Location = New-Object System.Drawing.Point(12, 24)
$lblId.Size = New-Object System.Drawing.Size(510, 100)
$lblId.Font = New-Object System.Drawing.Font("Consolas", 9)
$lblId.ForeColor = $FgColor
$grpId.Controls.Add($lblId)

New-GLabel $grpId "Timezone:" 12 130 70 | Out-Null
$cmbTz = New-Object System.Windows.Forms.ComboBox
$cmbTz.Location = New-Object System.Drawing.Point(90, 128)
$cmbTz.Size = New-Object System.Drawing.Size(220, 22)
$cmbTz.DropDownStyle = "DropDownList"
$cmbTz.BackColor = $Panel
$cmbTz.ForeColor = $FgColor
$cmbTz.FlatStyle = "Flat"
foreach ($tz in $script:Timezones) { [void]$cmbTz.Items.Add($tz) }
if ($cmbTz.Items.Count -gt 0) { $cmbTz.SelectedIndex = 0 }
$grpId.Controls.Add($cmbTz)

$btnRegen = New-PurpleBtn "Regenerate identity" 330 126 180 28
$grpId.Controls.Add($btnRegen)

# Buttons
$btnNew    = New-PurpleBtn "New account"      $rx 515 120
$btnSave   = New-PurpleBtn "Save account"     ($rx + 130) 515 140
$btnSave.Font = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)
$btnDelete = New-PurpleBtn "Delete account"   ($rx + 280) 515 140
$btnClose  = New-PurpleBtn "Close"            ($rx + 430) 515 110
$form.Controls.AddRange(@($btnNew, $btnSave, $btnDelete, $btnClose))

$lblStatus = New-Object System.Windows.Forms.Label
$lblStatus.Location = New-Object System.Drawing.Point(12, 565)
$lblStatus.Size = New-Object System.Drawing.Size(880, 40)
$lblStatus.Font = New-Object System.Drawing.Font("Segoe UI", 9, [System.Drawing.FontStyle]::Bold)
$form.Controls.Add($lblStatus)

# ── state ──────────────────────────────────────────────────────────────────────
$script:CurrentId = $null
$script:CurrentIdentity = $null
$script:AccountOrder = @()   # ids in list order (for SelectedIndex)

function Update-RunningStatus {
    if (Test-OxClientRunning) {
        $lblStatus.ForeColor = $Warn
        $lblStatus.Text = "WARNING: 0x Client looks RUNNING. Close it before saving if you edit shared settings."
    } else {
        $lblStatus.ForeColor = $Ok
        $lblStatus.Text = "Safe to edit.  Store: $script:OxAccountsDir"
    }
}

function Show-IdentityObj($id) {
    if (-not $id -or -not $id.mac) {
        $lblId.Text = "(no identity yet - a real-vendor MAC + HWID will be minted when you save)"
        return
    }
    $lblId.Text = ("MAC      : {0}`r`nHWID     : {1}`r`nOS       : {2} {3}`r`nHostname : {4}`r`nWin user : {5}`r`nJava     : {6} ({7})" -f `
        $id.mac, $id.machineId, $id.osName, $id.osVersion, $id.hostname, $id.username, $id.javaVersion, $id.javaVendor)
    if ($id.timezone -and $cmbTz.Items.Contains([string]$id.timezone)) {
        $cmbTz.SelectedItem = [string]$id.timezone
    }
}

function Get-AccountSummary($a, [int]$n) {
    $u = if ($a.username) { $a.username } else { "(no user)" }
    $px = if ([string]::IsNullOrWhiteSpace($a.proxy)) { "DIRECT" } else { ($a.proxy -split ":")[0] }
    $mac = if ($a.identity.mac) { $a.identity.mac } else { "no-mac" }
    $name = if ($a.displayName) { $a.displayName } else { "acct" }
    return ("{0,2}.  {1,-14}  user={2,-12}  [{3}]  {4}" -f $n, $name, $u, $px, $mac)
}

function Refresh-List {
    $keepId = $script:CurrentId
    $lst.BeginUpdate()
    $lst.Items.Clear()
    $script:AccountOrder = @()
    $n = 1
    foreach ($a in (Get-OxAccounts)) {
        [void]$lst.Items.Add((Get-AccountSummary $a $n))
        $script:AccountOrder += $a.id
        $n++
    }
    $lst.EndUpdate()
    if ($keepId) {
        $idx = [array]::IndexOf([string[]]$script:AccountOrder, [string]$keepId)
        if ($idx -ge 0) { $lst.SelectedIndex = $idx }
    }
}

function Clear-Editor {
    $script:CurrentId = $null
    $script:CurrentIdentity = New-OxIdentity
    $lblTitle.Text = "New account"
    $txtName.Text = ""
    $txtUser.Text = ""
    $txtPass.Text = ""
    $txtHost.Text = ""
    $txtPort.Text = ""
    $txtPUser.Text = ""
    $txtPPass.Text = ""
    Show-IdentityObj $script:CurrentIdentity
    Update-RunningStatus
}

function Load-AccountById([string]$id) {
    $a = Get-OxAccount $id
    if (-not $a) { return }
    $script:CurrentId = $a.id
    $script:CurrentIdentity = $a.identity
    $lblTitle.Text = "Edit: $($a.displayName)"
    $txtName.Text = [string]$a.displayName
    $txtUser.Text = [string]$a.username
    $txtPass.Text = [string]$a.password
    $pp = Parse-ProxyRaw ([string]$a.proxy)
    $txtHost.Text = $pp.host
    $txtPort.Text = $pp.port
    $txtPUser.Text = $pp.user
    $txtPPass.Text = $pp.pass
    Show-IdentityObj $script:CurrentIdentity
    Update-RunningStatus
}

$lst.Add_SelectedIndexChanged({
    if ($lst.SelectedIndex -ge 0 -and $lst.SelectedIndex -lt $script:AccountOrder.Count) {
        Load-AccountById ([string]$script:AccountOrder[$lst.SelectedIndex])
    }
})

$chkShow.Add_CheckedChanged({
    $txtPass.UseSystemPasswordChar = -not $chkShow.Checked
})

$btnRegen.Add_Click({
    $script:CurrentIdentity = New-OxIdentity
    if ($cmbTz.SelectedItem) { $script:CurrentIdentity.timezone = [string]$cmbTz.SelectedItem }
    Show-IdentityObj $script:CurrentIdentity
    $lblStatus.ForeColor = $Ok
    $lblStatus.Text = "New identity minted (not saved yet - click Save account)."
})

$btnNew.Add_Click({
    $lst.ClearSelected()
    Clear-Editor
})

$btnSave.Add_Click({
    $name = $txtName.Text.Trim()
    $user = $txtUser.Text.Trim()
    $pw   = $txtPass.Text
    $h    = $txtHost.Text.Trim()
    $port = $txtPort.Text.Trim()
    $pu   = $txtPUser.Text.Trim()
    $ppw  = $txtPPass.Text.Trim()

    if ([string]::IsNullOrWhiteSpace($name)) {
        [System.Windows.Forms.MessageBox]::Show("Display name is required.", "0x Account Manager") | Out-Null
        return
    }
    if ([string]::IsNullOrWhiteSpace($user)) {
        [System.Windows.Forms.MessageBox]::Show("Username is required.", "0x Account Manager") | Out-Null
        return
    }
    if ([string]::IsNullOrWhiteSpace($pw)) {
        [System.Windows.Forms.MessageBox]::Show("Password is required.", "0x Account Manager") | Out-Null
        return
    }
    if (-not [string]::IsNullOrWhiteSpace($h)) {
        if ($port -notmatch '^\d+$' -or [int]$port -lt 1 -or [int]$port -gt 65535) {
            [System.Windows.Forms.MessageBox]::Show("Port must be a number 1-65535.", "Bad port") | Out-Null
            return
        }
        if ($h -match ':') {
            [System.Windows.Forms.MessageBox]::Show("Host must not contain ':' - put the port in the Port box.", "Bad host") | Out-Null
            return
        }
        if (($pu -match ':') -or ($ppw -match ':')) {
            [System.Windows.Forms.MessageBox]::Show("Proxy user/pass must not contain ':'.", "Bad proxy credentials") | Out-Null
            return
        }
    }

    if (Test-OxClientRunning) {
        $r = [System.Windows.Forms.MessageBox]::Show(
            "0x Client looks like it is running.`r`n`r`nSave anyway?",
            "Client may be running",
            [System.Windows.Forms.MessageBoxButtons]::YesNo,
            [System.Windows.Forms.MessageBoxIcon]::Warning)
        if ($r -ne [System.Windows.Forms.DialogResult]::Yes) { return }
    }

    if (-not $script:CurrentIdentity -or -not $script:CurrentIdentity.mac) {
        $script:CurrentIdentity = New-OxIdentity
    }
    if ($cmbTz.SelectedItem) {
        $tz = [string]$cmbTz.SelectedItem
        $cur = $script:CurrentIdentity
        if ($cur -is [hashtable] -or $cur -is [System.Collections.IDictionary]) {
            $cur["timezone"] = $tz
        } else {
            $id = [ordered]@{}
            foreach ($prop in $cur.PSObject.Properties) { $id[$prop.Name] = $prop.Value }
            $id["timezone"] = $tz
            $script:CurrentIdentity = $id
        }
    }

    $proxy = Build-ProxyRaw $h $port $pu $ppw

    if (-not $script:CurrentId) {
        $acc = New-OxAccountObject -DisplayName $name -Username $user -Password $pw -Proxy $proxy
        $acc.identity = $script:CurrentIdentity
        Save-OxAccount $acc | Out-Null
        $script:CurrentId = $acc.id
    } else {
        $acc = Get-OxAccount $script:CurrentId
        if (-not $acc) {
            $acc = New-OxAccountObject -DisplayName $name -Username $user -Password $pw -Proxy $proxy
            $acc.id = $script:CurrentId
        }
        $acc.id = $script:CurrentId
        $acc.displayName = $name
        $acc.username = $user
        $acc.password = $pw
        $acc.proxy = $proxy
        $acc.identity = $script:CurrentIdentity
        $acc.enabled = $true
        Save-OxAccount $acc | Out-Null
    }

    Refresh-List
    Load-AccountById $script:CurrentId
    $lblStatus.ForeColor = $Ok
    $pxTag = if ($proxy) { ($proxy -split ":")[0] } else { "DIRECT" }
    $lblStatus.Text = "Saved '$name' ($user)  proxy=$pxTag  mac=$($script:CurrentIdentity.mac).  Use Launch-0x.bat to pick this account."
})

$btnDelete.Add_Click({
    if (-not $script:CurrentId) {
        [System.Windows.Forms.MessageBox]::Show("Select an account first (or nothing to delete for a new draft).", "0x") | Out-Null
        return
    }
    $r = [System.Windows.Forms.MessageBox]::Show(
        "Delete this account, its MAC identity, and cache folder?`r`n`r`nThis cannot be undone.",
        "Confirm delete",
        [System.Windows.Forms.MessageBoxButtons]::YesNo,
        [System.Windows.Forms.MessageBoxIcon]::Warning)
    if ($r -ne [System.Windows.Forms.DialogResult]::Yes) { return }
    Remove-OxAccount $script:CurrentId
    Clear-Editor
    Refresh-List
    $lblStatus.ForeColor = [System.Drawing.Color]::DarkOrange
    $lblStatus.Text = "Account deleted."
})

$btnClose.Add_Click({ $form.Close() })

Refresh-List
Clear-Editor
if ($lst.Items.Count -gt 0) { $lst.SelectedIndex = 0 }

if (-not $NoShow) { [void]$form.ShowDialog() }
