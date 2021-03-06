package common;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Set;

import org.omg.CORBA.INTERNAL;

import log_clients.LogFileClient;
import util.Keyboard;
import util.file.FileReader;
import util.net.TCPClientConnection;

/**
 * Implements a client that connects to the SimPatrol server and configures it,
 * letting agent clients connect to it, in the sequence.
 */
public abstract class Client extends Thread {
	/* Attributes. */
	/** The path of the file that contains the environment. */
	private String ENVIRONMENT_FILE_PATH = "";

	/** The path of the file to store the log of the simulation. */
	protected String LOG_FILE_PATH = "";

	/** The time of the simulation. */
	private double TIME_OF_SIMULATION = 1000;

	/** Holds if the simulator is a real time one. */
	protected boolean IS_REAL_TIME_SIMULATOR = false;

	/** The TCP connection with the server. */
	protected TCPClientConnection CONNECTION;

	/** The set of agents acting in the simulation. */
	protected Set<Agent> agents;

	/** The client added to log the simulation. */
	private LogFileClient log_client;

	protected boolean CREATE_AGENTS = true;

	private boolean START_SIMULATION = true;

	protected  boolean INTERATIVE_MODE = false;
	
	

	/* Methods. */
	/**
	 * Constructor.
	 * 
	 * @param remote_socket_address
	 *            The IP address of the SimPatrol server.
	 * @param remote_socket_number
	 *            The number of the socket that the server is supposed to listen
	 *            to this client.
	 * @param environment_file_path
	 *            The path of the file that contains the environment.
	 * @param metrics_file_paths
	 *            The paths of the files that will save the collected metrics:
	 *            index 0: The file that will save the mean instantaneous
	 *            idlenesses; index 1: The file that will save the max
	 *            instantaneous idlenesses; index 2: The file that will save the
	 *            mean idlenesses; index 3: The file that will save the max
	 *            idlenesses;
	 * @param log_file_path
	 *            The path of the file to log the simulation.
	 * @param metrics_collection_rate
	 *            The time interval used to collect the metrics.
	 * @param time_of_simulation
	 *            The time of simulation.
	 * @param is_real_time_simulator
	 *            TRUE if the simulator is a real time one, FALSE if not.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public Client(String remote_socket_address, int remote_socket_number,
			String environment_file_path, String log_file_path,
			double time_of_simulation, boolean is_real_time_simulator)
			throws UnknownHostException, IOException {
		this.ENVIRONMENT_FILE_PATH = environment_file_path;
		this.LOG_FILE_PATH = log_file_path;
		this.TIME_OF_SIMULATION = time_of_simulation;
		this.IS_REAL_TIME_SIMULATOR = is_real_time_simulator;
		this.CONNECTION = new TCPClientConnection(remote_socket_address,
				remote_socket_number);
		this.agents = null;
		this.log_client = null;
	}
	
	public Client(String cmd_args[])
			throws UnknownHostException, IOException {
		if( cmd_args.length < 3 ){
			System.out
			.println("Usage \"SimpatrolClient "
						+ "<IP address> <Remote socket number> <Environment file path> "
						+ "[-t <Time of simulation>] [-i] [-r] [-c] [-s] [-l <logfile>]\""
						+ "Where:\t\t -t <Time of Simulation> - Default 1000"
						+ "\t\t-c - Don't create and start agents: Default create"	
						+ "\t\t-s - Don't start simulation: Default start"
						+ "\t\t-r - Real time simulation: Default: cycled Simulation"
						+ "\t\t-i - Interative mode"
						+ "\t\t-l <logfile> - Log Simulation on logfile: Default don't save log");							
			System.exit(0);
		} else {
			processCmdLine(cmd_args);
			this.ENVIRONMENT_FILE_PATH = cmd_args[2];
			this.CONNECTION = new TCPClientConnection(cmd_args[0],
					Integer.parseInt(cmd_args[1]));
			this.agents = null;
			this.log_client = null;
		}
		
		
	}

	private void processCmdLine(String[] cmd_args) {
		boolean processedAllOptions = false;
		for(int i = 0; i < cmd_args.length; i++){
			if(cmd_args[i].equals("-t")){
				this.TIME_OF_SIMULATION = Double.parseDouble(cmd_args[i+1]);
				i++;
			}
			if(cmd_args[i].equals("-l")){
				this.LOG_FILE_PATH = cmd_args[i+1];
				i++;
			}
			if(cmd_args[i].equals("-r")){
				this.IS_REAL_TIME_SIMULATOR = true;				
			}
			if(cmd_args[i].equals("-c")){
				this.CREATE_AGENTS  = false;				
			}
			if(cmd_args[i].equals("-s")){
				this.START_SIMULATION   = false;				
			}
			if(cmd_args[i].equals("-i")){
				this.INTERATIVE_MODE    = true;				
			}
			
		}
	}

	/**
	 * Obtains the environment from the referred file and configures it into the
	 * server, returning the activated sockets for the remote agents, as well as
	 * the respective agent IDs.
	 * 
	 * @return The socket numbers for the agents' connections, as well as the
	 *         respective agent IDs.
	 * @throws IOException
	 */
	private StringAndInt[] configureEnvironment() throws IOException {
		// screen message
		System.out.print("Creating the environment of the simulation... ");

		// the file reader that will obtain the environment from the given file
		FileReader file_reader = new FileReader(this.ENVIRONMENT_FILE_PATH);

		// holds the environment obtained from the file
		StringBuffer buffer = new StringBuffer();
		while (!file_reader.isEndOfFile()) {
			buffer.append(file_reader.readLine());
		}

		// closes the read file
		file_reader.close();

		// mounts the message of configuration
		String message = "<configuration type=\"0\">" + buffer.toString()
				+ "</configuration>";

		// sends it to the server
		this.CONNECTION.send(message);

		// obtains the answer from the server
		String[] server_answer = this.CONNECTION.getBufferAndFlush();
		while (server_answer.length == 0)
			server_answer = this.CONNECTION.getBufferAndFlush();

		// from the answer, obtains the sockets activated for each agent,
		// as well as the respective agent IDs
		LinkedList<StringAndInt> ids_and_sockets = new LinkedList<StringAndInt>();
		String received_message = server_answer[0];
		int next_agent_index = received_message.indexOf("agent_id=\"");
		while (next_agent_index > -1) {
			received_message = received_message
					.substring(next_agent_index + 10);
			String agent_id = received_message.substring(0, received_message
					.indexOf("\""));

			int next_socket_index = received_message.indexOf("socket=\"");
			received_message = received_message
					.substring(next_socket_index + 8);
			int socket = Integer.parseInt(received_message.substring(0,
					received_message.indexOf("\"")));

			ids_and_sockets.add(new StringAndInt(agent_id, socket));
			next_agent_index = received_message.indexOf("agent_id=\"");
		}

		// mounts the answer of the method
		StringAndInt[] answer = new StringAndInt[ids_and_sockets.size()];
		for (int i = 0; i < answer.length; i++)
			answer[i] = ids_and_sockets.get(i);

		// screen message
		System.out.println("Finished.");

		// returns the answer
		return answer;
	}

	/**
	 * Configures the collecting of events during the simulation.
	 * 
	 * @return The socket number for the log clients be connected.
	 * @throws IOException
	 */
	private int configureLogging() throws IOException {
		// the answer for the method
		int answer = -1;
		String key = "";

		// asks if a log connection shall be established
		if( INTERATIVE_MODE ){
			System.out.println("Should I log the simulation? [y]es or [n]o?");
			key = Keyboard.readLine();
		}
		
		if ( (INTERATIVE_MODE && ! key.equalsIgnoreCase("n")) || !LOG_FILE_PATH.equals("") ) {
			// the message to establish a connection to the server to log the
			// simulation
			String message = "<configuration type=\"5\"/>";

			// sends it to the server
			this.CONNECTION.send(message);

			// obtains the answer from the server
			String[] server_answer = this.CONNECTION.getBufferAndFlush();
			while (server_answer.length == 0)
				server_answer = this.CONNECTION.getBufferAndFlush();

			// adds it to the answer of the method
			int metric_socket_index = server_answer[0].indexOf("message=\"");
			server_answer[0] = server_answer[0]
					.substring(metric_socket_index + 9);
			answer = Integer.parseInt(server_answer[0].substring(0,
					server_answer[0].indexOf("\"")));

			// screen message
			System.out.println("Log connection established.");
		}

		// returns the answer
		return answer;
	}

	/**
	 * Creates and starts the log client, given the numbers of the socket
	 * offered by the server.
	 * 
	 * @param socket_number
	 *            The socket number offered by the server to connect to the
	 *            remote client.
	 * @throws IOException
	 */
	private void createAndStartLogClient(int socket_number) throws IOException {
		// if the socket number is valid
		String key = "";
		if (socket_number > -1) {
			// asks if the client shall itself start the log client
			if( INTERATIVE_MODE ){
				System.out
						.println("Should I myself create and start the log client? [y]es or [n]o?");
				key = Keyboard.readLine();
			}
			if ( (INTERATIVE_MODE && ! key.equalsIgnoreCase("n")) || !LOG_FILE_PATH.equals("") ) {
				// screen message
				System.out.print("Creating and starting the log client... ");

				// creates the log client
				this.log_client = new LogFileClient(this.CONNECTION
						.getRemoteSocketAdress(), socket_number,
						this.LOG_FILE_PATH);

				// starts the log client
				this.log_client.start();

				// screen message
				System.out.println("Finished.");
			} else
				System.out
						.println("Port offered by SimPatrol to attend log client: "
								+ socket_number);
		}
	}

	/**
	 * Configures the server to start the simulation with the given time of
	 * duration.
	 * 
	 * @throws IOException
	 */
	private void configureStart() throws IOException {
		// waits for the user to press any key to start simulation
		if( INTERATIVE_MODE || !START_SIMULATION ){
			System.out.print("Press [ENTER] to start simulation.");
			Keyboard.readLine();
		}
		// the message to be sent to the server
		String message = "<configuration type=\"3\" parameter=\""
				+ this.TIME_OF_SIMULATION + "\"/>";
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// send the message to the server
		this.CONNECTION.send(message);

		// screen message
		System.out.println("Simulation started.");
	}

	/** Stops the agents. */
	private void stopAgents() {
		if (this.agents != null)
			for (Agent agent : this.agents)
				agent.stopWorking();
	}

	public void run() {
		// starts its TCP connection
		this.CONNECTION.start();

		// configures the environment of the simulation
		// and obtains the socket numbers for each agent
		StringAndInt[] agents_socket_numbers = new StringAndInt[0];
		try {
			agents_socket_numbers = this.configureEnvironment();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// configures the log client of the simulation, obtaining its socket
		// number
		int log_socket_number = -1;
		try {
			log_socket_number = this.configureLogging();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// creates, connects and starts the log client
		try {
			this.createAndStartLogClient(log_socket_number);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// creates, connects and starts the agents
		// asks if the client should itself create and start the agents
		String key = "";
		if( INTERATIVE_MODE ){
			System.out
					.println("Should I myself create and start the agent clients? [y]es or [n]o?");
			
			try {
				key = Keyboard.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if ((INTERATIVE_MODE && ! key.equalsIgnoreCase("n")) || CREATE_AGENTS ) {
			try {
				String[] agents_ids = new String[agents_socket_numbers.length];
				int[] socket_numbers = new int[agents_socket_numbers.length];
				for (int i = 0; i < socket_numbers.length; i++) {
					agents_ids[i] = agents_socket_numbers[i].STRING;
					socket_numbers[i] = agents_socket_numbers[i].INTEGER;
				}

				this.createAndStartAgents(agents_ids, socket_numbers);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			for (int i = 0; i < agents_socket_numbers.length; i++) {
				System.out
						.println("SimPatrol is offering the following configuration: ");
				System.out.println("Agent ID: "
						+ agents_socket_numbers[i].STRING);
				System.out.println("Port    : "
						+ agents_socket_numbers[i].INTEGER);
			}

		// configures the simulation to start
		try {
			this.configureStart();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// while the TCP connection is alive, waits...
		while (this.CONNECTION.getState() != Thread.State.TERMINATED){
			Thread.yield();
			/*try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}

		// stops the agents
		this.stopAgents();

		// stops the log client
		if (this.log_client != null)
			this.log_client.stopWorking();

		// screen message
		System.out.println("Finished working.");
	}

	/**
	 * Creates and starts the agents, given the numbers of the sockets for each
	 * agent.
	 * 
	 * @param agents_ids
	 *            The ids of the agents to create and start.
	 * @param socket_numbers
	 *            The socket numbers offered by the server to connect to the
	 *            remote agents.
	 * @throws IOException
	 */
	protected abstract void createAndStartAgents(String[] agents_ids,
			int[] socket_numbers) throws IOException;
}

/** Internal class that holds together a string and an integer. */
final class StringAndInt {
	/* Attributes */
	/** The string value. */
	public final String STRING;

	/** The integer value. */
	public final int INTEGER;

	/* Methods. */
	/**
	 * Constructor.
	 * 
	 * @param string
	 *            The string value of the pair.
	 * @param integer
	 *            The integer of the pair.
	 */
	public StringAndInt(String string, int integer) {
		this.STRING = string;
		this.INTEGER = integer;
	}
}
