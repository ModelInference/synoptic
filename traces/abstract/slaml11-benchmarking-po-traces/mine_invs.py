'''
Description:
-----------

Runs the synoptic_bigheap.sh script to process input files with a
specific args file. Captures Synoptic output to files in appropriately
named directories.

NOTE: All the paths to the various files/scripts are hardcoded (see
below). Change these, for your environment.

Usage:
------

$ python mine_invs.py
'''

import os
import sys

syn_dir = None

def run_exps(exps, args_path, trace_dir, results_dir, fnames):
    '''
    Runs syoptic on all files in which a particular simulator
    parameter was varied.
    '''
    os.system("rm -Rf " + results_dir)
    os.system("mkdir " + results_dir)
    for exp in exps:
        print "-> exp " + str(exp) + "tc[..",
        sys.stdout.flush()

        # TransitiveClosure
        for fname in fnames:
            out_fname = fname + "." + str(exp) + ".tc"
            os.system("cd " + syn_dir + " && ./synoptic_bigheap.sh -c " + args_path + " " + trace_dir + fname + " --useTransitiveClosureMining=true &> " + results_dir + out_fname)

        print "done] noNCwithDAG[..",
        sys.stdout.flush()

        # DAG Walker without NeverConcurrentWith
        for fname in fnames:
            out_fname = fname + "." + str(exp) + ".noNCwithDAG"
            os.system("cd " + syn_dir + " && ./synoptic_bigheap.sh -c " + args_path + " " + trace_dir + fname + " --useTransitiveClosureMining=false --mineNeverConcurrentWithInv=false &> " + results_dir + out_fname)

        print "done] dag[..",
        sys.stdout.flush()

        # DAGWalker
        for fname in fnames:
            out_fname = fname + "." + str(exp) + ".dag"
            os.system("cd " + syn_dir + " && ./synoptic_bigheap.sh -c " + args_path + " " + trace_dir + fname + " &> " + results_dir + out_fname)
        
        print "done]"
        sys.stdout.flush()
    
def main():
    # Require user confirmation (note the 'rm -Rf' above in run_exps())
    answer = None
    while answer != 'y':
        answer = raw_input("This will delete all the previously generated results, continue? (y/n) ")

    base_dir = syn_dir + "/traces/abstract/slaml11-benchmarking-po-traces/"
    args_path = base_dir + "args.txt"
    exps = range(5)

    dirs = ["vary-nodes", 
            "vary-tracelen",
            "vary-numtraces",
            "vary-etypes"]

    trace_dirs = map(lambda d: base_dir + d + "/", dirs)
    results_dirs = map(lambda d: base_dir + d + "-results/", dirs)

    for i in range(len(trace_dirs)):
        trace_dir = trace_dirs[i]
        results_dir = results_dirs[i]
        
        print "Running on traces from: " + trace_dir
        print "Outputting results to: " + results_dir
        fnames = os.listdir(trace_dir)
        run_exps(exps, args_path, trace_dir, results_dir, fnames)
        print

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print "ERROR: provide synoptic base dir as first argument"
        sys.exit(1)
    syn_dir = sys.argv[1]
    print "Using synoptic binaries from " + syn_dir
    main()
    
