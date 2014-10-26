package de.raysha.net.scs.model.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import de.raysha.net.scs.exception.SerializeException;
import de.raysha.net.scs.model.Message;

/**
 * Normally this {@link MessageSerializer} can be used for every {@link Message} that
 * implements the {@link Serializable} interface.
 *
 * @author rainu
 *
 * @param <M>
 */
public class ObjectSerializer <M extends Message> implements MessageSerializer<M> {

	@SuppressWarnings("unchecked")
	@Override
	public M deserialize(byte[] rawMessage) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(rawMessage));
			return (M) ois.readObject();
		} catch (Exception e) {
			throw new SerializeException("Could not deserialize message!", e);
		} finally {
			if(ois != null) try{ ois.close(); }catch(IOException e){}
		}
	}

	@Override
	public byte[] serialize(M message) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try{
			ObjectOutputStream oos = new ObjectOutputStream(byteStream);

			oos.writeObject(message);
			oos.close();
		}catch(Exception e){
			throw new SerializeException("Could not serialize message!", e);
		}

		return byteStream.toByteArray();
	}

}
