package functions;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;

import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;
import errors.AlreadyExistsException;
import errors.TooLateException;
import events.IListenable;

/**
 * A game. Composed of data, it aims to control the game flow.
 * 
 * @author Samuel Thiriot
 *
 */
public interface IGame extends IListenable, Remote, Serializable {
	
	//public void destroy() throws RemoteException;

	//general and rules observers
	
	IGameDescription getDescription() throws RemoteException;

	/**
	 * @return a non negative integer
	 */
	int getMaxTokensByPlayer() throws RemoteException;
	
	//content observers
	
	IItem getItem(int itemId) throws RemoteException; 
	
	LinkedList<Integer> getAllItemsIds() throws RemoteException;
	LinkedList<IItem> getAllItems() throws RemoteException;
	

	public int findIdeaFromComment(int commentId) throws RemoteException;

	
	IIdea getRootIdea() throws RemoteException;
	
	IIdea getIdea(int ideaId) throws RemoteException;
	
	LinkedList<IIdea> getAllIdeas() throws RemoteException;
	
	LinkedList<Integer> getIdeaParentIds(int ideaId) throws RemoteException;
	
	
	
	IComment getComment(int commentId) throws RemoteException;
	
	/**
	 * Warning: no garantee of sorting.
	 * @return
	 * @throws RemoteException
	 */
	LinkedList<IComment> getAllComments() throws RemoteException;
	

	/**
	 * Returns the sum of bids of this player on this idea 
	 * @return
	 */
	int currentBids(IIdea idea, IPlayer player) throws RemoteException;
	int currentBids(Integer ideaID, Integer playerID) throws RemoteException;
	
	/**
	 * Returns a copy which contains all the comments sorted in "intuitive" order
	 * (that is, deep first exploration of the tree).
	 * @param ideaId
	 * @return
	 * @throws RemoteException
	 */
	LinkedList<IComment> getAllIdeasComments(int ideaId) throws RemoteException;


	LinkedList<IComment> getIdeaMainComments(int ideaId) throws RemoteException;
	
	/**
	 * Return a TreeNode with the idea as content and its comments as children.<br/>
	 * Comments may have nested comments, recursively 
	 * @param ideaId the id of the idea whose comments are to return.
	 * @return a TreeNode withthe idea as content and its comments as children.
	 * @throws RemoteException if RMI fails
	 */
	DefaultMutableTreeNode getIdeaComments(int ideaId) throws RemoteException;

	/**
	 * Gives a collection of all the players in this game
	 * TODO shall we convert this to an iterator
	 * @return a collection of internal representation
	 * @throws RemoteException
	 */
	LinkedList<IPlayer> getAllPlayers() throws RemoteException;

	/**
	 * Gives a collection of all the players in this game
	 * TODO shall we convert this to an iterator
	 * @return a collection of identifiers
	 * @throws RemoteException
	 */
	LinkedList<Integer> getAllPlayersIds() throws RemoteException;
	
	IPlayer getPlayer(int playerId) throws RemoteException;
	
	
	
	//injectors

	
	/**
	 * @param authorId
	 * @param itemName
	 * @param itemDescription
	 * @return
	 * @throws RemoteException
	 */
	public int addItem(int authorId, String itemName, String itemDescription)
		throws RemoteException;
	
	/**
	 * @param authorId the id of the author
	 * @param ideaName the name of the new idea
	 * @param itemsIds a collection of item ids to be made of
	 * @param ideasIds a collection of idea ids to be parented with
	 * @return
	 * @throws AlreadyExistsException if this idea is already existing
	 * @throws TooLateException 
	 * @throws RemoteException whe network is a problem
	 */
	int addIdea(int authorId,
			String ideaName,
			String ideaDesc,
			Collection<Integer> itemsIds,
			Collection<Integer> ideasIds
	) throws AlreadyExistsException, TooLateException, RemoteException;
	
	/**
	 * @param authorId the id of the author
	 * @param ideaName the name of the new idea
	 * @param itemsIds a collection of item ids to be made of
	 * @param ideasIds a collection of idea ids to be parented with
	 * @param value value of the idea
	 * @param opinion of the idea
	 * @return
	 * @throws AlreadyExistsException if this idea is already existing
	 * @throws TooLateException 
	 * @throws RemoteException whe network is a problem
	 */
	int addBotIdea(int authorId,
			String ideaName,
			String ideaDesc,
			Collection<Integer> itemsIds,
			Collection<Integer> ideasIds,
			int _value,
			int[] opinion
	) throws AlreadyExistsException, TooLateException, RemoteException;
	
	/**
	 * Make an Idea parent of some others
	 * @param authorId the id of the author
	 * @param parentId the name of the affected idea
	 * @param ideasIds a collection of idea ids to be parented with
	 * @return
	 * @throws TooLateException 
	 * @throws RemoteException whe network is a problem
	 */
	void makeIdeaParentOf(
			int authorId,
			int parentId,
			Collection<Integer> ideasIds
	) throws TooLateException, RemoteException;
	
	/**
	 * Make an Idea child of some others
	 * @param authorId the id of the author
	 * @param childId the name of the affected idea
	 * @param ideasIds a collection of idea ids to be parented with
	 * @return
	 * @throws TooLateException 
	 * @throws RemoteException whe network is a problem
	 */
	void makeIdeaChildOf(
			int authorId,
			int childId,
			Collection<Integer> ideasIds
	) throws TooLateException, RemoteException;
	
	
	/**
	 * @param playerId
	 * @param ideaId
	 * @param text
	 * @throws RemoteException
	 */
	int commentIdea(int playerId, int ideaId, String text) throws RemoteException;

	int commentIdea(int playerId, int ideaId, String text, int tokens, CommentValence val) throws RemoteException;
	
	/**
	 * Ajout de commentaire pour bot
	 * @param playerId
	 * @param ideaId
	 * @param text
	 * @param tokens
	 * @param val
	 * @param value : valeur de l'idee
	 * @return
	 * @throws RemoteException
	 */
	int commentBotIdea(int playerId, int ideaId, String text, int tokens, CommentValence val, int value) throws RemoteException;

	/**
	 * @param playerId
	 * @param ideaId
	 * @param text
	 * @throws RemoteException
	 */
	int commentItem(int playerId, int itemId, String text) throws RemoteException;

	

	/**
	 * @param playerId
	 * @param ideaId
	 * @param text
	 * @throws RemoteException
	 */
	int answerComment(int playerId, int commentId, String text, int tokens) throws RemoteException;


	/**
	 * @param playerId
	 * @param ideaId
	 * @param text
	 * @throws RemoteException
	 */
	int answerComment(int playerId, int commentId, String text) throws RemoteException;

	

	/**
	 * Register a new player on this game.
	 * 
	 * @param playerName the name to be used by the new player
	 * @return the id generated for this player
	 * @throws RemoteException
	 */
	int addPlayer(String playerName) throws RemoteException;
	
	
	int addPlayer(String playerName, String avatar) throws RemoteException;
	
	/**
	 * Unregister a player from the game.
	 * 
	 * @param playerID the id used by the player
	 * @throws RemoteException
	 */
	void removePlayer(int playerId) throws RemoteException;


	/**
	 * Display the complete content of this game.
	 * This function is a replacement for toString (due to Remote constraints)
	 * @return a (long) string representing this game
	 * @throws RemoteException
	 */
	String status() throws RemoteException;

	/**
	 * Returns the timestamp for creation of the game, assuming that the game started 
	 * with the creation of the root idea.
	 * @return
	 */
	public long getDateCreation() throws RemoteException;

	boolean testExistingPlayer(String playerName) throws RemoteException;
	
	
	
}
