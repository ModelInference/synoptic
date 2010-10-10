package invariants.ltlcheck;

import java.util.HashMap;

import benchmarks.PerformanceMetrics;
import benchmarks.TimedTask;

import invariants.TemporalInvariant;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;
import gov.nasa.ltl.graph.SynchronousProduct;
import gov.nasa.ltl.trans.ParseErrorException;

public class LtlModelChecker {
	/**
	 * Cache graphs that we got as argument previously. This is possible since
	 * the only caller is GraphLTLChecker.check, and it guarantees that whenever
	 * the graph changes, a new graph object is passed to this method.
	 */
	private static HashMap<Graph, Graph> translationCache = new HashMap<Graph, Graph>();
	//CACHE: cache translated graphs

	public static Counterexample check(Graph transitionSystem,
			TemporalInvariant invariant, IModelCheckingMonitor monitor)
			throws ParseErrorException {
		assert monitor != null;

		// Generate Did/Can Expanded Graph
		Graph didCanTransitionSystem = transitionSystem;

		TimedTask didCanTrans = PerformanceMetrics.createTask("didCanTranslation");
		if (TemporalInvariant.useDIDCAN) {
			if (translationCache.containsKey(transitionSystem)) {
				monitor.subTask("Adding did/can attributes... (cached)");
				didCanTransitionSystem = translationCache.get(transitionSystem);
			} else {
				monitor.subTask("Adding did/can attributes...");
				didCanTransitionSystem = DidCanTranslator
						.translate(transitionSystem);
				translationCache.put(transitionSystem, didCanTransitionSystem);
			}
			if (translationCache.size() > 5) {
				translationCache.clear();
			}
		}
		didCanTrans.stop();

		// Remove deadlock
		monitor.subTask("Massaging deadlock states...");
		GraphTransformations.removeDeadlock(didCanTransitionSystem);

		// Generate Buchi Automata for negated LTL formula
		TimedTask buchiTrans = PerformanceMetrics.createTask("buchiTrans");
		monitor.subTask("Generate Buchi automaton...");
		Graph ba = invariant.getAutomaton();
		buchiTrans.stop();

		monitor.subTask("Parsing transition labels...");
		GraphActionParser.parseTransitions(ba);

		// Generate Product Automata of Did/Can Expanded Graph and Buchi
		// Automata of LTL formula
		TimedTask productAutomaton = PerformanceMetrics.createTask("productAutomaton");
		monitor.subTask("Generate product automaton...");
		final GeneralGraph pa = ProductTranslator.translate(
				didCanTransitionSystem, ba);
		productAutomaton.stop();

		// Check Property via reachable cycle detection
		TimedTask cycleChecking = PerformanceMetrics.createTask("cycleChecking");
		monitor.subTask("Checking property...");
		final PersistenceChecker pc = new PersistenceChecker(pa);
		pc.run();
		cycleChecking.stop();

		return pc.getCounterexample();

	}
}
