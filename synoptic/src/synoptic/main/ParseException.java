package synoptic.main;

public class ParseException extends Exception {

	/**
	 * Exception version id
	 */
	private static final long serialVersionUID = -4455111019098315998L;

	public ParseException() {
		super();
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}
	
}