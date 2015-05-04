# CSight parallel model checking with McScM #

CSight includes an option to run multiple McScM model checking runs in parallel. We've observed 2-7x CSight speedup when using this option.

## Experimental results ##

As an example of the speed-up we observed, the following table shows the run times for 3 CSight test-cases with a parallelization factor of 4 on a quad-core machine (Intel Core i5 CPU at 2.8GHz). OS is Ubuntu 14.04 LTS, with Java version 1.8.0\_11 and McScM version 1.2.1. Times are recorded as the average of 10 runs:

| Test-case | Time (seconds) |Time (seconds)| Speed up (x) |
|:----------|:---------------|:-------------|:-------------|
|  | Default (non-parallel) | Parallel (p = 4) |  |
| runABPSuccess | 22.181 | 4.026 | 5.5 |
| runTCPTrace | 69.415 | 24.903 | 2.8 |
| runSimpleConcurrencyString2Success | 60.494 | 8.665 | 7.0 |

To replicate the _non-parallelized_ timing for runABPSuccess, use this command:
`./timeTestCase.sh csight.main.CSightMainTests runABPSuccess`

To replicate the _parallelized_ timing for runABPSuccess, use this command:
`./timeTestCase.sh csight.main.CSightMainTests runABPSuccessParallel`

The other test-cases have similarly named targets.


## How it works ##

Parallel model checking runs **N** McScM model checking processes concurrently. There is one main thread that handles the invariants and their results to perform model refinement, and a second thread that manages the **N** model checking processes (and their corresponding Java threads).

When an invariant is satisfied, another invariant is checked to maintain the parallelization factor, unless there are no more invariants to check, in which case we wait for the invariant currently being checked to finish. When McScM reports an invariant violation, the model is refined and all other model checking processes are stopped (the model that those runs are checking is now stale). When an invariant times out, the time-out value for that invariant is increased, and the invariant will be re-checked after all other invariants have been checked, which allows us to complete the faster invariants first, and then the slower invariants (which may now be faster to check as the model has been refined). Parallel model checking continues until all invariants are satisfied, or invariants cannot be checked within the max time-out value.


## Implementation details ##

The McScMParallelizer runs the McScM processes. It does so when commanded by CSightMain, so CSightMain always know if McScMParallelizer is checking invariants, and results are returned to CSightMain when checking completes.

Though CSightMain is allowed to send tasks to McScMParallelizer in any order, provided the number of tasks does not exceed **N** (the max parallelization factor noted above), CSightMain currently implements the following algorithm when a model checking result is returned:

  * Invariant satisfied: CSightMain tells McScMParallelizer to start one new model checking run to maintain the parallelization factor, unless there are no more invariants to check, in which case CSightMain continues to wait for the next completed invariant.

  * Invariant not satisfied: CSightMain tells McScMParallelizer to stop all previous processes, refine the model against the returned counter-example, increase the refinement counter so all results from previous models are ignored, and start **N** model checking processes on the new model.

  * Invariant times out: CSightMain increases the timeout for that invariant and add the invariant to the back of the queue of invariants to check so that faster invariants are checked first, and sends the next immediate invariant in the queue for checking.


McScMParallelizer and CSightMain communicate through two queues: taskChannel and resultsChannel:

  * taskChannel queue: A BlockingQueue with capacity one that CSightMain uses to send tasks/commands to McScMParallelizer. ParallelizerTask contains a ParallelizerCommands to tell McScMParallelizer which task to do, a ParallelizerInput that corresponds to the command, and the refinement counter to act as a guard to prevent out-dated model checking runs from executing. There are three kinds of ParallelizerCommands:
    * START\_K: Starts K model checking processes, where K = min{N, invsToCheck.size()}.
    * START\_ONE: Starts one model checking process.
    * STOP\_ALL: Stop all checking processes discarding their results.
> > ParallelizerInput consists of the invariant to check, the model to check the invariant against, and the time-out in seconds.

  * resultsChannel queue: An unbounded BlockingQueue that McScMParallelizer uses to communicate completed model checking results to CSightMain. Each ParallelizerResult contains the invariant for the model checking run, the MCResult class, and the refinement counter to prevent CSightMain from using out-dated results. ParallelizerResult can also pass exceptions to CSightMain using this queue.