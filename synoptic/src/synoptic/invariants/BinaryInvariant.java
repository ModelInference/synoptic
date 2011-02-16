package synoptic.invariants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.trans.LTL2Buchi;
import gov.nasa.ltl.trans.ParseErrorException;

import synoptic.invariants.ltlchecker.LTLFormula;
import synoptic.model.interfaces.INode;
import synoptic.util.InternalSynopticException;

/**
 * Class for code that is shared by all binary temporal synoptic.invariants.
 * 
 * @author Sigurd Schneider
 */
public abstract class BinaryInvariant implements ITemporalInvariant {
    protected String first;
    protected String second;
    protected String relation;
    // CACHE:
    private Graph automaton;

    public BinaryInvariant(String typeFrist, String typeSecond, String relation) {
        first = typeFrist;
        second = typeSecond;
        this.relation = relation;
    }

    @Override
    public String toString() {
        return getLTLString();
    }

    @Override
    public String getRelation() {
        return relation;
    }

    /**
     * Removes loops from a trace path in 2n time.
     * 
     * @param <T>
     *            The type of node in the trace.
     * @param trace
     *            The trace from which to remove all loops.
     * @return A new trace that contains no loops.
     */
    public static <T extends INode<T>> List<T> removeLoops(List<T> trace) {
        LinkedList<T> traceWithoutLoops = new LinkedList<T>();
        LinkedHashMap<T, Integer> visitedAndNextHop = new LinkedHashMap<T, Integer>();
        // First iteration through trace -- keep track of what next node should
        // be added to the traceWithoutLoops in the visitedAndNextHop map.
        int i = 0;
        for (T node : trace) {
            visitedAndNextHop.put(node, i + 1);
            i = i + 1;
        }
        // Second iteration through trace -- add just the non-looped nodes to
        // the traceWithoutLoops.
        i = 0;
        int addAtVal = 0; // Always add the INITIAL node.
        // Could be made faster by iterating just through
        // visitedAndNextHop map starting from the trace[0] node.
        for (T node : trace) {
            if (addAtVal == i) {
                traceWithoutLoops.add(node);
                addAtVal = visitedAndNextHop.get(node);
            }
            i = i + 1;
        }
        return traceWithoutLoops;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = getClass().hashCode();
        result = prime * result + (first == null ? 0 : first.hashCode());
        result = prime * result + (relation == null ? 0 : relation.hashCode());
        result = prime * result + (second == null ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BinaryInvariant other = (BinaryInvariant) obj;
        if (first == null) {
            if (other.first != null) {
                return false;
            }
        } else if (!first.equals(other.first)) {
            return false;
        }
        if (relation == null) {
            if (other.relation != null) {
                return false;
            }
        } else if (!relation.equals(other.relation)) {
            return false;
        }
        if (second == null) {
            if (other.second != null) {
                return false;
            }
        } else if (!second.equals(other.second)) {
            return false;
        }
        return true;
    }

    @Override
    public gov.nasa.ltl.graph.Graph getAutomaton() {
        try {
            if (automaton == null) {
                String formula = getLTLString();
                if (useDIDCAN) {
                    formula = LTLFormula.prepare(getLTLString());
                }
                automaton = LTL2Buchi.translate("! (" + formula + ")");
            }
            return automaton;
        } catch (ParseErrorException e) {
            throw InternalSynopticException.Wrap(e);
        }
    }

    @Override
    public Set<String> getPredicates() {
        Set<String> set = new LinkedHashSet<String>();
        set.add(first);
        set.add(second);
        return set;
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }
}
