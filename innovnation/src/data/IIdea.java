package data;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import functions.IGame;

/**
 * 
 * @author Pierre Marques
 */
public interface IIdea 
extends IGameObject {

	Collection<Integer> getItemsIds();
	
	Collection<IItem> getItems();

	Collection<IComment> getComments();

	Collection<Integer> getParentsIds();
	
	Collection<Integer> getChildrenIds();

	Collection<IIdea> getChildren();

	Collection<IIdea> getParents();

	
	/**
	 * Returns true if the idea has a parent id (which should be true, 
	 * except for the root idea)
	 * @return
	 * @throws RemoteException
	 */
	boolean hasParents();

	/**
	 * Returns true if the idea has at least one child
	 * (and false if this is a leaf, indeed)
	 * @return
	 * @throws RemoteException
	 */
	boolean hasChildren();

	/**
	 * Returns the cumulate number of tokens 
	 * for this idea.
	 * @return
	 */
	public Integer getTotalBids();
	
	public Integer getMaxBids();
	
	
	public void betChanged(int nbTokens, IPlayer p, IGame g) throws RemoteException ;
	
	public void maxbetChanged(int nbTokens);
	
	String getDesc();

	public void addParentIndex(Integer _index);
	
	public ArrayList<Integer> getParentsIndexs();
	
	public Integer getIndex();
}
