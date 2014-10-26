package de.raysha.net.scs;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.raysha.net.scs.model.Message;

/**
 * This class is a special {@link AbstractConnector} and it is responsible for secure communication between server and client.
 * All messages will be encrypt by the AES-Cipher.
 *
 * @author rainu
 */
public class AESConnector extends AbstractConnector {
	private static final byte[] PASSWORD_SALT = "Secure-Client-Server-Salt".getBytes();
	private final Cipher encryptCipher;
	private final Cipher decryptCipher;

	private final SecretKey secretKey;

	/**
	 * Creates an secure connector for client- server- communication.
	 *
	 * @param socket The socket over which should be communicate.
	 * @param key The secret key for the AES-Encryption.
	 * @throws InvalidKeyException If the given key is invalid.
	 */
	public AESConnector(Socket socket, SecretKey key) throws InvalidKeyException {
		super(socket);

		this.secretKey = key;
		this.encryptCipher = initialiseEncryptionCipher();
		this.decryptCipher = initialiseDecryptionCipher();
	}

	/**
	 * Creates an secure connector for client- server- communication.
	 *
	 * @param socket The socket over which should be communicate.
	 * @param password The secret password for the AES-Encryption.
	 * @throws InvalidKeyException If the given key is invalid.
	 */
	public AESConnector(Socket socket, String password) throws InvalidKeyException {
		this(socket, initialiseKey(password));
	}

	private static SecretKey initialiseKey(String password){
		try{
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), PASSWORD_SALT, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			return new SecretKeySpec(tmp.getEncoded(), "AES");
		}catch(Exception e){
			throw new IllegalStateException(e);
		}
	}

	private Cipher initialiseDecryptionCipher() throws InvalidKeyException {
		Cipher cipher = initialiseCipher();

		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey,
					new IvParameterSpec(new byte[cipher.getBlockSize()]));
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalStateException(e);
		}

		return cipher;
	}

	private Cipher initialiseEncryptionCipher() throws InvalidKeyException {
		Cipher cipher = initialiseCipher();

		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey,
					new IvParameterSpec(new byte[cipher.getBlockSize()]));
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalStateException(e);
		}

		return cipher;
	}

	private Cipher initialiseCipher() {
		Cipher cipher = null;

		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		return cipher;
	}

	@Override
	public void send(Message message) throws IOException {
		byte[] rawMessage = serialize(message);
		try {
			rawMessage = encryptCipher.doFinal(rawMessage);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		sendRaw(message.getClass(), rawMessage);
	}

	@Override
	public Message receive() throws IOException {
		RawMessage rawMessage = receiveRaw();
		final byte[] decryptedMessage;

		try {
			decryptedMessage = decryptCipher.doFinal(rawMessage.rawMessage);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		return deserialize(rawMessage.messageId, decryptedMessage);
	}
}
