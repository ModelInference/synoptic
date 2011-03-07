#!/bin/sh

# 128mb heap to start with
# 6144mb heap max
java -ea -Xms128m -Xmx6144m -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./synoptic/bin/:./lib/jung/* synoptic.main.Main $*
