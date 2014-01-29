# -*- coding: utf-8 -*-
import re
import sys

# expressions that we mine from the trace files
exprs = [
    "Connecting to tr_server",
    "Issuing traceroutes",
    "Connecting to controller",
    "Tring to find .+ hops from",
    "Checking .+ for traceroutes to source",
    "Initializing RR VPs",
    "Connecting to vp_server",
    "using non_spoofed",
    "Issuing recordroutes",
    "RR VPs left to try",
    "FOUND RECENT SPOOFER TO USE",
    "Issuing spoofed recordroutes",
    "Current hop .+ seems to be unresponsive",
    "Connecting to adjacency_server",
    "No adjacents to",
    "Adjacents for .+ are",
    "Issuing timestamps",
#    "TS probe is non_spoofed .+ no reverse hop found",
#    "TS probe is non_spoofed .+ reverse hop!",
    "Checking .+ for traceroutes to source",
    "TS probe is .+ no reverse hop for dst that stamps 0",
    "TS probe is .+ dst does not stamp, but spoofer .+ got a stamp",
    "Issuing to verify for dest does not stamp",
    "Issuing spoofed_timestamps",
    "Issuing for dest does not stamp",
    "Current hop .+ seems to be TS unresponsive",
    "TS adjacents left to try",
    "Current hop .+ stamps 0, so not asking for stamp",
#   "Backing off along current path",
    "Rev Seg is",
#    "NONE",
#    "FAILED",
#    "FOUND",
    "REACHED",
]

partition_exprs = [
    "--------",
   ".+RevPath.*"
    ]

def parse(lines, compiled_res, compiled_res_parts):
    i = -1
    while i < len(lines) - 1:
        i += 1
        line = lines[i]
        found = False
        for expr,prog in compiled_res.items():
            if prog.match(line):
                print expr.replace(".*", "")
                found = True
                break

        if found:
            continue
        
        items = compiled_res_parts
        at_end = False
        parsed_end = False

        # hacky
        for j in range(len(items) - 1):
            (expr, prog) = items[j]
            if prog.match(line):
                at_end = True
                (expr2, prog2) = items[j+1]
                line2 = lines[i+1]
                if not prog2.match(line2):
                    print expr.replace(".*", "")
                    parsed_end = True
                    
                    while "--------" not in line:
                        i+=1
                        line = lines[i]

                    break

                i += 1
                line = lines[i]

        if at_end and not parsed_end:
            # print "line is :" , line
            print "PARSE_ERR"
    print

        
def compile_exprs_re():
    compiled_res = {}
    compiled_res_parts = []
    
    for expr in exprs:
        expr = ".*%s.*"%(expr)
        prog = re.compile(expr)
        compiled_res[expr] = prog

    # this one has to be in order
    for expr in partition_exprs:
        expr = ".*%s.*"%(expr)
        prog = re.compile(expr)
        compiled_res_parts.append((expr,prog))
        
    return compiled_res,compiled_res_parts


if __name__ == "__main__":
    # takes one argument: the reverse traceroute file to parse
    if len(sys.argv) != 2:
        print "Usage: %s [revtr.err]"%(sys.argv[0])
        sys.exit(0)

    f = open(sys.argv[1])
    lines = f.readlines()
    # compile the regular expressions
    compiled_res,compiled_res_parts = compile_exprs_re()
    # parse the file
    parse(lines, compiled_res, compiled_res_parts)
