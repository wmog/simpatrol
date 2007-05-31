/* DynamicVertex.java */

/* The package of this class. */
package model.graph;

/* Imported classes and/or interfaces. */
import util.tpd.TimeProbabilityDistribution;

/** Implements dynamic vertexes of a Graph object,
 *  that can appear and disappear with a specific time
 *  probability distribution. */
public class DynamicVertex extends Vertex {
	/* Attributes. */
	/** Verifies if the vertex is appearing. */
	private boolean is_appearing;	
	
	/** The time probability distribution for the vertex appearing. */
	private TimeProbabilityDistribution appearing_pd;

	/** The time probability distribution for the vertex disappearing. */
	private TimeProbabilityDistribution disappearing_pd;
	
	/* Methods. */
	/** Constructor.
	 * @param label The label of the vertex.
	 * @param appearing_pd The time probability distribution for the vertex appearing.
	 * @param diappearing_pd The time probability distribution for the vertex disappearing.
	 * @param is_appearing TRUE, if the vertex is appearing, FALSE if not. */
	public DynamicVertex(String label, TimeProbabilityDistribution appearing_pd, TimeProbabilityDistribution disappearing_pd, boolean is_appearing) {
		super(label);
		this.appearing_pd = appearing_pd;
		this.disappearing_pd = disappearing_pd;
		this.is_appearing = is_appearing;
	}

	/** Returns if the vertex is appearing.
	 *  @return TRUE, if the vertex is appearing, FALSE if not. */
	public boolean isAppearing() {
		return this.is_appearing;
	}
	
	/** Configures if the vertex is appearing.
	 *  @param is_appearing TRUE, if the vertex is appearing, FALSE if not. */
	public void setIsAppearing(boolean is_appearing) {
		this.is_appearing = is_appearing;

		// TODO assegura que arestas irao desaparecer e guardar em memoria!
	}
	
	/** Returns the appearing probability distribution.
	 *  @return The time probability distribution for the edge appearing. */	
	public TimeProbabilityDistribution getAppearingTPD() {
		return this.appearing_pd;
	}
	
	/** Returns the disappearing probability distribution.
	 *  @return The time probability distribution for the edge disappearing. */	
	public TimeProbabilityDistribution getDisappearingTPD() {
		return this.disappearing_pd;
	}	
	
	public String toXML(int identation) {
		// holds the answer being constructed
		StringBuffer buffer = new StringBuffer(super.toXML(identation));
		
		// finds the appearing attribute, atualizing it if necessary
		if(this.is_appearing) {
			int index_appearing_value = buffer.lastIndexOf("false");
			buffer.replace(index_appearing_value, index_appearing_value + 5, "true");
		}
		
		// removes the closing of the xml tag
		int last_valid_index = 0;
		if(this.stigmas == null) last_valid_index = buffer.indexOf("/>");
		else last_valid_index = buffer.indexOf("\n\t</vertex>");
		
		buffer.delete(last_valid_index, buffer.length());

		// adds the time probability distributions
		buffer.append("\n");
		buffer.append(this.appearing_pd.toXML(identation + 1));
		buffer.append(this.disappearing_pd.toXML(identation + 1));
		
		// applies the identation
		for(int i = 0; i < identation; i++)
			buffer.append("\t");
		
		// closes the tags
		buffer.append("</vertex>\n");
		
		// returns the buffer content
		return buffer.toString();
	}
}