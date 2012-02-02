'''
Alternating bit protocol emulator. Implemented using two threads, that
communicate using two uni-directional synchronized Queue objects that
mimic channels.

Can be used as a library, or from the command line.

usage: abp.py [-h] [-s SENDER_TIMEOUT] [-r RECEIVER_TIMEOUT] [-t RUNTIME]

Emulate the alternating bit protocol.

optional arguments:
  -h, --help           show this help message and exit
  -s SENDER_TIMEOUT    Sender timeout in waiting for an ack (in seconds).
  -r RECEIVER_TIMEOUT  Sender timeout in waiting for a message (in seconds).
  -t RUNTIME           Total runtime (in seconds).
'''


import time
import threading
import Queue
import argparse


# Data message types:
M0 = "m0"
M1 = "m1"

# Acknowledgement types:
A0 = "a0"
A1 = "a1"


class Process(threading.Thread):
    '''
    A process with a timeout, a receive queue, an integer state value,
    and a reference to a remote endpoint.
    '''
    def __init__(self, vtime_lindex, timeout, ok_terminal_states):
        super(Process, self).__init__()
        self.vtime = [0,0]
        self.vtime_lindex = vtime_lindex
        self.timeout = timeout
        self.rx_queue = Queue.Queue()
        self.remote_endpoint = None
        self.change_state(0)
        self.terminate_event = threading.Event()
        self.terminate_event.clear()
        self.ok_terminal_states = ok_terminal_states
        self.log = []

    def set_remote_endpoint(self, remote_endpoint):
        '''
        Sets a reference to a remote endpoint.
        '''
        self.remote_endpoint = remote_endpoint

    def change_state(self, new_state):
        '''
        Change the state of the process.
        '''
        self.vtime[self.vtime_lindex] = self.vtime[self.vtime_lindex] + 1
        self.state = new_state

    def update_local_vtime(self, remote_vtime):
        '''
        Update the local vtime based on received remote vtime.
        '''
        assert self.vtime[self.vtime_lindex] >= remote_vtime[self.vtime_lindex]
        # 0->1, 1->0
        vtime_rindex = abs(self.vtime_lindex - 1)
        # No need to take the max of the two values, since the remote
        # endpoint must have the most up to date clock val.
        self.vtime[vtime_rindex] = remote_vtime[vtime_rindex]

    def log_event(self, e):
        '''
        Logs an event.
        '''
        self.log.append(e)

    def terminate(self):
        '''
        Tells the thread to terminate.
        '''
        self.terminate_event.set()
        
    def run(self):
        '''
        Process thread main loop.
        '''
        # Sleep time in seconds. During potential timeout scenarios,
        # we will check the message queue about 4 times before timing
        # out.
        sleep_time = self.timeout / 4.0
        while True:
            if self.terminate_event.is_set() and (self.state in self.ok_terminal_states):
                return
                
            self.transition()
            time.sleep(sleep_time)
        return
    

class Sender(Process):
    '''
    Sender in an alternating bit protocol.
    '''
    def __init__(self, timeout):
        # Ok terminal states [0,1] are just before 'send_m' is generated in gen_send()
        super(Sender, self).__init__(0, timeout, [0,3])
        # The message we are currently sending -- either M0, or M1
        self.currently_sending = None
        # Timestamp of the last time we've sent the message.
        self.last_send = time.time()
    
    def gen_send(self):
        '''
        Generate a new message (e.g., by retrieving it from a higher layer).
        '''
        self.log_event("send_m")
        trans = {0 : (M0, 1),
                 3 : (M1, 4)}
        self.currently_sending, new_state = trans[self.state]
        self.change_state(new_state)

    def do_send(self):
        '''
        Send the currently_sending message to the receiver.
        '''
        # NOTE: An optimization extension that we can add here is to,
        # peek at the front of the queue prior to sending and dequeue
        # and discard a continugous sequence of 'bad acks' -- acks
        # that are not pertinent to the current state.

        self.last_send = time.time()
        self.log_event("M!" + self.currently_sending)
        trans = {1 : 2,
                 4 : 5}
        self.change_state(trans[self.state])
        self.remote_endpoint.rx_queue.put(self.currently_sending)

    def test_timeout(self):
        '''
        Decide whether or not to timeout.
        '''
        if ((time.time() - self.last_send) > self.timeout):
            self.log_event("timeout")
            trans = {5 : 4,
                     2 : 1}
            self.change_state(trans[self.state])
            
        return

    def process_ack(self, rx_a):
        '''
        Process a received acknowledgement.
        '''
        self.log_event("A?" + rx_a)
        trans = {5 : {A0 : 4, A1 : 0},
                 2 : {A0 : 3, A1 : 1}}
        self.change_state(trans[self.state][rx_a])
        return

    def transition(self):
        '''
        Determine what state we are currently in, and depending on
        this state, and possibly the ack channel, determine what to do
        next.
        '''
        # PRE: Make sure the old/current state is legal.
        assert self.state in range(6)
        
        if self.state in [0,3]:
            self.gen_send()
        elif self.state in [1,4]:
            self.do_send()
        elif self.state in [2,5]:
            if self.rx_queue.empty():
                self.test_timeout()
            else:
                # We have an ack to receive. Receive it, and process it.
                rx_a = self.rx_queue.get()
                self.process_ack(rx_a)

        # POST: Make sure the new/current state is legal.
        assert self.state in range(6)
        return
    
            
class Receiver(Process):
    '''
    Receiver in an alternating bit protocol.
    '''
    def __init__(self, timeout):
        # Ok terminal states [0,2] are just after 'recv_m' is generated
        super(Receiver, self).__init__(1, timeout, [0,3])
        # The ack we are currently sending -- either A0, or A1
        self.currently_sending = None
        # Timestamp of the last time we've sent the ack.
        self.last_send = time.time()

    def test_timeout(self):
        '''
        Decide whether or not to timeout.
        '''
        if ((time.time() - self.last_send) > self.timeout):
            self.log_event("timeout")
            trans = {0 : 5,
                     3 : 2}
            self.change_state(trans[self.state])
            
        return

    def process_message(self, rx_m):
        '''
        Process a received message/data packet.
        '''
        self.log_event("M?" + rx_m)
        trans = {0 : {M0 : 1, M1 : 5},
                 3 : {M0 : 2, M1 : 4}}
        self.change_state(trans[self.state][rx_m])
        return

    def do_recv(self):
        '''
        Consume a received message (e.g., by passing it to a higher layer).
        '''
        self.log_event("recv_m")
        trans = {1 : 2,
                 4 : 5}
        self.change_state(trans[self.state])

    def do_ack(self):
        '''
        Generate an ack for a received message.
        '''
        trans = {2 : (A0, 3),
                 5 : (A1, 0)}
        self.currently_sending, new_state = trans[self.state]
        self.change_state(new_state)

        self.log_event("A!" + self.currently_sending)
        self.remote_endpoint.rx_queue.put(self.currently_sending)

    def transition(self):
        '''
        Determine what state we are currently in, and depending on
        this state, and possibly the message channel, determine what
        to do next.
        '''
        # PRE: Make sure the old/current state is legal.
        assert self.state in range(6)
        
        if self.state in [0,3]:
            if self.rx_queue.empty():
                self.test_timeout()
            else:
                # We have a data packet to receive. Receive it, and process it.
                rx_m = self.rx_queue.get()
                self.process_message(rx_m)
                
        elif self.state in [1,4]:
            self.do_recv()

        elif self.state in [2,5]:
            self.do_ack()

        # POST: Make sure the new/current state is legal.
        assert self.state in range(6)
        return

    
def emulate(args):
    '''
    Starts the Sender/Receiver process threads, sleeps for
    args.RUNTIME, and terminates both threads. Returns a tuple of
    lists: (s_log, r_log), where s_log is sender's log and r_log is
    receiver's log.
    '''
    args.SENDER_TIMEOUT = float(args.SENDER_TIMEOUT)
    args.RECEIVER_TIMEOUT = float(args.RECEIVER_TIMEOUT)
    args.RUNTIME = float(args.RUNTIME)
    
    assert args.SENDER_TIMEOUT > 0
    assert args.RECEIVER_TIMEOUT > 0
    assert args.RUNTIME > 0
    
    s = Sender(args.SENDER_TIMEOUT)
    r = Receiver(args.RECEIVER_TIMEOUT)
    s.set_remote_endpoint(r)
    r.set_remote_endpoint(s)

    r.daemon = True
    s.daemon = True

    # Start the sender process.
    s.start()
    r.start()

    try:
        time.sleep(args.RUNTIME)
    except KeyboardInterrupt:
        print "Interrupted, terminating."

    # We have to be careful with terminating the two threads, as they
    # can only exit in specific states, and we can cause a deadlock.
    # First, we terminate the sender, and wait for it to finish. Once
    # this happens, we know that the receiver is in an ok terminal
    # state, so we terminate it right after.
    s.terminate()
    s.join()

    r.terminate()
    r.join()

    return (s.log, r.log)



############################################################################
# Command line invocation


def parse_arguments():
    '''
    Parse and return the command line arguments.
    '''
    parser = argparse.ArgumentParser(description='Emulate the alternating bit protocol.')
    
    parser.add_argument('-s', dest='SENDER_TIMEOUT', default=0.5,
                        help='Sender timeout in waiting for an ack (in seconds).')

    parser.add_argument('-r', dest='RECEIVER_TIMEOUT', default = 1.0,
                        help='Sender timeout in waiting for a message (in seconds).')

    parser.add_argument('-t', dest='RUNTIME', default=10,
                        help='Total runtime (in seconds).')

    return parser.parse_args()


def main():
    '''
    Parse the command line args, run the emulator, and output
    sender/receiver logs to stdout.
    '''
    args = parse_arguments()

    print "Running with args:", args
    s_log, r_log = emulate(args)

    print "# Sender's log:"
    for event in s_log:
        print event

    print
    print "# Receiver's log:"
    for event in r_log:
        print event
    print


if __name__ == "__main__":
    main()
