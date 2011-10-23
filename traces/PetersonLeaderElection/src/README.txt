Purpose
=======
This script is used to generate message traces for simulations of the
Peterson election algorithm, which solves leader election in an
asynchronous uni-directional ring network. For more information on
this algorithm, see: http://pine.cs.yale.edu/pinewiki/LeaderElection

Usage
=====
To run this script, call it from the command line as follows:

$ python gen_peterson_trace.py N 0|1 [S]

Where N is the total number of nodes to simulate (N>0), and S is the
random seed to use (S>0). Both are integers. The output is
_completely_ deterministic for a combination of (N,S).

0|1 is whether active/relay should come last in a round (1) or not (0)

S is optional, without an S parameter there is _no_ randomness in the
system! In fact, without an S parameter nodes are completely
deterministic, and the order in which nodes process messages is
completely deterministic. A random seed of 0 implies that a seed
corresponding to the system time or other system resource should be
used.

(NOTE: when no seed is given the 0|1 argument is ineffective -- the
system uses the deterministic choice of 1 regardless of the argument.)

S is used to control:

- The assignment of initial ids to nodes

- The order in which nodes execute their operations in any single
  round (e.g. node 3 then node 2, ..)

- The order of operations on a single node: sending first id, sending
  second id, changing state.

A python version 2 and higher should be acceptable. Python 3 is not
supported.


Notes
=====
The simulator runs nodes concurrently, and can be configured to
randomize the decision making process at each node for further
concurrency (e.g. to send its id before receiving a neighbor's
id). However, the simulator does _not_ overlap rounds of the
algorithm. Rounds could be overlapped, but right now the simulator
completes a single round on all nodes before moving to the next
round. Therefore, the simulator is _not_ fully asynchronous.

Output
======
The process traces are outputed to stdout. The format of a line in
this output is as follows:

- Each line begins with the unique and persistent node index. Think of
  these as node labels -- 0 ... N-1 where N is the input to the script
  for the number of nodes to have in the system. Note, these indeces
  are _not_ the same thing as the node identifier in the Peterson
  algorithm (see below for these).

- Each line then contains a vector clock that looks like
  '1,0,0,0,0'. The number of digits in the vector clock corresponds to
  the total number of processes (N) in the system. For this example
  N is 5.

- The vector clock is followed by one of the following string codes:
  {relay, active, send, recv}

  Of these:
  - 'relay' is never followed by any argument. The process is now
    acting as a relay process

  - 'active' is always follows by exactly one integer argument X,
  which represents a node uid. The process remains active in the next
  round and adopts X as its id

  - 'recv' and 'send' are always followed by 4 integers: mtype,
    roundid, payload, msg_id

    Here is an explanation of what these integers mean:

    mtype : whether this is a send1/send2 or recv1/recv2 in parlance
    of previous format. That is:

       send1 X : the process is sending its id to clockwise neighbor, or forwarding a previous recv1 it received
       recv1 X : the process received id of its counter-clockwise neighbor
       send2 X : the process is sending id of its counter-clockwise neighbor, or forwarding a previous recv2 it received
       recv2 X : the process received id of the counter-clockwise neighbor of its counter-clockwise neighbor  
    
    roundid : the roundid of the sender of the msg

    payload : the node uid in the message
    
    msg_id : global unique identifier for a message
