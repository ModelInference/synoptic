#!/bin/sh

# Runs Perfume from the compiled class files, passing all command
# line argument directly to PerfumeMain.main().

java -Xmx5g -ea -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./lib/daikonizer.jar:./lib/json-simple-1.1.1.jar:./synoptic/bin/:./daikonizer/bin/ synoptic.main.PerfumeMain $*
