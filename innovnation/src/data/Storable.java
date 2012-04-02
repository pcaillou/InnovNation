package data;

import java.io.Serializable;


/**
 * Does not garanty uniqueness of id, since it allow use of arbitrary id.
 * With normal use, 0 is garantied not to be a valid id.
 * @author marques
 *
 */
public abstract class Storable implements IStorable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static int lastId = notAnId;
	private static final int getNextId() {
		return lastId++;
	}
	
	protected final int id;
	private final String shortName;
	
	/**
	 * Empty constructor for deserialization
	 */
	protected Storable() {
		id = -1;
		shortName = null;
	}
	
	public Storable(String shortName) {
		id = getNextId();
		this.shortName=shortName;
	}

	public Storable(int id, String shortName) {
		this.id = id;
		this.shortName=shortName;
	}

	/* (non-Javadoc)
	 * @see data.IStorable#getUniqueId()
	 */
	@Override
	public final int getUniqueId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see data.IStorable#getShortName()
	 */
	@Override
	public final String getShortName(){
		return shortName;
	}
	
	@Override
	public String toString() {
		return shortName+" (id="+id+')';
	}
}
