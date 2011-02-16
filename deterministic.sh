#!/bin/sh

> output/mid_branching/diff.txt

numRuns=200

# run synoptic numRuns times
for (( i = 1; i <= $numRuns ; i++ ))
do
    java -ea -cp ./lib/plume.jar:./synoptic/bin/ synoptic.main.Main -o output/mid_branching/trace$i --randomSeed=19580427 --dumpInitialGraph=false --dumpPNG=false ./traces/abstract/mid_branching/trace.txt -c ./traces/abstract/mid_branching/determineArgs.txt 
done


# diff the first run with all other runs
for (( i = 2; i <= $numRuns ; i++ ))
do
    diff output/mid_branching/trace1.dot output/mid_branching/trace$i.dot >> output/mid_branching/diff.txt
done


# if every run is not identical, diff will exist with a length greater than 0
if [ -s diff.txt ]; then
    echo "Differences found";
else
    echo "No differences found";
fi


# delete files created by this script
for (( i = 1; i <= $numRuns ; i++ ))
do
    rm output/mid_branching/trace$i.dot
done
rm output/mid_branching/diff.txt

$*
