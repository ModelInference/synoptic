package csight.model.fifosys.gfsm;

/**
 * An exception thrown if a deterministic operation is performed on a
 * nondeterministic GFSM.
 */
public class NondeterministicGFSMException extends Exception {

    public NondeterministicGFSMException(String reason) {
        super(reason);
    }

    private static final long serialVersionUID = 1L;

}
