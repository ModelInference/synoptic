"""
See README.txt
"""

import sys
import random

# specifies whether active/relay messages always come last (True) or
# are concurrent with other messages in the round (False) for a node
# executing a round as an active node
with_active_relay_terminators = None

# global identifier to provide message uniqueness
global_msg_id = 0

# specifies whether there randomness should be excluded from the
# system -- this is set to True by omitting the S parameter in the
# args (see above)
no_random = False


# specifies whether or not we print the local node round separator
print_node_round_separator = True


# helper function to pretty print a vector clock timestamp
def tstamp_to_str(tstamp):
    return ",".join([str(t) for t in tstamp])

class Node:
    def __init__(self, mytstampindex, myid, init_tstamp, next_node):
        # our local id that potentially changes at each round
        self.myid = myid
        # this is the local id in the next round, computed when there
        # is sufficient information to update state (i.e. receive1 and
        # receive2 occurred)
        self.myid_in_next_round = None
        # our state -- active, relay, or leader
        self.state = 'active'
        # maintains id we received from neighbor
        self.recvd1 = None
        # maintains id we received from neighbor of neighbor
        self.recvd2 = None
        # vector clock
        self.tstamp = init_tstamp
        # our global id that never changes, also an index into the vector clock
        self.mytstampindex = mytstampindex
        # reference to next node in the communication ring
        self.next_node = next_node
        # current round id -- this is used to synchronize nodes and
        # perform all operations for one round before moving on to the
        # next one
        self.curr_round_id = 0
        # whether we forwarded self.recv1
        self.round_sent_myid = False
        # whether we forwarded self.myid
        self.round_sent_recvd1 = False
        # whether we updated our state in this round
        self.round_updated_state = False
        # the receive message queue for this node
        self.recv_msg_queue = []
        # initial 'active' log entry
        # self.incr_tstamp()
        # self.print_state()

    def node_print(self, stringy):
        print "%i %s"%(self.mytstampindex, stringy)
        
    def print_state(self, id_to_print=None):
        if self.state == 'relay':
            self.node_print("%s %s"%(tstamp_to_str(self.tstamp), self.state))
        else:
            if id_to_print == None:
                self.node_print("%s %s %s"%(tstamp_to_str(self.tstamp), self.state, self.myid))
            else:
                self.node_print("%s %s %s"%(tstamp_to_str(self.tstamp), self.state, id_to_print))         
        
    def incr_tstamp(self):
        self.tstamp[self.mytstampindex] += 1
        
    def update_tstamp(self, observed_tstamp):
        assert(len(observed_tstamp) == len(self.tstamp))
        for i in range(len(self.tstamp)):
            self.tstamp[i] = max(self.tstamp[i], observed_tstamp[i])

    def send_msg(self, msg):
        self.incr_tstamp()
        tstamp, mtype, payload, roundid, msg_id = msg
        self.node_print("%s send %i %i %i %i"%(tstamp_to_str(self.tstamp), mtype, roundid, payload, msg_id))
        # self.next_node.recv_msg(msg)
        self.next_node.recv_msg_queue.append(msg)
        
    def do_round_operation(self, round_id):
        """
        returns True\False if there is\isn't anything remaining to be
        done at this node for round identified by round_id

        returns 'leader'\'relay' if node is in leader\relay state
        """

        # for 'relay' nodes:
        if self.curr_round_id == -1:
            if len(self.recv_msg_queue) != 0:
                self.recv_msg(self.recv_msg_queue[0])
                self.recv_msg_queue = self.recv_msg_queue[1:]
                return False
            return True
        
        # NOTE: a 'relay' node is always stuck in its last round,
        # which keeps it from moving past this condition
        if round_id != self.curr_round_id:
            return True
        
        # leader state indicates that we are done
        if self.state == 'leader':
            return 'leader'

        def do_send1():
            # self.node_print("sending myid as send1")
            send1_msg = self.gen_msg(1, self.myid, self.curr_round_id)
            self.round_sent_myid = True
            self.send_msg(send1_msg)

        def do_send2():
            if self.recvd1 != None:
                send2_msg = self.gen_msg(2, self.recvd1, self.curr_round_id)
                self.round_sent_recvd1 = True
                self.send_msg(send2_msg)

        def do_recv():
            # recv_msg_queue is guaranteed to have a msg due to
            # invariant check below: len(self.recv_msg_queue) == 0
            self.recv_msg(self.recv_msg_queue[0])
            self.recv_msg_queue = self.recv_msg_queue[1:]

        def do_update_state():
            if self.recvd1 != None and self.recvd2 != None:
                # for next round!
                if max(self.recvd1, self.recvd2, self.myid) == self.recvd1:
                    self.myid_in_next_round = self.recvd1
                else:
                    self.state = 'relay'
                self.incr_tstamp()
                self.print_state(self.myid_in_next_round)
                self.round_updated_state = True

        potential_funcs = []

        # in this list ordering matters for no_random == True case
        preconditions_and_funcs = [(len(self.recv_msg_queue) == 0, do_recv),
                                   (self.round_sent_myid, do_send1),
                                   (self.round_sent_recvd1, do_send2)]
        
        if not with_active_relay_terminators:
            preconditions_and_funcs.append((self.round_updated_state, do_update_state))
        
        for (precondition,func) in preconditions_and_funcs:
            if not precondition:
                potential_funcs.append(func)

        if with_active_relay_terminators:
            # only allow to execute update_state transition if we have
            # completed the other two stages -- this way, state transition
            # is always last
            if len(potential_funcs) == 0 and not self.round_updated_state:
                potential_funcs.append(do_update_state)
                
        # determine which of the potential functions to execute and
        # then execute the sampled func print "sampling"
        if not no_random:
            func = random.sample(potential_funcs, 1)[0]
            func()
        else:
            for func in potential_funcs:
                func()
                # we could have transitioned to leader in one of the above
                # (actually in just do_send1() i think)
                if self.state == 'leader':
                    return 'leader'
                
            if self.round_updated_state not in potential_funcs and not self.round_updated_state:
                do_update_state()
        
        # we could have transitioned to leader in one of the above
        # (actually in just do_send1() i think)
        if self.state == 'leader':
            return 'leader'

        # update this node's curr_round_id if we have managed to
        # finish all the above functions for this round
        if self.round_sent_recvd1 and self.round_sent_myid and self.round_updated_state:
            self.round_sent_recvd1 = self.round_sent_myid = self.round_updated_state = False

            self.myid = self.myid_in_next_round

            if print_node_round_separator:
                self.node_print("%s round-done %i"%(tstamp_to_str(self.tstamp), self.curr_round_id))
            
            self.recvd1 = None
            self.recvd2 = None
            # we could have become a relay, in which case this
            # information might be used differently by the simulator,
            # so lets report it back
            if self.state == 'relay':
                self.curr_round_id = -1
                return False

            # do _not_ update round_id if we are a relay -- this is
            # what keeps us from ever performing any of the ops in
            # do_round_operation (see above first condition in this
            # function)
            self.curr_round_id += 1
            return False
        
        assert("should never reach this line 2")
        
        
    def gen_msg(self, mtype, payload, roundid):
        global global_msg_id
        assert(mtype in [1,2])
        assert(self.state != 'leader')
        global_msg_id += 1
        return [self.tstamp, mtype, payload, roundid, global_msg_id]
        

    def recv_msg(self, msg):
        assert(len(msg) == 5)
        remote_tstamp, mtype, payload, roundid, msg_id = msg
        self.incr_tstamp()
        self.update_tstamp(remote_tstamp)

        self.node_print("%s recv %i %i %i %i"%(tstamp_to_str(self.tstamp), mtype, roundid, payload, msg_id))

        if self.state == 'relay':
            fwd_msg = self.gen_msg(mtype, payload, roundid)
            self.send_msg(fwd_msg)
            return

        if mtype == 1:
            self.recvd1 = payload
            if self.myid == payload:
                # termination condition reached: i'm leader
                self.state = 'leader'
                self.incr_tstamp()
                self.print_state()
            return
            
        if mtype == 2:
            self.recvd2 = payload
            return

        assert("error: should not have reached here")

        
def perform_sim(node_count):
    # node array containing simulated Node instances
    nodes = []
    # initial node ids (per algorithm)
    init_myids = range(node_count)

    # random assignment of initial node ids
    if not no_random:
        random.shuffle(init_myids)
    
    for node_id in range(node_count):
        init_tstamp = node_count * [0]
        # we could also randomize the node_id, but that would simply be confusing
        node = Node(node_id, init_myids[node_id], init_tstamp, None)
        nodes.append(node)

    # now that we've created all the nodes, set the next_node pointer
    # in each node
    for i in range(len(nodes) - 1):
        nodes[i].next_node = nodes[i+1]
    nodes[len(nodes) - 1].next_node = nodes[0]

    # whether we have a leader yet or not
    have_leader = False
    # round id, incremented when all nodes are done in the current round
    rid = 0

    # list of nodes that are not complete yet in this round
    # nodes_not_done_in_round = []

    # initialize this list with all nodes for first round
    # for node in nodes:
    #     nodes_not_done_in_round.append(node)
    
    while True:

        redo_round = True
        while redo_round:
            redo_round = False
            # permute the order in which the nodes perform the next
            # operation in this round
            if not no_random:
                random.shuffle(nodes)
            for node in nodes:
                ret = node.do_round_operation(rid)
                if ret == 'leader':
                    # leader node found
                    return
                if ret != True:
                    redo_round = True

        #if nodes_not_done_in_round == []:
            # print "\nALL NODES DONE WITH ROUND %i\n"%(rid)
            # if all nodes are complete in this round, then we move to the next round
        rid += 1
            # in the next round we start with all the nodes again
            # TODO: could be slightly optimized by only adding nodes that are 'active'
            #for node in nodes:
            #    nodes_not_done_in_round.append(node)
    return


def usage(err_msg):
    print "ERROR:", err_msg
    print "USAGE: %s N 0|1 [S]"%(sys.argv[0])
    print "\t N : number of nodes to simulate (N > 0)"
    print "\t 0|1 : whether active/relay should come last in a round (1) or not (0)"
    print "\t [optional] S : random seed to use (S => 0)"
    print "\t                if S == 0; system seed is used"
    print "\t                if S omitted; simulation proceeds deterministically"
    print "\t note1: output is deterministic for a combination of (N,S)"
    print "\t note2: without an S parameter, the random seed is system time or other system resource"
    sys.exit(0)


if __name__ == "__main__":
    if len(sys.argv) < 3 or len(sys.argv) > 4:
        usage("wrong number of arguments")

    node_count = int(sys.argv[1])
    if node_count <= 0:
        usage("node count must be > 0")

    if int(sys.argv[2]) == 1:
        with_active_relay_terminators = True
    elif int(sys.argv[2]) == 0:
        with_active_relay_terminators = False
    else:
        usage("second argument must be 0 or 1")

    if len(sys.argv) == 4:
        rand_seed = int(sys.argv[3])
        if rand_seed < 0:
            usage("random seed must be => 0")
        elif rand_seed == 0:
            random.seed()
        else:
            random.seed(rand_seed)
    else:
        no_random = True
        
    perform_sim(node_count)

