#!/bin/sh
# Entrypoint for the local test servers in compose.yaml: installs the freshly built Shops JAR, then runs the image's
# normal startup.
set -eu

/shops/docker/install-shops.sh
exec /start "$@"
