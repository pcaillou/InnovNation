package events;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IListenable extends Remote {
	void addListener(IEventListener l) throws RemoteException;
	void removeListener(IEventListener l) throws RemoteException;
	
}
