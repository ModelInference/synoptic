This example contains 2 traces with 4 elements each. Partition c must be split,
while b need not be. If only partition c is split, no coarsening is needed. If
partition b is split (c will/must also be split), then b should be merged during
coarsening. The final model should contain 1 a partition, 1 b partition, 2 c
partitions, and 1 d partition.

To test in Eclipse, run with program arguments:
-p -o ../output/pynoptic-coarsening -c ../traces/abstract/pynoptic-coarsening/args.txt ../traces/abstract/pynoptic-coarsening/traces.txt

To run in terminal, cd to root synoptic/ directory, and run:
synoptic.sh -p -o output/pynoptic-coarsening -c traces/abstract/pynoptic-coarsening/args.txt traces/abstract/pynoptic-coarsening/traces.txt
