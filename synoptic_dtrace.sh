#!/bin/sh

# Runs Synoptic from the compiled class files, passing all command
# line argument directly to main().

# These options cause the execution to pause until the vm.paused.PID
# file in the current directory is removed:
# -XX:+UnlockDiagnosticVMOptions -XX:+PauseAtStartup 

# Enable dtrace method probe injection.
java -XX:+DTraceMethodProbes -ea -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./synoptic/bin/ synoptic.main.Main $*
