package openagent.api;

/**
 * A generic checked exception, used in OpenAgent.
 * @see UncheckedAgentException
*/
public class AgentException extends Exception {
	
	public AgentException(String msg) {
		super(msg);
	}
	
	public AgentException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
