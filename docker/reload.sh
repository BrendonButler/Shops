#!/bin/sh
# Installs the freshly built Shops JAR into running test servers and reloads their plugins with /reload, which is faster
# than a restart but not fully reliable. If something behaves strangely afterwards, restart the server instead.
#
# Usage: docker/reload.sh [server...]   (default: every running test server)
set -eu

cd "$(dirname "$0")/.."
# Look at every test server, not just the ones in the default profile.
export COMPOSE_PROFILES="*"

if [ "$#" -eq 0 ]; then
    set -- $(docker compose ps --services --status running)
fi

if [ "$#" -eq 0 ]; then
    echo "No test server is running. Start one with: docker compose up -d" >&2
    exit 1
fi

# Keep going when one server fails, so the others still get the new build, and report the failures at the end.
failed=""
for server in "$@"; do
    echo "== $server"
    # Namespaced, because on Paper 26.x a plain "reload" is the vanilla data pack reload and leaves plugins alone.
    if ! docker compose exec -T "$server" sh /shops/docker/install-shops.sh ||
        ! docker compose exec -T "$server" rcon-cli "bukkit:reload confirm"; then
        echo "Reloading $server failed. Restart it instead: docker compose restart $server" >&2
        failed="$failed $server"
    fi
done

if [ -n "$failed" ]; then
    echo "Not reloaded:$failed" >&2
    exit 1
fi
