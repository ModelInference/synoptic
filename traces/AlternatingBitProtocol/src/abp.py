'''
Alternating bit protocol emulator. Implemented using two threads, that
communicate using two uni-directional synchronized Queue objects that
mimic channels.

Can be used as a library, or from the command line.

usage: abp.py [-h] [-s SENDER_TIMEOUT] [-t RUNTIME]

Emulate the alternating bit protocol.

optional arguments:
  -h, --help         show this help message and exit
  -s SENDER_TIMEOUT  Sender timeout in waiting for an ack (in seconds).
  -t RUNTIME         Total runtime (in seconds).
'''

import argparse
import time

from sender import Sender
from receiver import Receiver


def emulate(args):
    '''
    Starts the Sender/Receiver process threads, sleeps for
    args.RUNTIME, and terminates both threads. Returns a tuple of
    lists: (s_log, r_log), where s_log is sender's log and r_log is
    receiver's log.
    '''
    args.SENDER_TIMEOUT = float(args.SENDER_TIMEOUT)
    args.RUNTIME = float(args.RUNTIME)
    
    assert args.SENDER_TIMEOUT > 0
    assert args.RUNTIME > 0
    
    s = Sender(args.SENDER_TIMEOUT)
    r = Receiver()
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
