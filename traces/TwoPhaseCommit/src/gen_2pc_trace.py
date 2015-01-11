"""
See README.txt
"""

import random
import sys

def gen_msg(mtype, src, dst, txid):
    print "%s, %s, %s, %s"%(src, dst, mtype, txid)

def perform_sim(num_nodes, num_itters):
    for txid in range(num_itters):
        for i in range(num_nodes):
            gen_msg("tx_prepare", "TM", i, txid)

        tx = True
        for i in range(num_nodes):
            mtype = random.sample(["abort", "commit"], 1)[0]
            if (mtype == "abort"):
                tx = False
            gen_msg(mtype, i, "TM", txid)

        if tx:
            mtype = "tx_commit"
        else:
            mtype = "tx_abort"
            
        for i in range(num_nodes):
            gen_msg(mtype, "TM", i, txid)


def usage(err_msg):
    print "ERROR:", err_msg
    print "USAGE: %s N R"%(sys.argv[0])
    print "\t N : number of nodes to simulate"
    print "\t R : number of protocol rounds to simulate"
    sys.exit(0)

        
if __name__ == "__main__":
    if len(sys.argv) != 3:
        usage("must specify two required arguments")

    num_nodes = int(sys.argv[1])
    if num_nodes <= 0:
        usage("number of nodes must be a positive number")

    num_itters = int(sys.argv[2])
    if num_itters <= 0:
        usage("number of nodes must be a positive numbre")

    perform_sim(num_nodes, num_itters)
        
