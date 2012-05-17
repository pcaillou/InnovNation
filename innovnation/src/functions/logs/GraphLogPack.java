package functions.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.graphstream.graph.Edge;
import org.graphstream.ui.swingViewer.Viewer;

import data.IComment;
import data.IIdea;
import data.IItem;
import functions.Game;

/**
 * Classe pour gerer les logs de graphe dans InnovNation
 * TODO tout rajouter
 * 
 * @author Destannes Alexandre
 *
 */
public class GraphLogPack implements LogPack {

	private Game game;
	
	private static GraphInnovNation graph;
	

	private Collection<String> nbVotesList;
	private Collection<String> weightVotesList;
	private Collection<String> persuasionList;
	private HashMap<Integer,Collection<String>> logPNbVotes;
	private HashMap<Integer,Collection<String>> logPWeightVotes;
	private HashMap<Integer,Collection<String>> logPPersuasion;
	
	public static Viewer getInnovGraphViewer()
	{
		try {
			return graph.displayGraph();
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Viewer getVoteGraphViewer()
	{
		return graph.getNbVoteGraph().displayGraph(Long.MAX_VALUE);
	}
	public static Viewer getWeightVoteGraphViewer()
	{
		return graph.getWeightVoteGraph().displayGraph(Long.MAX_VALUE);
	}
	public static Viewer getPersuasionGraphViewer()
	{
		return graph.getPersuasionGraph().displayGraph(Long.MAX_VALUE);
	}
	
	public GraphLogPack(Game _game)
	{
		game = _game;
		nbVotesList = new ArrayList<String>();
		weightVotesList = new ArrayList<String>();
		persuasionList = new ArrayList<String>();
		logPNbVotes = new HashMap<Integer, Collection<String>>();
		logPWeightVotes = new HashMap<Integer, Collection<String>>();
		logPPersuasion = new HashMap<Integer, Collection<String>>();
		try {
			graph = new GraphInnovNation("1");
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
	}
	
	static public String titles() {
		StringBuilder sb = new StringBuilder("nbVoteGraph;weightVoteGraph;persuasionGraph");
		return sb.toString(); 
	}

	static public String zeros() {
		StringBuilder sb = new StringBuilder("[];[];[];");
		return sb.toString(); 
	}

	public String log(int time) {
		StringBuilder sb = new StringBuilder();

		/* nouvelles liste de log des sous graphes */
		Collection<String> newNbVotesList = graph.getNbVoteGraph().toStringArray();
		Collection<String> newWeightVotesList = graph.getWeightVoteGraph().toStringArray();
		Collection<String> newPersuasionList = graph.getPersuasionGraph().toStringArray();
		
		/* listes contenant la difference entre les nouvelles logs et les anciennes */
		Collection<String> diffNbVotesList = new ArrayList<String>();
		Collection<String> diffWeightVotesList = new ArrayList<String>();
		Collection<String> diffPersuasionList = new ArrayList<String>();
		
		diffNbVotesList.addAll(nbVotesList);
		diffNbVotesList.removeAll(newNbVotesList);
		diffWeightVotesList.addAll(weightVotesList);
		diffWeightVotesList.removeAll(newWeightVotesList);
		diffPersuasionList.addAll(persuasionList);
		diffPersuasionList.removeAll(newPersuasionList);
		
		nbVotesList = newNbVotesList;
		weightVotesList = newWeightVotesList;
		persuasionList = newPersuasionList;
		
		/* on ajoute les nouvelles logs du graphe nbVotes */
		String list = "";
		for (String s : diffNbVotesList)
		{
			if (list.length() > 0)
			{
				list += DynamicGraph.SEPARATOR;
			}
			list += s;
		}
		sb.append(DynamicGraph.LEFT_BRACE).append(list).append(DynamicGraph.RIGHT_BRACE).append(';');

		/* on ajoute les nouvelles logs du graphe weightVotes */
		list = "";
		for (String s : diffWeightVotesList)
		{
			if (list.length() > 0)
			{
				list += DynamicGraph.SEPARATOR;
			}
			list += s;
		}
		sb.append(DynamicGraph.LEFT_BRACE).append(list).append(DynamicGraph.RIGHT_BRACE).append(';');

		/* on ajoute les nouvelles logs du graphe persuasion */
		list = "";
		for (String s : diffPersuasionList)
		{
			if (list.length() > 0)
			{
				list += DynamicGraph.SEPARATOR;
			}
			list += s;
		}
		sb.append(DynamicGraph.LEFT_BRACE).append(list).append(DynamicGraph.RIGHT_BRACE).append(';');
		
		return sb.toString();
	}

	public HashMap<Integer,String> getLogpLogs(int time) throws IOException
	{
		HashMap<Integer,String> result = new HashMap<Integer,String>();
		HashMap<Integer,Collection<String>> newLogPNbVotes = new HashMap<Integer,Collection<String>>();
		HashMap<Integer,Collection<String>> newLogPWeightVotes = new HashMap<Integer,Collection<String>>();
		HashMap<Integer,Collection<String>> newLogPPersuasions = new HashMap<Integer,Collection<String>>();
		
		/* on genere les nouvelles logPList */
		GraphInnovNation graph2 = graph.clone();
		graph2.divideTime(SimAnalyzerLog.SYM_LOGP_STEP);
		
		DynamicGraph gNbVotes = graph2.getNbVoteGraph();
		DynamicGraph gWeightVotes = graph2.getWeightVoteGraph();
		DynamicGraph gPersuasion = graph2.getPersuasionGraph();

		Collection<String> edges;
		for(Integer p : game.getAllPlayersIds())
		{
			/* on recupere la liste des arcs + declaration node pour le joueur p sur le graphe nbVotes */
			edges = new ArrayList<String>();
			
			edges.add(gNbVotes.nodeToString(gNbVotes.getNode(p.toString())));
			
			for (Edge e : gNbVotes.getNode(p.toString()).getEachLeavingEdge())
			{
				edges.add(gNbVotes.edgeToString(e));
			}
			
			newLogPNbVotes.put(p, edges);

			/* on recupere la liste des arcs + declaration node pour le joueur p sur le graphe weightVotes */
			edges = new ArrayList<String>();
			
			edges.add(gWeightVotes.nodeToString(gWeightVotes.getNode(p.toString())));
			
			for (Edge e : gWeightVotes.getNode(p.toString()).getEachLeavingEdge())
			{
				edges.add(gWeightVotes.edgeToString(e));
			}
			
			newLogPWeightVotes.put(p, edges);

			/* on recupere la liste des arcs + declaration node pour le joueur p sur le graphe persuasion */
			edges = new ArrayList<String>();
			
			edges.add(gPersuasion.nodeToString(gPersuasion.getNode(p.toString())));
			
			for (Edge e : gPersuasion.getNode(p.toString()).getEachLeavingEdge())
			{
				edges.add(gPersuasion.edgeToString(e));
			}
			
			newLogPPersuasions.put(p, edges);
		}

		/* on calcule la difference pour chaque logP et on l'ajoute au resultat */
		Collection<String> difference = new ArrayList<String>();
		for(Integer p : game.getAllPlayersIds())
		{
			result.put(p,"");
			
			if (!logPNbVotes.containsKey(p))
			{
				logPNbVotes.put(p,new ArrayList<String>());
			}
			difference.addAll(newLogPNbVotes.get(p));
			difference.removeAll(logPNbVotes.get(p));
			result.put(p, result.get(p) + DynamicGraph.tabToString(difference) + ";");
			difference.clear();
			
			if (!logPWeightVotes.containsKey(p))
			{
				logPWeightVotes.put(p,new ArrayList<String>());
			}
			difference.addAll(newLogPWeightVotes.get(p));
			difference.removeAll(logPWeightVotes.get(p));
			result.put(p, result.get(p) + DynamicGraph.tabToString(difference) + ";");
			difference.clear();
			
			if (!logPPersuasion.containsKey(p))
			{
				logPPersuasion.put(p,new ArrayList<String>());
			}
			difference.addAll(newLogPPersuasions.get(p));
			difference.removeAll(logPPersuasion.get(p));
			result.put(p, result.get(p) + DynamicGraph.tabToString(difference) + ";");
			difference.clear();
		}
		
		logPNbVotes.clear();
		logPNbVotes.putAll(newLogPNbVotes);
		logPWeightVotes.clear();
		logPWeightVotes.putAll(newLogPWeightVotes);
		logPPersuasion.clear();
		logPPersuasion.putAll(newLogPPersuasions);
		
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
