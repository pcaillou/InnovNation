/**
 * 
 */
package functions.logs;

import java.rmi.RemoteException;

import data.IComment;
import data.IIdea;
import data.IItem;
import functions.IGame;



/**
 * @author Pierre Marques
 *
 */
public class GameLogPack implements LogPack {
	private IGame game;
	
	private int players, totalRemainingTokens, totalUsedTokens, ideas, items, comments, betIdeas;
	
	public GameLogPack(IGame game) {
		super();
		if(game==null) throw new NullPointerException();
		
		this.game = game;
		
		players=0;
		totalRemainingTokens=0;
		totalUsedTokens=0;
		ideas=0;
		items=0;
		comments=0;
		betIdeas=0;
	}

	public static String titles() {
		return "players;totalRemainingTokens;totalUsedTokens;totalIdeas;totalItems;totalComments;totalIdeasWithBets;";
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
		sb.append(players).append(';');
		sb.append(totalRemainingTokens).append(';');
		sb.append(totalUsedTokens).append(';');
		sb.append(ideas).append(';');
		sb.append(items).append(';');
		sb.append(comments).append(';');
		sb.append(betIdeas).append(';');
		return sb.toString();
	}
	
	public void updateOnPlayer(int playerId){
		players++;
		try {
			totalRemainingTokens += game.getMaxTokensByPlayer();
		} catch (RemoteException e) {
			// RemoteException without using rmi?
			e.printStackTrace();
		}
	}

	public void updateOnPlayerLeft(int playerId){
		players--;
	}
	
	@Override
	public void updateOnItem(int playerId, IItem createdItem) {
		items++;
	}
	
	@Override
	public void updateOnIdea(int playerId, IIdea createdIdea) {
		ideas++;
	}

	@Override
	public void updateOnComment(int playerId, IComment createdComment) {
		comments++;
		int tokens = createdComment.getTokensCount();
		if(tokens!=0){
			totalUsedTokens += tokens;
			totalRemainingTokens -= tokens;
			//betIdeas++; if applies
			try {
				game.getIdea(createdComment.get());
			} catch (RemoteException e) {
				//RemoteException while not using RMI
				e.printStackTrace();
			}
		}
	}
}
