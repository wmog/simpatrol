/* PerpetualAgent.java */

/* The package of this class. */
package model.agent;

/* Imported classes and/or interfaces. */
import model.graph.Vertex;

/** Implements the agents that compound the 
 *  closed societies of SimPatrol. */
public class PerpetualAgent extends Agent {
	/* Methods. */
	/** Constructor.
	 *  @param vertex The vertex that the agent comes from.
	 *  @param label The label of the agent. */
	public PerpetualAgent(String label, Vertex vertex) {
		super(label, vertex);
	}
}
