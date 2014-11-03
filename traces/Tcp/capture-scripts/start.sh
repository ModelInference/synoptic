#!/bin/bash

# run this script with sudo!

# make sure that things we want logged do go into the log
# (into /var/log/system.log)
sysctl -w net.inet.ip.fw.verbose=1

#############################
# What goes through the pipe:

# all tcp packets with any flags:
# things_to_pipe="in tcpflags syn,fin,rst,ack"
# topipe="in tcpflags syn,fin,rst,ack"
fromServerToPipe=""

fromClientToPipe="in tcpflags syn"
# fromClientToPipe="in tcpflags syn"

#############################

#############################
# What gets dropped:

# all tcp packets with any flags:
# things_to_pipe="in tcpflags syn,fin,rst,ack"
todrop="tcpflags syn,fin,rst,ack"

# nothing:
# todrop=""
#############################

serverPort=2020
clientPort=8080

# install pipe @1 to shape traffic to/from localhost
ipfw add 200 pipe 1 log tcp from 127.0.0.1 to 127.0.0.1 src-port $serverPort $fromServerToPipe
ipfw add 250 pipe 2 log tcp from 127.0.0.1 to 127.0.0.1 src-port $clientPort $fromClientToPipe

# Configure pipe with:
# 1. delay for tcp packets with specific flags on that pipe
# 2. bandwidth doesn't matter, but set to 1Mbit/s
ipfw pipe 1 config delay 0ms bw 1Mbit/s

ipfw pipe 2 config delay 800ms bw 1Mbit/s

# what things to drop completely
# ipfw add 300 deny log tcp from 127.0.0.1 to 127.0.0.1 $todrop
# ipfw add 300 deny log tcp from 127.0.0.1 to 127.0.0.1

# at the end of the rules list : accept all traffic to/from localhost
ipfw add 400 accept log tcp from 127.0.0.1 to 127.0.0.1 src-port $serverPort
ipfw add 400 accept log tcp from 127.0.0.1 to 127.0.0.1 src-port $clientPort

# list rules we've created
ipfw list

# list pipe details
ipfw pipe 1 show

# real-time log display
tail -f /var/log/system.log



