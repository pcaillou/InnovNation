package client.console;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import util.CommandListener;
import util.RMIUtils;
import functions.GameServer;
import functions.IGame;
import functions.IGameDescription;
import functions.IGameServer;

public class ConsoleClientDisplay extends ConsoleClientBasic {
	private static final long serialVersionUID = 1L;

	transient private IGameServer remoteGameServer = null;
	
	public ConsoleClientDisplay(String firstServerUrl) throws RemoteException, UnknownHostException {
		super("client.console");
		
		commandManager.addCommandListener(new CommandListener("monitor","") {
			@Override
			public void command(Collection<String> arguments) {
				monitorAllOpenGames();
			}
		});
		
		commandManager.addCommandListener(new CommandListener("unmonitor","") {
			@Override
			public void command(Collection<String> arguments) {
				unmonitorAllOpenGames();
			}
		});
		
		commandManager.addCommandListener(new CommandListener("list","") {
			@Override
			public void command(Collection<String> arguments) {
				displayOpenGames();
			}
		});

		commandManager.addAlias("watch", "list");
		commandManager.addAlias("display", "list");
		
		commandManager.addAlias("leave", "unmonitor");
		
		fetchServer(firstServerUrl);
		monitorAllOpenGames();
	}
	
	public void fetchServerAction(IGameServer server) throws IllegalArgumentException{
		disconnectServer();
		remoteGameServer=server;
		logger.debug("connected to server...");

	}
	
	public void disconnectServer() {
		logger.info("disconnect from previous server...");
		unmonitorAllOpenGames();
		remoteGameServer=null;
	}
	
	public void displayOpenGames() {
		if(remoteGameServer==null) return;
		try {
			Collection<IGameDescription> games = remoteGameServer.getOpenGames();
			
			System.out.println("Open games: ");
			for (IGameDescription game : games) {
				System.out.println(game.getName());
			}
			
		} catch (RemoteException e) {
			throw new RuntimeException("unable to list the games",e);
		}
		
	}
	
	public void monitorAllOpenGames() {
		if(remoteGameServer==null) return;
		try {
			Collection<IGameDescription> games = remoteGameServer.getOpenGames();
			for (IGameDescription game : games) {
				try {
					IGame g = (IGame) Naming.lookup(game.getBindName());
					g.addListener(this);
					logger.info("monitoring "+game.getBindName());
				} catch (MalformedURLException e) {
				} catch (NotBoundException e) {
				}
			}
			
		} catch (RemoteException e) {
			throw new RuntimeException("unable to list the games",e);
		}
		
	}
	
	public void unmonitorAllOpenGames() {
		if(remoteGameServer==null) return;
		try {
			Collection<IGameDescription> games = remoteGameServer.getOpenGames();
			for (IGameDescription game : games) {
				try {
					IGame g = (IGame) Naming.lookup(game.getBindName());
					g.removeListener(this);
				} catch (MalformedURLException e) {
				} catch (NotBoundException e) {
				}
			}
			
		} catch (RemoteException e) {
			throw new RuntimeException("unable to list the games",e);
		}
		
	}	
	
	/*
	 * IEventListener implementation
	 */

	@Override
	public void shutDownAction() {
		disconnectServer();
		super.shutDownAction();
	}

	public static void main (String[] args) {
		String url;
		if(args.length>0) url = args[args.length-1];
		else{
			try {
				url = "rmi://"+RMIUtils.getLocalHost().getHostAddress()+"/"+GameServer.SERVER_NAME;
			} catch (UnknownHostException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
		//System.setSecurityManager(new RMISecurityManager());
		try {
			new ConsoleClientDisplay(url).start();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
