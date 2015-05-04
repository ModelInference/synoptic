**Table of Contents:**



# Introduction #

---


This document provides a fast-paced primer on how to use Synoptic from the command line. It does so with many examples, without dwelling much on the details of how things work or why they work the way that they do.
  * See the [tutorial](DocsSynopticCmdLineTutorial.md) for a more detailed and slower-paced introduction
  * See the [usage screen](DocsSynopticCmdLineHelpScreen.md) for a complete listing of all the command line options

# Inputs Overview #

---


Synoptic requires that the user supply it with the following inputs:
  * Log files to analyze
    * Specified by listing them at the end of the command line.
  * Output path prefix to use when outputting graphs and graphviz dot files
    * Specified with the `-o` option, e.g. `-o output/apache-trace`
  * A set of regular expressions that match every line in the log files

## Synoptic regular expressions ##

The user-supplied regular expressions are used to _parse_ lines that appear in the input log files. Synoptic needs the set of regular expressions to tell it three things about **each** log line:
  * Event type
  * Time value
  * Trace sample partition

Synoptic regular expressions have a number of particular properties, which you should be aware of:
  * Named group names `TYPE`, `TIME`, `DTIME`, `FTIME`, `VTIME`, `HIDDEN`, `FILE` are specially interpreted by Synoptic
    * We recommend that you use lower case names to distinguish user-defined group names
  * You may use the `-r` option multiple times
    * If you do not specify the `-r` option then the following default (using the entire log line as the event type) will be used: `-r '(?<TYPE>.*)'`
    * The ordering of regular expressions specified with `-r` is **significant** -- matching is attempted in the order in which the expressions are given
  * You should specify either the `-s` or the `-m` option
    * We do not recommended that you use these two options simultaneously
    * If neither option is specified then the following default (partitioning based on filename) is used: `-m '\k<FILE>'`

# Examples #

---


(Note: you can find a complete listing of all the options [here](DocsSynopticCmdLineHelpScreen.md))


## Example1: basic command line ##

**Log:**
```
a
b
c
```

**Command line:**
```
java -jar synoptic.jar -o some-dir/file-prefix -d /usr/local/bin/dot
```
  * `-o` : output prefix
  * `-d` : where to find the dot executable

| **Initial graph** | **Final graph** |
|:------------------|:----------------|
| <img src='http://wiki.synoptic.googlecode.com/hg/images/DocsUsage/1.initial.png' height='50%' /> | <img src='http://wiki.synoptic.googlecode.com/hg/images/DocsUsage/1.png' height='50%' /> |

**Notes:**
  * The default `-r` option is used when no `-r` option is passed -- it considers the entire log line as an event type.
  * The default time is log line counting time -- it totally orders events according to their order in the log.


# More Advanced Usage #

---


(Note: you can find a complete listing of all the options [here](DocsSynopticCmdLineHelpScreen.md))

## Visibility Options ##

Some options can provide more visibility into what Synoptic is doing. They do not change behavior, but produce more information. For example, `dumpInvariants` outputs the set of invariants that are true for the input logs.

**Log:**
```
a
b
a
b
c
```

**Command line:**
```
java -jar synoptic.jar -o some-dir/file-prefix -d /usr/local/bin/dot --dumpInvariants some-dir/log-file.log
```

Synoptic will output the mined invariants to console:
  * a AlwaysFollowedBy(t) b
  * a AlwaysPrecedes(t) b
  * a AlwaysFollowedBy(t) c
  * a AlwaysPrecedes(t) c
  * b AlwaysFollowedBy(t) c
  * b AlwaysPrecedes(t) c
  * c NeverFollowedBy(t) a
  * c NeverFollowedBy(t) b
  * c NeverFollowedBy(t) c
  * INITIAL AlwaysFollowedBy(t) a
  * INITIAL AlwaysFollowedBy(t) b
  * INITIAL AlwaysFollowedBy(t) c

**Notes:**
  * You can also use `outputInvariantsToFile` to have Synoptic output all the invariants to a text file.

`outputSupportCount` will output the number of observed events supporting each invariant mined by synoptic. For the log above, you would expect to see support count of 2 for a AlwaysFollowedBy(t) b, but a support count of 1 for a AlwaysFollowedBy(t) c:

**Command line:**
```
java -jar synoptic.jar -o some-dir/file-prefix -d /usr/local/bin/dot --dumpInvariants some-dir/log-file.log
```

Synoptic will output the following invariants and support count to console::
  * a AlwaysFollowedBy(t) b, Invariant support count: 2
  * a AlwaysPrecedes(t) b, Invariant support count: 2
  * a AlwaysFollowedBy(t) c, Invariant support count: 2
  * a AlwaysPrecedes(t) c, Invariant support count: 1
  * b AlwaysFollowedBy(t) c, Invariant support count: 2
  * b AlwaysPrecedes(t) c, Invariant support count: 1
  * c NeverFollowedBy(t) a, Invariant support count: 1
  * c NeverFollowedBy(t) b, Invariant support count: 1
  * c NeverFollowedBy(t) c, Invariant support count: 1
  * INITIAL AlwaysFollowedBy(t) a, Invariant support count: 2
  * INITIAL AlwaysFollowedBy(t) b, Invariant support count: 2
  * INITIAL AlwaysFollowedBy(t) c, Invariant support count: 1


**Notes:**
  * specifying `outputSupportCount` will always result in printing of invariants to console, regardless of the option `dumpInvariants`.
  * if `outputInvariantsToFile` is specified, support count for each invariant will be outputted to the file.

## Behavioral options ##
The second category of advanced options modify Synoptic's behavior:

For example, `supportCountThreshold` will filter Synoptic to use only invariants with support count greater than the threshold. For the above log file, with a threshold of one, Synoptic will filter out all invariants with a support count of 1 or lower:

**Command line:**
```
java -jar synoptic.jar -o some-dir/file-prefix -d /usr/local/bin/dot --supportCountThreshold=1 some-dir/log-file.log
```

Synoptic will only use the following invariants for refinement:
  * a AlwaysFollowedBy(t) b, Invariant support count: 2
  * a AlwaysPrecedes(t) b, Invariant support count: 2
  * a AlwaysFollowedBy(t) c, Invariant support count: 2
  * b AlwaysFollowedBy(t) c, Invariant support count: 2
  * INITIAL AlwaysFollowedBy(t) a, Invariant support count: 2
  * INITIAL AlwaysFollowedBy(t) b, Invariant support count: 2

You can also modify the invariants Synoptic uses for refinement with `ignoreInvsOverETypeSet`, which will ignore all invariants with event type as specified in the option:

**Command Line:**
```
java -jar synoptic.jar -o some-dir/file-prefix -d /usr/local/bin/dot --ignoreInvsOverETypeSet="a;b" some-dir/log-file.log
```

The above command will filter out all invariants with type a and b. For the above log file, this will reduce the invariants to:
  * a AlwaysFollowedBy(t) c
  * a AlwaysPrecedes(t) c
  * b AlwaysFollowedBy(t) c
  * b AlwaysPrecedes(t) c
  * c NeverFollowedBy(t) a
  * c NeverFollowedBy(t) b
  * c NeverFollowedBy(t) c
  * INITIAL AlwaysFollowedBy(t) a
  * INITIAL AlwaysFollowedBy(t) b
  * INITIAL AlwaysFollowedBy(t) c

The invariants a AlwaysFollowedBy(t) b, and a AlwaysPrecedes(t) b have been removed from the list as they of of event type a and b.

Synoptic can be used to only mine invariants of log files with the option `onlyMineInvariants`. You can use this option if you do not want Synoptic to generate, refine, and coarsen models of the system.

The invariant mining, and modeling stages can also be modified with the option `noCoarsening`, which commands Synoptic to complete model refinement, then terminate without coarsening the model. `noRefinement` option commands Synoptic to generate the initial model, then terminate without refining or coarsening the model.

Synoptic is non-deterministic, so the final model may differ in different runs of the program. If you wish for deterministic behavior, you may use the `randomSeed` option in Synoptic to feed a consistent random seed.