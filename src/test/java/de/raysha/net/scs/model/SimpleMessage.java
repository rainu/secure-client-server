package de.raysha.net.scs.model;

import java.io.Serializable;

import de.raysha.net.scs.model.serialize.ObjectSerializer;

public class SimpleMessage implements Message, Serializable {
	private static final long serialVersionUID = -4843579367078104398L;

	private String message;

	public SimpleMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public static class Serializer extends ObjectSerializer<SimpleMessage> {

	}
}
