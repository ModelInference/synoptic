#!/bin/sh

java -ea -cp ./lib/junit-4.9b2.jar:./lib/plume.jar:./synoptic/bin/:./lib/jung/* synoptic.main.Main $*
