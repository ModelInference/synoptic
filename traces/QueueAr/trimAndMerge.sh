#!/bin/bash

sed "s/^.\+ c .*<init>.*/<init>/g" $1 | sed "s/^.\+ [cr] //g" | uniq > $0.out
