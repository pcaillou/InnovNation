package data;

import java.rmi.RemoteException;

/**
 * A whiteboard that stores data on the server site
 * 
 * @author Samuel Thiriot
 *
 */
public final class ServerWhiteboard extends Whiteboard implements IDistantWhiteboard {

	public ServerWhiteboard(String rootIdeaName) throws RemoteException {
		super(rootIdeaName);
	}

	public ServerWhiteboard(IIdea rootIdea) throws RemoteException {
		super(rootIdea);
	}

}
