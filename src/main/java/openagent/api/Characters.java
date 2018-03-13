package openagent.api;

/**
 * Class that contains some default
 * characters (of the class {@code CharacterData}),
 * built into OpenAgent.
*/
public class Characters {
	
	/**
	 * The OpenAgent character Peedy, a green parrot
	 * who is fond of crackers and good music.
	*/
	public static final CharacterData PEEDY = new CharacterData("/res/chars/peedy", null, true, "Peedy");
	
	/**
	 * Internal variable used to declare both visible constants {@code CLIPPY} and {@code CLIPPIT}.
	*/
	private static final CharacterData PAPERCLIP = new CharacterData("/res/chars/clippy", null, true, "Clippy");
	
	/**
	 * The OpenAgent character Clippy/Clippit.<br>
	 * "When all else fails, bind some paper together.
	 * My name is Clippy." - Smore, Clippy.js (https://www.smore.com/clippy-js)<br><br>
	 * 
	 * This variable is identical to its alias, {@code CLIPPIT}.
	*/
	public static final CharacterData CLIPPY = PAPERCLIP;
	
	/**
	 * The OpenAgent character Clippy/Clippit.<br>
	 * "When all else fails, bind some paper together.
	 * My name is Clippy." - Smore, Clippy.js (https://www.smore.com/clippy-js)<br><br>
	 * 
	 * This variable is identical to {@code CLIPPY}.
	*/
	public static final CharacterData CLIPPIT = PAPERCLIP;
	
	/**
	 * The OpenAgent character Bonzi, taken from
	 * the program Bonzi Buddy.
	*/
	public static final CharacterData BONZI = new CharacterData("/res/chars/bonzi", null, true, "Bonzi");
	
}
