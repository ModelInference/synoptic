package dynoptic.model.automaton;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import synoptic.model.event.EventType;

/**
 * Encodes the EventType to be used by an EncodedAutomaton to characters. All
 * EventTypes to be used must be provided upon creation of an
 * EventTypeEncodings.
 */
public class EventTypeEncodings <T extends EventType> {

    private Map<T, Character> eventEncodings;
    private Map<Character, T> charEncodings;
    private RegExp alphabet;
    private char cur;

    public EventTypeEncodings(Set<T> events) {
        /** Maps an event type to a char. */
        eventEncodings = new HashMap<T, Character>();

        /** The reverse eventEncodings map -- always maintained up to date. */
        charEncodings = new HashMap<Character, T>();

        cur = 1000;
        for (T e : events) {
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
    private void addEncoding(T e, char c) {
        assert !eventEncodings.containsKey(e);
        assert !charEncodings.containsKey(c);

        eventEncodings.put(e, c);
        charEncodings.put(c, e);
    }

    /**
     * Returns a character encoding for the given EventType, assigning a new
     * character if this EventType has not yet been seen.
     */
    public char getEncoding(T e) {
        if (!eventEncodings.containsKey(e)) {
            addEncoding(e, cur);
            char c = cur;
            cur++;
            return c;
        }
        return eventEncodings.get(e);
    }

    public T getEventType(char c) {
        if (!charEncodings.containsKey(c)) {
            throw new IllegalArgumentException(
                    "The passed char has not been mapped to an event type.");
        }
        return charEncodings.get(c);
    }

    /**
     * Returns an Automaton that accepts Strings containing only the EventTypes
     * with known encodings
     */
    protected Automaton getInitialModel() {
        return alphabet.toAutomaton();
    }
    
    @Override
    public int hashCode() {
        return eventEncodings.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EventTypeEncodings)) {
            return false;
        }
        EventTypeEncodings<T> encodings = (EventTypeEncodings<T>) other;
        return eventEncodings.equals(encodings.eventEncodings);
    }
}
