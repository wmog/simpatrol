package control.daemon;

import logger.event.AgentRechargingEvent;
import logger.event.AgentTeleportingEvent;
import logger.event.AgentVisitEvent;
import logger.event.AgentStigmatizingEvent;
import logger.event.AgentBroadcastingEvent;
import model.graph.Graph;
import model.graph.Vertex;
import model.stigma.Stigma;
import model.agent.Agent;
import model.agent.Society;
import model.action.BroadcastAction;
import model.action.TeleportAction;

/**
 * This aspect is responsible for logging data of all daemon classes
 */
public aspect Logger {

	/**
	 * Logs a visit of an agent
	 */
	pointcut setLastVisitTime(ActionDaemon daemon) : call(* Vertex.setLast_visit_time(..)) && this(daemon);

	after(ActionDaemon daemon) : setLastVisitTime(daemon) {
		AgentVisitEvent event = new AgentVisitEvent(daemon.AGENT.getObjectId());
		// TODO completar enviando event por porta

		logger.Logger.getInstance().log(
				"[SimPatrol.Event]: Agent " + daemon.AGENT.getObjectId()
						+ " visited vertex "
						+ daemon.AGENT.getVertex().reducedToXML(0));
	}

	/**
	 * Logs the recharging of an agent
	 */
	pointcut incStamina(ActionDaemon daemon) : call(* Agent.incStamina(..)) && this(daemon);

	after(ActionDaemon daemon) : incStamina(daemon) {
		AgentRechargingEvent event = new AgentRechargingEvent(daemon.AGENT
				.getObjectId(), daemon.AGENT.getStamina());		
		// TODO completar enviando event por porta
		
		logger.Logger.getInstance().log(
				"[SimPatrol.Event]: Agent " + daemon.AGENT.getObjectId()
						+ " recharged.");
	}

	/**
	 * Logs the creation of stigmas in ActionDaemon.attendStigmatizeAction()
	 */
	pointcut attendStigmatizeAction(ActionDaemon daemon, Stigma stigma) : 
		call(* Graph.addStigma(..)) &&
		this(daemon) && 
		withincode(* ActionDaemon.attendStigmatizeAction(..)) &&
		args(stigma);

	after(ActionDaemon daemon, Stigma stigma) : attendStigmatizeAction(daemon, stigma) {
		AgentStigmatizingEvent event = new AgentStigmatizingEvent(daemon.AGENT
				.getObjectId(), stigma);
		// TODO enviar event pela porta

		String result = "";
		if (stigma.getEdge() != null) {
			result = "edge " + daemon.AGENT.getEdge().reducedToXML(0);
		} else if (stigma.getVertex() != null) {
			result = "vertex " + daemon.AGENT.getVertex().reducedToXML(0);
		}
		logger.Logger.getInstance().log(
				"[SimPatrol.Event]: Agent " + daemon.AGENT.getObjectId()
						+ " stigmatized " + result);
	}

	/**
	 * Logs the message broadcasting
	 */
	// pointcut broadcastMessage(ActionDaemon daemon, BroadcastAction action) :
	pointcut broadcastMessage(BroadcastAction action) :
		execution(* ActionDaemon.attendBroadcastAction(..)) 
// && this(daemon) && args(action);
		&& args(action);

	// after(ActionDaemon daemon, BroadcastAction action) :
	// broadcastMessage(daemon, action) {
	after(BroadcastAction action) : broadcastMessage(action) {
		// AgentBroadcastingEvent event = new
		// AgentBroadcastingEvent(daemon.AGENT
		// .getObjectId(), action.getMessage());
		// TODO enviar event por porta
		// logger.Logger.getInstance().log(
		// "[SimPatrol.Event]: Agent " + daemon.AGENT.getObjectId()
		// + " broadcasted a message.");
	}

	/**
	 * Logs the first teleport action
	 */
	pointcut teleportAction1(ActionDaemon daemon) : 
		call(* TeleportAction.assureTeleportVisibilityEffect()) 
		&& withincode(* ActionDaemon.attendTeleportAction(..)) && this(daemon);

	after(ActionDaemon daemon) : teleportAction1(daemon) {
		Agent agent = daemon.AGENT;
		AgentTeleportingEvent event = new AgentTeleportingEvent(agent
				.getObjectId(), agent.getVertex().getObjectId(), agent
				.getEdge().getObjectId(), agent.getElapsed_length());
		// TODO enviar event por porta
		
		logger.Logger.getInstance().log(
				"[SimPatrol.Event]: Agent " + daemon.AGENT.getObjectId()
						+ " teleported.");
	}

	/**
	 * Logs the second teleport action
	 */
	pointcut teleportAction2(ActionDaemon daemon) : 
		call(* TeleportAction.assureTeleportVisibilityEffect()) 
		&& withincode(* ActionDaemon.attendPlannedTeleportAction(..)) && this(daemon);

	after(ActionDaemon daemon) : teleportAction2(daemon) {
		Agent agent = daemon.AGENT;
		AgentTeleportingEvent event = new AgentTeleportingEvent(agent
				.getObjectId(), agent.getVertex().getObjectId(), agent
				.getEdge().getObjectId(), agent.getElapsed_length());
		// TODO enviar event por porta
		
		logger.Logger.getInstance().log(
				"[SimPatrol.Event]: Agent " + daemon.AGENT.getObjectId()
						+ " teleported to "
						+ daemon.AGENT.getVertex().getObjectId()
						+ ", elapsed length "
						+ daemon.AGENT.getElapsed_length());
	}

	/**
	 * ActionDaemon starts working
	 */
	pointcut startActionDaemon(ActionDaemon daemon) : execution(* ActionDaemon.start(..)) && this(daemon);

	after(ActionDaemon daemon) : startActionDaemon(daemon) {
		logger.Logger.getInstance().log(
				"[SimPatrol.ActionDaemon(" + daemon.AGENT.getObjectId()
						+ ")]: Started working.");
	}

	/**
	 * ActionDaemon stop working
	 */
	pointcut stopActionDaemon(ActionDaemon daemon) : execution(* ActionDaemon.stopWorking(..)) && this(daemon);

	after(ActionDaemon daemon) : stopActionDaemon(daemon) {
		logger.Logger.getInstance().log(
				"[SimPatrol.ActionDaemon(" + daemon.AGENT.getObjectId()
						+ ")]: Stopped working.");
	}

	/**
	 * Main daemon is attending an environment creation request
	 */
	pointcut attendEnvironmentCreationConfiguration(): call(* MainDaemon.attendEnvironmentCreationConfiguration(..));

	before() : attendEnvironmentCreationConfiguration() {
		logger.Logger
				.getInstance()
				.log(
						"[SimPatrol.MainDaemon]: \"Environment's creation\" configuration received.");
	}

	/**
	 * Main daemon is attending an agent creation request
	 */
	pointcut attendAgentCreationConfiguration(): call(* MainDaemon.attendAgentCreationConfiguration(..));

	before() : attendAgentCreationConfiguration() {
		logger.Logger
				.getInstance()
				.log(
						"[SimPatrol.MainDaemon]: \"Agent's creation\" configuration received.");
	}

	/**
	 * Main daemon is attending an agent death request
	 */
	pointcut attendAgentDeathConfiguration(): call(* MainDaemon.attendAgentDeathConfiguration(..));

	before() : attendAgentDeathConfiguration() {
		logger.Logger
				.getInstance()
				.log(
						"[SimPatrol.MainDaemon]: \"Agent's death\" configuration received.");
	}

	/**
	 * Main daemon is attending an event collecting request
	 */
	pointcut attendEventCollectingConfiguration(): call(* MainDaemon.attendEventCollectingConfiguration(..));

	before() : attendEventCollectingConfiguration() {
		logger.Logger
				.getInstance()
				.log(
						"[SimPatrol.MainDaemon]: \"Event collecting\" configuration received.");
	}

	/**
	 * Main daemon is attending a metric creation request
	 */
	pointcut attendMetricCreationConfiguration(): call(* MainDaemon.attendMetricCreationConfiguration(..));

	before() : attendMetricCreationConfiguration() {
		logger.Logger
				.getInstance()
				.log(
						"[SimPatrol.MainDaemon]: \"Metric creation\" configuration received.");
	}

	/**
	 * Main daemon is attending a simulation start request
	 */
	pointcut attendSimulationStartConfiguration(): call(* MainDaemon.attendSimulationStartConfiguration(..));

	before() : attendSimulationStartConfiguration() {
		logger.Logger
				.getInstance()
				.log(
						"[SimPatrol.MainDaemon]: \"Start simulation\" configuration received.");
	}

	/**
	 * MainDaemon starts working
	 */
	pointcut startMainDaemon() : execution(* MainDaemon.start(..));

	after() : startMainDaemon() {
		logger.Logger.getInstance().log(
				"[SimPatrol.MainDaemon]: Started working.");
	}

	/**
	 * MainDaemon creating agents
	 */
	pointcut createAgents(Agent agent, Society society) : 
		call(* MainDaemon.createAgents(..)) && args(agent, society);

	after(Agent agent, Society society) : createAgents(agent, society) {
		logger.Logger.getInstance().log(
				"[SimPatrol.Event]: Agent " + agent.reducedToXML(0)
						+ " created in society " + society.getObjectId() + ".");
	}

	/**
	 * MetricDaemon starts working
	 */
	pointcut startMetricDaemon(MetricDaemon daemon) : execution(* MetricDaemon.start(..)) && this(daemon);

	after(MetricDaemon daemon) : startMetricDaemon(daemon) {
		logger.Logger.getInstance().log(
				"[SimPatrol.MetricDaemon("
						+ daemon.getMetric().getClass().getName()
						+ ")]: Started working.");
	}

	/**
	 * MetricDaemon stops working
	 */
	pointcut stopMetricDaemon(MetricDaemon daemon) : execution(* MetricDaemon.stopWorking(..)) && this(daemon);

	after(MetricDaemon daemon) : stopMetricDaemon(daemon) {
		logger.Logger.getInstance().log(
				"[SimPatrol.MetricDaemon("
						+ daemon.getMetric().getClass().getName()
						+ ")]: Stopped working.");
	}

	/**
	 * PerceptionDaemon starts working
	 */
	pointcut startPerceptionDaemon(PerceptionDaemon daemon) : execution(* PerceptionDaemon.start(..)) && this(daemon);

	after(PerceptionDaemon daemon) : startPerceptionDaemon(daemon) {
		logger.Logger.getInstance().log(
				"[SimPatrol.PerceptionDaemon(" + daemon.AGENT.getObjectId()
						+ ")]: Started working.");
	}

	/**
	 * PerceptionDaemon.insertMessage
	 */
	pointcut insertMessage(PerceptionDaemon daemon, String message) : 
		call(* PerceptionDaemon.insertMessage(..)) && this(daemon) && args(message);

	after(PerceptionDaemon daemon, String message) : insertMessage(daemon, message) {
		System.out.println("[SimPatrol.Event]: Agent "
				+ daemon.AGENT.getObjectId() + " received message " + message
				+ ".");
	}

	/**
	 * PerceptionDaemon stops working
	 */
	pointcut stopPerceptionDaemon(PerceptionDaemon daemon) : execution(* PerceptionDaemon.stopWorking(..)) && this(daemon);

	after(PerceptionDaemon daemon) : stopPerceptionDaemon(daemon) {
		logger.Logger.getInstance().log(
				"[SimPatrol.PerceptionDaemon(" + daemon.AGENT.getObjectId()
						+ ")]: Stopped working.");
	}
}