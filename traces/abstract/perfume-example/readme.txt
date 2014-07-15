These are example traces with only 7 types of events, [a-g].  As of this
writing, Pynoptic has 4 constrained invariants: APUpper, APLower,
AFbyUpper, and AFbyLower.  To resolve all violations of these 4
invariants in the initial partition graph, at least 3 of the 4 are
needed (i.e., the partitions to-be-split for each is not completely
overlapping).  This should help in verifying that refinement for all
constrained invariants--not just one that happens to cover all of the
needed splits--is working.

To test in Eclipse, run with program arguments:
-p -o ../output/pynoptic-example -c ../traces/abstract/pynoptic-example/args.txt ../traces/abstract/pynoptic-example/traces.txt

To run in terminal, cd to root synoptic/ directory, and run:
synoptic.sh -p -o output/pynoptic-example -c traces/abstract/pynoptic-example/args.txt traces/abstract/pynoptic-example/traces.txt
