package client.gui.prefuse;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import prefuse.Display;

/**
 * This thread monitors the performance of a prefuse Display (notably framerate), and raise
 * events in case of low perf). May be used to automatically switch to a low quality display. 
 * Automatically starts when someone is listening, and stops when no more listener. 
 * 
 * @author Samuel Thiriot
 *
 */
public class ThreadMonitorPrefusePerformance extends Thread {

	/**
	 * Implement this interface, listen for your threadMonitorPrefusePerformance, 
	 * and you will be warned in case of repeated low efficiency.
	 * 
	 * @author Samuel Thiriot
	 *
	 */
	public interface IMonitorPrefusePerformanceListener {
		
		public void notifyLowPerformance(String value);
		
	}
	
	private Logger logger = Logger.getLogger("client.gui.prefusemonitor");
	
	private Display prefuseDisplay;
	
	public long delay = 1000;
	
	
	public double thresholdFrameRateMin = 20;
	public int thresholdCount = 20;
	
	public int count = 0;
	
	private LinkedList<IMonitorPrefusePerformanceListener> listeners = new LinkedList<IMonitorPrefusePerformanceListener>();
	
	public ThreadMonitorPrefusePerformance(Display prefuseDisplay) {
		
		this.prefuseDisplay = prefuseDisplay;
	
		setDaemon(true);
		setName("prefusePerfMonitor");
	}
	
	/**
	 * Restarts the thread if it was stopped.
	 * @param list
	 */
	public void addListener(IMonitorPrefusePerformanceListener list) {
		
		synchronized (listeners) {
			
			if (!listeners.contains(list)) {
				listeners.add(list);
				
				if (!isAlive()) {
					logger.info("listener added, starting the prefuse performance monitor");
					start();
				}
			}
		}
	}
	
	/**
	 * Stops the thread if no more listener
	 * @param list
	 */
	public void removeListener(IMonitorPrefusePerformanceListener list) {
		
		synchronized (listeners) {
			listeners.remove(list);
		
			if (listeners.isEmpty()) {
				logger.info("no more listener; interrupting the thread");
				this.interrupt();
			}
		}
	}
	
	public void notifyListeners(String msg) {
	
		synchronized (listeners) {
			
			for (IMonitorPrefusePerformanceListener listener : listeners) {
				
					listener.notifyLowPerformance(msg);
				
			}
				
		}
		
		// reset count
		logger.debug("listeners were notified; resetting count");
		count = 0;
		
	}
	
	public void run() {
	
		String msg = null;
		
		count = 0;
		
		logger.debug("starting");
		while (true) {
			
			final double frameRate = prefuseDisplay.getFrameRate();
			if (frameRate < thresholdFrameRateMin) {
			
				
				if (count > thresholdCount) {
			
					msg = "framerate < "+thresholdFrameRateMin;
					logger.info("raising a low framerate alert (now "+frameRate+", "+count+" alerts)");
					
				} else {
					logger.debug("low framerate detected (now "+frameRate+", "+count+" alerts / "+thresholdCount+")");
					count ++ ;
				}
			}
			
			if (msg != null)
				notifyListeners(msg);
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				logger.debug("stopped by interruption");
				return;
			}
		}
		
	}
	
}
