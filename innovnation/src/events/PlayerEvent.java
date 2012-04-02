/**
 * 
 */
package events;


/**
 * @author Pierre Marques
 *
 */
public class PlayerEvent implements Event{
	private static final long serialVersionUID = 1L;
	
	private int playerId;
	/**
	 * @param source
	 */
	public PlayerEvent(int source) {
		playerId=source;
	}
	
	/* (non-Javadoc)
	 * @see events.Event#getPlayerId()
	 */
	@Override
	public int getPlayerId() {
		return playerId;
	}

	
}
