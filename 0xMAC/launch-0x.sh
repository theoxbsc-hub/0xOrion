#!/usr/bin/env bash
# ============================================================
#  0xMAC Client launcher — pick account (proxy + MAC identity)
#  Works with macOS /bin/bash 3.2 (no mapfile / bash-4-only features).
# ============================================================
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
IDPY="$HERE/lib/ox_identity.py"
MAIN="net.runelite.client.plugins.ox.OxLauncher"

# Make scripts runnable even when cloned from Windows without +x
chmod +x "$HERE"/*.sh "$HERE"/*.command 2>/dev/null || true

find_newest() {
  local pattern="$1"
  local best="" best_mtime=0
  local dir f m
  for dir in "$HERE" "$HERE/dist" "$ROOT/dist" "$ROOT"; do
    [ -d "$dir" ] || continue
    for f in "$dir"/$pattern; do
      [ -f "$f" ] || continue
      m=$(stat -f %m "$f" 2>/dev/null || stat -c %Y "$f" 2>/dev/null || echo 0)
      if [ "$m" -ge "$best_mtime" ] 2>/dev/null; then
        best_mtime=$m
        best=$f
      fi
    done
  done
  echo "$best"
}

JAR="$(find_newest '0x-client*.jar')"
AGENT="$(find_newest 'ox-incognito-agent*.jar')"

JAVA_BIN="java"
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
elif [[ -x "/usr/libexec/java_home" ]]; then
  # Prefer JDK 11+ if available
  if HOME11="$(/usr/libexec/java_home -v 11+ 2>/dev/null)"; then
    JAVA_BIN="$HOME11/bin/java"
  fi
fi

echo
echo "============================================================"
echo "   0xMAC Client"
echo "============================================================"
echo

if [[ -z "$JAR" || ! -f "$JAR" ]]; then
  echo " ERROR: 0x-client.jar not found."
  echo " Copy dist/0x-client.jar here, or build on Windows/Mac into ../dist/"
  exit 1
fi
if [[ -z "$AGENT" || ! -f "$AGENT" ]]; then
  echo " ERROR: ox-incognito-agent.jar missing."
  echo " Run: ./build-agent.sh   (or copy from ../dist/)"
  exit 1
fi
if [[ ! -f "$IDPY" ]]; then
  echo " ERROR: identity library missing: $IDPY"
  exit 1
fi

# Prepare account → UI on stderr, result line on stdout (id|proxy|name)
set +e
SEL="$(python3 "$IDPY" prepare)"
EC=$?
set -e
if (( EC == 2 )); then
  echo " No accounts yet. Run: ./add-account.sh"
  exit 1
fi
if (( EC != 0 )) || [[ -z "$SEL" ]]; then
  echo " Cancelled."
  exit 0
fi

IFS='|' read -r ACC_ID PROXY_RAW ACC_NAME <<<"$SEL"
JVM_ARGS_FILE="${HOME}/.oxrl./incognito/jvm_args.txt"
if [[ ! -f "$JVM_ARGS_FILE" ]]; then
  echo " ERROR: jvm_args.txt was not written."
  exit 1
fi

# Load one -D per line into array
JVM_ARGS=()
while IFS= read -r line || [[ -n "$line" ]]; do
  [[ -z "$line" ]] && continue
  JVM_ARGS+=("$line")
done < "$JVM_ARGS_FILE"

PROXY_ARG=()
if [[ -n "${PROXY_RAW:-}" ]]; then
  PROXY_ARG=(--proxy "$PROXY_RAW")
fi

echo " Launching ${ACC_NAME}..."
echo " JAVA   $JAVA_BIN"
echo " JAR    $JAR"
echo " AGENT  $AGENT"
echo

# Bash 3.2 + set -u: empty "${arr[@]}" is an unbound-variable error
JAVA_CMD=(
  "$JAVA_BIN"
  -javaagent:"$AGENT"
)
# shellcheck disable=SC2206
JAVA_CMD+=("${JVM_ARGS[@]}")
JAVA_CMD+=(
  -XX:+DisableAttachMechanism
  -Xmx768m -Xss2m
  -XX:+UseSerialGC
  -XX:MaxMetaspaceSize=256m
  -XX:CompressedClassSpaceSize=128m
  -XX:ReservedCodeCacheSize=128m
  -XX:MaxDirectMemorySize=256m
  -XX:ActiveProcessorCount=2
  -XX:+ExitOnOutOfMemoryError
  --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED
  --add-opens java.base/java.net=ALL-UNNAMED
  -Dawt.useSystemAAFontSettings=on
  -Dswing.aatext=true
  -Drunelite.launcher.version=0xmac-1.0
  -Drunelite.launcher.nojvm=true
  -cp "$JAR"
  "$MAIN"
)
if [[ ${#PROXY_ARG[@]} -gt 0 ]]; then
  JAVA_CMD+=("${PROXY_ARG[@]}")
fi

# Don't use exec so launch failures stay visible in Terminal / .command windows
set +e
"${JAVA_CMD[@]}"
JC=$?
set -e
if (( JC != 0 )); then
  echo
  echo " Client exited with code $JC."
  exit "$JC"
fi
exit 0
