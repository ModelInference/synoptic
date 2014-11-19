#!/bin/bash

# run this script with sudo!

# remove firewall logging
sysctl -w net.inet.ip.fw.verbose=0

# delete all the rules
ipfw flush

# delete all pipes
ipfw pipe flush


