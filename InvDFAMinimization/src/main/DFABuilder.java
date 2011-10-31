package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.EventType;
import synoptic.model.export.GraphExporter;

/**
 * Builds a DFA representation of the Synoptic initial models by intersecting
 * all NIFby invariants and stipulating INITIAL and TERMINAL conditions.
 * 
 * @author Jenny
 */
public class DFABuilder {

	// DFA of the initial model.
	private Automaton initialModel;

	// DFA of the final model.
	private Automaton finalModel;

	// Map of encoding characters to EventType labels.
	private Map<Character, String> encodings;
	
	// Number of possible unicode characters, for encoding EventType labels.
	private static final int UNICODE_CHARS = 0x10FFFF;

	// Character encodings for the initial and terminal EventTypes.
	private static char INITIAL;
	private static char TERMINAL;

	public DFABuilder(TemporalInvariantSet NIFbys, TemporalInvariantSet minedInvariants) {
		this.encodings = new HashMap<Character, String>();
		initialModel = buildInitialDFAModel(NIFbys);
		finalModel = buildFinalModel(minedInvariants);
	}

	/* Returns a character encoding for the given EventType. Updates the encodings map and possibly
	 * INITIAL/TERMINAL characters if this EventType has not been seen before.
	 * TODO: Address possibility of encoding collisions */
	private char getEncoding(EventType event) {
		char encoding = (char) (event.hashCode() % UNICODE_CHARS);
		if (!encodings.containsKey(encoding)) {
			if (event.isInitialEventType()) {
				INITIAL = encoding;
			} else if (event.isTerminalEventType()) {
				TERMINAL = encoding;
			}
			encodings.put(encoding, event.toString());
		}
		return encoding;
	}

	/* Builds the initial DFAModel from the given set of NIFby invariants. */
	private Automaton buildInitialDFAModel(TemporalInvariantSet NIFbys) {

		Automaton model = intersectModelWith(BasicAutomata.makeAnyString(), NIFbys);

		// Add INITIAL/TERMINAL constraints.
		model = BasicOperations.intersection(model, new RegExp(
				initialAndTerminalRegex()).toAutomaton());

		// Use the encodings to limit the alphabet of the automaton.
		model = BasicOperations.intersection(model,
				new RegExp(alphabetRegex()).toAutomaton());

		// Optimize by minimizing the model.
		model.minimize();

		return model;
	}

	/* Builds the final DFAModel from the initial model and the given set of mined invariants. */
	private Automaton buildFinalModel(TemporalInvariantSet minedInvariants) {
		Automaton finalModel = intersectModelWith(initialModel.clone(), minedInvariants);
		finalModel.minimize();
		return finalModel;
	}
	
	/* Intersects the given model with DFAs for all of the given invariants */
	private Automaton intersectModelWith(Automaton model, TemporalInvariantSet invariants) {
		for (ITemporalInvariant invariant : invariants) {
			char source = getEncoding(invariant.getFirst());
			char dest = getEncoding(invariant.getSecond());
			
			RegExp re = new RegExp(invariant.getRegex(source, dest));
			model = BasicOperations.intersection(model, re.toAutomaton());
		}
		return model;
	}

	/* Returns a regular expression describing the INITIAL and TERMINAL constraints for the DFA. */
	private String initialAndTerminalRegex() {
		return INITIAL + "[^" + INITIAL + TERMINAL + "]*" + TERMINAL;
	}

	/* Returns a regular expression describing the alphabet for this DFA,
	 * eliminating extraneous characters. */
	private String alphabetRegex() {
		StringBuilder alphabet = new StringBuilder();
		for (Character character : encodings.keySet()) {
			alphabet.append("|" + character);
		}
		alphabet.replace(0, 1, "("); // Hacky fix to fence post issue.
		alphabet.append(")*");
		return alphabet.toString();
	}
	
	/**
	 * Exports this Builder's model as a Graphviz dot file and associated png.
	 * 
	 * @param filename
	 *            the name of the dot file
	 * @param exportFinal
	 * 			  whether to export the final or initial model
	 * @throws IOException
	 */
	public void exportDotAndPng(String filename, boolean exportFinal) throws IOException {
		String dot = toGraphviz(exportFinal);
		Writer output = new BufferedWriter(new FileWriter(new File(filename)));
		try {
			output.write(dot);
		} finally {
			output.close();
		}
		GraphExporter.generatePngFileFromDotFile(filename);
	}

	/* Constructs a Graphviz dot representation of the model. */
	private String toGraphviz(boolean exportFinal) {
		Automaton model = exportFinal ? finalModel : initialModel;
		StringBuilder b = new StringBuilder("digraph {\n");
		
		// Assign states consecutive numbers.
		Set<State> states = model.getStates();
		Map<State, Integer> stateOrdering = new HashMap<State, Integer>();
		int number = 0;
		for (State s : states) {
			stateOrdering.put(s, number++);
		}

		for (State s : states) {
			b.append("  ").append(stateOrdering.get(s));
			if (s.isAccept())
				b.append(" [shape=doublecircle,label=\"\"];\n");
			else
				b.append(" [shape=circle,label=\"\"];\n");
			if (s == initialModel.getInitialState()) {
				b.append("  initial [shape=plaintext,label=\"\"];\n");
				b.append("  initial -> ").append(stateOrdering.get(s))
						.append("\n");
			}
			for (Transition t : s.getTransitions()) {
				int source = stateOrdering.get(s);
				int dest = stateOrdering.get(t.getDest());
				char cur = t.getMin();
				appendTransition(b, encodings.get(cur), source, dest);
				while (cur < t.getMax()) {
					cur++;
					appendTransition(b, encodings.get(cur), source, dest);
				}
			}
		}
		return b.append("}\n").toString();
	}

	/* Add a transition line to the given StringBuilder with the given label, from the source
	 * to the dest nodes. */
	private void appendTransition(StringBuilder b, String label, int source,
			int dest) {
		b.append("  ").append(source);
		b.append(" -> ").append(dest).append(" [label=\"");
		b.append(label);
		b.append("\"]\n");
	}
}
