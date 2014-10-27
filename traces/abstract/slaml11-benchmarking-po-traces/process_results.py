'''
Description:
-----------

Parses the Synoptic output files to construct a gnuplot file that can
plot the median values of the Synoptic running times across varying
simulator parameters.

Usage:
------

$ python process_results.py
'''

import os
import sys
import commands
import numpy
import re

# String that will go at the top of each gnuplot file. Note the "%s%
# in this string, which is a place-holder for the parameter being
# varied.
header_str = """
# each row represents a run of the simulator with the following column
# meanings:
#
# 1 %s
# 2 invariant mining time using transitive closure algorithm (ms)
# 3 invariant mining time using co-occurrence counting algorithm (ms)
# 4 invariant mining time using co-occurrence counting algorithm WITHOUT NeverConcurrentWith invariant (ms) 

"""

base_dir = "/Users/ivan/synoptic/traces/abstract/slaml11-benchmarking-po-traces/"
output_dir = "/Users/ivan/papers/Synoptic/SLAML11/data/"

def extract_var_cnts(fnames, index):
    '''
    Takes a set of filenames, and extracts the value of the simulator
    parameter indicated by offset index (arg to this function) for
    each filename. Returns an array of these values.

    For example,
    extract_var_cnts([nodes-20_etypes-10_events-500_execs-20.txt.1.tc],
    1) would return [10]

    extract_var_cnts([nodes-20_etypes-10_events-500_execs-20.txt.1.tc],
    2) would return [500]
    '''
    s = set()
    for fname in fnames:
        # Example of fname: nodes-20_etypes-10_events-500_execs-20.txt.1.tc
        var = fname.split("_")[index]
        n = ""
        for letter in var[var.index("-") + 1:]:
            if letter.isdigit():
                n += letter
            else:
                break
                
        s.add(int(n))
    return s


def main():
    dirs = [("vary-nodes", 0, 'nodes-%d_etypes-\d*_events-\d*_execs-\d*.txt.\d*'),
            ("vary-numtraces", 3, 'nodes-\d*_etypes-\d*_events-\d*_execs-%d.txt.\d*'),
            ("vary-etypes", 1, 'nodes-\d*_etypes-%d_events-\d*_execs-\d*.txt.\d*'),
            ("vary-tracelen", 2, 'nodes-\d*_etypes-\d*_events-%d_execs-\d*.txt.\d*'),
            ]

    for (d,index,base_format) in dirs:
        print "processing ", d
        result_dir = base_dir + d + "-results/"
        
        output_fname = output_dir + "latency_" + d + ".data"
        os.system("rm -f " + output_fname)
        fout = open(output_fname, 'w')
        fout.write(header_str % d)
        
        fnames = os.listdir(result_dir)
        #print result_dir

        for var in sorted(extract_var_cnts(fnames, index)):
            fout.write("%s " % var)
            tc_med_time = []
            co_occur_med_time = []
            co_occur_no_ncwith_med_time = []
            for fname in fnames:
                print fname
                tc=False
                dag=False
                dag_no_ncwith=False

                #print fname

                if re.match((base_format + ".tc") % var, fname):
                    tc=True
                elif re.match((base_format + ".dag") % var, fname):
                    dag = True
                elif re.match((base_format + ".noNCwithDAG") % var, fname):
                    dag_no_ncwith = True
                else:
                    continue

                ms_time = commands.getoutput("cat %s | grep 'Mining took' | awk '{print $4}'" % (result_dir + fname))
                t = ms_time[:-2] # drop 'ms' at end
                #print t                
                #print tc
                #print dag
                #print dag_no_ncwith

                if tc:
                    tc_med_time.append(int(t))
                if dag:
                    co_occur_med_time.append(int(t))
                if dag_no_ncwith:
                    co_occur_no_ncwith_med_time.append(int(t))

            fout.write("%s %s %s\n" % (numpy.median(tc_med_time), numpy.median(co_occur_med_time), numpy.median(co_occur_no_ncwith_med_time)))

        fout.close()

if __name__ == "__main__":
    main()
