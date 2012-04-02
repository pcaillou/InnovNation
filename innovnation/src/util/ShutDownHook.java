package util;

/**
 * a thread which is executed on runtime shutdown.
 * @author Pierre Marques
 */
public abstract class ShutDownHook extends Thread {
	
	public ShutDownHook() {
		super();
		Runtime.getRuntime().addShutdownHook(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public final void run() {
		action();
		try{
			Runtime.getRuntime().removeShutdownHook(this);
		} catch(IllegalStateException e){
			//this occurs when Hook is in process
		}
	}

	/**
	 * the method getting executed when runtime stops
	 */
	abstract protected void action();
}