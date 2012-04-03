package client.gui;

import java.util.Map;
// AD import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

// AD import data.IPlayer;

public class PlayersScores implements Comparable<PlayersScores> {

	public final Double score;
	public final Integer idplayer;
	
	public PlayersScores(Double score, Integer idplayer) {
		this.score = score;
		this.idplayer = idplayer;
	}


	@Override
	public int compareTo(PlayersScores otherScore) {
		return -score.compareTo(otherScore.score);
	}
	
	public static SortedSet<PlayersScores> calculer(Map<Integer, Double> idPlayer2score) {
		SortedSet<PlayersScores> res = new TreeSet<PlayersScores>();
		
		for (Integer idPlayer : idPlayer2score.keySet()) {
			Double score = idPlayer2score.get(idPlayer);
			PlayersScores current = new PlayersScores(score, idPlayer);
			res.add(current);
		}
		
		return res;
	}
	
	/**
	 * Computes the rank of a player in the sorted set of scores.
	 * @param sortedScores
	 * @param playerId
	 * @return
	 */
	public static Integer computeRank(SortedSet<PlayersScores> sortedScores, Integer playerId) {
		
		int i = 1;
		for (PlayersScores currentScore: sortedScores) {
			if (currentScore.idplayer == playerId)
				return i;
			i++;
		}
		
		return i;
	}
	
}
