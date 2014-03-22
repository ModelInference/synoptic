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
import simulator


def clear_dir(dirname):
    '''
    Remove directories with prior simulator outputs.
    '''
    os.system("rm -Rf " + dirname)
    os.system("mkdir " + dirname)

def run_simulator(dirname, nodes, etypes, events, execs):
    '''
    Run the parametrized simulator and store the log of simulator
    events in a parametrized filename.
    '''
    fname = dirname + "/nodes-%d_etypes-%d_events-%d_execs-%d.txt" % (nodes, etypes, events, execs)
    f = open(fname, 'w')
    def logEventFn(e):
        f.write(e + "\n")
    # os.system("python simulator.py %d %d %d %d > %s" % (nodes, etypes, events, execs, fname))
    simulator.main(nodes, etypes, events, execs, logEventFn)
    f.close()

def get_defaults():
    '''
    Returns the default parameter values to be used for simulation.
    '''
    etypes = 50
    events = 1000
    execs = 50
    nodes = 30
    return (etypes, events, execs, nodes)

def get_node_range():
    '''
    Returns a list of node count datapoints, for each of which a
    single experiment will be generated.
    '''
    # return range(4, 41, 4)
    return range(5, 51, 5)

def get_tracelen_range():
    # return range(200,2001,200)
    return range(300,3001,300)

def get_numtraces_range():
    # return range(10,101,10)
    return range(20,201,20)

def get_etypes_range():
    #return range(10,101,10)
    return range(20,201,20)

def main():
    '''
    Varies parameters, calling simulator for each parameter set.
    '''
    # vary nodes:
    etypes, events, execs, nodes = get_defaults()
    dirname = "vary-nodes"
    clear_dir(dirname)
    for nodes in get_node_range():
        run_simulator(dirname, nodes, etypes, events, execs)

    # vary length of trace:
    etypes, events, execs, nodes = get_defaults()
    dirname = "vary-tracelen"
    clear_dir(dirname)
    for events in get_tracelen_range():
        run_simulator(dirname, nodes, etypes, events, execs)

    # vary number of traces:
    etypes, events, execs, nodes = get_defaults()
    dirname = "vary-numtraces"
    clear_dir(dirname)
    for execs in get_numtraces_range():
        run_simulator(dirname, nodes, etypes, events, execs)

    # vary event types:
    etypes, events, execs, nodes = get_defaults()
    dirname = "vary-etypes"
    clear_dir(dirname)
    for etypes in get_etypes_range():
        run_simulator(dirname, nodes, etypes, events, execs)

if __name__ == "__main__":
    answer = raw_input("This will delete all the previously generated input files, continue? (y/n) ")
    if answer != 'y':
        sys.exit(0)

    main()
