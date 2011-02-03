package synoptic.algorithms.bisim;

import java.util.Iterator;

import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

public class KTails {
    /**
     * We perform k-tails comparison on the message graph to the given depth k.
     * TODO: differentiate between weak subsumption and strong subsumption? As
     * is done here: http://portal.acm.org/ft_gateway.cfm?id=1368157&type=pdf
     * 
     * @return true if the k-tails are equivalent under the current definition
     *         of equivalence.
     */
    static public <NodeType extends INode<NodeType>> boolean kEquals(
            INode<NodeType> s1, INode<NodeType> s2, int k, boolean subsumption) {

        if (!s1.getLabel().equals(s2.getLabel())) {
            return false;
        }

        // Base case.
        if (k == 0) {
            return true;
        }

        for (Iterator<? extends ITransition<NodeType>> i1 = s1
                .getTransitionsIterator(); i1.hasNext();) {
            ITransition<NodeType> t1 = i1.next();
            boolean notExistent = true;
            for (Iterator<? extends ITransition<NodeType>> i2 = s2
                    .getTransitionsIterator(t1.getRelation()); i2.hasNext();) {
                ITransition<NodeType> t2 = i2.next();
                if (!kEquals(t1.getTarget(), t2.getTarget(), k - 1, subsumption)) {
                    return false;
                }
                notExistent = false;
            }
            if (notExistent) {
                checkNotThere(s2, t1.getRelation());
                return false;
            }
        }

        // If it is not subsumption, do it the other way around as well.
        if (!subsumption) {
            for (Iterator<? extends ITransition<NodeType>> i2 = s2
                    .getTransitionsIterator(); i2.hasNext();) {
                ITransition<NodeType> t2 = i2.next();
                boolean notExistent = true;
                for (Iterator<? extends ITransition<NodeType>> i1 = s1
                        .getTransitionsIterator(t2.getRelation()); i1.hasNext();) {
                    ITransition<NodeType> t1 = i1.next();
                    if (!kEquals(t1.getTarget(), t2.getTarget(), k - 1,
                            subsumption)) {
                        return false;
                    }
                    notExistent = false;
                }
                if (notExistent) {
                    // checkNotThere(s2, t2.getAction());
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check that {@code state} has no outgoing transition labeled with {@code
     * relation}.
     * 
     * @param <NodeType>
     * @param state
     *            the state to check
     * @param relation
     *            the name of the relation
     */
    private static <NodeType extends INode<NodeType>> void checkNotThere(
            INode<NodeType> state, String relation) {
        for (Iterator<? extends ITransition<NodeType>> i1 = state
                .getTransitionsIterator(); i1.hasNext();) {
            if (i1.next().getRelation().equals(relation)) {
                throw new InternalSynopticException("inconsistent");
            }
        }
    }

}
