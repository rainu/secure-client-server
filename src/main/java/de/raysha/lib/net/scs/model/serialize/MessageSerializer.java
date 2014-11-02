package de.raysha.lib.net.scs.model.serialize;

import de.raysha.lib.net.scs.model.Message;

/**
 * A class that implements this interface is responsible for de-/serialze a {@link Message}
 * for transfer between client and server.
 *
 * @author rainu
 *
 * @param <M>
 */
public interface MessageSerializer <M extends Message>{

	/**
	 * Serialize the given message.
	 *
	 * @param message The message
	 * @return The serialized raw message that can be used for transferring.
	 */
	public byte[] serialize(M message);

	/**
	 * Deserialize the given raw message to a instance of my type of message.
	 *
	 * @param rawMessage The raw message.
	 * @return The deserialized {@link Message} instance.
	 */
	public M deserialize(byte[] rawMessage);
}
