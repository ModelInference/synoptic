import time
import copy
from process import Process


class Receiver(Process):
    '''
    Receiver in an alternating bit protocol.
    '''
    def __init__(self):
        # Ok terminal states are just after an ack was generated
        super(Receiver, self).__init__(1, [0,3])
        # The ack we are currently sending -- either self.A0, or self.A1
        self.currently_sending = None
        # Timestamp of the last time we've sent the ack.
        self.last_send = time.time()
        # Main loop sleep time in seconds.
        # TODO: hardcoded constant.
        self.sleep_time = .005

    def process_message(self, rx_m):
        '''
        Process a received message/data packet.
        '''
        self.log_event("M?" + rx_m)
        trans = {0 : {self.M0 : 1, self.M1 : 5},
                 3 : {self.M0 : 2, self.M1 : 4}}
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
        trans = {2 : (self.A0, 3),
                 5 : (self.A1, 0)}
        self.currently_sending, new_state = trans[self.state]
        self.change_state(new_state)

        self.log_event("A!" + self.currently_sending)
        self.remote_endpoint.rx_queue.put((copy.deepcopy(self.vtime), self.currently_sending))

    def transition(self):
        '''
        Determine what state we are currently in, and depending on
        this state, and possibly the message channel, determine what
        to do next.
        '''
        # PRE: Make sure the old/current state is legal.
        assert self.state in range(6)
        
        if self.state in [0,3]:
            if not self.rx_queue.empty():
                # We have a data packet to receive. Receive it, and process it.
                (remote_vtime, rx_m) = self.rx_queue.get()
                self.update_local_vtime(remote_vtime)
                self.process_message(rx_m)
                
        elif self.state in [1,4]:
            self.do_recv()

        elif self.state in [2,5]:
            self.do_ack()

        # POST: Make sure the new/current state is legal.
        assert self.state in range(6)
        return

    
