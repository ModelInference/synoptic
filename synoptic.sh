#!/bin/sh

# Runs Synoptic from the compiled class files, passing all command
# line argument directly to main().

java -ea -cp ./lib/*:./synoptic/bin/:./daikonizer/bin/ synoptic.main.SynopticMain $*
