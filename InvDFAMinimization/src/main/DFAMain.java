package main;

import model.InvModel;
import model.InvsModel;
import model.ModelFactory;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.model.EventType;
import synoptic.model.PartitionGraph;
import synoptic.model.StringEventType;

/**
 * InvDFAMinimization accepts a log file and regular expression arguments and uses Synoptic to
 * parse the log and mine invariants. Implicit NIFby invariants along with an Initial/Terminal
 * invariant are used to construct and export an initial dfa model. Synoptic's mined invariants
 * are then intersected with the initial model to construct and export a final model.
 * 
 * @author Jenny
 */
public class DFAMain {

	public static void main(String[] args) throws Exception {
        Main synMain = Main.processArgs(args);
        PartitionGraph initialModel = synMain.createInitialPartitionGraph();
        
        // Construct initial dfa from NIFby invariants.
        TemporalInvariantSet NIFbys = initialModel.getNIFbyInvariants();
        InvsModel dfa = ModelFactory.getModel(NIFbys);
        
        // Intersect with initial/terminal InvModel.
        /* 
         * TODO: Replace once getInitial and getTerminal are implemented
         * EventType initial = initialModel.getInitialEvent();
         * EventType terminal = initialModel.getTerminalEvent();
         */
        EventType initial = StringEventType.newInitialStringEventType();
        EventType terminal = StringEventType.newTerminalStringEventType();
		dfa.intersectWith(InvModel.initialTerminalInv(initial, terminal));
		
		// Limit the alphabet to currently encoded names.
		dfa.finalizeAlphabet();
		
		// Export initial model.
		dfa.exportDotAndPng("initialDfaModel.dot");
		
		// Intersect with mined invariants.
        TemporalInvariantSet minedInvariants = initialModel.getInvariants();
		dfa.intersectWith(ModelFactory.getModel(minedInvariants));
		
		// Minimize the model.
		dfa.minimize();
		
		// Export final model.
        dfa.exportDotAndPng("finalDfaModel.dot");
	}
}