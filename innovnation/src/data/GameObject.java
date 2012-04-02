/**
 * 
 */
package data;

import java.io.Serializable;


/**
 * @author marques
 *
 */
public abstract class GameObject extends Storable implements IGameObject, Serializable {
	
	private static final long serialVersionUID = 1L;

	private final int authorId;
	private final long creationDate;
	
	
	/**
	 * Empty constructor for deserialization 
	 */
	protected GameObject() {
		authorId = 0;
		creationDate = 0;
	}

	/**
	 * @param shortName
	 */
	public GameObject(int authorId, String shortName) {
		super(shortName);
		creationDate=createCreationDate();
		this.authorId=authorId;
	}
	
	/* (non-Javadoc)
	 * @see data.IGameObject#getPlayer()
	 */
	@Override
	public final int getPlayerId(){
		return authorId;
	}

	/* (non-Javadoc)
	 * @see data.ITimable#getCreationDate()
	 */
	@Override
	public final long getCreationDate(){
		return creationDate;
	}

	private static final long createCreationDate() {
		return System.currentTimeMillis();
	}
	
}
