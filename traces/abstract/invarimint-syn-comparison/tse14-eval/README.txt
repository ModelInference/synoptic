Scripts and files for the TSE'14 InvariMint paper evaluation that
compares declarative and procedural Synoptic algorithms.

Important notes:
================

1. The InvariMint project includes a static boolean variable
   InvariMintSynoptic.tseEval that must be set to true to output
   procedural/declarative Synoptic comparison metric values.

2. The variable strLenBound in the
   InvariMintSynoptic.compareToStandardAlg method is by default set to
   7. This number is crucial to computing the metric (see paper).


Inputs/Ouputs:
===============

logs-input/
 The 100 input logs used in the experiment.

output-bound-X/  (NOT maintained in the repository, due to space constraints).
 Output of running invarimint on each of the logs in logs-input/
 considering a string length metric comparison of X (i.e., generate
 sets of strings of length < X in both the declarative and procedural
 models and compare these two sets).

output/
 Does not exist, created with run-syn-exp.sh when running new
 experiments.
 

Scripts:
========

gen_traces.py
 Generates traces for evaluating/comparing procedural and declarative
 Synoptic algorithms: 100 log files, ranging over event types from an
 alphabet of 8 events, each log containing 24 events.

run-syn-exp.sh
 Runs both versions of Synoptic on the traces in vary-etypes/
 directory. Must be run from top level synoptic directory. Writes its
 output to output/, which it creates.

get-intersection.sh
 Simple script to scrape the output files from the previous step and
 output the model overap metric output.
