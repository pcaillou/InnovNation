/**
 * 
 */
package events;


/**
 * @author Pierre Marques
 *
 */
public class GameObjectEvent implements Event{
	private static final long serialVersionUID = 1L;
	
	private int playerId, objectId;
	/**
	 * @param source
	 */
	public GameObjectEvent(int authorId, int objectId) {
		this.playerId = authorId;
		this.objectId = objectId;
	}
	
	/* (non-Javadoc)
	 * @see events.Event#getPlayerId()
	 */
	@Override
	public int getPlayerId() {
		return playerId;
	}

	public int getObjectId() {
		return objectId;
	}
	
}
