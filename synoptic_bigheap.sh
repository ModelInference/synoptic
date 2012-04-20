#!/bin/sh

# Runs Synoptic from the compiled class files, passing all command
# line argument directly to main().

# Used to run Synoptic with a larger than normal maximum heap size:
#   128MB : starting heap size heap to start with
# 6,144MB : max heap size

java -ea -Xms128m -Xmx6144m -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./synoptic/bin/:./lib/jung/* synoptic.main.SynopticMain $*
