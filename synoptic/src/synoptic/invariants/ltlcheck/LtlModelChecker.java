package synoptic.invariants.ltlcheck;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.trans.ParseErrorException;

import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.ITemporalInvariant;

public class LtlModelChecker {
    private static Logger logger = Logger.getLogger("LtlModelChecker Logger");

    /**
     * Cache graphs that we got as argument previously. This is possible since
     * the only caller is GraphLTLChecker.check, and it guarantees that whenever
     * the graph changes, a new graph object is passed to this method.
     */
    private static LinkedHashMap<Graph, Graph> translationCache = new LinkedHashMap<Graph, Graph>();

    // CACHE: cache translated graphs

    public static Counterexample check(Graph transitionSystem,
            ITemporalInvariant invariant) throws ParseErrorException {

        // Generate Did/Can Expanded Graph
        // Graph didCanTransitionSystem = transitionSystem;
        Graph didCanTransitionSystem = null;

        TimedTask didCanTrans = PerformanceMetrics
                .createTask("didCanTranslation");

        if (translationCache.containsKey(transitionSystem)) {
            logger.finest("Adding did/can attributes... (cached)");
            didCanTransitionSystem = translationCache.get(transitionSystem);
        } else {
            logger.finest("Adding did/can attributes...");
            didCanTransitionSystem = DidCanTranslator
                    .translate(transitionSystem);
            translationCache.put(transitionSystem, didCanTransitionSystem);
        }
        // TODO: why limit the translation cache to 5 entries?
        if (translationCache.size() > 5) {
            translationCache.clear();
        }

        didCanTrans.stop();

        // Remove deadlock
        GraphTransformations.removeDeadlock(didCanTransitionSystem);

        // Generate Buchi Automata for negated LTL formula
        TimedTask buchiTrans = PerformanceMetrics.createTask("buchiTrans");
        logger.finest("Generate Buchi automaton...");
        Graph ba = invariant.getAutomaton();
        buchiTrans.stop();

        logger.finest("Parsing transition labels...");
        GraphActionParser.parseTransitions(ba);

        // Generate Product Automata of Did/Can Expanded Graph and Buchi
        // Automata of LTL formula
        TimedTask productAutomaton = PerformanceMetrics
                .createTask("productAutomaton");
        logger.finest("Generate product automaton...");
        final GeneralGraph pa = ProductTranslator.translate(
                didCanTransitionSystem, ba);
        productAutomaton.stop();

        // Check Property via reachable cycle detection
        TimedTask cycleChecking = PerformanceMetrics
                .createTask("cycleChecking");
        logger.finest("Checking property...");
        final PersistenceChecker pc = new PersistenceChecker(pa);
        pc.run();
        cycleChecking.stop();

        return pc.getCounterexample();

    }
}
