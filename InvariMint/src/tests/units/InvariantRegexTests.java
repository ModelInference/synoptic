package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.KTailInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.NeverImmediatelyFollowedInvariant;

/**
 * Uses the Automaton class to test if the getRegex methods for Synoptic's
 * temporal invariants are correct.
 * 
 */
public class InvariantRegexTests {
    @Test
    public void testNFbyRegex() {
        NeverFollowedInvariant nfby = new NeverFollowedInvariant("x", "y", "t");
        Automaton model = new RegExp(nfby.getRegex('x', 'y')).toAutomaton();
        model.minimize();

        assertTrue(model.run("ax"));
        assertTrue(model.run("y"));
        assertTrue(model.run("axabc"));
        assertTrue(model.run("ayyyyfsfsfx"));

        assertFalse(model.run("axabcy"));
        assertFalse(model.run("axabcyx"));

        model = new RegExp(nfby.getRegex('x', 'x')).toAutomaton();
        assertFalse(model.run("xyyx"));
        assertTrue(model.run("xfsafdsa"));
        assertFalse(model.run("yyyxx"));
    }

    @Test
    public void testAFbyRegex() {
        AlwaysFollowedInvariant afby = new AlwaysFollowedInvariant("x", "y",
                "t");
        Automaton model = new RegExp(afby.getRegex('x', 'y')).toAutomaton();
        model.minimize();

        assertTrue(model.run("axy"));
        assertTrue(model.run("axabcy"));
        assertTrue(model.run("axabcyabd"));

        assertFalse(model.run("axabc"));
        assertFalse(model.run("ayyyyfsfsfx"));
        assertFalse(model.run("axabcyx"));
    }

    @Test
    public void testAPRegex() {
        AlwaysPrecedesInvariant ap = new AlwaysPrecedesInvariant("x", "y", "t");
        Automaton model = new RegExp(ap.getRegex('x', 'y')).toAutomaton();
        model.minimize();

        assertTrue(model.run("axy"));
        assertTrue(model.run("axabcy"));
        assertTrue(model.run("axabc"));
        assertTrue(model.run("axabcyx"));
        assertTrue(model.run("axabcyabd"));
        assertTrue(model.run("xyyyyyyy"));

        assertFalse(model.run("ayyyyfsfsfx"));
        assertFalse(model.run("fsyxyx"));
    }

    @Test
    public void testNIFbyRegex() {
        NeverImmediatelyFollowedInvariant nifby = new NeverImmediatelyFollowedInvariant(
                "a", "b", "t");
        Automaton model = new RegExp(nifby.getRegex('a', 'b')).toAutomaton();
        model.minimize();

        assertTrue(model.run("ayb"));
        assertTrue(model.run("a"));
        assertTrue(model.run("b"));

        assertFalse(model.run("ab"));
        assertFalse(model.run("aab"));

        model = new RegExp(nifby.getRegex('a', 'a')).toAutomaton();
        assertTrue(model.run("aba"));
        assertTrue(model.run("a"));

        assertFalse(model.run("aa"));
        assertFalse(model.run("abaa"));

    }

    @Test
    public void testKTailRegex() {
        List<Character> tailEvents = new ArrayList<Character>();
        List<Character> followEvents = new ArrayList<Character>();

        tailEvents.add('a');
        tailEvents.add('b');
        tailEvents.add('c');

        followEvents.add('x');

        Automaton model = new RegExp(KTailInvariant.getRegex(tailEvents,
                followEvents)).toAutomaton();
        model.minimize();

        assertTrue(model.run("abcx"));
        assertFalse(model.run("abc"));
        assertFalse(model.run("aabc"));
        assertFalse(model.run("abcf"));
        assertFalse(model.run("aabcf"));
        assertFalse(model.run("ababcf"));
        assertFalse(model.run("abcxabcf"));

        followEvents.add('a');
        model = new RegExp(KTailInvariant.getRegex(tailEvents, followEvents))
                .toAutomaton();
        model.minimize();

        assertTrue(model.run("abca"));
        assertFalse(model.run("abcb"));

        followEvents.clear();
        followEvents.add('c');
        model = new RegExp(KTailInvariant.getRegex(tailEvents, followEvents))
                .toAutomaton();
        model.minimize();

        assertTrue(model.run("abcc"));
        assertFalse(model.run("abc"));

        tailEvents.clear();
        tailEvents.add('a');
        tailEvents.add('b');
        tailEvents.add('b');
        followEvents.clear();
        followEvents.add('x');
        model = new RegExp(KTailInvariant.getRegex(tailEvents, followEvents))
                .toAutomaton();
        model.minimize();
        assertTrue(model.run("abbx"));
        assertFalse(model.run("abbb"));
    }

    @Test
    public void testKTailOverlappingRegex() {
        List<Character> tailEvents = new ArrayList<Character>();
        tailEvents.add('b');
        tailEvents.add('a');
        tailEvents.add('b');

        List<Character> followSet = new ArrayList<Character>();
        followSet.add('a');
        String re = KTailInvariant.getRegex(tailEvents, followSet);

        Automaton a = new RegExp(re).toAutomaton();
        assertTrue(a.run("baba"));
        assertFalse(a.run("babc"));
        assertFalse(a.run("Ibabc"));
        assertFalse(a.run("Ibabcd"));
        assertFalse(a.run("IbabababcT"));
        assertFalse(a.run("IabababcT"));
        assertTrue(a.run("IababaT"));
        assertTrue(a.run("IabababaT"));
    }
}
