#!/bin/sh

cd "$( dirname "$0" )" || exit
java -Xms500M -Xmx1200M -XX:+UseG1GC -jar spongevanilla-*.jar nogui