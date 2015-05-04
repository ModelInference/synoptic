# CSight Command Line Options #
```
Usage: csight [options] <logfiles-to-analyze>

General Options:
  -h --help=<boolean>                       - Print short usage message [default false]
  -H --allHelp=<boolean>                    - Print extended usage message (includes debugging options) [default false]

Input Options:
  -c --argsFilename=<string>                - Command line arguments input filename
  --randomSeed=<long>                       - Use a specific random seed for pseudo-random number generator
  -s --separatorRegExp=<string>             - Partitions separator reg-exp: log lines below and above the matching line are placed into different partitions
  -r --regExps=<string> [+]                 - Parser reg-exp: extracts event type and event time from a log line
  -m --partitionRegExp=<string>             - Partitions mapping reg-exp: maps a log line to a partition [default \k<FILE>]
  -q --channelSpec=<string>                 - Queue/channel specification. For example, 'M:0->1;A:1->0' specifies channels 'M' and 'A' in the log. For 'M' pid 0 is the sender and pid 1 is the receiver.
  -i --ignoreNonMatchingLines=<boolean>     - Ignore lines that do not match any of the passed regular expressions [default false]
  --recoverFromParseErrors=<boolean>        - Ignore parser warnings and attempt to recover from parse errors if possible [default false]
  --debugParse=<boolean>                    - Debug the parser by printing field values extracted from the log and then terminate. [default false]
  --dateFormat=<string>                     - Format of the dates contained in the log (required by DATETIME) [default dd/MMM/yyyy:HH:mm:ss]
  --topKElements=<int>                      - Set number of top queue elements to compare states for partition graph construction. [default 1]

Debugging Options:
  -t --useTransitiveClosureMining=<boolean> - Use the transitive closure invariant mining algorithm (usually slower) [default false]

Model Checking Options:
  --mcPath=<string>                         - Complete path to the model checker binary
  --mcType=<string>                         - Model checker type to use. Must be either 'spin' or 'mcscm'. [default mcscm]
  --spinChannelCapacity=<int>               - Default channel capacity to use when using the spin model checker. [default 8]
  -p --runParallel=<boolean>                - Run model checking processes in parallel. (Only available for McScM) [default true]
  --spinMultipleInvs=<boolean>              - Check multiple invariants per model checking run when using Spin. [default true]
  --numParallel=<int>                       - Number of model checking processes to run in parallel. Default is dynamically computed to be the number of available cores. [default 4]
  --baseTimeout=<int>                       - Initial timeout (in seconds) that is used to time out a model-checker run. [default 20]
  --timeoutDelta=<int>                      - Time (in seconds) to add to -base-timeout after each time the model checker times out, before reaching max timeout. [default 10]
  --maxTimeout=<int>                        - Maximum timeout (in seconds) to use for a model checking run. [default 60]

Output Options:
  -o --outputPathPrefix=<string>            - Output path prefix for generating Graphviz dot files graphics
  -d --dumpInvariants=<boolean>             - Output complete list of mined invariants to stdout [default false]
  --logLvlQuiet=<boolean>                   - Quietest logging, warnings only [default false]
  --logLvlVerbose=<boolean>                 - Verbose logging [default false]
  --logLvlExtraVerbose=<boolean>            - Extra verbose logging [default false]

Strategy Options:
  --consistentInitState=<boolean>           - Each process begins execution in the same initial state across all traces in the log [default true]
  --minimize=<boolean>                      - Minimize each of the process FSMs during GFSM to CFSM conversion [default false]
```

As of revision:
b4e374b9a560

**Note: this page is auto-generated. Do not edit.**