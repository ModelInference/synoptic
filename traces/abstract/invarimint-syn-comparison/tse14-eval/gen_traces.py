'''
Description:
-----------

Generates traces for comparing declarative and procedural Synoptic algorithms.
Generates 100 log files, ranging over event types from an alphabet of 8 events,
each log contains 24 event instances.

$ python gen_traces.py
'''

import sys

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

def create_trace_etypes(dirname, total_events, etypes, i):
    fname = dirname + "/log-%d_etypes-%d-%d.txt" % (total_events, etypes, i)
    f = open(fname, 'w')
    def logEventFn(e):
        f.write(e + "\n")

    execs = total_events / 4
    for ex in range(execs):
        etype = 0
        for ev in range(total_events / execs):
            etype_s = random.randint(0, etypes)
            logEventFn("e" + str(etype_s))
        logEventFn("--")
    return
    
def main():
    # vary number of invariants:
    dirname = "vary-etypes"
    total_events = 24
    clear_dir(dirname)
    for etypes in [8]:
        for i in range(100):
            create_trace_etypes(dirname, total_events, etypes, i)

if __name__ == "__main__":
    answer = raw_input("This will delete all the previously generated trace files in, continue? (y/n) ")
    if answer != 'y':
        sys.exit(0)

    main()
