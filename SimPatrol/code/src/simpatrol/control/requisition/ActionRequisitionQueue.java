package simpatrol.control.requisition;

import java.util.List;

import simpatrol.control.parser.ActionIntentionParser;
import simpatrol.util.Queue;

/**
 * @model.uin <code>design:node:::4gdw3f17vcg2c3hi59r</code>
 */
public class ActionRequisitionQueue extends Queue {

	/**
	 * @model.uin <code>design:node:::gtuu7f17vcg2c-779mfa</code>
	 */
	public List<ActionRequisition> actionRequisition;

	/**
	 * @model.uin <code>design:node:::7fxyuf17vbztqy5hrpa</code>
	 */
	public ActionIntentionParser actionIntentionParser;

	/**
	 * @param requisition
	 * @model.uin <code>design:node:::f5tfef17vcg2cxt64xx:4gdw3f17vcg2c3hi59r</code>
	 */
	public void insert(ActionRequisition requisition) {
		/* default generated stub */;

	}

	/**
	 * @model.uin <code>design:node:::aj9uef17vcg2c-rkhjvn:4gdw3f17vcg2c3hi59r</code>
	 */
	public ActionRequisition remove() {
		/* default generated stub */;
		return null;
	}
}
