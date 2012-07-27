/**
 * 
 */
package data;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import client.DelegatingBotCore;

import functions.AbstractGame;
import functions.IGame;
import functions.TypeScore;

/**
 * @author Pierre Marques
 *
 */
public class Player extends Storable implements IPlayer, Serializable {
	private static final long serialVersionUID = 1L;
	private static final EnumMap<TypeScore, Double> EmptyScores = new EnumMap<TypeScore, Double>(TypeScore.class);

	private int[] opinion;
	
	static {
		for(TypeScore score : TypeScore.values()) EmptyScores.put(score, 0.);
	}
	
	
	private String picturePath = null;
	
	private int remainingTokens = AbstractGame.INITIAL_TOKEN_COUNT_BY_PLAYER;
	
	private EnumMap<TypeScore, Double> scores = new EnumMap<TypeScore, Double>(EmptyScores);
	private EnumMap<TypeScore, Double> scoresrk = new EnumMap<TypeScore, Double>(EmptyScores);
	
	/**
	 * @param shortName
	 */
	public Player(String shortName) {
		this(shortName, Avatars.getOneAvatarRandomly());
	}
	
	public Player(String shortName, String avatar) {
		super(shortName);
		this.picturePath = avatar;
		opinion = new int[DelegatingBotCore.TOTAL_OPINION];
		for (int i = 0 ; i < opinion.length ; i++)
		{
			opinion[i] = 0;
		}
	}
	
	public Player(String playerName, String avatar, int[] _opinion) 
	{
		this(playerName,avatar);
		opinion = _opinion;
		
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString()).append(" scores{ ");
		for(Entry<TypeScore, Double> i : scores.entrySet()){
			sb.append(i.getKey().nom).append(' ').append(i.getValue()).append(' ');
		}
		return sb.append("}").toString();
	}

	/* (non-Javadoc)
	 * @see data.IPlayer#getRemainingTokens()
	 */
	@Override
	public int getRemainingTokens(){
		return remainingTokens;
	}

	@Override
	public String getPicturePath(){
		return this.picturePath;
	}
	
	public void setPicturePath(String picture){
		
		if (!Avatars.doesAvatarExist(picture))
			throw new IllegalArgumentException("Invalid avatar: "+picture);
		
		this.picturePath = picture;
	}
	
	public boolean hasPicturePath(){
		return (picturePath != null);
	}

	@Override
	public Map<IIdea, Integer> getTokensBets() throws RemoteException {
		// TODO !!!
		return null;
	}

	@Override
	public void tokensWereBet(int nbTokens) throws RemoteException {
		remainingTokens -= nbTokens;
	}

	public void setOpinion(int[] _opinion)
	{
		opinion = _opinion;
	}
	
	public int[] getOpinion()
	{
		return opinion;
	}
	
	/* (non-Javadoc)
	 * @see data.IPlayer#getScore(functions.TypeScore)
	 */
	@Override
	public double getScore(TypeScore type) {
		return scores.get(type);
	}
	@Override
	public double getRank(TypeScore type) {
		return scoresrk.get(type);
	}
	

	/* (non-Javadoc)
	 * @see data.IPlayer#setScore(functions.TypeScore, double)
	 */
	@Override
	public void setScore(TypeScore type, double valeur) {
		scores.put(type, valeur);
	}
	@Override
	public void setRank(TypeScore type, double valeur) {
		scoresrk.put(type, valeur);
	}
	
	@Override
	public void setScores(EnumMap<TypeScore, Double> valeurs) {
		scores.putAll(valeurs);
	}

	@Override
	public void majScores(IGame g)  throws RemoteException {
		for(TypeScore score : TypeScore.values()) setScore(score, score.calculer(g, this.getUniqueId()));
		for(TypeScore score : TypeScore.values()) setRank(score, score.calculerrk(g, this.getUniqueId()));
		
	}

}
