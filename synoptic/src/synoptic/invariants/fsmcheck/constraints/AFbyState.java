package synoptic.invariants.fsmcheck.constraints;

/**
 * Represents various states in a AFby FSM.
 */
public enum AFbyState {
	NIL(true),
	FIRST_A(false),
	NOT_B(false),
	SUCCESS_B(true),
	FAIL_B(false);
	
	private boolean isSuccess;

	AFbyState(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	public boolean getIsSuccess() {
		return isSuccess;
	}	
}