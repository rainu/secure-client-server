package de.raysha.lib.net.scs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.raysha.lib.net.scs.Server;

public class AbstractServerTest {

	private Server buildServer() throws IOException{
		return new Server(0) {
			@Override
			protected void handleNewSocket(Socket newSocket) {
			}
		};
	}

	@Test(expected = IllegalStateException.class)
	public void stopBeforeStart() throws IOException{
		Server server = buildServer();

		server.shutdown();
	}

	@Test(expected = IllegalStateException.class)
	public void startMultiple() throws IOException{
		Server server = buildServer();

		server.start();
		server.start();
	}

	@Test(expected = IllegalStateException.class)
	public void stopMultiple() throws IOException{
		Server server = buildServer();

		server.start();
		server.shutdown();
		server.shutdown();
	}

	@Test
	public void newConnection() throws IOException, InterruptedException{
		ServerSocket socket = new ServerSocket(0);
		final List<Socket> list = new ArrayList<Socket>();

		Server server = new Server(socket) {
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
		Server server = new Server(socket) {
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
		Server server = new Server(socket) {
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
