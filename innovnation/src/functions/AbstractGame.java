package functions;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import util.Pair;
import util.graph.Dag;
import data.Comment;
import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;
import data.IStorable;
import data.Idea;
import errors.AlreadyExistsException;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;


/**
 * A game is a data holder.<br/>
 * This abstract game only implements data access.
 * It allows data coherence checks, but do not force them.<br/>
 * (check must be done in inject callers)
 * @author Pierre Marques
 *
 */
public abstract class AbstractGame extends UnicastRemoteObject implements IGame{

	private static final long serialVersionUID = 1L;

	
	public static final int INITIAL_TOKEN_COUNT_BY_PLAYER = 10;
	
	private IGameDescription description;

	private Map<Integer, IPlayer> players;

	private Map<Integer, IItem> items;

	public Dag<Integer, IIdea> ideas;//public, to allow idea to know it (fast play)
	private Integer rootIdeaId = 0;
	
	private Logger logger = Logger.getLogger("abstractGame");
	
	/**
	 * ideaComments maps idea Id to a tree root node.<br/>
	 * the root of each tree is the idea's shortName, while each other node is a comment
	 */
	private Map<Integer, DefaultMutableTreeNode> ideaComments;
	
	/**
	 * provides fast access to find a comment by its id.<br/>
	 */
	private Map<Integer, DefaultMutableTreeNode> fastCommentLookup;
	
	private EventListenerList listeners;

	/**
	 * Creates an empty abstract game.
	 * @param descr
	 * @throws RemoteException
	 * @throws UnknownHostException
	 */
	public AbstractGame(IGameDescription descr) throws RemoteException, UnknownHostException {
		super();
		description = new GameDescription(descr);

		listeners = new EventListenerList();
		players = new LinkedHashMap<Integer, IPlayer>();

		items = new HashMap<Integer, IItem>();

		ideas = new Dag<Integer, IIdea>();
		
		ideaComments = new HashMap<Integer, DefaultMutableTreeNode>();
		fastCommentLookup = new HashMap<Integer, DefaultMutableTreeNode>();
		
		rootIdeaId = IStorable.notAnId;
	}

	/*
	 * usefull functions
	 */
	public  int findIdeaFromComment(int commentId)  throws RemoteException{
		//if given id is an idea, return it
		if(ideaComments.containsKey(commentId)) return commentId;
		
		DefaultMutableTreeNode n = fastCommentLookup.get(commentId);
		
		if(n.isRoot()) return ((IStorable) n.getUserObject()).getUniqueId();
		else return ((IStorable) ((DefaultMutableTreeNode) n.getRoot()).getUserObject()).getUniqueId();
	}
	
	protected int createRootIdea(IIdea idea) throws RemoteException{
		return injectIdea(new Idea(IStorable.notAnId, description.getTheme(), "", ideas, null));
	}
	
	protected final void terminate() throws RemoteException {
		try {
			System.out.println("game "+description.getName()+" unbound from "+description.getBindName());
			Naming.unbind(description.getBindName());
			unexportObject(this, true);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	/*
	 * true injectors
	 */

	/**
	 * injects an item in this Game, but do not fire an event;
	 * @param item the item to inject
	 * @return the unique identifier of this item
	 * @throws AlreadyExistsException if the id or the shortname is already used
	 * @throws NullPointerException if the given item is null
	 */
	protected final int injectItem(IItem item) throws AlreadyExistsException{
		if(item==null) throw new NullPointerException();
		if( items.containsKey(item.getUniqueId()) ) throw new AlreadyExistsException();

		for(IItem i : items.values()){
			if(i.getShortName().equals(item.getShortName())) throw new AlreadyExistsException();
		}

		items.put(item.getUniqueId(), item);
		return item.getUniqueId();
	}

	protected final IItem ejectItem(int id) throws AlreadyExistsException{
		return items.remove(id);
	}

	
	/*
	 * idea
	 */
	
	/**
	 * check if Idea components are valids and throw an exception if this is not the case
	 * @param playerId to check if author exists
	 * @param ideaName to check if this name is not used
	 * @param itemsIds to check if those exits
	 * @param parentIdeasIds to check if those exits
	 * @throws IllegalArgumentException if any part does not exists (author, parent, item)
	 * @throws AlreadyExistsException if the name is alreadyUsed
	 */
	protected final void validateAsIdea(int playerId, String ideaName, Collection<Integer> itemsIds, Collection<Integer> parentIdeasIds){
		if(!players.containsKey(playerId)) throw new IllegalArgumentException("no player known as "+playerId);

		IIdea idea;
		Iterator<IIdea> i = ideas.iterator();
		while(i.hasNext()){
			idea=i.next();
			if(idea.getShortName().equals(ideaName)) throw new AlreadyExistsException(ideaName+" is already used");
		}

		if (itemsIds == null)
			itemsIds = Collections.<Integer>emptyList();

		for(Integer item : itemsIds){
			if(!itemExists(item)) throw new IllegalArgumentException("no item known as "+item);
		}

		if (parentIdeasIds == null)
			parentIdeasIds = Collections.<Integer>emptyList();

		// integrity checks
		for(Integer parent : parentIdeasIds){
			if(!ideaExists(parent)) throw new IllegalArgumentException("no idea known as "+parent);
		}
	}

	protected final int injectIdea(int playerId, String ideaName, String desc, Collection<Integer> itemsIds, Collection<Integer> parentIdeasIds)
	throws AlreadyExistsException{
		validateAsIdea(playerId, ideaName, itemsIds, parentIdeasIds);
		
		IIdea idea = new Idea(playerId, ideaName, desc, ideas, itemsIds); 

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
		
		ideaComments.put(id, new DefaultMutableTreeNode(idea));
		return id;
	}

	/**
	 * injects an idea in this Game, but do not fire an event;
	 * @param idea the idea to inject
	 * @return the unique identifier of this idea
	 * @throws AlreadyExistsException if an idea with the same ID exists
	 * @throws NullPointerException if the given item is null
	 */
	protected final int injectIdea(IIdea idea) throws AlreadyExistsException{
		if(idea==null) throw new NullPointerException();
		if(ideas.exists(idea.getUniqueId())) throw new AlreadyExistsException();

		
		Collection<Integer> parentIdeasIds = idea.getParentsIds();

		int id = idea.getUniqueId();
		ideas.createNode(id, idea);
		
		if(parentIdeasIds.isEmpty()) {
			if(rootIdeaId==IStorable.notAnId){
				// if no parent provided, and there is no root idea, then automatically set as root
				rootIdeaId=id;
			}
		} else {
			for(Integer parent : parentIdeasIds){
				ideas.makeDepend(id, parent);
			}
		}
		
//		ideaComments.put(id, new DefaultMutableTreeNode(idea.getShortName()));
		ideaComments.put(id, new DefaultMutableTreeNode(idea));
		return id;
	}
	
	protected final void linkIdeas(int parentId, int childId){
		ideas.makeRelation(parentId, childId);
	}
	
	
	/*
	 * comment
	 */

	/**
	 * check if Comment components are valids and throw an exception if this is not the case
	 * @param playerId to check if author exists
	 * @param ideaId to check if this idea exits
	 * @param text the content of the comment
	 * @throws IllegalArgumentException if author or comment idea does not exists
	 */
	protected final void validateAsIdeaComment(int playerId, int ideaId, String text){
		if(!players.containsKey(playerId)) throw new IllegalArgumentException("no player known as "+playerId);
		if(!ideaExists(ideaId)) throw new IllegalArgumentException("no idea known as "+ideaId);
//		if(text.trim().isEmpty())  throw new IllegalArgumentException("this is a stupidly empty comment");
	}
	
	/**
	 * injects a comment in this Game, but do not fire an event<br/>
	 * the comment is an idea main comment (direct comment of an idea, not an answer)
	 * @param playerId
	 * @param ideaId
	 * @param text
	 * @return
	 * @throws AlreadyExistsException
	 */
	protected final int injectIdeaComment(int playerId, int ideaId, String text, int tokens, CommentValence valence)
	throws AlreadyExistsException{
		
		/* test choix id idea par index */
		
		validateAsIdeaComment(playerId, ideaId, text);
		
		String shortText = new StringBuilder("on ")
				.append(ideas.get(ideaId).getShortName())
				.append(" (by ")
				.append(players.get(playerId).getShortName())
				.append(')')
				.toString();
		
		

		System.out.println("AJOUT COMMENT injectIdea1");
/*		CommentValence valence =
			(tokens==0)?CommentValence.NEUTRAL : 
				(tokens>0)? CommentValence.POSITIVE : CommentValence.NEGATIVE;*/
		IComment comment = new Comment(playerId, ideaId, shortText, text, valence, tokens); 

		comment.setIndexSource(ideas.get((Integer)ideaId).getIndex());
		
		int id = comment.getUniqueId();
		
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(comment);
		ideaComments.get(ideaId).add(node);
		fastCommentLookup.put(id, node);

		try {
			manageTokensForCreatedComment(comment);
		} catch (RemoteException e) {
			logger.debug("remote exception on token management for comment "+comment);
		}
		
		return id;
	}
	
	protected void manageTokensForCreatedComment(IComment comment) throws RemoteException {
		
		if (comment.getTokensCount() == 0)
			return;	// quick exit
		
		
		logger.debug(comment.getTokensCount()+" token(s) included into this comment; ");
		
		//find the top idea.
		int commentedId = comment.get();
		IIdea idea = getIdea( findIdeaFromComment(commentedId) );
		int playerId = comment.getPlayerId();
		IPlayer player = getPlayer(playerId);
		idea.betChanged(comment.getTokensCount(),player, this);		
		
		player.tokensWereBet(comment.getTokensCount());
	}
	

	/**
	 * injects a comment in this Game, but do not fire an event<br/>
	 * it might be a comment over an idea, or an answer to another such comment.
	 * @param comment the comment to inject
	 * @return the unique identifier of this comment
	 * @throws AlreadyExistsException if a comment with the same ID exists
	 * @throws NullPointerException if the given comment is null
	 */
	protected final int injectIdeaComment(IComment comment) throws AlreadyExistsException, RemoteException {
		if(comment==null) throw new NullPointerException();
		if(fastCommentLookup.containsKey(comment.getUniqueId())) throw new AlreadyExistsException();

		// utile pour synchro et ajout local
		System.out.println("AJOUT COMMENT injectIdea2 - " + comment.getUniqueId());
		
		int id = comment.getUniqueId();
		int commentedId = 0;

		for (IIdea idea : ideas)
		{
			if (idea.getIndex().equals(comment.getIndexSource()))
			{
				commentedId = idea.getUniqueId();
				break;
			}
		}
		
		
		System.out.println("source : " + commentedId);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(comment);
		
		if(fastCommentLookup.containsKey(commentedId)){
			System.out.println("CONTAINS KEY");
			//this is an answer to another comment
			fastCommentLookup.get(commentedId).add(node);
			fastCommentLookup.put(id, node);
		} else {
			System.out.println("DO NOT CONTAINS KEY");
			//the commented id doesn't refer to a comment
			ideaComments.get(commentedId).add(node);
			fastCommentLookup.put(id, node);
			//fireCommentCreatedEvent(new GameObjectEvent(comment.getPlayerId(), comment.getUniqueId()));
		}
		
		manageTokensForCreatedComment(comment);
		
		return id;
	}
	
	protected final int injectCommentAnswer(int playerId, int commentId, String text, int tokens)
	throws AlreadyExistsException, RemoteException  {
		//cool, every comment is in the same look-up!
		String shortText = new StringBuilder("after previous comment ")
		.append(
			( (IComment) fastCommentLookup.get(commentId).getUserObject() ).getShortName()
		)
		.append(" (by ")
		.append(players.get(playerId).getShortName())
		.append(')')
		.toString();
		
		CommentValence valence = (tokens==0)?
				CommentValence.NEUTRAL:
				( (tokens>0)? CommentValence.POSITIVE: CommentValence.NEGATIVE );
		IComment comment = new Comment(playerId, commentId, shortText, text, valence, tokens); 
		
		int id = comment.getUniqueId();
		
		fastCommentLookup.put(id, new DefaultMutableTreeNode(comment));
		
		manageTokensForCreatedComment(comment);
		
		return id;
	}
	
	
	/*
	 * player
	 */
	
	/**
	 * @param player
	 * @return
	 * @throws AlreadyExistsException
	 */
	protected final int injectPlayer(IPlayer player) throws AlreadyExistsException{
		if(players.containsKey(player.getUniqueId())) throw new AlreadyExistsException();
		players.put(player.getUniqueId(), player);
		int id = player.getUniqueId();
		try {
			calculerScore(id);
		} catch (RemoteException e) {
			logger.error("error while computing scores: ",e);
		}
		return id;
	}

	/**
	 * @param playerID id of the player to ban
	 * @return the player data of this player
	 */
	protected final IPlayer ejectPlayer(int playerID){
		//maybe a lock is needed, look at it
		return players.get(playerID);
	}
	
	/*
	 * scores
	 */

	protected void calculerScore(TypeScore type, int playerId) throws RemoteException{
		players.get(playerId).setScore( type, type.calculer(this, playerId) );
	}

	protected void calculerScore(int playerId) throws RemoteException{
		IPlayer p = players.get(playerId);
		if(p==null) return;
		
		for(TypeScore type : TypeScore.values())
			p.setScore( type, type.calculer(this, playerId) );
	}
	
	
	
	/*
	 * partial implementation of IGame (injectors skipped)
	 */

	/* (non-Javadoc)
	 * @see functions.IGame#getDescription()
	 */
	@Override
	public final IGameDescription getDescription() throws RemoteException {
		return new GameDescription(description);
	}

	@Override
	public int getMaxTokensByPlayer() throws RemoteException {
		return INITIAL_TOKEN_COUNT_BY_PLAYER;
	}

	/*
	 * items
	 */
	
	/**
	 * Tells if an item is mapped on an identifier
	 * @param id the identifier to test
	 * @return true if an item is known with the given identifier
	 */
	public final boolean itemExists(int id){
		return items.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getItem(int)
	 */
	@Override
	public final IItem getItem(int itemId) throws RemoteException {
		return items.get(itemId);
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getAllItemsIds()
	 */
	@Override
	public final LinkedList<Integer> getAllItemsIds() throws RemoteException {
		return new LinkedList<Integer>(items.keySet());
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getAllItems()
	 */
	@Override
	public final LinkedList<IItem> getAllItems() throws RemoteException {
		return new LinkedList<IItem>(items.values());
	}

	/*
	 * ideas
	 */
	
	/**
	 * @param id
	 * @return
	 */
	public final boolean ideaExists(int id){
		return ideas.exists(id);
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getRootIdea()
	 */
	@Override
	public final IIdea getRootIdea() throws RemoteException {
		return ideas.get(rootIdeaId);
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getIdea(int)
	 */
	@Override
	public final IIdea getIdea(int ideaId) throws RemoteException {
		return ideas.get(ideaId);
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getAllIdeas()
	 */
	@Override
	public final LinkedList<IIdea> getAllIdeas() throws RemoteException {
		LinkedList<IIdea> res = new LinkedList<IIdea>();
		Iterator<IIdea> i = ideas.iterator();
		while(i.hasNext()) res.add(i.next());
		return res;
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getIdeaParentIds(int)
	 */
	public final LinkedList<Integer> getIdeaParentIds(int ideaId) throws RemoteException{
		return new LinkedList<Integer>(ideas.getParentsIds(ideaId));
	}

	/*
	 * comments 
	 */
	
	@Override
	final public int commentIdea(int authorId, int ideaId, String text) 
			throws RemoteException {
		return commentIdea(authorId, ideaId, text, 0, CommentValence.NEUTRAL);
	}

	@Override
	final public int answerComment(int authorId, int commentId, String text) 
			throws RemoteException {
		return answerComment(authorId, commentId, text, 0);
	}
	
	
	public final boolean commentExists(int id){
		return fastCommentLookup.containsKey(id);
	}
	
	/* (non-Javadoc)
	 * @see functions.IGame#getComment(int)
	 */
	@Override
	public final IComment getComment(int commentId) throws RemoteException {
		try{
			return (IComment) fastCommentLookup.get(commentId).getUserObject();
		} catch (NullPointerException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getAllComments()
	 */
	@Override
	public final LinkedList<IComment> getAllComments() throws RemoteException {
		LinkedList<IComment> res = new LinkedList<IComment>();
		for (DefaultMutableTreeNode n : fastCommentLookup.values()) {
			res.add( (IComment) n.getUserObject());
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getIdeaComments(int)
	 */
	@Override
	public LinkedList<IComment> getIdeaMainComments(int ideaId) throws RemoteException {
		Enumeration<?> e = ideaComments.get(ideaId).children();
		LinkedList<IComment> res = new LinkedList<IComment>();
		while(e.hasMoreElements()) {
			res.add( (IComment) ((DefaultMutableTreeNode)e.nextElement()).getUserObject() );
		}
		
		return res;
	}

	public DefaultMutableTreeNode getIdeaComments(int ideaId) throws RemoteException {
		
		return ideaComments.get(ideaId);
	}
	
	
	@Override
	public LinkedList<IComment> getAllIdeasComments(int ideaId)
			throws RemoteException {
		
		LinkedList<IComment> comments = new LinkedList<IComment>();
		
		// AD Enumeration enumComments = ideaComments.get(ideaId).children(); /* code original
		Enumeration<?> enumComments = ideaComments.get(ideaId).children(); // */
		
		while (enumComments.hasMoreElements()) {
			
			// AD Enumeration enumComments2 = ((DefaultMutableTreeNode)enumComments.nextElement()).depthFirstEnumeration();
			Enumeration<?> enumComments2 = ((DefaultMutableTreeNode)enumComments.nextElement()).depthFirstEnumeration(); // */
			while (enumComments2.hasMoreElements()) {
				IComment currentComment = (IComment)((DefaultMutableTreeNode)enumComments2.nextElement()).getUserObject();
				comments.add(currentComment);
					
			}
				
		}
		
		return comments;
		
	}

	
	public long getDateCreation() throws RemoteException {
		return getRootIdea().getCreationDate();
	}
	
	
	/*
	 * players 
	 */

	/* (non-Javadoc)
	 * @see functions.IGame#getPlayer(int)
	 */
	@Override
	public final IPlayer getPlayer(int playerId) throws RemoteException {
		return players.get(playerId);
	}

	/* (non-Javadoc)
	 * @see functions.IGame#getAllPlayers()
	 */
	@Override
	public final LinkedList<IPlayer> getAllPlayers() throws RemoteException {
		return new LinkedList<IPlayer>(players.values());
	}
	
	/* (non-Javadoc)
	 * @see functions.IGame#getAllPlayersIds()
	 */
	@Override
	public final LinkedList<Integer> getAllPlayersIds() throws RemoteException {
		return new LinkedList<Integer>(players.keySet());
	}
	
	/*
	 * IListenable 
	 */
	
	/* (non-Javadoc)
	 * @see events.IListenable#addListener(events.IEventListener)
	 */
	@Override
	public final void addListener(IEventListener l) throws RemoteException {
		listeners.add(IEventListener.class, l);
		addListenerAction();
	}
	
	protected void addListenerAction(){}

	/* (non-Javadoc)
	 * @see events.IListenable#removeListener(events.IEventListener)
	 */
	@Override
	public final void removeListener(IEventListener l) throws RemoteException {
		listeners.remove(IEventListener.class, l);
		removeListenerAction();
	}

	protected void removeListenerAction(){}
	
	protected final void firePlayerJoinedEvent(int id) throws RemoteException {
		firePlayerJoinedEvent(new PlayerEvent(id));
	}
	
	protected final void firePlayerJoinedEvent(PlayerEvent event) throws RemoteException {
		if(event==null) throw new NullPointerException();
		Object[] list = listeners.getListenerList();
		for (int i = list.length-2; i>=0; i-=2) {
			try {
			if(list[i]==IEventListener.class) {
				((IEventListener)list[i+1]).playerJoined(event);
			}
			} catch (Exception e) {
				logger.warn("Caught error during the diffusion of events; removing this listener", e);
				listeners.remove(IEventListener.class, (IEventListener)list[i+1]);
			}
		}
	}

	protected final void firePlayerLeftEvent(int id) throws RemoteException {
		firePlayerLeftEvent(new PlayerEvent(id));
	}
	
	protected final void firePlayerLeftEvent(PlayerEvent event) throws RemoteException {
		if(event==null) throw new NullPointerException();
		Object[] list = listeners.getListenerList();
		for (int i = list.length-2; i>=0; i-=2) {
			try {
			if (list[i]==IEventListener.class)
			((IEventListener)list[i+1]).playerLeft(event);
			} catch (Exception e) {
				logger.warn("Caught error during the diffusion of events; removing this listener", e);
				listeners.remove(IEventListener.class, (IEventListener)list[i+1]);
			}
		}
	}

	protected final void fireItemCreatedEvent(int authorId, int id)
	throws RemoteException {
		fireItemCreatedEvent( new GameObjectEvent(authorId, id) );
	}
	
	protected final void fireItemCreatedEvent(GameObjectEvent event)
	throws RemoteException {
		if(event==null) throw new NullPointerException();
		Object[] list = listeners.getListenerList();
		for (int i = list.length-2; i>=0; i-=2) {
			try {
			if (list[i]==IEventListener.class)
			((IEventListener)list[i+1]).ItemCreated(event);
			} catch (Exception e) {
				logger.warn("Caught error during the diffusion of events; removing this listener", e);
				listeners.remove(IEventListener.class, (IEventListener)list[i+1]);
			}
		}
	}
	
	protected final void fireIdeaCreatedEvent(int authorId, int id)
	throws RemoteException {
		fireIdeaCreatedEvent(new GameObjectEvent(authorId, id));
	}
	
	protected final void fireIdeaCreatedEvent(GameObjectEvent event)
	throws RemoteException {
		if(event==null) throw new NullPointerException();
		Object[] list = listeners.getListenerList();
		for (int i = list.length-2; i>=0; i-=2) {
			try {
			if (list[i]==IEventListener.class)
			((IEventListener)list[i+1]).IdeaCreated(event);
			} catch (Exception e) {
				logger.warn("Caught error during the diffusion of events; removing this listener", e);
				listeners.remove(IEventListener.class, (IEventListener)list[i+1]);
			}
		}
	}

	protected final void fireIdeaLinkCreatedEvent(int authorId, Collection<Pair<Integer, Integer>> links)
	throws RemoteException {
		fireIdeaLinkCreatedEvent( new LinkEvent(authorId, links) );
	}
	
	protected final void fireIdeaLinkCreatedEvent(LinkEvent event)
	throws RemoteException {
		if(event==null) throw new NullPointerException();
		Object[] list = listeners.getListenerList();
		for (int i = list.length-2; i>=0; i-=2) {
			try {
			if (list[i]==IEventListener.class)
			((IEventListener)list[i+1]).IdeaLinkCreated(event);
			} catch (Exception e) {
				logger.warn("Caught error during the diffusion of events; removing this listener", e);
				listeners.remove(IEventListener.class, (IEventListener)list[i+1]);
			}
		}
	}

	
	protected final void fireCommentCreatedEvent(int authorId, int id)
	throws RemoteException {
		fireCommentCreatedEvent(new GameObjectEvent(authorId, id));
	}
		
	protected final void fireCommentCreatedEvent(GameObjectEvent event)
	throws RemoteException {
		if(event==null) throw new NullPointerException();
		Object[] list = listeners.getListenerList();
		for (int i = list.length-2; i>=0; i-=2) {
			try {
			if (list[i]==IEventListener.class)
			((IEventListener)list[i+1]).ideaCommentCreated(event);
			} catch (Exception e) {
				logger.warn("Caught error during the diffusion of events; removing this listener", e);
				listeners.remove(IEventListener.class, (IEventListener)list[i+1]);
			}
		}
	}


	
	/**
	 * "This game is ending."
	 * Every listener must get kicked form here, as this game will probably be deleted
	 * @throws RemoteException
	 */
	protected final void fireEndOfGame() throws RemoteException {
		Object[] list = listeners.getListenerList();
		for (int i = list.length-2; i>=0; i-=2) {
			try {
			if (list[i]==IEventListener.class)
				((IEventListener)list[i+1]).endOfGame();
			} catch (Exception e) {
				logger.warn("Caught error during the diffusion of events; removing this listener", e);
			} finally {//listener must get removed
				listeners.remove(IEventListener.class, (IEventListener)list[i+1]);
			}
		}
	}
	
	@Override
	public String status() throws RemoteException{
		StringBuilder sb = new StringBuilder(description.toString());
		
		sb.append("\n\tjoueurs:");
		for (IPlayer i : players.values()) {
			sb.append("\n\t").append(i.toString());
		}
		
		sb.append("\n\titems:");
		for (IItem i : items.values()) {
			sb.append("\n\t").append(i.toString());
		}
		
		sb.append("\n\tideas:");
		for (IIdea i : ideas) {
			sb.append("\n\t").append(i.toString());
		}
		
		return sb.toString();
	}
	


	@Override
	public int currentBids(Integer ideaID, Integer playerID) {
		
		Enumeration<?> e = ideaComments.get(ideaID).children();
		int total = 0;
		while(e.hasMoreElements()) {
			IComment currentComment = (IComment) ((DefaultMutableTreeNode)e.nextElement()).getUserObject();
			if (currentComment.getPlayerId() == playerID)
				total += currentComment.getTokensCount();
		}
		
		return total;
		
	}
	
	@Override
	public int currentBids(IIdea idea, IPlayer player) {
		
		return currentBids(idea.getUniqueId(), player.getUniqueId());
		
	}
	

}