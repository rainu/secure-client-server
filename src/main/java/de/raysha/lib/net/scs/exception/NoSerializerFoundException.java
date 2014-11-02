package de.raysha.lib.net.scs.exception;

import de.raysha.lib.net.scs.model.Message;

public class NoSerializerFoundException extends SerializeException {
	private static final long serialVersionUID = 5785670435364098008L;

	public NoSerializerFoundException(Class<? extends Message> messageClass) {
		super("The is no serializer registered for that type of message! " + messageClass.getName());
	}

}
