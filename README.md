# 0x Client

Personal fork of the same private-server RuneLite/OpenOSRS stack used by the Orion x64 client, with:

- **No** x64 licence dialog / entitlement gate / `svc` bot suite
- **Our** plugins under `net.runelite.client.plugins.ox.*` (starts with **0xWCBot**)
- Separate config home: `%USERPROFILE%\.oxrl.` (so it does not fight `%USERPROFILE%\.orion`)

## Build

```powershell
cd $env:USERPROFILE\Desktop\0x-client
.\build.ps1
```

Requires JDK 11+ (`javac`) and the vendor jar at `Desktop\Orion x64\client_patched.jar` as the game-client base.

Output: `dist\0x-client.jar` + `dist\Launch-0x.bat` (+ incognito agent if present).

## Accounts (proxy + MAC isolation)

Same flow as Orion x64: manage accounts in a GUI, then pick a number at launch.

1. **Account Manager** (like x64 `Add-Account.bat`)
   ```text
   Add-Account.bat
   ```
   Add / edit / delete accounts. Each gets username + password + proxy + a frozen MAC/HWID.

2. **Launch** — console lists accounts; type the number to run:
   ```text
   Launch-0x.bat
   ```
   Applies that account's proxy + `ox-incognito-agent` MAC spoof, then starts the client.

Data: `%USERPROFILE%\.oxrl.\accounts\` · cache: `%USERPROFILE%\.oxrl.\account-cache\<id>\`

## Add more plugins

1. Create a package under `src/main/java/net/runelite/client/plugins/ox/<name>/`
2. Extend `ox.core.OxLoopedPlugin`
3. Annotate with `@PluginDescriptor`
4. Re-run `.\build.ps1`

## Notes

- This is for personal use on the same RSPS the base jar targets (`CODEBASE` / modulus already baked in).
- Stock QoL plugins (Menu Entry Swapper, etc.) remain from the base jar.
- Do not redistribute the base gamepack / injective client if that violates the server or vendor terms.
