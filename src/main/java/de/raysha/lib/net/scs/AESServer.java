package de.raysha.lib.net.scs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;

import javax.crypto.SecretKey;

/**
 * This {@link Server} establish a {@link AESConnector} for each incoming connections.
 *
 * @author rainu
 */
public abstract class AESServer extends Server {
	private final SecretKey secretKey;
	private final String stringKey;

	public AESServer(SecretKey key, int port) throws IOException, InvalidKeyException {
		super(port);

		this.secretKey = key;
		this.stringKey = null;
		checkKey();
	}

	public AESServer(SecretKey key, ServerSocket socket) throws InvalidKeyException {
		super(socket);

		this.secretKey = key;
		this.stringKey = null;
		checkKey();
	}

	public AESServer(String key, int port) throws IOException, InvalidKeyException {
		super(port);

		this.secretKey = null;
		this.stringKey = key;
		checkKey();
	}

	public AESServer(String key, ServerSocket socket) throws InvalidKeyException {
		super(socket);

		this.secretKey = null;
		this.stringKey = key;
		checkKey();
	}

	private void checkKey() throws InvalidKeyException {
		createConnector(null);
	}

	@Override
	protected void handleNewSocket(Socket newSocket) {
		AESConnector connector;
		try {
			connector = createConnector(newSocket);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException("The key should be never changed!", e);
		}

		handleNewConnetion(connector);
	}

	private AESConnector createConnector(Socket socket) throws InvalidKeyException {
		AESConnector connector;
		if(secretKey != null){
			connector = new AESConnector(socket, secretKey);
		}else{
			connector = new AESConnector(socket, stringKey);
		}

		return connector;
	}

	protected abstract void handleNewConnetion(AESConnector connector);

}
