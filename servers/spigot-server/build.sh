#!/bin/sh

: "${VERSION:=latest}"
cd "$( dirname "$0" )" || exit
[[ ! -f BuildTools.jar ]] && echo "BuildTools.jar is missing and will be downloaded." && curl -o ./BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

if [[ -f BuildTools.jar && ${VERSION} == "latest" ]]; then
    echo "BuildTools.jar will be updated."
    curl -o ./BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
fi

java -jar ./BuildTools.jar --rev "${VERSION}"