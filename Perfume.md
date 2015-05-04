# Introduction #

**Perfume** infers performance-aware, behavioral models of software using existing runtime logs. Performance metrics present in these logs convey vital information about the execution of most systems, but previous model inference work ignores performance data. Perfume generates more precise models by utilizing this information, which might include such metrics as event timestamps, network traffic, or energy consumption.

# Perfume resources #

## Example use-cases ##

---


Examples to motivate (a) how Perfume might be helpful to developers, and (b) how Perfume might be more useful than Synoptic and related approaches.

**Caching:**

Using Perfume to uncover underlying caching behavior that is not visible at the event sequence level. For example, it might reveal a performance difference due to cache hits and cache misses along a single abstract path of execution.

**Differentiating mocked and real object behavior:**

Mocked objects are a standard means of unit-testing complex applications that have dependencies on external resources, such as a database. A mocked object provides sufficient behavior to stand-in for the true object that will be used in production. Perfume can be used to differentiate between executions that were derived with mocked objects versus those that used true underlying resources.


**Login-database example:**

Let's say the user logs into some application, and then the application accesses a database on the user's behalf. There might be two executions that look identical in the events that they generate: "load page", "user login", "get db info". But, there might underlying dependency that is not apparent from this sequence, such as the impact of the user's authentication on the db operation. This dependency may be visible in the timing of the operations. For example, the first sequence with timing might looks like "0s : load page", "5s : user login", "7s: get db info". While a second sequence might be "0s: load page", "2s: user login", "7s: get db info". The two paths have identical total times, and identical event sequences, but in the first sequence the login takes a long time (e.g., perhaps the login is incorrect) while in the second sequence the login is fast (e.g., a correct login might be faster to verify).

**Differentiating machine architectures:**

Run the same code on different architectures/networks and you'll get different performance. Perfume might be used to differente traces based on the performance characteristics of the resources accessed by the program that generated the traces.

**Enforce splitting of branches if they differ in performance:**

Let's say we have four traces -- two fast traces that looks like "a x c Z" and two slow traces that look like "a y c Z". Synoptic will create two branches -- one for "x" and another for "y". These two branches will merge back at the "c" event, followed by a merged tail of some "Z" sequence of events. Due to the performance differences between the "x" and the "y" traces, Perfume will break apart the merged tails and will create completely independent branches -- one for "x" and another for "y".

**Memory performance models:**

If instead of timestamps the log contains a sequence of total memory usage, then we could use Perfume to construct a memory model of the system that generated the log. That is, an edge between two events would have a distribution of the delta in allocated memory --- this distribution may contain negative allocations (e.g., deallocs). Could the resulting model could be used to find memory leaks?

**File system performance models:**

Use a tool, such as [blktrace](http://www.scribd.com/doc/2288714/blktrace-usage) on Linux, to trace requests through the kernel vfs and the disk controller/driver for specific block requests. Build a model that describes performance of these block requests and differentiates blocks based on the path/latency that was observed in the corresponding requests.

**WProf / webpage load performance:**

[WProf](http://wprof.cs.washington.edu/) instruments Chrome and logs timing and dependency information for page loads. A page's load time will vary based on where the client is located, characteristics of the network, characteristics of the client device, etc. So, by collecting a bunch of page load measurements for the same page, we can derive a non-trivial Perfume model that can help to pin-point aspects of the page load that are slow under certain conditions. And, perhaps even more usefully, the model may reveal how the set/sequence of events in the load process varies with performance (e.g., for slow connections the site may show an image instead of a video preview).

**Application-specific garbage collection optimization:**

Perfume models could tell GC when not to run by predicting what's coming next. One likely positive outcome: Don't run GC if the execution is almost done. Could use the Boehm-Demers-Weiser GC for C/C++ [here](http://www.hpl.hp.com/personal/Hans_Boehm/gc/), which might be easier to control and modify since it's already an add-on itself.

_Potential challenges:_
  1. GC is very heavily researched, and it's hard to improve on that. (Although the "we're almost done" optimization is likely to help.)
  1. GC in Java seems to be very hard to control.

**Performance debugging (generally):**

Find poorly performing, predicted paths to help identify what to remove.

**Energy usage**

Like time, energy usage is monotonic. Perfume models of power might be completely novel to the field.
  * We can imagine inferring a model of a system before a change, and then after a change, and using the model to predict if now paths exists that will consume LOTS of energy, or if perhaps such paths have been eliminated.

_Potential challenges:_
  1. Energy use is not linear and so two deltas are not completely comparable (i.e., different # joules used on two edges in a model may actually be identical if the battery level was the same). It has really weird patterns and researchers are still working on figuring out how we should model energy use exactly.
  1. Retrieving a log with energy numbers. Potentially we could do something that is at method call granularity. But, there are tools out there and researchers who build them would be excited about us trying these out.

_Resources:_
  * How do architectural choices influence energy consumption? [link](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.66.9919&rep=rep1&type=pdf)
  * How do code changes affect energy consumption: [link](http://softwareprocess.es/a/green-change-web.pdf)
  * "V-edge: Fast Self-constructive Power Modeling of Smartphones Based on Battery Voltage Dynamics" [link](https://www.usenix.org/system/files/conference/nsdi13/nsdi13-final135.pdf)
  * "eDoctor: Automatically Diagnosing Abnormal Battery Drain Issues on Smartphones" [link](https://www.usenix.org/system/files/conference/nsdi13/nsdi13-final198.pdf)
  * "Carat: Collaborative Energy Diagnosis for Mobile Devices" [link](https://amplab.cs.berkeley.edu/wp-content/uploads/2013/10/oliner-Carat-SenSys13.pdf)
  * "Where is the energy spent inside my app? Fine grained energy accounting on smartphones with eprof." [link](http://research.microsoft.com/en-us/people/mzh/eurosys-2012.pdf)
  * "Empowering developers to estimate app energy consumption" [link](http://research.microsoft.com/pubs/166288/WattsonMobicom12.pdf)
  * "Energy consumption in mobile phones: A measurement study and implications for network applications." [link](http://ciir-publications.cs.umass.edu/getpdf.php?id=904)

**Performance counters**

Use some performance counter (retrieved by perf) to find known bad behavior. Then Perfume models could show the types of executions that lead to this bad behavior, and we could then apply some kind of fix.

_Specific idea:_
> Look at executions that lead to lots of cache misses, and simply move the conflicting objects in memory farther apart if we're heading down one of these paths.

**Utilizing multiple metrics**

We could record multiple metrics as a program runs (time, memory usage, network bytes sent/received, etc.) and experiment with using them individually or all at once. Comparing models generated using each metric individually might convey correlations between them, e.g., if time is correlated with network traffic, this part of the execution is being bottlenecked by the network or is at least network heavy.

<br />
## Work related to Perfume ##

---


(_Warning: not all of this work is closely related._)

  * [Hard-to-Answer Questions about Code](http://www.ics.uci.edu/~tlatoza/papers/plateau2010.pdf), PLATEAU 2010
    * Has some motivation for the kinds of performance questions developers ask about code
  * [Maintaining mental models: a study of developer work habits](http://dl.acm.org/citation.cfm?id=1134355), ICSE 2006
    * General work on software comprehension and how developers maintain (and fail to maintain) mental models of software.

  * IBM Whole-system Analysis of Idle Time (WAIT)
    * http://researcher.watson.ibm.com/researcher/files/us-sjfink/res0000076-altman.pdf
    * http://researcher.watson.ibm.com/researcher/view_project.php?id=1332
  * Model-Based Software Performance Analysis (book)
    * http://link.springer.com/book/10.1007/978-3-642-13621-4/page/1
  * Deriving performance models of software architectures from message sequence charts (workshop paper)
    * http://dl.acm.org/citation.cfm?id=350404
  * Model-based performance prediction in software development: a survey
    * http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=1291833

Formalisms and theory:
  * A theory of Timed Automata by Rajeev Alur and David L. Dill
    * http://dl.acm.org/citation.cfm?id=180519
  * An Abstraction Refinement Technique for Timed Automata Based on Counterexample-Guided Abstraction Refinement Loop
    * http://search.ieice.org/bin/summary.php?id=e93-d_5_994
  * Behavioral Cartography of Timed Automata
    * http://link.springer.com/chapter/10.1007/978-3-642-15349-5_5

Inference of timed automata:
  * A bunch of work by Olga Grinchtein, but all of it (seems to be) constrained to the active learning context with positive and negative examples.
    * https://sites.google.com/site/olgagrinchtein/publications

Specification patterns:
  * http://patterns.projects.cis.ksu.edu/

Systems performance debugging/tracing:
  * "Performance Debugging for Distributed Systems of Black Boxes" [link](http://pdos.csail.mit.edu/~athicha/papers/blackboxes%3Asosp03.pdf)
  * "Automatic construction of coordinated performance skeletons" [link](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=4536405)
  * "Catch me if you can: performance bug detection in the wild" [link](http://dl.acm.org/citation.cfm?doid=2048066.2048081)

Systems work on tracing and log analysis:
  * "Be Conservative: Enhancing Failure Diagnosis with Proactive Logging" OSDI'12 [link](http://opera.ucsd.edu/paper/osdi12-errlog.pdf)
  * [X-Trace: A Pervasive Network Tracing Framework](http://dl.acm.org/citation.cfm?id=1973450). Fonseca et al. NSDI 2007. [project site](http://www.x-trace.net/wiki/doku.php).
  * [Using Magpie for request extraction and workload modeling](http://dl.acm.org/citation.cfm?id=1251272). Barham et al. OSDI 2004.
  * [Performance debugging for distributed systems of black boxes](http://dl.acm.org/citation.cfm?id=945454). Aguilera et al. SOSP 2003.
  * [Structured Comparative Analysis of Systems Logs to Diagnose Performance Problems](https://www.usenix.org/system/files/conference/nsdi12/nsdi12-final61.pdf). NSDI 2012.
  * [Diagnosing performance changes by comparing request flows](http://static.usenix.org/event/nsdi11/tech/full_papers/Sambasivan.pdf). NSDI 2011.
  * [Characterising Logging Practices in Open-Source Software](http://dl.acm.org/citation.cfm?id=2337236). ICSE 2012.

Performance profiling by method/procedure:
  * "Evaluating the accuracy of Java profilers" [link](http://dl.acm.org/citation.cfm?doid=1806596.1806618)
  * "The Mature Optimization Handbook" [link](https://www.facebook.com/notes/facebook-engineering/the-mature-optimization-handbook/10151784131623920)

Questions developers ask about software:
  * "Maintaining mental models: a study of developer work habits" [link](https://www.st.cs.uni-saarland.de/edu/empirical-se/2006/PDFs/latoza06.pdf)
  * "Hard-to-Answer Questions about Code" [link](http://www.ics.uci.edu/~tlatoza/papers/plateau2010.pdf)


<br />
## Model Checking (Traversing loops) ##

---

Loops will often occur in models, and we need some method of deciding when to stop traversing a loop during model checking, or model checking will never terminate.  Possible implementations:

  1. (Current) After completing a loop, if we inhabit any new state in the FSM that we did not inhabit at the start of the loop, continue. If we inhabit no new states, stop looping.
    * _This is simple but might result in over-splitting (and under-merging), as some legitimate loops that existed in the traces will be considered illegal during model checking and split out_
  1. Count the number of times a loop (e.g., A->B->C->A) appears in each individual trace. Use the count from the trace that contains the most of that loop as a _loop limit_. When model checking, allow that loop to be traversed at most the _loop limit_ number of times.
    * _This is fairly straightforward to implement, although it is insensitive to the fact that traces might have 5 of some loop but never more than one in a row, so even 2x around the loop would not be accurate to the traces_
  1. At each node while model checking, follow all concrete traces to determine if any actual loops at this point existed and how many. Allow at most this many loops at this node
    * _Looks at which loops actually occurred at this node in traces, but harder to implement and would probably be slow_



<br />
## Refinement ##

---

The purpose of constrained refinement is to split apart abstract groupings (partitions) of events where the partition as it currently exists causes a time-constrained invariant to be violated.  Synoptic's refinement is only concerned with reachability; for example, if some partition of type X can reach some partition of type Y where there were no actual traces with an X before a Y, some intermediate partition needs to be split.  Perfume's constrained refinement can do this while also splitting partitions that are only illegal because, following some path of partitions, one can get from some event X to some event Y with some combined time less than or greater than the time that any X-to-Y occurred in actual traces.

### Definitions ###
> _**Min/max transitions**_: Consider all concrete transitions with both (1) the source event in some partition _i_ and (2) the target event in some partition _j_. When refining based on an upper-bound invariant violation, find the maximum time delta of any of those transitions. Now the set of transitions with exactly that maximum time delta are the _max transitions_ between _i_ and _j_.  For a lower-bound invariant, find the minimum time delta instead, and the resulting set is _min transitions_. _Min/max transitions_ means using the former for upper-bound refinement and the latter for lower-bound. <font color='red'><i>(TODO: Add diagram)</i></font>

> _**Stitch**_: In a counter-example path, partition _previous_ immediately precedes partition _this_, and partition _this_ immediately precedes partition _next_. Define set _arriving_ as the _**min/max transitions**_ between _previous_ and _this_. Define set _departing_ as the _**min/max transitions**_ between _this_ and _next_. Partition _this_ contains a _stitch_ if the set of _arriving_'s target events and the set of _departing_'s source events are not equal. <font color='red'><i>(TODO: Add diagram)</i></font>

### Algorithm ###
```
1   for (partition j from end of violation subpath to beginning)
2     for (partition i from j to beginning)
3       if (stitch at i)
4         if (>0 legal paths and >0 illegal paths between i and j)
5           add to potential splits
6
7   for (potential splits)
8     if (globally resolves)
9       split(this)
10      return
11    else if (locally resolves)
12      localresolution = this
13    else
14      arbitrary = this
15
16  if (localresolution exists)
17    split(localresolution)
18  else
19    split(arbitrary)
```

#### Comments by line ####
1: We considered not including this outer loop, and then 'j' in line 2 was instead 'end of violation subpath'. However, that can prevent the check in line 4 from working in some double-stitch cases [like this](http://i.imgur.com/xKfJXMK.png), where the needed split at the partition containing (2) would not be detected.

4: Find all concrete paths starting from an event in _i_ and ending on an event in _j_. Consider what happens if we replace the counter-example path's current subpath between _i_ and _j_ with one of the just-mentioned concrete paths. If this would mean the violation subpath (including the replaced path between _i_ and _j_) no longer violates the invariant, this concrete path is legal. If not, it is illegal.

5: Events beginning legal paths go in one half of the split, events beginning illegal ones go in the other half. All other events are randomly distributed between the two halves.

8: If performing this split resolves all violations of the current invariant type (e.g., login AP logout upper-bound=60) from the entire partition graph, then this split globally resolves the violation.

11: If performing this split resolves all violations of the current invariant type only between the start and end of the violation subpath, then this split locally resolves the violation.

11-12, 16-17: Planned to be implemented at a later stage. These are not necessary for correct refinement but should help maximize the amount of progress made on each iteration of this algorithm and might result in a more concise final partition graph.

#### Algorithm commentary ####
  * Starting from the end (as we do) or the beginning of the violation subpath makes very little difference.  Simple examples can be constructed where the former splits better than the latter and vice versa. <font color='red'><i>(TODO: contrive a better way to visualize the splitting process, and make such examples)</i></font>

  * There must be a stitch in a partition before it is considered for splitting because if there is no stitch, then we passed through this partition by following a real path that existed in a trace.  So splitting this partition cannot help resolve this violation.

  * A counter-example is some combination of concrete transitions that can be followed based on the current partition configuration but lead to a violation of an invariant.

  * Progress (toward resolving the violation) is defined as breaking at least one counter-example without creating any new counter-examples.  Splitting one partition into two can only remove combinations of concrete paths, never create new ones, so we will never create new counter-examples with this algorithm.

  * As per comment for line 4, we look for concrete paths between two partitions that, if used, (1) would resolve the violation or (2) wouldn't affect anything / would retain the violation.
    * If we have only the former (legal paths), then there is no counter-example because none of these subpaths could be taken to cause a violation.  The algorithm should never have been run in the first place in this case.
    * If we have only the latter (illegal paths), then the counter-example path could follow any of these, and the violation would remain. So splitting here does not help.
    * If we have at least one of each, then this split makes progress toward resolving the violation because we break some combination of transitions between events (specifically _start_ to _illegal path i->j_ to _end_) that currently violations an invariant but after splitting can no longer be followed.
    * Because of the previous point, every split chosen by this algorithm makes progress toward resolving the violation. So the violation is guaranteed eventually to be resolved.
    * <font color='red'><i>TODO: Find a way to show that the algorithm always finds some split to make, or the above claim is technically not complete)</i></font>

  * When considering which partition to split, looking only at those within the violation subpath is an optimization.  The algorithm would never find a partition to split outside the violation subpath because the timing information outside it is not pertinent to the current violation.



<br />
## Invariant-related additions ##

---

Adding new invariants or new types of time constraints should help Perfume's models convey more useful performance information.

### New invariants ###
#### Counting ####
Every trace has the same number of x and y.
  * _ex:_ xxxyyxyyyx
  * _Possible time constraint:_ Time since the number of x and y were not equal.

#### Interrupter ####
After an x, there cannot be another x without first y.
  * _ex:_ xyx
  * _Possible time constraint:_ Min/max time from x->y, min/max time from y->x.

### New time constraints ###
#### Median time ####
Find the median time of an invariant (e.g., for login AP logout, consider the times of all such paths between login->logout and compute the median).  We can then ensure during refinement and coarsening that the median time of login->logout paths is within some range, maybe no more than 1 stdev away from the mined median.

### Normalization ###
As a pre-processing step, take a given trace and normalize its times into [0,1].  This would be very helpful in circumstances where some traces came from a very slow machine and others came from a very fast machine but where they both exhibit the same relative timing patterns.  Normalizing such traces would mean the constrained invariants would actually capture patterns present in the log rather than finding outlier maxes and mins that don't say much about the system.



<br />
## Miscellaneous ##

---


### OBSERVATION: Never split start or end ###
This seems to only be useful for optimization.

#### Claim ####
> _An invariant violation can never be resolved by splitting a partition corresponding to the start or end of the **violation subpath** of this invariant violation._

#### Definitions ####
> _**Violation subpath**_: The subpath of this counter-example that starts with the last encountered _t=0_ state and ends with the first encountered permanent failure state. In other words, where the actual violation was detected during model checking.

#### Example ####
As an example, a counter-example path for the invariant "x AP z upper-bound=4" might be:

> _INIT --> a --1--> **x** --3--> x --3--> **z** --> TERM_

The claim is not that no partition of type x or of type z should be split but that the x and z partitions which form the endpoints of the actual violation should not be split. Those are in **bold** above.

### IDEA: Normalize traces / transitions ###
Some server might contain traces from users with very fast connections/PCs/environments and some much slower.  Imagine there are some relative patterns that exist in all traces, e.g., A->B takes twice as long as B->C.  But if the slow traces take 10x as long for every transition in the entire slow trace, Perfume cannot currently detect that these traces are all very similar even if the absolute times are very different.  We might therefore consider normalizing the traces in some way.

#### Possible normalizations ####
  1. Normalize each trace individually to sum to 1, so _**A --2--> B --4--> C --6--> D**_ becomes _**A --2/12--> B --4/12--> C --6/12--> D**_, etc.
  1. Apply some sort of sequence alignment algorithm to the traces, and then normalize the traces in some way that would take advantage of this
  1. Normalize not relative to the trace but horizontally across common transitions, so if 3 traces each have an A->B, respectively _**A --2--> B**_ and _**A --4--> B**_ and _**A --6--> B**_, the weights would become 2/12, 4/12, and 6/12.  Possibly apply sequence alignment first

### IDEA: Optimize merging ###
Early tests suggest that merging is slow compared to the rest of Perfume. Possibly optimize the merging process by maintaining a list/matrix of already-rejected merges to cut down on the amount of model checking needed during merging.

### IDEA: Compare Synoptic and naive Perfume ###
Given a log where each event occurred exactly 1 unit (e.g., second) after the previous one, Synoptic and Perfume will produce different models even though no illuminating performance information is present. Experimenting with this might reveal something about one method or the other.

### IDEA: Remove monotonically-increasing restriction ###
Perfume is currently restricted to modeling logs where each trace's performance information is monotonically increasing (e.g., time, total network traffic). It would become more widely applicable if non-monotonically-increasing traces were supported, as this would allow metrics such as memory usage, CPU usage, network transfer speed, and disk I/O to be used to infer Perfume models.

#### Required changes ####
  * Mining may or may not need to change. If it does, changes will likely be confined to ConstrainedInvariantMiner.
  * Model checking will definitely need to change. Currently, much of it expects that when an upper bound is violated or a lower bound is satisfied, the violation or satisfaction cannot be undone, as this is the case when confined to monotonically-increasing series. State machines for the invariants (TracingStateSets) and/or the model checker (FsmModelChecker) will need to be updated, and likely other classes will as well.

### IDEA: Investigate the delta between Synoptic and Perfume models ###

When Synoptic and Perfume models are identical, it means that
the performance invariants do not induce any further refinement and
that Synoptic invariants encompass the Perfume
invariants.

Now, if the Synoptic and Perfume models are different, we are
_guaranteed_ to have less behavior in the Perfume model (traces in the
Perfume model have to satisfy the same Synoptic invariants +
performance invariants, so if this causes more refinement then there
will be less behavior in the Perfume model).

If the two models differ, we could focus/show the
parts of the model that are different and ignore the rest. That is,
show just the sub-model of Perfume that is relevant to the splits that
removed behavior that is present in the Synoptic model. In some sense,
Perfume's refinement that goes beyond the Synoptic model can be used
to focus user's attention on parts of the model that are most relevant
to performance (since the Synoptic invariants are mostly there to
assure sufficient model-log matching accuracy).