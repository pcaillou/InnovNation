/**
 * 
 */
package functions.logs;

// AD import java.rmi.RemoteException;

import java.rmi.RemoteException;

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
	private int creationTime, size, rank, valence,tokens, inceptions, outceptions, ownerId, ideaId, commentId,nbCommentBefore,nbPosCommentBefore,nbNegCommentBefore;
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
		ownerId = comment.getPlayerId();
		commentId = comment.getUniqueId();
		
		nbCommentBefore = 0;
		nbPosCommentBefore = 0;
		nbNegCommentBefore = 0; 
		
//		for (IComment c : comment.getIdea().getComments())
			try {
				for (IComment c : game.getAllComments())
					if (c.getIdea().getUniqueId()==comment.getIdea().getUniqueId())
{
				nbCommentBefore++;
				if (c.getValence() == CommentValence.POSITIVE)
				{
					nbPosCommentBefore++;
				}
				if (c.getValence() == CommentValence.NEGATIVE)
				{
					nbNegCommentBefore++;
				}
}
			} catch (RemoteException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} 
		
		 
		try {
			for (IIdea idea : game.getAllIdeas())
			{
				if (comment.getIndexSource().equals(idea.getIndex()))
				{
					ideaId = idea.getUniqueId();
					break;
				}
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
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
		
		// voir le joueur cible du commentaire
		try {
			game.getPlayer(comment.getPlayerId());
			// ajouter � player une variable contenant le temps exact de ce contact 
		} catch (RemoteException e) {e.printStackTrace();}


	}
	
	static public String titles() {
		return "commentId;commentIdeaId;commentOwnerId;commentCreationTime;commentSize;commentRank;voteTokens;voteValence;voteInceptions;voteOutceptions;nbCommentBefore;nbPosCOmmentBefore;nbNegCOmmentBefore;";
	}

	static public String zeros() {
		return "0;0;0;0;0;0;0;0;0;0;0;0;0;";
	}
	
	/* (non-Javadoc)
	 * @see functions.logs.LogPack#log()
	 */
	@Override
	public String log(int time) {
		StringBuilder sb = new StringBuilder();
		sb.append(commentId).append(';');
		sb.append(ideaId).append(';');
		sb.append(ownerId).append(';');
		sb.append(creationTime).append(';');
		sb.append(size).append(';');
		sb.append(rank).append(';');
		sb.append(tokens).append(';');
		sb.append(valence).append(';');
		sb.append(inceptions).append(';');
		sb.append(outceptions).append(';');
		sb.append(nbCommentBefore).append(';');
		sb.append(nbPosCommentBefore).append(';');
		sb.append(nbNegCommentBefore).append(';');
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
