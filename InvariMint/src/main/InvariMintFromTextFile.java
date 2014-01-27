package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.KTailInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

/**
 * Generates an EncodedAutomaton by intersecting all invariants in a given text
 * file. Invariants are expected to be in the following form, each on a separate
 * line:
 * 
 */
/*
 * IT // Constructs Initial-Terminal invariant
 * 
 * NIFby a b // Constructs a NIFby b invariant
 * 
 * AFby a b // Constructs a AFby b invariant
 * 
 * NFby a b // Constructs a NFby b invariant
 * 
 * AP a b // Construct a AP b invariant
 * 
 * KTail x y a_1 ... a_x b_1 ... b_n // Constructs <a_1 ... a_x> {b_1 ... b_n}
 * ktail invariant
 */
public class InvariMintFromTextFile {

    public static Logger logger = null;

    public static void main(String[] args) {
        logger = Logger.getLogger("InvariMintTextFile");
        if (args.length != 1) {
            logger.severe("Usage: must pass a single text file containing invariants");
            System.exit(0);
        }

        EncodedAutomaton dfa = createDFA(args[0]);
        if (dfa != null) {
            try {
                dfa.exportDotAndPng("InvariantsFromText.dot");
            } catch (IOException e) {
                logger.severe("Error exporting final model");
            }
        }
    }

    public static EncodedAutomaton createDFA(String filename) {
        try {
            Scanner input = new Scanner(new File(filename));
            TemporalInvariantSet invariants = new TemporalInvariantSet();
            Set<EventType> events = new HashSet<EventType>();
            while (input.hasNextLine()) {
                String[] invariant = input.nextLine().split(" ");
                if (invariant[0].equals("KTail")) {

                    int tailLength = Integer.parseInt(invariant[1]);
                    List<EventType> tail = new ArrayList<EventType>();
                    for (int i = 0; i < tailLength; i++) {
                        tail.add(getEventType(invariant[3 + i], events));
                    }

                    int followSize = Integer.parseInt(invariant[2]);
                    Set<EventType> follow = new HashSet<EventType>();
                    for (int i = 0; i < followSize; i++) {
                        follow.add(getEventType(invariant[3 + tailLength + i],
                                events));
                    }
                    invariants.add(new KTailInvariant(tail, follow,
                            Event.defTimeRelationStr));
                } else if (invariant[0].equals("IT")) {
                    invariants.add(new TOInitialTerminalInvariant(
                            StringEventType.newInitialStringEventType(),
                            StringEventType.newTerminalStringEventType(),
                            Event.defTimeRelationStr));
                } else {
                    EventType first = getEventType(invariant[1], events);
                    EventType second = getEventType(invariant[2], events);
                    if (invariant[0].equals("NIFby")) {
                        invariants.add(new NeverImmediatelyFollowedInvariant(
                                first, second, Event.defTimeRelationStr));
                    } else if (invariant[0].equals("AFby")) {
                        invariants.add(new AlwaysFollowedInvariant(first,
                                second, Event.defTimeRelationStr));
                    } else if (invariant[0].equals("NFby")) {
                        invariants.add(new NeverFollowedInvariant(first,
                                second, Event.defTimeRelationStr));
                    } else if (invariant[0].equals("AP")) {
                        invariants.add(new AlwaysPrecedesInvariant(first,
                                second, Event.defTimeRelationStr));
                    } else {
                        logger.severe("Skipping unknown invariant type: "
                                + invariant[0]);
                    }
                }
            }
            EventTypeEncodings encodings = new EventTypeEncodings(events);
            EncodedAutomaton dfa = new InvsModel(encodings);
            for (ITemporalInvariant invariant : invariants) {
                InvModel invDFA = new InvModel(invariant, encodings);
                dfa.intersectWith(invDFA);
                // dfa.minimize();
            }
            return dfa;
        } catch (FileNotFoundException e) {
            logger.severe("Error reading invariant text file");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            logger.severe("Misformed text file");
            e.printStackTrace();
        }
        return null;
    }

    private static EventType getEventType(String inputString,
            Set<EventType> events) {
        EventType event;
        if (inputString.equals("INITIAL")) {
            event = StringEventType.newInitialStringEventType();
        } else if (inputString.equals("TERMINAL")) {
            event = StringEventType.newTerminalStringEventType();
        } else {
            event = new StringEventType(inputString);
        }
        events.add(event);
        return event;
    }
}
