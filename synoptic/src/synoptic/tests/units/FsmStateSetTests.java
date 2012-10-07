package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.birelational.AFBiRelationInvariant;
import synoptic.invariants.birelational.APBiRelationInvariant;
import synoptic.invariants.birelational.NFBiRelationInvariant;
import synoptic.invariants.fsmcheck.AFbyInvFsms;
import synoptic.invariants.fsmcheck.APInvFsms;
import synoptic.invariants.fsmcheck.FsmStateSet;
import synoptic.invariants.fsmcheck.NFbyInvFsms;
import synoptic.invariants.fsmcheck.birelational.AFBiRelationStateSet;
import synoptic.invariants.fsmcheck.birelational.APBiRelationStateSet;
import synoptic.invariants.fsmcheck.birelational.FsmBiRelationalStateSet;
import synoptic.invariants.fsmcheck.birelational.NFBiRelationStateSet;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.tests.SynopticTest;

public class FsmStateSetTests extends SynopticTest {
    public static String nonTimeRelation = "r";
    public static Set<String> nonTimeSet;
    public static Set<String> emptySet = Collections.emptySet();
    
    static {
        nonTimeSet = new HashSet<String>();
        nonTimeSet.add(nonTimeRelation);
    }
    
    public static EventNode msgA = new EventNode(new Event("a"));
    public static EventNode msgB = new EventNode(new Event("b"));
    public static EventNode msgZ = new EventNode(new Event("z"));

    /**
     * Converts a string representation of a bit set to a BitSet instance.
     */
    private static BitSet parseBitSet(String str) {
        BitSet result = new BitSet();
        boolean b;
        for (int i = 0; i < str.length(); i++) {
            b = (str.charAt(i) == '1');
            result.set(i, b);
        }
        return result;
    }

    /**
     * Makes sure that we implemented parseBitSet correctly.
     */
    @Test
    public void helpersTest() {
        BitSet b1 = parseBitSet("0000");
        BitSet b2 = new BitSet();
        assertTrue(b1.equals(b2));

        b1 = parseBitSet("1010");
        b2 = new BitSet();
        b2.set(0);
        b2.set(2);
        assertTrue(b1.equals(b2));
    }

    /**
     * Tests equality between two FsmStateSets.
     */
    @Test
    public void equalityTest() {
        List<BinaryInvariant> invs1, invs2;
        FsmStateSet<EventNode> f1, f2;
        BinaryInvariant inv1, inv2;

        // AFby with AFby.
        inv1 = new AlwaysFollowedInvariant("a", "b",
                Event.defTimeRelationStr);
        inv2 = new AlwaysFollowedInvariant("a", "c",
                Event.defTimeRelationStr);
        invs1 = new LinkedList<BinaryInvariant>();
        invs1.add(inv1);

        f1 = new AFbyInvFsms<EventNode>(invs1);
        f2 = new AFbyInvFsms<EventNode>(invs1);
        assertTrue(f1.equals(f2));
        assertTrue(!f1.equals(null));
        assertTrue(!f1.equals("some non-FsmStateSet type"));

        invs2 = new LinkedList<BinaryInvariant>();
        invs2.add(inv2);
        f2 = new AFbyInvFsms<EventNode>(invs2);
        assertTrue(!f1.equals(f2)); // differ in invariantsMap

        invs2.add(inv1);
        assertTrue(!f1.equals(f2)); // differ in count

        // AFby with NFby.
        inv1 = new AlwaysFollowedInvariant("a", "b",
                Event.defTimeRelationStr);
        inv2 = new NeverFollowedInvariant("a", "b",
                Event.defTimeRelationStr);
        invs1 = new LinkedList<BinaryInvariant>();
        invs1.add(inv1);
        invs2 = new LinkedList<BinaryInvariant>();
        invs2.add(inv2);

        f1 = new AFbyInvFsms<EventNode>(invs1);
        f2 = new NFbyInvFsms<EventNode>(invs2);
        assertTrue(!f1.equals(f2)); // differ in getClass() values
    }

    /**
     * Helper interface for testing different invariant types.
     */
    private interface iInvSpecificGenerator {
        BinaryInvariant genInv(EventType a, EventType b, String relation);

        FsmStateSet<EventNode> genFsmStateSet(List<BinaryInvariant> invs);
    }

    private static FsmStateSet<EventNode> initStateSet(String input,
            iInvSpecificGenerator generator, String relation) {
        String[] inputs = input.split(" ");
        assertTrue(inputs.length == 2);

        List<BinaryInvariant> invs = new LinkedList<BinaryInvariant>();
        BinaryInvariant inv;
        EventType s1, s2;
        for (int i = 0; i < inputs[0].length(); i++) {
            if (inputs[0].charAt(i) == '1') {
                s1 = msgA.getEType();
            } else {
                s1 = new StringEventType("x");
            }
            if (inputs[1].charAt(i) == '1') {
                s2 = msgB.getEType();
            } else {
                s2 = new StringEventType("y");
            }

            inv = generator.genInv(s1, s2, relation);
            invs.add(inv);
        }
        return generator.genFsmStateSet(invs);
    }

    @Test
    public void AFbyInvFsmsTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new AlwaysFollowedInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                return new AFbyInvFsms<EventNode>(invs);
            }
        };

        FsmStateSet<EventNode> f1, f2;

        // ////////
        // Simulate a single "a AFby b" invariant.

        // z (initial) != a (initial)
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // z is not fail
        assertTrue(!f1.isFail());
        // a is fail
        assertTrue(f2.isFail());

        // z (initial) == b (initial)
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgB);
        assertTrue(f1.equals(f2));

        // z->a (initial z and then transition by a) == a (initial
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // z->z == z
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgZ);
        assertTrue(f1.equals(f2));

        // a->a == a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->b != a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // a->b is not fail
        assertTrue(!f1.isFail());

        // a->b->a == a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->b->a->b == a->b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgA);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // TODO: test multiple simultaneous AFby machines
    }

    @Test
    public void NFbyInvFsmsTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new NeverFollowedInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                return new NFbyInvFsms<EventNode>(invs);
            }
        };

        FsmStateSet<EventNode> f1, f2;

        // ////////
        // Simulate a single "a NFby b" invariant.

        // z != a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // z is not fail
        assertTrue(!f1.isFail());
        // a is not fail
        assertTrue(!f2.isFail());

        // z == b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgB);
        assertTrue(f1.equals(f2));

        // z->a == a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // z->b == z
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgB);
        assertTrue(f1.equals(f2));

        // z->z == z
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgZ);
        assertTrue(f1.equals(f2));

        // a->a == a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->b != a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // a->b is fail
        assertTrue(f1.isFail());

        // a->b->a == a->b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // a->b->b == a->b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // a->b->z == a->b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // TODO: test multiple simultaneous NFby machines
    }

    @Test
    public void APInvFsmsTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new AlwaysPrecedesInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                return new APInvFsms<EventNode>(invs);
            }
        };

        FsmStateSet<EventNode> f1, f2;

        // ////////
        // Simulate a single "a AP b" invariant.

        // z != a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // z is not fail
        assertTrue(!f1.isFail());
        // a is not fail
        assertTrue(!f2.isFail());

        // z != b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgB);
        assertTrue(!f1.equals(f2));
        // b is fail
        assertTrue(f2.isFail());

        // a != b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgB);
        assertTrue(!f1.equals(f2));

        // z->b == b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgB);
        assertTrue(f1.equals(f2));

        // z->a == a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // z->z == z
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgZ);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgZ);
        assertTrue(f1.equals(f2));

        // a->a == a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->b == a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->z == a
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.setInitial(msgA);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // b->a == b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.transition(msgB);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // b->b == b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.transition(msgB);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // b->z == b
        f1 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f1.transition(msgB);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, Event.defTimeRelationStr);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // TODO: test multiple simultaneous AP machines
    }
    
    @Test
    public void AFbyInvFsmsMultiRelationalSingleTransitionTypeTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new AFBiRelationInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                FsmStateSet<EventNode> fsm = new AFbyInvFsms<EventNode>(invs);
                return fsm;
            }
        };

        FsmStateSet<EventNode> f1, f2;

        // ////////
        // Simulate a single "a AFby b" invariant.

        // z (initial) != a (initial)
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // z is not fail
        assertTrue(!f1.isFail());
        // a is fail
        assertTrue(f2.isFail());

        // z (initial) == b (initial)
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgB);
        assertTrue(f1.equals(f2));

        // z->a (initial z and then transition by a) == a (initial
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // z->z == z
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgZ);
        assertTrue(f1.equals(f2));

        // a->a == a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->b != a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // a->b is not fail
        assertTrue(!f1.isFail());

        // a->b->a == a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->b->a->b == a->b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgA);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // TODO: test multiple simultaneous AFby machines
    }

    @Test
    public void NFbyInvFsmsMultiRelationalSingleTransitionTypeTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new NFBiRelationInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                FsmStateSet<EventNode> fsm = new NFbyInvFsms<EventNode>(invs);
                return fsm;
            }
        };

        FsmStateSet<EventNode> f1, f2;

        // ////////
        // Simulate a single "a NFby b" invariant.

        // z != a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // z is not fail
        assertTrue(!f1.isFail());
        // a is not fail
        assertTrue(!f2.isFail());

        // z == b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgB);
        assertTrue(f1.equals(f2));

        // z->a == a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // z->b == z
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgB);
        assertTrue(f1.equals(f2));

        // z->z == z
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgZ);
        assertTrue(f1.equals(f2));

        // a->a == a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->b != a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // a->b is fail
        assertTrue(f1.isFail());

        // a->b->a == a->b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // a->b->b == a->b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // a->b->z == a->b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // TODO: test multiple simultaneous NFby machines
    }

    @Test
    public void APInvFsmsMultiRelationalSingleTransitionTypeTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new APBiRelationInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                FsmStateSet<EventNode> fsm = new APInvFsms<EventNode>(invs);
                return fsm;
            }
        };

        FsmStateSet<EventNode> f1, f2;

        // ////////
        // Simulate a single "a AP b" invariant.

        // z != a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(!f1.equals(f2));
        // z is not fail
        assertTrue(!f1.isFail());
        // a is not fail
        assertTrue(!f2.isFail());

        // z != b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgB);
        assertTrue(!f1.equals(f2));
        // b is fail
        assertTrue(f2.isFail());

        // a != b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgB);
        assertTrue(!f1.equals(f2));

        // z->b == b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgB);
        assertTrue(f1.equals(f2));

        // z->a == a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // z->z == z
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgZ);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgZ);
        assertTrue(f1.equals(f2));

        // a->a == a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->b == a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // a->z == a
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.setInitial(msgA);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.setInitial(msgA);
        assertTrue(f1.equals(f2));

        // b->a == b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.transition(msgB);
        f1.transition(msgA);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // b->b == b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.transition(msgB);
        f1.transition(msgB);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // b->z == b
        f1 = initStateSet("1 1", invGen, nonTimeRelation);
        f1.transition(msgB);
        f1.transition(msgZ);
        f2 = initStateSet("1 1", invGen, nonTimeRelation);
        f2.transition(msgB);
        assertTrue(f1.equals(f2));

        // TODO: test multiple simultaneous AP machines
    }

    @Test
    public void AFbyInvFsmsMultiRelationalDualTransitionTypeTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new AFBiRelationInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                FsmBiRelationalStateSet<EventNode> fsm = new AFBiRelationStateSet<EventNode>(invs);
                fsm.addRelation(nonTimeRelation);
                fsm.addClosureRelation(Event.defTimeRelationStr);
                return fsm;
            }
        };

        FsmStateSet<EventNode> f1 = initStateSet("1 1", invGen, nonTimeRelation);
        
        // success : (a) -t-> (z) -r-> (z) 
        f1.setInitial(msgA, Event.defTimeRelationSet);
        f1.transition(msgZ, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgZ, nonTimeRelation, emptySet);
        assertTrue(!f1.isFail());
        
        // success : (a) -r-> (a) -t-> (z) -t-> (b) -r-> (b) 
        f1.setInitial(msgA, nonTimeSet);
        f1.transition(msgA, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgZ, Event.defTimeRelationStr, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgB, nonTimeRelation, emptySet);
        assertTrue(!f1.isFail());
        
        // fail : (a) -r-> (a) -t-> (b) -t-> (z) -r-> (z) 
        f1.setInitial(msgA, nonTimeSet);
        f1.transition(msgA, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, Event.defTimeRelationSet);
        f1.transition(msgZ, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgZ, nonTimeRelation, emptySet);
        assertTrue(f1.isFail());
        
        // fail : (a) -r-> (a) -t-> (b)
        f1.setInitial(msgA, nonTimeSet);
        f1.transition(msgA, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, emptySet);
        assertTrue(f1.isFail());
    }
    
    @Test
    public void NFbyInvFsmsMultiRelationalDualTransitionTypeTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new NFBiRelationInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                FsmBiRelationalStateSet<EventNode> fsm = new NFBiRelationStateSet<EventNode>(invs);
                fsm.addRelation(nonTimeRelation);
                fsm.addClosureRelation(Event.defTimeRelationStr);
                return fsm;
            }
        };

        FsmStateSet<EventNode> f1 = initStateSet("1 1", invGen, nonTimeRelation);
        
        // success : (a) -t-> (b) -r-> (b) 
        f1.setInitial(msgA, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgB, nonTimeRelation, emptySet);
        assertTrue(!f1.isFail());
        
        // fail : (a) -r-> (a) -t-> (z) -t-> (b) -r-> (b) 
        f1.setInitial(msgA, nonTimeSet);
        f1.transition(msgA, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgZ, Event.defTimeRelationStr, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgB, nonTimeRelation, emptySet);
        assertTrue(f1.isFail());
        
        // success : (a) -r-> (a) -t-> (b) -t-> (z) -r-> (z) 
        f1.setInitial(msgA, nonTimeSet);
        assertTrue(!f1.isFail()); // regression test
        f1.transition(msgA, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, Event.defTimeRelationSet);
        f1.transition(msgZ, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgZ, nonTimeRelation, emptySet);
        assertTrue(!f1.isFail());
        
        // success : (a) -r-> (a) -t-> (b)
        f1.setInitial(msgA, nonTimeSet);
        f1.transition(msgA, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, emptySet);
        assertTrue(!f1.isFail());
    }
    
    @Test
    public void APInvFsmsMultiRelationalDualTransitionTypeTest() {
        iInvSpecificGenerator invGen = new iInvSpecificGenerator() {
            @Override
            public BinaryInvariant genInv(EventType a, EventType b,
                    String relation) {
                return new APBiRelationInvariant(a, b, relation);
            }

            @Override
            public FsmStateSet<EventNode> genFsmStateSet(
                    List<BinaryInvariant> invs) {
                FsmBiRelationalStateSet<EventNode> fsm = new APBiRelationStateSet<EventNode>(invs);
                fsm.addRelation(nonTimeRelation);
                fsm.addClosureRelation(Event.defTimeRelationStr);
                return fsm;
            }
        };

        FsmStateSet<EventNode> f1 = initStateSet("1 1", invGen, nonTimeRelation);
        
        // fail : (a) -t-> (b) -r-> (b)
        f1.setInitial(msgA, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgB, nonTimeRelation, emptySet);
        assertTrue(f1.isFail());
        
        // fail : (z) -r-> (z) -t-> (a) -t-> (b) -r-> (b)
        f1.setInitial(msgZ, nonTimeSet);
        f1.transition(msgZ, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgA, Event.defTimeRelationStr, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgB, nonTimeRelation, emptySet);
        assertTrue(f1.isFail());
        
        // success : (a) -r-> (a) -t-> (z) -t-> (b) -r-> (b)
        f1.setInitial(msgA, nonTimeSet);
        f1.transition(msgA, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgZ, Event.defTimeRelationStr, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, nonTimeSet);
        f1.transition(msgB, nonTimeRelation, emptySet);
        assertTrue(!f1.isFail());
        
        // success : (z) -r-> (z) -t-> (b)
        f1.setInitial(msgZ, nonTimeSet);
        f1.transition(msgZ, nonTimeRelation, Event.defTimeRelationSet);
        f1.transition(msgB, Event.defTimeRelationStr, emptySet);
        assertTrue(!f1.isFail());
    }
}
