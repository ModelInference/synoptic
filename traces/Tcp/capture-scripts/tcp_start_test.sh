#!/bin/bash

# run this script with sudo!

# install pipe @1 to shape traffic to/from localhost
ipfw add pipe 1 ip from 127.0.0.1 to 127.0.0.1

# Configure pipe with:
# 1. delay for tcp packets with specific flags on that pipe
# 2. bandwidth doesn't matter, but set to 1Mbit/s
ipfw pipe 1 config delay 10ms bw 1Mbit/s

# allow certain traffic to/from localhost
ipfw add 2 pipe 1 all from 127.0.0.1 to 127.0.0.1 tcpflags syn,fin,rst,ack

# drop all other traffic to/from localhost
ipfw add 3 deny tcp from 127.0.0.1 to 127.0.0.1 

# list rules we've created
ipfw list

# list pipe details
ipfw pipe 1 show



