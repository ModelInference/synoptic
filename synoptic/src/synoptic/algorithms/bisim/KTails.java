package synoptic.algorithms.bisim;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

public class KTails {
    /**
     * We perform k-tails comparison on the message graph to the given depth k.
     * At k=0 we are just comparing the labels of the two nodes, k > 0 compares
     * the subgraphs that extend down k levels. <br />
     * <br />
     * TODO: differentiate between weak subsumption and strong subsumption? As
     * is done here: http://portal.acm.org/ft_gateway.cfm?id=1368157&type=pdf
     * 
     * @return true if the k-tails are equivalent under the current definition
     *         of equivalence.
     */

    static public <NodeType extends INode<NodeType>> boolean kEquals(
            INode<NodeType> n1, INode<NodeType> n2, int k, boolean subsumption) {
        if (k == 0) {
            return n1.getLabel().equals(n2.getLabel());
        }
        if (subsumption) {
            return kEqualsWithSubsumption(n1, n2, k);
        }
        return kEqualsWithoutSubsumption(n1, n2, k);
    }

    /**
     * With subsumption its okay for some transitions of n1 to be unmatched with
     * any transitions in n2, but all of n2 transitions must be matched to some
     * transition in n1.
     */
    static private <NodeType extends INode<NodeType>> boolean kEqualsWithSubsumption(
            INode<NodeType> n1, INode<NodeType> n2, int k) {

        // The labels must match.
        if (!n1.getLabel().equals(n2.getLabel())) {
            return false;
        }

        // Base case.
        if (k == 0) {
            return true;
        }

        // Short circuit: even with subsumption all of n2 transitions must map
        // to exactly one n1 transition. Therefore number of n2 transition must
        // be less than number of n1 transitions.
        if (n1.getTransitions().size() < n2.getTransitions().size()) {
            return false;
        }

        // Here we will match up transition destinations between n1 and n2 based
        // on whether or not they are kEqual with k=k-1. Because of subsumption
        // we have to keep track of all possible matches, since we don't know
        // a-priori which ones to match to exactly (another node might need to
        // match to the one we've matched to previously).
        LinkedHashMap<NodeType, NodeType> childKEquivMatches = new LinkedHashMap<NodeType, NodeType>();

        throw new InternalSynopticException(
                "kTails with Subsumption unimplemented.");
    }

    /**
     * For subsumption == false both sets must match each other exactly -- there
     * needs to be a 1-1 correspondence.
     */
    static private <NodeType extends INode<NodeType>> boolean kEqualsWithoutSubsumption(
            INode<NodeType> n1, INode<NodeType> n2, int k) {

        // The labels must match.
        if (!n1.getLabel().equals(n2.getLabel())) {
            return false;
        }

        // Base case.
        if (k == 0) {
            return true;
        }

        // Short circuit: since we are not subsuming, the number of transitions
        // from the two nodes must be the same.
        // NOTE: this comparison considers all relations simultaneously. For
        // efficiency we could also check for matching transition counts for
        // each relation.
        if (n1.getTransitions().size() != n2.getTransitions().size()) {
            return false;
        }

        // Here we will match up children of n1 with children of n2 based
        // on whether or not they are kEqual with k=k-1. We keep track of
        // n2 children that we've already matched some children of n1. We skip
        // these matched children of n2 since we can't re-use matches for
        // children of n1.

        // This set contains a child of n2 if that child has been
        // mapped in some earlier iteration of the outer loop.
        LinkedHashSet<NodeType> childKEquivMatches = new LinkedHashSet<NodeType>();

        Iterator<? extends ITransition<NodeType>> i1, i2;
        i1 = n1.getTransitionsIterator();
        while (i1.hasNext()) {
            ITransition<NodeType> t1 = i1.next();
            NodeType c1 = t1.getTarget();

            boolean kEqual = false;
            // Make sure to get transitions of the same relation.
            i2 = n2.getTransitionsIterator(t1.getRelation());
            while (i2.hasNext()) {
                NodeType c2 = i2.next().getTarget();
                // Skip c2 if its already been mapped to a c1 previously
                if (childKEquivMatches.contains(c2)) {
                    continue;
                }

                if (kEqualsWithoutSubsumption(c1, c2, k - 1)) {
                    kEqual = true;
                    childKEquivMatches.add(c2);
                    break;
                }
            }
            // Could not find any kEqual c2 to match with c1.
            if (!kEqual) {
                return false;
            }
        }
        // We are k-equivalent at this point because:
        // 0. Labels of n1 and n2 match
        // 1. The child sets have the same size
        // 2. Each child of n1 matches to exactly one child of n2
        // 3. Each pair of matched children are k-1 equivalent.
        return true;
    }
}
