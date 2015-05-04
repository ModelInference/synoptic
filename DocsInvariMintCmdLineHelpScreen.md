# InvariMint Command Line Options #
```
Usage: invarimint [options] <logfiles-to-analyze>

General Options:
  -h --help=<boolean>                    - Print short usage message [default false]
  -H --allHelp=<boolean>                 - Print extended usage message (includes debugging options) [default false]

Input Options:
  -c --argsFilename=<string>             - Command line arguments input filename
  --randomSeed=<long>                    - Use a specific random seed for pseudo-random number generator
  -s --separatorRegExp=<string>          - Partitions separator reg-exp: log lines below and above the matching line are placed into different partitions
  -r --regExps=<string> [+]              - Parser reg-exp: extracts event type and event time from a log line
  -m --partitionRegExp=<string>          - Partitions mapping reg-exp: maps a log line to a partition [default \k<FILE>]
  -i --ignoreNonMatchingLines=<boolean>  - Ignore lines that do not match any of the passed regular expressions [default false]
  --recoverFromParseErrors=<boolean>     - Ignore parser warnings and attempt to recover from parse errors if possible [default false]
  --debugParse=<boolean>                 - Debug the parser by printing field values extracted from the log and then terminate. [default false]
  --dateFormat=<string>                  - Format of the dates contained in the log (required by DATETIME) [default dd/MMM/yyyy:HH:mm:ss]

kTails Options:
  --invMintKTails=<boolean>              - Run the InvariMint-KTails algorithm. [default false]
  --kTailLength=<int>                    - Size of tail when performing kTails [default 2]

Synoptic Options:
  --invMintSynoptic=<boolean>            - Run the InvariMint-Synoptic algorithm. [default false]

InvariMint Options:
  --removeSpuriousEdges=<boolean>        - Remove spurious edges from InvariMint model [default false]
  --minimizeIntersections=<boolean>      - Minimize afer each intersection and the final model [default false]

Property Type Options:
  --alwaysFollowedBy=<boolean>           - Run with Always Followed by property type. [default false]
  --alwaysPrecedes=<boolean>             - Run with Always Precedes property type. [default false]
  --neverFollowedBy=<boolean>            - Run with Never Followed by property type. [default false]
  --neverImmediatelyFollowedBy=<boolean> - Run with Never Immediately Followed by property type [default false]

Output Options:
  -o --outputPathPrefix=<string>         - Output path prefix for generating Graphviz dot files graphics
  --compareToStandardAlg=<boolean>       - Whether to compare the InvariMint model to the model derived using the non-InvariMint algorithm [default false]
  --exportStdAlgPGraph=<boolean>         - Export the PGraph model derived using StandardAlg [default false]
  --exportStdAlgDFA=<boolean>            - Exports the DFA model corresponding to the PGraph derived using StandardAlg [default false]
  --exportMinedInvariantDFAs=<boolean>   - Export every mined invariant DFA [default false]
  --logLvlQuiet=<boolean>                - Quietest logging, warnings only [default false]
  --logLvlVerbose=<boolean>              - Verbose logging [default false]
  --logLvlExtraVerbose=<boolean>         - Extra verbose logging [default false]
```

As of revision:
b4e374b9a560

**Note: this page is auto-generated. Do not edit.**