"""
See README.txt
"""

import sys
import random

def print_event(node_id, msg_id, event_txt):
    print "%i %i %s"%(node_id, msg_id, event_txt)    

class Sender:
    def __init__(self, receiver, drop_p, reorder_p):
        self.receiver = receiver
        self.drop_p = drop_p
        self.reorder_p = reorder_p
        
        self.curr_msg_id = 0
        self.num_acked = 0
        self.my_node_id = 0
        self.recv_msg_queue = []
        self.sent_first = False

    def send(self):
        print_event(self.my_node_id, self.curr_msg_id, 'send')
        drop = (random.random() < drop_p)
        reorder = (random.random() < reorder_p)
        if drop:
            # print "dropping"
            pass
        else:
            self.receiver.recv_msg_queue.append(self.curr_msg_id)
            
        # TODO: reorder with windows > 1
        
    def take_one_step(self):
        if len(self.recv_msg_queue) != 0:
                
            #for msg in recv_msg_queue:
            assert(len(self.recv_msg_queue) == 1)
            msg = self.recv_msg_queue[0]
            self.recv_msg_queue = self.recv_msg_queue[1:]

            if not self.sent_first:
                assert(int(msg) == self.curr_msg_id)
                self.sent_first = True

            if (int(msg) == self.curr_msg_id):
                self.curr_msg_id += 1
                # first send
                self.send()
            elif int(msg) == self.curr_msg_id - 1:
                # resend
                self.send()
            else:
                print "ERROR"
                sys.exit(0)
                
        if not self.sent_first:
            self.send()

class Receiver:
    def __init__(self, sender, reack_thresh):
        self.sender = sender
        self.last_recvd = -1
        self.reack_thresh = reack_thresh
        self.t_since_ack = 0
        self.my_node_id = 1
        self.recv_msg_queue = []
        self.recvd_first = False

    def send(self):
        self.sender.recv_msg_queue.append(self.last_recvd)
        print_event(self.my_node_id, self.last_recvd, 'ack')
        
    def take_one_step(self):
        if len(self.recv_msg_queue) != 0:
            #for msg in recv_msg_queue:
            assert(len(self.recv_msg_queue) == 1)
            msg = self.recv_msg_queue[0]
            self.recv_msg_queue = self.recv_msg_queue[1:]
            assert(int(msg) == self.last_recvd + 1)
            self.last_recvd += 1
            self.t_since_ack = 0
            # first ack
            self.send()
            return

        if self.last_recvd == -1:
            # only accumulate reack time if we have something to ack
            return
        
        if self.t_since_ack > self.reack_thresh:
            # re-ack
            self.send()
            self.t_since_ack = 0
            return
            
        self.t_since_ack += 1



def perform_sim(num_msgs, drop_p, reorder_p):
    reack_thresh = 3
    s = Sender(None, drop_p, reorder_p)
    r = Receiver(s, reack_thresh)
    s.receiver = r
    while (s.curr_msg_id != num_msgs):
        s.take_one_step()
        r.take_one_step()
    return
    

def usage(err_msg):
    print "ERROR:", err_msg
    print "USAGE: %s N D R S"%(sys.argv[0])
    print "\t N : number of messages to simulate"
    print "\t D : drop probability for a message"
    print "\t R : reordering probability for a message"
    print "\t S : random seed to use (S => 0)"
    print "\t                if S == 0; system seed is used"
    sys.exit(0)

if __name__ == "__main__":
    if len(sys.argv) != 5:
        usage("wrong number of arguments")

    num_msgs = int(sys.argv[1])
    if num_msgs <= 0:
        usage("number of messages must be > 0")

    drop_p = float(sys.argv[2])
    if drop_p < 0 or drop_p > 1:
        usage("drop probability must be => 0 and < 1")

    reorder_p = float(sys.argv[3])
    if reorder_p < 0 or reorder_p > 1:
        usage("reordering probability must be => 0 and < 1")

    rand_seed = int(sys.argv[4])
    if rand_seed < 0:
        usage("random seed must be => 0")
    elif rand_seed == 0:
        random.seed()
        
    perform_sim(num_msgs, drop_p, reorder_p)
