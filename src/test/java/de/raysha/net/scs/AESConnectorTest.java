package de.raysha.net.scs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.raysha.net.scs.exception.NoSerializerFoundException;
import de.raysha.net.scs.exception.UnknownMessageException;
import de.raysha.net.scs.model.Message;
import de.raysha.net.scs.model.SimpleMessage;

public class AESConnectorTest {
	private static String secretKey;
	private static Socket server;
	private static Socket client;
	private static ServerSocket serverSocket;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException{
		byte[] raw = new byte[1024];
		new Random().nextBytes(raw);

		secretKey = new String(raw);

		serverSocket = new ServerSocket(0);

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server = serverSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					client = new Socket("localhost", serverSocket.getLocalPort());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t2.start();

		t1.join();
		t2.join();
	}

	@AfterClass
	public static void clean(){
		try { serverSocket.close(); } catch (Exception e) { }
		try { server.close(); } catch (Exception e) { }
		try { client.close(); } catch (Exception e) { }
	}

	private AESConnector buildConnector(Socket socket) throws InvalidKeyException {
		AESConnector connector = new AESConnector(socket, secretKey);

		connector.registerSerializer(SimpleMessage.class, new SimpleMessage.Serializer());

		return connector;
	}

	@Test
	public void clientToServer() throws Exception{
		final String message = "Hello World!";

		AESConnector serverConnector = buildConnector(server);
		AESConnector clientConnector = buildConnector(client);

		clientConnector.send(new SimpleMessage(message));
		Message msg = serverConnector.receive();

		assertTrue(msg instanceof SimpleMessage);
		assertEquals(message, ((SimpleMessage)msg).getMessage());
	}

	@Test
	public void serverToClient() throws Exception{
		final String message = "Hello World!";

		AESConnector serverConnector = buildConnector(server);
		AESConnector clientConnector = buildConnector(client);

		serverConnector.send(new SimpleMessage(message));
		Message msg = clientConnector.receive();

		assertTrue(msg instanceof SimpleMessage);
		assertEquals(message, ((SimpleMessage)msg).getMessage());
	}

	@Test
	public void clientToServerVeryLongMessage() throws Exception{
		final String message = StringUtils.repeat("Long", Connector.BUFFER_SIZE);

		AESConnector serverConnector = buildConnector(server);
		AESConnector clientConnector = buildConnector(client);

		clientConnector.send(new SimpleMessage(message));
		Message msg = serverConnector.receive();

		assertTrue(msg instanceof SimpleMessage);
		assertEquals(message, ((SimpleMessage)msg).getMessage());
	}

	@Test
	public void serverToClientVeryLongMessage() throws Exception{
		final String message = StringUtils.repeat("Long", Connector.BUFFER_SIZE);

		AESConnector serverConnector = buildConnector(server);
		AESConnector clientConnector = buildConnector(client);

		serverConnector.send(new SimpleMessage(message));
		Message msg = clientConnector.receive();

		assertTrue(msg instanceof SimpleMessage);
		assertEquals(message, ((SimpleMessage)msg).getMessage());
	}

	@Test
	public void clientToServerMultiMessage() throws Exception{
		final String message = "Hello World!";

		AESConnector serverConnector = buildConnector(server);
		AESConnector clientConnector = buildConnector(client);

		for(int i=0; i < 10; i++) {
			clientConnector.send(new SimpleMessage(message));
		}
		for(int i=0; i < 10; i++) {
			Message msg = serverConnector.receive();

			assertTrue(msg instanceof SimpleMessage);
			assertEquals(message, ((SimpleMessage)msg).getMessage());
		}
	}

	@Test
	public void serverToClientMultiMessage() throws Exception{
		final String message = "Hello World!";

		AESConnector serverConnector = buildConnector(server);
		AESConnector clientConnector = buildConnector(client);

		for(int i=0; i < 10; i++) {
			serverConnector.send(new SimpleMessage(message));
		}
		for(int i=0; i < 10; i++) {
			Message msg = clientConnector.receive();

			assertTrue(msg instanceof SimpleMessage);
			assertEquals(message, ((SimpleMessage)msg).getMessage());
		}
	}

	@Test(expected = UnknownMessageException.class)
	public void unknownMessageOnReceive() throws Exception{
		final String message = "Hello World!";

		AESConnector serverConnector = new AESConnector(server, secretKey);
		AESConnector clientConnector = buildConnector(client);

		clientConnector.send(new SimpleMessage(message));
		serverConnector.receive();
	}

	@Test(expected = NoSerializerFoundException.class)
	public void noSerializerOnSend() throws Exception{
		final String message = "Hello World!";

		AESConnector clientConnector = new AESConnector(client, secretKey);

		clientConnector.send(new SimpleMessage(message));
	}
}
