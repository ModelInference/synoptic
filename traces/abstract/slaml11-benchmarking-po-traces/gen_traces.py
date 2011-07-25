'''
Description:
-----------

Generates simulator traces for experiments described below (varying
simulator parameters). Outputs the files to directories named vary-X,
for different parameters of type X.

Parameters varied:
- number of nodes in simulations
- length of trace (number of event instances in each trace)
- number of traces in the log
- number of event types


Usage:
------

$ python gen_traces.py

(Depends on simulator.py. Assumes this file is in the same directory as
this script.)
'''

import os


def clear_dir(dirname):
    '''
    Remove directories with prior simulator outputs.
    '''
    os.system("rm -Rf " + dirname)
    os.system("mkdir " + dirname)


def run_simulator(dirname, nodes, etypes, events, execs):
    '''
    Run the simulator from the command line.

    TODO: import simulator.py module and use it directly.
    '''
    fname = dirname + "/nodes-%d_etypes-%d_events-%d_execs-%d.txt" % (nodes, etypes, events, execs)
    os.system("python simulator.py %d %d %d %d > %s" % (nodes, etypes, events, execs, fname))


def main():
    '''
    Varies parameters, calling simulator for each parameter set.
    '''
    answer = None
    while answer != 'y':
        answer = raw_input("This will delete all the previously generated input files, continue? (y/n) ")

    # vary nodes:
    etypes = 10
    events = 500
    execs = 20
    dirname = "vary-nodes"
    clear_dir(dirname)
    # nodes =  5
    
    nodes = 5 # note: must have at least 2 nodes
    while (nodes != 45):
        run_simulator(dirname, nodes, etypes, events, execs)
        nodes += 5


    # vary length of trace:
    etypes = 10
    # events = 500
    execs = 20
    nodes = 10
    dirname = "vary-tracelen"
    clear_dir(dirname)
    for events in range(100,1000,100):
        run_simulator(dirname, nodes, etypes, events, execs)
        

    # vary number of traces:
    etypes = 10
    events = 500
    # execs = 40
    nodes = 10
    dirname = "vary-numtraces"
    clear_dir(dirname)

    execs = 1
    while (execs != 90):
        run_simulator(dirname, nodes, etypes, events, execs)
        if execs == 1:
            execs = 10
        else:
            execs += 10

    # vary event types:
    # etypes = 10
    events = 500
    execs = 20
    nodes = 10
    dirname = "vary-etypes"
    clear_dir(dirname)
    etypes = 4
    while etypes != 90:
        run_simulator(dirname, nodes, etypes, events, execs)
        if etypes == 4:
            etypes = 10
        else:
            etypes += 10
        
        

if __name__ == "__main__":
    main()
    
