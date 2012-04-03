/**
 * 
 */
package functions.logs;

// AD import java.rmi.RemoteException;

import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IItem;
import functions.IGame;


/**
 * @author Pierre Marques
 *
 */
public class CommentLogPack implements LogPack {
	private int creationTime, size, rank, valence,tokens, inceptions, outceptions;
	@SuppressWarnings("unused")
	private IGame game;
		
	
	public CommentLogPack(IGame game, IComment comment, int time) {
		this.game = game;
		this.valence=0;
		if (comment.getValence().equals(CommentValence.NEGATIVE)) valence=-1;
		if (comment.getValence().equals(CommentValence.POSITIVE)) valence=1;
		creationTime = time;
		size = comment.getText().length();
		rank=0;
		inceptions = 0;
		outceptions = 0;
/*		IIdea i;
		try {
			i = game.getIdea(comment.get());
			int rk=0;
			if (comment.getTokensCount()!=0)
			for (IComment c:i.getComments())
			{
				if (c.getTokensCount()*comment.getTokensCount()>0)
				{
					rk++;
					
				}
				if (c.getTokensCount()*comment.getTokensCount()<0)
				{
					rk++;
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/		tokens = comment.getTokensCount();
	}
	
	static public String titles() {
		return "commentCreationTime;commentSize;commentRank;voteTokens;voteValence;voteInceptions;voteOutceptions;";
	}

	static public String zeros() {
		return "0;0;0;0;0;0;0;";
	}
	
	/* (non-Javadoc)
	 * @see functions.logs.LogPack#log()
	 */
	@Override
	public String log(int time) {
		StringBuilder sb = new StringBuilder();
		sb.append(creationTime).append(';');
		sb.append(size).append(';');
		sb.append(rank).append(';');
		sb.append(tokens).append(';');
		sb.append(valence).append(';');
		sb.append(inceptions).append(';');
		sb.append(outceptions).append(';');
		return sb.toString();
	}



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
		
	}
}
