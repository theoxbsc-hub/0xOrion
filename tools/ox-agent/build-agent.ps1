#Requires -Version 5.1
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Src = Join-Path $Root "src"
$Lib = Join-Path $Root "lib\asm-9.6.jar"
$Out = Join-Path $Root "out"
$Dist = Join-Path (Split-Path (Split-Path $Root)) "dist"
$AgentJar = Join-Path $Dist "ox-incognito-agent.jar"

if (-not (Test-Path $Lib)) { throw "Missing $Lib" }

Remove-Item $Out -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $Out, $Dist | Out-Null

$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
if (-not $javac) {
  $cands = @(
    "C:\Program Files\AdoptOpenJDK\jdk-11.0.8.10-hotspot\bin\javac.exe",
    "$env:LOCALAPPDATA\Orion\jre\..\..\.."
  )
  foreach ($j in @("C:\Program Files\AdoptOpenJDK\jdk-11.0.8.10-hotspot\bin\javac.exe")) {
    if (Test-Path $j) { $javac = $j; break }
  }
}
if (-not $javac) { throw "javac not found" }

$files = Get-ChildItem $Src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& $javac -encoding UTF-8 -cp $Lib -d $Out @files
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

$mf = @"
Manifest-Version: 1.0
Premain-Class: ox.incognito.OxIncognitoAgent
Can-Retransform-Classes: true
Can-Redefine-Classes: true
Boot-Class-Path: ox-incognito-agent.jar

"@
$mfPath = Join-Path $Out "MANIFEST.MF"
Set-Content $mfPath $mf -Encoding ASCII

# Stage classes + shade ASM into the agent jar
$stage = Join-Path $Root "stage"
Remove-Item $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $stage | Out-Null
Copy-Item (Join-Path $Out "*") $stage -Recurse -Force
Push-Location $stage
jar xf $Lib
Remove-Item META-INF\MANIFEST.MF -Force -ErrorAction SilentlyContinue
Remove-Item META-INF\maven -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item $mfPath .\MANIFEST.MF -Force
if (Test-Path $AgentJar) {
  try { Remove-Item $AgentJar -Force -ErrorAction Stop } catch {
    $AgentJar = Join-Path $Dist "ox-incognito-agent-next.jar"
  }
}
jar cfm $AgentJar MANIFEST.MF .
Pop-Location

Write-Host "Built $AgentJar ($((Get-Item $AgentJar).Length) bytes)"
