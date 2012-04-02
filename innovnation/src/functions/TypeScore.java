/**
 * 
 */
package functions;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;

import client.gui.PlayersScores;

import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IPlayer;

/**
 * @author Pierre Marques
 *
 */
public enum TypeScore{
	creativité("créativité") {
		@Override
		// somme(chacune de ses idées,
		// max(temps,nb mises des autres joueurs))
		// a l'origine: somme(temps*nb mises sur ses idées)
		public double calculer(IGame g, int playerId) throws RemoteException {
			StringBuilder sb = new StringBuilder(nom+'\n');
			sb.append("Avez vous proposé des idées qui ont eu du succès?\n");
			sb.append("Somme(Scores maximum de vos idées)");
			tooltip=sb.toString();
			double res=0;
			try {
				for (IIdea idea: g.getAllIdeas()) 
				if (idea.getPlayerId()==playerId)
				{
				res=res+idea.getMaxBids();					
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return res;
		}
	},
	pertinence("pertinence") {
		@Override
		// somme(chacune de ses mises,nombre total de mise sur l'idée-rang de la mise)
		public double calculer(IGame g, int playerId) throws RemoteException {
			StringBuilder sb = new StringBuilder(nom+'\n');
			sb.append("Avez vous su détecter tot les idées qui ont eu du succès?\n");
			sb.append("Pour les idées ou vous avez des jetons, somme des mises APRES vos mises");
			tooltip=sb.toString();
			
				
			int cumulatedTotal = 0;

			for (IIdea currentIdea: g.getAllIdeas()) {
				
				final LinkedList<IComment> allComments = g.getAllIdeasComments(currentIdea.getUniqueId()); // this is a COPY of the comments, it will not change because of changes by another player
				
				// init the mapping between comments and a timeline based on local ids
				Map<IComment,Integer> comment2id = new HashMap<IComment, Integer>();
				{
					int previousId = 0;
					for (IComment currentComment : allComments) {
						comment2id.put(currentComment, previousId++);
				}
				}
				
				// init the various lists for players
				Map<IPlayer,ArrayList<Integer>> player2scores = new HashMap<IPlayer, ArrayList<Integer>>();
				for (IPlayer currentPlayer : g.getAllPlayers()) {
					ArrayList<Integer> list = new ArrayList<Integer>(allComments.size());
					for (int i=0; i<allComments.size();i++)
						list.add(0);
					player2scores.put(
							currentPlayer, 
							list
							);
				}
				
				// populates the lists of scores for each player
				for (IComment currentComment : allComments) {
					if (currentComment.getTokensCount() != 0) {
						// right, this one deserves some processing
						Integer localID = comment2id.get(currentComment);
						IPlayer author = g.getPlayer(currentComment.getPlayerId());
						player2scores.get(author).set(localID, currentComment.getTokensCount());
					}
				}
				
				// iterates through each list of scores and removes the negative values
				for (IPlayer currentPlayer : player2scores.keySet()) {
					LinkedList<Integer> indexesPositivesValues = new LinkedList<Integer>();
					ArrayList<Integer> playerVotes = player2scores.get(currentPlayer);
//					logger.debug("Player "+currentPlayer.getShortName()+" bids (with negative): \t "+playerVotes.toString());
					for (int i=0; i<playerVotes.size(); i++) {
						Integer currentValue = playerVotes.get(i);
						if (currentValue < 0) {
							
							// for each negative value, removes the tokens from the previous positive values
							int tokensToRemove = -currentValue;
							while (tokensToRemove > 0) {
								Integer idxLastPositiveValue = 	indexesPositivesValues.getLast();
								int valueLastVote = playerVotes.get(idxLastPositiveValue);
								int novelValue = Math.max(0, valueLastVote - tokensToRemove); 
								
								if (novelValue == 0) {
									indexesPositivesValues.removeLast(); // remove this index, nothing more there
									tokensToRemove -= valueLastVote;
								} else {
									tokensToRemove = 0;
								}
							
								playerVotes.set(idxLastPositiveValue, novelValue);
								//logger.debug("bid "+idxLastPositiveValue+"("+valueLastVote+"): now "+novelValue);
									
							}
							
							// actually remove this negative value that was already processed (removed from previous positive bids)
							playerVotes.set(i, 0);
							
							
						} else if (currentValue > 0) {
							// this is a positive vote that could be used later to remove tokens
							indexesPositivesValues.add(i);
						} // as default, nothing happens if 0
					}
					
	//				logger.debug("Player "+currentPlayer.getShortName()+" bids (removing negative): \t "+playerVotes.toString());
					
				}
				

				// well, we now have the list of positive votes, without any negative votes. 
				// we can now compute the score
				// dixit Philippe: "Maintenant, c'est facile"
				IPlayer currentPlayer = g.getPlayer(playerId);		
				ArrayList<Integer> playerVotes = player2scores.get(currentPlayer);
				for (int i=0; i<playerVotes.size(); i++) {
					Integer currentValue = playerVotes.get(i);
					if (currentValue > 0) {
						// we have to compute the tokens from other players 
						for (IPlayer currentPlayerOther : player2scores.keySet()) {
							if (currentPlayerOther != currentPlayer) {
								ArrayList<Integer> playerVotesOther = player2scores.get(currentPlayerOther);
								for (int j=i+1; j<playerVotesOther.size(); j++) {
									cumulatedTotal += playerVotesOther.get(j)*currentValue;
								}		
							}
						}
						
					}
				}
				
	//			logger.debug("Player "+currentPlayer.getShortName()+"'s score: \t "+cumulatedTotal);

			}
			
			return cumulatedTotal;
		
		} // end of calculer()
	},	// end of the class
	
	persuasion("persuasion") {
		@Override
		// somme(chacun de ses commentaire,changements suite a se commentaire/distance au commentaire)
		public double calculer(IGame g, int playerId) throws RemoteException {
			StringBuilder sb = new StringBuilder(nom+'\n');
			sb.append("Avez vous convaincu les joueurs de vous suivre?\n");
			sb.append("= Nombre de mises après vos commentaires dans le même sens que la valence / (Distance)");
			tooltip=sb.toString();
	

			double cumulatedTotal = 0;
			double localTotal = 0;
			double distcoef=1.0;
			int distance=0;
			HashMap<Integer,Integer> mises;
			for (IIdea currentIdea: g.getAllIdeas()) {
				
				localTotal = 0;
				final LinkedList<IComment> allComments = g.getAllIdeasComments(currentIdea.getUniqueId()); // this is a COPY of the comments, it will not change because of changes by another player
				mises=new HashMap<Integer,Integer>();
				Iterator<IComment> iterat=allComments.iterator();
				
				// init the mapping between comments and a timeline based on local ids
					distance=0;
					IComment currentComment;
					while (iterat.hasNext())
					{
						currentComment=iterat.next();
						distance++;
						if (currentComment.getPlayerId()==playerId)
						{
							if (currentComment.getValence().equals(CommentValence.POSITIVE))
								mises.put(distance,1);								
//							if (currentComment.getValence().equals(CommentValence.POSITIVE))
//							logger.debug("Player+ "+playerId+"'s persuasion id "+ currentIdea.getUniqueId()+" score: \t "+localTotal);
							if (currentComment.getValence().equals(CommentValence.NEGATIVE))
								mises.put(distance,-1);								
//							if (currentComment.getValence().equals(CommentValence.NEGATIVE))
//							logger.debug("Player- "+playerId+"'s persuasion id "+ currentIdea.getUniqueId()+" score: \t "+localTotal);
/*							if (currentComment.getTokensCount()!=0)
							{
								mises.put(distance,currentComment.getTokensCount());								
							}*/
						}
						else
						{
							if (currentComment.getTokensCount()!=0)
							{
								Iterator<Integer> miseprecdate=mises.keySet().iterator();
								while (miseprecdate.hasNext())
								{
									int datemise=miseprecdate.next();
									localTotal=localTotal+(currentComment.getTokensCount()*mises.get(datemise))/(distcoef*(distance-datemise));
/*									if ((currentComment.getTokensCount()*mises.get(datemise))>0)
									{
//										localTotal=localTotal+1/(distance-datemise);										
										localTotal=localTotal+1;										
									}
									else
									{
//										localTotal=localTotal-1/(distance-datemise);																				
										localTotal=localTotal-1;																				
									}*/
								}
							}
							
						}
					}
				if (localTotal!=0)
				logger.debug("Player "+playerId+"'s persuasion id "+ currentIdea.getUniqueId()+" score: \t "+localTotal);
				cumulatedTotal=cumulatedTotal+localTotal;

			}
			return cumulatedTotal;
/*			
			double cumulatedTotal = 0;

			for (IIdea currentIdea: g.getAllIdeas()) {
				
				final LinkedList<IComment> allComments = g.getAllIdeasComments(currentIdea.getUniqueId()); // this is a COPY of the comments, it will not change because of changes by another player
					
				
				// init the mapping between comments and a timeline based on local ids
				Map<IComment,Integer> comment2id = new HashMap<IComment, Integer>();
				{
					int previousId = 0;
					for (IComment currentComment : allComments) {
						comment2id.put(currentComment, previousId++);
					}
				}
				
				// init the various lists for players
				Map<IPlayer,ArrayList<Integer>> player2scores = new HashMap<IPlayer, ArrayList<Integer>>();
				for (IPlayer currentPlayer : g.getAllPlayers()) {
					ArrayList<Integer> list = new ArrayList<Integer>(allComments.size());
					for (int i=0; i<allComments.size();i++)
						list.add(0);
					player2scores.put(
							currentPlayer, 
							list
							);
				}
				
				// populates the lists of scores for each player
				for (IComment currentComment : allComments) {
					if (currentComment.getTokensCount() != 0) {
						// right, this one deserves some processing
						Integer localID = comment2id.get(currentComment);
						IPlayer author = g.getPlayer(currentComment.getPlayerId());
						player2scores.get(author).set(localID, currentComment.getTokensCount());
					}
				}
				
	
				// we can now compute the score
				// dixit Philippe: "Maintenant, c'est encore plus facile"
				IPlayer currentPlayer = g.getPlayer(playerId);		
				ArrayList<Integer> playerVotes = player2scores.get(currentPlayer);
				double baseDistance = -1;
				int baseValue = -1;
				for (int i=0; i<playerVotes.size(); i++) {
					Integer currentValue = playerVotes.get(i);
					if (baseDistance > -1) { // we are counting
						
						baseDistance++;
						
						// explore all the bids from other players
						for (IPlayer currentPlayerOther : player2scores.keySet()) {
							if (currentPlayerOther != currentPlayer) {
								cumulatedTotal += player2scores.get(currentPlayerOther).get(i)*baseValue/(baseDistance);
								logger.debug("+ "+player2scores.get(currentPlayerOther).get(i)+"*"+baseValue+"/"+baseDistance+"="+(player2scores.get(currentPlayerOther).get(i)*baseValue/(baseDistance)));
							}
						}
							
					}

					if (currentValue != 0) {
						// start the accumulation of points for this score.
						// also inc the distance count.
						baseDistance = 0;
						baseValue = currentValue;
					}
					
				}
				
				logger.debug("Player "+currentPlayer.getShortName()+"'s score: \t "+cumulatedTotal);

			}
			return cumulatedTotal;*/
		}
		
	},
	adaptation("adaptation") {
		@Override
		// somme(chacune des mises,(commentaire précédent dans le meme sens-commentaire précédent dans sens inverse)/distance au commentaire)
		public double calculer(IGame g, int playerId) throws RemoteException {
			StringBuilder sb = new StringBuilder(nom+'\n');
			sb.append("Avez vous accepté d'écouter les autres?\n");
			sb.append("= Nombre de commentaires précédents de valence similaire a la mise / (Distance)");
			tooltip=sb.toString();
				
			double cumulatedTotal = 0;
			double localTotal = 0;
			double distcoef=1.0;
			int distance=0;
			HashMap<Integer,Integer> mises;
			for (IIdea currentIdea: g.getAllIdeas()) {
				
				localTotal = 0;
				final LinkedList<IComment> allComments = g.getAllIdeasComments(currentIdea.getUniqueId()); // this is a COPY of the comments, it will not change because of changes by another player
				mises=new HashMap<Integer,Integer>();
				Iterator<IComment> itreverse=allComments.descendingIterator();
				
				// init the mapping between comments and a timeline based on local ids
					distance=0;
					IComment currentComment;
					while (itreverse.hasNext())
					{
						currentComment=itreverse.next();
						distance++;
						if (currentComment.getPlayerId()==playerId)
						{
/*							if (currentComment.getValence().equals(CommentValence.POSITIVE))
								mises.put(distance,1);								
							if (currentComment.getValence().equals(CommentValence.POSITIVE))
							logger.debug("Player+ "+playerId+"'s adaptation id "+ currentIdea.getUniqueId()+" score: \t "+localTotal);
							if (currentComment.getValence().equals(CommentValence.NEGATIVE))
								mises.put(distance,-1);								
							if (currentComment.getValence().equals(CommentValence.NEGATIVE))
							logger.debug("Player- "+playerId+"'s adaptation id "+ currentIdea.getUniqueId()+" score: \t "+localTotal);
*/
							if (currentComment.getTokensCount()!=0)
							{
								mises.put(distance,currentComment.getTokensCount());								
							}
						}
						else
						{
							if (!currentComment.getValence().equals(CommentValence.NEUTRAL))
							{
								Iterator<Integer> miseprecdate=mises.keySet().iterator();
								while (miseprecdate.hasNext())
								{
									int datemise=miseprecdate.next();
									if (currentComment.getValence().equals(CommentValence.NEGATIVE))
										localTotal=localTotal+((-1)*mises.get(datemise))/(distcoef*(distance-datemise));
									if (currentComment.getValence().equals(CommentValence.POSITIVE))
										localTotal=localTotal+((+1)*mises.get(datemise))/(distcoef*(distance-datemise));
//										localTotal=localTotal+(currentComment.getTokensCount()*mises.get(datemise))/(distance-datemise);
/*									if ((currentComment.getTokensCount()*mises.get(datemise))>0)
									{
//										localTotal=localTotal+1/(distance-datemise);										
										localTotal=localTotal+1;										
									}
									else
									{
//										localTotal=localTotal-1/(distance-datemise);																				
										localTotal=localTotal-1;																				
									}*/
								}
							}
							
						}
					}
				if (localTotal!=0)
				logger.debug("Player "+playerId+"'s adaptation id "+ currentIdea.getUniqueId()+" score: \t "+localTotal);
				cumulatedTotal=cumulatedTotal+localTotal;

			}
			return cumulatedTotal;
		}
	}
/*	,
	zero("zero") {
		@Override
		public double calculer(IGame g, int playerId) throws RemoteException {
			return 0;
		}
	},
	myId("my Id"){
		@Override
		public double calculer(IGame g, int playerId) throws RemoteException {
			return playerId;
		}
	}*/;

	public final String nom;
	public String tooltip;
	
	protected Logger logger;
	
	private TypeScore(String nom){
		this.nom = nom;
		this.tooltip="score description";
		this.logger = Logger.getLogger("scores."+nom);
	}
	
	
	/**
	 * calcule ce type de score sur le jeu indiqué, pour le joueur souhaité
	 * @param g le jeu à utiliser
	 * @param playerId l'identifiant du joueur dont on souhaite ce score
	 * @return la valeur du score
	 * @throws RemoteException quand le jeu ne se laisse pas faire :)
	 */
	public abstract double calculer(IGame g, int playerId) throws RemoteException;

	public double calculerrk(IGame g, int playerId) throws RemoteException
	{
		LinkedList<Integer> playerIds;
		playerIds = g.getAllPlayersIds();
		
		Map<Integer, Double> sctab = this.calculer(g);
		
		int i=0;
		Map<Integer, Double>  sccop;
				
				double scval=sctab.get(playerId);
				ArrayList sol=new ArrayList(sctab.values());
				Collections.sort(sol);
	
				int rgval=playerIds.size()-sol.indexOf(scval);
			
				return rgval;
	}


	/**
	 * calcule ce type de score sur le jeu indiqué, pour tous les joueurs
	 * @param g le jeu à utiliser
	 * @return une map des identifiants de joueur vers leurs scores respectifs
	 * @throws RemoteException quand le jeu ne se laisse pas faire :)
	 */
	public Map<Integer, Double> calculer(IGame g) throws RemoteException {
		LinkedList<Integer> playerIds;
		playerIds = g.getAllPlayersIds();
		
		Map<Integer, Double> scores = new HashMap<Integer, Double>(playerIds.size()*2, 0.5f);
		
		for(int id : playerIds) scores.put(id, calculer(g, id));
		
		return scores;
	}
}
