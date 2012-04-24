/**
 * 
 */
package functions.logs;

import java.rmi.RemoteException;

import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;
import functions.Game;
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
	
	private int remainingTokens, usedTokens,
		ideas, items, comments, votes,
		lastActionTime,
		bestIdeaScore;
	
	private int itemUsage, ideaUsage, commentUsage;
	
	public PlayerLogPack(IGame game,IPlayer p, int time) {
		if(p==null) throw new NullPointerException();
		this.game = game;
		this.player = p;
		this.myId = p.getUniqueId();
		this.lastActionTime = time;

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
	}
	
	static public String titles() {
		StringBuilder sb = new StringBuilder(
				"playerId;playerRemainingTokens;playerUsedTokens;playerIdeas;playerItems;playerComments;playerVotes;playerTimeSinceAction;playerBestIdeaScore;playerItemUsage;playerIdeaUsage;playerCommentUsage;"
		);
		for(TypeScore s : TypeScore.values()) sb.append("player").append(s.nom).append(';');
		for(TypeScore s : TypeScore.values()) sb.append("rank").append(s.nom).append(';');
		return sb.toString(); 
		
	}

	static public String zeros() {
		StringBuilder sb = new StringBuilder("0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;");
		for(@SuppressWarnings("unused") TypeScore s : TypeScore.values()) sb.append("0.0;");
		return sb.toString(); 
		
	}
	
	/* (non-Javadoc)
	 * @see functions.logs.LogPack#log()
	 */
	@Override
	public String log(int time) {
		StringBuilder sb = new StringBuilder();
		sb.append(myId).append(';');
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
		
		for(TypeScore s : TypeScore.values()) sb.append(player.getScore(s)).append(';');
		for(TypeScore s : TypeScore.values()) sb.append(player.getRank(s)).append(';');
		
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
		}
	}

	@Override
	public void updateOnComment(int playerId, IComment createdComment) {
		if(createdComment==null) throw new NullPointerException();
		if(playerId == myId){
			comments++;
			
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
