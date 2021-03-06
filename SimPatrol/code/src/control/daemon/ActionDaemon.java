/* ActionDaemon.java */

/* The package of this class. */
package control.daemon;

/* Imported classes and/or interfaces. */
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.xml.sax.SAXException;
import util.data_structures.Queue;
import control.exception.EdgeNotFoundException;
import control.exception.NodeNotFoundException;
import control.parser.CompoundActionsParser;
import control.simulator.CycledSimulator;
import control.simulator.RealTimeSimulator;
import control.simulator.SimulatorStates;
import control.translator.ActionTranslator;
import model.action.*;
import model.agent.*;
import model.graph.*;
import model.limitation.*;
import model.permission.ActionPermission;
import model.stigma.Stigma;

/**
 * Implements the daemons of SimPatrol that attend an agent's intentions of
 * actions.
 * 
 * @developer New Limitation classes can change this class.
 * @developer New Action classes must change this class.
 * @modeler This class must have its behavior modeled.
 */
public final class ActionDaemon extends AgentDaemon {
	/* Attributes. */
	/** Used for logging events. */
	// Used by AspectJ
	protected String action_message;
	
	/*
	 * Used to freeze the cycle count to do the visit time egual to zero 
	 */
	private boolean freezeCycleCount;

	private int numCyclesByVisit = 0;
	
	private int cyclesVisited = 4;
	
	/**
	 * Queue that holds the planning of atomic actions that the daemon must
	 * regularly attend, in order to satisfy an eventual compound action.
	 */
	private final Queue<AtomicAction> PLANNING;
	
	
	/*
	 * 
	 */

	/* Methods. */
	/**
	 * Constructor.
	 * 
	 * Doesn't initiate its own connection, as it will be shared with a
	 * PerceptionDaemon object. So the connection must be set by the
	 * setConection() method.
	 * 
	 * @see PerceptionDaemon
	 * 
	 * @param thread_name
	 *            The name of the thread of the daemon.
	 * @param agent
	 *            The agent whose intentions are to be attended.
	 */
	public ActionDaemon(String thread_name, Agent agent) {
		super(thread_name, agent);
		this.PLANNING = new Queue<AtomicAction>();

		if (simulator instanceof RealTimeSimulator) {
			this.clock.setStep(simulator.getUpdate_time_rate());
		} else
			this.clock = null;
	}
	
	
	
	/**
	 * Attends a Wait action
	 * 
	 * Does nothing, used by the logger
	 * 
	 * @param action
	 * 			The action of visiting a node intended by the agent.
	 */
	private void attendWaitAction(WaitAction action) {
		return;
	}

	/**
	 * Attends an intention of visiting the node where the agent is.
	 * 
	 * If the agent is not on a node (i.e., its "elapsed_length" attribute is
	 * not zero), then the action has no effect.
	 * 
	 * @param action
	 *            The action of visiting a node intended by the agent.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendVisitAction(VisitAction action, Limitation[] limitations) {
		// holds an eventual stamina limitation
		double stamina = 0;

		// for each limitation, tries to set the stamina limitation
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof StaminaLimitation)
				stamina = ((StaminaLimitation) limitations[i]).getCost();
			// developer: new limitations can add code here
		}

		// if the agent is on a node and there's enough stamina to act
		if (this.AGENT.getElapsed_length() == 0
				&& this.AGENT.getStamina() > stamina) {
			// decrements the agent's stamina, if necessary
			if (stamina > 0)
				this.AGENT.decStamina(stamina);

			// resets the idleness of the node where the agent is
			this.AGENT.getNode().setLast_visit_time(
					simulator.getElapsedTime());
		}
	}

	/**
	 * Attends an intention of immediately recharge the stamina of the agent.
	 * 
	 * If the agent is not on a "fueled" node (i.e. the node's "fuel"
	 * attribute is not TRUE), then the action has no effect.
	 * 
	 * @param action
	 *            The action of recharging the agent's stamina.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendAtomicRechargeAction(AtomicRechargeAction action,
			Limitation[] limitations) {
		// holds an eventual stamina limitation
		double stamina = 0;

		// holds an eventual speed limitation
		double speed = -1;

		// for each limitation, tries to set the stamina and speed limitations
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof StaminaLimitation)
				stamina = ((StaminaLimitation) limitations[i]).getCost();
			else if (limitations[i] instanceof SpeedLimitation)
				speed = ((SpeedLimitation) limitations[i]).getSpeed();
			// developer: new limitations must add code here
		}

		// if the agent is on a "fueled" node and there's enough stamina to
		// act
		if (this.AGENT.getNode().isFuel()
				&& this.AGENT.getStamina() > stamina) {
			// decrements the agent's stamina
			if (stamina > 0)
				this.AGENT.decStamina(stamina);

			// obtains the value to be added to the agent's stamina
			double value = action.getStamina();

			// if the value is bigger than the speed limitation,
			// sets it as the speed limitation
			if (speed > -1 && value > speed)
				value = speed;

			// increments the agent's stamina by the obtained value
			this.AGENT.incStamina(value);
		}
	}

	/**
	 * Attends an intention of depositing stigmas on the graph of the
	 * simulation.
	 * 
	 * @param action
	 *            The action of depositing a stigma intended by the agent.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendStigmatizeAction(StigmatizeAction action,
			Limitation[] limitations) {
		// holds an eventual stamina limitation
		double stamina = 0;

		// for each limitation, tries to set the stamina limitation
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof StaminaLimitation)
				stamina = ((StaminaLimitation) limitations[i]).getCost();
			// developer: new limitations must add code here
		}

		// if there's enough stamina to act
		if (this.AGENT.getStamina() > stamina) {
			// decrements the agent's stamina
			if (stamina > 0)
				this.AGENT.decStamina(stamina);

			// puts a new stigma on the node, if the agent is on it
			if (this.AGENT.getEdge() == null
					|| this.AGENT.getElapsed_length() == 0) {
				simulator.getEnvironment().getGraph().addStigma(
						new Stigma(this.AGENT.getNode()));
			}
			// else, puts the stigma on the edge
			else {
				simulator.getEnvironment().getGraph().addStigma(
						new Stigma(this.AGENT.getEdge()));
			}
		}
	}

	/**
	 * Attends an intention of broadcasting a message.
	 * 
	 * @param action
	 *            The action of broadcasting a message.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendBroadcastAction(BroadcastAction action,
			Limitation[] limitations) {
		// holds an eventual depth limitation
		int depth = -1;

		// holds an eventual stamina limitation
		double stamina = 0;

		// for each limitation, tries to set the depth and stamina limitations
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof DepthLimitation) {
				depth = ((DepthLimitation) limitations[i]).getDepth();
			} else if (limitations[i] instanceof StaminaLimitation) {
				stamina = ((StaminaLimitation) limitations[i]).getCost();
				// developer: new limitations must add code here
			}
		}

		// if there's enough stamina to act
		if (this.AGENT.getStamina() > stamina) {
			// decrements the agent's stamina
			if (stamina > 0) {
				this.AGENT.decStamina(stamina);
			}

			// obtains the depth of the broadcasted message
			int message_depth = action.getMessage_depth();

			// if the depth of the message is bigger than the
			// depth limitation, replace it by the depth limitation
			if (depth > -1 && (message_depth > depth || message_depth < 0)) {
				message_depth = depth;
			}

			// broadcasts the message
			this.action_message = action.getMessage();
			this.broadcastMessage(this.action_message, message_depth);
		}
	}
	/**
	 * Attends an intention of broadcasting a message.
	 * 
	 * @param action
	 *            The action of broadcasting a message.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendBroadcastSocietyAction(BroadcastSocietyAction action,
			Limitation[] limitations) {
		// holds an eventual depth limitation
		int depth = -1;

		// holds an eventual stamina limitation
		double stamina = 0;

		// for each limitation, tries to set the depth and stamina limitations
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof DepthLimitation) {
				depth = ((DepthLimitation) limitations[i]).getDepth();
			} else if (limitations[i] instanceof StaminaLimitation) {
				stamina = ((StaminaLimitation) limitations[i]).getCost();
				// developer: new limitations must add code here
			}
		}

		// if there's enough stamina to act
		if (this.AGENT.getStamina() > stamina) {
			// decrements the agent's stamina
			if (stamina > 0) {
				this.AGENT.decStamina(stamina);
			}

			// obtains the depth of the broadcasted message
			int message_depth = action.getMessage_depth();

			// if the depth of the message is bigger than the
			// depth limitation, replace it by the depth limitation
			if (depth > -1 && (message_depth > depth || message_depth < 0)) {
				message_depth = depth;
			}

			// broadcasts the message
			this.action_message = action.getMessage();
			this.broadcastSocietyMessage(this.action_message, message_depth);
		}
	}

	/**
	 * Broadcasts the given message. Its depth must be also informed.
	 * 
	 * @param message
	 *            The message to be broadcasted.
	 * @param message_depth
	 *            The depth of the message to be broadcasted.
	 */
	private void broadcastMessage(String message, int message_depth) {
		// holds the reachable agents
		List<Agent> reachable_agents = new LinkedList<Agent>();

		// obtains the visible subgraph
		// with the given message depth
		Graph subgraph = simulator.getEnvironment().getGraph()
				.getVisibleEnabledSubgraph(this.AGENT.getNode(),
						message_depth);

		// obtains the societies of the simulation
		Society[] societies = simulator.getEnvironment().getActiveSocieties();

		// for each society
		for (int i = 0; i < societies.length; i++) {
			
			// obtains its agents			
			Agent[] agents = societies[i].getAgents();			
			// for each agent
			for (int j = 0; j < agents.length; j++) {
				// if the current agent is not the one that's acting
				if (!this.AGENT.equals(agents[j])) {
					// obtains the node that the current agent comes from
					Node node = agents[j].getNode();

					// obtains the edge where the agent is
					Edge edge = agents[j].getEdge();

					// if the obtained node and edge are part of the
					// subgraph
					if (subgraph.hasNode(node)
							&& (edge == null || subgraph.hasEdge(edge))) {
						// adds the current agent to the reachable ones
						reachable_agents.add(agents[j]);
					}
				}
			}
		}

		// for each reachable agent, obtains its perception daemon
		// and sends the message
		for (Agent agent : reachable_agents)
			simulator.getPerceptionDaemon(agent).receiveMessage(message);
	}
	/**
	 * Broadcasts the given message. Its depth must be also informed.
	 * 
	 * @param message
	 *            The message to be broadcasted.
	 * @param message_depth
	 *            The depth of the message to be broadcasted.
	 */
	private void broadcastSocietyMessage(String message, int message_depth) {
		// holds the reachable agents
		List<Agent> reachable_agents = new LinkedList<Agent>();

		// obtains the visible subgraph
		// with the given message depth
		Graph subgraph = simulator.getEnvironment().getGraph()
				.getVisibleEnabledSubgraph(this.AGENT.getNode(),
						message_depth);

		// obtains the societies of the simulation
		Society[] societies = simulator.getEnvironment().getActiveSocieties();

		// for each society
		for (int i = 0; i < societies.length; i++) {
			
			//if the agent doesn't exists in this society skip
			if(! societies[i].hasAgent(this.AGENT) ) continue;
			
			// obtains its agents
			Agent[] agents = societies[i].getAgents();

			// for each agent
			for (int j = 0; j < agents.length; j++) {
				// if the current agent is not the one that's acting
				if (!this.AGENT.equals(agents[j])) {
					// obtains the node that the current agent comes from
					Node node = agents[j].getNode();

					// obtains the edge where the agent is
					Edge edge = agents[j].getEdge();

					// if the obtained node and edge are part of the
					// subgraph
					if (subgraph.hasNode(node)
							&& (edge == null || subgraph.hasEdge(edge))) {
						// adds the current agent to the reachable ones
						reachable_agents.add(agents[j]);
					}
				}
			}
		}

		// for each reachable agent, obtains its perception daemon
		// and sends the message
		for (Agent agent : reachable_agents)
			simulator.getPerceptionDaemon(agent).receiveMessage(message);
	}

	/**
	 * Attends an intention of teleport action.
	 * 
	 * @param action
	 *            The action of teleport intended by the agent.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendTeleportAction(TeleportAction action,
			Limitation[] limitations) {
		// obtains the node and the edge to where the agent shall be
		// teleported
		Node goal_node = action.getNode();
		Edge goal_edge = action.getEdge();
		double elapsed_length = action.getElapsed_length();

		// if the goal node is not valid, quits the method
		if (goal_node == null)
			return;

		// holds an eventual depth limitation
		int depth = -1;

		// holds an eventual stamina limitation
		double stamina = 0;

		// for each limitation, tries to set the depth and stamina limitations
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof DepthLimitation)
				depth = ((DepthLimitation) limitations[i]).getDepth();
			else if (limitations[i] instanceof StaminaLimitation)
				stamina = ((StaminaLimitation) limitations[i]).getCost();
			// developer: new limitations must add code here
		}

		// if there's enough stamina to act
		if (this.AGENT.getStamina() > stamina) {
			// decrements the agent's stamina
			if (stamina > 0)
				this.AGENT.decStamina(stamina);

			// obtains the visible subgraph with the given depth
			Graph subgraph = simulator.getEnvironment().getGraph()
					.getEnabledSubgraph(this.AGENT.getNode(), depth);

			// if the obtained subgraph contains the goal node
			if (subgraph.hasNode(goal_node))
				// if the goal edge is not valid or belongs to the obtained
				// subgraph
				if (goal_edge == null || subgraph.hasEdge(goal_edge)) {
					// teleports the agent
					this.AGENT.setNode(goal_node);
					this.AGENT.setEdge(goal_edge, elapsed_length);

					// assures that the goal node and eventual goal edge
					// are visible
					action.assureTeleportVisibilityEffect();
				}
		}
	}

	/**
	 * Attends an intention of recharging the stamina of the agent.
	 * 
	 * If the agent is not on a "fueled" node (i.e., its "fuel" attribute is
	 * not TRUE), then the action has no effect.
	 * 
	 * @param action
	 *            The action of recharge the agent's stamina.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendRechargeAction(RechargeAction action,
			Limitation[] limitations) {
		// holds an eventual stamina limitation
		double stamina = 0;

		// holds an eventual speed limitation
		double speed = -1;

		// for each limitation, tries to set the stamina and speed limitations
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof StaminaLimitation)
				stamina = ((StaminaLimitation) limitations[i]).getCost();
			else if (limitations[i] instanceof SpeedLimitation)
				speed = ((SpeedLimitation) limitations[i]).getSpeed();
			// developer: new limitations must add code here
		}

		// if the agent is on a "fueled" node and there's enough stamina to
		// act
		if (this.AGENT.getElapsed_length() == 0
				&& this.AGENT.getNode().isFuel()
				&& this.AGENT.getStamina() > stamina) {
			// sets the amount of stamina to be spent by the agent
			// as the planning is being executed
			if (this.stamina_robot != null)
				this.stamina_robot.setActions_spent_stamina(stamina);
			else if (coordinator != null)
				coordinator.setActionsSpentStamina(this.AGENT, stamina);

			// parses the recharge action and adds the result to the local
			// planning
			AtomicAction[] parsed_actions = null;
			if (simulator instanceof RealTimeSimulator)
				parsed_actions = CompoundActionsParser.parseRechargeAction(
						action, speed, simulator.getUpdate_time_rate());
			else
				parsed_actions = CompoundActionsParser.parseRechargeAction(
						action, speed, -1);

			for (int i = 0; i < parsed_actions.length; i++)
				this.PLANNING.insert(parsed_actions[i]);
		}
	}

	/**
	 * Attends an intention of goto action.
	 * 
	 * @param action
	 *            The action of movement intended by the agent.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendGoToAction(GoToAction action, Limitation[] limitations) {
		// obtains the node to where the agent shall go
		Node goal_node = action.getNode();

		// if the goal node is not valid, quits the method
		if (goal_node == null)
			return;

		// holds an eventual depth limitation
		int depth = -1;

		// holds an eventual stamina limitation
		double stamina = 0;

		// holds an eventual speed limitation
		double speed = -1;

		// holds an eventual acceleration limitation
		double acceleration = -1;

		// for each limitation, tries to set the depth, stamina,
		// speed and acceleration limitations
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof DepthLimitation)
				depth = ((DepthLimitation) limitations[i]).getDepth();
			else if (limitations[i] instanceof StaminaLimitation)
				stamina = ((StaminaLimitation) limitations[i]).getCost();
			else if (limitations[i] instanceof SpeedLimitation)
				speed = ((SpeedLimitation) limitations[i]).getSpeed();
			else if (limitations[i] instanceof AccelerationLimitation)
				acceleration = ((AccelerationLimitation) limitations[i])
						.getAcceleration();
			// developer: new limitations must add code here
		}

		// if there's enough stamina to act
		if (this.AGENT.getStamina() > stamina) {
			// obtains the path that the agent shall take during the movement
			Graph path = simulator
					.getEnvironment()
					.getGraph()
					.getEnabledDijkstraPath(this.AGENT.getNode(), goal_node);

			// if the path is valid
			if (path != null) {
				// sets the amount of stamina to be spent by the agent
				// as the planning is being executed
				if (this.stamina_robot != null)
					this.stamina_robot.setActions_spent_stamina(stamina);
				else if (coordinator != null)
					coordinator.setActionsSpentStamina(this.AGENT, stamina);

				// parses the goto action and adds the result to the local
				// planning
				AtomicAction[] parsed_actions = null;
				if (simulator instanceof RealTimeSimulator)
					parsed_actions = CompoundActionsParser.parseGoToAction(
							action, this.AGENT, path, depth, speed,
							acceleration, simulator.getUpdate_time_rate());
				else
					parsed_actions = CompoundActionsParser.parseGoToAction(
							action, this.AGENT, path, depth, speed,
							acceleration, 1);

				for (int i = 0; i < parsed_actions.length; i++)
					this.PLANNING.insert(parsed_actions[i]);
			}
		}
	}

	/**
	 * Attends the given atomic recharge action, since it was obtained from the
	 * planning queue of atomic actions.
	 * 
	 * @param action
	 *            The atomic action of recharge planned to be executed.
	 */
	private void attendPlannedAtomicRechargeAction(AtomicRechargeAction action) {
		// if there isn't enough stamina to act, or the agent is not on a
		// "fueled"
		// node, clears the planning and quits the method
		double remained_stamina = 0;

		if (this.stamina_robot != null)
			remained_stamina = this.stamina_robot.getActions_spent_stamina();
		else if (coordinator != null)
			remained_stamina = coordinator.getActionsSpentStamina(this.AGENT);

		if (this.AGENT.getStamina() < remained_stamina
				|| this.AGENT.getElapsed_length() > 0
				|| !this.AGENT.getNode().isFuel()) {
			this.PLANNING.clear();
			if (this.stamina_robot != null)
				this.stamina_robot.setActions_spent_stamina(0);
			else if (coordinator != null)
				coordinator.setActionsSpentStamina(this.AGENT, 0);

			return;
		}

		// obtains the stamina value to add to the agent
		double stamina = action.getStamina();

		// increments the agent's stamina by the obtained value
		this.AGENT.incStamina(stamina);
	}

	/**
	 * Attends the given teleport action, since it was obtained from the
	 * planning queue of atomic actions.
	 * 
	 * @param action
	 *            The action of teleport planned to be executed.
	 */
	private void attendPlannedTeleportAction(TeleportAction action) {
		System.err.println("Agent " + this.AGENT.getObjectId()
				+ " going as planned.");

		// if there isn't enough stamina to act, clears the planning
		// and quits the method
		double remained_stamina = 0;

		if (this.stamina_robot != null)
			remained_stamina = this.stamina_robot.getActions_spent_stamina();
		else if (coordinator != null)
			remained_stamina = coordinator.getActionsSpentStamina(this.AGENT);

		if (this.AGENT.getStamina() < remained_stamina) {
			this.PLANNING.clear();
			if (this.stamina_robot != null)
				this.stamina_robot.setActions_spent_stamina(0);
			else if (coordinator != null)
				coordinator.setActionsSpentStamina(this.AGENT, 0);

			return;
		}

		// obtains the node and the edge to where the agent shall be
		// teleported
		Node goal_node = action.getNode();
		Edge goal_edge = action.getEdge();
		double elapsed_length = action.getElapsed_length();

		// if the goal_node is not enabled, clears the
		// planning and quits the method
		if (goal_node instanceof DynamicNode
				&& !((DynamicNode) goal_node).isEnabled()) {
			this.PLANNING.clear();
			if (this.stamina_robot != null)
				this.stamina_robot.setActions_spent_stamina(0);
			else if (coordinator != null)
				coordinator.setActionsSpentStamina(this.AGENT, 0);

			return;
		}

		// if the eventual goal_edge is not enabled, clears
		// the planning and quits the method
		if (goal_edge != null && !goal_edge.isEnabled()) {
			this.PLANNING.clear();
			if (this.stamina_robot != null)
				this.stamina_robot.setActions_spent_stamina(0);
			else if (coordinator != null)
				coordinator.setActionsSpentStamina(this.AGENT, 0);

			return;
		}

		// teleports the agent
		this.AGENT.setNode(goal_node);
		this.AGENT.setEdge(goal_edge, elapsed_length);

		// assures that the goal node and eventual goal edge,
		// as well as other eventual objects, are all visible
		action.assureTeleportVisibilityEffect();
	}
	
	
	/**
	 * Attends the given DeActivate action : removes the agent from its current society and
	 * transfers it in the Unactive Society
	 * 
	 * @param action
	 *            The action of Deactivation
	 */
	private void attendDeActivateAction(DeActivateAction action){
		((SeasonalAgent)(this.AGENT)).deactivate();
		((SeasonalAgent)(this.AGENT)).setSociety(simulator.getEnvironment().getInactiveSociety());
		((SeasonalAgent)(this.AGENT)).activate();
		
		/*
		simulator.stopAndRemoveAgentDaemons((Agent) this.AGENT);

		if(simulator instanceof RealTimeSimulator){
			// stops and removes its eventual stamina controller robot
			((RealTimeSimulator)simulator).stopAndRemoveStaminaControllerRobot((Agent)(this.AGENT));
			
			((RealTimeSimulator)simulator).stopAndRemoveMortalityControllerRobot((SeasonalAgent)(this.AGENT));
		}
		else {
			((CycledSimulator)simulator).removeAgentSpentStaminas((Agent)(this.AGENT));
		}
		*/
	}
	
	/**
	 * Attends the given Activate action : removes the agent from the Unactive Society and
	 * transfers it in the given society if it is an open society
	 * 
	 * @param action
	 *            The action of activation
	 */
	private boolean attendActivateAction(ActivateAction action){
		OpenSociety society = null;
		for(Society soc : simulator.getEnvironment().getActiveSocieties())
			if((soc.getObjectId().equals(action.getSocietyId())) && (soc instanceof OpenSociety)){
				society = (OpenSociety) soc;
				break;
			}
		
		if(society != null && ((SeasonalAgent)this.AGENT).isInactive()) {
			((SeasonalAgent)(this.AGENT)).deactivate();
			((SeasonalAgent)(this.AGENT)).setSociety(society);
			((SeasonalAgent)(this.AGENT)).activate();
			
			return true;
		}

		return false;
		
		/*
		simulator.stopAndRemoveAgentDaemons((Agent) this.AGENT);

		if(simulator instanceof RealTimeSimulator){
			// stops and removes its eventual stamina controller robot
			((RealTimeSimulator)simulator).stopAndRemoveStaminaControllerRobot((Agent)(this.AGENT));
			
			((RealTimeSimulator)simulator).stopAndRemoveMortalityControllerRobot((SeasonalAgent)(this.AGENT));
		}
		else {
			((CycledSimulator)simulator).removeAgentSpentStaminas((Agent)(this.AGENT));
		}
		*/
	}
	
	/**
	 * Attends the given ChangeSociety action : removes the agent from its current society and
	 * transfers it in the given society if it is an open society
	 * 
	 * @param action
	 *            The action of activation
	 */
	private boolean attendChangeSocietyAction(ChangeSocietyAction action){
		OpenSociety society = null;
		for(Society soc : simulator.getEnvironment().getActiveSocieties())
			if((soc.getObjectId().equals(action.getSocietyId())) && (soc instanceof OpenSociety)){
				society = (OpenSociety) soc;
				break;
			}
		
		if(society != null) {
			((SeasonalAgent)(this.AGENT)).deactivate();
			((SeasonalAgent)(this.AGENT)).setSociety(society);
			((SeasonalAgent)(this.AGENT)).activate();
			
			return true;
		}

		return false;
	}

	
	
	/**
	 * Attends an intention of sending a message.
	 * 
	 * @param action
	 *            The action of sending a message.
	 * @param limitations
	 *            The limitations imposed to the action.
	 * @developer New Limitation classes can change this method.
	 */
	private void attendSendMessageAction(SendMessageAction action,
			Limitation[] limitations) {
		// holds an eventual depth limitation
		int depth = -1;

		// holds an eventual stamina limitation
		double stamina = 0;

		// for each limitation, tries to set the depth and stamina limitations
		for (int i = 0; i < limitations.length; i++) {
			if (limitations[i] instanceof DepthLimitation) {
				depth = ((DepthLimitation) limitations[i]).getDepth();
			} else if (limitations[i] instanceof StaminaLimitation) {
				stamina = ((StaminaLimitation) limitations[i]).getCost();
				// developer: new limitations must add code here
			}
		}

		// if there's enough stamina to act
		if (this.AGENT.getStamina() > stamina) {
			// decrements the agent's stamina
			if (stamina > 0) {
				this.AGENT.decStamina(stamina);
			}

			// obtains the depth of the broadcasted message
			int message_depth = action.getMessage_depth();

			// if the depth of the message is bigger than the
			// depth limitation, replace it by the depth limitation
			if (depth > -1 && (message_depth > depth || message_depth < 0)) {
				message_depth = depth;
			}

			// sends the message
			this.action_message = action.getMessage();
			this.SendMessage(action.getTargetAgent(), this.action_message, message_depth);
		}
	}
	
	
	/**
	 * Sends the given message to the given target agent, if it is in the same society as this.AGENT. 
	 * Its depth must be also informed.
	 * 
	 * @param target_agent
	 * 			  The agent that must get the message
	 * @param message
	 *            The message to be sent.
	 * @param message_depth
	 *            The depth of the message to be sent.
	 */
	private void SendMessage(String target_agent, String message, int message_depth) {
		// obtains the visible subgraph
		// with the given message depth
		Graph subgraph = simulator.getEnvironment().getGraph()
				.getVisibleEnabledSubgraph(this.AGENT.getNode(),
						message_depth);

		// obtains the societies of the simulation
		Society[] societies = simulator.getEnvironment().getActiveSocieties();

		// for each society
		for (int i = 0; i < societies.length; i++) {
			
			//if the agent doesn't exists in this society skip
			if(! societies[i].hasAgent(this.AGENT) ) continue;
			
			// obtains its agents
			Agent[] agents = societies[i].getAgents();

			// for each agent
			for (int j = 0; j < agents.length; j++) {
				// if the current agent is not the one that's acting
				if (target_agent.equals(agents[j].getObjectId())) {
					// obtains the node that the current agent comes from
					Node node = agents[j].getNode();

					// obtains the edge where the agent is
					Edge edge = agents[j].getEdge();

					// if the obtained node and edge are part of the
					// subgraph
					if (subgraph.hasNode(node)
							&& (edge == null || subgraph.hasEdge(edge))) {
						// sends the message to the reachable agent
						simulator.getPerceptionDaemon(agents[j]).receiveMessage(message);
					}
					
					// if we went through that part, the target agent has been reached, we can stop
					return;
				}
			}
		}
	}
	
	
	
	
	
	
	/**
	 * 
	 * The Activate Agent is open to all seasonal agents, even inactive ones
	 * All other actions need for the agent to be perpetual or active.
	 * 
	 * @developer New action must change this method.
	 * @modeler This method must be modeled.
	 */
	public void run() {
		// waits until the simulator is in the SIMULATING state
		while (simulator.getState() == SimulatorStates.CONFIGURING)			
				Thread.yield();
		//while (simulator.getState() == SimulatorStates.CONFIGURING)
			/*try {
				Thread.sleep(1);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}*/

		// while the deamon is supposed to work
		while (this.is_active)
			synchronized (simulator) {
				if (simulator.getState() == SimulatorStates.SIMULATING
						&& !this.is_blocked 
						&& !(this.AGENT.getAgentState() == AgentStates.JUST_ACTED)) {
					// registers if some action was attended
					boolean attended_actions = false;
					if( this.BUFFER.getSize() == 0 ){
						synchronized (this) {
							try {
								this.wait(20);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else
					// while the buffer has messages to be attended					
					while (this.BUFFER.getSize() > 0
							&& !(this.AGENT.getAgentState() == AgentStates.JUST_ACTED)) {
						
						if( cyclesVisited < numCyclesByVisit && cyclesVisited !=0 )	{
							cyclesVisited++;
							this.AGENT.setState(AgentStates.JUST_ACTED);
							continue;
						}
						else if( cyclesVisited !=0 ) cyclesVisited = 0;
						
						// destroys the current planning of actions
						this.PLANNING.clear();
						if (this.stamina_robot != null)
							this.stamina_robot.setActions_spent_stamina(0);
						else if (coordinator != null)
							coordinator.setActionsSpentStamina(this.AGENT, 0);

						// obtains the elder message
						String message = this.BUFFER.remove();

						// the action to be obtained from the message
						Action action = null;

						// tries to obtain the action with the ActionTranslator
						try {
							action = ActionTranslator.getAction(message);
						} catch (SAXException e1) {
							System.err.println(message);
							e1.printStackTrace(); // traced XML error
						} catch (IOException e1) {
							e1.printStackTrace(); // traced IO error
						}

						// if the action is still null, tries to obtain it as a
						// teleport action
						if (action == null) {
							try {
								action = ActionTranslator.getTeleportAction(
										message, simulator.getEnvironment()
												.getGraph());
							} catch (SAXException e) {
								e.printStackTrace(); // traced XML error
							} catch (IOException e) {
								e.printStackTrace(); // traced IO error
							} catch (NodeNotFoundException e) {
								e.printStackTrace(); // Node XML error
							} catch (EdgeNotFoundException e) {
								e.printStackTrace(); // traced Edge XML error
							}
						}

						// if the action is still null, tries to obtain it as a
						// goto action
						if (action == null) {
							try {
								action = ActionTranslator.getGoToAction(
										message, simulator.getEnvironment()
												.getGraph());
							} catch (SAXException e) {
								e.printStackTrace(); // traced XML error
							} catch (IOException e) {
								e.printStackTrace(); // traced IO error
							} catch (NodeNotFoundException e) {
								e.printStackTrace(); // Node XML error
							}
						}
						
						// this action is open to any agent, even inactive
						if (action instanceof ActivateAction){
							if((this.AGENT instanceof SeasonalAgent) && attendActivateAction((ActivateAction) action)){
								System.err.println("Agent "+ this.AGENT.getObjectId() + " activating in society " 
										+ ((ActivateAction) action).getSocietyId());
		
								attended_actions = true;
							}
						}
						
						// other actions are only for active agents
						if((this.AGENT instanceof PerpetualAgent) || !((SeasonalAgent)this.AGENT).isInactive()){
							
							// if the obtained action is a waiting one
							if (action instanceof WaitAction) {
								System.err.println("Agent "
										+ this.AGENT.getObjectId() + " waiting");
								// attends the action
								attendWaitAction((WaitAction) action);
		
								attended_actions = true;
								// quits the loop
							}
	
							// if the obtained action is a visiting one
							else if (action instanceof VisitAction) {
	
								// verifies if the agent has permission to visit
								// nodes
								ActionPermission[] permissions = this.AGENT
										.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.VISIT) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId() + " visiting");
										// attends the action
										this.attendVisitAction(
												(VisitAction) action,
												permissions[i].getLimitations());
	
										attended_actions = true;
										// quits the loop
										break;
									}
							}
							// else if the obtained action is a broadcasting one
							else if (action instanceof BroadcastAction) {
	
								// verifies if the agent has permission to broadcast
								// messages
								ActionPermission[] permissions = this.AGENT
										.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.BROADCAST) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId()
												+ " broadcasting");
										// attends the action
										this.attendBroadcastAction(
												(BroadcastAction) action,
												permissions[i].getLimitations());
	
										// quits the loop
										break;
									}
							}
							// else if the obtained action is a broadcasting society one
							else if (action instanceof BroadcastSocietyAction) {
	
								// verifies if the agent has permission to broadcast
								// messages
								ActionPermission[] permissions = this.AGENT
										.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.BROADCAST_SOCIETY) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId()
												+ " broadcasting to your society");
										// attends the action
										this.attendBroadcastSocietyAction(
												(BroadcastSocietyAction) action,
												permissions[i].getLimitations());
	
										// quits the loop
										break;
									}
							}
							// else if the obtained action is a stigmatize one
							else if (action instanceof StigmatizeAction) {
	
								// verifies if the agent has permission to deposit
								// stigmas
								ActionPermission[] permissions = this.AGENT
										.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.STIGMATIZE) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId()
												+ " stigmatizing");
										// attends the action
										this.attendStigmatizeAction(
												(StigmatizeAction) action,
												permissions[i].getLimitations());
	
										attended_actions = true;
										// quits the loop
										break;
									}
							}
							// else if the obtained action is an atomic recharge one
							else if (action instanceof AtomicRechargeAction) {
	
								// verifies if the agent has permission to
								// immediately recharge
								ActionPermission[] permissions = this.AGENT
										.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.ATOMIC_RECHARGE) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId()
												+ " atomic recharging");
										// attends the action
										this.attendAtomicRechargeAction(
												(AtomicRechargeAction) action,
												permissions[i].getLimitations());
	
										attended_actions = true;
										// quits the loop
										break;
									}
							}
							// else if the obtained action is a recharge one
							else if (action instanceof RechargeAction) {
	
								// verifies if the agent has permission to recharge
								ActionPermission[] permissions = this.AGENT
										.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.RECHARGE) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId() + " recharging");
										// attends the action
										this.attendRechargeAction(
												(RechargeAction) action,
												permissions[i].getLimitations());
	
										// if the simulator is a cycled one,
										// calls this.act(1)
										if (simulator instanceof CycledSimulator)
											this.act();
	
										attended_actions = true;
										// quits the loop
										break;
									}
							}
							// else if the action is a teleport action
							else if (action instanceof TeleportAction) {
	
								// verifies if the agent has permission to teleport
								ActionPermission[] permissions = this.AGENT
										.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.TELEPORT) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId()
												+ " teleporting");
										// attends the intention of action
										this.attendTeleportAction(
												(TeleportAction) action,
												permissions[i].getLimitations());
	
										attended_actions = true;
										// quits the loop
										break;
									}
							}
							// else if the action is a goto action
							else if (action instanceof GoToAction) {
	
								// verifies if the agent has permission to move
								ActionPermission[] permissions = this.AGENT
										.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.GOTO) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId() + " going");
										// attends the intention of action
										this.attendGoToAction((GoToAction) action,
												permissions[i].getLimitations());
	
										// if the simulator is a cycled one,
										// calls this.act(1)
										if (simulator instanceof CycledSimulator)
											this.act();
	
										attended_actions = true;
										// quits the loop
										break;
									}
	
							}
							// else if the obtained action is a sendMessage one
							else if (action instanceof SendMessageAction) {
	
								// verifies if the agent has permission to send
								// messages
								ActionPermission[] permissions = this.AGENT.getAllowedActions();
	
								for (int i = 0; i < permissions.length; i++)
									if (permissions[i].getAction_type() == ActionTypes.SEND_MESSAGE) {
										System.err.println("Agent "
												+ this.AGENT.getObjectId()
												+ " sends a message to " + ((SendMessageAction) action).getTargetAgent());
										// attends the action
										((SendMessageAction) action).setSenderAgent(this.AGENT.getObjectId());
										this.attendSendMessageAction((SendMessageAction) action, permissions[i].getLimitations());
	
										// quits the loop
										break;
									}
							}
							
							else if (action instanceof DeActivateAction){
								if(this.AGENT instanceof SeasonalAgent){
									System.err.println("Agent "+ this.AGENT.getObjectId() + " deactivating.");
									// attends the action
									attendDeActivateAction((DeActivateAction) action);
			
									attended_actions = true;
								}
							}
						
							
							else if (action instanceof ChangeSocietyAction){
								if(this.AGENT instanceof SeasonalAgent && attendChangeSocietyAction((ChangeSocietyAction) action)){
									System.err.println("Agent "+ this.AGENT.getObjectId() + " changing society to "
											+ ((ChangeSocietyAction) action).getSocietyId() + ".");
			
									attended_actions = true;
								}
							}
							
							// developer: new action types must add code here
	
							// registers that the agent just acted,
							// if some action was attended
							if (attended_actions)
								this.AGENT.setState(AgentStates.JUST_ACTED);
							
						}
					}
					
					if((this.AGENT instanceof SeasonalAgent) && ((SeasonalAgent)this.AGENT).isInactive())
						this.AGENT.setState(AgentStates.JUST_ACTED);
					
					
				}
				/*try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			
	}

	/**
	 * @developer New CompoundAction classes must change this method.
	 * @modeler This method must be modeled.
	 */
	public void act() {
		synchronized (simulator) {
			if ((simulator.getState() == SimulatorStates.SIMULATING)
					&& !(this.AGENT.getAgentState() == AgentStates.JUST_ACTED)){				
				// if the planning is not empty
				if (this.PLANNING.getSize() > 0) {
					// executes the current atomic action
					AtomicAction action = null;
					if (this.is_active)
						action = this.PLANNING.remove();

					// if the atomic action is a teleport one
					if (action instanceof TeleportAction)
						// attends it, as a planned teleport action
						this
								.attendPlannedTeleportAction((TeleportAction) action);

					// else if the atomic action is an atomic recharge
					// action
					else if (action instanceof AtomicRechargeAction)
						// attends it, as a planned atomic recharge action
						this
								.attendPlannedAtomicRechargeAction((AtomicRechargeAction) action);

					// new AtomicAction classes must add code here

					// registers that the agent just acted
					if (this.is_active)
						this.AGENT.setState(AgentStates.JUST_ACTED);
				}
				// else, sets the stamina to be spent as 0
				else if (this.stamina_robot != null)
					this.stamina_robot.setActions_spent_stamina(0);
				else if (coordinator != null)
					coordinator.setActionsSpentStamina(this.AGENT, 0);
			}
		}
	}

	public void start(int local_socket_number) throws IOException {
		super.start(local_socket_number);
	}

	public void stopActing() {
		// used by AspectJ
		super.stopActing();
	}
	
	public boolean isFreezeCycleCount() {
		return freezeCycleCount;
	}

	
	public void update() {
			
		if (this.is_active)
			synchronized (simulator) {
				if (simulator.getState() == SimulatorStates.SIMULATING
						&& !this.is_blocked
						&& !(this.AGENT.getAgentState() == AgentStates.JUST_ACTED)) {
					// registers if some action was attended
					boolean attended_actions = false;
					
					// while the buffer has messages to be attended
					while (this.BUFFER.getSize() > 0 
							&& !(this.AGENT.getAgentState() == AgentStates.JUST_ACTED)) {
						
						
						if( cyclesVisited < numCyclesByVisit && cyclesVisited !=0 ){
							cyclesVisited++;
							continue;
						}
						else if( cyclesVisited !=0 ) cyclesVisited = 0;
						// destroys the current planning of actions
						this.PLANNING.clear();
						if (this.stamina_robot != null)
							this.stamina_robot.setActions_spent_stamina(0);
						else if (coordinator != null)
							coordinator.setActionsSpentStamina(this.AGENT, 0);

						// obtains the elder message
						String message = this.BUFFER.remove();

						// the action to be obtained from the message
						Action action = null;

						// tries to obtain the action with the ActionTranslator
						try {
							action = ActionTranslator.getAction(message);
						} catch (SAXException e1) {
							e1.printStackTrace(); // traced XML error
						} catch (IOException e1) {
							e1.printStackTrace(); // traced IO error
						}

						// if the action is still null, tries to obtain it as a
						// teleport action
						if (action == null) {
							try {
								action = ActionTranslator.getTeleportAction(
										message, simulator.getEnvironment()
												.getGraph());
							} catch (SAXException e) {
								e.printStackTrace(); // traced XML error
							} catch (IOException e) {
								e.printStackTrace(); // traced IO error
							} catch (NodeNotFoundException e) {
								e.printStackTrace(); // Node XML error
							} catch (EdgeNotFoundException e) {
								e.printStackTrace(); // traced Edge XML error
							}
						}

						// if the action is still null, tries to obtain it as a
						// goto action
						if (action == null) {
							try {
								action = ActionTranslator.getGoToAction(
										message, simulator.getEnvironment()
												.getGraph());
							} catch (SAXException e) {
								e.printStackTrace(); // traced XML error
							} catch (IOException e) {
								e.printStackTrace(); // traced IO error
							} catch (NodeNotFoundException e) {
								e.printStackTrace(); // Node XML error
							}
						}

						// if the obtained action is a visiting one
						if (action instanceof VisitAction) {

							// verifies if the agent has permission to visit
							// nodes
							ActionPermission[] permissions = this.AGENT
									.getAllowedActions();

							for (int i = 0; i < permissions.length; i++)
								if (permissions[i].getAction_type() == ActionTypes.VISIT) {
									System.err.println("Agent "
											+ this.AGENT.getObjectId() + " visiting");
									// attends the action
									this.attendVisitAction(
											(VisitAction) action,
											permissions[i].getLimitations());

									attended_actions = true;
									// quits the loop
									break;
								}
						}
						// else if the obtained action is a broadcasting one
						else if (action instanceof BroadcastAction) {

							// verifies if the agent has permission to broadcast
							// messages
							ActionPermission[] permissions = this.AGENT
									.getAllowedActions();

							for (int i = 0; i < permissions.length; i++)
								if (permissions[i].getAction_type() == ActionTypes.BROADCAST) {
									System.err.println("Agent "
											+ this.AGENT.getObjectId()
											+ " broadcasting");
									// attends the action
									this.attendBroadcastAction(
											(BroadcastAction) action,
											permissions[i].getLimitations());

									// quits the loop
									break;
								}
						}
						// else if the obtained action is a broadcasting society one
						else if (action instanceof BroadcastSocietyAction) {

							// verifies if the agent has permission to broadcast
							// messages
							ActionPermission[] permissions = this.AGENT
									.getAllowedActions();

							for (int i = 0; i < permissions.length; i++)
								if (permissions[i].getAction_type() == ActionTypes.BROADCAST_SOCIETY) {
									System.err.println("Agent "
											+ this.AGENT.getObjectId()
											+ " broadcasting to your society");
									// attends the action
									this.attendBroadcastSocietyAction(
											(BroadcastSocietyAction) action,
											permissions[i].getLimitations());

									// quits the loop
									break;
								}
						}
						// else if the obtained action is a stigmatize one
						else if (action instanceof StigmatizeAction) {

							// verifies if the agent has permission to deposit
							// stigmas
							ActionPermission[] permissions = this.AGENT
									.getAllowedActions();

							for (int i = 0; i < permissions.length; i++)
								if (permissions[i].getAction_type() == ActionTypes.STIGMATIZE) {
									System.err.println("Agent "
											+ this.AGENT.getObjectId()
											+ " stigmatizing");
									// attends the action
									this.attendStigmatizeAction(
											(StigmatizeAction) action,
											permissions[i].getLimitations());

									attended_actions = true;
									// quits the loop
									break;
								}
						}
						// else if the obtained action is an atomic recharge one
						else if (action instanceof AtomicRechargeAction) {

							// verifies if the agent has permission to
							// immediately recharge
							ActionPermission[] permissions = this.AGENT
									.getAllowedActions();

							for (int i = 0; i < permissions.length; i++)
								if (permissions[i].getAction_type() == ActionTypes.ATOMIC_RECHARGE) {
									System.err.println("Agent "
											+ this.AGENT.getObjectId()
											+ " atomic recharging");
									// attends the action
									this.attendAtomicRechargeAction(
											(AtomicRechargeAction) action,
											permissions[i].getLimitations());

									attended_actions = true;
									// quits the loop
									break;
								}
						}
						// else if the obtained action is a recharge one
						else if (action instanceof RechargeAction) {

							// verifies if the agent has permission to recharge
							ActionPermission[] permissions = this.AGENT
									.getAllowedActions();

							for (int i = 0; i < permissions.length; i++)
								if (permissions[i].getAction_type() == ActionTypes.RECHARGE) {
									System.err.println("Agent "
											+ this.AGENT.getObjectId() + " recharging");
									// attends the action
									this.attendRechargeAction(
											(RechargeAction) action,
											permissions[i].getLimitations());

									// if the simulator is a cycled one,
									// calls this.act(1)
									if (simulator instanceof CycledSimulator)
										this.act();

									attended_actions = true;
									// quits the loop
									break;
								}
						}
						// else if the action is a teleport action
						else if (action instanceof TeleportAction) {

							// verifies if the agent has permission to teleport
							ActionPermission[] permissions = this.AGENT
									.getAllowedActions();

							for (int i = 0; i < permissions.length; i++)
								if (permissions[i].getAction_type() == ActionTypes.TELEPORT) {
									System.err.println("Agent "
											+ this.AGENT.getObjectId()
											+ " teleporting");
									// attends the intention of action
									this.attendTeleportAction(
											(TeleportAction) action,
											permissions[i].getLimitations());

									attended_actions = true;
									// quits the loop
									break;
								}
						}
						// else if the action is a goto action
						else if (action instanceof GoToAction) {

							// verifies if the agent has permission to move
							ActionPermission[] permissions = this.AGENT
									.getAllowedActions();

							for (int i = 0; i < permissions.length; i++)
								if (permissions[i].getAction_type() == ActionTypes.GOTO) {
									System.err.println("Agent "
											+ this.AGENT.getObjectId() + " going");
									// attends the intention of action
									this.attendGoToAction((GoToAction) action,
											permissions[i].getLimitations());

									// if the simulator is a cycled one,
									// calls this.act(1)
									if (simulator instanceof CycledSimulator)
										this.act();

									attended_actions = true;
									// quits the loop
									break;
								}
						}
						// developer: new action types must add code here

						// registers that the intentions of actions were
						// attended
						// registers that the agent just acted,
						// if some action was attended
						if (attended_actions)
							this.AGENT.setState(AgentStates.JUST_ACTED);
					}
				}
				
			}		
	}
}