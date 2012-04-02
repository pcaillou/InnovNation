package client.gui.prefuse;

public enum GuiClientMode {

	/**
	 * The GUI is connected to no game. 
	 */
	DISCONNECTED,	
	
	/**
	 * The GUI is connected to a game, listens for events, 
	 * but did not registered any player. 
	 */
	MONITOR,	
	
	/**
	 * Connected to a game, listening, and playing !
	 */
	PLAYING;
	
}
