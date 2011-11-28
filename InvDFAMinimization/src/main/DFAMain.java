package main;

import model.InvModel;
import model.InvsModel;

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
 * along with an Initial/Terminal invariant are used to construct and export an
 * initial DFA model. Synoptic's mined invariants are then intersected with the
 * initial model to construct and export a final model.
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
        InvDFAMinimizationOptions opts = new InvDFAMinimizationOptions(args);
        Main synMain = Main.processArgs(args);
        PartitionGraph initialModel = synMain.createInitialPartitionGraph();

        // Construct initial DFA from NIFby invariants.
        TemporalInvariantSet NIFbys = initialModel.getNIFbyInvariants();
        InvsModel dfa = getMinModelFromInvs(NIFbys);

        // Intersect with initial/terminal InvModel.
        /*
         * TODO: Replace once getInitial and getTerminal are implemented (Issue
         * 173) EventType initial = initialModel.getInitialEvent(); EventType
         * terminal = initialModel.getTerminalEvent();
         */
        EventType initial = StringEventType.newInitialStringEventType();
        EventType terminal = StringEventType.newTerminalStringEventType();
        InvModel initialTerminalInv = new InvModel(
                new TOInitialTerminalInvariant(initial, terminal,
                        TraceParser.defaultRelation));
        dfa.intersectWith(initialTerminalInv);

        // Export initial model.
        dfa.exportDotAndPng(opts.initialModelFile);

        // Intersect with mined invariants.
        TemporalInvariantSet minedInvariants = initialModel.getInvariants();
        dfa.intersectWith(getMinModelFromInvs(minedInvariants));

        // Minimize the model.
        dfa.minimize();

        // Export final model.
        dfa.exportDotAndPng(opts.finalModelFile);
    }

    /**
     * Constructs an InvsModel by intersecting InvModels for each of the given
     * temporal invariants.
     * 
     * @param invariants
     *            a set of TemporalInvariants
     * @return the intersected InvsModel
     */
    public static InvsModel getMinModelFromInvs(TemporalInvariantSet invariants) {
        // Initial model will accept all Strings.
        InvsModel model = new InvsModel();

        // Intersect provided invariants.
        for (ITemporalInvariant invariant : invariants) {
            InvModel current = new InvModel(invariant);
            model.intersectWith(current);
        }

        // Optimize by minimizing the model.
        model.minimize();

        return model;
    }
}