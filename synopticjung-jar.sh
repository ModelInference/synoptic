#!/bin/sh

# Runs SynopticJUNG from the compiled JAR file, passing all command
# line argument directly to main().

java -ea -jar ./lib/synopticjung.jar --dumpInitialGraphDotFile=false --dumpInitialGraphPngFile=false $*
