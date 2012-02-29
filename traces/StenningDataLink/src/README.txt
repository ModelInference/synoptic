Purpose
=======

This script is used to generate message traces for simulations of the
Stenning data link algorithm, which ensures reliable FIFO message
delivery in a network that has loss, duplication, and reordering.

The current simulator only handles loss.

For more information about this algorithm see:
http://books.google.com/books?id=7C7oIV48RQQC&lpg=PA693&ots=PQtqo4X4WA&dq=Stenning's%20protocol&pg=PA693#v=onepage&q=Stenning's%20protocol&f=false

Usage
=====
To run this script, call it from the command line as follows:

$ python gen_stenning_trace.py N D R S

where,
  N : number of messages to simulate
  D : drop probability for a message
  R : reordering probability for a message
  S : random seed to use (S => 0)
      if S == 0; system seed is used

Notes
=====

Reordering is currently NOT implemented. The current version of the
simulator only handles message loss, and uses a stop-and-wait
algorithm to deliver each message.

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

