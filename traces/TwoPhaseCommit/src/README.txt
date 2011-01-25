Purpose
=======

This script is used to generate message traces for simulations of the
Two Phase Commit distributed protocol.

For more information about this algorithm see:
http://en.wikipedia.org/wiki/Two-phase_commit_protocol

Usage
=====
To run this script, call it from the command line as follows:

$ python gen_2pc_trace.py N R

where,
  N : number of nodes to simulate
  R : number of protocol rounds to simulate

Notes
=====

This simulator needs to be extended to handle loss and message
reordering. At the moment, it is completely deterministic.

Output
======
The output looks like the following:

TM, 0, tx_prepare, 0
TM, 1, tx_prepare, 0
TM, 2, tx_prepare, 0

The fields are:
1: sender of the message
2: receiver of the messages
3: message type
4: transaction id

