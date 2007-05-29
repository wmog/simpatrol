/* TimeProbabilityDistribution.java */

/* The package of this class. */
package util.tpd;

/* Imported classes and/or interfaces. */
import model.interfaces.XMLable;
import cern.jet.random.engine.MersenneTwister;

/** Implements probability distributions based on time of simulation. */
public abstract class TimeProbabilityDistribution implements XMLable {
	/* Attributes. */
	/** The id of the object.
	 *  Not part of the patrol problem modelling. */
	private String id;
	
	/** The seed for the random number generator. */
	protected int seed;
	
	/** The generator of random numbers. */
	protected MersenneTwister rn_generator;	
	
	/** Counts how many times the method nextBoolean() was
	 *  called. */
	protected int next_bool_counter;
	
	/* Methods. */
	/** Constructor.
	 *  @param seed The seed for the random number generation. */
	public TimeProbabilityDistribution(int seed) {
		this.id = this.getClass().getName() + "@" +
                  Integer.toHexString(this.hashCode()) + "#" +
                  Float.toHexString(System.currentTimeMillis());			
		
		this.seed = seed;
		this.rn_generator = new MersenneTwister(this.seed);
		this.next_bool_counter = -1;
	}
	
	/** Returns if an associated event shall happen, according
	 *  to the time probability distribution.
	 *  
	 *  The next_bool_counter attribute must be increased,
	 *  when this method is implemented, before any code!
	 *  
	 *  @return TRUE if the event must happen, FALSE if not. */
	public abstract boolean nextBoolean();
	
	public String getObjectId() {
		return this.id;
	}
}