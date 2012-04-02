/**
 * 
 */
package events;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Pierre Marques
 *
 */
public class EventAdapter extends UnicastRemoteObject implements IEventListener {
	
	private static final long serialVersionUID = 1L;

	public EventAdapter() throws RemoteException{
		super();
	}
	
	/* (non-Javadoc)
	 * @see events.IEventListener#playerLeft(events.PlayerEvent)
	 */
	@Override
	public void playerLeft(PlayerEvent e) throws RemoteException {}

	/* (non-Javadoc)
	 * @see events.IEventListener#playerJoined(events.PlayerEvent)
	 */
	@Override
	public void playerJoined(PlayerEvent e) throws RemoteException {}

	/* (non-Javadoc)
	 * @see events.IEventListener#ItemCreated(events.GameObjectEvent)
	 */
	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException {}

	/* (non-Javadoc)
	 * @see events.IEventListener#IdeaCreated(events.GameObjectEvent)
	 */
	@Override
	public void IdeaCreated(GameObjectEvent e) throws RemoteException {}

	/* (non-Javadoc)
	 * @see events.IEventListener#IdeaLinkCreated(events.LinkEvent)
	 */
	@Override
	public void IdeaLinkCreated(LinkEvent e) throws RemoteException{}
	
	/* (non-Javadoc)
	 * @see events.IEventListener#commentCreated(events.GameObjectEvent)
	 */
	@Override
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {}

	/* (non-Javadoc)
	 * @see events.IEventListener#endOfGame()
	 */
	@Override
	public void endOfGame() throws RemoteException {}
}
