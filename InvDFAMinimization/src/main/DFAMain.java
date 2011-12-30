package main;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;
import model.SynopticModel;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.main.TraceParser;
import synoptic.model.EventType;
import synoptic.model.PartitionGraph;
import synoptic.model.StringEventType;

/**
 * InvDFAMinimization accepts a log file and regular expression arguments and
 * uses Synoptic to parse the log and mine invariants. Implicit NIFby invariants
 * along with an Initial/Terminal invariant are used to construct an initial DFA
 * model. The initial model is then intersected with each of the invariants
 * mined by Synoptic to construct and export a final model. For comparison
 * purposes, the final Synoptic model is translated to a DFA and exported. The
 * program prints whether the DFAmin language is a subset of the translated
 * Synoptic DFA language and vice versa.
 * 
 * @author Jenny
 */
public class DFAMain {
    /**
     * Main entrance into the application. Application arguments (args) are
     * processed using Synoptic's build-in argument parser, extended with a few
     * InvDFAMinimization-specific arguments. For more information, see
     * InvDFAMinimizationOptions class.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Set up Synoptic.
        InvDFAMinimizationOptions opts = new InvDFAMinimizationOptions(args);
        Main synMain = Main.processArgs(args);
        PartitionGraph initialModel = synMain.createInitialPartitionGraph();

        // Construct initial DFA from NIFby invariants.
        TemporalInvariantSet NIFbys = initialModel.getNIFbyInvariants();
        Set<EventType> allEvents = new HashSet<EventType>(
                initialModel.getEventTypes());

        EventTypeEncodings encodings = new EventTypeEncodings(allEvents);
        InvsModel dfa = getMinModelFromInvs(NIFbys, encodings);

        // Intersect with initial/terminal InvModel.
        /*
         * TODO: Replace once getInitial and getTerminal are implemented (Issue
         * 173). EventType initial = initialModel.getInitialEvent(); EventType
         * terminal = initialModel.getTerminalEvent();
         */
        EventType initial = StringEventType.newInitialStringEventType();
        EventType terminal = StringEventType.newTerminalStringEventType();
        InvModel initialTerminalInv = new InvModel(
                new TOInitialTerminalInvariant(initial, terminal,
                        TraceParser.defaultRelation), encodings);
        dfa.intersectWith(initialTerminalInv);

        // Intersect with mined invariants.
        TemporalInvariantSet minedInvariants = initialModel.getInvariants();
        dfa.intersectWith(getMinModelFromInvs(minedInvariants, encodings));
        dfa.minimize();

        // Export final model.
        dfa.exportDotAndPng(opts.finalModelFile);

        compareTranslatedSynopticModel(synMain, initialModel, encodings,
                opts.synopticModelFile, dfa);
    }

    /**
     * Runs Synoptic on the initial model then translates the final Synoptic
     * model to a DFA. The Synoptic DFA is exported and then compared with the
     * final DFAmin model.
     * 
     * @throws IOException
     */
    private static void compareTranslatedSynopticModel(Main synMain,
            PartitionGraph initialModel, EventTypeEncodings encodings,
            String synFileName, EncodedAutomaton dfa) throws IOException {

        // To compare, we'll translate and export the Synoptic model.
        // First run synoptic on the initial model, initial model becomes final
        // model.
        synMain.runSynoptic(initialModel);
        SynopticModel convertedDfa = new SynopticModel(initialModel, encodings);
        convertedDfa.minimize();
        convertedDfa.exportDotAndPng(synFileName);

        // Print whether the language accepted by dfa is a subset of the
        // language accepted by synDfa and vice versa.
        System.out
                .println("DFAmin language a subset of translated Synoptic DFA language: "
                        + dfa.subsetOf(convertedDfa));
        System.out
                .println("Translated Synoptic DFA language a subset of DFAmin language: "
                        + convertedDfa.subsetOf(dfa));
    }

    /**
     * Constructs an InvsModel by intersecting InvModels for each of the given
     * temporal invariants.
     * 
     * @param invariants
     *            a set of TemporalInvariants
     * @return the intersected InvsModel
     */
    public static InvsModel getMinModelFromInvs(
            TemporalInvariantSet invariants, EventTypeEncodings encodings) {
        // Initial model will accept all Strings.
        InvsModel model = new InvsModel(encodings);

        // Intersect provided invariants.
        for (ITemporalInvariant invariant : invariants) {
            InvModel current = new InvModel(invariant, encodings);
            model.intersectWith(current);
        }

        // Optimize by minimizing the model.
        model.minimize();

        return model;
    }
}