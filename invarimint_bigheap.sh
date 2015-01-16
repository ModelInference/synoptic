#!/bin/sh

# Runs InvariMint from the compiled class files, passing all command
# line argument directly to main().

# Used to run InvariMint with a larger than normal maximum heap size:
#   128MB : starting heap size heap to start with
# 6,144MB : max heap size

java -ea -Xms128m -Xmx6144m -cp "./synoptic/bin/:./InvariMint/bin/:./daikonizer/bin/:./lib/*" main.InvariMintMain $*
