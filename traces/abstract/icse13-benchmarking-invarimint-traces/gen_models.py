'''
Description:
-----------

Runs the invarimint_bigheap.sh script to process input files with a
specific args file. Captures Invarimint output to files in appropriately
named directories.

NOTE: All the paths to the various files/scripts are hardcoded (see
below). Change these, for your environment.

Usage:
------

$ python gen_models.py
'''

import os
import sys

syn_dir = None

def run_exps(exps, args_path, trace_dir, results_dir, fnames):
    '''
    Runs invarimint on all files in which a parameter was varied.
    '''
    os.system("rm -Rf " + results_dir)
    os.system("mkdir " + results_dir)
    for exp in exps:
        print "-> exp " + str(exp) + "ktails[..",
        sys.stdout.flush()

        for fname in fnames:
            print "\t" + fname + " ....",
            sys.stdout.flush()
            out_fname = fname + "." + str(exp) + ".ktails"
            os.system("cd " + syn_dir + " && ./invarimint_bigheap.sh -c " + args_path + " " + trace_dir + fname + " &> " + results_dir + out_fname)
            print "done"
            sys.stdout.flush()
        sys.stdout.flush()
    
def main():
    # Require user confirmation (note the 'rm -Rf' above in run_exps())
    answer = None
    while answer != 'y':
        answer = raw_input("This will delete all the previously generated results, continue? (y/n) ")

    base_dir = syn_dir + "/traces/abstract/icse13-benchmarking-invarimint-traces/"
#    args_path = base_dir + "args.txt"
    args_path = base_dir + "args_synoptic.txt"
    # exps = range(3)
    exps = [0]

    #dirs = ["vary-tracelen",
     #       "vary-numtraces",
      #      "vary-etypes"]
    # dirs = ["vary-tracelen"]
#    dirs = ["vary-tracelen"]
    dirs = ["vary-invs-fixed2"]

    trace_dirs = map(lambda d: base_dir + d + "/", dirs)
    results_dirs = map(lambda d: base_dir + d + "-syn-results/", dirs)

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
        print "ERROR: provide invarimint base dir as first argument"
        sys.exit(1)
    syn_dir = sys.argv[1]
    print "Using invarimint sh script from " + syn_dir
    main()
    
