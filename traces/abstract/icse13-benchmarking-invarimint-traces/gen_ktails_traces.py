'''
Description:
-----------

Generates traces for benchmarking kTails.

Parameters varied:
- length of trace/execution (number of event instances in each trace)
- number of traces in the log
- total number of event types to choose events from

Usage:
------

$ python gen_ktails_traces.py
'''

import sys
from gen_traces import *

def get_defaults():
    '''
    Returns the default parameter values to be used for trace generation.
    '''
    etypes = 10
    events = 1000
    execs = 20
    return (etypes, events, execs)

def get_tracelen_range():
    # return range(200,2001,200)
    return range(250,2501,250)

def get_invs_range():
    # return range(200,2001,200)
    # return range(100,1001,100)
    # return range(8,20,1)
    # return [8,12,15,18,21,24,27,30,33,36]
    # return [8,12,15,18,21,24,27,30,33,36]
    # return [5,6,7,8]
    return [9,10,11]

#def get_numtraces_range():
#    # return range(10,101,10)
#    return range(20,201,20)

#def get_etypes_range():
    #return range(10,101,10)
#    return range(20,201,20)

def main():
    '''
    Varies parameters, calling simulator for each parameter set.
    '''
    # vary length of trace:

# DONEEE
#    etypes, events, execs = get_defaults()
#    dirname = "vary-tracelen"
#    clear_dir(dirname)
#    for events in get_tracelen_range():
#        create_trace(dirname, etypes, events, execs)

    # vary number of invariants:
    dirname = "vary-invs-fixed2"
    total_events = 25000
    clear_dir(dirname)
    for invs in get_invs_range():
        create_trace_invs(dirname, total_events, invs)

if __name__ == "__main__":
    answer = raw_input("This will delete all the previously generated trace files in, continue? (y/n) ")
    if answer != 'y':
        sys.exit(0)

    main()


