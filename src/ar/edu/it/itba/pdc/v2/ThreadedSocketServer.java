package ar.edu.it.itba.pdc.v2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ar.edu.it.itba.pdc.v2.implementations.proxy.AnalyzerImp;
import ar.edu.it.itba.pdc.v2.implementations.proxy.Attend;
import ar.edu.it.itba.pdc.v2.implementations.proxy.ClientHandler;
import ar.edu.it.itba.pdc.v2.implementations.proxy.ConnectionManagerImpl;
import ar.edu.it.itba.pdc.v2.interfaces.Analyzer;
import ar.edu.it.itba.pdc.v2.interfaces.ConnectionHandler;
import ar.edu.it.itba.pdc.v2.interfaces.ConnectionManager;

public class ThreadedSocketServer  {
	private ServerSocket serverSocket;
	private ConnectionHandler handler;
	private ConnectionManager connectionManager;
	private Analyzer analyzer;

	public ThreadedSocketServer(final int port, final InetAddress interfaz,
			final ConnectionHandler handler) throws IOException {
		init(new ServerSocket(port, 50, interfaz), handler);
	}

	private void init(final ServerSocket s, final ConnectionHandler handler) {
		if (s == null || handler == null) {
			throw new IllegalArgumentException();
		}

		this.serverSocket = s;
		this.handler = handler;
		this.connectionManager = new ConnectionManagerImpl();
		this.analyzer = new AnalyzerImp(connectionManager);
	}

	private void run() throws IOException, InterruptedException {
		ExecutorService es = Executors.newCachedThreadPool();
		System.out.println("Proxy listening on port 9090");
		while (true) {
			Socket socket = this.serverSocket.accept();
			es.execute(new Attend(socket, handler, connectionManager, analyzer));
			Thread.sleep(500);
		}
	}

	public static void main(String args[]) {
		try {
			ThreadedSocketServer server = new ThreadedSocketServer(9090,
					InetAddress.getByName("localhost"), new ClientHandler());
			server.run();
		} catch (final Exception e) {
			System.out.println("Ocurrio un error");
			e.printStackTrace();
		}
	}

}

