package de.raysha.net.scs;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import de.raysha.net.scs.exception.NoSerializerFoundException;
import de.raysha.net.scs.exception.UnknownMessageException;
import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.serialize.MessageSerializer;
import de.raysha.net.scs.utils.HashGenerator;

/**
 * This class is an abstraction level for make it easy to communicate between server and client over {@link Socket}s.
 *
 * @author rainu
 */
public abstract class Connector {
	protected static final int BUFFER_SIZE = 8096;
	private static final int NORMALIZED_MESSAGE_ID_LENGTH = 32; //length of md5 string

	private final Object sendMonitor = new Object();
	private final Object receiveMonitor = new Object();

	private final Socket socket;

	protected final Map<String, Class<? extends Message>> messageIdToClass = new HashMap<String, Class<? extends Message>>();
	protected final Map<Class<? extends Message>, String> classToMessageId = new HashMap<Class<? extends Message>, String>();
	protected final Map<Class<? extends Message>, MessageSerializer<? extends Message>> messageSerializer =
			new HashMap<Class<? extends Message>, MessageSerializer<? extends Message>>();

	public Connector(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Register a {@link MessageSerializer} for a message-class. This serializer
	 * is responsible for de-/serialize the in-/out- coming messages.
	 *
	 * @param messageClass For which class is the given serializer responsible for.
	 * @param serializer The serailizer for the given message class.
	 */
	public <M extends Message> void registerSerializer(
			Class<M> messageClass, MessageSerializer<M> serializer){

		final String messageId = messageClass.getName();
		registerSerializer(messageId, messageClass, serializer);
	}

	/**
	 * Register a {@link MessageSerializer} for a message-class. This serializer
	 * is responsible for de-/serialize the in-/out- coming messages.
	 *
	 * @param messageId This id will transfer between client and server! So the id must absolutely
	 * unique! Otherwise it can cause trouble at the de/-serialization of messages.
	 * @param messageClass For which class is the given serializer responsible for.
	 * @param serializer The serailizer for the given message class.
	 */
	public <M extends Message> void registerSerializer(
			String messageId, Class<M> messageClass, MessageSerializer<M> serializer){

		final String normalizedMessageId = normalizeMessageId(messageId);

		messageIdToClass.put(normalizedMessageId, messageClass);
		classToMessageId.put(messageClass, normalizedMessageId);
		messageSerializer.put(messageClass, serializer);
	}

	private String normalizeMessageId(String messageId) {
		final String md5 = HashGenerator.toMD5(messageId);

		assert md5.length() == NORMALIZED_MESSAGE_ID_LENGTH;

		return md5;
	}

	/**
	 * Get the {@link MessageSerializer} that is responsible for the given class. If no serializer was
	 * registered before, null will be returned.
	 *
	 * @param messageClass The message class.
	 * @return The corresponding {@link MessageSerializer} or <b>null</b> if there is no serializer for that message class.
	 */
	@SuppressWarnings("unchecked")
	public final <M extends Message> MessageSerializer<M> getSerializerFor(Class<M> messageClass) {
		return (MessageSerializer<M>) messageSerializer.get(messageClass);
	}

	/**
	 * Disconnect from my partner.
	 *
	 * @throws IOException if an I/O error occurs when closing my socket.
	 */
	public void disconnect() throws IOException {
		socket.close();
	}

	/**
	 * Use the registered {@link MessageSerializer} to serialize the given message. If there was no serializer
	 * found, an {@link NoSerializerFoundException} will be thrown!
	 *
	 * @param message The message which should be serialize.
	 * @return The raw byte-array that contains the serialiesed message.
	 * @throws NoSerializerFoundException if no serialiser is registered for this type of message.
	 */
	protected final byte[] serialize(Message message) {
		@SuppressWarnings("unchecked")
		MessageSerializer<? super Message> serializer =
				(MessageSerializer<? super Message>) getSerializerFor(message.getClass());

		if(serializer == null){
			throw new NoSerializerFoundException(message.getClass());
		}

		return serializer.serialize(message);
	}

	/**
	 * Use the registered {@link MessageSerializer} to deserialize the given raw message. If there was no serializer
	 * found, an {@link NoSerializerFoundException} will be thrown!
	 *
	 * @param messageId The received uniqe message-type-id.
	 * @param raw The serialized raw message, which should be deserialise.
	 * @return The deserialized {@link Message}-instance.
	 * @throws UnknownMessageException if the message id is unknown (no class is mapped for it).
	 * @throws NoSerializerFoundException if no serialiser is registered for this type of message.
	 */
	@SuppressWarnings("unchecked")
	protected final <M extends Message> M deserialize(String messageId, byte[] raw){
		Class<? extends Message> messageClass = messageIdToClass.get(messageId);

		if(messageClass == null){
			throw new UnknownMessageException(messageId);
		}

		MessageSerializer<? super Message> serializer =
				(MessageSerializer<? super Message>) getSerializerFor(messageClass);

		if(serializer == null){
			throw new NoSerializerFoundException(messageClass);
		}

		return (M) serializer.deserialize(raw);
	}

	/**
	 * Send a {@link Message} through my {@link Socket}.
	 *
	 * @param message The message to be send.
	 * @throws IOException If an error occurs while sending the message.
	 */
	public abstract void send(Message message) throws IOException;

	/**
	 * Sends a the given raw message.
	 *
	 * @param messageType Which type of message are the raw-content?
	 * @param message The raw message.
	 * @throws IOException If an error occurs while sending a message.
	 */
	protected final void sendRaw(Class<? extends Message> messageType, byte[] message) throws IOException{
		final String messageId = classToMessageId.get(messageType);
		byte[] rawMessageId = messageId.getBytes();
		byte[] length = ByteBuffer.allocate(4).putInt(message.length + rawMessageId.length).array();

		synchronized (sendMonitor) {
			socket.getOutputStream().write(length);
			socket.getOutputStream().write(rawMessageId);
			socket.getOutputStream().write(message);
			socket.getOutputStream().flush();
		}
	}

	/**
	 * Receive a message from my {@link Socket}. This is a blocking call! That
	 * means that this method blocks until a message was received or a {@link IOException} was
	 * thrown.
	 *
	 * @return The received {@link Message}.
	 * @throws IOException If an error occurs while receiving a message.
	 */
	public abstract Message receive() throws IOException;

	/**
	 * Receive the next raw message. This is a blocking call! That
	 * means that this method blocks until a message was received or a {@link IOException} was
	 * thrown.
	 *
	 * @return The received {@link RawMessage}.
	 * @throws IOException If an error occurs while receiving a message.
	 */
	protected final RawMessage receiveRaw() throws IOException{
		final ByteBuffer builder;
		final int length;

		synchronized (receiveMonitor) {
			InputStream in = socket.getInputStream();
			length = receiveLength(in);
			int totalRead = 0;

			builder = ByteBuffer.allocate(length);

			while(length > totalRead){
				byte[] buffer = new byte[length - totalRead];

				int read = in.read(buffer);
				if(read < 0) {
					break;
				}

				totalRead += read;

				builder.put(buffer, 0, read);
			}

			builder.flip();

			return parseRawMessage(builder, length);
		}
	}

	private int receiveLength(InputStream in) throws IOException {
		byte[] length = new byte[4];

		int read = in.read(length);
		if(read != 4){
			throw new IOException("The protocol was not followed. No length is given!");
		}

		return ByteBuffer.wrap(length).getInt();
	}

	private RawMessage parseRawMessage(final ByteBuffer builder, final int totalLength) {
		byte[] rawMessageId = new byte[NORMALIZED_MESSAGE_ID_LENGTH];
		builder.get(rawMessageId, 0, NORMALIZED_MESSAGE_ID_LENGTH);

		byte[] rawMessage = new byte[totalLength - NORMALIZED_MESSAGE_ID_LENGTH];
		builder.get(rawMessage, 0, rawMessage.length);

		return new RawMessage(new String(rawMessageId), rawMessage);
	}

	protected static class RawMessage {
		protected final String messageId;
		protected final byte[] rawMessage;

		public RawMessage(String messageId, byte[] rawMessage) {
			this.messageId = messageId;
			this.rawMessage = rawMessage;
		}
	}

	@Override
	public String toString() {
		if(socket == null) super.toString();

		return socket.toString();
	}
}
