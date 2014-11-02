package de.raysha.lib.net.scs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.raysha.lib.net.scs.AESConnector;
import de.raysha.lib.net.scs.AESServer;

public class AESServerTest {

	@Test
	public void newConnection() throws IOException, InterruptedException, InvalidKeyException{
		ServerSocket socket = new ServerSocket(0);
		final List<AESConnector> list = new ArrayList<AESConnector>();

		AESServer server = new AESServer("secret", socket) {
			@Override
			protected void handleNewConnetion(AESConnector connector) {
				list.add(connector);
			}
		};
		server.start();

		//client connect
		new Socket("localhost", socket.getLocalPort());
		Thread.sleep(500);

		assertEquals(1, list.size());
	}
}
