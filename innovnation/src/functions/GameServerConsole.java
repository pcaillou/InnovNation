/**
 * 
 */
package functions;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import util.CommandListener;
import util.CommandManager;
import errors.AlreadyExistsException;
import events.EventAdapter;
import events.GameObjectEvent;
import events.IEventListener;
import events.PlayerEvent;

/**
 * @author Pierre Marques
 *
 */
public class GameServerConsole extends GameServer {

	private static final long serialVersionUID = 1L;


	transient protected final CommandManager commandManager = new CommandManager();


	private final IEventListener gameObserver = new EventAdapter() {
		private static final long serialVersionUID = 1L;

		@Override
		public void playerLeft(PlayerEvent e) {
			System.out.println("\tPlayer left: ("+e.getPlayerId()+")");
		}

		@Override
		public void playerJoined(PlayerEvent e) {
			System.out.println("\tPlayer joined: ("+e.getPlayerId()+")");
		}

		@Override
		public void IdeaCreated(GameObjectEvent e) {
			System.out.println("\tnew idea");
		}

		@Override
		public void ItemCreated(GameObjectEvent e) {
			System.out.println("\tnew item "+e.getObjectId());
		}
	};

	/**
	 * @param name
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 */
	public GameServerConsole(String name, String ip) throws RemoteException,
			UnknownHostException, MalformedURLException {
		super(name, ip);
		initialize();
	}
	
	/**
	 * @throws RemoteException
	 * @throws UnknownHostException 
	 * @throws AlreadyBoundException 
	 * @throws MalformedURLException 
	 */
	public GameServerConsole(String name) throws RemoteException, UnknownHostException, MalformedURLException, AlreadyBoundException{
		super(name);

	
		initialize();
	}
	
	private void initialize(){

		commandManager.addCommandListener(new CommandListener("games","") {
			@Override
			public void command(Collection<String> arguments) {
				if(arguments.isEmpty()){
					for(IGameDescription g : games.values()){
						logger.info(g.toString());
					}
				} else {
					for(IGameDescription g : games.values()){
						try {
							if(arguments.contains(g.getBindName()) || arguments.contains(g.getName())){
								IGame game = (IGame) Naming.lookup(g.getBindName());
								logger.info(game.status());
							}
						} catch (MalformedURLException e) {
							logger.error("can't describe: "+g, e);
						} catch (RemoteException e) {
							logger.error("can't describe: "+g, e);
						} catch (NotBoundException e) {
							
						}
					}
				}
			}
		});

		commandManager.addCommandListener(new CommandListener("new","") {
			@Override
			public void command(Collection<String> arguments) {
				if(arguments.size()>=2){
					Iterator<String> i = arguments.iterator();
					String name=i.next();
					String rootIdea=i.next();
					try {
						createGame(name, rootIdea);
					} catch (RemoteException e) {
						logger.error(e);
					} catch (MalformedURLException e) {
						logger.error(e);
					} catch (AlreadyExistsException e) {
						logger.error(e);
					} catch (UnknownHostException e) {
						logger.error(e);
					}
				}
			}
		});

		commandManager.addCommandListener(new CommandListener("kill","") {
			@Override
			public void command(Collection<String> arguments) {
				for (String argument : arguments) {
					try{
						releaseGame(argument);
					} catch (MalformedURLException e){
						logger.error(e);
					} catch (NotBoundException e) {
						logger.error(e);
					}
				}
			}
		});
		
		commandManager.addCommandListener(new CommandListener("quit","") {
			@Override
			public void command(Collection<String> arguments) {
				System.exit(0);
			}
		});

		commandManager.addAlias("fetch", "games");
		commandManager.addAlias("list", "games");

		commandManager.addAlias("newgame", "new");
		commandManager.addAlias("create", "new");
		commandManager.addAlias("game", "new");
		commandManager.addAlias("exit", "quit");

		new Thread(commandManager).start();

	}


	@Override
	public String createGame(String name, String theme) 
	throws RemoteException, MalformedURLException, AlreadyExistsException, UnknownHostException {
		
		String res = super.createGame(name, theme);
		try {
			((IGame)Naming.lookup(res)).addListener(gameObserver);
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;

	}

	public void recoverGame(String gameBindName){
		

		try {
			super.recoverGame(gameBindName);
			((IGame)Naming.lookup(gameBindName)).addListener(gameObserver);
			

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	

	@Override
	public void releaseGame(String gameBindName) throws NotBoundException, MalformedURLException{
	
		try {
			IServerSideGame g = (IServerSideGame) Naming.lookup(gameBindName);
			logger.debug("unlistening...");
			g.removeListener(gameObserver);
		} catch (RemoteException e) {
			hammer(e);//e.printStackTrace();
		}
		
		super.releaseGame(gameBindName);
	}
	
	public static void main(String[] args) {
		System.setSecurityManager(new RMISecurityManager());
		try {
			if(args.length>0){
				new GameServerConsole(SERVER_NAME, args[args.length-1]);
			} else {
				new GameServerConsole(SERVER_NAME);
			}
		} catch (AlreadyBoundException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
