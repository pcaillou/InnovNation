package data;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;

import errors.AlreadyExistsException;
import errors.TooLateException;

/**
 * A Whiteboard stores the whole data of the game: ideas, items, comments and so on.
 * No manipulation of data there. 
 * 
 * @author Samuel Thiriot
 *
 */
public interface IWhiteboard {
	

	/**
	 * LinkedList are used as they are serialisable
	 * @return
	 * @throws RemoteException
	 */
	LinkedList<Integer> getAllItemsIds() throws RemoteException;
	
	LinkedList<IItem> getAllItems() throws RemoteException;
	
	IItem getItem(int itemId) throws RemoteException;
	
	
	IIdea getRootIdea() throws RemoteException;
	
	IIdea getIdea(int ideaId) throws RemoteException;

	LinkedList<IIdea> getAllIdeas() throws RemoteException;
	
	
	
	LinkedList<IComment> getAllComments() throws RemoteException;
	
	/**
	 * 
	 * @param commented
	 * @return
	 * @throws RemoteException
	 */
	LinkedList<IComment> getComments(int commented) throws RemoteException;
	
	IComment getComment(int comment) throws RemoteException;

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
}
