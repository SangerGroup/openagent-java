package openagent.api.javafx;

class AgentManager {
	
	private static FXAgent instance;
	
	private static Callback cb;
	
	public static FXAgent getAgent() {
		return instance;
	}
	
	
	/**
	 * Sets the {@code AgentManager}'s instance creation listener.
	 * The listener is called when the {@code FXAgent} instance
	 * stored in the {@code AgentManager} class is set.
	 * @param callback - The code to run after the Agent instance
	 * is created.
	*/
	public static void setOnInstanceCreated(Callback callback) {
		cb = callback;
	}
	
	/**
	 * 
	*/
	public static void setAgent(FXAgent a) {
		instance = a;
		if(cb != null) {
			instance.setOnFinish((agent) -> cb.finished(a));
		}
	}

}
