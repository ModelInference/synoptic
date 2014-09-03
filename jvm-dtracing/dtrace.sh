#!/bin/bash
# arg1 is the java class to be executed
# arg2 is the filesystem classpath of the package being traced

# These options cause the execution to pause until the vm.paused.PID
# file in the current directory is removed:
# -XX:+UnlockDiagnosticVMOptions -XX:+PauseAtStartup 

# For example, to trace through synoptic, you would run this with:
# ./dtrace.sh 'synoptic.main.SynopticMain' './lib/junit-4.9b2.jar:./lib/plume.jar:./lib/daikonizer.jar:./synoptic/bin/'

# TODO: we need a way of passing arguments to the program being
# traced. For example, we want something like this:
# java -XX:+DTraceMethodProbes -ea -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./lib/daikonizer.jar:./synoptic/bin/ synoptic.main.SynopticMain $*

java -XX:+DTraceMethodProbes -XX:+UnlockDiagnosticVMOptions -XX:+PauseAtStartup $1 &
#JAVAPID="$!"
PAUSEFILE="vm.paused.$!"
sudo dtrace -s synflow.d "\"$2\"" > DTrace.out 2> /dev/null &
#DTRACEPID="$!"
while [ ! -e $PAUSEFILE ]; do
    true
done
rm $PAUSEFILE
#wait $JAVAPID
#kill -9 $DTRACEPID
#sudo killall dtrace
