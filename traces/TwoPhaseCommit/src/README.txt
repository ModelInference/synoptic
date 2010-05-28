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
  R : number of 2pc rounds to simulate

Notes
=====

This simulator needs to be extended to handle loss and message
reordering. At the moment, it is completely deterministic.

Output
======
The output looks like the following:

0 0 send
0 0 send
0 0 send
1 0 ack
0 1 send
1 0 ack
0 1 send
1 1 ack
0 2 send

The first integer indicates whether the node is a sender (0) or a
receiver (1). The second integer indicates the message id. The last
string specifies whether the message is a 'send' or an 'ack'. Right
now the sender only generates 'send' and the receiver only generates
'ack.'

