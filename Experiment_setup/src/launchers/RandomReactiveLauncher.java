package launchers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;

import random_reactive.RandomReactiveAgent;
import util.net.TCPClientConnection;
import util.net.UDPClientConnection;

import common.Agent;

public class RandomReactiveLauncher extends Launcher {

	/* Methods. */
	public RandomReactiveLauncher(String environment_dir_path, 
			String env_gen_name, int numEnv,
			String log_dir_path,  String log_gen_name,
			int time_of_simulation) throws UnknownHostException,
			IOException {
		super(environment_dir_path, 
				env_gen_name, numEnv, 
				log_dir_path, log_gen_name, 
				time_of_simulation);
	}
	
	

	protected void createAndStartAgents(String[] agent_ids, int[] socket_numbers)
			throws IOException {
		this.agents = new HashSet<Agent>();

		for (int i = 0; i < agent_ids.length; i++) {
			RandomReactiveAgent agent = new RandomReactiveAgent();

			if (this.IS_REAL_TIME_SIMULATOR)
				agent.setConnection(new UDPClientConnection(this.CONNECTION
						.getRemoteSocketAdress(), socket_numbers[i]));
			else
				agent.setConnection(new TCPClientConnection(this.CONNECTION
						.getRemoteSocketAdress(), socket_numbers[i]));

			agent.start();
			this.agents.add(agent);
		}
	}

	/**
	 * Turns this class into an executable one.
	 * 
	 * @param args
	 *            Arguments: index 0: The IP address of the SimPatrol server.
	 *            index 1: The number of the socket that the server is supposed
	 *            to listen to this client. index 2: The path of the file that
	 *            contains the environment. index 3. The path of the file that
	 *            will save the mean instantaneous idlenesses; index 4. The path
	 *            of the file that will save the max instantaneous idlenesses;
	 *            index 5. The path of the file that will save the mean
	 *            idlenesses; index 6. The path of the file that will save the
	 *            max idlenesses; index 7: The time interval used to collect the
	 *            metrics; index 8: The path of the file that will save the
	 *            collected events; index 9: The time of simulation. index 10:
	 *            false if the simulator is a cycled one, true if not.
	 */
	public static void main(String[] args) {
		System.out.println("Random reactive agents!");

		try {
			String environment_dir_path = args[0];
			String env_gen_name = args[1];
			int numEnv  = Integer.parseInt(args[2]);
			String log_dir_path = args[3];
			String log_gen_name = args[4];
			int time_of_simulation= Integer.parseInt(args[5]);

			RandomReactiveLauncher client = new RandomReactiveLauncher(
					environment_dir_path, 
					env_gen_name, numEnv, 
					log_dir_path, log_gen_name, 
					time_of_simulation);
			client.start();
		} catch (Exception e) {
			System.out
					.println("Usage \"java launchers.RandomReactiveLauncher\n"
							+ "<Environment directory path> <Environment generic name> <number of environments>\n"
							+ "<log directory path> <Log generic name> <num of cycle in simulations> \n" 
							+ "It will launch N simulations with the environments ENV_DIR_PATH\\ENV_GEN_NAME_i.txt \n"
							+ "and save the logs as LOG_DIR_PATH\\LOG_GEN_NAME_i.txt");
		}
	}
}