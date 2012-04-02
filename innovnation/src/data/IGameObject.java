package data;


/**
 * A game object was created by a player and a given time
 * 
 * @author Samuel Thiriot
 *
 */
public interface IGameObject extends ITimable, IStorable {

	public int getPlayerId();
	
}
