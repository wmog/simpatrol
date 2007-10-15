/* AgentUDPConnection.java */

/* The package of this class. */
package view.connection;

/* Imported classes and/or interfaces. */
import java.io.IOException;
import util.Queue;

/** Implements the UDP connections with the external agents. */
public final class AgentUDPConnection extends UDPConnection {
	/* Attributes. */
	/** The buffer where the connection writes the received
	 *  perception messages. */
	private Queue<String> perception_buffer;
	
	/** The buffer where the connection writes the received
	 *  action messages. */
	private Queue<String> action_buffer;
	
	/* Methods. */
	/** Constructor.
	 * 
	 *  @param name The name of the thread of the connection. 
	 *  @param perception_buffer The buffer where the connection writes the received perception messages.
	 *  @param action_buffer The buffer where the connection writes the received action messages. */
	public AgentUDPConnection(String name, Queue<String> perception_buffer, Queue<String> action_buffer) {
		super(name, perception_buffer);
		this.perception_buffer = this.buffer;
		this.action_buffer = action_buffer;
	}
	
	public void run() {
		while(!this.stop_working) {
			String message = null;
			
			try {
				message = this.socket.receive();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			// decides if the message is a perception or action
			// related message
			if(message.indexOf("action") > -1)
				this.action_buffer.insert(message);
			else this.perception_buffer.insert(message);
		}
		
		// screen message
		System.out.println("[SimPatrol.AgentConnection(" + this.getName() + ")]: Received message.");
	}	
}