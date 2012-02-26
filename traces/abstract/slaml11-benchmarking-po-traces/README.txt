Simulator, scripts, and input/output files for the SLAML'11 paper.


Scripts:
========

gen_traces.py :
 generates simulator traces for experiments described below (varying
 simulator parameters). Outputs the files to directories named vary-X,
 for different parameters of type X.

mine_invs.py :
 processes the simulator traces with Synoptic, writing the output
 files to vary-X-results, for different parameters of type X.

process_results.py :
 processes the Synoptic output files to generate files that can be
 plotted with gnuplot.

simulator.py :
 module implementing the simulator (used by gen_traces script)


Text from paper, that describes the simulator:
==============================================

"""
The simulator is parameterized by the number of hosts, number of
events types, number of events per execution, and the number of
executions.  For each event, it chooses the host that will execute the
event, and the event's type, both with uniform probability. The
simulator also decides to either associate the event with a message
send to some other random node (with probability 0.3); or if the node
has messages in its queue, to associate it with a message receipt
(with probability 0.4); or to make the event local to the selected
host (remaining probability). Any outstanding messages in the receive
queues are drained when the simulation ends.

The simulator maintains vector clocks. The simulator outputs a log of
multiple executions composed of events, each of which has a vector
timestamp.
"""


Description of the experiments from the paper:
==============================================

"""
We ran all experiments on an Intel i7 (2.8 GHz) OS X 10.6.7 machine
with 8GB RAM. 

The goal of the evaluation was to measure how the co-occurrence
counting algorithm scales, as compared to the transitive closure-based
algorithm, in four dimensions:

(1) with the length of the system trace,
(2) with the number of traces in the log,
(3) number of hosts, and
(4) the number of event types.  

For each of the dimensions, we first used the simulator to generate a
set of logs, varying that dimension and keeping the others constant.
The constant values were: 30 hosts, 50 host event types per host (=
1,500 total since event types at different hosts are considered
different), 1,000 events per execution, and 50 executions.

We ran each algorithm 5 times and report the median value.
"""
