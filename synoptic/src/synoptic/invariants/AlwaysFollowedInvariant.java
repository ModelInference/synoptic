package synoptic.invariants;

import java.util.List;

import synoptic.main.Main;
import synoptic.model.interfaces.INode;

/**
 * @author Sigurd Schneider
 */
public class AlwaysFollowedInvariant extends BinaryInvariant {

    public AlwaysFollowedInvariant(String typeFrist, String typeSecond,
            String relation) {
        super(typeFrist, typeSecond, relation);
    }

    @Override
    public String toString() {
        return first + " alwaysFollowedBy(" + relation + ") " + second;
    }

    @Override
    public String getLTLString() {
        if (useDIDCAN) {
            /**
             * Version 1:
             * 
             * <pre>
             * [] ( did(first) -> <> did(second) )
             * </pre>
             * 
             * Can loop infinitely in a loop that does not reach a terminal
             * node. In a sense it is completely unfair -- it has no fairness
             * constraints.
             */
            /**
             * Version 2:
             * 
             * <pre>
             * (<>(did(TERMINAL))) -> [] ( did(first) -> <> did(second) )
             * </pre>
             * 
             * Only considers paths that can reach the TERMINAL node, and only
             * then checks the AFby invariant along those paths. WARNING: this
             * version does not work (at all) for non-terminating traces!
             */
            /**
             * For more information see: http://mitpress.
             * mit.edu/catalog/item/default.asp?ttype=2&tid=11481
             */
            // Using Version 2:
            return "(<> (did(" + Main.terminalNodeLabel + "))) -> [](did("
                    + first + ") -> (<> (did(" + second + "))))";

            // return "[](did(" + first + ") -> <> did(" + second + ")))";
        } else {
            // Version 1: return "[](" + first + " -> (<>" + second + "))";
            // Using Version 2:
            return "(<> (" + Main.terminalNodeLabel + ")) -> [](" + first
                    + " -> (<>" + second + "))";
        }
    }

    /**
     * TODO: why does this invariant type not need violating trace shortening
     * like the other types?
     */
    @Override
    public <T extends INode<T>> List<T> shorten(List<T> trace) {
        return trace;
    }

    @Override
    public String getShortName() {
        return "AFby";
    }

}
