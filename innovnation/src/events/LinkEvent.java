/**
 * 
 */
package events;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import util.Pair;


/**
 * an event to propagate a list of links (a:parent, b:child)
 * @author Pierre Marques
 */
public class LinkEvent implements Event, Iterable<Pair<Integer, Integer>>{
	private static final long serialVersionUID = 1L;
	
	private int playerId;

	private final LinkedList<Pair<Integer, Integer>> list;
	
	
	/**
	 * @param authorId
	 */
	public LinkEvent(int authorId) {
		this.playerId = authorId;
		list = new LinkedList<Pair<Integer, Integer>>();
	}

	/**
	 * @param e
	 */
	public LinkEvent(LinkEvent e) {
		this.playerId = e.playerId;
		list = new LinkedList<Pair<Integer, Integer>>(e.list);
	}
	
	/**
	 * @param authorId
	 * @param links
	 */
	public LinkEvent(int authorId, Collection<Pair<Integer, Integer>> links) {
		this.playerId = authorId;
		list = new LinkedList<Pair<Integer, Integer>>(links);
	}

	public void add(int parentId, int childId) {
		list.add(new Pair<Integer, Integer>(parentId, childId));
	}
	/* (non-Javadoc)
	 * @see events.Event#getPlayerId()
	 */
	@Override
	public int getPlayerId() {
		return playerId;
	}

	
	@Override
	public Iterator<Pair<Integer, Integer>> iterator() {
		return list.iterator();
	}

	
}
