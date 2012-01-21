#!/bin/sh

# Runs SynopticJUNG from the compiled class files, passing all command
# line argument directly to main().

java -ea -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./synoptic/bin/:./synopticjung/bin/:./lib/jung/* synopticjung.SynopticJungMain --dumpInitialGraphDotFile=false --dumpInitialGraphPngFile=false $*
