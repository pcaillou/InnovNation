package data;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import util.Misc;

/**
 * Lists available avatars to be used by players. Lists the image files available in a directory,
 * which is supposed to contain the same pictures in the server and all the clients. 
 * SUpposed to deal both with regular filesystems and jar-distributed applications.
 * 
 * @author Samuel Thiriot
 *
 */
public class Avatars {

	//"."+File.separator+
	public final static String resourcePath = ""; //+File.separator;
	
	public final static Logger logger = Logger.getLogger("misc.avatars");
	
	
	private static List<String> availableAvatars = null;
	

	/**
	 * Updates the list of available avatars from filesystem (or JAR).
	 * 
	 */
	public static void refreshAvailableAvatars() {

		logger.debug("Refreshing the list of avatars available as a ressource into "+resourcePath);
		
		try {
			String[] children = Misc.getResourceListing(Avatars.class,resourcePath);
			LinkedList<String> res = new LinkedList<String>();

			for (String currentFile : children) {
				
				String currentType = Misc.getMimeType(currentFile);
				if ( (currentType != null) && currentType.startsWith("image"))
					res.add(currentFile);
				
			}
			
			//Collections.shuffle(res);
			availableAvatars = Collections.unmodifiableList(res);	
		
			logger.debug("Found avatars : "+availableAvatars.toString());
			
			return;
			
		} catch (URISyntaxException e) {
			logger.warn("unable to list the avatars in "+resourcePath, e);
		} catch (IOException e) {
			logger.warn("unable to list the avatars in "+resourcePath, e);
		}
		
		// in case of pb...

		availableAvatars = Collections.EMPTY_LIST;
	}
	
	/**
	 * returns the list of available avatars in the ressource path.
	 * 
	 * @return
	 */
	public static List<String> getAvailableAvatars() {
	
		if (availableAvatars == null)
			refreshAvailableAvatars();
		
		return availableAvatars;
	}
	
	/**
	 * NB: is case sensitive.
	 * @param avatar
	 * @return
	 */
	public static boolean doesAvatarExist(String avatar) {
		
		return getAvailableAvatars().contains(avatar);
	}
	
	/**
	 * Returns null if no avatar available, or one avatar picked up randomnly from the list.
	 * @return
	 */
	public static String getOneAvatarRandomly() {
		
		
		List<String> avatars = getAvailableAvatars();
		
		if (avatars.isEmpty())
			return null;
		else
			return avatars.get(Misc.random.nextInt(avatars.size()));
		
		
	}
	
	public static String getPathForAvatar(String avatar) {
		if (resourcePath.length()>1)
		return resourcePath+File.separator+avatar;
		else return avatar;
	}
	
}
