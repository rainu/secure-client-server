package de.raysha.lib.net.scs.exception;

public class SerializeException extends RuntimeException {
	private static final long serialVersionUID = -8054880647494138021L;

	public SerializeException() {
	}

	public SerializeException(String message) {
		super(message);
	}

	public SerializeException(Throwable cause) {
		super(cause);
	}

	public SerializeException(String message, Throwable cause) {
		super(message, cause);
	}

}
