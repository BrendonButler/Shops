#!/bin/sh

cd "$( dirname "$0" )" || exit
java -Xms1G -Xmx2G -XX:+UseG1GC -jar spigot-*.jar nogui