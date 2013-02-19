#!/bin/sh

# Runs Synoptic from the compiled class files, passing all command
# line argument directly to main().

# Used for debugging OutOfMemoryErrors by dumping the heap information
# in hprof file format to OutOfMemoryErrorHeapDump.hprof

java -ea -XX:HeapDumpPath=./OutOfMemoryErrorHeapDump.hprof -XX:+HeapDumpOnOutOfMemoryError -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./lib/daikonizer.jar:./synoptic/bin/:./lib/jung/* synoptic.main.SynopticMain $*
