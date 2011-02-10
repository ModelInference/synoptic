#!/bin/sh

java -ea -cp ./lib/plume.jar:./synoptic/bin/:./lib/jung/* synoptic.main.Main $*
