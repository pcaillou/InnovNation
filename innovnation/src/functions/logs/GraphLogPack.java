package functions.logs;

import java.util.ArrayList;
import java.util.Collection;

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
	
	
	public static Viewer getInnovGraphViewer()
	{
		try {
			return graph.displayGraph();
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public GraphLogPack(Game _game)
	{
		game = _game;
		nbVotesList = new ArrayList<String>();
		weightVotesList = new ArrayList<String>();
		try {
			graph = new GraphInnovNation("1");
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
	}
	
	static public String titles() {
		StringBuilder sb = new StringBuilder("nbVoteGraph;weightVoteGraph;");
		return sb.toString(); 
		
	}

	static public String zeros() {
		StringBuilder sb = new StringBuilder("[];[];");
		return sb.toString(); 
	}
	
	public String log(int time) {
		StringBuilder sb = new StringBuilder();

		/* nouvelles liste de log des sous graphes */
		Collection<String> newNbVotesList = graph.getNbVoteGraph().toStringArray();
		Collection<String> newWeightVotesList = graph.getWeightVoteGraph().toStringArray();
		
		/* listes contenant la difference entre les nouvelles logs et les anciennes */
		Collection<String> diffNbVotesList = new ArrayList<String>();
		Collection<String> diffWeightVotesList = new ArrayList<String>();
		
		diffNbVotesList.addAll(nbVotesList);
		diffNbVotesList.removeAll(newNbVotesList);
		diffWeightVotesList.addAll(weightVotesList);
		diffWeightVotesList.removeAll(newWeightVotesList);
		
		nbVotesList = newNbVotesList;
		weightVotesList = newWeightVotesList;
		
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
		
		return sb.toString();
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
