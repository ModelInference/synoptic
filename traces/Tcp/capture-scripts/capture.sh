#!/bin/bash

# $1 : output file for the captured trace
sudo tcpdump -i lo0 -w $1 '((tcp) and (host 127.0.0.1) and (port 8080))' 

