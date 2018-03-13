package openagent.api;

/**
 * An unchecked version of {@code AgentException}, used
 * for minor errors that can be recovered from.
 * @see AgentException
*/
public class UncheckedAgentException extends RuntimeException {
	
	public UncheckedAgentException(String msg) {
		super(msg);
	}
	
	public UncheckedAgentException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
