package synoptic.invariants.fsmcheck.constraints;

public enum APState {
	NIL(true),
	FIRST_A(false),
	FAIL_B(false),
	AFTER_A(true);
	
	private boolean isSuccess;

	APState(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	public boolean getIsSuccess() {
		return isSuccess;
	}	
}
