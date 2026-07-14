# 0xMAC — macOS launch / accounts / build helpers

Shared game code stays in the repo root (`src/…`, `dist/*.jar`).  
This folder is **Mac-only wrappers**. When launch/account behaviour changes, update Windows scripts **and** `0xMAC/` in the same push when you can.

**Dual PC:** work on Windows or Mac → `git push` → other machine `git pull`. Live bots live under `src/` once; jars in `dist/` must be rebuilt+pushed from Windows after plugin changes. x64 reference: `../DecompiledScripts/`.

## Quick start (clone → play)

```bash
git clone https://github.com/theoxbsc-hub/0xOrion.git
cd 0xOrion/0xMAC
chmod +x *.sh *.command
brew install --cask temurin@11   # JDK 11+
./add-account.sh                 # GUI — or double-click add-account.command
./launch-0x.sh                   # pick account #
```

`../dist/0x-client.jar` and `../dist/ox-incognito-agent.jar` ship in the repo — no copy step.

If the account GUI fails to open (missing Tk): `brew install python-tk` then retry, or `./add-account.sh --cli`.

Accounts: `~/.oxrl./accounts/*.json` (create them on the Mac so identities are Mac-flavoured).

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
