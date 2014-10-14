package synoptic.invariants;

/**
 * <p>
 * Provides implementation of various types of invariants, code to mine
 * invariants, and model checkers that can check invariant validity over
 * partition graphs.
 * </p>
 * <h3>Temporal Invariants</h3>
 * <p>
 * Classes representing temporal invariants implement the
 * <code>ITemporalInvariant</code> interface. A set of invariants is represented
 * as a <tt>TemporalInvariantSet</tt>. There are different kinds of invariants,
 * and corresponding invariant miners, depending on the use-case.
 * </p>
 * <p>
 * The standard set of invariants is:
 * <ul>
 * <li>a AlwaysFollowedBy b (a AFby b) : <code>AlwaysFollowedByInvariant</code></li>
 * <li>a NeverFollowedBy b (a NFby b) : <code>NeverFollowedByInvariant</code></li>
 * <li>a AlwaysPrecedes b (a AP b) : <code>AlwaysPrecedesInvariant</code></li>
 * </ul>
 * In addition to these invariants, there are the following invariants:
 * <ul>
 * <li>Concurrency invariants (only mined by partially ordered (PO) traces, not
 * model checked, yet)</li>
 * <ul>
 * <li>a AlwaysConcurrentWith b (a || b) :
 * <code>AlwaysConcurrentInvariant</code></li>
 * <li>a NeverConcurrentWith b : <code>NeverConcurrentInvariant</code></li>
 * </ul>
 * <li>Constrained invariants (work in progress; used when the log contains
 * timestamps indicating when events occur; adds constraints to each of the
 * standard invariants with a temporal constraint, e.g., a AFby b, in < t time.)
 * </li>
 * <ul>
 * <li>Inv(a,b) < t : every pair of event instances (a,b) is separated by at
 * <b>most</b> t time</li>
 * <li>Inv(a,b) > t : every pair of event instances (a,b) is separated by at
 * <b>least</b> t time</li>
 * </ul>
 * <li>KTail invariants : these are used by InvariMint</li>
 * <li>NeverImmediatelyFollowed invariants : these are used by InvariMint</li>
 * </ul>
 * <p>
 * Each set of invariants above has an invariant miner to generate it from an
 * input <code>TraceGraph</code>, which is the data structure representing the
 * input log. A TraceGraph is either a <code>ChainTraceGraph</code> (totally
 * ordered) or a <code>DAGTraceGraph</code> (partially ordered). For a
 * ChainTraceGraph there are two miners:
 * <ul>
 * <li><code>ChainWalkingTOInvMiner</code></li>
 * <li><code>TransitiveClosureInvMiner</code></li>
 * </ul>
 * For a DAGTraceGraph there is the DAGWalkingPOInvariantMiner.
 * </p>
 * <p>
 * There are two model checkers for invariants: FSM checker (fsmcheck package),
 * and LTL checker (ltlcheck, ltlchecker packages).
 * </p>
 * <h3>FSM checker</h3>
 * <p>
 * A customized checker that is purposefully built for the standard set of
 * invariants listed above. The basic idea is to create a small FSM per
 * invariant, and to update this FSM while walking the model. If we end up in an
 * FSM reject state once we've reached the model terminal state then the
 * invariant does not hold along some path in the model. In addition, the
 * checker tracks history so that a counter-example path can be generated, and
 * it optimizes the checking of multiple invariants along multiple paths
 * simultaneously by updating the state of multiple FSMs during traversal using
 * bit arrays.
 * </p>
 * <h3>LTL checker (mostly deprecated, because it is slower than the FSM
 * checker)</h3>
 * <p>
 * Temporal invariants are checked using an off-the-shelf model checker, with
 * some preprocessing of the input graph. The preprocessing is necessary because
 * the model checker will find violating traces, such that a trace is a sequence
 * of transition labels and the traces that need to be obtained in the partition
 * graph setting are sequences of state labels. Thus the partition graph must be
 * transformed into a graph that contains, as transition label sequences,
 * exactly the set of state label sequences of the partition graph. We need to
 * prove the soundness of this process. The entry point to the model checking
 * logic is <tt>TemporalInvariantSet.getViolations</tt>, which utilizes
 * <code>GraphLTLChecker</code>
 * </p>
 */
