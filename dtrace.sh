#!/bin/bash
# arg1 is the java class to be executed
# arg2 is the filesystem classpath of the package being traced

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
