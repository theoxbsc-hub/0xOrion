#!/usr/bin/env bash
# Build ox-incognito-agent.jar on macOS (JDK 11+).
# Compatible with macOS /bin/bash 3.2 (no mapfile).
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
SRC="$ROOT/tools/ox-agent/src"
LIB_DIR="$ROOT/tools/ox-agent/lib"
LIB="$LIB_DIR/asm-9.6.jar"
OUT="$HERE/build/agent-classes"
DIST="$ROOT/dist"
AGENT_JAR="$DIST/ox-incognito-agent.jar"
ASM_URL="https://repo1.maven.org/maven2/org/ow2/asm/asm/9.6/asm-9.6.jar"

if [[ ! -f "$LIB" ]]; then
  echo "ASM jar missing — downloading from Maven Central..."
  mkdir -p "$LIB_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$LIB" "$ASM_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$LIB" "$ASM_URL"
  else
    echo "Need curl or wget to fetch: $ASM_URL"
    exit 1
  fi
fi

JAVAC=""
if command -v javac >/dev/null 2>&1; then
  JAVAC="$(command -v javac)"
elif [[ -x /usr/libexec/java_home ]]; then
  JH="$(/usr/libexec/java_home -v 11+ 2>/dev/null || true)"
  if [[ -n "$JH" && -x "$JH/bin/javac" ]]; then
    JAVAC="$JH/bin/javac"
  fi
fi
if [[ -z "$JAVAC" || ! -x "$JAVAC" ]]; then
  echo "javac not found. Install Temurin 11+: brew install --cask temurin@11"
  exit 1
fi

JAR_BIN=""
if command -v jar >/dev/null 2>&1; then
  JAR_BIN="$(command -v jar)"
else
  JH_DIR="$(dirname "$JAVAC")"
  if [[ -x "$JH_DIR/jar" ]]; then
    JAR_BIN="$JH_DIR/jar"
  fi
fi
if [[ -z "$JAR_BIN" ]]; then
  echo "jar tool not found (same JDK as javac)."
  exit 1
fi

rm -rf "$OUT"
mkdir -p "$OUT" "$DIST"

FILES=()
while IFS= read -r f; do
  [[ -n "$f" ]] && FILES+=("$f")
done <<EOF
$(find "$SRC" -name '*.java' 2>/dev/null | sort)
EOF

if [[ ${#FILES[@]} -eq 0 ]]; then
  echo "No Java sources under $SRC"
  exit 1
fi

"$JAVAC" -encoding UTF-8 -cp "$LIB" -d "$OUT" "${FILES[@]}"

STAGE="$HERE/build/agent-stage"
rm -rf "$STAGE"
mkdir -p "$STAGE"
cp -R "$OUT/." "$STAGE/"
(
  cd "$STAGE"
  "$JAR_BIN" xf "$LIB"
  rm -f META-INF/MANIFEST.MF
  rm -rf META-INF/maven
  cat > MANIFEST.MF <<'EOF'
Manifest-Version: 1.0
Premain-Class: ox.incognito.OxIncognitoAgent
Can-Retransform-Classes: true
Can-Redefine-Classes: true
Boot-Class-Path: ox-incognito-agent.jar

EOF
  if [[ -f "$AGENT_JAR" ]] && ! rm -f "$AGENT_JAR" 2>/dev/null; then
    AGENT_JAR="$DIST/ox-incognito-agent-next.jar"
  fi
  "$JAR_BIN" cfm "$AGENT_JAR" MANIFEST.MF .
)

cp -f "$AGENT_JAR" "$HERE/ox-incognito-agent.jar" 2>/dev/null || true
BYTES="$(wc -c < "$AGENT_JAR" | tr -d ' ')"
echo "Built $AGENT_JAR ($BYTES bytes)"
