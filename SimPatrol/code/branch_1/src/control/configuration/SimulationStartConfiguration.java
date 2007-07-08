/* SimulationStartConfiguration.java */

/* The package of this class. */
package control.configuration;

/** Implements objects that express configurations to start
 *  a simulation with a specific time of duration. */
public final class SimulationStartConfiguration extends Configuration {
	/* Attributes. */
	/** The time of simulation. */
	private int simulation_time;
	
	/* Methods. */
	/** Constructor.
	 * 
	 *  @param sender_address The The IP address of the sender of the configuration.
	 *  @param sender_socket The number of the UDP socket of the sender.
	 *  @param simulation_time The time of simulation. */	
	public SimulationStartConfiguration(String sender_address, int sender_socket, int simulation_time) {
		super(sender_address, sender_socket);
		this.simulation_time = simulation_time;
	}
	
	/** Returns the time of simulation.
	 * 
	 *  @return The time of simulation. */
	public int getSimulation_time() {
		return this.simulation_time;
	}
	
	@Override
	protected int getType() {
		return ConfigurationTypes.SIMULATION_START; 
	}
	
	public String toXML(int identation) {
		// holds the answer to the method
		StringBuffer buffer = new StringBuffer();
		
		// applies the identation and fills the "configuration" tag
		for(int i = 0; i < identation; i++) buffer.append("/t");
		buffer.append("<configuration type=\"" + ConfigurationTypes.SIMULATION_START +
				      "\" sender_adress=\"" + this.sender_address +
				      "\" sender_socket=\"" + this.sender_socket +
				      "\" parameter=\"" + this.simulation_time +
				      "\"/>\n");
		
		// return the answer to the method
		return buffer.toString();
	}
}