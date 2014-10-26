package de.raysha.net.scs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AbstractServerTest {

	private AbstractServer buildServer() throws IOException{
		return new AbstractServer(0) {
			@Override
			protected void handleNewSocket(Socket newSocket) {
			}
		};
	}

	@Test(expected = IllegalStateException.class)
	public void stopBeforeStart() throws IOException{
		AbstractServer server = buildServer();

		server.shutdown();
	}

	@Test(expected = IllegalStateException.class)
	public void startMultiple() throws IOException{
		AbstractServer server = buildServer();

		server.start();
		server.start();
	}

	@Test(expected = IllegalStateException.class)
	public void stopMultiple() throws IOException{
		AbstractServer server = buildServer();

		server.start();
		server.shutdown();
		server.shutdown();
	}

	@Test
	public void newConnection() throws IOException, InterruptedException{
		ServerSocket socket = new ServerSocket(0);
		final List<Socket> list = new ArrayList<Socket>();

		AbstractServer server = new AbstractServer(socket) {
			@Override
			protected void handleNewSocket(Socket newSocket) {
				list.add(newSocket);
			}
		};
		server.start();

		//client connect
		new Socket("localhost", socket.getLocalPort());
		Thread.sleep(100);

		assertEquals(1, list.size());
	}

	@Test(timeout = 1000)
	public void stopSoft() throws IOException{
		ServerSocket socket = new ServerSocket(0);
		AbstractServer server = new AbstractServer(socket) {
			@Override
			protected void handleNewSocket(Socket newSocket) {
				while(isShuttingDown()){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {}
				}
			}
		};
		server.start();

		//client connect
		new Socket("localhost", socket.getLocalPort());

		server.shutdown();
	}

	@Test(timeout = 5000)
	public void stopHard() throws IOException, InterruptedException{
		ServerSocket socket = new ServerSocket(0);
		AbstractServer server = new AbstractServer(socket) {
			@Override
			protected void handleNewSocket(Socket newSocket) {
				while(true){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {}
				}
			}
		};
		server.start();

		//client connect
		new Socket("localhost", socket.getLocalPort());
		Thread.sleep(100);

		server.shutdown();
	}
}