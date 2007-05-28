/* Vertex.java */

/* The package of this class. */
package model.graph;

/* Imported classes and/or interfaces. */
import java.util.HashSet;
import java.util.Set;
import model.interfaces.XMLable;

/** Implements the vertexes of a Graph object.
 *  @see Graph */
public class Vertex implements XMLable {
	/* Atributes. */
	/** The set of edges whose emitter is this vertex. */
	private Set<Edge> in_edges;

	/** The set of edges whose collector is this vertex. */
	private Set<Edge> out_edges;

	/** The stigma eventually deposited by a patroller.
	 *  Its default value is NULL. */
	private Stigma stigma = null;

	/** The label of the vertex. */
	private String label;

	/** The priority to visit this vertex.
	 *  Its default value is ZERO. */
	private int priority = 0;

	/** Expresses if this vertex is visible in the graph.
	 *  Its default value is TRUE. */
	private boolean visibility = true;

	/** Registers the idleness of this vertex. */
	private int idleness;

	/** Expresses if this vertex is a point of recharging the energy
	 *  of the patrollers.
	 *  Its default value is FALSE. */
	private boolean fuel = false;
	
	/* Methods. */
	/** Constructor.
	 *  @param label The label of the vertex. */
	public Vertex(String label) {
		this.in_edges  = new HashSet<Edge>();
		this.out_edges = new HashSet<Edge>();
		this.label = label;
		this.idleness = 0;
	}
	
	/** Adds the passed edge to the vertex.
	 *  @param edge The edge added to the vertex. It cannot be an arc. */
	public void addEdge(Edge edge) {
		// as the edge is not an arc, it must be added to both
		// in and out edge sets
		this.in_edges.add(edge);
		this.out_edges.add(edge);
	}
	
	/** Adds the passed edge as a way out arc to the vertex.
	 *  @param out_arc The edge whose emitter is this vertex. */
	public void addOutEdge(Edge out_arc) {
		this.out_edges.add(out_arc);
	}
	
	/** Adds the passed edge as a way in arc to the vertex.
	 *  @param in_arc The edge whose collector is this vertex. */
	public void addInEdge(Edge in_arc) {
		this.in_edges.add(in_arc);
	}
	
	/** Returns the edges set of the vertex.
	 *  @return The set of edges associated with the vertex.*/
	public Set<Edge> getEdges() {		
		Set<Edge> answer = new HashSet<Edge>();
		answer.addAll(this.in_edges);
		answer.addAll(this.out_edges);
		return answer;		
	}
}