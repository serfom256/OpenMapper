#!/usr/bin/env bash
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
version="$(node -p "require('$here/package.json').version")"
target="$HOME/.vscode/extensions/serfom256.openmapper-fsql-$version"

npx tsc -p "$here"

rm -rf "$target"
mkdir -p "$target"
cp -R "$here/package.json" "$here/language-configuration.json" "$here/README.md" \
      "$here/out" "$here/syntaxes" "$here/images" "$target/"

echo "installed to $target"
echo "reload VS Code to pick it up"
