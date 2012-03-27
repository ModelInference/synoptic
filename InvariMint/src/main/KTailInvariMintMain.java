package main;

import model.EventTypeEncodings;
import model.InvsModel;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.PartitionGraph;

/**
 * KTails InvariMint accepts a log file and regular expression arguments and
 * uses Synoptic to parse the log and mine k tail invariants for tails of length
 * k, as specified by the kTailLength option. Implicit NIFby invariants along
 * with an Initial/Terminal invariant are used to construct an initial DFA
 * model. The initial model is then intersected with each of the k tail
 * invariants to construct and export a final model.
 * 
 * @author Jenny
 */
public class KTailInvariMintMain {

    public static void main(String[] args) throws Exception {
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintMain.setUpLogging(opts);
        InvariMintMain.handleOptions(opts);
        ChainsTraceGraph inputGraph = InvariMintMain.setUpSynoptic(opts);

        // Set up Synoptic to mine k Tails
        Main.options.performKTails = true;
        Main.options.kTailLength = opts.kTailLength;

        // Mine kTail invariants
        TemporalInvariantSet minedInvs = Main.mineTOInvariants(false,
                inputGraph);

        // Construct initial model
        PartitionGraph initialModel = new PartitionGraph(inputGraph, true,
                minedInvs);

        // Mine immediate invariants
        TemporalInvariantSet NIFbys = initialModel.getNIFbyInvariants();

        // Generate encodings
        EventTypeEncodings encodings = new EventTypeEncodings(
                initialModel.getEventTypes());

        // Construct initial DFA from NIFby invariants.
        InvsModel dfa = InvariMintMain.getMinModelFromInvs(NIFbys, encodings);

        // Intersect initial/terminal invariant
        InvariMintMain.applyInitialTerminalCondition(dfa, encodings);

        dfa.exportDotAndPng(opts.outputPathPrefix + ".initial.dot");

        // Intersect mined kTail invariants.
        dfa.intersectWith(InvariMintMain.getMinModelFromInvs(minedInvs,
                encodings));

        dfa.minimize();

        // Export final model.
        dfa.exportDotAndPng(opts.outputPathPrefix + ".invarimintDFA.dot");
    }
}
