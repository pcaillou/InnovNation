/**
 * 
 */
package functions;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import util.AbstractRmiServer;
import errors.AlreadyExistsException;
import errors.RequestRefusedException;

/**
 * @author Pierre Marques
 *
 */
public class GameServer extends AbstractRmiServer implements IGameServer, Serializable {
	
	/**
	 * GameServers will always bind this name.
	 */
	public final static String SERVER_NAME = "testServer";
	
	private static final long serialVersionUID = 1L;
	protected static final String PREFIX_RMI_GAME = "GAME_";
	protected final String name;

	protected Map<String, IGameDescription> games = new LinkedHashMap<String, IGameDescription>();

	transient protected final Logger logger;



	
	/**
	 * @param name
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 */
	public GameServer(String name, String ip) throws RemoteException,
			UnknownHostException, MalformedURLException {
		super(name, ip);
		
		this.name=name;
		logger = Logger.getLogger("GameServer:"+name);
	}
	



	/**
	 * @throws RemoteException
	 * @throws UnknownHostException 
	 * @throws AlreadyBoundException 
	 * @throws MalformedURLException 
	 */
	public GameServer(String name) throws RemoteException, UnknownHostException, MalformedURLException, AlreadyBoundException{
		super(name);

		this.name=name;
		logger = Logger.getLogger("GameServer:"+name);
	}
	
	protected static void hammer(Throwable t){
		if(t==null)return;
		System.err.println(t.getMessage());
		for(StackTraceElement s : t.getStackTrace())
			System.err.println("\t"+s.toString());
		hammer(t.getCause());
	}

	
	/* (non-Javadoc)
	 * @see util.AbstractRmiServer#shutDownAction()
	 */
	@Override
	public final void shutDownAction(){
		System.out.println(getBindName()+" is asked to die.");
		if (games != null) {
			for( String gameBindName : games.keySet()){
				try {
					releaseGame(gameBindName);
				} catch (MalformedURLException e) {
					hammer(e);
				} catch (NotBoundException e) {
					hammer(e);
				}
			}
		}
		super.shutDownAction();
	}


	/* (non-Javadoc)
	 * @see functions.IGameServer#getOpenGames()
	 */
	@Override
	public Collection<IGameDescription> getOpenGames() throws RemoteException {
		List<IGameDescription> res = new LinkedList<IGameDescription>(games.values());
		return res;
	}

	/* (non-Javadoc)
	 * @see functions.IGameServer#createGame(java.lang.String, java.lang.String)
	 */
	@Override
	public String createGame(String name, String theme)
	throws RemoteException, MalformedURLException, AlreadyExistsException, UnknownHostException, RequestRefusedException {
		String gameBindName = getBindNamePrefix()+PREFIX_RMI_GAME+this.name+"_"+name;
		try{
			IGameDescription descr = new GameDescription(gameBindName, name, theme);
			IGame game = new Game(descr);

			Naming.bind(gameBindName, game);
			games.put(descr.getBindName(), descr);

			System.out.println("game "+descr.getName()+" about "+descr.getTheme()+" created at\n\t"+gameBindName);
		} catch (AlreadyBoundException e) {
			throw new AlreadyExistsException(gameBindName, e);
		} catch (IOException e) {
			throw new RequestRefusedException(gameBindName, e);
		}
		return gameBindName;
	}

	public void recoverGame(String gameBindName){
		try {
			System.out.println("recovering game at "+gameBindName);
			IGame g = (IGame) Naming.lookup(gameBindName);
			games.put(gameBindName, g.getDescription());
			System.out.println("\tdone");
		} catch (MalformedURLException e) {
			hammer(e);//e.printStackTrace();
		} catch (RemoteException e) {
			hammer(e);//e.printStackTrace();
		} catch (NotBoundException e) {
			hammer(e);//e.printStackTrace();
		}
	}

	public void releaseGame(String gameBindName) throws NotBoundException, MalformedURLException{
		try {
			logger.info("releasing game at "+gameBindName);
			logger.debug("lookup...");
			IServerSideGame g = (IServerSideGame) Naming.lookup(gameBindName);
			logger.debug("shutting down...");
			g.shutDown();
			logger.debug("removing from server's games list...");
			games.remove(gameBindName);
			logger.info("done");
		} catch (RemoteException e) {
			hammer(e);//e.printStackTrace();
		}
	}

	 
	
}
