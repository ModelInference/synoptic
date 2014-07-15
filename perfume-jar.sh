#!/bin/sh

# Runs Perfume from the compiled JAR file, passing all command
# line argument directly to main().

java -ea -jar ./lib/perfume.jar $*
