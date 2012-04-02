package events;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * Listener interface for game events.
 * 
 * <br/>TODO (Sam) ajouter les evenements liés aux mises
 * 
 * @author Pierre Marques
 *
 */
public interface IEventListener extends EventListener, Remote {
	
	/**
	 * Invoked when a player left the game.
	 * @param e a player event object, containing the id of the player.
	 * @throws RemoteException if RMI fails
	 */
	void playerLeft(PlayerEvent e) throws RemoteException;
	
	/**
	 * invoked when a player joined the game.
	 * @param e a player event object, containing the id of the player.
	 * @throws RemoteException if RMI fails
	 */
	void playerJoined(PlayerEvent e) throws RemoteException;
	
	/**
	 * Invoked when a new item is created.
	 * @param e an event object containing the id of the item
	 * @throws RemoteException if RMI fails
	 */
	void ItemCreated(GameObjectEvent e) throws RemoteException;
	
	/**
	 * Invoked when a new idea is created.
	 * @param e an event object containing the id of the idea
	 * @throws RemoteException if RMI fails
	 */
	void IdeaCreated(GameObjectEvent e) throws RemoteException;
	
	/**
	 * Invoked when an idea is defined as linked to some others.
	 * @param e a link event object containing the implicated ideas id.
	 * @throws RemoteException if RMI fails
	 */
	void IdeaLinkCreated(LinkEvent e) throws RemoteException;
	
	/**
	 * Invoked when a comment is made on an idea.<br/>
	 * Also gets invoked when an answer is made 
	 * @param e an event object containing the id of the comment
	 * @throws RemoteException if RMI fails
	 */
	void ideaCommentCreated(GameObjectEvent e) throws RemoteException;

	/**
	 * Invoked when the game this listener is registered on is finishing
	 * @throws RemoteException if RMI fails
	 */
	void endOfGame() throws RemoteException;
}
