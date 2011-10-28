
package log_clients;

import java.io.IOException;

import util.Keyboard;
import util.file.FileWriter;
import util.net.UDPClientConnection;

/**
 * Implements a client that collects the events generated by the SimPatrol 
 * during a simulation and saves them in a XML file.
 */
public class LogFileClient extends Thread {

	// Registers if the log client shall stop working
	private boolean stopWorking;

	// The UDP connection of the log client
	private UDPClientConnection connection;

	// The object that writes on the output file the obtained events.
	private FileWriter fileWriter;

	/**
	 * Constructor.
	 * 
	 * @param remoteSocketAddress
	 *            The IP address of the SimPatrol server.
	 * @param remoteSocketNumber
	 *            The number of the socket that the server writes to, related to
	 *            this client.
	 * @param filePath
	 *            The path of the file where the events will be saved.
	 */
	public LogFileClient(String remoteSocketAddress,
			int remoteSocketNumber, String filePath) throws IOException {
		this.stopWorking = false;
		this.connection = new UDPClientConnection(remoteSocketAddress, remoteSocketNumber);
		this.fileWriter = new FileWriter(filePath);
	}

	/** 
	 * Indicates that the client must stop working. 
	 */
	public void stopWorking() {
		try {
			this.stopWorking = true;
			this.connection.stopWorking();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		this.connection.start();
		
		this.fileWriter.println("<simulation_log>");

		while (!this.stopWorking) {
			String[] events = this.connection.getBufferAndFlush();

			for (int i = 0; i < events.length; i++) {
				String event = events[i];
				event = event.substring(0, event.lastIndexOf("\n"));
				this.fileWriter.println(event);
			}
		}

		this.fileWriter.println("</simulation_log>");
		this.fileWriter.close();
	}

	/**
	 * Turns this class into an executable one. Useful when running this client
	 * in an individual machine.
	 * 
	 * @param args List of arguments from command line
	 *             index 0: The IP address of the SimPatrol server.
	 *             index 1: The number of the socket of the server. 
	 *             index 2: The path of the file that will store the collected events.
	 */
	public static void main(String args[]) {
		try {
			
			String serverAddress = args[0];
			int serverSocketNumber = Integer.parseInt(args[1]);
			String filePath = args[2];

			LogFileClient client = new LogFileClient(serverAddress,
					serverSocketNumber, filePath);
			client.start();

			System.out.println("Press [t] key to terminate this client.");
			String key = "";
			
			while (!key.equals("t"))
				key = Keyboard.readLine();

			client.stopWorking();
			
		} catch (Exception e) {
			System.out.println("Usage \"java log_clients.LogFileClient "
					+ "<Server IP address> <Server socket number> <File path>\"");
		}
	}
}