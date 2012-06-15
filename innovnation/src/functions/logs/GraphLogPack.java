package functions.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import data.IComment;
import data.IIdea;
import data.IItem;
import functions.Game;

/**
 * Classe pour gerer les logs de graphe dans InnovNation
 * 
 * @author Destannes Alexandre
 *
 */
public class GraphLogPack implements LogPack {

	private Game game;
	
	private static GraphInnovNation graph;
	

	private Collection<String> innovNationList;
	private Collection<String> logPInnovNation;
	private Collection<String> logIInnovNation;

	public GraphLogPack(Game _game)
	{
		game = _game;
		innovNationList = new ArrayList<String>();
		logPInnovNation = new ArrayList<String>();
		logIInnovNation = new ArrayList<String>();
		try {
			graph = new GraphInnovNation("1");
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
	}
	
	static public ArrayList<String> getPlayerGraphList()
	{
		ArrayList<String> result = new ArrayList<String>();
		
		result.add("nbVoteGraph");
		result.add("weightVoteGraph");
		result.add("persuasionGraph");
		result.add("InnovNationGraph");
		
		return result;
		
	}
	
	static public ArrayList<String> getIdeaGraphList()
	{
		ArrayList<String> result = new ArrayList<String>();
		
		result.add("InnovNationGraph");
		
		return result;
		
	}
	
	static public ArrayList<String> getGraphList()
	{
		ArrayList<String> result = new ArrayList<String>();
		
		result.add("nbVoteGraph");
		result.add("weightVoteGraph");
		result.add("persuasionGraph");
		result.add("InnovNationGraph");
		
		return result;
	}
	
	static public String titles() {
		StringBuilder sb = new StringBuilder("InnovNationGraph;");
		return sb.toString(); 
	}

	static public String zeros() {
		StringBuilder sb = new StringBuilder("[];");
		return sb.toString(); 
	}

	public String log(int time) {
		StringBuilder sb = new StringBuilder();

		/* nouvelles liste de log des sous graphes */
		Collection<String> newInnovNationList = graph.toStringArray();
		Collection<String> diffInnovNationList = new ArrayList<String>();
		
		diffInnovNationList.addAll(newInnovNationList);
		diffInnovNationList.removeAll(innovNationList);
		
		innovNationList = newInnovNationList;
		String list = "";

		/* on ajoute les nouvelles logs du graphe innovNation */
		list = "";
		for (String s : diffInnovNationList)
		{
			if (list.length() > 0)
			{
				list += GraphInnovNation.SEPARATOR;
			}
			list += s;
		}
		sb.append(GraphInnovNation.LEFT_BRACE).append(list).append(GraphInnovNation.RIGHT_BRACE).append(';');
		
		return sb.toString();
	}

	public HashMap<Integer,String> getLogpLogs(int time) throws IOException
	{
		HashMap<Integer,String> result = new HashMap<Integer,String>();
		Collection<String> newLogPInnovNation = new ArrayList<String>();
		
		/* on genere les nouvelles logPList */
		GraphInnovNation graph2 = graph.clone();
		
		GraphInnovNation gInnovNation = graph2.clone();

		newLogPInnovNation = gInnovNation.toStringArray();

		/* on calcule la difference pour chaque logP et on l'ajoute au resultat */
		Collection<String> difference = new ArrayList<String>();
		boolean innovGraphAdded = false;
		for(Integer p : game.getAllPlayersIds())
		{
			result.put(p,"");
			
			if(!innovGraphAdded)
			{
				difference.addAll(newLogPInnovNation);
				difference.removeAll(logPInnovNation);
				result.put(p, result.get(p) + GraphInnovNation.tabToString(difference) + ";");
				difference.clear();
				innovGraphAdded = true;
			}
			else
			{
				result.put(p, result.get(p) + GraphInnovNation.tabToString(new ArrayList<String>())+ ";");
			}
		}
		logPInnovNation.clear();
		logPInnovNation.addAll(newLogPInnovNation);
		
		return result;
		
	}
	
	public HashMap<Integer,String> getLogiLogs(int time) throws IOException
	{
		HashMap<Integer,String> result = new HashMap<Integer,String>();
		Collection<String> newLogIInnovNation = new ArrayList<String>();
		
		/* on genere les nouvelles logIList */
		GraphInnovNation graph2 = graph.clone();
		
		GraphInnovNation gInnovNation = graph2.clone();

		//Collection<String> edges;
		newLogIInnovNation = gInnovNation.toStringArray();

		/* on calcule la difference pour chaque logP et on l'ajoute au resultat */
		Collection<String> difference = new ArrayList<String>();
		boolean innovGraphAdded = false;
		for(IIdea i : game.getAllIdeas())
		{

			if(!innovGraphAdded)
			{
				difference.addAll(newLogIInnovNation);
				difference.removeAll(logIInnovNation);
				result.put(i.getUniqueId(), GraphInnovNation.tabToString(difference) + ";");
				difference.clear();
				innovGraphAdded = true;
			}
			else
			{
				result.put(i.getUniqueId(), GraphInnovNation.tabToString(new ArrayList<String>())+ ";");
			}
		}
		
		logIInnovNation.clear();
		logIInnovNation.addAll(newLogIInnovNation);
		
		return result;
		
	}
	
	public void updateOnPlayer(int playerId) {
		try {
			graph.addPlayer(String.valueOf(playerId),(long) game.getTime());
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
	}

	public void updateOnItem(int playerId, IItem createdItem) {}

	public void updateOnIdea(int playerId, IIdea createdIdea) {
		ArrayList<String> idIdeasSource = new ArrayList<String>();

		for (IIdea i : createdIdea.getParents())
		{
			idIdeasSource.add(String.valueOf(i.getUniqueId()));
		}
		
		try {
			graph.addIdea(String.valueOf(createdIdea.getUniqueId()), idIdeasSource, String.valueOf(playerId), (long)game.getTime());
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
	}

	public void updateOnComment(int playerId, IComment createdComment) {
		
		int valence = 0;
		switch(createdComment.getValence())
		{
		case POSITIVE :
			valence = 1;
			break;
		case NEGATIVE :
			valence = -1;
			break;
		case NEUTRAL :
			valence = 0;
			break;
		}
		
		try {
			graph.addVote(
					String.valueOf(createdComment.getUniqueId()), 
					String.valueOf(playerId), 
					String.valueOf(createdComment.getIdea().getUniqueId()), 
					createdComment.getTokensCount(), 
					valence, 
					(long)game.getTime());
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
	}

	
}
