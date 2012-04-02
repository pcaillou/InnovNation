package events;

import java.rmi.RemoteException;
import java.util.Stack;

import org.apache.log4j.Logger;


/**
 * Receives events in a non blocking way, 
 * and transmits them to a listener in a decoupled way.
 * 
 * Supposed to be as reactive as possible !
 * 
 * @author Samuel Thiriot
 *
 */
public class UncoupledListener extends Thread implements IEventListener {
	
	private Logger logger = Logger.getLogger("client.uncouple");
	
	private final IEventListener listener;
	
	private final Object lock = new Object();
	
	private Stack<PendingEvent> pendingEvents = new Stack<PendingEvent>();
	

	/**
	 * 
	 * @param listener the listener which will receive messages in a uncoupled way
	 */
	public UncoupledListener(IEventListener listener) {
		
		assert listener != null;
		
		this.listener = listener;
		setDaemon(true);
		setName("decoupleEvents"+listener.toString());
		setPriority(MAX_PRIORITY);
		
		logger.debug("created thread "+getName());
		
		start();
	}
	
	protected void fireEvent(PendingEvent pendingEvent) throws RemoteException {
		
		switch (pendingEvent.eventType) {
		
		case COMMENT_CREATED:
			listener.ideaCommentCreated((GameObjectEvent) pendingEvent.data);
			break;

		case END_OF_GAME:
			listener.endOfGame();
			break;
			
		case IDEA_CREATED:
			listener.IdeaCreated((GameObjectEvent) pendingEvent.data);
			break;
		
		case ITEM_CREATED:
			listener.ItemCreated((GameObjectEvent) pendingEvent.data);
			break;
		
		case IDEA_LINK_CREATED:		
			listener.IdeaLinkCreated((LinkEvent) pendingEvent.data);
			break;
		
		case PLAYER_JOINED:		
			listener.playerJoined((PlayerEvent) pendingEvent.data);
			break;
		
		case PLAYER_LEFT:		
			listener.playerLeft((PlayerEvent) pendingEvent.data);
			break;
			
		default:
			break;
			
		}
		
	}

	
	public void run() {
	
		logger.debug("starting...");
		
		while (true) {
			
			
			try {
				
				logger.debug("processing...");
				
				// there is probably something to process
				while (true) { // loop until no more pending events
					PendingEvent pendingEvent = null;
					synchronized (listener) {
						if (!pendingEvents.isEmpty())
							pendingEvent = pendingEvents.pop();
					}
					if (pendingEvent == null)
						break;
					
					try {
						logger.debug("processing "+pendingEvent);
						
						fireEvent(pendingEvent);
					} catch (RemoteException e) {
						
						logger.error("error during forwarding, event will be lost, sorry :-(", e);
						
					}
				}
					
				logger.debug("waiting...");
				synchronized (lock) {
					lock.wait();
				}
				
			} catch (InterruptedException e) {

				logger.warn("was interrupted o_O", e);
				
			} catch (RuntimeException e) {
				logger.error("Catched an exception while relaying an event.", e);
			}
			
		}
	}
	
	private void queueEvent(PendingEvent pendingEvent) {
	
		logger.debug("queing "+pendingEvent);
		synchronized (pendingEvents) {
			pendingEvents.add(pendingEvent);	
		}
		synchronized (lock) {
			lock.notify();

		}

			
	}
	
	@Override
	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
		
		queueEvent(EventType.IDEA_CREATED.getEvent(e));
		
	}

	@Override
	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
		queueEvent(EventType.IDEA_LINK_CREATED.getEvent(e));

		
	}

	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException {

		queueEvent(EventType.ITEM_CREATED.getEvent(e));

		
	}

	@Override
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {
	
		queueEvent(EventType.COMMENT_CREATED.getEvent(e));

		
	}

	@Override
	public void endOfGame() throws RemoteException {
		queueEvent(EventType.END_OF_GAME.getEvent());		

		
	}

	@Override
	public void playerJoined(PlayerEvent e) throws RemoteException {
		queueEvent(EventType.PLAYER_JOINED.getEvent(e));

		
	}

	@Override
	public void playerLeft(PlayerEvent e) throws RemoteException {
	
		queueEvent(EventType.PLAYER_LEFT.getEvent(e));

		
	}

}
