'''
Description:
-----------

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

- Prints the generated executions to stdout.
- Uses execution separator "--"
- Uses format "%d,%d,%d,..,%d %s" for log lines where first field is
  vector timestamp and second field is an event string


Usage:
------

$ python simulator.py #hosts #e-types #events #executions
'''

import sys
import random
import copy

# number of hosts
NumHosts = 5

# total number of events to generate in this execution
TotalEvents = 10

# number of different event types
NumEventTypes = 10

# probability that a node sends a message
P_send = 0.3

# probability that a node will receive a message if it has something queued
P_recv = 0.4

# Etypes = [e0, e1, ... , eNumEventTypes-1]
Etypes = []
    
class Process:
    def __init__(self, index):
        self.index = index
        # initialize vtime to an array of 0s
        self.vtime = [0] * NumHosts
        self.queue = []

    def gen_local_event(self):
        e = random.choice(Etypes)
        self.vtime[self.index] += 1
        print "%s %s" % (str(self.vtime).replace(' ', '')[1:-1], e)

    def recv_msg(self):
        if len(self.queue) == 0:
            print "ERROR"
            sys.exit(1)

        other_vtime = self.queue.pop()
        # update local vtime with other vtime
        for i in range(NumHosts):
            self.vtime[i] = max(self.vtime[i], other_vtime[i])
        # generate the msg as the local receive event
        self.gen_local_event()

    def enqueue_msg(self, vtime):
        self.queue.insert(0,vtime)

    def have_msgs_to_recv(self):
        if len(self.queue) > 0:
            return True
        return False


def main():
    '''
    Simulator driver: iterates between different nodes and does one of:

    1) send a message from one node to another with probability X
    2) receive a message at a node if the node has one in its queue
    3) generates a local event at the node
    '''

    # executions generation loop
    num_executions = 0

    while (num_executions != NumExecutions):
        num_executions += 1

        # total of msgs generated so far
        num_events_generated = 0

        # create all the processes
        processes = {}
        for p in range(NumHosts):
            processes[p] = Process(p)

        # main event generation loops
        while (num_events_generated < TotalEvents):
            # choose a node at random
            n = random.choice(xrange(NumHosts))
            node = processes[n]

            action = random.random()
            if (action < P_send):
                # choose a recipient of message at random
                r = n
                while (r == n):
                    r = random.choice(xrange(NumHosts))
                # generate a local 'send' event at the sender
                node.gen_local_event()
                # enqueue the message into the recipient's queue
                processes[r].enqueue_msg(copy.copy(node.vtime))
                # we generated two events -- one send and one receive
                num_events_generated += 2
            else:
                if node.have_msgs_to_recv() and action < P_send + P_recv:
                    # receive a message
                    node.recv_msg()
                    continue
                # otherwise generate a local event
                node.gen_local_event()
                num_events_generated += 1
        

        # now we drain all the node queues that may be outstanding in a
        # random node order
        nodes = range(NumHosts)
        random.shuffle(nodes)
        for n in nodes:
            while processes[n].have_msgs_to_recv():
                processes[n].recv_msg()
    
        # print the execution separator
        if (num_executions != NumExecutions):
            print "--"

if __name__ == "__main__":
    assert len(sys.argv) == 5
    NumHosts      = int(sys.argv[1])
    NumEventTypes = int(sys.argv[2])
    TotalEvents   = int(sys.argv[3])
    NumExecutions = int(sys.argv[4])
    assert NumHosts > 0 and TotalEvents > 0 and NumEventTypes > 0 and NumExecutions > 0
    print "# Simulation with #hosts[%d] #e-types[%d] #events[%d] #executions[%d]" \
          % (NumHosts, NumEventTypes, TotalEvents, NumExecutions)
 
    Etypes = map(lambda x : "e" + str(x), range(NumEventTypes))
    main()
