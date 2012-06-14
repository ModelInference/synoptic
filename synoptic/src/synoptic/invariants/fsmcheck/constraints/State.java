package synoptic.invariants.fsmcheck.constraints;

/**
 * Represents various states for a constrained AFby and AP FSM.
 */
public enum State {
	// Used by both AFby and AP FSM
	NIL(true),
	FIRST_A(false),
	SUCCESS_B(true),
	FAIL_B(false),
	
	// Only AFby
	NOT_B(false),
	
	// Only AP
	NEITHER(true);

	private boolean isSuccess;

	State(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}	
}