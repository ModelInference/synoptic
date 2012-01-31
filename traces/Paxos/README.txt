Overview
========
This directory contains traces from runs of the vanilla Paxos, a
distributed consensus algorithm, loosely described here:
http://en.wikipedia.org/wiki/Paxos_algorithm

Each run is performed by some number of proposer nodes, and some
number of acceptor nodes. Each run decides the value of some number of
ballots, which can be thought of as a sequence of slots, the value of
which is decided by Paxos.

Runs with a single proposer are straightforward -- there is a
concurrency of messages, but there is guaranteed liveness -- an upper
bound on number of messages exchanged by the proposer and the
acceptors before agreement is reached.

Runs with multiple proposers are not guranteed to be live -- they may
last forever. However, because there is randomization, eventually all
ballots are decided.


Log format
==========

Each log file contains output from a single Paxos run between P
proposers, and A acceptors that decides B ballots. The filename of the
log file captures these variables. For example
"paxos_trace-p2-a3-b1-s46.txt" means P=2, A=3, B=1, s46 indicates that
this is the 46th run in some series.

Each line in a log file has at least 5 fields. Each field is separated
by a space.  has the same format\meaning for the leading 5
fields. Fields 6+ have a format that depends on the message type. The
first 5 fields mean the following:
1: source address (ip:port format)
2: destination address (ip:port format)
3: message type (string)
4: ballot identifier (integer)
5: proposal identifier (ip:port:integer)

There are four types of messages: Prepare, Promise, Decide,
DecideRes. Prepare has 5 fields.

The message-specific fields for Promise, Decide, and DecideRes
messages have the following meanings:

Promise.6: highest known proposal identifier (ip:port:integer)
Promise.7: value of prior decision accepted (string)
Promise.8: proposal identifier of the prior decision accepted (ip:port:integer)
Promise.9: whether the prepare message is promised positively or not (True/False)

Decide.6: value to be accepted by the acceptor (string)

DecideRes.6: highest known proposal identifier (ip:port:integer)
DecideRes.7: whether or not the decision was accepted by the acceptor (True/False)
