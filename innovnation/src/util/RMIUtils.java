package util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * 
 * Various utilities for using RMI in an efficient way.
 * 
 * @author Samuel Thiriot 
 */
public class RMIUtils {

	public static final boolean DO_NOT_LIST_IPV6_ADDRESSES = true;
	
	/**
	 * Returns an InetAddress representing the address of the localhost.  
	 * Every attempt is made to find an address for this host that is not 
	 * the loopback address.  If no other address can be found, the 
	 * loopback will be returned.
	 * 
	 * Retrieved from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037
	 * 
	 * @return InetAddress - the address of localhost
	 * @throws UnknownHostException - if there is a problem determing the address
	 * @throws java.net.UnknownHostException 
	 */
	public static InetAddress getLocalHost() throws java.net.UnknownHostException {
		
		InetAddress localHost = InetAddress.getLocalHost();
		if(!localHost.isLoopbackAddress()) 
			return localHost;
		InetAddress[] addrs = getAllLocalUsingNetworkInterface();
		for(int i=0; i<addrs.length; i++) {
			if(!addrs[i].isLoopbackAddress()) 
				return addrs[i];
		}
		return localHost;	
	}
	
	/**
	 * Utility method that delegates to the methods of NetworkInterface to 
	 * determine addresses for this machine.
	 * 
	 * Retrieved from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037
	 * and modified for removing IPv6 which don't always work for RMI.
	 * 
	 * @return InetAddress[] - all addresses found from the NetworkInterfaces
	 * @throws UnknownHostException - if there is a problem determining addresses
	 */
	private static InetAddress[] getAllLocalUsingNetworkInterface() throws java.net.UnknownHostException {
		ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
		Enumeration<NetworkInterface> e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException ex) {
			throw new UnknownHostException("127.0.0.1");
		}
		while(e.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface)e.nextElement();
				
			for(InterfaceAddress currentInterface : ni.getInterfaceAddresses()) {
			
				InetAddress currentAddress = currentInterface.getAddress();
				
				if (!DO_NOT_LIST_IPV6_ADDRESSES || (currentAddress instanceof Inet4Address))
					addresses.add(currentAddress);
			}	
		}
		InetAddress[] iAddresses = new InetAddress[addresses.size()];
		for(int i=0; i<iAddresses.length; i++) {
			iAddresses[i] = (InetAddress)addresses.get(i);
		}
		return iAddresses;
	}
	

	private static Logger logger = Logger.getLogger("rmi.registryAuto");
	
	/**
	 * Avoids automatically created registry to be garbage collected.
	 */
	private static Registry reg = null;
	
	private static void createRegistry() {
		
		try {
			reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			
		} catch (RemoteException e) {
			logger.error("unable to create registry :-(", e);
		}
		
	}
	
	public static void ensureRegistryExists() throws UnknownHostException {
		ensureRegistryExists(getLocalHost().getHostAddress());
	}
	
	private static boolean registryExists(String host) {
		try {
			reg = LocateRegistry.getRegistry(host);
			reg.list();
			return true;
		} catch (RemoteException e) {
			return false;
		}
	}
	
	public static void ensureRegistryExists(String host) {
		
		if (registryExists(host)) {
			logger.debug("registry already running !");
		} else {
			logger.debug("registry not operational, creating one...");
			createRegistry();
			if (!registryExists(host))
				logger.debug("attempted to create the registry, but still not working, sorry");
				
		}
		
	}
	
	
}
