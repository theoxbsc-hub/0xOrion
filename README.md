# 0x Client (0xOrion)

Personal fork of the Orion x64 RSPS client stack, with **our** plugins under `ox.*` (no `svc` licence bots).

Config home: `~/.oxrl.` (Windows: `%USERPROFILE%\.oxrl.`)

---

## Dual PC workflow (Windows + Mac)

One git repo. Shared live code updates both machines when you **push** then **pull**.

| You change… | Where it lives | Both PCs get it via |
|-------------|----------------|---------------------|
| Bots / plugins | `src/main/java/.../ox/` | `git pull` (then rebuild jar on Windows and **commit `dist/*.jar`** so Mac can run without building) |
| Windows launch / accounts | `Launch-0x.bat`, `Add-Account.bat`, `tools/` | `git pull` |
| Mac launch / accounts | `0xMAC/` | `git pull` |
| Shipping client binaries | `dist/0x-client.jar`, `dist/ox-incognito-agent.jar` | `git pull` |
| x64 reference (read-only) | `DecompiledScripts/` | `git pull` |

**Daily loop**

1. Work on whichever PC you’re on.
2. Commit + push when done.
3. On the other PC next day: `git pull` (Mac: also `chmod +x 0xMAC/*.sh 0xMAC/*.command` once if needed).

**If you only edit Java plugins:** rebuild on Windows (`.\build.ps1`), then commit the updated `dist/0x-client.jar` so the Mac gets a runnable client without PowerShell/vendor jar.

**If you only edit launch scripts / docs / DecompiledScripts:** pull is enough — no jar rebuild.

Keep launch behaviour in sync: when you change account/identity/launcher logic, update **Windows and `0xMAC/`** in the same commit when possible.

---

## Launch

Jars ship in the repo (`dist/`). Clone → run.

**Windows:** `Add-Account.bat` → `Launch-0x.bat`  
**Mac:** `cd 0xMAC && ./add-account.sh` → `./launch-0x.sh`  
(Needs JDK 11+; Mac GUI needs Python 3 + Tk.)

Accounts: `~/.oxrl./accounts/` (same JSON on both OSes; prefer creating accounts on the OS you play on so MAC/OS spoof matches).

---

## DecompiledScripts/

CFR decompile of Orion **x64** (`svc`) bots + `unethicalite` API — **reference only**, not part of the live build. Browse here when porting features into `ox.*`.

See [DecompiledScripts/README.md](DecompiledScripts/README.md).

---

## Rebuild (Windows)

```powershell
.\build.ps1
```

Needs JDK 11+ and `Desktop\Orion x64\client_patched.jar`. Commit updated `dist/*.jar` if Mac should run the new build.

## Add more plugins

1. Package under `src/main/java/net/runelite/client/plugins/ox/<name>/`
2. Extend `ox.core.OxLoopedPlugin`
3. `@PluginDescriptor`
4. `.\build.ps1` → commit `dist/0x-client.jar` for Mac

## Notes

- Personal use on the same RSPS the base jar targets.
- Do not redistribute the vendor gamepack if that violates server/vendor terms.
