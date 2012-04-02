package functions;

import java.io.Serializable;
import java.rmi.RemoteException;


/**
 * A game description interface, made so that games do not get serialized.
 * @author Pierre Marques
 */
public interface IGameDescription extends Serializable {
	
	/**
	 * Indicates what is the name of this game.
	 * @return a string representing the name of this game
	 * @throws RemoteException
	 */
	public String getName(); 

	/**
	 * Indicates what is the RMI name of this game.
	 * @return a string representing the RMI binding name
	 * @throws RemoteException
	 */
	public String getBindName();
	
	/**
	 * Indicates what is the theme of this game.
	 * @return a string representing the name of this game
	 * @throws RemoteException
	 */
	public String getTheme();
	
	
}
