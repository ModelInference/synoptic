package synoptic.invariants.fsmcheck.constraints;

/**
 * Represents various states for a constraint AP FSM.
 */
public enum APState {
	NIL(true),
	FIRST_A(false),
	FAIL_B(false),
	NEITHER(true),
	SUCCESS_B(true);
	
	private boolean isSuccess;

	APState(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	public boolean getIsSuccess() {
		return isSuccess;
	}	
}
