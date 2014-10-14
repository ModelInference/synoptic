#!/bin/sh

# Runs CSight from the compiled JAR file, passing all command line
# argument directly to main().

java -ea -jar ./lib/csight.jar $*
