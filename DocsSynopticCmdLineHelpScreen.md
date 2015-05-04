# Synoptic Command Line Options #
```
Usage: synoptic [options] <logfiles-to-analyze>

General Options:
  -h --help=<boolean>                    - Print short usage message [default false]
  -H --allHelp=<boolean>                 - Print extended usage message (includes debugging options) [default false]
  -V --version=<boolean>                 - Print the current synoptic repo changeset [default false]

Execution Options:
  -q --logLvlQuiet=<boolean>             - Be quiet, do not print much information [default false]
  -v --logLvlVerbose=<boolean>           - Print detailed information during execution [default false]
  -f --useFSMChecker=<boolean>           - Use FSM checker instead of the default NASA LTL-based checker [default true]
  --randomSeed=<long>                    - Use a specific random seed for pseudo-random number generator
  --separateVTimeIndexSets=<string>      - Vector time index sets for partitioning the graph by system node type, e.g. '1,2;3,4'
  --multipleRelations=<boolean>          - Mine multiple relations from the trace graph [default false]
  --stateProcessing=<boolean>            - Enable state processing [default false]
  -t --testGeneration=<boolean>          - Enable abstract test generation [default false]
  --supportCountThreshold=<int>          - filter all invariants that have support values greater than the threshold [default 0]
  --ignoreIntrByInvs=<boolean>           - Ignore IntrBy Invariants. Synoptic currently doesn't support IntrBy Invariants. [default true]

Parser Options:
  -s --separatorRegExp=<string>          - Partitions separator reg-exp: log lines below and above the matching line are placed into different partitions
  -r --regExps=<string> [+]              - Parser reg-exp: extracts event type and event time from a log line
  -m --partitionRegExp=<string>          - Partitions mapping reg-exp: maps a log line to a partition [default \k<FILE>]
  -i --ignoreNonMatchingLines=<boolean>  - Ignore lines that do not match any of the passed regular expressions [default false]
  --recoverFromParseErrors=<boolean>     - Ignore parser warnings and attempt to recover from parse errors if possible [default false]
  --debugParse=<boolean>                 - Debug the parser by printing field values extracted from the log and then terminate. [default false]
  --dateFormat=<string>                  - Format of the dates contained in the log (required by DATETIME) [default dd/MMM/yyyy:HH:mm:ss]

Input Options:
  -c --argsFilename=<string>             - Command line arguments input filename

Output Options:
  -o --outputPathPrefix=<string>         - Output path prefix for generating Graphviz dot files graphics
  --outputSupportCount=<boolean>         - Output support counts along with mined invariants [default false]
  --outputInvariantsToFile=<boolean>     - Output invariants to a file [default false]
  --exportAsGML=<boolean>                - Export models as GML and not DOT files [default false]
  -d --dotExecutablePath=<string>        - Path to the Graphviz dot command executable to use
  --outputEdgeLabels=<boolean>           - Output transition probabilities on the graph's edge labels [default true]
  --showTerminalNode=<boolean>           - Show TERMINAL node in generated graphs. [default true]
  --showInitialNode=<boolean>            - Show INITIAL node in generated graphs. [default true]

Verbosity Options:
  --dumpInvariants=<boolean>             - Dump complete list of mined invariant to stdout [default false]
  --dumpTraceGraphDotFile=<boolean>      - Dump the DOT file for the trace graph to file <outputPathPrefix>.tracegraph.dot [default false]
  --dumpTraceGraphPngFile=<boolean>      - Dump the PNG of the trace graph to file <outputPathPrefix>.tracegraph.dot.png [default false]
  --dumpInitialPartitionGraph=<boolean>  - Dump the initial condensed partition graph [default false]
  --dumpIntermediateStages=<boolean>     - Dump dot files from intermediate Synoptic stages to files of form outputPathPrefix.stage-S.round-R.dot [default false]

Debugging Options:
  --logLvlExtraVerbose=<boolean>         - Print extra detailed information during execution [default false]
  --ignoreInvsOverETypeSet=<string>      - Ignore invariants that include event types from the following set (use ';' to separate event types).
  --useTransitiveClosureMining=<boolean> - Use the transitive closure invariant mining algorithm (usually slower) [default false]
  --mineNeverConcurrentWithInv=<boolean> - Mine the NeverConcurrentWith invariant (only changes behavior for PO traces with useTransitiveClosureMining=false) [default true]
  --onlyMineInvariants=<boolean>         - Mine invariants and then quit. [default false]
  --noCoarsening=<boolean>               - Do not perform the coarsening stage [default false]
  --doBenchmarking=<boolean>             - Perform benchmarking and output benchmark information [default false]
  --internCommonStrings=<boolean>        - Intern commonly occurring strings, such as event types, as a memory-usage optimization [default true]
  --runTests=<boolean>                   - Run all tests in synoptic.tests.units, and then terminate. [default false]
  --runAllTests=<boolean>                - Run all tests in synoptic.tests, and then terminate. [default false]
  --performExtraChecks=<boolean>         - Perform extra correctness checks at the expense of cpu and memory usage. [default false]
  --noRefinement=<boolean>               - Do not perform refinement [default false]
```

As of revision:
b4e374b9a560

**Note: this page is auto-generated. Do not edit.**