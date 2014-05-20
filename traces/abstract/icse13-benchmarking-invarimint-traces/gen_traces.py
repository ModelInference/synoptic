'''
Description:
-----------

Generates benchmarking traces for experiments described below with
varying parameters. Outputs the files to directories named vary-X, for
different parameters of type X.

Parameters varied:
- length of trace/execution (number of event instances in each trace)
- number of traces in the log
- total number of event types to choose events from

NOTES:
- event types are chosen with uniform probability
- this is a library, so this script cannot be used directly
'''

import os
import random
import sys

def clear_dir(dirname):
    '''
    Remove and re-create a directory.
    '''
    print dirname
    os.system("rm -Rf " + dirname)
    os.system("mkdir " + dirname)

def create_trace(dirname, etypes, events, execs):
    '''
    Create the parametrized trace and store it in a filename.
    '''
    fname = dirname + "/etypes-%d_events-%d_execs-%d.txt" % (etypes, events, execs)
    f = open(fname, 'w')
    def logEventFn(e):
        f.write(e + "\n")
    gen_trace(etypes, events, execs, logEventFn)
    f.close()

def gen_trace(etypes, events, execs, logEventFn):
    '''
    Generates a set of linear traces, of specific length, sampling
    from a specific number of event types. Uses logEventFn for logging
    the generated trace.
    '''
    s = ""
    # Generate execs number of traces
    for exec_ in range(execs):
        # Generate events number of event instances
        for event_ in range(events):
            # For each event instance, decide with uniform prob which
            # event type its going to be:
            etype = random.randint(0, etypes - 1)
            logEventFn("e" + str(etype))
        logEventFn("--")
    return s

################### INVS:

def create_trace_invs(dirname, total_events, invs):
    fname = dirname + "/log-%d_invs-%d.txt" % (total_events, invs)
    f = open(fname, 'w')
    def logEventFn(e):
        f.write(e + "\n")
    gen_trace_invs(total_events, invs, logEventFn)
    f.close()

def gen_trace_invs(total_events, invs, logEventFn):
    execs = 25
    for ex in range(execs):
        etype = 0
        for ev in range(total_events / execs):
            #if (etype % invs == 0):
            #    etype = 0
            etype_s = random.randint(0, invs)
            logEventFn("e" + str(etype_s))
            #etype += 1
        logEventFn("--")
    return
    
    

