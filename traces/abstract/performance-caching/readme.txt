This is a log conveying a system where one execution reads data from a
database twice, knowing that the database implements a cache.
Unfortunately, there is a bug in the cache mechanism: Looking up an item
that happens to be in the cache removes it from the cache. What we
expect to see is either a fast execution (both queries hit the cache),
or a medium speed execution (first query misses and seconds hits).  But
what we actually always get are either the first misses and the second
hits, or the first hits and the second misses.  Running pynoptic on this
log should produce a model that makes this bug apparent.

To test in Eclipse, run with program arguments:
-p -o ../output/performance-caching -c ../traces/abstract/performance-caching/args.txt ../traces/abstract/performance-caching/traces.txt

To run in terminal, cd to root synoptic/ directory, and run:
synoptic.sh -p -o output/performance-caching -c traces/abstract/performance-caching/args.txt traces/abstract/performance-caching/traces.txt
