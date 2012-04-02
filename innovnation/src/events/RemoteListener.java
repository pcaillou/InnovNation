package events;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Makes any IEventListener remote by acting as a proxy: the RemoteListener extends UnicastRemoteObject,
 * and delegates any calls to the listener of your choice, which does not has to be Unicast itself.
 * 
 * @author Samuel Thiriot
 *
 */
public class RemoteListener extends UnicastRemoteObject implements IEventListener {

	private final IEventListener listener;
	
	public RemoteListener (IEventListener listener) throws RemoteException {
	
		this.listener = listener;
	}

	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {
		listener.ideaCommentCreated(e);
	}

	public void endOfGame() throws RemoteException {
		listener.endOfGame();
	}

	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
		listener.IdeaCreated(e);
	}

	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
		listener.IdeaLinkCreated(e);
	}

	public void ItemCreated(GameObjectEvent e) throws RemoteException {
		listener.ItemCreated(e);
	}

	public void playerJoined(PlayerEvent e) throws RemoteException {
		listener.playerJoined(e);
	}

	public void playerLeft(PlayerEvent e) throws RemoteException {
		listener.playerLeft(e);
	}
	
	
}
