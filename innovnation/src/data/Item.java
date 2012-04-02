/**
 * 
 */
package data;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author marques
 *
 */
public class Item extends GameObject implements IItem, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String description;
	
	/**
	 * @param authorId
	 * @param shortName
	 */
	public Item(int authorId, String shortName, String description) {
		super(authorId, shortName);
		this.description=description;
	}

	/* (non-Javadoc)
	 * @see data.IItem#getDescription()
	 */
	@Override
	public final String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see data.IItem#getAllHosts()
	 */
	@Override
	public LinkedList<Integer> getAllHosts() {
		//TODO to do this, we need a way to tell which idea use this item
		throw new UnsupportedOperationException("still needs implementation");
	}

	@Override
	public String toString(){
			return super.toString()+": "+description;
	}
}
