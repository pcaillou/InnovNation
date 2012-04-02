import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Une solution simple pour détruire violemment tous les jeux. 
 * @author Pierre Marques
 */
public class GameKiller {

	public static void main(String[] args) {
		try {
			for( String s : LocateRegistry.getRegistry().list()){
				if(s.contains("/GAME_")) Naming.unbind(s);
			}
		} catch (Throwable e) {
		}
	}

}
