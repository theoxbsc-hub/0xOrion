#!/usr/bin/env python3
"""0xMAC Account Manager GUI — same role as Windows OxAccountManager.ps1."""

from __future__ import annotations

import sys
import tkinter as tk
from pathlib import Path
from tkinter import messagebox, ttk
from typing import Any, Dict, List, Optional

HERE = Path(__file__).resolve().parent
if str(HERE) not in sys.path:
    sys.path.insert(0, str(HERE))

import ox_identity as ox  # noqa: E402

BG = "#18181e"
PANEL = "#22222a"
FG = "#ebebf5"
MUTED = "#9191a0"
ACCENT = "#4ecdc4"
OK = "#50be78"
WARN = "#dc5a5a"


def parse_proxy(raw: str) -> Dict[str, str]:
    parts = (raw or "").split(":")
    out = {"host": "", "port": "", "user": "", "pass": ""}
    if len(parts) >= 1:
        out["host"] = parts[0]
    if len(parts) >= 2:
        out["port"] = parts[1]
    if len(parts) >= 4:
        out["user"] = parts[2]
        out["pass"] = parts[3]
    return out


def build_proxy(host: str, port: str, user: str, password: str) -> str:
    h = host.strip()
    po = port.strip()
    u = user.strip()
    pw = password.strip()
    if not h:
        return ""
    if u and pw:
        return f"{h}:{po}:{u}:{pw}"
    return f"{h}:{po}"


class AccountManagerApp:
    def __init__(self) -> None:
        ox.ensure_dirs()
        self.root = tk.Tk()
        self.root.title("0xMAC Account Manager")
        self.root.geometry("920x640")
        self.root.resizable(False, False)
        self.root.configure(bg=BG)

        self.current_id: Optional[str] = None
        self.current_identity: Optional[Dict[str, Any]] = None
        self.account_order: List[str] = []

        self._style()
        self._build()
        self.refresh_list()
        self.clear_editor()
        if self.account_order:
            self.listbox.selection_set(0)
            self.load_account(self.account_order[0])

    def _style(self) -> None:
        style = ttk.Style(self.root)
        try:
            style.theme_use("clam")
        except tk.TclError:
            pass
        style.configure("TFrame", background=BG)
        style.configure("TLabel", background=BG, foreground=FG)
        style.configure("Muted.TLabel", background=BG, foreground=MUTED)
        style.configure("Accent.TLabel", background=BG, foreground=ACCENT, font=("Helvetica", 16, "bold"))
        style.configure("Title.TLabel", background=BG, foreground=FG, font=("Helvetica", 12, "bold"))
        style.configure("TEntry", fieldbackground=PANEL, foreground=FG)
        style.configure("TCombobox", fieldbackground=PANEL, foreground=FG)
        style.configure("TCheckbutton", background=BG, foreground=MUTED)
        style.configure("TButton", padding=6)
        style.configure("TLabelframe", background=BG, foreground=ACCENT)
        style.configure("TLabelframe.Label", background=BG, foreground=ACCENT)

    def _entry(self, parent: tk.Misc, width: int = 28, show: Optional[str] = None) -> ttk.Entry:
        e = ttk.Entry(parent, width=width)
        if show is not None:
            e.configure(show=show)
        return e

    def _build(self) -> None:
        pad = {"padx": 10, "pady": 4}

        hdr = ttk.Frame(self.root)
        hdr.pack(fill="x", **pad)
        ttk.Label(hdr, text="0xMAC Account Manager", style="Accent.TLabel").pack(side="left")
        ttk.Label(
            hdr,
            text="Add accounts here, then run launch-0x.sh and pick a number.",
            style="Muted.TLabel",
        ).pack(side="left", padx=(16, 0))

        body = ttk.Frame(self.root)
        body.pack(fill="both", expand=True, **pad)

        left = ttk.Frame(body)
        left.pack(side="left", fill="y")
        ttk.Label(left, text="Accounts", style="Muted.TLabel").pack(anchor="w")
        self.listbox = tk.Listbox(
            left,
            width=42,
            height=28,
            bg=PANEL,
            fg=FG,
            selectbackground=ACCENT,
            selectforeground="#111",
            font=("Menlo", 10),
            relief="flat",
            highlightthickness=1,
            highlightbackground="#333",
        )
        self.listbox.pack(fill="y", pady=(4, 0))
        self.listbox.bind("<<ListboxSelect>>", self._on_select)

        right = ttk.Frame(body)
        right.pack(side="left", fill="both", expand=True, padx=(16, 0))

        self.title_lbl = ttk.Label(right, text="New account", style="Title.TLabel")
        self.title_lbl.grid(row=0, column=0, columnspan=3, sticky="w", pady=(0, 8))

        ttk.Label(right, text="Display name", style="Muted.TLabel").grid(row=1, column=0, sticky="w")
        self.txt_name = self._entry(right, 34)
        self.txt_name.grid(row=1, column=1, columnspan=2, sticky="we", pady=2)

        ttk.Label(right, text="Username", style="Muted.TLabel").grid(row=2, column=0, sticky="w")
        self.txt_user = self._entry(right, 34)
        self.txt_user.grid(row=2, column=1, columnspan=2, sticky="we", pady=2)

        ttk.Label(right, text="Password", style="Muted.TLabel").grid(row=3, column=0, sticky="w")
        self.txt_pass = self._entry(right, 28, show="*")
        self.txt_pass.grid(row=3, column=1, sticky="we", pady=2)
        self.show_pass = tk.BooleanVar(value=False)
        ttk.Checkbutton(
            right,
            text="show",
            variable=self.show_pass,
            command=self._toggle_pass,
        ).grid(row=3, column=2, sticky="w", padx=(6, 0))

        proxy = ttk.LabelFrame(right, text="Proxy  (leave Host blank for DIRECT)", padding=8)
        proxy.grid(row=4, column=0, columnspan=3, sticky="we", pady=(12, 8))

        ttk.Label(proxy, text="Host / IP", style="Muted.TLabel").grid(row=0, column=0, sticky="w")
        self.txt_host = self._entry(proxy, 24)
        self.txt_host.grid(row=0, column=1, sticky="we", padx=4)
        ttk.Label(proxy, text="Port", style="Muted.TLabel").grid(row=0, column=2, sticky="w")
        self.txt_port = self._entry(proxy, 8)
        self.txt_port.grid(row=0, column=3, sticky="w", padx=4)

        ttk.Label(proxy, text="User", style="Muted.TLabel").grid(row=1, column=0, sticky="w", pady=(6, 0))
        self.txt_puser = self._entry(proxy, 24)
        self.txt_puser.grid(row=1, column=1, sticky="we", padx=4, pady=(6, 0))
        ttk.Label(proxy, text="Pass", style="Muted.TLabel").grid(row=1, column=2, sticky="w", pady=(6, 0))
        self.txt_ppass = self._entry(proxy, 14, show="*")
        self.txt_ppass.grid(row=1, column=3, sticky="w", padx=4, pady=(6, 0))

        ttk.Label(
            proxy,
            text="Format when saved: host:port or host:port:user:pass",
            style="Muted.TLabel",
        ).grid(row=2, column=0, columnspan=4, sticky="w", pady=(8, 0))

        ident = ttk.LabelFrame(right, text="Device identity (frozen MAC / HWID)", padding=8)
        ident.grid(row=5, column=0, columnspan=3, sticky="we", pady=(4, 8))

        self.ident_text = tk.Text(
            ident,
            height=7,
            width=62,
            bg=PANEL,
            fg=FG,
            font=("Menlo", 10),
            relief="flat",
            highlightthickness=0,
            state="disabled",
        )
        self.ident_text.grid(row=0, column=0, columnspan=3, sticky="we")

        ttk.Label(ident, text="Timezone", style="Muted.TLabel").grid(row=1, column=0, sticky="w", pady=(8, 0))
        self.cmb_tz = ttk.Combobox(ident, values=list(ox.TIMEZONES), state="readonly", width=28)
        self.cmb_tz.grid(row=1, column=1, sticky="w", pady=(8, 0), padx=4)
        if ox.TIMEZONES:
            self.cmb_tz.set(ox.TIMEZONES[0])
        ttk.Button(ident, text="Regenerate identity", command=self.regen_identity).grid(
            row=1, column=2, sticky="e", pady=(8, 0)
        )

        btns = ttk.Frame(right)
        btns.grid(row=6, column=0, columnspan=3, sticky="we", pady=(12, 4))
        ttk.Button(btns, text="New account", command=self.on_new).pack(side="left", padx=(0, 6))
        ttk.Button(btns, text="Save account", command=self.on_save).pack(side="left", padx=(0, 6))
        ttk.Button(btns, text="Delete account", command=self.on_delete).pack(side="left", padx=(0, 6))
        ttk.Button(btns, text="Close", command=self.root.destroy).pack(side="left")

        self.status = ttk.Label(self.root, text="", style="Muted.TLabel")
        self.status.pack(fill="x", padx=10, pady=(0, 10))
        self._set_status(f"Safe to edit.  Store: {ox.ACCOUNTS_DIR}", OK)

    def _toggle_pass(self) -> None:
        self.txt_pass.configure(show="" if self.show_pass.get() else "*")

    def _set_status(self, text: str, colour: str = MUTED) -> None:
        self.status.configure(text=text, foreground=colour)

    def _show_identity(self, ident: Optional[Dict[str, Any]]) -> None:
        self.ident_text.configure(state="normal")
        self.ident_text.delete("1.0", "end")
        if not ident or not ident.get("mac"):
            self.ident_text.insert("1.0", "(no identity yet — minted on save)")
        else:
            lines = [
                f"MAC      : {ident.get('mac', '')}",
                f"HWID     : {ident.get('machineId', '')}",
                f"OS       : {ident.get('osName', '')} {ident.get('osVersion', '')}",
                f"Hostname : {ident.get('hostname', '')}",
                f"User     : {ident.get('username', '')}",
                f"Java     : {ident.get('javaVersion', '')} ({ident.get('javaVendor', '')})",
            ]
            self.ident_text.insert("1.0", "\n".join(lines))
            tz = str(ident.get("timezone") or "")
            if tz in ox.TIMEZONES:
                self.cmb_tz.set(tz)
        self.ident_text.configure(state="disabled")

    def _summary(self, account: Dict[str, Any], n: int) -> str:
        name = (account.get("displayName") or "acct")[:14]
        user = (account.get("username") or "(no user)")[:12]
        proxy = (account.get("proxy") or "").strip()
        px = "DIRECT" if not proxy else proxy.split(":")[0]
        mac = (account.get("identity") or {}).get("mac") or "no-mac"
        return f"{n:2d}.  {name:<14}  user={user:<12}  [{px}]  {mac}"

    def refresh_list(self) -> None:
        keep = self.current_id
        self.listbox.delete(0, "end")
        self.account_order = []
        for i, acc in enumerate(ox.list_accounts(), start=1):
            self.listbox.insert("end", self._summary(acc, i))
            self.account_order.append(str(acc["id"]))
        if keep and keep in self.account_order:
            idx = self.account_order.index(keep)
            self.listbox.selection_clear(0, "end")
            self.listbox.selection_set(idx)
            self.listbox.see(idx)

    def clear_editor(self) -> None:
        self.current_id = None
        self.current_identity = ox.new_identity()
        self.title_lbl.configure(text="New account")
        self.txt_name.delete(0, "end")
        self.txt_user.delete(0, "end")
        self.txt_pass.delete(0, "end")
        self.txt_host.delete(0, "end")
        self.txt_port.delete(0, "end")
        self.txt_puser.delete(0, "end")
        self.txt_ppass.delete(0, "end")
        self._show_identity(self.current_identity)
        self._set_status(f"Safe to edit.  Store: {ox.ACCOUNTS_DIR}", OK)

    def load_account(self, account_id: str) -> None:
        acc = ox.get_account(account_id)
        if not acc:
            return
        self.current_id = str(acc["id"])
        self.current_identity = dict(acc.get("identity") or ox.new_identity())
        self.title_lbl.configure(text=f"Edit: {acc.get('displayName') or ''}")
        for entry, value in (
            (self.txt_name, acc.get("displayName") or ""),
            (self.txt_user, acc.get("username") or ""),
            (self.txt_pass, acc.get("password") or ""),
        ):
            entry.delete(0, "end")
            entry.insert(0, str(value))
        px = parse_proxy(str(acc.get("proxy") or ""))
        for entry, value in (
            (self.txt_host, px["host"]),
            (self.txt_port, px["port"]),
            (self.txt_puser, px["user"]),
            (self.txt_ppass, px["pass"]),
        ):
            entry.delete(0, "end")
            entry.insert(0, value)
        self._show_identity(self.current_identity)

    def _on_select(self, _event: Any = None) -> None:
        sel = self.listbox.curselection()
        if not sel:
            return
        idx = int(sel[0])
        if 0 <= idx < len(self.account_order):
            self.load_account(self.account_order[idx])

    def regen_identity(self) -> None:
        self.current_identity = ox.new_identity()
        tz = self.cmb_tz.get().strip()
        if tz:
            self.current_identity["timezone"] = tz
        self._show_identity(self.current_identity)
        self._set_status("New identity minted (not saved yet — click Save account).", OK)

    def on_new(self) -> None:
        self.listbox.selection_clear(0, "end")
        self.clear_editor()

    def on_save(self) -> None:
        name = self.txt_name.get().strip()
        user = self.txt_user.get().strip()
        password = self.txt_pass.get()
        host = self.txt_host.get().strip()
        port = self.txt_port.get().strip()
        puser = self.txt_puser.get().strip()
        ppass = self.txt_ppass.get().strip()

        if not name:
            messagebox.showerror("0xMAC", "Display name is required.")
            return
        if not user:
            messagebox.showerror("0xMAC", "Username is required.")
            return
        if not password:
            messagebox.showerror("0xMAC", "Password is required.")
            return
        if host:
            if not port.isdigit() or not (1 <= int(port) <= 65535):
                messagebox.showerror("0xMAC", "Port must be a number 1–65535.")
                return
            if ":" in host:
                messagebox.showerror("0xMAC", "Host must not contain ':' — put the port in Port.")
                return
            if ":" in puser or ":" in ppass:
                messagebox.showerror("0xMAC", "Proxy user/pass must not contain ':'.")
                return

        if not self.current_identity or not self.current_identity.get("mac"):
            self.current_identity = ox.new_identity()
        tz = self.cmb_tz.get().strip()
        if tz:
            self.current_identity["timezone"] = tz

        proxy = build_proxy(host, port, puser, ppass)

        if not self.current_id:
            acc = ox.new_account(name, user, password, proxy)
            acc["identity"] = self.current_identity
            ox.save_account(acc)
            self.current_id = str(acc["id"])
        else:
            acc = ox.get_account(self.current_id) or ox.new_account(name, user, password, proxy)
            acc["id"] = self.current_id
            acc["displayName"] = name
            acc["username"] = user
            acc["password"] = password
            acc["proxy"] = proxy
            acc["identity"] = self.current_identity
            acc["enabled"] = True
            ox.save_account(acc)

        self.refresh_list()
        self.load_account(self.current_id)
        px_tag = proxy.split(":")[0] if proxy else "DIRECT"
        mac = (self.current_identity or {}).get("mac", "")
        self._set_status(
            f"Saved '{name}' ({user})  proxy={px_tag}  mac={mac}.  Use ./launch-0x.sh to pick this account.",
            OK,
        )

    def on_delete(self) -> None:
        if not self.current_id:
            messagebox.showinfo("0xMAC", "Select an account first.")
            return
        if not messagebox.askyesno(
            "Confirm delete",
            "Delete this account, its MAC identity, and cache folder?\n\nThis cannot be undone.",
        ):
            return
        ox.remove_account(self.current_id)
        self.clear_editor()
        self.refresh_list()
        self._set_status("Account deleted.", WARN)

    def run(self) -> None:
        self.root.mainloop()


def main() -> int:
    try:
        AccountManagerApp().run()
    except tk.TclError as exc:
        print(f"GUI unavailable ({exc}). Falling back to CLI menu.", file=sys.stderr)
        return ox.cmd_menu([])
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
