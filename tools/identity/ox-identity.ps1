# Ox identity library — generate / load per-account fingerprints for MAC spoofing.
# Dot-source: . "$PSScriptRoot\ox-identity.ps1"

$script:OxHome = Join-Path $env:USERPROFILE ".oxrl."
$script:OxAccountsDir = Join-Path $script:OxHome "accounts"
$script:OxCacheRoot = Join-Path $script:OxHome "account-cache"
$script:OxIncognitoDir = Join-Path $script:OxHome "incognito"

$script:VendorOuis = @(
    "00:1B:21","00:1F:3C","3C:97:0E","8C:A9:82",
    "00:E0:4C","1C:BF:CE",
    "00:14:22","18:03:73","B8:CA:3A","F8:BC:12",
    "00:1A:92","2C:56:DC","50:46:5D","04:D4:C4",
    "00:1F:29","3C:D9:2B","70:5A:0F","98:E7:F4",
    "50:C7:BF","14:CC:20","A4:2B:B0","98:DA:C4"
)
$script:FirstNames = @(
    "James","Robert","John","Michael","David","William","Richard","Joseph",
    "Thomas","Daniel","Matthew","Anthony","Mark","Steven","Andrew","Joshua",
    "Kenneth","Kevin","Brian","George","Timothy","Ronald","Edward","Jason"
)
$script:HostPrefixes = @("DESKTOP","LAPTOP","WORKSTATION","HOME-PC","WIN-PC","MY-PC")
$script:Timezones = @(
    "America/New_York","America/Chicago","America/Denver","America/Los_Angeles",
    "America/Phoenix","America/Detroit","America/Boise"
)
$script:Win10 = @("10.0.19041","10.0.19042","10.0.19043","10.0.19044","10.0.19045")
$script:Win11 = @("10.0.22000","10.0.22621","10.0.22631","10.0.26100")
$script:JavaVersions = @("11.0.20","11.0.21","11.0.22","11.0.23","11.0.24","11.0.25","11.0.26","11.0.27")
$script:JavaVendors = @("Eclipse Adoptium","Eclipse Foundation","Amazon.com Inc.","BellSoft","Microsoft")
$script:MaxMemChoices = @(1024, 1536, 2048, 3072, 4096)
$script:CpuChoices = @(4, 6, 8, 12, 16)

function Ensure-OxDirs {
    foreach ($d in @($script:OxHome, $script:OxAccountsDir, $script:OxCacheRoot, $script:OxIncognitoDir)) {
        if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force -Path $d | Out-Null }
    }
}

function Ox-Pick([object[]]$arr) {
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $buf = New-Object byte[] 4
    $rng.GetBytes($buf)
    $val = [BitConverter]::ToUInt32($buf, 0)
    return $arr[[int]($val % [uint32]$arr.Count)]
}

function New-OxMac {
    $oui = Ox-Pick $script:VendorOuis
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $buf = New-Object byte[] 3
    $rng.GetBytes($buf)
    $tail = ($buf | ForEach-Object { "{0:X2}" -f $_ }) -join ":"
    return "$oui`:$tail"
}

function New-OxMachineId {
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $buf = New-Object byte[] 32
    $rng.GetBytes($buf)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    $hash = $sha.ComputeHash($buf)
    return ($hash[0..15] | ForEach-Object { "{0:x2}" -f $_ }) -join ""
}

function New-OxIdentity {
    $first = Ox-Pick $script:FirstNames
    $num = Get-Random -Minimum 10 -Maximum 99
    $user = ("{0}{1}" -f $first.ToLower(), $num)
    $is11 = (Get-Random -Maximum 2) -eq 1
    $osName = if ($is11) { "Windows 11" } else { "Windows 10" }
    $osVer = if ($is11) { Ox-Pick $script:Win11 } else { Ox-Pick $script:Win10 }
    $realCpu = [Environment]::ProcessorCount
    $cpuPool = @($script:CpuChoices | Where-Object { $_ -le $realCpu })
    if (-not $cpuPool -or $cpuPool.Count -eq 0) { $cpuPool = @([Math]::Max(2, $realCpu)) }
    return [ordered]@{
        mac          = New-OxMac
        machineId    = New-OxMachineId
        username     = $user
        hostname     = "{0}-{1}" -f (Ox-Pick $script:HostPrefixes), (Get-Random -Minimum 100000 -Maximum 999999)
        homePath     = "C:\Users\$user"
        localIp      = "192.168.{0}.{1}" -f (Get-Random -Minimum 0 -Maximum 255), (Get-Random -Minimum 2 -Maximum 254)
        timezone     = Ox-Pick $script:Timezones
        osName       = $osName
        osVersion    = $osVer
        osArch       = "amd64"
        javaVersion  = Ox-Pick $script:JavaVersions
        javaVendor   = Ox-Pick $script:JavaVendors
        maxMemMb     = [int](Ox-Pick $script:MaxMemChoices)
        cpuCount     = [int](Ox-Pick $cpuPool)
        language     = "en"
        country      = "US"
        createdAt    = (Get-Date).ToString("o")
    }
}

function New-OxAccountId {
    return ("a{0}" -f ([guid]::NewGuid().ToString("N").Substring(0, 10)))
}

function Get-OxAccounts {
    Ensure-OxDirs
    $list = @()
    Get-ChildItem $script:OxAccountsDir -Filter *.json -ErrorAction SilentlyContinue | ForEach-Object {
        try {
            $list += (Get-Content $_.FullName -Raw | ConvertFrom-Json)
        } catch {}
    }
    return @($list | Sort-Object displayName)
}

function Save-OxAccount($account) {
    Ensure-OxDirs
    $path = Join-Path $script:OxAccountsDir ("{0}.json" -f $account.id)
    ($account | ConvertTo-Json -Depth 8) | Set-Content -Path $path -Encoding UTF8
    return $path
}

function Remove-OxAccount([string]$id) {
    $path = Join-Path $script:OxAccountsDir ("{0}.json" -f $id)
    if (Test-Path $path) { Remove-Item $path -Force }
    $cache = Join-Path $script:OxCacheRoot $id
    if (Test-Path $cache) { Remove-Item $cache -Recurse -Force -ErrorAction SilentlyContinue }
}

function Get-OxAccount([string]$id) {
    $path = Join-Path $script:OxAccountsDir ("{0}.json" -f $id)
    if (-not (Test-Path $path)) { return $null }
    return (Get-Content $path -Raw | ConvertFrom-Json)
}

function New-OxAccountObject {
    param(
        [string]$DisplayName,
        [string]$Username = "",
        [string]$Password = "",
        [string]$Proxy = ""
    )
    $id = New-OxAccountId
    $ident = New-OxIdentity
    return [pscustomobject]@{
        id          = $id
        displayName = $DisplayName
        username    = $Username
        password    = $Password
        proxy       = $Proxy
        enabled     = $true
        identity    = $ident
        createdAt   = (Get-Date).ToString("o")
    }
}

function Ensure-OxJunction([string]$link, [string]$target) {
    if (-not (Test-Path $target)) { return }
    if (Test-Path $link) { return }
    try {
        New-Item -ItemType Junction -Path $link -Target $target -ErrorAction Stop | Out-Null
        return
    } catch {}
    try { & cmd /c mklink /J "`"$link`"" "`"$target`"" | Out-Null } catch {}
}

function New-OxUid24([string]$path) {
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $buf = New-Object byte[] 24
    $rng.GetBytes($buf)
    $dir = Split-Path $path -Parent
    if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
    [System.IO.File]::WriteAllBytes($path, $buf)
}

function Get-OxUidFile([string]$accountId) {
    $p = Join-Path $script:OxIncognitoDir ("uid-{0}.random.dat" -f $accountId)
    if (-not (Test-Path $p) -or (Get-Item $p).Length -ne 24) { New-OxUid24 $p }
    return $p
}

# unethicalite resolves: ~/.orion/custom-cache/<name>  (Orion.CLIENT_DIR is still .orion)
# Pass ONLY a relative name via -Dunethicalite.cache-dir — never an absolute path.
function Stage-OxCacheIsolation([string]$accountId) {
    $name = "ox-$accountId"
    # Live CLIENT_DIR for Unethicalite/Orion is still ~/.orion (not patched with RuneLite)
    $orionHome = Join-Path $env:USERPROFILE ".orion"
    $sharedCandidates = @(
        (Join-Path $orionHome "jagexcache"),
        (Join-Path $script:OxHome "jagexcache")
    )
    $sharedC = $sharedCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1

    $cdir = Join-Path $orionHome "custom-cache\$name"
    if (-not (Test-Path $cdir)) { New-Item -ItemType Directory -Force -Path $cdir | Out-Null }

    if ($sharedC) {
        Ensure-OxJunction (Join-Path $cdir "jagexcache") (Join-Path $sharedC "jagexcache")
        $oldschool = Join-Path $sharedC "oldschool"
        if (Test-Path $oldschool) { Ensure-OxJunction (Join-Path $cdir "oldschool") $oldschool }
        # Avoid shared random.dat linking accounts when override is missed
        try {
            $sharedRnd = Join-Path $sharedC "random.dat"
            if (Test-Path $sharedRnd) { Remove-Item $sharedRnd -Force -ErrorAction Stop }
        } catch {}
    }

    $uid = [System.IO.File]::ReadAllBytes((Get-OxUidFile $accountId))
    try { [System.IO.File]::WriteAllBytes((Join-Path $cdir "random.dat"), $uid) } catch {}
    return $name
}

function Write-OxJvmArgsFile($account) {
    Ensure-OxDirs
    $id = $account.identity
    $cacheName = Stage-OxCacheIsolation ([string]$account.id)

    $args = @(
        "-Duser.name=$($id.username)",
        "-Duser.timezone=$($id.timezone)",
        "-Duser.language=$($id.language)",
        "-Duser.country=$($id.country)",
        "-Dos.name=$($id.osName)",
        "-Dos.version=10.0",
        "-Dos.arch=$($id.osArch)",
        "-Djava.version=$($id.javaVersion)",
        "-Djava.vendor=$($id.javaVendor)",
        "-Dox.mac=$($id.mac)",
        "-Dox.machineId=$($id.machineId)",
        "-Dox.javaVersion=$($id.javaVersion)",
        "-Dox.javaVendor=$($id.javaVendor)",
        "-Dox.maxMem=$($id.maxMemMb)",
        "-Dox.cpu=$($id.cpuCount)",
        "-Dox.localIp=$($id.localIp)",
        "-Dox.hostname=$($id.hostname)",
        "-Dox.accountId=$($account.id)",
        "-Dox.displayName=$($account.displayName)",
        "-Dox.username=$($account.username)",
        "-Dox.password=$(if ($null -ne $account.password) { [string]$account.password } else { '' })",
        "-Dox.proxy=$(if ($account.proxy) { [string]$account.proxy } else { '' })",
        "-Dox.active=true",
        "-Dox.quiet=true",
        "-Dunethicalite.cache-dir=$cacheName"
    )
    $file = Join-Path $script:OxIncognitoDir "jvm_args.txt"
    # Quote each arg so cmd.exe keeps spaces inside values (e.g. -Dos.name=Windows 10)
    $quoted = $args | ForEach-Object {
        $a = [string]$_
        # Escape embedded quotes for cmd.exe "..." wrapping
        $a = $a.Replace('"', '\"')
        "`"$a`""
    }
    ($quoted -join " ") | Set-Content -Path $file -Encoding ASCII -NoNewline
    return $file
}

function Format-OxProxyArg([string]$proxy) {
    if ([string]::IsNullOrWhiteSpace($proxy)) { return "" }
    return $proxy.Trim()
}
