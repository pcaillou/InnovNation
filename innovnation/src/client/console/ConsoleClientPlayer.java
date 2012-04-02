package client.console;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import util.CommandListener;
import util.RMIUtils;
import data.CommentValence;
import data.IIdea;
import data.IItem;
import data.IPlayer;
import data.IStorable;
import events.GameObjectEvent;
import events.PlayerEvent;
import functions.GameServer;


//TODO remove IPlayer interface, this is not a server data...
public class ConsoleClientPlayer extends ConsoleClientBasic {

	private static final long serialVersionUID = 1L;

	private String name;
	
	/**
	 * @param name
	 * @throws RemoteException
	 * @throws UnknownHostException 
	 */
	public ConsoleClientPlayer(String name) throws RemoteException, UnknownHostException {
		super("player.console");

		this.name=name;
		
		commandManager.addCommandListener(new CommandListener("nick",
				"nick nom",
				"change votre nom, si vous n'êtes pas connecté."
		) {
			@Override
			public void command(Collection<String> arguments) {
				try{
					ConsoleClientPlayer.this.name = arguments.iterator().next();
				} catch (Exception e){
					logger.info("refused: "+e.getMessage());
				}
			}
		});
		
		commandManager.addCommandListener(new CommandListener("item",
				"nom description",
				"crée un item, si le serveur l'accepte."
		) {
			@Override
			public void command(Collection<String> arguments) {
				try{
					Iterator<String> i = arguments.iterator();
					String itemName = i.next();
					String itemDescr = i.next();
					getGame().addItem(core.getPlayerId(), itemName, itemDescr);
				} catch (Exception e){
					logger.info("refused: "+e.getMessage());
				}
			}
		});
		
		commandManager.addCommandListener(new CommandListener("idea",
				"nom <parents> / <items>",
				"crée une idée, si le serveur l'accepte. le premier parent et le '/' est obligatoire..."
		) {
			@Override
			public void command(Collection<String> arguments) {
				try{
					String ideaName, s;
					LinkedList<Integer> itemsIds = new LinkedList<Integer>();
					LinkedList<Integer> ideasIds = new LinkedList<Integer>();
					
					//separate
					Iterator<String> i = arguments.iterator();
					
					ideaName = i.next();
					
					//
					s=i.next();
					
					while(i.hasNext() && !s.equals("/") ){
						try{
							ideasIds.add(Integer.parseInt(s));
						} catch (NumberFormatException e){
							logger.error(s+" is not an id");
						}
						s=i.next();
					}
					
					while(i.hasNext()){
						s=i.next();
						try{
							itemsIds.add(Integer.parseInt(s));
						} catch (NumberFormatException e){
							logger.error(s+" is not an id");
						}
					}
					//inject
					
					getGame().addIdea(core.getPlayerId(), ideaName, "", itemsIds, ideasIds);


				} catch (Exception e){
					logger.info("refused",e);
				}
			}
		});

		commandManager.addCommandListener(new CommandListener("link",
				"idee <parents>",
				"lie une idée à chacun des nouveaux parents."
		) {
			@Override
			public void command(Collection<String> arguments) {
				try{
					Iterator<String> i = arguments.iterator();
					int ideaId  = Integer.parseInt(i.next());
					
					LinkedList<Integer> parents= new LinkedList<Integer>();
					
					while(i.hasNext()){
						try{
							parents.add(Integer.parseInt(i.next()));
						} catch (NumberFormatException e){
						}
					}
					
					getGame().makeIdeaChildOf(core.getPlayerId(), ideaId, parents);
				} catch (Exception e){
					logger.info("refused: "+e.getMessage());
				}
			}
		});
	
		commandManager.addCommandListener(new CommandListener("comment",
				"idee <tokens> texte",
				"crée un commentaire. Permet aussi de répondre à un commentaire."
		) {
			
			@Override
			public void command(Collection<String> arguments) {
				//arguments should be: commentedId text
				//or: commentId tokens text
				try{
					Iterator<String> i = arguments.iterator();
					int id = Integer.parseInt(i.next());
					String text = i.next();
					int tokens = 0;
					try{
						tokens = Integer.parseInt(text);
						text = i.next();
					} catch (NumberFormatException e){
						//can't convert the second argument to a token count
					}
					try{
						core.spendTokens(tokens);
						if(getGame().getIdea(id)!=null){
							getGame().commentIdea(core.getPlayerId(), id, text, tokens, CommentValence.NEUTRAL);
						} else {
							getGame().answerComment(core.getPlayerId(), id, text, tokens);
						}
					} catch (IllegalStateException e) {
						logger.error("you can't spend so many tokens:\n"+e.getMessage());
					}
				} catch (NumberFormatException e){
					logger.error("the given id was not an id.");
				} catch (Exception e){
					logger.info("refused: ", e);
				}
			}
		});
	
		commandManager.addCommandListener(new CommandListener("tokens",
				"tells how many tokens you can still spend"
		) {
			@Override
			public void command(Collection<String> arguments) {
				logger.info("you have this many tokens left: "+core.getRemainingTokens());
			}
		});
	}

	/*
	 * Connection management
	 */

	protected void connectToGame()
	throws IllegalArgumentException {
		try {
			int myID = getGame().addPlayer(name);
			this.core.setPlayerId(myID);
			logger.info("Your ID is "+myID);
		} catch (RemoteException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected void disconnectFromGame()
	throws IllegalArgumentException {
		try {
			if(core.getPlayerId() != IStorable.notAnId) getGame().removePlayer(core.getPlayerId());
		} catch (RemoteException e) {
			throw new IllegalArgumentException("could not disconnect", e); 
		}
	}

	/*
	 * Running
	 */


	public static void main (String[] args) {
		String url;
		if(args.length>0) url = args[args.length-1];
		else {
			try {
				url = "rmi://"+RMIUtils.getLocalHost().getHostAddress()+"/"+GameServer.SERVER_NAME;
			} catch (UnknownHostException uhe) {
				System.out.println(uhe.getMessage());
				return;
			}
		}

		//System.setSecurityManager(new RMISecurityManager());
		try {
			ConsoleClientPlayer player = new ConsoleClientPlayer("sam");
			try{
				player.fetchServer(url);
			} catch (IllegalArgumentException e){
				player.logger.error(e);
			}
			player.start();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	//reaction to events
	
	@Override
	public void playerJoined(PlayerEvent e) throws RemoteException {
		IPlayer p = getGame().getPlayer(e.getPlayerId());
		logger.info("a player joined: "+p);
	}

	@Override
	public void playerLeft(PlayerEvent e) throws RemoteException {
		IPlayer p = getGame().getPlayer(e.getPlayerId());
		logger.info("a player left: "+p);
	}


	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException {
		IPlayer p = getGame().getPlayer(e.getPlayerId());
		IItem i = getGame().getItem(e.getObjectId());
		logger.info("a new item ("+i+") was proposed by "+p );
	}

	@Override
	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
		IPlayer p = getGame().getPlayer(e.getPlayerId());
		IIdea i = getGame().getIdea(e.getObjectId());
		logger.info("a new idea ("+i+") was proposed by "+p );
	}

	@Override
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {
		logger.info("a new comment is made");
	}

	public Map<IIdea, Integer> getTokensBets() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void tokensWereBet(int nbTokens) throws RemoteException {
		core.spendTokens(nbTokens);
	}

}