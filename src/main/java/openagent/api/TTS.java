package openagent.api;

public enum TTS {
	/**
	 * The MaryTTS engine.
	 * Quick and realistic-sounding.
	*/
	MARYTTS,
	
	/**
	 * The FreeTTS engine.
	 * Bad-sounding but fast.
	 * FreeTTS support has been
	 * removed from OpenAgent.
	*/
	@Deprecated
	FREETTS,
	
	/**
	 * The SAPI4 engine.
	 * This engine has the original
	 * voices from the Microsoft Agent
	 * program, but requires an Internet
	 * connection and is very slow
	 * (as the audio must download over
	 * the Internet). Also, privacy of
	 * spoken words is not guaranteed
	 * as the 
	*/
	SAPI4,
	
	/**
	 * No TTS engine.
	 * If this engine is
	 * used, no sound will
	 * play when the character
	 * speaks.
	*/
	NONE
}
