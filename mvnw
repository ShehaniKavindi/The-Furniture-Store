#!/bin/sh
basedir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java -classpath "$basedir/.mvn/wrapper/maven-wrapper.jar" -Dmaven.multiModuleProjectDirectory="$basedir" org.apache.maven.wrapper.MavenWrapperMain "$@"
