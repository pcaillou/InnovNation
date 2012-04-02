/**
 * 
 */
package util;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Pierre Marques
 *
 */
public abstract class AbstractShutDownable extends UnicastRemoteObject implements Shutdownable {
	private static final long serialVersionUID = 1L;
		
	/**
	 * @throws RemoteException
	 * @throws UnknownHostException 
	 */
	public AbstractShutDownable() throws RemoteException{
		super();
		new ShutDownHook(){
			@Override
			protected void action() {
				shutDownAction();
			}
		};
	}

	public final void shutDown(){
		System.exit(0);		
	}
	
	abstract protected void shutDownAction();
}
