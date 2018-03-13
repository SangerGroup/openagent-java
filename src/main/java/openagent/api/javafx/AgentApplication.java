package openagent.api.javafx;

import javax.swing.JFrame;

/**
 * {@code AgentApplication} is an interface
 * that assists you in coding applications
 * that work with OpenAgent.
*/
public interface AgentApplication {
	
	/**
	 * Configures the agent.
	 * This method should be used
	 * to set properties of the agent, and not
	 * to show the main frame.
	 * @param agent - The agent to configure
	*/
	public void configure(JFrame mainFrame, FXAgent agent);
	
	/**
	 * Use this method to set the properties
	 * of the main frame, then show it.
	 * Note that the frame is not shown automatically.<br>
	 * This method is called after the agent is shown.
	 * @param mainFrame - The JFrame for
	 * the main content of your app
	*/
	public void show(JFrame mainFrame, FXAgent agent);
	
}
