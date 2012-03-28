package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import synoptic.model.EventType;

/**
 * Encodes the EventTypes to be used by an EncodedAutomaton to characters. All
 * EventTypes to be used must be provided upon creation of an
 * EventTypeEncodings.
 * 
 * @author Jenny
 */
public class EventTypeEncodings {

    private Map<String, Character> eventEncodings;
    private Map<Character, String> charEncodings;
    private RegExp alphabet;
    private char cur;

    public EventTypeEncodings(Set<EventType> events) {
        /** Maps a string representation of an event to a char. */
        eventEncodings = new HashMap<String, Character>();
        /** The reverse eventEncodings map -- always maintained up to date. */
        charEncodings = new HashMap<Character, String>();

        cur = 1000;
        for (EventType e : events) {
            addEncoding(e, cur);
            cur++;
        }

        // Defines the alphabet available to Automata using this Encoding.
        StringBuilder chars = new StringBuilder();
        for (Character c : charEncodings.keySet()) {
            chars.append("|" + c);
        }
        chars.replace(0, 1, "("); // TODO: hacky fix to fence post issue.
        chars.append(")*");
        alphabet = new RegExp(chars.toString());
    }

    /**
     * Adds a new encoding of e to c. Maintains both the forward and the reverse
     * event to character maps.
     */
    private void addEncoding(EventType e, char c) {
        assert !eventEncodings.containsKey(e.toString());
        assert !charEncodings.containsKey(c);

        eventEncodings.put(e.toString(), c);
        charEncodings.put(c, e.toString());
    }

    /**
     * Returns a character encoding for the given EventType, assigning a new
     * character if this EventType has not yet been seen.
     */
    public char getEncoding(EventType e) {
        if (!eventEncodings.containsKey(e.toString())) {
            addEncoding(e, cur);
            char c = cur;
            cur++;
            return c;
        }
        return eventEncodings.get(e.toString());
    }

    public String getString(char c) {
        if (!charEncodings.containsKey(c)) {
            throw new IllegalArgumentException(
                    "The passed char has not been mapped to an event type.");
        }
        return charEncodings.get(c);
    }

    protected Automaton getInitialModel() {
        return alphabet.toAutomaton();
    }
}
