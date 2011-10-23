#!/bin/sh

# Runs Synoptic from the compiled JAR file, passing all command
# line argument directly to main().

java -ea -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./synoptic/bin/:./lib/jung/* -jar ./lib/synoptic.jar $*
