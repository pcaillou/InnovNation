package events;

/**
 * Describes an event that was propagated and is temporarly stored 
 * for uncoupling purpose.
 *  
 * @author Samuel Thiriot
 *
 */
public class PendingEvent {
	
	public final EventType eventType;
	public final Object data;
	
	public PendingEvent(EventType eventType, Object data) {
		assert eventType != null;
		this.eventType = eventType;
		this.data = data;
	}
	
	public PendingEvent(EventType eventType) {
		assert eventType != null;
		this.eventType = eventType;
		this.data = null;
	}
	
	@Override
	public String toString() {
		return eventType.name()+","+data;
	}
	
}