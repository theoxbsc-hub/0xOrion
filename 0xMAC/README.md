# 0xMAC — macOS launch / accounts / build helpers

Shared game code stays in the repo root (`src/…`, `dist/*.jar`).  
This folder is **Mac-only wrappers**. When launch/account behaviour changes, update Windows scripts **and** `0xMAC/`.

## Quick start (clone → play)

```bash
git clone git@github.com:theoxbsc-hub/0xOrion.git
cd 0xOrion/0xMAC
chmod +x *.sh *.command
```

1. **JDK 11+**  
   `brew install --cask temurin@11`
2. **Python 3** with Tk (for the Account Manager GUI)  
   macOS usually has `python3`; if the GUI fails:  
   `brew install python-tk`  or  `brew install python-tk@3.12`
3. **Jars** — copy from a Windows build into `../dist/`:
   - `0x-client.jar`
   - `ox-incognito-agent.jar`  
   Or build the agent on Mac: `./build-agent.sh` (downloads ASM if needed).  
   Full client patches still run best via Windows `build.ps1`.
4. **Accounts (GUI)**  
   `./add-account.sh`  
   or double-click `add-account.command`  
   (CLI: `./add-account.sh --cli`  or  `./accounts.sh list|add|…`)
5. **Launch**  
   `./launch-0x.sh`  
   or double-click `launch-0x.command`  
   Pick the account number → client starts with that proxy + MAC spoof.

Accounts use the **same JSON path as Windows**: `~/.oxrl./accounts/*.json`.  
Create accounts on the Mac so identities are Mac-flavoured (`Mac OS X`, MacBook hostnames, etc.).

## Scripts

| Script | Role |
|--------|------|
| `add-account.sh` / `.command` | **GUI** account manager (New / Save / Delete / Regen identity) |
| `accounts.sh` | CLI: `list` `add` `remove` `show` `menu` |
| `launch-0x.sh` / `.command` | Account picker + javaagent + OxLauncher |
| `build-agent.sh` | Builds `ox-incognito-agent.jar` on Mac |
| `build.sh` | Helper — prefers copied Windows jars; optional `pwsh` + vendor jar |

## How launch works

1. You pick an account in `launch-0x.sh`.
2. `ox_identity.py prepare` writes `~/.oxrl./incognito/jvm_args.txt` (MAC, HWID, cache dir, creds, proxy flags).
3. Java starts with `-javaagent:ox-incognito-agent.jar` + those `-D` flags + `OxLauncher` (`--proxy` if set).
4. Config/home stays under `~/.oxrl.` so it does not fight a stock `.orion` install.

## Dual maintenance rule

- **Plugins / bots** → edit once under `src/main/java/.../ox/`
- **Launcher / accounts / identity** → edit Windows (`Launch-0x.bat`, `Add-Account.bat`, `tools/…`) **and** the matching files here

## Layout

```
0xMAC/
  lib/ox_identity.py          # JSON accounts + jvm_args
  lib/account_manager_gui.py  # Tk GUI (Add-Account equivalent)
  add-account.sh|.command
  launch-0x.sh|.command
  accounts.sh
  build-agent.sh
  build.sh
  README.md
```
