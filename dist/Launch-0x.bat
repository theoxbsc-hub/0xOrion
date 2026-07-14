@echo off
rem ============================================================
rem   0x Client launcher — pick account (proxy + MAC identity)
rem ============================================================
title 0x Client
setlocal enabledelayedexpansion

set "HERE=%~dp0"
rem Prefer project root (parent of dist) so tools\ always resolves
set "ROOT=%HERE%"
if exist "%HERE%tools\identity\ox-identity.ps1" (
  set "ROOT=%HERE%"
) else if exist "%HERE%..\tools\identity\ox-identity.ps1" (
  set "ROOT=%HERE%..\"
)

rem Prefer the newest 0x-client*.jar (avoids stale *-next.jar winning forever)
set "JAR="
for /f "delims=" %%F in ('powershell -NoProfile -Command ^
  "$cands=@(); foreach($p in @('%HERE%','%HERE%dist\','%ROOT%dist\')){ if(Test-Path $p){ $cands += Get-ChildItem -LiteralPath $p -Filter '0x-client*.jar' -ErrorAction SilentlyContinue } }; if($cands){ ($cands | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName }"') do set "JAR=%%F"

set "AGENT="
for /f "delims=" %%F in ('powershell -NoProfile -Command ^
  "$cands=@(); foreach($p in @('%HERE%','%HERE%dist\','%ROOT%dist\')){ if(Test-Path $p){ $cands += Get-ChildItem -LiteralPath $p -Filter 'ox-incognito-agent*.jar' -ErrorAction SilentlyContinue } }; if($cands){ ($cands | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName }"') do set "AGENT=%%F"

set "IDLIB=%ROOT%tools\identity\ox-identity.ps1"
set "JVMARGS=%USERPROFILE%\.oxrl.\incognito\jvm_args.txt"
set "MAIN=net.runelite.client.plugins.ox.OxLauncher"

set "JAVA="
if exist "%LOCALAPPDATA%\Orion\jre\bin\java.exe" set "JAVA=%LOCALAPPDATA%\Orion\jre\bin\java.exe"
if not defined JAVA set "JAVA=java"

if not defined JAR (
  echo  ERROR: 0x-client.jar not found. Run build.ps1 first.
  pause
  exit /b 1
)
if not defined AGENT (
  echo  ERROR: ox-incognito-agent.jar missing. Run tools\ox-agent\build-agent.ps1
  pause
  exit /b 1
)
if not exist "%IDLIB%" (
  echo  ERROR: identity library missing: %IDLIB%
  pause
  exit /b 1
)

echo.
echo ============================================================
echo    0x Client
echo ============================================================

rem --- Account picker via PowerShell ---
set "ACC_FILE=%TEMP%\ox_selected_account.txt"
del "%ACC_FILE%" >nul 2>&1
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  ". '%IDLIB%'; Ensure-OxDirs; $accs = @(Get-OxAccounts); if ($accs.Count -eq 0) { Write-Host 'No accounts configured.'; exit 2 }; Write-Host ''; Write-Host '  #   Name            User            Proxy        MAC'; Write-Host '  --  --------------  --------------  -----------  -----------------'; for ($i=0; $i -lt $accs.Count; $i++) { $a=$accs[$i]; $px= if ([string]::IsNullOrWhiteSpace($a.proxy)) {'DIRECT'} else {($a.proxy -split ':')[0]}; Write-Host ('  {0,2}) {1,-14}  {2,-14}  {3,-11}  {4}' -f ($i+1), $a.displayName, $a.username, $px, $a.identity.mac) }; Write-Host ''; Write-Host '   0) Cancel'; Write-Host ''; $c = Read-Host '  Launch account #'; if (-not $c -or $c -eq '0') { exit 3 }; $n=0; if (-not [int]::TryParse($c, [ref]$n) -or $n -lt 1 -or $n -gt $accs.Count) { Write-Host '  Invalid choice'; exit 4 }; $pick=$accs[$n-1]; Write-OxJvmArgsFile $pick | Out-Null; $px=Format-OxProxyArg $pick.proxy; $line = $pick.id + '|' + $px + '|' + $pick.displayName; Set-Content -Path '%ACC_FILE%' -Value $line -Encoding ASCII"

if errorlevel 2 (
  echo.
  echo  No accounts yet. Open Add-Account.bat first to create profiles.
  pause
  exit /b 1
)
if not exist "%ACC_FILE%" (
  echo  Cancelled.
  exit /b 0
)

set /p SEL=<"%ACC_FILE%"
for /f "tokens=1,2,3 delims=|" %%A in ("%SEL%") do (
  set "ACC_ID=%%A"
  set "PROXY_RAW=%%B"
  set "ACC_NAME=%%C"
)

set "PROXY_ARG="
if defined PROXY_RAW if not "!PROXY_RAW!"=="" set "PROXY_ARG=--proxy !PROXY_RAW!"

set "INCOGNITO_ARGS="
if exist "%JVMARGS%" set /p INCOGNITO_ARGS=<"%JVMARGS%"

echo.
echo  Launching !ACC_NAME!...
echo.

"%JAVA%" -javaagent:"%AGENT%" !INCOGNITO_ARGS! -XX:+DisableAttachMechanism -Xmx768m -Xss2m -XX:+UseSerialGC -XX:MaxMetaspaceSize=256m -XX:CompressedClassSpaceSize=128m -XX:ReservedCodeCacheSize=128m -XX:MaxDirectMemorySize=256m -XX:ActiveProcessorCount=2 -XX:+ExitOnOutOfMemoryError --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -Drunelite.launcher.version=0x-1.0 -Drunelite.launcher.nojvm=true -cp "%JAR%" %MAIN% %PROXY_ARG%

set "EC=%ERRORLEVEL%"
if not "%EC%"=="0" (
  echo.
  echo  Exited with code %EC%
  pause
)
endlocal
