#Requires -Version 5.1
<#
.SYNOPSIS
  Build the 0x Client: Orion/x64 game stack minus svc licence bots, plus our ox plugins.
#>
param(
    [string]$SourceJar = "C:\Users\0x\Desktop\Orion x64\client_patched.jar",
    [string]$IncognitoJar = "C:\Users\0x\Desktop\Orion x64\incognito-agent.jar"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Dist = Join-Path $Root "dist"
$Work = Join-Path $Root "build\work"
$Classes = Join-Path $Root "build\classes"
$Src = Join-Path $Root "src\main\java"
$OutJar = Join-Path $Dist "0x-client.jar"

function Find-Java {
    $candidates = @(
        (Join-Path $env:LOCALAPPDATA "Orion\jre\bin\javac.exe"),
        "C:\Program Files\AdoptOpenJDK\jdk-11.0.8.10-hotspot\bin\javac.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-11.0.27.6-hotspot\bin\javac.exe"
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { return $c }
    }
    $cmd = Get-Command javac -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    throw "javac not found. Install JDK 11+."
}

if (-not (Test-Path $SourceJar)) {
    throw "Source jar not found: $SourceJar"
}

Write-Host "==> Cleaning build dirs"
Remove-Item $Work, $Classes -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $Work, $Classes, $Dist | Out-Null

Write-Host "==> Copying base client"
Copy-Item $SourceJar (Join-Path $Work "base.jar")

Write-Host "==> Expanding jar"
Push-Location $Work
jar xf base.jar
Pop-Location

Write-Host "==> Removing x64 svc package (licence + their bots)"
$svcDir = Join-Path $Work "net\runelite\client\plugins\svc"
if (Test-Path $svcDir) {
    Remove-Item $svcDir -Recurse -Force
}

# Drop vendor META that might point at OrionLauncher — we set Main-Class ourselves
$manifest = Join-Path $Work "META-INF\MANIFEST.MF"
if (Test-Path $manifest) {
    # Keep a minimal manifest; jar tool will rewrite Main-Class
    Set-Content -Path $manifest -Value "Manifest-Version: 1.0`r`n" -Encoding ASCII
}

Write-Host "==> Compiling 0x plugins against full source jar (APIs)"
$javac = Find-Java
$javaFiles = Get-ChildItem $Src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if (-not $javaFiles) { throw "No Java sources under $Src" }

$cp = $SourceJar
& $javac -encoding UTF-8 -cp $cp -d $Classes @javaFiles
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

Write-Host "==> Merging compiled ox classes into work tree"
Copy-Item (Join-Path $Classes "*") $Work -Recurse -Force

$logbackSrc = Join-Path $Root "src\main\resources\logback.xml"
if (Test-Path $logbackSrc) {
    Write-Host "==> Installing quiet logback.xml"
    Copy-Item $logbackSrc (Join-Path $Work "logback.xml") -Force
}

Write-Host "==> Branding window title Orion -> 0xOrion"
$propsPath = Join-Path $Work "net\runelite\client\runelite.properties"
if (Test-Path $propsPath) {
    $props = Get-Content $propsPath -Raw
    $props = $props -replace '(?m)^runelite\.title=Orion\s*$', 'runelite.title=0xOrion'
    Set-Content -Path $propsPath -Value $props -NoNewline -Encoding ASCII
    Write-Host "    runelite.properties title patched"
} else {
    Write-Host "    WARNING: runelite.properties not found"
}

# Splash window title is hardcoded in SplashScreen.class — same-length patch only
$splashClass = Join-Path $Work "net\runelite\client\ui\SplashScreen.class"
if (Test-Path $splashClass) {
    $bytes = [IO.File]::ReadAllBytes($splashClass)
    $old = [Text.Encoding]::ASCII.GetBytes("Orion Launcher")   # 14
    $new = [Text.Encoding]::ASCII.GetBytes("0xOrion Client")  # 14
    $hits = 0
    for ($i = 0; $i -le $bytes.Length - $old.Length; $i++) {
        $match = $true
        for ($j = 0; $j -lt $old.Length; $j++) {
            if ($bytes[$i + $j] -ne $old[$j]) { $match = $false; break }
        }
        if ($match) {
            for ($j = 0; $j -lt $new.Length; $j++) { $bytes[$i + $j] = $new[$j] }
            $hits++
        }
    }
    if ($hits -gt 0) {
        [IO.File]::WriteAllBytes($splashClass, $bytes)
        Write-Host "    SplashScreen.class patches: $hits"
    }
}

Write-Host "==> Patching config dir .orion -> .oxrl. (same length, avoids clashing with vendor client)"
$rlClass = Join-Path $Work "net\runelite\client\RuneLite.class"
if (Test-Path $rlClass) {
    $bytes = [IO.File]::ReadAllBytes($rlClass)
    $old = [Text.Encoding]::ASCII.GetBytes(".orion")
    $new = [Text.Encoding]::ASCII.GetBytes(".oxrl.")
    $hits = 0
    for ($i = 0; $i -le $bytes.Length - $old.Length; $i++) {
        $match = $true
        for ($j = 0; $j -lt $old.Length; $j++) {
            if ($bytes[$i + $j] -ne $old[$j]) { $match = $false; break }
        }
        if ($match) {
            for ($j = 0; $j -lt $new.Length; $j++) { $bytes[$i + $j] = $new[$j] }
            $hits++
        }
    }
    [IO.File]::WriteAllBytes($rlClass, $bytes)
    Write-Host "    RuneLite.class patches: $hits"
}

Write-Host "==> Packing $OutJar"
$mf = @"
Manifest-Version: 1.0
Main-Class: net.runelite.client.plugins.ox.OxLauncher

"@
$mfPath = Join-Path $Work "MANIFEST.MF"
Set-Content -Path $mfPath -Value $mf -Encoding ASCII
$stagingJar = Join-Path $Dist ("0x-client-build-{0}.jar" -f [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())
Push-Location $Work
jar cfm $stagingJar $mfPath .
Pop-Location
try {
    if (Test-Path $OutJar) { Remove-Item $OutJar -Force -ErrorAction Stop }
    Move-Item $stagingJar $OutJar -Force
} catch {
    $fallback = Join-Path $Dist "0x-client-next.jar"
    try {
        if (Test-Path $fallback) { Remove-Item $fallback -Force -ErrorAction Stop }
        Move-Item $stagingJar $fallback -Force
        Write-Host "WARNING: $OutJar is locked (client running?). Left build at:"
        Write-Host "  $fallback"
        Write-Host "Close the client, then: move /Y dist\0x-client-next.jar dist\0x-client.jar"
        $OutJar = $fallback
    } catch {
        Write-Host "WARNING: could not replace locked jars. Build left at:"
        Write-Host "  $stagingJar"
        Write-Host "Close the client and rename that file to 0x-client.jar"
        $OutJar = $stagingJar
    }
}

Write-Host "==> Building 0x MAC / identity agent"
$agentBuild = Join-Path $Root "tools\ox-agent\build-agent.ps1"
if (Test-Path $agentBuild) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File $agentBuild
    if ($LASTEXITCODE -ne 0) { throw "ox-incognito-agent build failed" }
} else {
    Write-Host "WARNING: $agentBuild missing"
}

# Optional vendor agent kept for reference only — launcher uses ox-incognito-agent.jar
if (Test-Path $IncognitoJar) {
    Copy-Item $IncognitoJar (Join-Path $Dist "incognito-agent.jar") -Force
}

Copy-Item (Join-Path $Root "Launch-0x.bat") $Dist -Force -ErrorAction SilentlyContinue
Copy-Item (Join-Path $Root "tools\account-manager\Launch-AccountManager.bat") $Dist -Force -ErrorAction SilentlyContinue

$svcGone = -not (jar tf $OutJar | Select-String '^net/runelite/client/plugins/svc/' | Select-Object -First 1)
$oxPresent = [bool](jar tf $OutJar | Select-String '^net/runelite/client/plugins/ox/woodcutting/WoodcuttingPlugin.class')
$thieve = [bool](jar tf $OutJar | Select-String '^net/runelite/client/plugins/ox/thieving/ThievingPlugin.class')
$launcher = [bool](jar tf $OutJar | Select-String '^net/runelite/client/plugins/ox/OxLauncher.class')
$agentOk = Test-Path (Join-Path $Dist "ox-incognito-agent.jar")

Write-Host ""
Write-Host "Build complete: $OutJar"
Write-Host ("  size       : {0:N0} bytes" -f (Get-Item $OutJar).Length)
Write-Host "  svc removed: $svcGone"
Write-Host "  OxLauncher : $launcher"
Write-Host "  0xWCBot    : $oxPresent"
Write-Host "  0xThieve   : $thieve"
Write-Host "  MAC agent  : $agentOk"
Write-Host '  config dir : %USERPROFILE%\.oxrl.'
Write-Host ""
Write-Host 'Accounts : tools\account-manager\Launch-AccountManager.bat'
Write-Host 'Run      : Launch-0x.bat  (or dist\Launch-0x.bat)'
