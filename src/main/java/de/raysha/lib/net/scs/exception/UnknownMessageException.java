package de.raysha.lib.net.scs.exception;

public class UnknownMessageException extends SerializeException {
	private static final long serialVersionUID = -5872958900540602812L;

	public UnknownMessageException() {
	}

	public UnknownMessageException(String messageId) {
		super("The received message is unknown! MessageId: " + messageId);
	}
}
