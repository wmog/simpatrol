package model.agent;

import model.interfaces.XMLable;
import model.graph.Vertex;
import model.graph.Edge;
import model.perception.Perception;

/**
 * @model.uin <code>design:node:::i172kf17ujey8agupu8</code>
 */
public abstract class Agent implements XMLable {

	/**
	 * @model.uin <code>design:node:::a7glof17uk14ujzrw3k</code>
	 */
	public Edge edge;

	/**
	 * @model.uin <code>design:node:::e2gmtf17uk14u-dyl3jm</code>
	 */
	public Vertex vertex;

	/**
	 * @model.uin <code>design:node:::j6ugpf17ujey8btcg67:i172kf17ujey8agupu8</code>
	 */
	private double stamina = 1.0;

	/**
	 * @param perception
	 * @model.uin <code>design:node:::4nanf17ujey8-fenk9p:i172kf17ujey8agupu8</code>
	 */
	public void setPerception(Perception perception) {
		/* default generated stub */;

	}

	/**
	 * @param requisition
	 * @model.uin <code>design:node:::dlwtnf17ujey8bzkqn9:i172kf17ujey8agupu8</code>
	 */
	public void requireBroadcastingPercetion(String requisition) {
		/* default generated stub */;

	}
}
