package csight.mc.parallelizer;

import csight.invariants.BinaryInvariant;

/**
 * Stores an invariant with its corresponding timeout value. Used in
 * checkInvsRefineGFSMParallel to manage timeout value per invariant.
 */
public class InvariantTimeoutPair {
    private final BinaryInvariant inv;

    /**
     * Timeout in seconds.
     */
    private final int timeout;

    public InvariantTimeoutPair(BinaryInvariant inv, int timeout) {
        this.inv = inv;
        this.timeout = timeout;
    }

    public BinaryInvariant getInv() {
        return inv;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inv == null) ? 0 : inv.hashCode());
        result = prime * result + timeout;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InvariantTimeoutPair other = (InvariantTimeoutPair) obj;
        if (inv == null) {
            if (other.inv != null)
                return false;
        } else if (!inv.equals(other.inv))
            return false;
        if (timeout != other.timeout)
            return false;
        return true;
    }

}
