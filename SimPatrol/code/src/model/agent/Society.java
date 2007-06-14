/* Society.java */

/* The package of this class. */
package model.agent;

/* Imported classes and/or interfaces. */
import java.util.HashSet;
import java.util.Set;
import model.interfaces.XMLable;

/** Implements the societies of agents of SimPatrol. */
public abstract class Society implements XMLable {
	/* Attributes. */
	/** The object id of the graph.
	 *  Not part of the patrol problem modelling. */
	private String id;
	
	/** The label of the society. */	
	private String label;
	
	/** The set of agents of the society. */	
	protected Set<Agent> agents;
	
	/* Methods. */
	/** Constructor.
	 *  @param label The label of the society.
	 *  @param agents The agents that compound the society. */
	public Society(String label, Agent[] agents) {
		this.label  = label;
		
		this.agents = new HashSet<Agent>();
		for(int i = 0; i < agents.length; i++)
			this.agents.add(agents[i]);
	}
	
	/** Activates the agents of the society. */
	public void startAgents() {
		Object[] agents_array = this.agents.toArray();
		for(int i = 0; i < agents_array.length; i++)
			((Agent) agents_array[i]).start();
	}
	
	/** Deactivates the agents of the society. */
	public void stopAgents() {
		Object[] agents_array = this.agents.toArray();
		for(int i = 0; i < agents_array.length; i++)
			((Agent) agents_array[i]).stopWorking();
	}
	
	/** Returns the agents of the society. */
	public Agent[] getAgents() {
		Object[] agents_array = this.agents.toArray();
		
		Agent[] answer = new Agent[agents_array.length];
		for(int i = 0; i < answer.length; i++)
			answer[i] = (Agent) agents_array[i];
		
		return answer;
	}
	
	public String toXML(int identation) {
		// holds the answer being constructed
		StringBuffer buffer = new StringBuffer();
		
		// applies the identation
		for(int i = 0; i < identation; i++)
			buffer.append("\t");
		
		// fills the buffer 
		buffer.append("<society id=\"" + this.id + 
				      "\" label=\"" + this.label +
				      "\" is_closed=\"true" +
				      "\">\n");
		
		// inserts the agents
		Object[] agents_array = this.agents.toArray();
		for(int i = 0; i < agents_array.length; i++)
			buffer.append(((Agent) agents_array[i]).toXML(identation + 1));
		
		// finishes the buffer content
		for(int i = 0; i < identation; i++)
			buffer.append("\t");		
		buffer.append("</society>\n");		
		
		// returns the buffer content
		return buffer.toString();
	}
	
	public String getObjectId() {
		return this.id;
	}

	public void setObjectId(String object_id) {
		this.id = object_id;
	}
}