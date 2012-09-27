'''
Runs the alternating bit protocol emulator with a variety of options
and outputs the sender/receiver traces to a file.

usage: gen_abp_logs.py [-h] [-t RUNTIME] [-r RECEIVER_LOG_FILE]
                       [-s SENDER_LOG_FILE]

Run the alternating bit protocol emulator and produce logs.

optional arguments:
  -h, --help            show this help message and exit
  -t RUNTIME            Approximate desired total runtime (in seconds).
  -r RECEIVER_LOG_FILE  Receiver log filename to use.
  -s SENDER_LOG_FILE    Sender log filename to use.
'''


import time
import threading
import Queue
import argparse
import abp
import itertools


def parse_arguments():
    '''
    Parse and return the command line arguments.
    '''
    parser = argparse.ArgumentParser(description='Run the alternating bit protocol emulator and produce logs.')
    
    parser.add_argument('-t', dest='RUNTIME', default=10.0,
                        help='Approximate desired total runtime (in seconds).')

    parser.add_argument('-r', dest='RECEIVER_LOG_FILE', default="trace_r.txt",
                        help='Receiver log filename to use.')

    parser.add_argument('-s', dest='SENDER_LOG_FILE', default="trace_s.txt",
                        help='Sender log filename to use.')

    return parser.parse_args()


def append_log_to_file(log, f, include_separator):
    '''
    Appends a log to an already opened file. Prefix this with a
    separator '--' if include_separator is true.
    '''
    if include_separator:
        f.write("--\n")
        
    for event in log:
        f.write("%s\n" % event)
    return


def main():
    '''
    Parses the command line args, runs the alternating bit protocol
    emulator about 100 times to explore a variety of timeout
    combinations.
    '''
    args = parse_arguments()
    
    print "Running with args:", args

    s_log_file = open(args.SENDER_LOG_FILE, 'w')
    r_log_file = open(args.RECEIVER_LOG_FILE, 'w')

    # Explore the space of sender timeout values:
    timeout_vals = [0.01, 0.02, 0.03]
    
    # Dummy args class for abp.emulate()
    class AbpArgs:
        pass
    abp_args = AbpArgs()
    
    # Each experiment is long enough to explore all timeout pairs.
    abp_args.RUNTIME = float(args.RUNTIME) / float(len(timeout_vals))

    print "run s-timeout emulator-runtime"
    run_cnt = 0
    for s_timeout in timeout_vals:
        abp_args.SENDER_TIMEOUT = s_timeout

        print run_cnt, abp_args.SENDER_TIMEOUT, abp_args.RUNTIME

        #######################
        # Run the emulator.
        s_log, r_log = abp.emulate(abp_args)

        #######################
        # Output logs to files

        # This may happen if the runtime is set to too small a value
        # (i.e., smaller than python thread context switch time)
        if len(s_log) == 0 or len(r_log) == 0:
            continue

        append_log_to_file(s_log, s_log_file, run_cnt > 0)
        append_log_to_file(r_log, r_log_file, run_cnt > 0)
        run_cnt += 1
        
    return


if __name__ == "__main__":
    main()
