package dynoptic.model.fifosys.cfsm;

import java.util.Iterator;
import java.util.List;

/**
 * Represents a bad configuration in the CFSM, which will be tested for
 * reachability by the McScM model checker. A bad configuration includes (1) a
 * CFSM state, and (2) a list of regular-expressions, one per channel, that
 * describes queue contents.
 */
public class BadState {

    private final CFSMState fsmState;

    // Regular expressions, ordered according to the scm ids of the channels in
    // the CFSM.
    private final List<String> qReList;

    public BadState(CFSMState s, List<String> qReList) {
        assert s != null;
        assert qReList != null;

        this.fsmState = s;
        this.qReList = qReList;
    }

    @Override
    public String toString() {
        return toScmString();
    }

    public String toScmString() {
        String ret = "(";

        // Encode the state of all the FSMs in the CFSM.
        for (int pid = 0; pid < fsmState.getNumProcesses(); pid++) {
            ret += "automaton p" + pid + ":" + " in "
                    + fsmState.getFSMState(pid).getScmId() + ": true\n";
        }

        // Encode the queue regular expressions in SCM format, with '#'
        // acting as the channel separator.
        if (!qReList.isEmpty()) {
            ret += "with ";
            Iterator<String> iter = qReList.iterator();
            while (iter.hasNext()) {
                String re = iter.next();
                ret += re;
                if (iter.hasNext()) {
                    ret += " . # . ";
                }
            }
            ret += "\n";
        }

        return ret + ")";
    }
}
