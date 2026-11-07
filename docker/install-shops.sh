#!/bin/sh
# Copies the Shops JAR built into spigot/target (mounted read-only at /shops/target) to plugins/Shops.jar. Runs inside
# a test server container, from start-with-shops.sh on every start and from reload.sh.
set -eu

target=/shops/target
plugins=/data/plugins

# The shaded plugin JAR, skipping the unshaded original-* copy and any javadoc/sources JARs.
jar=$(find "$target" -maxdepth 1 -name 'shops-spigot-*.jar' \
    ! -name '*-javadoc.jar' ! -name '*-sources.jar' 2>/dev/null | sort | tail -n 1)

if [ -z "$jar" ]; then
    echo "No Shops JAR found in spigot/target. Build it first with: mvn package -DskipTests" >&2
    exit 1
fi

# Write a temporary file and rename it over the old JAR, so a running server never reads a half-copied file.
# Always overwrite, because the image's own plugin sync skips files that have the same size.
mkdir -p "$plugins"
cp "$jar" "$plugins/.Shops.jar.tmp"
# The server runs as UID/GID 1000 unless the image's UID/GID settings say otherwise.
chown "${UID:-1000}:${GID:-1000}" "$plugins" "$plugins/.Shops.jar.tmp"
mv -f "$plugins/.Shops.jar.tmp" "$plugins/Shops.jar"
echo "Installed $(basename "$jar") as plugins/Shops.jar"
