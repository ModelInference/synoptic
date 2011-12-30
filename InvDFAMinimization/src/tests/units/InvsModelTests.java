package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;

import org.junit.Before;
import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.model.EventType;

/**
 * Basic tests for the InvsModel class -- checks that invariants are used and
 * maintained correctly.
 * 
 * @author Jenny
 */
public class InvsModelTests {

    EventTypeEncodings encodings;

    @Before
    public void setUp() {
        encodings = EncodingTests.getBasicEncodings();
    }

    /**
     * Constructs an InvsModel with encodings and the given set of invariants.
     */
    public InvsModel generateModel(Set<ITemporalInvariant> invariants) {
        InvsModel model = new InvsModel(encodings);

        for (ITemporalInvariant inv : invariants) {
            model.intersectWith(new InvModel(inv, encodings));
        }
        return model;
    }

    @Test
    public void testConstructor() {
        InvsModel model = new InvsModel(encodings);
        assertTrue(model.getInvariants().size() == 0);
    }

    @Test
    public void testGetInvariants() {
        Set<ITemporalInvariant> invariants = new HashSet<ITemporalInvariant>();
        invariants.add(new AlwaysFollowedInvariant("a", "b", "t"));
        invariants.add(new AlwaysFollowedInvariant("b", "c", "t"));
        invariants.add(new AlwaysFollowedInvariant("c", "d", "t"));
        InvsModel model = generateModel(invariants);
        assertTrue(invariants.equals(model.getInvariants()));
    }

    @Test
    public void testIntersection() {
        Set<ITemporalInvariant> invariants = new HashSet<ITemporalInvariant>();
        invariants.add(new AlwaysFollowedInvariant("a", "b", "t"));
        invariants.add(new NeverFollowedInvariant("c", "d", "t"));
        InvsModel model = generateModel(invariants);
        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(EncodingTests.aEvent);
        sequence.add(EncodingTests.dEvent);
        sequence.add(EncodingTests.cEvent);
        assertFalse(model.run(sequence));
        sequence.add(EncodingTests.bEvent);
        assertTrue(model.run(sequence));
    }

    @Test
    public void testHypothesis() {
        Set<ITemporalInvariant> invariants = new HashSet<ITemporalInvariant>();
        invariants.add(new AlwaysFollowedInvariant("a", "b", "t"));
        invariants.add(new AlwaysFollowedInvariant("a", "c", "t"));
        invariants.add(new AlwaysFollowedInvariant("b", "c", "t"));
        invariants.add(new AlwaysFollowedInvariant("d", "b", "t"));
        invariants.add(new AlwaysFollowedInvariant("d", "c", "t"));

        invariants.add(new NeverFollowedInvariant("a", "a", "t"));
        invariants.add(new NeverFollowedInvariant("b", "a", "t"));
        invariants.add(new NeverFollowedInvariant("b", "b", "t"));
        invariants.add(new NeverFollowedInvariant("b", "d", "t"));
        invariants.add(new NeverFollowedInvariant("d", "a", "t"));
        invariants.add(new NeverFollowedInvariant("d", "d", "t"));

        invariants.add(new AlwaysPrecedesInvariant("a", "b", "t"));
        invariants.add(new AlwaysPrecedesInvariant("a", "d", "t"));

        invariants.add(new AlwaysPrecedesInvariant("c", "a", "t"));
        invariants.add(new AlwaysPrecedesInvariant("c", "b", "t"));
        invariants.add(new AlwaysPrecedesInvariant("c", "d", "t"));

        invariants.add(new NeverImmediatelyFollowedInvariant("a", "a", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("a", "b", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("a", "c", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("b", "a", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("b", "b", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("b", "d", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("c", "b", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("c", "c", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("c", "d", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("d", "a", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("d", "c", "t"));
        invariants.add(new NeverImmediatelyFollowedInvariant("d", "d", "t"));

        InvsModel model = generateModel(invariants);
        try {
            model.exportDotAndPng("hypothesis.png");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
