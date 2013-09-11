import time
import copy
import threading
import Queue


class Process(threading.Thread):
    '''
    A process with a timeout, a receive queue, an integer state value,
    and a reference to a remote endpoint.
    '''
    # Data message types:
    M0 = "m0"
    M1 = "m1"

    # Acknowledgement types:
    A0 = "a0"
    A1 = "a1"

    def __init__(self, vtime_lindex, ok_terminal_states):
        super(Process, self).__init__()
        # The current vector clock timestamp:
        self.vtime = [0,0]
        # This process' vector time index into the vtime array:
        self.vtime_lindex = vtime_lindex
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
        self.state = new_state

    def update_local_vtime(self, remote_vtime):
        '''
        Update the local vtime based on received remote vtime.
        '''
        assert self.vtime[self.vtime_lindex] >= remote_vtime[self.vtime_lindex]
        # 0->1, 1->0
        vtime_rindex = abs(self.vtime_lindex - 1)
        # No need to take the max of the two values, since in a two
        # process system the remote endpoint must have the most up to
        # date clock val.
        self.vtime[vtime_rindex] = remote_vtime[vtime_rindex]

    def log_event(self, e):
        '''
        Updates the local time and logs a timestamped event.
        '''
        self.vtime[self.vtime_lindex] += 1
        self.log.append((copy.deepcopy(self.vtime), e))

    def terminate(self):
        '''
        Tells the thread to terminate.
        '''
        self.terminate_event.set()
        
    def run(self):
        '''
        Process thread main loop.
        '''
        while True:
            if self.terminate_event.is_set() and (self.state in self.ok_terminal_states):
                return
                
            self.transition()
            time.sleep(self.sleep_time)
        return
    

