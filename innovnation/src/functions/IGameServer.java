package functions;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import errors.AlreadyExistsException;
import errors.RequestRefusedException;

/**
 * This is the RMI interface of the game server.
 * This server is no more than a game store.
 * @author Pierre Marques
 */
public interface IGameServer extends Remote {
	
	/**
	 * Shut down the server, closing every games that as not started.
	 * @throws RemoteException
	 */
	void shutDown() throws RemoteException;
	
	/**
	 * Tells what are the games currently opened.
	 * @return a collection of games.
	 * @throws RemoteException
	 */
	Collection<IGameDescription> getOpenGames() throws RemoteException;
	
	/**
	 * Creates a new game and register it on RMI.
	 * @param name an identifier to distinguish this game from others
	 * @param rootIdea the theme of this game
	 * @return the RMI path of the newly created game
	 * @throws RemoteException
	 * @throws AlreadyBoundException if this name is already used
	 * @throws MalformedURLException if the name is not valid for RMI registry
	 * @throws UnknownHostException if the game can't get created for some obscure reason
	 */
	String createGame(String name, String rootIdea)
		throws RemoteException, MalformedURLException, AlreadyExistsException, UnknownHostException, RequestRefusedException;

}
