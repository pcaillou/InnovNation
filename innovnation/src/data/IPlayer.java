package data;

import java.rmi.RemoteException;
import java.util.EnumMap;
import java.util.Map;

import functions.IGame;
import functions.TypeScore;

/**
 * @author Pierre Marques
 *
 */
public interface IPlayer extends IStorable {
	
	Map<IIdea,Integer> getTokensBets() throws RemoteException;
	
	int getRemainingTokens() throws RemoteException;
	
	/**
	 * This player has spend some tokens.
	 * @param nbTokens
	 * @throws RemoteException
	 */
	public void tokensWereBet(int nbTokens) throws RemoteException;
	
	/**
	 * Returns a String that provides an access to the picture chosen as an avatar
	 * TODO how to build absolute URL ? Should we rather transmit the image itself ???
	 * @return
	 * @throws RemoteException
	 */
	String getPicturePath() throws RemoteException;
	
	/**
	 * defines the path to the picture to use as an avatar.
	 * @param picture
	 * @throws RemoteException
	 */
	public void setPicturePath(String picture) throws RemoteException;
	
	public boolean hasPicturePath() throws RemoteException;

	
	/**
	 * Returns the value of the given score
	 * @return the double value of the score as reminded here
	 */
	double getScore(TypeScore type);
	double getRank(TypeScore type);
	

	public void setOpinion(int[] _opinion);
	public int[] getOpinion();
	
	
	/**
	 * defines the value of the given score.
	 * @param type
	 * @param valeur
	 */
	public void setScore(TypeScore type, double valeur);
	public void setRank(TypeScore type, double valeur);

	/**
	 * defines the values of the scores.
	 * @param valeurs
	 */
	public void setScores(EnumMap<TypeScore, Double> valeurs);

	public void majScores(IGame g) throws RemoteException ;
	
	
}
