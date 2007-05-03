package simpatrol.control.parser;

import java.util.List;
import simpatrol.control.intention.ActionIntention;
import simpatrol.control.requisition.ActionRequisition;
import simpatrol.control.requisition.ActionRequisitionQueue;
import simpatrol.util.Clock;
import simpatrol.view.connection.AgentConnection;

/**
 * @model.uin <code>design:node:::7fxyuf17vbztqy5hrpa</code>
 */
public class ActionIntentionParser {

	/**
	 * @model.uin <code>design:node:::8j88bf17vghjr-7fjxy4</code>
	 */
	public Clock clock;

	/**
	 * @model.uin <code>design:node:::cmvoxf17vflv5-nneeo7</code>
	 */
	public AgentConnection agentConnection;

	/**
	 * @model.uin <code>design:node:::gtuu7f17vcg2c-779mfa</code>
	 */
	public List<ActionRequisition> actionRequisition;

	/**
	 * @model.uin <code>design:node:::4gdw3f17vcg2c3hi59r</code>
	 */
	public ActionRequisitionQueue actionRequisitionQueue;

	/**
	 * @param intention
	 * @model.uin <code>design:node:::dntruf17vbztq-1y70wc:7fxyuf17vbztqy5hrpa</code>
	 */
	public void parseActionIntention(ActionIntention intention) {
		/* default generated stub */;

	}

	/**
	 * @param current_time
	 * @model.uin <code>design:node:::a5jbef17vbztq-gttgps:7fxyuf17vbztqy5hrpa</code>
	 */
	public void requireCurrentActions(int current_time) {
		/* default generated stub */;

	}

	/**
	 * @param requisition
	 * @model.uin <code>design:node:::byxoyf17vbztqo7zwz7:7fxyuf17vbztqy5hrpa</code>
	 */
	private void requireAction(ActionRequisition requisition) {
		/* default generated stub */;

	}
}