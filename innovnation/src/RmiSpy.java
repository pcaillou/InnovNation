import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


/**
 * une application listant le contenu du registre RMI
 * @author Pierre Marques
 */
public class RmiSpy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			for( String s : LocateRegistry.getRegistry().list()){
				System.out.println("found "+s);
			}
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
