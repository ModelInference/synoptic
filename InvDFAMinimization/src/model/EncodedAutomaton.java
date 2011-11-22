package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import synoptic.model.export.GraphExporter;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * Wrapper class for dk.brics.automaton.Automaton which provides name encodings for building
 * Automaton with Strings rather than characters.
 * 
 * @author Jenny
 *
 */
public class EncodedAutomaton {

	// The Automaton wrapped with String encodings.
	private Automaton model;
	
	// Stores encodings for all characters in the model.
	private Map<Character, String> encoding;
	
	// Range of possible unicode characters for encoding names.
	private static final int UNICODE_CHARS = 0x10FFFF;
	
	/**
	 * Constructs a new EncodedAutomaton that accepts all strings.
	 */
	public EncodedAutomaton() {
		this.model = BasicAutomata.makeAnyString();
		this.encoding = new HashMap<Character, String>();
	}
	
	/**
	 * Limits this Automaton to names current encoded.
	 */
	public void finalizeAlphabet() {
		StringBuilder alphabet = new StringBuilder();
		for (Character character : encoding.keySet()) {
			alphabet.append("|" + character);
		}
		alphabet.replace(0, 1, "("); // Hacky fix to fence post issue.
		alphabet.append(")*");
		intersectWithRE(alphabet.toString());
	}

	/**
	 * Intersects this Automaton with a model created with the given (encoded) regular expression.
	 */
	public void intersectWithRE(String re) {
		model = BasicOperations.intersection(model, new RegExp(re).toAutomaton());
	}
	
	/**
	 * Intersects this Automaton with the given Automaton, accepting all of
	 * other's current encodings.
	 */
	public void intersectWith(EncodedAutomaton other) {
		// Add other's encodings.
		encoding.putAll(other.encoding);
		
		// Update this model.
		model = BasicOperations.intersection(model, other.model);
	}
	
	/* Returns a character encoding for the given name. Updates the encodings map.
	 * TODO: Address possibility of encoding collisions */
	protected char getEncoding(String name) {
		char c = (char) (name.hashCode() % UNICODE_CHARS);
		if (!encoding.containsKey(c)) {
			encoding.put(c, name.toString());
		}
		return c;
	}
	
	/**
	 * Uses Hopcroft's algorithm to minimize this Automaton.
	 */
	public void minimize() {
		model.minimize();
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
	public void exportDotAndPng(String filename) throws IOException {
		String dot = toGraphviz();
		Writer output = new BufferedWriter(new FileWriter(new File(filename)));
		try {
			output.write(dot);
		} finally {
			output.close();
		}
		GraphExporter.generatePngFileFromDotFile(filename);
	}
	
	/** Constructs a Graphviz dot representation of the model. */
	public String toGraphviz() {
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
			if (s == model.getInitialState()) {
				b.append("  initial [shape=plaintext,label=\"\"];\n");
				b.append("  initial -> ").append(stateOrdering.get(s))
						.append("\n");
			}
			for (Transition t : s.getTransitions()) {
				int source = stateOrdering.get(s);
				int dest = stateOrdering.get(t.getDest());
				char cur = t.getMin();
				appendDotTransition(b, encoding.get(cur), source, dest);
				while (cur < t.getMax()) {
					cur++;
					appendDotTransition(b, encoding.get(cur), source, dest);
				}
			}
		}
		return b.append("}\n").toString();
	}

	/* Add a transition line to the given StringBuilder with the given label, from the source
	 * to the dest nodes. */
	private void appendDotTransition(StringBuilder b, String label, int source,
			int dest) {
		b.append("  ").append(source);
		b.append(" -> ").append(dest).append(" [label=\"");
		b.append(label);
		b.append("\"]\n");
	}
}