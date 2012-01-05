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
        eventEncodings = new HashMap<String, Character>();
        charEncodings = new HashMap<Character, String>();

        cur = 1000;
        for (EventType e : events) {
            eventEncodings.put(e.toString(), cur);
            charEncodings.put(cur, e.toString());
            cur++;
        }

        // Defines the alphabet available to Automata using this Encoding.
        StringBuilder chars = new StringBuilder();
        for (Character c : charEncodings.keySet()) {
            chars.append("|" + c);
        }
        chars.replace(0, 1, "("); // hacky fix to fence post issue.
        chars.append(")*");
        alphabet = new RegExp(chars.toString());
    }

    /**
     * Returns a character encoding for the given EventType, assigning a new
     * character if this EventType has not yet been seen.
     */
    public char getEncoding(EventType e) {
        if (!eventEncodings.containsKey(e.toString())) {
            char c = cur;
            cur++;
            return c;
        }
        return eventEncodings.get(e.toString());
    }

    public String getString(char c) {
        return charEncodings.get(c);
    }

    protected Automaton getInitialModel() {
        return alphabet.toAutomaton();
    }
}
