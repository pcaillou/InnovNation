/**
 * 
 */
package util;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import functions.IGameServer;

/**
 * @author Pierre Marques
 *
 */
public abstract class AbstractRmiServer extends UnicastRemoteObject {
	
	private static final long serialVersionUID = 1L;
	
	private final String bindNamePrefix, bindName;

	private transient ShutDownHook ii;
	
	private final static String makeRmiBindName(String ip){
		return "rmi://"+ip+"/";
	}
	
	/**
	 * constructor on automatic localhost
	 * @throws RemoteException
	 * @throws MalformedURLException 
	 * @throws UnknownHostException if getLocalHost can't get resolved
	 */
	public AbstractRmiServer(String name) throws RemoteException, UnknownHostException, MalformedURLException {
		this(name, RMIUtils.getLocalHost().getHostAddress());
		
	}
	
	/**
	 * constructor on a specified ip
	 * @throws RemoteException
	 * @throws MalformedURLException 
	 */
	public AbstractRmiServer(String name, String ip) throws RemoteException, MalformedURLException {
		super();
		
		try {
			RMIUtils.ensureRegistryExists();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ii = new ShutDownHook(){
			@Override
			protected void action() {
				shutDownAction();
			}
		};
		
		this.bindNamePrefix=makeRmiBindName(ip);
		bindName=bindNamePrefix+name;
		
		
		System.out.println("binding server to "+bindName+"...");
		
		//try to shut down previously bound server
		try {
			Remote previousServerCatch = Naming.lookup(bindName);
			if(previousServerCatch instanceof IGameServer) {
				IGameServer previousServer = (IGameServer) previousServerCatch;
				System.out.println("remote shut down sent");
				previousServer.shutDown();
				System.out.println("remote shut down finished");
			} else {
				System.err.println("Something was bound here but was not a server. We erase it.");
			}
		} catch (NotBoundException e) {}

		//replace binding
		Naming.rebind(bindName, this);
	}
	
	public final String getBindNamePrefix() {
		return bindNamePrefix;
	}

	public final String getBindName() {
		return bindName;
	}

	public final void shutDown(){
		ii.action();
	}
	
	protected void shutDownAction(){
		try {
			Naming.unbind(bindName);
			unexportObject(this, true);
		} catch (MalformedURLException e) {
		} catch (NotBoundException e) {
		} catch (RemoteException e) {
		}
	}
}
