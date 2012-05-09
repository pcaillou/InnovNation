/**
 * 
 */
package functions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import data.Avatars;
import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;
import data.IStorable;
import data.Idea;
import data.Item;
import data.Player;
import errors.AlreadyExistsException;
import errors.TooLateException;
import events.LinkEvent;
import functions.logs.CommentLogPack;
import functions.logs.GameLogPack;
import functions.logs.GraphLogPack;
import functions.logs.IdeaLogPack;
import functions.logs.ItemLogPack;
import functions.logs.LogType;
import functions.logs.PlayerLogPack;

/**
 * This is an RMI server which dies when it has no more observer; 
 * @author Pierre Marques
 */
public class Game extends AbstractGame implements IServerSideGame {
	private static final long serialVersionUID = 1L;

	public static final String LOG_TIME_NAME = "time";
	public static final String LOG_TYPE_NAME = "type";
	
	private long startingTime;
	
	//utilitaires de log
	private FileWriter logFileWriter;
	
	private GameLogPack gameLP = new GameLogPack(this);
	private GraphLogPack graphLP = new GraphLogPack(this);
	private Map<Integer, PlayerLogPack> playerLPs = new HashMap<Integer, PlayerLogPack>();
	
	private Map<Integer, ItemLogPack> itemLPs = new HashMap<Integer, ItemLogPack>();
	private Map<Integer, IdeaLogPack> ideaLPs = new HashMap<Integer, IdeaLogPack>();
	
	private Map<Integer, CommentLogPack> commentLPs = new HashMap<Integer, CommentLogPack>();


	/**
	 * Construit une partie InnovNation
	 * @param descr
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws IOException if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
	 */
	public Game(IGameDescription descr) throws RemoteException, UnknownHostException, IOException {
		super(descr);
		
		/* on cree l'idee racine de la partie */
		int root = createRootIdea(new Idea(IStorable.notAnId, descr.getTheme(), "", ideas,null));

		/* on cree le fichier servant aux logs de la partie */
		logFileWriter = new FileWriter(new File(descr.getName()+".csv"));
		logTitles();
		
		/* on ajoute l'idee racine aux idees */
		ideaLPs.put(root, new IdeaLogPack(this, getIdea(root), 0));
		
		/* on recupere le temps actuel comme temps du debut de la partie */
		startingTime=System.currentTimeMillis();
	}


	/**
	 * tells what time is it since game started
	 * @return a positive time in second
	 */
	private int getNow() {
		return (int) ((System.currentTimeMillis()-startingTime) / 1000);
	}
	
	
	public int getTime()
	{
		return getNow();
	}
	
	/*
	 * IGame
	 */
	
	/* (non-Javadoc)
	 * @see functions.IGame#addItem(int, java.lang.String, java.lang.String)
	 */
	@Override
	public int addItem(int authorId, String itemName, String itemDescription)
	throws RemoteException {
		
		/* on cree l'item et on l'ajoute */
		IItem item = new Item(authorId, itemName, itemDescription);
		int id = injectItem(item);
		
		/* on ajoute aux logs du jeu l'ajout de l'item */
		itemLPs.put(id, new ItemLogPack(getNow()));
		
		/* on ajoute aux logs statistique l'ajout de l'item */
		try {
			log(authorId, LogType.item, id, 
				new StringBuilder(item.toString())
					.append(" by ")
					.append(getPlayer(authorId).toString())
					.toString()
			);
		} catch (IOException e) {
			//TODO what shall I do?
		}
		
		/* on lance un evenement pour dire que l'item est cree */
		fireItemCreatedEvent(authorId, id);
		return id;
	}

	/* (non-Javadoc)
	 * @see functions.IGame#addIdea(int, java.lang.String, java.util.Collection, java.util.Collection)
	 */
	@Override
	public int addIdea(int authorId, String ideaName, String desc, Collection<Integer> itemsIds, Collection<Integer> parentIdeasIds)
	throws AlreadyExistsException, TooLateException, RemoteException {
		
		/* on cree l'idee */
		int id = injectIdea(authorId, ideaName, desc, itemsIds, parentIdeasIds);

		/* on ajoute aux logs du jeu l'ajout de l'idee */
		ideaLPs.put(id, new IdeaLogPack(this, getIdea(id), getNow()));
		
		/* on joute aux logs statistique l'ajout de l'idee */
		try {
			log(authorId, LogType.idea, id, 
				new StringBuilder(getIdea(id).toString())
					.append(" by ")
					.append(getPlayer(authorId).toString())
					.toString()
			);
		} catch (IOException e) {
			//TODO what shall I do?
		}
		
		/* on lance un evenement indiquant la creation de l'idee */
		fireIdeaCreatedEvent(authorId, id);
		return id;
	}

	/* (non-Javadoc)
	 * @see functions.IGame#makeIdeaParentOf(int, int, java.util.Collection)
	 */
	@Override
	public void makeIdeaParentOf(int authorId, int parentId,
			Collection<Integer> ideasIds) throws TooLateException,
			RemoteException {
		/* on cree un evenement de creation de lien */
		LinkEvent event = new LinkEvent(authorId);
		
		for(int childId : ideasIds){
			try{
				/* on lie l'idee a son pere, et on ajoute le lien a l'evenement */
				linkIdeas(parentId, childId);
				event.add(parentId, childId);

				/* on ajoute la creation du lien aux logs du jeu et statistique */
				ideaLPs.get(childId).updateOnLinkToParent(authorId, getIdea(parentId));
				ideaLPs.get(parentId).updateOnLinkToChild(authorId, getIdea(childId));
			} catch(NullPointerException e){
			}
		}
		
		/* on envoie l'evenement de creation de link */
		fireIdeaLinkCreatedEvent(event);
	}

	/* (non-Javadoc)
	 * @see functions.IGame#makeIdeaChildOf(int, int, java.util.Collection)
	 */
	@Override
	public void makeIdeaChildOf(int authorId, int childId,
			Collection<Integer> ideasIds) throws TooLateException,
			RemoteException {
		LinkEvent event = new LinkEvent(authorId);
		
		for(int parentId : ideasIds){
			try{
				linkIdeas(parentId, childId);
				event.add(parentId, childId);
				ideaLPs.get(childId).updateOnLinkToParent(authorId, getIdea(parentId));
				ideaLPs.get(parentId).updateOnLinkToChild(authorId, getIdea(childId));
			} catch(NullPointerException e){
			}
		}
		
		fireIdeaLinkCreatedEvent(event);
	}

	
	/* (non-Javadoc)
	 * @see functions.IGame#addComment(int, int, java.lang.String)
	 */
	@Override
	public int commentIdea(int authorId, int ideaId, String text, int tokens, CommentValence valence)
			throws RemoteException {
		int id = injectIdeaComment(authorId, ideaId, text, tokens, valence);

		commentLPs.put(id, new CommentLogPack(this, getComment(id), getNow()));
		try {
			log(authorId, (tokens==0)?LogType.comment:LogType.vote, id, 
				new StringBuilder(getComment(id).toString())
					.append(" by ")
					.append(getPlayer(authorId).toString())
					.toString()
			);
		} catch (IOException e) {
			//TODO what shall I do?
		}
		
		fireCommentCreatedEvent(authorId, id);
		return id;
	}
	
	
	/* (non-Javadoc)
	 * @see functions.IGame#commentItem(int, int, java.lang.String)
	 */
	@Override
	public int commentItem(int authorId, int itemId, String text)
			throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see functions.IGame#answerComment(int, int, java.lang.String)
	 */
	@Override
	public int answerComment(int authorId, int commentId, String text, int tokens)
			throws RemoteException {
		int id = injectCommentAnswer(authorId, commentId, text, tokens);

		commentLPs.put(id, new CommentLogPack(this, getComment(id), getNow()));
		try {
			log(authorId, (tokens==0)?LogType.comment:LogType.vote, id, 
				new StringBuilder(getComment(id).toString())
					.append(" by ")
					.append(getPlayer(authorId).toString())
					.toString()
			);
		} catch (IOException e) {
			//what shall I do?
		}
		
		fireCommentCreatedEvent(authorId, id);
		return id;
	}

	/* (non-Javadoc)
	 * @see functions.IGame#addPlayer(java.lang.String)
	 */
	@Override
	public int addPlayer(String playerName) throws RemoteException {
		return addPlayer( playerName, Avatars.getOneAvatarRandomly() );
	}
	

	@Override
	public boolean testExistingPlayer(String playerName) throws RemoteException
	{
		boolean newplayer=true;
		int id=-1;
		for (IPlayer pl:this.getAllPlayers())
		{
			if (pl.getShortName().equals(playerName))
			{
				newplayer=false;
				id=pl.getUniqueId();
				System.out.println("existing player "+id);
			}
		}
		return (!newplayer);
		
	}
	@Override
	public int addPlayer(String playerName, String avatar)
			throws RemoteException {
		
		boolean newplayer=true;
		int id=-1;
		for (IPlayer pl:this.getAllPlayers())
		{
			if (pl.getShortName().equals(playerName))
			{
				newplayer=false;
				id=pl.getUniqueId();
				System.out.println("use existing player "+id);
			}
		}
		
		if (newplayer)
		{
		id = injectPlayer(new Player(playerName, avatar));
		graphLP.updateOnPlayer(id);
		}
		
		gameLP.updateOnPlayer(id);
		playerLPs.put(id, new PlayerLogPack(this,getPlayer(id), getNow()));
		
		firePlayerJoinedEvent(id);
		return id;
	}

	/* (non-Javadoc)
	 * @see functions.IGame#removePlayer(int)
	 */
	@Override
	public void removePlayer(int playerId) throws RemoteException {
		ejectPlayer(playerId);
		
		gameLP.updateOnPlayerLeft(playerId);
		
		firePlayerLeftEvent(playerId);
	}

	/*
	 * IShutdownable
	 */
	
	/* (non-Javadoc)
	 * @see util.Shutdownable#shutDown()
	 */
	@Override
	public void shutDown() throws RemoteException {
		
		try {
			logFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fireEndOfGame();
		super.terminate();
	}


	//logs scientifiques:
	private void updateLogDataOnItem(int playerId, int thingId) throws RemoteException{
		gameLP.updateOnItem(playerId, getItem(thingId));
		playerLPs.get(playerId).updateOnItem(playerId, getItem(thingId));
	}
	
	private void updateLogDataOnIdea(int playerId, int thingId) throws RemoteException{
		IIdea data = getIdea(thingId);
		gameLP.updateOnIdea(playerId, data);
		graphLP.updateOnIdea(playerId, data);
		playerLPs.get(playerId).updateOnIdea(playerId, data);
		
		for(int i : data.getItemsIds()){
			playerLPs.get(getItem(i).getPlayerId()).updateItemUsage(1);
		}
		
		for(IIdea i : data.getParents()){
			//as root has no owner, we can't update him... 
			ideaLPs.get(i.getUniqueId()).updateOnIdea(playerId, data);
			if(i != getRootIdea()){
				playerLPs.get(i.getPlayerId()).updateIdeaUsage(1);
			}
		}
	}

	private void updateLogDataOnComment(int playerId, int thingId) throws RemoteException{
		//int commentedIdea = findIdeaFromComment(thingId);
		IComment data = getComment(thingId);
		
		gameLP.updateOnComment(playerId, data);
		graphLP.updateOnComment(playerId, data);
		
		playerLPs.get(playerId).updateOnComment(playerId, data);
		
		//idea notification (commented one, its parents and maybe its children)
		//TODO only notify some ideas
		for (IdeaLogPack i : ideaLPs.values()) {
			i.updateOnComment(playerId, data);
		}
		
		//players' comment usage
	}
	
	private void logTitles() throws IOException{
		logFileWriter.append(LOG_TYPE_NAME +";");
		//now
		logFileWriter.append(LOG_TIME_NAME + ";");
		
		//game data
		logFileWriter.write(GameLogPack.titles());

		//graph data
		logFileWriter.write(GraphLogPack.titles());
		
		//player data
		logFileWriter.write(PlayerLogPack.titles());

		//item data
		logFileWriter.write(ItemLogPack.titles());
		
		//idea data
		logFileWriter.write(IdeaLogPack.titles());
		
		//à vérifier
		//idea sons data
		//logFileWriter.write(IdeaSonsLogPack.titles());
		
		//comment data
		logFileWriter.write(CommentLogPack.titles());

		logFileWriter.write("abstract;\n");
		logFileWriter.flush();
	}
	
	/**
	 * TODO may need to be synchronized
	 * @param playerId
	 * @param type
	 * @param thingId
	 * @throws IOException if the log writer emits an IOException
	 */
	private void log(int playerId, LogType type, int thingId, String logMessage) throws IOException{
		int now = getNow();
		//first, update datas
		
		//TODO do I need to update scores
		getPlayer(playerId).majScores(this);
		
		switch (type) {
		case item:
			updateLogDataOnItem(playerId, thingId);
			break;
		case idea:
			updateLogDataOnIdea(playerId, thingId);
			break;
		default:
			updateLogDataOnComment(playerId, thingId);
			break;
		}
		
		//then, print useful components
		
		//type
		logFileWriter.append(type.toString()).append(';');
		//now
		logFileWriter.append(Integer.toString(now)).append(';');
		
		//game data
		logFileWriter.write(gameLP.log(now));

		//graph data
		logFileWriter.write(graphLP.log(now));
		
		//player data
		logFileWriter.write(playerLPs.get(playerId).log(now));

		//item data
		logFileWriter.write(
			type==LogType.item? itemLPs.get(thingId).log(now): ItemLogPack.zeros()
		);
		
		//idea data
		if(type==LogType.item) logFileWriter.write( IdeaLogPack.zeros() );
		else {
			logFileWriter.write(
				(type==LogType.idea)?
					ideaLPs.get(thingId).log(now):
					ideaLPs.get( findIdeaFromComment(thingId) ).log(now)
			);
		}
		
		//à vérifier
		//idea sons data
		//(type==LogType.vote||type==LogType.comment)? ideaSonsLPs.get(thingId).log(now): IdeaSonsLogPack.zeros()
		
		//comment data
		logFileWriter.write(
			(type==LogType.vote||type==LogType.comment)? commentLPs.get(thingId).log(now): CommentLogPack.zeros()
		);
		
		logFileWriter.write(logMessage);
		logFileWriter.write('\n');
		
		for(IIdea i : this.getAllIdeas()){
			//as root has no owner, we can't update him... 
			//type
			logFileWriter.append("logi").append(';');
			//now
			logFileWriter.append(Integer.toString(now)).append(';');
			
			//game data
			logFileWriter.write(gameLP.log(now));
			
			//graph data
			logFileWriter.write(GraphLogPack.zeros());
			
			//player data
			logFileWriter.write(
					PlayerLogPack.zeros()
				);

			//item data
			logFileWriter.write(
				ItemLogPack.zeros()
			);
			
			//idea data
				logFileWriter.write(
						ideaLPs.get(i.getUniqueId()).log(now));
			
			//à vérifier
			//idea sons data
			//(type==LogType.vote||type==LogType.comment)? ideaSonsLPs.get(thingId).log(now): IdeaSonsLogPack.zeros()
			
			//comment data
			logFileWriter.write(
				CommentLogPack.zeros()
			);
			
			
			logFileWriter.write(logMessage);
			logFileWriter.write('\n');
		}
		
		HashMap<Integer, String> graphLogs = graphLP.getLogpLogs(now);
		
		for(int p : this.getAllPlayersIds()){
			//type
			logFileWriter.append("logp").append(';');
			//now
			logFileWriter.append(Integer.toString(now)).append(';');
			
			//game data
			logFileWriter.write(gameLP.log(now));

			//graph data
			String t = graphLogs.get(p);
			logFileWriter.write(t);
			
			//player data
			logFileWriter.write(playerLPs.get(p).log(now));

			
			//item data
			logFileWriter.write(
				ItemLogPack.zeros()
			);			

			
			//idea data
			logFileWriter.write(
					IdeaLogPack.zeros()
				);			

			
			//à vérifier
			//idea sons data
			//(type==LogType.vote||type==LogType.comment)? ideaSonsLPs.get(thingId).log(now): IdeaSonsLogPack.zeros()
			
			//comment data
			logFileWriter.write(
				CommentLogPack.zeros()
			);
			
			System.out.println("Comment : " + CommentLogPack.titles().split(";").length + "/" + CommentLogPack.zeros().split(";").length   + "/" + CommentLogPack.zeros().split(";").length );

			
			logFileWriter.write(logMessage);
			logFileWriter.write('\n');
			//as root has no owner, we can't update him... 
		}
		
		
		logFileWriter.flush();
		
		//mettre à jour les données de temps
		playerLPs.get(playerId).noticeAction(now);
	}
	
	
}
