package util;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.log4j.Logger;

/**
 * Various utils for the Innov Nation project.
 * 
 * permission java.security.AllPermission "", "";
permission java.net.SocketPermission "192.168.0.5:1024-", "listen,connect,resolve"; 


 * @author Samuel Thiriot
 *
 */
public class Misc {
	public final static Logger logger = Logger.getLogger("misc.avatars");

	  public static String[] getResourceListing(String path) throws java.net.URISyntaxException, java.io.IOException {
		  return getResourceListing(path.getClass(), path);
	  }
	  
	  /**
	   * List directory contents for a resource folder. Not recursive.
	   * This is basically a brute-force implementation.
	   * Works for regular files and also JARs.
	   * 
	   * @author Greg Briggs
	   * @param clazz Any java class that lives in the same place as the resources you want.
	   * @param path Should end with "/", but not start with one.
	   * @return Just the name of each member item, not the full paths.
	   * @throws URISyntaxException 
	   * @throws IOException 
	   */
	  public static String[] getResourceListing(@SuppressWarnings("rawtypes") Class clazz, String path) throws java.net.URISyntaxException, java.io.IOException { // */
	     
		  ClassLoader cl = clazz.getClassLoader();
		  if (cl == null)
			  cl = ClassLoader.getSystemClassLoader();
		  
		  URL dirURL = cl.getResource(path);
		  
			if (dirURL!=null) logger.debug("avatar dir1 "+dirURL.toString());
	      if (dirURL != null && dirURL.getProtocol().equals("file")) {
	        /* A file path: easy enough */
	        return new File(dirURL.toURI()).list();
	      } 

	      if (dirURL == null) {
	        /* 
	         * In case of a jar file, we can't actually find a directory.
	         * Have to assume the same jar as clazz.
	         */
	        String me = clazz.getName().replace(".", "/")+".class";
	        dirURL = cl.getResource(me);
	      }
			logger.debug("avatar dir "+dirURL.toString());
	      if (dirURL.getProtocol().equals("jar")) {
	        /* A JAR path */
	        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
	        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
	        while(entries.hasMoreElements()) {
	          String name = entries.nextElement().getName();
	          if (name.startsWith(path)) { //filter according to the path
	            String entry = name.substring(path.length());
	            int checkSubdir = entry.indexOf("/");
	            if (checkSubdir >= 0) {
	              // if it is a subdirectory, we just return the directory name
	              entry = entry.substring(0, checkSubdir);
	            }
	            result.add(entry);
	          }
	        }
			logger.debug("avatar res "+result.size()+" det "+result.toString());
	        
	        return result.toArray(new String[result.size()]);
	      } 
	        
	      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
	  }
	  
	public static String getMimeType(String fileUrl) throws java.io.IOException
    {
      FileNameMap fileNameMap = URLConnection.getFileNameMap();
      String type = fileNameMap.getContentTypeFor(fileUrl);

      return type;
    }
	
	public static final Random random = new Random(System.currentTimeMillis());
	
}
