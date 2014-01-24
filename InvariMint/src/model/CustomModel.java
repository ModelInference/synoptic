package model;

import dk.brics.automaton.State;

/**
 * CustomModel constructs an EncodedAutomaton with the given encodings and
 * initial State. Useful for constructing EncodedAutomaton by hand.
 * 
 */
public class CustomModel extends EncodedAutomaton {

    public CustomModel(EventTypeEncodings encodings, State initial) {
        super(encodings);
        super.setInitialState(initial);
    }
}
