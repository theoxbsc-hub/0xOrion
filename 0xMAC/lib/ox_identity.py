#!/usr/bin/env python3
"""0xMAC identity + accounts — same JSON schema as Windows ox-identity.ps1."""

from __future__ import annotations

import json
import platform
import random
import secrets
import sys
import uuid
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Optional

OX_HOME = Path.home() / ".oxrl."
ACCOUNTS_DIR = OX_HOME / "accounts"
CACHE_ROOT = OX_HOME / "account-cache"
INCOGNITO_DIR = OX_HOME / "incognito"
ORION_HOME = Path.home() / ".orion"

VENDOR_OUIS = [
    "00:1B:21", "00:1F:3C", "3C:97:0E", "8C:A9:82",
    "00:E0:4C", "1C:BF:CE",
    "00:14:22", "18:03:73", "B8:CA:3A", "F8:BC:12",
    "00:1A:92", "2C:56:DC", "50:46:5D", "04:D4:C4",
    "00:1F:29", "3C:D9:2B", "70:5A:0F", "98:E7:F4",
    "50:C7:BF", "14:CC:20", "A4:2B:B0", "98:DA:C4",
]
FIRST_NAMES = [
    "James", "Robert", "John", "Michael", "David", "William", "Richard", "Joseph",
    "Thomas", "Daniel", "Matthew", "Anthony", "Mark", "Steven", "Andrew", "Joshua",
    "Kenneth", "Kevin", "Brian", "George", "Timothy", "Ronald", "Edward", "Jason",
]
HOST_PREFIXES = ["MacBook-Pro", "MacBook-Air", "iMac", "Mac-mini", "Mac-Studio"]
TIMEZONES = [
    "America/New_York", "America/Chicago", "America/Denver", "America/Los_Angeles",
    "America/Phoenix", "America/Detroit", "America/Toronto", "America/Vancouver",
]
JAVA_VERSIONS = [
    "11.0.20", "11.0.21", "11.0.22", "11.0.23", "11.0.24", "11.0.25", "11.0.26", "11.0.27",
]
JAVA_VENDORS = [
    "Eclipse Adoptium", "Eclipse Foundation", "Amazon.com Inc.", "BellSoft", "Microsoft",
]
MAX_MEM = [1024, 1536, 2048, 3072, 4096]
CPU_CHOICES = [4, 6, 8, 12, 16]


def ensure_dirs() -> None:
    for path in (OX_HOME, ACCOUNTS_DIR, CACHE_ROOT, INCOGNITO_DIR):
        path.mkdir(parents=True, exist_ok=True)


def new_mac() -> str:
    oui = random.choice(VENDOR_OUIS)
    tail = ":".join(f"{b:02X}" for b in secrets.token_bytes(3))
    return f"{oui}:{tail}"


def new_machine_id() -> str:
    return secrets.token_hex(16)


def new_identity() -> Dict[str, Any]:
    first = random.choice(FIRST_NAMES)
    user = f"{first.lower()}{random.randint(10, 99)}"
    # Mac-flavoured fingerprint (still same schema as Windows accounts)
    return {
        "mac": new_mac(),
        "machineId": new_machine_id(),
        "username": user,
        "hostname": f"{random.choice(HOST_PREFIXES)}-{random.randint(100000, 999999)}",
        "homePath": f"/Users/{user}",
        "localIp": f"192.168.{random.randint(0, 255)}.{random.randint(2, 254)}",
        "timezone": random.choice(TIMEZONES),
        "osName": "Mac OS X",
        "osVersion": random.choice(["14.5", "14.6", "15.0", "15.1", "15.2"]),
        "osArch": "aarch64" if platform.machine().lower() in ("arm64", "aarch64") else "x86_64",
        "javaVersion": random.choice(JAVA_VERSIONS),
        "javaVendor": random.choice(JAVA_VENDORS),
        "maxMemMb": random.choice(MAX_MEM),
        "cpuCount": random.choice(CPU_CHOICES),
        "language": "en",
        "country": "US",
        "createdAt": datetime.now(timezone.utc).isoformat(),
    }


def new_account_id() -> str:
    return "a" + uuid.uuid4().hex[:10]


def list_accounts() -> List[Dict[str, Any]]:
    ensure_dirs()
    accounts: List[Dict[str, Any]] = []
    for path in sorted(ACCOUNTS_DIR.glob("*.json")):
        try:
            accounts.append(json.loads(path.read_text(encoding="utf-8")))
        except (OSError, json.JSONDecodeError):
            continue
    accounts.sort(key=lambda a: (a.get("displayName") or "").lower())
    return accounts


def save_account(account: Dict[str, Any]) -> Path:
    ensure_dirs()
    path = ACCOUNTS_DIR / f"{account['id']}.json"
    path.write_text(json.dumps(account, indent=2) + "\n", encoding="utf-8")
    return path


def get_account(account_id: str) -> Optional[Dict[str, Any]]:
    path = ACCOUNTS_DIR / f"{account_id}.json"
    if not path.is_file():
        return None
    return json.loads(path.read_text(encoding="utf-8"))


def remove_account(account_id: str) -> None:
    path = ACCOUNTS_DIR / f"{account_id}.json"
    if path.is_file():
        path.unlink()
    cache = CACHE_ROOT / account_id
    if cache.exists():
        import shutil
        shutil.rmtree(cache, ignore_errors=True)


def new_account(
    display_name: str,
    username: str = "",
    password: str = "",
    proxy: str = "",
) -> Dict[str, Any]:
    return {
        "id": new_account_id(),
        "displayName": display_name,
        "username": username,
        "password": password,
        "proxy": proxy,
        "enabled": True,
        "identity": new_identity(),
        "createdAt": datetime.now(timezone.utc).isoformat(),
    }


def _ensure_symlink(link: Path, target: Path) -> None:
    if not target.exists() or link.exists():
        return
    try:
        link.symlink_to(target, target_is_directory=True)
    except OSError:
        pass


def stage_cache_isolation(account_id: str) -> str:
    """Mirror Windows Stage-OxCacheIsolation using symlinks instead of junctions."""
    name = f"ox-{account_id}"
    shared_candidates = [
        ORION_HOME / "jagexcache",
        OX_HOME / "jagexcache",
    ]
    shared = next((p for p in shared_candidates if p.exists()), None)
    cdir = ORION_HOME / "custom-cache" / name
    cdir.mkdir(parents=True, exist_ok=True)

    if shared is not None:
        _ensure_symlink(cdir / "jagexcache", shared / "jagexcache" if (shared / "jagexcache").exists() else shared)
        oldschool = shared / "oldschool"
        if oldschool.exists():
            _ensure_symlink(cdir / "oldschool", oldschool)
        shared_rnd = shared / "random.dat"
        if shared_rnd.is_file():
            try:
                shared_rnd.unlink()
            except OSError:
                pass

    uid_path = INCOGNITO_DIR / f"uid-{account_id}.random.dat"
    if not uid_path.is_file() or uid_path.stat().st_size != 24:
        INCOGNITO_DIR.mkdir(parents=True, exist_ok=True)
        uid_path.write_bytes(secrets.token_bytes(24))
    (cdir / "random.dat").write_bytes(uid_path.read_bytes())
    return name


def write_jvm_args(account: Dict[str, Any]) -> Path:
    """Write one -D flag per line (bash-friendly). Same properties as Windows."""
    ensure_dirs()
    ident = account.get("identity") or {}
    cache_name = stage_cache_isolation(str(account["id"]))
    password = account.get("password") or ""
    proxy = account.get("proxy") or ""

    # os.version is kept as 10.0 in Windows script for compatibility; on Mac we spoof Darwin-ish version numbers into os.name/os.version fields used by agent.
    os_version = str(ident.get("osVersion") or "14.5")
    args = [
        f"-Duser.name={ident.get('username', '')}",
        f"-Duser.timezone={ident.get('timezone', 'America/New_York')}",
        f"-Duser.language={ident.get('language', 'en')}",
        f"-Duser.country={ident.get('country', 'US')}",
        f"-Dos.name={ident.get('osName', 'Mac OS X')}",
        f"-Dos.version={os_version}",
        f"-Dos.arch={ident.get('osArch', 'aarch64')}",
        f"-Djava.version={ident.get('javaVersion', '11.0.27')}",
        f"-Djava.vendor={ident.get('javaVendor', 'Eclipse Adoptium')}",
        f"-Dox.mac={ident.get('mac', '')}",
        f"-Dox.machineId={ident.get('machineId', '')}",
        f"-Dox.javaVersion={ident.get('javaVersion', '')}",
        f"-Dox.javaVendor={ident.get('javaVendor', '')}",
        f"-Dox.maxMem={ident.get('maxMemMb', 2048)}",
        f"-Dox.cpu={ident.get('cpuCount', 8)}",
        f"-Dox.localIp={ident.get('localIp', '')}",
        f"-Dox.hostname={ident.get('hostname', '')}",
        f"-Dox.accountId={account.get('id', '')}",
        f"-Dox.displayName={account.get('displayName', '')}",
        f"-Dox.username={account.get('username', '')}",
        f"-Dox.password={password}",
        f"-Dox.proxy={proxy}",
        "-Dox.active=true",
        "-Dox.quiet=true",
        f"-Dunethicalite.cache-dir={cache_name}",
    ]
    out = INCOGNITO_DIR / "jvm_args.txt"
    out.write_text("\n".join(args) + "\n", encoding="utf-8")
    return out


def cmd_list(_: List[str]) -> int:
    accounts = list_accounts()
    if not accounts:
        print("No accounts yet. Run: ./accounts.sh add")
        return 2
    print()
    print(f"  {'#':>2}  {'Name':<16}  {'User':<16}  {'Proxy':<14}  MAC")
    print(f"  {'--':>2}  {'-'*16}  {'-'*16}  {'-'*14}  {'-'*17}")
    for i, acc in enumerate(accounts, start=1):
        proxy = (acc.get("proxy") or "").strip()
        px = "DIRECT" if not proxy else proxy.split(":")[0]
        mac = (acc.get("identity") or {}).get("mac", "")
        print(f"  {i:2d}  {(acc.get('displayName') or '')[:16]:<16}  {(acc.get('username') or '')[:16]:<16}  {px:<14}  {mac}")
    print()
    return 0


def cmd_add(_: List[str]) -> int:
    print("── Add 0x account (Mac) ──")
    display = input("Display name: ").strip()
    if not display:
        print("Cancelled.")
        return 1
    username = input("Login username: ").strip()
    password = input("Password: ").strip()
    print("Proxy (host:port or host:port:user:pass, blank = none):")
    proxy = input("> ").strip()
    account = new_account(display, username, password, proxy)
    path = save_account(account)
    print(f"Saved {account['id']} → {path}")
    print(f"MAC {account['identity']['mac']}  host {account['identity']['hostname']}")
    return 0


def cmd_remove(argv: List[str]) -> int:
    accounts = list_accounts()
    if not accounts:
        print("No accounts.")
        return 2
    cmd_list([])
    raw = argv[0] if argv else input("Remove # (or id): ").strip()
    if not raw:
        return 1
    target = None
    if raw.isdigit():
        n = int(raw)
        if 1 <= n <= len(accounts):
            target = accounts[n - 1]
    else:
        target = get_account(raw)
    if not target:
        print("Not found.")
        return 1
    confirm = input(f"Delete '{target.get('displayName')}'? [y/N] ").strip().lower()
    if confirm != "y":
        print("Cancelled.")
        return 1
    remove_account(str(target["id"]))
    print("Removed.")
    return 0


def cmd_show(argv: List[str]) -> int:
    accounts = list_accounts()
    if not accounts:
        print("No accounts.")
        return 2
    if not argv:
        cmd_list([])
        raw = input("Show # : ").strip()
    else:
        raw = argv[0]
    if raw.isdigit():
        n = int(raw)
        acc = accounts[n - 1] if 1 <= n <= len(accounts) else None
    else:
        acc = get_account(raw)
    if not acc:
        print("Not found.")
        return 1
    print(json.dumps(acc, indent=2))
    return 0


def cmd_prepare(argv: List[str]) -> int:
    """Write jvm_args for account id or picker index; print id|proxy|displayName."""
    accounts = list_accounts()
    if not accounts:
        print("No accounts.", file=sys.stderr)
        return 2
    if not argv:
        cmd_list([])
        raw = input("Launch account # : ").strip()
    else:
        raw = argv[0]
    if not raw or raw == "0":
        return 3
    if raw.isdigit():
        n = int(raw)
        if n < 1 or n > len(accounts):
            print("Invalid choice.", file=sys.stderr)
            return 4
        acc = accounts[n - 1]
    else:
        acc = get_account(raw)
        if not acc:
            print("Not found.", file=sys.stderr)
            return 4
    write_jvm_args(acc)
    proxy = (acc.get("proxy") or "").strip()
    line = f"{acc['id']}|{proxy}|{acc.get('displayName') or ''}"
    print(line)
    return 0


def cmd_menu(_: List[str]) -> int:
    while True:
        print()
        print("════════════════════════════════════")
        print("  0xMAC Account Manager")
        print("════════════════════════════════════")
        print("  1) List accounts")
        print("  2) Add account")
        print("  3) Show account JSON")
        print("  4) Remove account")
        print("  0) Quit")
        print()
        choice = input("  Choice: ").strip()
        if choice == "0":
            return 0
        if choice == "1":
            cmd_list([])
        elif choice == "2":
            cmd_add([])
        elif choice == "3":
            cmd_show([])
        elif choice == "4":
            cmd_remove([])
        else:
            print("Invalid.")


def main() -> int:
    ensure_dirs()
    argv = sys.argv[1:]
    if not argv:
        return cmd_menu([])
    cmd = argv[0].lower()
    rest = argv[1:]
    if cmd in ("list", "ls"):
        return cmd_list(rest)
    if cmd == "add":
        return cmd_add(rest)
    if cmd in ("remove", "rm", "delete"):
        return cmd_remove(rest)
    if cmd == "show":
        return cmd_show(rest)
    if cmd == "prepare":
        return cmd_prepare(rest)
    if cmd in ("menu", "ui"):
        return cmd_menu(rest)
    print(f"Unknown command: {cmd}")
    print("Usage: ox_identity.py [list|add|remove|show|prepare|menu]")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
