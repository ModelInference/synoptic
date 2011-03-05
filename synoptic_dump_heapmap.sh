#!/bin/sh

java -ea -XX:HeapDumpPath=./OutOfMemoryErrorHeapDump.hprof -XX:+HeapDumpOnOutOfMemoryError -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./synoptic/bin/:./lib/jung/* synoptic.main.Main $*
