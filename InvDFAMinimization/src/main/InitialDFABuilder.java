package main;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;

import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.model.EventType;

/**
 * Builds a DFA representation of the Synoptic initial models by intersecting all NIFby invariants
 * and stipulating INITIAL and TERMINAL conditions. Currently provides a rudimentary means of
 * displaying this model.
 * 
 * @author Jenny
 */
public class InitialDFABuilder {
	
	private Automaton model;

	public InitialDFABuilder(Set<NeverImmediatelyFollowedInvariant> NIFbys) {
		this.encodings = new HashMap<EventType, Character>();
		model = buildInitialDFAModel(NIFbys);
	}

    private Automaton buildInitialDFAModel(Set<NeverImmediatelyFollowedInvariant> NIFbys) {

    	// Start with an Automaton containing the INITIAL and TERMINAL conditions.
    	Automaton model = new RegExp(initialAndTerminalRegex()).toAutomaton();
    	
    	// Constructs all of the NIFby DFAs and intersects each with the model.
    	// TODO: As a side effect, this loop generates the EventType encodings -- should this be
    	// a separate, earlier step?
    	for (NeverImmediatelyFollowedInvariant invariant : NIFbys) {
    		char source = getEncoding(invariant.getFirst());
    		char target = getEncoding(invariant.getSecond());
    		
    		RegExp re = new RegExp(NIFbyRegex(source, target));
    		model = BasicOperations.intersection(model,  re.toAutomaton());
    	}
    	
    	// Use the encodings to limit the alphabet of the automaton.
    	// TODO: This seems to be an optional step, but improves both correctness of the model and
    	// readability of the output
    	model = BasicOperations.intersection(model, new RegExp(alphabetRegex()).toAutomaton());
    	
    	// Optimize by minimizing the model.
    	model.minimize();

    	return model;
    }
    
	/**
	 * Displays the initial model.
	 * TODO: convert automatically back to EventType representations, return a representation rather
	 * than printing, provide graphical output
	 */
    public void displayModel() {
    	System.out.println(encodings);
    	System.out.println(model);
    }
    
	/**
	 * Returns a regular expression describing the NIFby invariant for source NIFby target. For
	 * x NIFby y, the expression is "([^x]|x[^y])*x*".
	 * 
	 * @param source a character representation of the first invariant EventType
	 * @param target a character representation of the second invariant
	 * @return a regex describing this invariant for source NIFby target
	 */
	private String NIFbyRegex(char source, char target) {
		return "([^" + source + "]|" + source + "[^" + target + "])*" + source + "*";
	}
	
	/**
	 * Returns a regular expression describing the INITIAL and TERMINAL constraints for the DFA.
	 * 
	 * @return a regex describing the INITIAL and TERMINAL constraints for the DFA
	 */
	private String initialAndTerminalRegex() {
		return INITIAL + "[^" + INITIAL + TERMINAL + "]*" + TERMINAL;
	}
	
	/**
	 * Returns a regular expression describing the alphabet for this DFA (eliminating extraneous
	 * unicode characters).
	 * 
	 * @return a regex describing the alphabet for this DFA
	 */
    private String alphabetRegex() {
    	StringBuilder alphabet = new StringBuilder();
    	alphabet.append("(" + INITIAL + "|" + TERMINAL);
    	for (Character character : encodings.values()) {
    		alphabet.append("|" + character);
    	}
    	alphabet.append(")*");
    	return alphabet.toString();
    }
    

	/*
	 * TODO: Fix encoding method below.
	 * The current method of encoding is human readable, useful for manually drawing the DFA and
	 * converting back to EventType representations. Once these items are handled automatically,
	 * a more scalable encoding method will be necessary.
	 * One other possible method for generating encodings is to map each EventType to a unicode
	 * character using its hash function, and mod-ing by the number of possible unicode chars, ie:
	 * // Range of unicode characters
	 * private static final int UNICODE_CHARS = 0x10FFFF;
	 * char encoding = (char) (event.hashCode() % UNICODE_CHARS);
	 * To convert back, we could also maintain a map from Character->EventType for easy look up.
	 */
	
	private char currentChar = 'a';
	private Map<EventType, Character> encodings;

	private static final char INITIAL = 'I';
	private static final char TERMINAL = 'T';
    
    private char getEncoding(EventType event) {
    	if (event.isInitialEventType()) {
    		return INITIAL;
    	} else if (event.isTerminalEventType()) {
    		return TERMINAL;
    	} else {
    		if (!encodings.containsKey(event)) {
    			char encoding = currentChar;
    			currentChar++;
    			encodings.put(event, encoding);
    		}
    		return encodings.get(event);
    	}
    }
}
