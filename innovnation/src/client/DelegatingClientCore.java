package client;

import java.rmi.RemoteException;

import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;

/**
 * Functional core of a client (player).
 * Receives requests from a GUI or another interface; 
 * also receives updates (events) from a server. 
 * 
 * @author Samuel Thiriot
 *
 */
public class DelegatingClientCore extends ClientCore {

	private IEventListener listener;

	public DelegatingClientCore(IEventListener ui) {
		super();
		if(ui==null) throw new NullPointerException();
		listener = ui;
	}

	@Override
	public void playerLeft(PlayerEvent e) throws RemoteException {
		listener.playerLeft(e);
	}

	@Override
	public void playerJoined(PlayerEvent e) throws RemoteException {
		listener.playerJoined(e);
	}

	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException {
		listener.ItemCreated(e);
	}

	@Override
	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
		listener.IdeaCreated(e);
	}

	@Override
	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
		listener.IdeaLinkCreated(e);
	}

	@Override
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {
		listener.ideaCommentCreated(e);
	}

	@Override
	public void endOfGame() throws RemoteException {
		listener.endOfGame();
	}
}
