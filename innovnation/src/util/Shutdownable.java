package util;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Some process that has something to do at shutdown
 *
 * @author Pierre Marques
 */
public interface Shutdownable extends Remote{
	/**
	 * method to call on shutdown
	 * @throws RemoteException
	 */
	void shutDown() throws RemoteException;

}