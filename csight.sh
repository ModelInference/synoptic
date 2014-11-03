#!/bin/sh

# Runs CSight from the compiled class files, passing all command
# line argument directly to main().

java -ea -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./lib/daikonizer.jar:./lib/synoptic.jar:./csight/bin/ csight.main.CSightMain $*
