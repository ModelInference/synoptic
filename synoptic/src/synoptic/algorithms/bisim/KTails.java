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
            NodeType n1, NodeType n2, int k, boolean subsumption) {
        if (k == 0) {
            return n1.getLabel().equals(n2.getLabel());
        }
        if (subsumption) {
            return kEqualsWithSubsumption(n1, n2, k);
        }
        LinkedHashMap<NodeType, NodeType> allVisitedMatches = new LinkedHashMap<NodeType, NodeType>();
        return kEqualsWithoutSubsumption(n1, n2, k, allVisitedMatches);
    }

    /**
     * With subsumption its okay for some transitions of n1 to be unmatched with
     * any transitions in n2, but all of n2 transitions must be matched to some
     * transition in n1.
     */
    static private <NodeType extends INode<NodeType>> boolean kEqualsWithSubsumption(
            NodeType n1, NodeType n2, int k) {

        throw new InternalSynopticException(
                "kTails with Subsumption unimplemented.");

        // // The labels must match.
        // if (!n1.getLabel().equals(n2.getLabel())) {
        // return false;
        // }
        //
        // // Base case.
        // if (k == 0) {
        // return true;
        // }
        //
        // // Short circuit: even with subsumption all of n2 transitions must
        // map
        // // to exactly one n1 transition. Therefore number of n2 transition
        // must
        // // be less than number of n1 transitions.
        // if (n1.getTransitions().size() < n2.getTransitions().size()) {
        // return false;
        // }
        //
        // // Here we will match up transition destinations between n1 and n2
        // based
        // // on whether or not they are kEqual with k=k-1. Because of
        // subsumption
        // // we have to keep track of all possible matches, since we don't know
        // // a-priori which ones to match to exactly (another node might need
        // to
        // // match to the one we've matched to previously).
        // LinkedHashMap<NodeType, NodeType> childKEquivMatches = new
        // LinkedHashMap<NodeType, NodeType>();

    }

    /**
     * Without subsumption the children sets of both nodes must match each other
     * exactly -- there needs to be a 1-1 correspondence that holds recursively.
     */
    static private <NodeType extends INode<NodeType>> boolean kEqualsWithoutSubsumption(
            NodeType n1, NodeType n2, int k,
            LinkedHashMap<NodeType, NodeType> allVisitedMatches) {

        // The labels must match.
        if (!n1.getLabel().equals(n2.getLabel())) {
            return false;
        }

        if (allVisitedMatches.containsKey(n1)) {
            if (allVisitedMatches.get(n1) != n2) {
                // n1 has been visited previously, but it doesn't map to n2
                return false;
            }
        } else {
            if (allVisitedMatches.containsValue(n2)) {
                // n1 has not been visited previously, but n2 has been.
                return false;
            }
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

        // If any of the children of n1 have been previously visited, then check
        // that each visited child of n1 corresponds to some visited child of
        // n2. And that no visited child of n2 is visited otherwise.
        Iterator<? extends ITransition<NodeType>> i1, i2;
        LinkedHashSet<NodeType> visitedN1Children = new LinkedHashSet<NodeType>();
        LinkedHashSet<NodeType> visitedN2Children = new LinkedHashSet<NodeType>();
        i1 = n1.getTransitionsIterator();
        while (i1.hasNext()) {
            NodeType c1 = i1.next().getTarget();
            if (allVisitedMatches.containsKey(c1)) {
                visitedN1Children.add(c1);
                visitedN2Children.add(allVisitedMatches.get(c1));
            }
        }
        i2 = n2.getTransitionsIterator();
        int numVisitedN2ChildrenFound = 0;
        while (i2.hasNext()) {
            NodeType c2 = i2.next().getTarget();
            if (allVisitedMatches.containsValue(c2)) {
                if (!visitedN2Children.contains(c2)) {
                    // c2 has been visited but doesn't map to a visited child of
                    // n1.
                    return false;
                } else {
                    numVisitedN2ChildrenFound++;
                }
            } else {
                if (visitedN2Children.contains(c2)) {
                    // c2 has not been visited but _does_ map to a visited child
                    // of n1 -- visitedMatches hash is therefore inconsistent.
                    throw new InternalSynopticException(
                            "Inconsistent kTails exploration.");
                } else {

                }
            }
        }
        // We were not able to find all visitedN2Children as children of n2.
        if (numVisitedN2ChildrenFound != visitedN2Children.size()) {
            return false;
        }

        // Below, we are going to skip nodes that have been previously visited
        // -- those in visitedN1Children and visitedN2Children.

        // This set contains a child of n2 if that child has been
        // mapped in some earlier iteration of the outer loop (through children
        // of n2) below.
        LinkedHashSet<NodeType> childKEquivMatches = new LinkedHashSet<NodeType>();

        // Record that we're _currently_ visiting n1 and n2 -- this is
        // necessary for cycles (for DAGs it is sufficient to mark n1
        // and n2 as visited after determining that they are kEqual).
        allVisitedMatches.put(n1, n2);

        i1 = n1.getTransitionsIterator();
        while (i1.hasNext()) {
            ITransition<NodeType> t1 = i1.next();
            NodeType c1 = t1.getTarget();
            // Skip c1 if it was visited by this method earlier.
            if (visitedN1Children.contains(c1)) {
                continue;
            }

            boolean kEqual = false;
            // Make sure to get transitions of the same relation.
            i2 = n2.getTransitionsIterator(t1.getRelation());
            while (i2.hasNext()) {
                NodeType c2 = i2.next().getTarget();
                // Skip c2 if it was visited by this method earlier.
                if (visitedN2Children.contains(c2)) {
                    continue;
                }

                // Skip c2 if its already been mapped to a c1 previously in the
                // outer loop.
                if (childKEquivMatches.contains(c2)) {
                    continue;
                }

                if (kEqualsWithoutSubsumption(c1, c2, k - 1, allVisitedMatches)) {
                    kEqual = true;
                    childKEquivMatches.add(c2);
                    break;
                } else {

                }
            }
            // Could not find any kEqual c2 to match with c1.
            if (!kEqual) {
                // Remove the record of visiting n1 and n2.
                allVisitedMatches.remove(n1);
                return false;
            }
        }
        // TODO: update this description for loops
        // We are k-equivalent at this point because:
        // 0. Labels of n1 and n2 match
        // 1. The child sets have the same size
        // 2. Each child of n1 matches to exactly one child of n2
        // 3. Each pair of matched children are k-1 equivalent.
        return true;
    }
}
