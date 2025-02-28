#!/bin/sh

cd "$( dirname "$0" )" || exit
/opt/homebrew/Cellar/openjdk@17/17.0.13/bin/java -Xms500M -Xmx1200M -XX:+UseG1GC -jar spongevanilla-*.jar nogui
