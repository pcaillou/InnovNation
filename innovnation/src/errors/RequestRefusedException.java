package errors;

@SuppressWarnings("serial")
public class RequestRefusedException extends RuntimeException {

	public RequestRefusedException() {
		super();
	}

	public RequestRefusedException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestRefusedException(String message) {
		super(message);
	}

	public RequestRefusedException(Throwable cause) {
		super(cause);
	}

}
