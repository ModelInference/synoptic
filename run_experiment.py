#!/usr/bin/python

import sys
import os

vjava = "java -cp ./lib/plume.jar:./synoptic/bin/ synoptic.main.Main -v"
vprofjava = "java -cp ./lib/plume.jar:./synoptic/bin/ -prof synoptic.main.Main -v"

peterson_args="""
-r "^(?:#.*|\s*|.*round-done.*)(?<HIDE=>true)"
-r "(?<nodename>)(?<TIME>)(?<TYPE>)(?:(?<mtype>)(?:(?<roundId>)(?:(?<payload>)(?:(?<id>))?)?)?)?"
-m "\k<FILE>"
-o output/fsm-peterson
"""

peterson_args = " ".join(peterson_args.splitlines())

ppath = " traces/PetersonLeaderElection/generated_traces/peterson_trace-"

peterson_all = ppath + "n5-1-s*.txt"

peterson_one = ppath + "n5-1-s1.txt"

peterson_three = peterson_one + " "    \
               + ppath + "n5-1-s2.txt" \
               + ppath + "n5-1-s3.txt" \
               + ppath + "n5-1-s4.txt"

if "menagerie" in sys.argv:
    prefix = vjava + (" -f " if "-f" in sys.argv else "") + \
        " -r '(?<TYPE>.*)' -s '^$' -o output/menagerie-"
    path = " traces/abstract/menagerie/"
    calls = [prefix + os.path.splitext(x)[0] + path + x
             for x in os.listdir(path.strip())]
    print calls
    map(os.system, calls)
    quit()

for ar in sys.argv[1:]:
    if ar[0] == '-':
        continue
    values = { "peterson_all": peterson_args + peterson_all,
               "peterson_debug": " --debugParse=true" + peterson_args + peterson_one,
               "peterson_some": peterson_args + peterson_three,
             }
    if ar in values:
        cmd = vprofjava if "-prof" in sys.argv else vjava
        if "-f" in sys.argv:
            cmd += " -f "
        cmd += values[ar]
        print "Executing: " + cmd
        os.system(cmd);
    else:
        print ar + " not found. Available: menagerie, " + str(values.keys())
