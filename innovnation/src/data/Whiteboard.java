/**
 * 
 */
package data;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import util.HashMultiMap;
import util.MultiMap;
import util.graph.Dag;
import errors.AlreadyExistsException;
import errors.TooLateException;


/**
 * 
 * <p>The idea graph is by essence garanteed to have one and only one root.
 * this implies that a child forward exploration from this root
 * will iterate on every ideas.</p>
 * 
 * @author Pierre Marques
 */
public class Whiteboard implements IWhiteboard {
	
	protected Map<Integer, IItem> items;
	protected Dag<Integer, IIdea> ideas;
	private Integer rootIdeaId = 0;
	
	//private int ideaSerializer;
	
	private MultiMap<Integer, IComment > ideaComments;
	//private MultiMap<Integer, IComment > itemComments;
	
	

	public Whiteboard(String rootIdeaName) throws RemoteException{

		this(
				new Idea(
						0, 
						rootIdeaName, 
						"",
						new Dag<Integer, IIdea>(), 
						null
						)
				);
		
		
	}

	public Whiteboard(IIdea rootIdea) throws RemoteException{
		super();
		
		items = new HashMap<Integer, IItem>();
		
		ideas = new Dag<Integer, IIdea>();
	
		ideas.createNode(rootIdea.getUniqueId(), rootIdea);
		rootIdeaId = rootIdea.getUniqueId();
		
		ideaComments = new HashMultiMap<Integer, IComment>();
		//itemComments = new HashMultiMap<IItem, IComment<IItem>>();
		
	}
	

	/**
	 * @param itemId
	 * @return
	 */
	public IItem getItem(int itemId) {
		return items.get(itemId);
	}
	
	/* (non-Javadoc)
	 * @see data.IWhiteboard#getAllItemsIds()
	 */
	public LinkedList<Integer> getAllItemsIds() throws RemoteException{
		return new LinkedList<Integer>(items.keySet());
	}
	
	/* (non-Javadoc)
	 * @see data.IWhiteboard#getAllItems()
	 */
	public LinkedList<IItem> getAllItems() throws RemoteException{
		return new LinkedList<IItem>(items.values());
	}
	

	
	/* (non-Javadoc)
	 * @see data.IWhiteboard#getIdea(int)
	 */
	public IIdea getIdea(int ideaId) throws RemoteException{
		return ideas.get(ideaId);
	}
	
	/* (non-Javadoc)
	 * @see data.IWhiteboard#getRootIdea()
	 */
	@Override
	public IIdea getRootIdea() throws RemoteException {
		return ideas.get(rootIdeaId);
	}

	/* (non-Javadoc)
	 * @see data.IWhiteboard#getAllIdeas()
	 */
	@Override
	public LinkedList<IIdea> getAllIdeas() throws RemoteException {
		LinkedList<IIdea> res = new LinkedList<IIdea>();
		Iterator<IIdea> i = ideas.iterator();
		while(i.hasNext()) res.add(i.next());
		return res;
	}

	/* (non-Javadoc)
	 * @see data.IWhiteboard#getAllComments()
	 */
	@Override
	public LinkedList<IComment> getAllComments() throws RemoteException {
		return new LinkedList<IComment>(ideaComments.values());
	}
	
	/* (non-Javadoc)
	 * @see data.IWhiteboard#getAllComments()
	 */
	@Override
	public LinkedList<IComment> getComments(int commented) throws RemoteException {
		return new LinkedList<IComment>(ideaComments.get(commented));
	}

	/*
	 * content creation
	 */
	
	/* (non-Javadoc)
	 * @see data.IWhiteboard#addItem(java.lang.String, java.lang.String)
	 */
	@Override
	public int addItem(int authorId, String itemName, String itemDescription)
			throws RemoteException {
		for(IItem i : items.values()){
			if(i.getShortName().equals(itemName)) throw new AlreadyExistsException(itemName);
		}
		IItem i = new Item(authorId, itemName, itemDescription);
		items.put(i.getUniqueId(), i);
		return i.getUniqueId();
	}

	/**
	 * Stores the copy of the item as an item, and preserves its unique ID
	 * @param otherItem
	 * @return
	 */
	public int addItemCopy(IItem otherItem) {
		items.put(otherItem.getUniqueId(), otherItem);
		return otherItem.getUniqueId();
	}
	
	public void removeItemCopy(Integer itemId) { /* TODO */ }
	
	public void removeItemCopy(IItem item) { /* TODO */ }
	
	public int addIdeaCopy(IIdea copy) {
		ideas.createNode(copy.getUniqueId(), copy);
		for(Integer parent : copy.getParentsIds()){
			ideas.makeDepend(copy.getUniqueId(), parent);
		}
		return copy.getUniqueId();
	}
	
	/* (non-Javadoc)
	 * @see data.IWhiteboard#addIdea(java.lang.String, java.util.Collection, java.util.Collection, int)
	 */
	@Override
	public int addIdea(int authorId, String ideaName, String ideaDesc,
			Collection<Integer> itemsIds,
			Collection<Integer> parentIdeasIds
	) throws AlreadyExistsException, TooLateException, RemoteException{
		
		IIdea idea;
		
		Iterator<IIdea> i = ideas.iterator();
		while(i.hasNext()){
			idea=i.next();
			if(idea.getShortName().equals(ideaName))
				throw new AlreadyExistsException(ideaName);
		}
		
		// integrity checks
		for(Integer parent : parentIdeasIds){
			if(!ideas.exists(parent)) throw new IllegalArgumentException("no idea known as "+parent);
		}
		
		for(Integer item : itemsIds){
			if(!items.containsKey(item)) throw new IllegalArgumentException("no item known as "+item);
		}
	
		
		idea = new Idea(authorId, ideaName, ideaDesc, ideas, itemsIds);
		int id = idea.getUniqueId();
		
		
		ideas.createNode(id, idea);
		for(Integer parent : parentIdeasIds){
			ideas.makeDepend(id, parent);
			idea.addParentIndex(ideas.get(parent).getIndex());
		}

		if ( (id != rootIdeaId) && (parentIdeasIds.isEmpty()) ) {
			// if no parent provided, but this idea is not a root idea, then automatically add the root idea as parent
			ideas.makeDepend(id, rootIdeaId);
		}
		
		return id;
	}

	@Override
	public IComment getComment(int comment) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	 

	
}
