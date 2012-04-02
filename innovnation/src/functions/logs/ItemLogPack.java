/**
 * 
 */
package functions.logs;

import data.IComment;
import data.IIdea;
import data.IItem;


/**
 * @author Pierre Marques
 *
 */
public class ItemLogPack implements LogPack {
	private int creationTime;
		
	
	public ItemLogPack(int time) {
		creationTime = time;
	}
	
	static public String titles() {
		return "itemCreationTime;";
	}

	static public String zeros() {
		return "0;";
	}
	
	/* (non-Javadoc)
	 * @see functions.logs.LogPack#log()
	 */
	@Override
	public String log(int time) {
		StringBuilder sb = new StringBuilder();
		sb.append(creationTime).append(';');
		return sb.toString();
	}

	@Override
	public void updateOnItem(int playerId, IItem createdItem) {
		if(createdItem==null) throw new NullPointerException();
		//nothing, really, this is not a missing feature
	}
	
	@Override
	public void updateOnIdea(int playerId, IIdea createdIdea) {
		if(createdIdea==null) throw new NullPointerException();
		//nothing, really, this is not a missing feature
	}

	@Override
	public void updateOnComment(int playerId, IComment createdComment) {
		if(createdComment==null) throw new NullPointerException();
		//nothing, really, this is not a missing feature
	}
}
