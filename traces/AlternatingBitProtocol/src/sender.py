import time
import copy

from process import Process


class Sender(Process):
    '''
    Sender in an alternating bit protocol.
    '''
    def __init__(self, timeout):
        # Ok terminal states are just before 'send_m' is generated in gen_send()
        super(Sender, self).__init__(0, [0,3])
        # Re-send timeout.
        self.timeout = timeout
        # The message we are currently sending -- either self.M0, or self.M1
        self.currently_sending = None
        # Timestamp of the last time we've sent the message.
        self.last_send = time.time()
        # Main loop sleep time in seconds. During potential timeout
        # scenarios, we will check the message queue about 4 times
        # before timing out.
        self.sleep_time = self.timeout / 4.0
    
    def gen_send(self):
        '''
        Generate a new message (e.g., by retrieving it from a higher layer).
        '''
        self.log_event("send_m")
        trans = {0 : (self.M0, 1),
                 3 : (self.M1, 4)}
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
        self.remote_endpoint.rx_queue.put((copy.deepcopy(self.vtime), self.currently_sending))

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
        trans = {5 : {self.A0 : 5, self.A1 : 0},
                 2 : {self.A0 : 3, self.A1 : 2}}
        self.change_state(trans[self.state][rx_a])
        return

    def consume_acks(self):
        while not self.rx_queue.empty():
            (remote_vtime, rx_a) = self.rx_queue.get()
            self.update_local_vtime(remote_vtime)
            self.log_event("A?" + rx_a)

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
                (remote_vtime, rx_a) = self.rx_queue.get()
                self.update_local_vtime(remote_vtime)
                self.process_ack(rx_a)

        # POST: Make sure the new/current state is legal.
        assert self.state in range(6)
        return
    
            
