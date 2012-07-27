package client;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.swing.tree.DefaultMutableTreeNode;

import events.IEventListener;
import functions.IGame;
import functions.IGameDescription;

public interface IClientCore extends IEventListener {
	/**
	 * @param serverBindName
	 * @return the collection of games available on the given server
	 */
	Collection<IGameDescription> fetchServer(String serverBindName);
	
	/**
	 * connect this client to a game
	 * @param gameBindName
	 * @throws MalformedURLException
	 * @throws UnknownHostException
	 * @throws NotBoundException
	 * @throws RemoteException
	 */
	void connectToGame(String gameBindName) throws MalformedURLException,
			RemoteException, NotBoundException, UnknownHostException;


	/**
	 * disconnect this client from its game
	 */
	void disconnectFromGame();

	/**
	 * @return the current game, or null if disconnected
	 */
	IGame getGame();

	boolean isConnected();
	
	public void setPlayerOpinion(int[] opinion);
	
	int getRemainingTokens();
	int getPlayerId();
	void setPlayerId(int id);
	
	void spendTokens(int tokens) throws IllegalStateException;
	
	public interface TreeExplorator {
		void start(String startMessage);
		void end(String endMessage);
		
		/**
		 * must take care of recursion.
		 * @param content
		 * @param depth
		 */
		void work(DefaultMutableTreeNode content, int depth);
	}
	
	/**
	 * @param ideaId
	 * @param worker a worker whose work() is called on each child.
	 * @throws RemoteException
	 */
	void displayIdeaComments(int ideaId, TreeExplorator worker) throws RemoteException;
}