/* CycledSimulator.java */

/* The package of this class. */
package control.simulator;

/* Imported classes and/or interfaces. */
import java.net.SocketException;
import model.agent.Agent;
import model.agent.AgentStates;
import model.agent.Society;
import model.graph.Vertex;
import control.coordinator.Coordinator;
import control.daemon.ActionDaemon;
import control.daemon.AgentDaemon;
import control.daemon.PerceptionDaemon;

/** Implements a simulator of the patrolling task of which time counting
 *  is based on the agents' perceive-think-act cycle.
 *  
 *  @modeller This class must have its behaviour modelled. */
public final class CycledSimulator extends Simulator {
	/* Attributes. */
	/** The coordinator of the simulation, that assures the correct elapsing
	 *  of cycles when the simulator is working. */
	private Coordinator coordinator;
	
	/* Methods. */
	/** Constructor.
	 * 
	 *  @param local_socket_number The number of the UDP socket of the main connection.
	 *  @param actualization_time_rate The time rate, in seconds, to actualize the internal model of the simulation. 
	 *  @throws SocketException */
	public CycledSimulator(int local_socket_number, double actualization_time_rate) throws SocketException {
		super(local_socket_number, actualization_time_rate);		
		this.coordinator = null;
	}
	
	/** Verifies if all agents just acted.
	 * 
	 *  @return TRUE if all agent are in the JUST_ACTED state, FALSE if not. */
	public boolean allAgentsJustActed() {
		// for each society
		Society[] societies = this.getEnvironment().getSocieties();
		for(int i = 0; i < societies.length; i++) {
			// for each agent
			Agent[] agents = societies[i].getAgents();
			for(int j = 0; j < agents.length; j++)
				if(agents[j].getAgentState() != AgentStates.JUST_ACTED)
					return false;
		}
		
		// default answer
		return true;
	}
	
	/** Verifies if all agents just perceived.
	 * 
	 *  @return TRUE if all agent are in the JUST_PERCEIVED state, FALSE if not. */
	public boolean allAgentsJustPerceived() {
		// for each society
		Society[] societies = this.getEnvironment().getSocieties();
		for(int i = 0; i < societies.length; i++) {
			// for each agent
			Agent[] agents = societies[i].getAgents();
			for(int j = 0; j < agents.length; j++)
				if(agents[j].getAgentState() != AgentStates.JUST_PERCEIVED)
					return false;
		}
		
		// default answer
		return true;
	}
	
	/** Locks all the agents' perceptions.
	 * 
	 * @param lock FALSE if the agents can perceive, TRUE if cannot. */
	public void lockAgentsPerceptions(boolean lock) {
		Object[] perception_daemons_array = this.perception_daemons.toArray();
		for(int i = 0; i < perception_daemons_array.length; i++)
			((PerceptionDaemon) perception_daemons_array[i]).setCan_work(!lock);
	}
	
	/** Locks or unlocks all the agents' actions.
	 * 
	 *  @param lock FALSE if the agents can act, TRUE if cannot. */
	public void lockAgentsActions(boolean lock) {
		Object[] action_daemons_array = this.action_daemons.toArray();
		for(int i = 0; i < action_daemons_array.length; i++)
			((ActionDaemon) action_daemons_array[i]).setCan_work(!lock);
	}
	
	/** Removes the eventual "agent - action spent stamina - perception spent stamina"
	 *  trio memorized in the coordinator of the simulation.
	 *  
	 *  Used when the given agent dies.
	 *  
	 *  @param agent The agent of which trio must be removed. */
	public void removeAgentSpentStaminas(Agent agent) {
		this.coordinator.removeAgentSpentStaminas(agent);
	}
	
	/** @modeller This method must be modelled. */
	public void startSimulation(int simulation_time) {
		// super code execution
		super.startSimulation(simulation_time);
		
		// creates the coordinator, and sets it as the
		// time counter to the vertexes
		this.coordinator = new Coordinator(this, simulation_time);
		Vertex.setTime_counter(this.coordinator);
		
		// sets the coordinator to the agent daemons
		AgentDaemon.setStamina_coordinator(this.coordinator);		
		
		// starts the coordinator (and so, the simulation)
		this.coordinator.start();
	}	
}