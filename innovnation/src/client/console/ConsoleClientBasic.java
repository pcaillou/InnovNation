package client.console;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import util.AbstractShutDownable;
import util.CommandListener;
import util.CommandManager;
import client.DelegatingClientCore;
import client.IClientCore;
import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;
import functions.IGame;
import functions.IGameDescription;
import functions.IGameServer;

public abstract class ConsoleClientBasic extends AbstractShutDownable implements IEventListener {
	
	private static final long serialVersionUID = 1L;

	transient protected IClientCore core;
	
	transient protected final Logger logger;
	
	transient protected final CommandManager commandManager;
	
	IClientCore.TreeExplorator ideaCommentDisplayer = new IClientCore.TreeExplorator(){
		/* (non-Javadoc)
		 * @see client.IClientCore.TreeExplorator#start(java.lang.Object)
		 */
		@Override
		public void start(String startMessage) {
			logger.info("commentaires de "+startMessage);
		}

		/* (non-Javadoc)
		 * @see client.IClientCore.TreeExplorator#end(java.lang.Object)
		 */
		@Override
		public void end(String endMessage) {
			//nothing for the moment
		}

		/* (non-Javadoc)
		 * @see client.IClientCore.TreeExplorator#work(javax.swing.tree.DefaultMutableTreeNode, int)
		 */
		@Override
		public void work(DefaultMutableTreeNode node, int depth) {
			if( depth!=0 ){
				IComment com = (IComment) node.getUserObject();
				
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i<depth;++i) sb.append(" | ");
				try {
					sb.append(getGame().getPlayer(com.getPlayerId()).getShortName());
					sb.append(": ");
				} catch (RemoteException e) {
					sb.append("?: ");
				}

				sb.append(" +> ").append( com.getText() )
				.append(' ').append(com.getTokensCount())
				.append('[').append(com.getValence()==CommentValence.NEGATIVE?'-':'+').append(']');
				
//					sb.append(" (by ").append(getGame().getPlayer(com.getPlayerId()).getShortName()).append(')');
				logger.info(sb.toString());
				
			}
			
			@SuppressWarnings("unchecked")
			Enumeration<DefaultMutableTreeNode> children = node.children();
			while(children.hasMoreElements()) {
				work(children.nextElement(), depth+1);
			}
		}
		
	}; 
	
	public ConsoleClientBasic(String logStreamName) throws RemoteException, UnknownHostException {
		super();
		core = new DelegatingClientCore(this);
		
		logger = Logger.getLogger(logStreamName);
		
		commandManager = new CommandManager();

		commandManager.addCommandListener( new CommandListener( "test",
				"connection automatique au server testServer local"
		) {
			@Override
			public void command(Collection<String> arguments) {
				final String target = "GAME_testServer_test";
				try{
					core.connectToGame(target);
					connectToGame();
				} catch (Exception e) {
					if(e.getCause()!=null)
						logger.error(target+": "+e.getMessage()+" ("+e.getCause().getMessage()+")");
					else logger.error(target+": "+e.getMessage());
				}
			}
		});
		
		commandManager.addCommandListener( new CommandListener( "fetch",
				"server < server <...> >",
				"liste les jeux disponible sur chaque serveur."
		) {
			@Override
			public void command(Collection<String> arguments) {
				for(String argument: arguments){
					try{
						fetchServer(argument);
					} catch (IllegalArgumentException e) {
						logger.info(argument+": "+e.getMessage()+" ("+e.getCause().getMessage()+")");
						logger.debug(e.getCause());
					}
				}
			}
		});
		
		commandManager.addCommandListener( new CommandListener( "items",
				"<ids>",
				"liste tous les items ou affiche les items indiqués."
		) {
			@Override
			public void command(Collection<String> arguments) {
				IGame game = core.getGame();
				if(game==null) return;
				if(arguments.isEmpty()){
					logger.info("known items are:");
					try {
						for(IItem i : game.getAllItems()) logger.info(i);
					} catch (RemoteException e) {
						logger.error("command items: ",e);
					}
				} else {
					for(String a : arguments){
						try {
							IItem i = game.getItem(Integer.parseInt(a));
							if(i!=null)logger.info("item "+a+" is "+i);
						} catch (Exception e) {
							logger.error("no item found at id "+a);
						}
					}
				}
			}
		});
		
		commandManager.addCommandListener( new CommandListener( "ideas",
				"<ids>",
				"liste toutes les idées ou affiche les idées indiquées."
		) {
			@Override
			public void command(Collection<String> arguments) {
				IGame game = core.getGame();
				if(game==null) return;
				if(arguments.isEmpty()){
					logger.info("known ideas are:");
					try {
						for(IIdea i : game.getAllIdeas()) logger.info(i);
					} catch (RemoteException e) {
						logger.error("command \"ideas\": ",e);
					}
				} else {
					for(String a : arguments){
						try {
							IIdea i = game.getIdea(Integer.parseInt(a));
							if(i!=null) logger.info("idea "+a+" is "+i);
						} catch (Exception e) {
							logger.error("no idea found at id "+a);
						}
					}
				}
			}
		});

		commandManager.addCommandListener( new CommandListener( "comments",
				"<ids>",
				"liste tous les commentaires ou affiche les commentaires indiqués. BROKEN"
		) {
			/** 
			 * Affiche tous les commentaires liés aux arguments fournis
			 * @see util.CommandListener#command(java.util.Collection)
			 */
			@Override
			public void command(Collection<String> arguments) {
				IGame game = core.getGame();
				if(game==null) return;
				if(arguments.isEmpty()){
					logger.info("you are supposed to give some idea identifiers");
				} else {
					for(String a : arguments){
						try {
							core.displayIdeaComments(Integer.parseInt(a), ideaCommentDisplayer);
						} catch (Exception e) {
							logger.error("no idea found at id "+a, e);
						}
					}
				}
			}
		});
		
		commandManager.addCommandListener( new CommandListener( "players",
				"<ids>",
				"liste tous les joueurs ou affiche les joueurs indiqués."
		) {
			@Override
			public void command(Collection<String> arguments) {
				IGame game = core.getGame();
				if(game==null) return;
				if(arguments.isEmpty()){
					logger.info("known players are:");
					try {
						for(IPlayer i : game.getAllPlayers()) logger.info(i);
					} catch (RemoteException e) {
						logger.error("command players: ",e);
					}
				} else {
					for(String s : arguments){
						try {
							IPlayer i = game.getPlayer(Integer.parseInt(s));
							if(i!=null)logger.info("player "+s+" is "+i);
						} catch (Exception e) {
							logger.error("no player found at id "+s);
						}
					}
				}
			}
		});
		
		
		//connects to the first argument
		commandManager.addCommandListener( new CommandListener( "connect",
				"jeu",
				"permet de se connecter au jeu indiqué"
		) {
			@Override
			public void command(Collection<String> arguments) {
				if(arguments.isEmpty()) return;

				String target = arguments.iterator().next();
				try{
					core.connectToGame(target);
					connectToGame();
				} catch (Exception e) {
					if(e.getCause()!=null)
						logger.error(target+": "+e.getMessage()+" ("+e.getCause().getMessage()+")");
					else logger.error(target+": "+e.getMessage());
				}
			}
		});
		
		commandManager.addCommandListener( new CommandListener( "quit",
						"ferme le programme."
		) {
			@Override
			public void command(Collection<String> arguments) {
				System.exit(0);//provoque l'éxecution de shutDownAction.
			}
		});
		
		commandManager.addAlias("join", "connect");
		commandManager.addAlias("exit", "quit");
	}

	public IGame getGame(){
		return core.getGame();
	}
	
	public final void start(){
		new Thread(commandManager).start();
	}
	
	
	public void shutDownAction(){
		core.disconnectFromGame();
		disconnectFromGame();
		logger.info("I am asked to die.");
	}
	
	/*
	 * command definitions
	 */

	public final void fetchServer(String serverBindName) {
		logger.info("fetching server...");
		try {
			logger.debug("lookup for server...");
			IGameServer server = (IGameServer) Naming.lookup(serverBindName);
			
			Collection<IGameDescription> games = server.getOpenGames();

			logger.info("Open games on "+serverBindName+":");
			for (IGameDescription game : games) {
				logger.info(game.getName()+" about "+game.getTheme()+"\n\twas founded at "+game.getBindName());
			}
			
			fetchServerAction(server);
			
			logger.debug("server fetched...");
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid server url", e);
		} catch (RemoteException e) {
			throw new IllegalArgumentException("unable to lookup server", e);
		} catch (NotBoundException e) {
			throw new IllegalArgumentException("no server there", e);
		}
	}
	
	protected void fetchServerAction(IGameServer server)
	throws IllegalArgumentException {}


	protected void connectToGame(){}

	protected void disconnectFromGame(){}

	/*
	 * IEventListener implementation
	 */

	@Override
	public void playerLeft(PlayerEvent e) throws RemoteException  {
		logger.info("a player left");
	}

	@Override
	public void playerJoined(PlayerEvent e) throws RemoteException  {
		logger.info("a player joined");
	}

	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException  {
		logger.info("a new item was proposed ("+e.getObjectId()+" by player"+e.getPlayerId()+")");
	}

	@Override
	public void IdeaCreated(GameObjectEvent e) throws RemoteException  {
		logger.info("a new item was proposed ("+e.getObjectId()+" by player"+e.getPlayerId()+")");
	}

	
	/* (non-Javadoc)
	 * @see events.IEventListener#IdeaLinkCreated(events.LinkEvent)
	 */
	@Override
	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
		logger.info("some new links are made by "+e.getPlayerId());
	}

	@Override
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException  {
		logger.info("a new comment is made");
	}
	

	/* (non-Javadoc)
	 * @see events.IEventListener#endOfGame()
	 */
	@Override
	public void endOfGame() throws RemoteException {
		logger.info("your game has ended!");
		disconnectFromGame();
		core.disconnectFromGame();
	}


}
