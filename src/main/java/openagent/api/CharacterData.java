package openagent.api;

import java.io.File;
import java.nio.file.Paths;

/**
 * The {@code CharacterData} class
 * wraps OpenAgent character data in
 * an object.
*/
public class CharacterData {
	
	private String dataPath = "";
	
	private boolean animated = false;
	
	private String charName = "Agent";
	
	/**
	 * Initializes a new instance of {@code CharacterData}.
	 * @param internalPath - The path to the character
	 * data folder.
	 * @param charName - The name of the character to create.
	*/
	public CharacterData(String dataPath, String charName) {
		this(dataPath, false, charName);
	}
	
	/**
	 * Initializes a new instance of {@code CharacterData}.
	 * @param internalPath - The path to the character
	 * data folder.
	 * @param animated - Determines whether the character
	 * will use animations to be more expressive or not.
	 * For this property to be set to true, the character
	 * must implement the animations "show", "hide", 
	 * and (optionally) "breathe".
	*/
	public CharacterData(String dataPath, boolean animated, String charName) {
		this.dataPath = dataPath;
		this.animated = animated;
		this.charName = charName;
	}
	
	/**
	 * Initializes a new instance of {@code Character}.
	 * @param internalPath - The path to the character
	 * data folder within the library's JAR file.
	 * @param v - This parameter does nothing and is
	 * used to differentiate between this constructor
	 * and the visible one. Pass {@code null} for this
	 * parameter.
	*/
	protected CharacterData(String internalPath, Void v, boolean animated, String charName) {
		//boolean inJarFile = getClass().getResource("CharacterData.class").toExternalForm().startsWith("jar:");
		String optionalSeparator = (internalPath.startsWith(File.separator) ? "" : File.separator);
		this.dataPath = Paths.get(".").toAbsolutePath().normalize().toString() + optionalSeparator + internalPath;
		this.animated = animated;
		this.charName = charName;
		/*
		 * (inJarFile ? 
						"jar:" + getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm() + "!" + optionalSeparator
						:*/
	}
	
	public String getDataPath() {
		return dataPath;
	}
	
	public void setAnimated(boolean animated) {
		this.animated = animated;
	}
	
	public boolean getAnimated() { return this.animated; } 
	
	public String getCharName() {
		return charName;
	}
	
	public void setCharName(String name) {
		this.charName = name;
	}

}
