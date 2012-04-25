package functions.logs;

import java.util.ArrayList;
import java.util.Collection;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.Viewer;

/**
 * 
 * @author Destannes Alexandre
 *
 */
public class GraphInnovNation extends MultiGraph{

	public static String CSS_FILE = "css\\style.css";
	
	public static String TIME_ADD = "timeAdd";
	public static String PLAYER_SOURCE = "playerSource";
	public static String PLAYER_TARGET = "playerTarger";
	public static String IDEA_SOURCE = "ideaSource";
	public static String IDEA_TARGET = "ideaTarger";
	public static String VOTE_SOURCE = "voteSource";
	public static String VOTE_TARGET = "voteTarger";
	public static String VOTE_NOTE = "voteNote";
	public static String VOTE_HIST = "voteHist";
	public static String VOTE_VALENCE = "voteHist";
	
	public static String PREFIX_IDEA = "I_";
	public static String PREFIX_PLAYER = "P_";
	public static String PREFIX_VOTE = "V_";
	
	public static String SEPARATOR = ";";
	
	private ArrayList<Integer> players;
	private ArrayList<Integer> ideas;
	private ArrayList<Integer> votes;
	private Integer root;
	
	private long lastEventTime;
	
	/**
	 * Constructeur de Graphe InnovNation
	 * @param idRootIdea : identifiant de l'idee de base
	 * @throws GraphInnovNationException : erreur si l'id du root est deja prise
	 */
	public GraphInnovNation(String idRootIdea) throws GraphInnovNationException
	{
		super("InnovNation");
		lastEventTime = 0;
		players = new ArrayList<Integer>();
		ideas = new ArrayList<Integer>();
		votes = new ArrayList<Integer>();
		init();
		
		addRoot(idRootIdea);
	}
	
	/**
	 * Ajoute une idee de type root au graphe
	 * @param id : l'id de l'idee root
	 * @throws GraphInnovNationException : erreur dans le cas ou l'id de l'idee est deja prise
	 */
	private void addRoot(String id) throws GraphInnovNationException
	{
		if (ideaExists(id))
		{
			throw new AlreadyTakenIdeaId(PREFIX_IDEA + id);
		}
		String IdeaId = PREFIX_IDEA + id;
		addNode(IdeaId);
		Node n = getNode(IdeaId);
		root = n.getIndex();
		n.addAttribute("ID", id);
		n.addAttribute("ui.class","root_idea");
		n.addAttribute("ui.label",IdeaId);
		n.addAttribute(TIME_ADD, Long.valueOf(0));
		n.addAttribute("layout.force", 0);
	}
	
	/**
	 * Initialise le graphe en gardant le CSS actuel
	 */
	private void init()
	{
        /* on vide le graphe actuel */
        clear();
        addAttribute("ui.stylesheet","url('" + CSS_FILE + "')");
        
        players.clear();
        votes.clear();
        ideas.clear();
	}
	
	/**
	 * Retourne retourne le temps du dernier event ajoute
	 * @return Integer : le temps du dernier event ajoute
	 */
	public long getLastEventTime()
	{
		return lastEventTime;
	}
	
	/**
	 * Retourne la liste des indexs des nodes Player
	 * @return ArrayList<Integer> : la liste des indexs des nodes Player
	 */
	public ArrayList<Integer> getPlayersIndexs()
	{
		return players;
	}
	
	/**
	 * Retourne la liste des indexs des nodes Idee
	 * @return ArrayList<Integer> : la liste des indexs des nodes Idee
	 */
	public ArrayList<Integer> getIdeasIndexs()
	{
		return ideas;
	}
	
	/**
	 * Retourne la liste des indexs des nodes Vote
	 * @return ArrayList<Integer> : la liste des indexs des nodes Vote
	 */
	public ArrayList<Integer> getVotesIndexs()
	{
		return votes;
	}
	
	/**
	 * Indique s'il existe un joueur ayant l'id donne
	 * @param id : l'id a tester
	 * @return vrai si le joueur existe, faux sinon
	 */
	public boolean playerExists(String id)
	{
		return getNode(PREFIX_PLAYER + id) != null;
	}
	
	/**
	 * Indique s'il existe un joueur ayant l'id donne au temps donne
	 * @param id : l'id a tester
	 * @param time : temps auquel l'existence du joueur est testee
	 * @return vrai si le joueur existe au temps donne, faux sinon
	 */
	public boolean playerExists(String id, long time)
	{
		return playerExists(id) && (Long)getNode(PREFIX_PLAYER + id).getAttribute(TIME_ADD) <= time;
	}
	
	/**
	 * Indique s'il existe un vote ayant l'id donne
	 * @param id : l'id a tester
	 * @return vrai si le vote existe, faux sinon
	 */
	public boolean voteExists(String id)
	{
		return getNode(PREFIX_VOTE + id) != null;
	}

	/**
	 * Indique s'il existe un vote ayant l'id donne au temps donne
	 * @param id : l'id a tester
	 * @param time : temps auquel l'existence du vote est testee
	 * @return vrai si le vote existe au temps donne, faux sinon
	 */
	public boolean voteExists(String id, long time)
	{
		return voteExists(id) && (Long)getNode(PREFIX_VOTE + id).getAttribute(TIME_ADD) <= time;
	}
	
	/**
	 * Indique s'il existe une idee ayant l'id donne
	 * @param id : l'id a tester
	 * @return vrai si l'idee existe, faux sinon
	 */
	public boolean ideaExists(String id)
	{
		return getNode(PREFIX_IDEA + id) != null;
	}

	/**
	 * Indique s'il existe une idee ayant l'id donne au temps donne
	 * @param id : l'id a tester
	 * @param time : temps auquel l'existence de l'idee est testee
	 * @return vrai si l'idee existe au temps donne, faux sinon
	 */
	public boolean ideaExists(String id, long time)
	{
		return ideaExists(id) && (Long)getNode(PREFIX_IDEA + id).getAttribute(TIME_ADD) <= time;
	}
	
	/**
	 * Ajoute un joueur au graphe
	 * @param id : identifiant du joueur
	 * @param timeAdd : temps auquel le joueur a ete ajoute
	 * @throws GraphInnovNationException
	 */
	public void addPlayer(String id, Long timeAdd) throws GraphInnovNationException
	{
		id = toRightIdFormat(id);
		String playerId = PREFIX_PLAYER + id;
		if (getNode(playerId) != null)
		{
			throw new AlreadyTakenPlayerId(playerId);
		}
		
		addNode(playerId);
		
		Node n = getNode(playerId);
		players.add(n.getIndex());
		n.addAttribute("ID", id);
		n.addAttribute(TIME_ADD, timeAdd);
		n.addAttribute("ui.class","player");
		n.addAttribute("ui.label",PREFIX_PLAYER + id);
		
		lastEventTime = Math.max(lastEventTime, timeAdd);
	}
	
	/**
	 * Ajoute une idee au graphe
	 * @param id : identifiant de l'idee
	 * @param idIdeasSource : liste des identifiants source de l'idee
	 * @param idPlayerSource : identifiant du joueur source de l'idee
	 * @param timeAdd : temps auquel l'idee a ete ajoutee
	 * @throws GraphInnovNationException
	 */
	
	public void addIdea(String id,ArrayList<String> idIdeasSource, String idPlayerSource, Long timeAdd) throws GraphInnovNationException
	{
		id = toRightIdFormat(id);
		for (String idIdea : idIdeasSource)
		{
			if (!ideaExists(idIdea))
			{
				throw new NonexistentIdeaId(idIdea);
			}
			if (!ideaExists(idIdea, timeAdd))
			{
				throw new TemporalIncoherence("Node " + idIdea + "( " + getNode(idIdea).getAttribute(TIME_ADD) + " ) does not exists at time " + timeAdd);
			}
		}
		if (ideaExists(id))
		{
			throw new AlreadyTakenIdeaId(PREFIX_IDEA + id);
		}
		if (!playerExists(idPlayerSource))
		{
			throw new NonexistentPlayerId(PREFIX_PLAYER + idPlayerSource);
		}
		if (!playerExists(idPlayerSource,timeAdd))
		{
			throw new TemporalIncoherence("Node " + PREFIX_PLAYER + idPlayerSource + "( " + getNode(PREFIX_PLAYER + idPlayerSource).getAttribute(TIME_ADD) + " ) does not exists at time " + timeAdd);
		}
		
		String ideaId     = PREFIX_IDEA + id,
			   playerId   = PREFIX_PLAYER + idPlayerSource;
		ArrayList<String> ideaSourceIds = new ArrayList<String>();
		
		for (String idIdea : idIdeasSource)
		{
			ideaSourceIds.add(PREFIX_IDEA + idIdea);
		}
			
		
		addNode(PREFIX_IDEA + id);
		
		Node n = getNode(ideaId);
		ideas.add(n.getIndex());
		n.addAttribute("ID", id);
		n.addAttribute("ui.class","idea");
		n.addAttribute("ui.label",PREFIX_IDEA + id);
		n.addAttribute(TIME_ADD, timeAdd);
		n.addAttribute(IDEA_SOURCE, ideaSourceIds);
		n.addAttribute(PLAYER_SOURCE, playerId);
		
		addEdge(playerId + " has idea " + ideaId, playerId, ideaId, false);
		Edge e = getEdge(playerId + " has idea " + ideaId);
		e.addAttribute("ui.class","has_idea");
		e.addAttribute("ui.label","has idea");
		e.addAttribute(TIME_ADD, timeAdd);
		
		for (String idIdea : ideaSourceIds)
		{
			addEdge(idIdea + " from " + ideaId, ideaId, idIdea, true);
			e = getEdge(idIdea + " from " + ideaId);
			e.addAttribute("ui.class","fromidea");
			e.addAttribute("ui.label","from");
			e.addAttribute(TIME_ADD, timeAdd);
		}
		lastEventTime = Math.max(lastEventTime, timeAdd);
	}
	
	/**
	 * Ajoute un vote au graphe, on le modifie s'il existe deja
	 * @param id : identifiant du vote
	 * @param idPlayerSource : identifiant du joueur source du vote
	 * @param idIdeaTarget : identifiant de l'idee cible du vote
	 * @param vote : note du vote
	 * @param timeAdd : temps auquel le vote a ete ajoute
	 * @throws GraphInnovNationException
	 */
	public void addVote(String id,String idPlayerSource, String idIdeaTarget, Integer vote,Integer valence, Long timeAdd) throws GraphInnovNationException
	{
		id = toRightIdFormat(id);
		if (!ideaExists(idIdeaTarget))
		{
			throw new NonexistentIdeaId(PREFIX_IDEA + idIdeaTarget);
		}
		if (!ideaExists(idIdeaTarget,timeAdd))
		{
			throw new TemporalIncoherence("Node " + PREFIX_IDEA + idIdeaTarget + "( " + getNode(PREFIX_IDEA + idIdeaTarget).getAttribute(TIME_ADD) + " ) does not exists at time " + timeAdd);
		}
		if (!playerExists(idPlayerSource))
		{
			throw new NonexistentPlayerId(PREFIX_PLAYER + idPlayerSource);
		}
		if (!playerExists(idPlayerSource,timeAdd))
		{
			throw new TemporalIncoherence("Node " + PREFIX_PLAYER + idPlayerSource + "( " + getNode(PREFIX_PLAYER + idPlayerSource).getAttribute(TIME_ADD) + "-" + " ) does not exists at time " + timeAdd);
		}
		
		String ideaId     = PREFIX_IDEA + idIdeaTarget,
			   playerId   = PREFIX_PLAYER + idPlayerSource,
			   voteId     = PREFIX_VOTE + id;
				
		/* si le vote existe deja, on l'ajoute a l'historique de la branche */
		Edge e = null;
		for (Edge sortant : getNode(playerId).getEachLeavingEdge())
		{
			if (sortant.getOpposite(getNode(playerId)).equals(getNode(ideaId)) && sortant.getAttribute(VOTE_NOTE) != null )
			{
				e = sortant;
				break;
			}
		}
		
		ArrayList<long[]> hist;
		if (e != null)
		{
			
			if (e.getAttribute(TIME_ADD) == timeAdd){
				throw new TemporalIncoherence("Cannot update Edge " + PREFIX_VOTE + id + " at the time it is created (" + timeAdd + ")");
			}
			
			if (e.getOpposite(getNode(playerId)).equals(getNode(ideaId)))
			{
				hist = e.getAttribute(VOTE_HIST); 
				int pos = 0;
				for (pos = 0 ; pos < hist.size(); pos++)
				{
					if (hist.get(pos)[0] == timeAdd)
					{
						throw new TemporalIncoherence("Cannot update Edge " + PREFIX_VOTE + id + " : an update already exists at time " + timeAdd);
					}
					if (hist.get(pos)[0] > timeAdd)
					{
						break;
					}
				}
				hist.add(pos,new long[]{timeAdd,vote,valence});
				e.setAttribute(VOTE_NOTE, ((Integer)e.getAttribute(VOTE_NOTE)) + vote);
			}
			else
			{
				throw new AlreadyTakenVoteId(voteId);
			}
		}
		else
		{
			addEdge(voteId,playerId,ideaId,true);
				
			e = getEdge(voteId);
			votes.add(e.getIndex());
			e.addAttribute("ID", id);
			e.addAttribute(VOTE_NOTE, vote);
			e.addAttribute("ui.class","vote");
			e.addAttribute(TIME_ADD, timeAdd);
			e.addAttribute(VOTE_HIST, new ArrayList<long[]>());
			e.addAttribute(IDEA_TARGET, ideaId);
			e.addAttribute(PLAYER_SOURCE, playerId);
			hist = e.getAttribute(VOTE_HIST); 
			hist.add(new long[]{timeAdd,vote,valence});
		}
		
		hist = e.getAttribute(VOTE_HIST);
		String label = "vote ";
		if (hist.get(hist.size()-1)[2] == -1)
		{
			label += "NEG ";
		}
		else if (hist.get(hist.size()-1)[2] == 1)
		{
			label += "POS ";
		}
		else if (hist.get(hist.size()-1)[2] == 0)
		{
			label += "NEU ";
		}
		
		long totalVote = 0;
		for (long[] h : hist)
		{
			totalVote += h[1];
		}
		
		if (totalVote > 0)
		{
			label += "+" + totalVote;
		}
		else if (totalVote < 0)
		{
			label += + totalVote;
		}
		e.addAttribute("ui.label",label);
		
		lastEventTime = Math.max(lastEventTime, timeAdd);
	}
	
	/**
	 * Affiche le graphe
	 * @return Viewer : un Viewer pour le graphe
	 */
	public Viewer displayGraph() throws GraphInnovNationException
	{
		
		return displayGraph(Long.MAX_VALUE);
	}
	
	/**
	 * Affiche le graphe au temps donne
	 * @param time : le temps auquel le graphe doit etre affiche
	 * @return Viewer : un Viewer du graphe
	 * @throws GraphInnovNationException
	 */
	public Viewer displayGraph(long time) throws GraphInnovNationException
	{
		return getGraphAtTime(time).display();
	}
	
	/**
	 * Cree un graphe ne contenant que les noeuds et arretes a un instant donne
	 * @param time : instant de creation du graphe
	 * @return GraphInnovNation : le graphe au temps time
	 * @throws GraphInnovNationException
	 */
	private GraphInnovNation getGraphAtTime(long time) throws GraphInnovNationException
	{
		GraphInnovNation g = new GraphInnovNation((String) getNode(root).getAttribute("ID"));
		Node tmpn;
		Edge tmpe;
		
		for (Integer p : players)
		{
			tmpn = getNode(p);
			if ((Long)tmpn.getAttribute(TIME_ADD) <= time)
			{
				g.addPlayer((tmpn.getId()).substring(PREFIX_PLAYER.length()), (Long)tmpn.getAttribute(TIME_ADD));
			}
		}
		
		for (Integer p : ideas)
		{
			tmpn = getNode(p);
			if ((Long)tmpn.getAttribute(TIME_ADD) <= time)
			{	
				ArrayList<String> idsSource = tmpn.getAttribute(IDEA_SOURCE),
								  ids = new ArrayList<String>();
				for (String idSource : idsSource)
				{
					ids.add(idSource.substring(PREFIX_IDEA.length()));
				}
				
				g.addIdea((tmpn.getId()).substring(PREFIX_IDEA.length()),ids, ((String)tmpn.getAttribute(PLAYER_SOURCE)).substring(PREFIX_PLAYER.length()), (Long)tmpn.getAttribute(TIME_ADD));
			}
		}
		
		for (Integer p : votes)
		{
			tmpe = getEdge(p);
			if ((Long)tmpe.getAttribute(TIME_ADD) <= time)
			{
				ArrayList<long[]> hist = tmpe.getAttribute(VOTE_HIST);
				for (long[] h : hist)
				{
					if (h[0] > time)
					{
						break;
					}
					g.addVote((tmpe.getId()).substring(PREFIX_VOTE.length()), ((String)tmpe.getAttribute(PLAYER_SOURCE)).substring(PREFIX_PLAYER.length()), ((String)tmpe.getAttribute(IDEA_TARGET)).substring(PREFIX_IDEA.length()), ((Long)h[1]).intValue(), ((Long)h[2]).intValue(), h[0]);
				}
			}
		}
		
		if (hasAttribute("ui.stylesheet"))
		{
			g.setAttribute("ui.stylesheet", getAttribute("ui.stylesheet"));
		}
		
		return g;
	}

	/**
	 * Charge un graphe depuis un fichier de logs du logiciel InnovNation
	 * @param logFile : le fichier source des logs InnovNation
	 * @throws GraphInnovNationException
	 */
	/*public void loadGraphFromInnovnationLogs(String logFile) throws GraphInnovNationException
	{
        File fileSource = new File(logFile);
        Matrix matrix = null;
        
        init();
        
        /* on charge la matrice des logs en memoire *
        try {
        	matrix=MatrixFactory.importFromFile(FileFormat.CSV, fileSource,SEPARATOR);
		} catch (MatrixException e) {
			System.out.println("Erreur sur le chargementde la matrice : \n");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Erreur sur le chargement du fichier : \n");
			e.printStackTrace();
			System.exit(-2);
		}
        
		for(long column = 0; column < matrix.getColumnCount(); column++ ){
			matrix.setColumnLabel(column, matrix.getAsString(0,column));
		}
		
		String type;
		Long time;
		
		/* on regarde chaque ligne et recupere les donnees graphes *
		for(long row = 1; row < matrix.getRowCount(); row++ ){
			type = matrix.getAsString(row,matrix.getColumnForLabel("type"));
			time = Long.valueOf(matrix.getAsInt(row,matrix.getColumnForLabel("time")));

			/* si il n'y a aucune node, on cree la node root *
			if (getNodeCount() == 0)
			{
				String idRoot = matrix.getAsString(row,matrix.getColumnForLabel("rootIdeaId"));
				addRoot(idRoot);
			}
			
			
			/* si le nombre de joueur a augmente, on ajoute les jours manquant *
			if (matrix.getAsInt(row,matrix.getColumnForLabel("players")) != players.size())
			{
				String playersList = matrix.getAsString(row,matrix.getColumnForLabel("playersList"));
				
				for (String id : playersList.substring(1, playersList.length()-1).split(","))
				{
					if (!playerExists(id))
					{
						//System.out.println("ajout joueur (t="+time+") : ID=" + id);
						addPlayer(id, time);
					}
				}
			}
			
			/* si la ligne est de type idea, on ajoute l'idee *
			if (type.equals("idea"))
			{
				String id="",idPlayerSource="",idIdeaSource="";
				ArrayList<String> idIdeasSource = new ArrayList<String>();
				
				id = matrix.getAsString(row,matrix.getColumnForLabel("ideaId"));
				idIdeaSource = matrix.getAsString(row,matrix.getColumnForLabel("ideaParentsId"));
				idPlayerSource = matrix.getAsString(row,matrix.getColumnForLabel("ideaOwnerId"));
				
				//System.out.println("ajout idee (t="+time+") : ID=" + id + " IDEE_SOURCE=" + idIdeaSource + " JOUEUR_SOURCE=" + idPlayerSource);
				
				for (String idIdea : idIdeaSource.substring(1, idIdeaSource.length()-1).split(","))
				{
					idIdeasSource.add(idIdea);
				}
				
				addIdea(id, idIdeasSource, idPlayerSource, time);
			}
			
			/* si la ligne est de type vote, on ajoute le vote *
			else if(type.equals("vote"))
			{
				String id="",idPlayerSource="",idIdeaTarget="";
				Integer vote = 0, valence = 0;
				
				id = matrix.getAsString(row,matrix.getColumnForLabel("commentId"));
				idIdeaTarget = matrix.getAsString(row,matrix.getColumnForLabel("commentIdeaId"));
				idPlayerSource = matrix.getAsString(row,matrix.getColumnForLabel("commentOwnerId"));
				vote = matrix.getAsInt(row,matrix.getColumnForLabel("voteTokens"));
				valence = matrix.getAsInt(row,matrix.getColumnForLabel("voteValence"));
				
				//System.out.println("ajout vote (t="+time+") : ID=" + id + " IDEE_CIBLE=" + idIdeaTarget + " JOUEUR_SOURCE=" + idPlayerSource + " VOTE=" + vote);
				
				addVote(id, idPlayerSource, idIdeaTarget, vote, valence, time);
			}
		}
	}*/
	
	/** 
	 * Transforme l'id donne dans un format qui ne posera aucun probleme a l'application
	 * @param id : l'id a formater
	 * @return String : le nouvel id formate
	 */
    private static String toRightIdFormat(String id)
	{
		return id.replace(":", "_").replace(";", "|").replace("[", "{").replace("]", "}");
	}
    
    /* fonctions de converstion en GraphGeneric */
    
    /**
     * Retourne un GraphGeneric ou chaque branche contient le nombre de vote de chaque joueur envers un autre joueur (pour une de ses idees)
     * @return GraphGeneric des votes
     */
    public DynamicGraph getNbVoteGraph()
    {
    	DynamicGraph g = new DynamicGraph();
    	Node player,target;
    	
    	/* on ajoute les joueurs */
    	for (Integer p : players)
    	{
    		g.addNode((String)getNode(p).getAttribute("ID"));
    	}
    	/* on ajoute la racine */
    	g.addNode("root");
    	
    	for (Integer p : players)
    	{
    		player = getNode(p);
    		
    		/* on recupere la liste des arcs sortant puis on les trie par date d'ajout */
    		Collection<Edge> voteList = player.getLeavingEdgeSet();
    		ArrayList<Edge> sortedVoteList = new ArrayList<Edge>();
    		
    		for (Edge v : voteList)
    		{
    			int index = 0;
    			long time = v.getAttribute(TIME_ADD);
    			
    			for (index = 0 ; index < sortedVoteList.size(); index++)
    			{
    				if (time < (Long)sortedVoteList.get(index).getAttribute(TIME_ADD))
    				{
    					break;
    				}
    			}
        		sortedVoteList.add(index, v);
    		}
    		
    		for (Edge e : sortedVoteList)
    		{
    			if (e.getAttribute(VOTE_NOTE) != null)
    			{
    				/* on a un arc vote, on l'ajoute au graphe */
    				
    				target = getNode((String)e.getOpposite(player).getAttribute(PLAYER_SOURCE));
    				
    				/* on augmente le poids de l'arc ou on le cree s'il n'existe pas */
    				
    				long timeAdd = (Long)e.getAttribute(TIME_ADD);
    				Node nSource = g.getNode((String)player.getAttribute("ID")),
    					 nTarget;
    				int weight = 1;

        			if (target == null)
        			{
        				nTarget = g.getNode("root");
        			}
        			else
        			{
        				nTarget = g.getNode((String)target.getAttribute("ID"));
        			}
        			
    				if (g.edgesExists(nSource,nTarget,timeAdd,timeAdd))
    				{     				
    					weight += g.getWeight(g.getEdge(nSource.getId(), nTarget.getId(), timeAdd));
    				}
    				
    				
    				g.insertEdge(nSource.getId(),nTarget.getId(), weight, timeAdd, Long.MAX_VALUE);
    			
    			}
    		}
    		
    	}
    	
    	return g;
    }
    
    /**
     * Retourne un GraphGeneric ou chaque branche contient la somme des votes de chaque joueur envers un autre joueur (pour une de ses idees)
     * @return GraphGeneric des votes
     */
    public DynamicGraph getWeightVoteGraph()
    {
    	DynamicGraph g = new DynamicGraph();
    	Node player,target;
    	
    	/* on ajoute les joueurs */
    	for (Integer p : players)
    	{
    		g.addNode((String)getNode(p).getAttribute("ID"));
    	}
    	/* on ajoute la racine */
    	g.addNode("root");
    	
    	for (Integer p : players)
    	{
    		player = getNode(p);
    		
    		/* on recupere la liste des arcs sortant puis on les trie par date d'ajout */
    		Collection<Edge> voteList = player.getLeavingEdgeSet();
    		ArrayList<Edge> sortedVoteList = new ArrayList<Edge>();
    		
    		for (Edge v : voteList)
    		{
    			
    			int index = 0;
    			long time = v.getAttribute(TIME_ADD);
    			
    			for (index = 0 ; index < sortedVoteList.size(); index++)
    			{
    				if (time < (Long)sortedVoteList.get(index).getAttribute(TIME_ADD))
    				{
    					break;
    				}
    			}
        		sortedVoteList.add(index, v);
    		}
    		
    		for (Edge e : sortedVoteList)
    		{
    			if (e.getAttribute(VOTE_NOTE) != null)
    			{
    				/* on a un arc vote, on l'ajoute au graphe */
    				target = getNode((String)e.getOpposite(player).getAttribute(PLAYER_SOURCE));

    				
    				/* on augmente le poids de l'arc ou on le cree s'il n'existe pas */
    				
    				long timeAdd = (Long)e.getAttribute(TIME_ADD);
    				Node nSource = g.getNode((String)player.getAttribute("ID")),
    					 nTarget;
    				int weight = e.getAttribute(VOTE_NOTE);
    				
        			if (target == null)
        			{
        				nTarget = g.getNode("root");
        			}
        			else
        			{
        				nTarget = g.getNode((String)target.getAttribute("ID"));
        			}
    				
    				if (g.edgesExists(nSource,nTarget,timeAdd,timeAdd))
    				{    				
    					weight += g.getWeight(g.getEdge(nSource.getId(), nTarget.getId(), timeAdd));
    				}
    				
    				g.insertEdge(nSource.getId(),nTarget.getId(), weight, timeAdd, Long.MAX_VALUE);
    			
    			}
    		}
    		
    	}
    	
    	return g;
    }
}

class GraphInnovNationException extends Exception{
	private static final long serialVersionUID = 1L;
	private String message;
	
	public GraphInnovNationException()
	{
		message = "";
	}

	public GraphInnovNationException(String _message)
	{
		message = _message;
	}
	
	public String toString()
	{
		return message;
	}
}

class TemporalIncoherence extends GraphInnovNationException
{
	private static final long serialVersionUID = 1L;
	TemporalIncoherence()
	{
		super("Temporal incoherence");
	}
	TemporalIncoherence(String _message)
	{
		super("Temporal incoherence : " + _message);
	}
}
class NonexistentIdeaId extends GraphInnovNationException
{
	private static final long serialVersionUID = 1L;
	NonexistentIdeaId()
	{
		super("Idea id non existent error");
	}
	NonexistentIdeaId(String _message)
	{
		super("Idea id non existent error : " + _message);
	}
}
class NonexistentPlayerId extends GraphInnovNationException
{
	private static final long serialVersionUID = 1L;
	NonexistentPlayerId()
	{
		super("Player id non existent error");
	}
	NonexistentPlayerId(String _message)
	{
		super("Player id non existent error : " + _message);
	}
}
class NonexistentVoteId extends GraphInnovNationException
{
	private static final long serialVersionUID = 1L;
	NonexistentVoteId()
	{
		super("Vote id non existent error");
	}
	NonexistentVoteId(String _message)
	{
		super("Vote id non existent error : " + _message);
	}
}
class AlreadyTakenIdeaId extends GraphInnovNationException
{
	private static final long serialVersionUID = 1L;
	AlreadyTakenIdeaId()
	{
		super("Idea id already taken error");
	}
	AlreadyTakenIdeaId(String _message)
	{
		super("Idea id already taken error : " + _message);
	}
}
class AlreadyTakenPlayerId extends GraphInnovNationException
{
	private static final long serialVersionUID = 1L;
	AlreadyTakenPlayerId()
	{
		super("Player id already taken error");
	}
	AlreadyTakenPlayerId(String _message)
	{
		super("Player id already taken error : " + _message);
	}
}
class AlreadyTakenVoteId extends GraphInnovNationException
{
	private static final long serialVersionUID = 1L;
	AlreadyTakenVoteId()
	{
		super("Vote id already taken error");
	}
	AlreadyTakenVoteId(String _message)
	{
		super("Vote id already taken error : " + _message);
	}
}