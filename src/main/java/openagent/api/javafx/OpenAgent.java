package openagent.api.javafx;

import javax.swing.JFrame;

import openagent.api.AgentException;
import openagent.api.CharacterData;
import openagent.api.javafx.AgentManager;
import openagent.api.javafx.Callback;
import openagent.api.javafx.FXAgent;

public abstract class OpenAgent {
	
	private static boolean wasAgentCalled = false;
	
	/**
	 * Loads an instance of {@code FXAgent}.<br>
	 * The {@code FXAgent} constructor should never be called directly;
	 * it is misleading to call it because the instance created
	 * by the constructor is not the same instance that shows up
	 * on the screen.<br><br>
	 * 
	 * @param dataPath - The path to the character data folder, including
	 * the folder name but *no* a path separator (\ or / character) at the end.
	 * @param animated - This property determines whether the character will use
	 * animations to be more expressive or not.
	 * @param beforeShown - The code to run before the {@code FXAgent} is shown.
	 * Use this callback to add menu items, set properties, etc.
	 * @param afterShown - The code to run after the {@code FXAgent} is shown.
	 * Use this callback to use the {@code FXAgent} after setup, to do things
	 * like speaking, playing animations, etc.
	*/
	public static void loadAgent(
			CharacterData data,
			Callback beforeShown,
			Callback afterShown) {
		if(!wasAgentCalled) {
			FXAgent agent = new FXAgent(data.getDataPath(), data.getAnimated(), data.getCharName());
			AgentManager.setOnInstanceCreated((a) -> {
				beforeShown.finished(a);
				a.show(() -> afterShown.finished(a));
			});
		} else {
			throw new IllegalStateException("Cannot launch more than one FXAgent."
					+ " Use the class openagent.api.swing.Agent if you want to create more than one.");
		}
	}
	
	/**
	 * Loads an instance of {@code FXAgent}.<br>
	 * The {@code FXAgent} constructor should never be called directly;
	 * it is misleading to call it because the instance created
	 * by the constructor is not the same instance that shows up
	 * on the screen.<br><br>
	 * 
	 * @param dataPath - The path to the character data folder, including
	 * the folder name but *no* a path separator (\ or / character) at the end.
	 * @param animated - This property determines whether the character will use
	 * animations to be more expressive or not.
	 * @param beforeShown - The code to run before the {@code FXAgent} is shown.
	 * Use this callback to add menu items, set properties, etc.
	 * @param afterShown - The code to run after the {@code FXAgent} is shown.
	 * Use this callback to use the {@code FXAgent} after setup, to do things
	 * like speaking, playing animations, etc.
	*/
	public static void loadAgent(
			String dataPath,
			boolean animated,
			Callback beforeShown,
			Callback afterShown) {
		if(!wasAgentCalled) {
			FXAgent agent = new FXAgent(dataPath, animated, "Agent");
			AgentManager.setOnInstanceCreated((a) -> {
				beforeShown.finished(a);
				a.show(() -> afterShown.finished(a));
			});
		} else {
			throw new IllegalStateException("Cannot launch more than one FXAgent."
					+ " Use the class openagent.api.swing.Agent if you want to create more than one.");
		}
	}
	
	/**
	 * Launches an instance of {@code AgentApplication}.<br>
	 * The {@code FXAgent} constructor should never be called directly;
	 * it is misleading to call it because the instance created
	 * by the constructor is not the same instance that shows up
	 * on the screen.<br><br>
	 * 
	 * @param dataPath - The path to the character data folder, including
	 * the folder name but *not* a path separator (\ or / character) at the end.
	 * @param isAgentAnimated - This property determines whether the character will use
	 * animations to be more expressive or not. To use this 
	 * @param clazz - The class to launch. This class must extend {@code AgentApplication}.
	 * @throws AgentException 
	*/
	public static void launchApplication(
			CharacterData data,
			Class<? extends AgentApplication> clazz) throws AgentException {
		try {
			JFrame appFrame = new JFrame("OpenAgent Application");
			appFrame.setSize(200, 200);
			final AgentApplication toInit = clazz.newInstance();
			loadAgent(data.getDataPath(), data.getAnimated(),
			agent -> {
				toInit.configure(appFrame, agent);
			},
			agent -> {
				toInit.show(appFrame, agent);
			});
		} catch (Exception e) {
			throw new AgentException(
					"Could not initialize given instance of AgentApplication.", e);
		}
	}

}
