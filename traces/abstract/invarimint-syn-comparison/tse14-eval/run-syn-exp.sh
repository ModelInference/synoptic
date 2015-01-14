#!/bin/bash

# Iterates over the input logs and runs invarimint on each log.
#
# Must be run from the top level synoptic directory (where
# invarimint.sh lives).

# Create the output directory.
mkdir traces/abstract/invarimint-syn-comparison/tse14-eval/output/

for i in `ls traces/abstract/invarimint-syn-comparison/tse14-eval/logs-input/`
do
    ./invarimint.sh -c traces/abstract/invarimint-syn-comparison/tse14-eval/args.txt traces/abstract/invarimint-syn-comparison/tse14-eval/logs-input/$i --compareToStandardAlg  &> traces/abstract/invarimint-syn-comparison/tse14-eval/output/$i-out.txt
done
