package events;


public enum EventType {
	
	IDEA_CREATED,
	IDEA_LINK_CREATED,
	ITEM_CREATED,
	COMMENT_CREATED,
	PLAYER_JOINED,
	END_OF_GAME,
	PLAYER_LEFT;
	
	public PendingEvent getEvent() {
		return getEvent(null);
	}

	public PendingEvent getEvent(Object data) {
		
		switch (this) {
			case IDEA_CREATED:	// events for GameObjectEvents
			case ITEM_CREATED:
			case COMMENT_CREATED:
		
			case IDEA_LINK_CREATED:	// events for links
			
			case PLAYER_JOINED:	// events based on PlayerEvent
			case PLAYER_LEFT:
				return new PendingEvent(this, data);
				
			case END_OF_GAME:	// events without data
			
				if (data != null)
					throw new RuntimeException("No data allowed for event type "+this+".");
				return new PendingEvent(this);
				
			default: // notifies if code integrity wasn't ensured ("you suck" alert)
				throw new RuntimeException("Event type not supported: "+this+" (data provided: "+data+"); please update your code, thanks.");
				
				
		}
	}
	
}
