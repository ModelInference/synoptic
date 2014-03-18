#!/bin/sh

# Runs Perfume from the compiled JAR file, passing all command
# line argument directly to Perfume's main().

java -ea -cp ./lib/synoptic.jar synoptic.main.PerfumeMain $*
