/* RechargeAction.java */

/* The package of this class. */
package simpatrol.userclient.util.action;

import simpatrol.userclient.util.agent.Agent;
import simpatrol.userclient.util.agent.AgentStates;

/* Imported classes and/or interfaces. */


/**
 * Implements the action of recharging the agent's stamina.
 * 
 * Its effect can be controlled by stamina and speed limitations.
 * 
 * @see StaminaLimitation
 * @see SpeedLimitation
 */
public final class RechargeAction extends CompoundAction {
	/* Attributes. */
	/** The value to be added to the agent's stamina. */
	private double stamina;

	/* Methods. */
	/**
	 * Constructor.
	 * 
	 * @param stamina
	 *            The value to be added to the agent's stamina.
	 */
	public RechargeAction(double stamina) {
		this.stamina = stamina;
	}

	/**
	 * Returns the value to be added to the agent's stamina.
	 * 
	 * @return The value to be added to the agent's stamina.
	 */
	public double getStamina() {
		return this.stamina;
	}

	@Override
	public boolean perform_action(Agent agent) {
		agent.setState(AgentStates.RECHARGING);
		return true;
		
	}

}