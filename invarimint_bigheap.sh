#!/bin/sh

# Runs InvariMint from the compiled class files, passing all command
# line argument directly to main().

# Used to run InvariMint with a larger than normal maximum heap size:
#   128MB : starting heap size heap to start with
# 6,144MB : max heap size

java -ea -Xms128m -Xmx6144m -cp ./lib/junit-4.9b2.jar:./lib/automaton.jar:./lib/plume.jar:./lib/synoptic.jar:./InvariMint/bin/ main.InvariMintMain $*
