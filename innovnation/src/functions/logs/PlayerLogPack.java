/**
 * 
 */
package functions.logs;

import java.rmi.RemoteException;

import client.DelegatingBotCore;

import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;
//import functions.Game;
import functions.IGame;
import functions.TypeScore;

/**
 * @author Pierre Marques
 *
 */
public class PlayerLogPack implements LogPack {
	private int myId;
	private IGame game;
	private IPlayer player;
	private int[] opinion;
	
	private int remainingTokens, usedTokens,
		ideas, items, comments, votes,
		lastActionTime,
		bestIdeaScore;

	private int 
	PositiveComments,NegativeComments; 
	private double averageCommentLength, averageIdeaLength,averageIdeaDescription;
	private double PositiveProportion,NegativeProportion,NulProportion;
	
	
	private int itemUsage, ideaUsage, commentUsage;
	
	public PlayerLogPack(IGame game,IPlayer p, int time) {
		if(p==null) throw new NullPointerException();
		this.game = game;
		this.player = p;
		this.myId = p.getUniqueId();
		this.lastActionTime = time;

		opinion = p.getOpinion();
		
		try {
			remainingTokens = p.getRemainingTokens();
		} catch (RemoteException e) {
			remainingTokens = 0;
		}
		usedTokens = 0;
		ideas=0;
		items = 0;
		comments = 0;
		votes = 0;
		PositiveComments=0;
		NegativeComments=0; 
		averageCommentLength=0;
		averageIdeaLength=0;
		averageIdeaDescription=0;
		PositiveProportion=0;
		NegativeProportion=0;
		NulProportion=0;

	}
	
	static public String titles() {
		StringBuilder sb = new StringBuilder(
				"playerId;playerOpinion;playerRemainingTokens;playerUsedTokens;playerIdeas;playerItems;playerComments;playerVotes;playerTimeSinceAction;playerBestIdeaScore;playerItemUsage;playerIdeaUsage;playerCommentUsage;"+
		"plPositiveComments;plNegativeComments;plAvgCommentLength;plAvgIdeaLength;plAvgIdeaDescription;plPositiveProportion;plNegativeProportion;plNulProportion;"
		);
		for(TypeScore s : TypeScore.values()) sb.append("player").append(s.nom).append(';');
		for(TypeScore s : TypeScore.values()) sb.append("rank").append(s.nom).append(';');
		sb.append("nbVotedIdeas").append(";");
		return sb.toString(); 
		
	}

	static public String zeros() {
		String sopinion = "";
		for (int i = 0 ; i < DelegatingBotCore.TOTAL_OPINION ; i++)
		{
			sopinion += "0";
		}
		StringBuilder sb = new StringBuilder("0;[" + sopinion + "];0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;");
		for(@SuppressWarnings("unused")TypeScore s : TypeScore.values()) sb.append("0.0;0.0;");
		sb.append("0;");
		return sb.toString(); 
		
	}
	
	/* (non-Javadoc)
	 * @see functions.logs.LogPack#log()
	 */
	@Override
	public String log(int time) {
		opinion = player.getOpinion();
		String sopinion = "[";
		for (int i = 0 ; i < DelegatingBotCore.TOTAL_OPINION ; i++)
		{
			sopinion += String.valueOf(opinion[i]);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(myId).append(';');
		sb.append(sopinion).append(']').append(';');
		sb.append(remainingTokens).append(';');
		sb.append(usedTokens).append(';');
		sb.append(ideas).append(';');
		sb.append(items).append(';');
		sb.append(comments).append(';');
		sb.append(votes).append(';');
		
		sb.append(time-lastActionTime).append(';');
		
		sb.append(bestIdeaScore).append(';');
		sb.append(itemUsage).append(';');
		sb.append(ideaUsage).append(';');
		sb.append(commentUsage).append(';');
		sb.append(PositiveComments).append(';');
		sb.append(NegativeComments).append(';');
		sb.append(averageCommentLength).append(';');
		sb.append(averageIdeaLength).append(';');
		sb.append(averageIdeaDescription).append(';');
		sb.append(PositiveProportion).append(';');
		sb.append(NegativeProportion).append(';');
		sb.append(NulProportion).append(';');
		
		for(TypeScore s : TypeScore.values()) sb.append(player.getScore(s)).append(';');
		for(TypeScore s : TypeScore.values()) sb.append(player.getRank(s)).append(';');
		int i=0;
//		for (Idea i:player.tokensWereBet(nbTokens))
			sb.append("0;");
//			sb.append(player.getTokensBets().size()).append(';');
		
		return sb.toString();
	}


	/**
	 * notice that the logged player acted at date
	 * @param now
	 */
	public void noticeAction(int date){
		lastActionTime = date;
	}

	public void updateItemUsage(int delta){ itemUsage+=delta; }
	
	public void updateIdeaUsage(int delta){ ideaUsage+=delta;}
	
	public void updateCommentUsage(int delta){ commentUsage+=delta; }
	
	@Override
	public void updateOnItem(int playerId, IItem createdItem) {
		if(createdItem==null) throw new NullPointerException();
		if(playerId == myId){
			items++;
		}
	}
	
	@Override
	public void updateOnIdea(int playerId, IIdea createdIdea) {
		if(createdIdea==null) throw new NullPointerException();
		if(playerId == myId){
			ideas++;
			averageIdeaLength=(averageIdeaLength*(ideas-1)+createdIdea.getShortName().length())/ideas;
			averageIdeaDescription=(averageIdeaDescription*(ideas-1)+createdIdea.getDesc().length())/ideas;
		}
	}

	@Override
	public void updateOnComment(int playerId, IComment createdComment) {
		if(createdComment==null) throw new NullPointerException();
		if(playerId == myId){
			comments++;

			if (createdComment.getValence().equals(CommentValence.POSITIVE))
			{
				PositiveComments++;
			}
			if (createdComment.getValence().equals(CommentValence.NEGATIVE))
			{
				NegativeComments++;
			}
			PositiveProportion=((double)PositiveComments)/comments;
			NegativeProportion=((double)NegativeComments)/comments;
			NulProportion=1.0-PositiveProportion-NegativeProportion;

			averageCommentLength=(averageCommentLength*(comments-1)+createdComment.getText().length())/comments;
			
			int tokens = createdComment.getTokensCount();
			if(tokens!=0){
				usedTokens += tokens;
				remainingTokens -= tokens;
				votes++;
				try {
					IIdea iid=game.getIdea(createdComment.get());
					if (iid.getPlayerId()==myId)
					if (iid.getTotalBids()>this.bestIdeaScore)
						this.bestIdeaScore=iid.getTotalBids();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
}
