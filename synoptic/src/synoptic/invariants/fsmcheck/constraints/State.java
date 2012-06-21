package synoptic.invariants.fsmcheck.constraints;

/**
 * Represents various states for a constrained AFby and AP FSM.
 */
public enum State {
	// Used by both AFby and AP FSM
	NIL(true),
	SUCCESS_B(true),
	FAIL_B(false),
	
	// Only AFby
	NOT_B(false),
	FIRST_A_REJECT(false),
	
	// Only AP
	NEITHER(true),
	FIRST_A_ACCEPT(true);

	private boolean isSuccess;

	State(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}	
}