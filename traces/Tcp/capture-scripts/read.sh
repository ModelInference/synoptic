#!/bin/bash

# $1 : intput file to read from and output to stdout

# Just to see packets with the SYN option only, add:
#   tcp[13] == 2

# To see packets with SOME option in the header, add:
#   '((tcp) and (tcp[tcpflags] & (tcp-syn|tcp-ack|tcp-fin|tcp-rst) != 0))'

# More flag options:
# via http://www.ihtb.org/security/tcpdump-explained.txt
# URG: tcpdump 'tcp[13] & 32 != 0'
# ACK: tcpdump 'tcp[13] & 16 != 0'
# PSH: tcpdump 'tcp[13] & 8 != 0'
# RST: tcpdump 'tcp[13] & 4 != 0'
# SYN: tcpdump 'tcp[13] & 2 != 0'
# FIN: tcpdump 'tcp[13] & 1 != 0'

# tcpdump -r $1 -N -S -n -ttt -vvv -K -g 
 tcpdump -r $1 -N -S -n -ttt  -K -g 