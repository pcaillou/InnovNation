package functions.logs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.IdAlreadyInUseException;
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
	public static String IDEA_HAS_LABEL = "has idea";
	public static String IDEA_FROM_LABEL = "from";
	public static String VOTE_SOURCE = "voteSource";
	public static String VOTE_TARGET = "voteTarger";
	public static String VOTE_NOTE = "voteNote";
	public static String VOTE_HIST = "voteHist";
	public static String VOTE_VALENCE = "voteHist";
	
	public static String PREFIX_IDEA = "I_";
	public static String PREFIX_PLAYER = "P_";
	public static String PREFIX_VOTE = "V_";
	
	public static final char LEFT_BRACE =  '[';
	public static final char RIGHT_BRACE = ']';
	public static final char SEPARATOR =   ':';
	
	private static final int TYPE_NODE_PLAYER = 1;
	private static final int TYPE_NODE_IDEA = 2;
	private static final int TYPE_NODE_ROOT = 3;
	private static final int TYPE_EDGE_VOTE = 4;
	private static final int TYPE_EDGE_IDEA_PARENT = 5;
	private static final int TYPE_EDGE_IDEA_SOURCE = 6;
	
	
	private ArrayList<Integer> players;
	private ArrayList<Integer> ideas;
	private ArrayList<Integer> votes;
	private Integer root;
	
	private long lastEventTime;
	
	/**
	 * Retourne le tableau sous forme de chaine en utilisant le separateur et les encadrant definit dans LEFT_BRACE, RIGHT_BRACE, SEPARATOR
	 * @param tab : la chaine sous forme de tableau {cell1,cell2,cell3,...}
	 * @return la chaine au format "[cell1,cell2,cell3,...]"
	 */
	public static String tabToString(Collection<String> tab)
	{
		String result = LEFT_BRACE + "";
		for(String s : tab)
		{
			if (result.length() > 1)
			{
				result += SEPARATOR;
			}
			result += s;
		}
		result += RIGHT_BRACE;
		
		return result;
	}
	
	/**
	 * Retourne la chaine sous forme de tableau en utilisant le separateur et les encadrant definit dans LEFT_BRACE, RIGHT_BRACE, SEPARATOR
	 * @param tab : la chaine au format "[cell1,cell2,cell3,...]"
	 * @return la chaine sous forme de tableau {cell1,cell2,cell3,...}
	 */
	public static ArrayList<String> stringToTab(String tab)
	{
		Integer level = 0;
		String buffer = "";
		ArrayList<String> result = new ArrayList<String>();
		tab = tab.substring(1, tab.length()-1);
		
		/* on parcourt la chaine pour recuperer toutes les cellules */
		for (char c : tab.toCharArray())
		{
			if (c == LEFT_BRACE)
			{
				level++;
			}
			else if (c == RIGHT_BRACE)
			{
				level--;
			}
			
			if (c == SEPARATOR && level == 0)
			{
				result.add(buffer);
				buffer = "";
			}
			else
			{
				buffer+= c;
			}
		}
		
		if (buffer != "")
		{
			result.add(buffer);
		}
		
		return result;
	}
	
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
		root = -1;
		addRoot(idRootIdea);
	}

	/**
	 * Constructeur de Graphe InnovNation sans racine
	 * @throws GraphInnovNationException : erreur si l'id du root est deja prise
	 */
	public GraphInnovNation() throws GraphInnovNationException
	{
		super("InnovNation");
		lastEventTime = 0;
		players = new ArrayList<Integer>();
		ideas = new ArrayList<Integer>();
		votes = new ArrayList<Integer>();
		init();
	}
	
	/**
	 * Ajoute une idee de type root au graphe
	 * @param id : l'id de l'idee root
	 * @throws GraphInnovNationException : erreur dans le cas ou l'id de l'idee est deja prise
	 */
	public void addRoot(String id) throws GraphInnovNationException
	{
		if (root != -1)
		{
			removeNode(root);
		}
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
		n.addAttribute("layout.force", 0);
		n.addAttribute(TIME_ADD, Long.valueOf(0));
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
		e.addAttribute("ui.label",IDEA_HAS_LABEL);
		e.addAttribute(TIME_ADD, timeAdd);

		for (String idIdea : ideaSourceIds)
		{
			try
			{
				e = addEdge(idIdea + " from " + ideaId, ideaId, idIdea, true);
				e.addAttribute("ui.class","fromidea");
				e.addAttribute("ui.label",IDEA_FROM_LABEL);
				e.addAttribute(TIME_ADD, timeAdd);
			}
			catch(IdAlreadyInUseException ex)
			{
				System.err.println("Error : impossible to add edge : " + idIdea + " from " + ideaId);
			}

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
			
			/*if (e.getAttribute(TIME_ADD) == timeAdd){
				throw new TemporalIncoherence("Cannot update Edge " + PREFIX_VOTE + id + " at the time it is created (" + timeAdd + ")");
			}*/
			
			if (e.getOpposite(getNode(playerId)).equals(getNode(ideaId)))
			{
				hist = e.getAttribute(VOTE_HIST); 
				int pos = 0;
				for (pos = 0 ; pos < hist.size(); pos++)
				{
					/*if (hist.get(pos)[0] == timeAdd)
					{
						throw new TemporalIncoherence("Cannot update Edge " + PREFIX_VOTE + id + " : an update already exists at time " + timeAdd);
					}*/
					if (hist.get(pos)[0] > timeAdd)
					{
						break;
					}
				}
				hist.add(pos,new long[]{timeAdd,vote,valence});
			}
			else
			{
				throw new AlreadyTakenVoteId(voteId);
			}
		}
		else
		{
			e = addEdge(voteId,playerId,ideaId,true);
			
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
	 * Divise le temps de creation et les historiques de tout les arcs et noeuds
	 * @param factor : tout les temps seront multiplies par cette valeur
	 */
	public void divideTime(Integer step)
	{
		for(Node n : getEachNode())
		{
			n.setAttribute(TIME_ADD,(long)(((Long)n.getAttribute(TIME_ADD))/step));
		}
		
		for (Edge e : getEachEdge())
		{
			e.setAttribute(TIME_ADD,(long)(((Long)e.getAttribute(TIME_ADD))/step));
		}
		
		for (Integer i : votes)
		{
			Edge e = getEdge(i);
			
			ArrayList<long[]> hist = e.getAttribute(VOTE_HIST);
			
			for (long[] h : hist)
			{
				h[0] = (long)(h[0]/step);
			}
		}
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
	 * Clone le graphe
	 * @return GraphInnovNation
	 */
	public GraphInnovNation clone()
	{
		GraphInnovNation g = null;
		try {
			g = new GraphInnovNation((String) getNode(root).getAttribute("ID"));
	
			Node tmpn;
			Edge tmpe;
			
			for (Integer p : players)
			{
				tmpn = getNode(p);
				try {
					g.addPlayer((tmpn.getId()).substring(PREFIX_PLAYER.length()), (Long)tmpn.getAttribute(TIME_ADD));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for (Integer p : ideas)
			{
				tmpn = getNode(p);
				ArrayList<String> idsSource = tmpn.getAttribute(IDEA_SOURCE),
								  ids = new ArrayList<String>();
				for (String idSource : idsSource)
				{
					ids.add(idSource.substring(PREFIX_IDEA.length()));
				}
				
				g.addIdea((tmpn.getId()).substring(PREFIX_IDEA.length()),ids, ((String)tmpn.getAttribute(PLAYER_SOURCE)).substring(PREFIX_PLAYER.length()), (Long)tmpn.getAttribute(TIME_ADD));
			}
			
			for (Integer p : votes)
			{
				tmpe = getEdge(p);
				ArrayList<long[]> hist = tmpe.getAttribute(VOTE_HIST);
				try {
					for (long[] h : hist)
					{
						g.addVote((tmpe.getId()).substring(PREFIX_VOTE.length()), ((String)tmpe.getAttribute(PLAYER_SOURCE)).substring(PREFIX_PLAYER.length()), ((String)tmpe.getAttribute(IDEA_TARGET)).substring(PREFIX_IDEA.length()), ((Long)h[1]).intValue(), ((Long)h[2]).intValue(), h[0]);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (hasAttribute("ui.stylesheet"))
			{
				g.setAttribute("ui.stylesheet", getAttribute("ui.stylesheet"));
			}
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
		return g;
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
				
				g.addIdea(
						(tmpn.getId()).substring(PREFIX_IDEA.length()),
						ids, 
						((String)tmpn.getAttribute(PLAYER_SOURCE)).substring(PREFIX_PLAYER.length()), 
						(Long)tmpn.getAttribute(TIME_ADD));
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
					g.addVote(
							(tmpe.getId()).substring(PREFIX_VOTE.length()), 
							((String)tmpe.getAttribute(PLAYER_SOURCE)).substring(PREFIX_PLAYER.length()), 
							((String)tmpe.getAttribute(IDEA_TARGET)).substring(PREFIX_IDEA.length()), 
							((Long)h[1]).intValue(), 
							((Long)h[2]).intValue(), h[0]);
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
		return id.replace(SEPARATOR, '_').replace(';', '|').replace(LEFT_BRACE, '{').replace(RIGHT_BRACE, '}');
	}
    
    /* fonctions de converstion en GraphGeneric */
    
    /**
     * Retourne le graphe de persuasion ou chaque arc indique le nombre de points de persuation que donne chaque joueur a un autre
     * @return DynamicGraph
     */
    public DynamicGraph getPersuationGraph()
    {
		DynamicGraph g = new DynamicGraph();
		
		/* on ajoute les joueurs au graphe */
		for (Integer p : getPlayersIndexs())
		{
			g.addNode((String)getNode(p).getAttribute("ID"));
		}
		
		double distcoef=1.0;
		Node idea;
		
		HashMap<Integer,Integer> lastVoteTime, lastVoteValence;
		
		/* on cree le comparateur pour trier les votes sur le temps */
		//Collections.sort(list, c);
		Comparator<int[]> comparator = new Comparator<int[]>() {
			// 01 plus petit que 02 => neg
			public int compare(int[] o1, int[] o2) {
				if (o1[0] < o2[0])
				{
					return -1;
				}
				else if (o1[0] > o2[0])
				{
					return 1;
				}
				return 0;
			} 
		};
		
		for (Integer i : getIdeasIndexs())
		{
			idea = getNode(i);
			
			/* liste des votes pour le noeud {time,source,poids,valence} */
			List<int[]> votes = new ArrayList<int[]>();

			/* on ajoute les votes dans le desordre */
			for (Edge e : idea.getEachEnteringEdge())
			{
				if (e.getAttribute("ui.label").equals(IDEA_FROM_LABEL) || e.getAttribute("ui.label").equals(IDEA_HAS_LABEL))
				{
					continue;
				}
				
				Integer indexSource = e.getOpposite(idea).getIndex();
			
				ArrayList<long[]> hist = e.getAttribute(VOTE_HIST);
				
				for (long[] h : hist)
				{
					votes.add(new int[]{(int) h[0],indexSource,(int) h[1],(int) h[2]});
				}
			}

			/* on trie les votes par temps d'arrivee */
			Collections.sort(votes,comparator);
			

			lastVoteTime = new HashMap<Integer, Integer>();
			lastVoteValence = new HashMap<Integer, Integer>();
			int poids = 0;
			for (int[] v : votes) // {time,source,poids,valence} 
			{
				
				lastVoteTime.put(v[1], v[0]);
				lastVoteValence.put(v[1], v[3]);
				if (v[3] == 0)
				{
					if (v[2] > 0)
					{
						lastVoteValence.put(v[1], 1);
					}
					else if (v[2] < 0)
					{
						lastVoteValence.put(v[1], -1);
					}
				}

				poids = poids - v[2];
				
				if (poids != 0)
				{
					// ajouter au graphe les arc qui contiennent la valeur de persuation
					for (int j = 0 ; j < getPlayersIndexs().size(); j++)
					{
						if (j == v[1] || !lastVoteTime.containsKey(v[1]) || !lastVoteTime.containsKey(j))
						{
							continue;
						}
						
						int distance = lastVoteTime.get(v[1]) - lastVoteTime.get(j);
						/* si les votes sont arrives en m�me temps, on considere que chacun persuade l'autre */
						if (distance == 0)
						{
							distance = 1;
						}
						int value = 0;
						
						if (g.edgesExists(g.getNode((String)getNode(j).getAttribute("ID")), g.getNode((String)getNode(v[1]).getAttribute("ID")), v[0], v[0]))
						{
							Edge e = g.getEdge((String)getNode(j).getAttribute("ID"), (String)getNode(v[1]).getAttribute("ID"), v[0]);
							value += g.getWeight(e);
							g.removeEdge(e);
							
						}
						
						value += (int) (poids*lastVoteValence.get(j)) * 100 / (distcoef*distance);
												
						g.addEdge(
								(String)getNode(j).getAttribute("ID"), 
								(String)getNode(v[1]).getAttribute("ID"), 
								value, 
								v[0], 
								v[0]
										);
						
					}
				}
				poids = v[2];
			}
		}
		
		/* on recree un graphe resultat a partir de celui genere mais en sommant les valeurs sur le temps */
		
		DynamicGraph result = new DynamicGraph();
		
		/* on ajoute les joueurs au graphe */
		for (Integer p : getPlayersIndexs())
		{
			result.addNode((String)getNode(p).getAttribute("ID"));
		}
		
		Node source, target;
		long timeAdd;
		
		/* on cree une liste d'arc que l'on trie */
		List<Edge> edges = new ArrayList<Edge>();
		for (Edge e : g.getEachEdge())
		{
			edges.add(e);
		}
		Collections.sort(edges,new Comparator<Edge>() {
			public int compare(Edge arg0, Edge arg1) {
				if ((Long)arg0.getAttribute(DynamicGraph.TIME_CREATION) < (Long)arg1.getAttribute(DynamicGraph.TIME_CREATION))
				{
					return -1;
				}
				else if ((Long)arg0.getAttribute(DynamicGraph.TIME_CREATION) > (Long)arg1.getAttribute(DynamicGraph.TIME_CREATION))
				{
					return 1;
				}
				return 0;
			}

		});
			
			
		for (Edge e : edges)
		{
			int poids = 0;
			
			source = result.getNode(e.getSourceNode().getId());
			target = result.getNode(e.getTargetNode().getId());
			timeAdd = g.getTimeCreation(e);
			
			if (result.edgesExists(source,target,timeAdd,timeAdd))
			{
				Edge edge = result.getEdge(source.getId(), target.getId(),timeAdd);
				poids += result.getWeight(edge);
				
			}
			
			poids += g.getWeight(e);
						
			result.insertEdge(
					source.getId(), target.getId(),
					poids, 
					timeAdd,
					Long.MAX_VALUE
							);
		}
		
		return result;
    }
    
    /**
     * Retourne le graphe des tokens ou chaque branche contient les tokens donnes a chaque idee
     * @return DynamicGraph
     */
    public DynamicGraph getTokenGraph()
    {
    	DynamicGraph g = new DynamicGraph();
    	Node player;
    	
    	/* on ajoute les joueurs */
    	for (Integer p : players)
    	{
    		g.addNode((String)getNode(p).getAttribute("ID"));
    	}
    	
    	/* on ajoute les idees */
    	for (Integer p : ideas)
    	{
    		g.addNode("i"+(String)getNode(p).getAttribute("ID"));
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
    		
    		/* pour chaque arc, on rajoute au graphe ceux ajoutant des tokens */
    		for (Edge e : sortedVoteList)
    		{
    			ArrayList<long[]> hist = e.getAttribute(VOTE_HIST);
    			//{timeAdd,vote,valence}
    			
    			if (hist == null)
    			{
    				continue;
    			}
    			
    			Node n = e.getOpposite(player);
    			
    			for (long[] h : hist)
    			{
    				if (h[1] != 0)
    				{
    					//System.out.println("insert " + (String)player.getAttribute("ID") + ",\t" + "i"+(String)n.getAttribute("ID") + ",\t" + (int)h[1] + ",\t" + h[0] + ",\t" + Long.MAX_VALUE);
    					g.insertEdge((String)player.getAttribute("ID"), "i"+(String)n.getAttribute("ID"), (int)h[1], h[0],Long.MAX_VALUE);
    				}
    			}
    		}
    	}
    	
    	return g;
    }
    
    /**
     * Retourne le graphe des commentaires ou chaque branche contient le nombre de commentaire sur une idee
     * @return DynamicGraph
     */
    public DynamicGraph getCommentGraph()
    {
    	DynamicGraph g = new DynamicGraph();
    	Node player;
    	
    	/* on ajoute les joueurs */
    	for (Integer p : players)
    	{
    		g.addNode((String)getNode(p).getAttribute("ID"));
    	}
    	
    	/* on ajoute les idees */
    	for (Integer p : ideas)
    	{
    		g.addNode("i"+(String)getNode(p).getAttribute("ID"));
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
    		    		
    		/* pour chaque arc, on rajoute au graphe ceux n'ajoutant pas de tokens */
    		for (Edge e : sortedVoteList)
    		{
    			ArrayList<long[]> hist = e.getAttribute(VOTE_HIST);
    			//{timeAdd,vote,valence}
    			
    			if (hist == null)
    			{
    				continue;
    			}
    			
    			Node n = e.getOpposite(player);
    			
    			for (long[] h : hist)
    			{
    				int nbComment = 1;
    				if (h[1] == 0)
    				{
    					g.insertEdge((String)player.getAttribute("ID"), "i"+(String)n.getAttribute("ID"), nbComment, h[0],Long.MAX_VALUE);
    				}
    				
    			}
    		}
    	}
    	
    	return g;
    }
    
    /**
     * Retourne un graphe d'idees
     * @return DynamicGraph
     */
    public DynamicGraph getIdeaGraph()
    {
    	DynamicGraph g = new DynamicGraph();
    	Node idea;
    	
    	/* on ajoute les idees */
    	for (Integer p : ideas)
    	{
    		g.addNode((String)getNode(p).getAttribute("ID"));
    	}
    	
    	for (Integer p : ideas)
    	{
    		idea = getNode(p);
    		
    		/* on recupere la liste des sources depuis les arcs sortant puis on cree les liens*/
    		for (Edge e : idea.getLeavingEdgeSet())
    		{
    			g.insertEdge((String)idea.getAttribute("ID"), (String)e.getOpposite(idea).getAttribute("ID"), 1, (Long)e.getAttribute(TIME_ADD), Long.MAX_VALUE);
    		}
    	}
    	
    	return g;
    }

    
    /* fonctions d'export / import en chaine */
    
	/**
	 * Transforme l'arc en String
	 * @param e
	 * @return
	 */
	public String edgeToString(Edge e)
	{
		String result = "" + LEFT_BRACE;
		
		if (ideas.contains(e.getNode0().getIndex()))
		{
			/* cas de liaison idee fille / mere */
			result += "" + TYPE_EDGE_IDEA_PARENT 
					+ SEPARATOR + (String)e.getNode0().getAttribute("ID") 
					+ SEPARATOR + (String)e.getNode1().getAttribute("ID")
					+ SEPARATOR + (Long)e.getAttribute(TIME_ADD);
		}
		else
		{
			if (votes.contains(e.getIndex()))
			{
				/* cas du vote */
				String cible = (String)e.getAttribute(IDEA_TARGET);
				String source = (String)e.getAttribute(PLAYER_SOURCE) ;
				result += "" + TYPE_EDGE_VOTE 									// 0
						+ SEPARATOR + (String)e.getNode0().getAttribute("ID") 	// 1
						+ SEPARATOR + (String)e.getNode1().getAttribute("ID")	// 2
					    + SEPARATOR + (Long)e.getAttribute(TIME_ADD) 			// 3
					    + SEPARATOR + (Integer)e.getAttribute(VOTE_NOTE) 		// 4
					    + SEPARATOR + (String)e.getAttribute("ID") 				// 5
					    + SEPARATOR + cible.substring(2,cible.length())			// 6
						+ SEPARATOR + source.substring(2,source.length())		// 7
						;

				ArrayList<long[]> hist = e.getAttribute(VOTE_HIST);
				String tmpHist = "" + LEFT_BRACE;
				for(long[] h : hist)
				{
					if (tmpHist.equals(""+LEFT_BRACE))
					{
						tmpHist += "" + LEFT_BRACE + h[0] + SEPARATOR + h[1] + SEPARATOR + h[2] + RIGHT_BRACE;
					}
					else
					{
						tmpHist += "" + SEPARATOR + LEFT_BRACE + h[0] + SEPARATOR + h[1] + SEPARATOR + h[2] + RIGHT_BRACE;
					}
				}
				result += "" + SEPARATOR + tmpHist + RIGHT_BRACE;				// 8
			}
			else
			{
				/* cas de source d'idee */
				result += "" + TYPE_EDGE_IDEA_SOURCE 
						+ SEPARATOR + (String)e.getNode0().getAttribute("ID") 
						+ SEPARATOR + (String)e.getNode1().getAttribute("ID")
						+ SEPARATOR + (Long)e.getAttribute(TIME_ADD);
			}
		}
		
		result += RIGHT_BRACE;
				
		return result;
	}
	
	/**
	 * Transforme la node en String
	 * @param n
	 * @return
	 */
	public String nodeToString(Node n)
	{
		if (players.contains(n.getIndex()))
		{
			return "" + LEFT_BRACE + TYPE_NODE_PLAYER + SEPARATOR + (String)n.getAttribute("ID") + SEPARATOR + (Long)n.getAttribute(TIME_ADD) + RIGHT_BRACE;
		}
		else if (ideas.contains(n.getIndex()))
		{
			String result = "" + LEFT_BRACE + TYPE_NODE_IDEA 
					+ SEPARATOR + (String)n.getAttribute("ID") 
					+ SEPARATOR + (Long)n.getAttribute(TIME_ADD);
			
			@SuppressWarnings("unchecked")
			ArrayList<String> idea_sources = (ArrayList<String>)n.getAttribute(IDEA_SOURCE);
			String sources = "" + LEFT_BRACE;
			for (String s : idea_sources)
			{
				if (sources.equals(""+LEFT_BRACE))
				{
					sources += s.substring(2,s.length());
				}
				else
				{
					sources += SEPARATOR + s.substring(2,s.length());
				}
			}
			sources += RIGHT_BRACE;
			
			String pSource = (String)n.getAttribute(PLAYER_SOURCE);
			
			result += SEPARATOR + sources
					+ SEPARATOR + pSource.substring(2,pSource.length())
					+ RIGHT_BRACE;
			
			return result;		
		}
		return "" + LEFT_BRACE + TYPE_NODE_ROOT + SEPARATOR + (String)n.getAttribute("ID") + SEPARATOR + (Long)n.getAttribute(TIME_ADD) +  RIGHT_BRACE;
	}
	
	/**
	 * Transforme le graphe en chaine en utilisant le separateur et les encadrant definit dans LEFT_BRACE, RIGHT_BRACE, SEPARATOR
	 * @return la chaine creee
	 */
	public String toString()
	{
		String result = "" + LEFT_BRACE;
		
		for (Node n : getEachNode())
		{
			if (result.length() != 1)
			{
				result += SEPARATOR;
			}
			result += nodeToString(n);
		}
		
		for (Edge e : getEachEdge())
		{
			if (result.length() != 1)
			{
				result += SEPARATOR;
			}
			
			result += edgeToString(e);
		}
		result += RIGHT_BRACE;
		
		return result;
	}

	/**
	 * Transforme le graphe en tableau de String, chaque String contenant la declaration d'une node ou d'un arc
	 * @return le tableau creee
	 */
	public Collection<String> toStringArray()
	{
		ArrayList<String> result = new ArrayList<String>();
		
		for (Node n : getEachNode())
		{
			result.add(nodeToString(n));
		}
		
		for (Edge e : getEachEdge())
		{
			result.add(edgeToString(e));
		}
		
		return result;
	}
	
	/**
	 * Charge les donnees graphes depuis la chaine donne
	 * @param source : chaine contenant les donnes graphes dans le format LEFT_BRACE attr1 SEPARATOR attr2 SEPARATOR ... RIGHT_BRACE
	 * @param weightDefault : poids par defaut si non specifie
	 * @param timeStartDefault : temps de creation par defaut si non specifie
	 * @param timeEndDefault : temps de suppression par defaut si non specifie (ou egal au temps de creation si la valeur lui est inf�rieure)
	 * @param insert : vrai si les donnes doivent etre inserees, faux si elles doivent etre ajoutees (voir fonction addEdge et insertEdge)
	 */
	public void loadFromString(String source)
	{
		if (source == null)
		{
			return;
		}
		ArrayList<String> chaines = stringToTab(source);
		
		for(String s : chaines)
		{
			if (s.length() == 0)
			{
				continue;
			}
			
			ArrayList<String> graphContent = stringToTab(s);
			Node n;
			Edge e;
			ArrayList<String> hist;
			String node0, node1;
			try
			{
				switch(Integer.valueOf(graphContent.get(0)))
				{
					case TYPE_NODE_IDEA :
						if (getNode(PREFIX_IDEA + graphContent.get(1)) != null)
						{
							break;
						}
						n = addNode(PREFIX_IDEA + graphContent.get(1));
						ideas.add(n.getIndex());
						n.addAttribute("ui.class","idea");
						n.addAttribute("ui.label",PREFIX_IDEA + graphContent.get(1));
						n.addAttribute("layout.force", 0);
						n.addAttribute("ID", graphContent.get(1));
						n.addAttribute(TIME_ADD, Long.valueOf(graphContent.get(2)));
						ArrayList<String> sources = stringToTab((String)graphContent.get(3));
						for (int i = 0 ; i < sources.size(); i++)
						{
							sources.set(i, PREFIX_IDEA + sources.get(i));
						}
						n.addAttribute(IDEA_SOURCE,sources);
						n.addAttribute(PLAYER_SOURCE, PREFIX_PLAYER + (String)graphContent.get(4));
						break;
					case TYPE_NODE_PLAYER :
						if (getNode(PREFIX_PLAYER + graphContent.get(1)) != null)
						{
							break;
						}
						n = addNode(PREFIX_PLAYER + graphContent.get(1));
						players.add(n.getIndex());
						n.addAttribute("ID", graphContent.get(1));
						n.addAttribute(TIME_ADD, Long.valueOf(graphContent.get(2)));
						n.addAttribute("ui.class","player");
						n.addAttribute("ui.label",PREFIX_PLAYER + graphContent.get(1));
						break;
					case TYPE_NODE_ROOT :
						if (getNode(PREFIX_IDEA + graphContent.get(1)) != null)
						{
							break;
						}
						n = addNode(PREFIX_IDEA + graphContent.get(1));
						root = n.getIndex();
						n.addAttribute("ID", graphContent.get(1));
						n.addAttribute("ui.class","root_idea");
						n.addAttribute("layout.force", 100);
						n.addAttribute("ui.label",PREFIX_IDEA + graphContent.get(1));
						n.addAttribute(TIME_ADD, Long.valueOf(graphContent.get(2)));
						//n.addAttribute(IDEA_SOURCE, graphContent.get(3));
						//n.addAttribute(PLAYER_SOURCE, graphContent.get(4));
						break;
					case TYPE_EDGE_IDEA_PARENT :
						node0 = PREFIX_IDEA + graphContent.get(1);
						node1 = PREFIX_IDEA + graphContent.get(2);
						if (getEdge(node0 + " from " + node1) != null)
						{
							break;
						}
						e = addEdge(node0 + " from " + node1, node0, node1);
						e.addAttribute("ui.class","fromidea");
						e.addAttribute("ui.label",IDEA_FROM_LABEL);
						e.addAttribute(TIME_ADD, Long.valueOf(graphContent.get(3)));
						break;
					case TYPE_EDGE_IDEA_SOURCE :
						node0 = PREFIX_PLAYER + graphContent.get(1);
						node1 = PREFIX_IDEA + graphContent.get(2);
						if (getEdge(node0 + " has idea " + node1) != null)
						{
							break;
						}
						e = addEdge(node0 + " has idea " + node1, node0, node1);
						e.addAttribute("ui.class","has_idea");
						e.addAttribute("ui.label",IDEA_HAS_LABEL);
						e.addAttribute(TIME_ADD, Long.valueOf(graphContent.get(3)));
						break;
					case TYPE_EDGE_VOTE :
						node0 = PREFIX_PLAYER + graphContent.get(1);
						node1 = PREFIX_IDEA + graphContent.get(2);
						ArrayList<long[]> attHist;
						if (getEdge(PREFIX_VOTE + graphContent.get(5)) != null)
						{
							break;
						}
						e = addEdge(PREFIX_VOTE + graphContent.get(5), node0, node1);
						votes.add(e.getIndex());
						e.addAttribute("ID", graphContent.get(5));
						e.addAttribute("ui.class","vote");
						e.addAttribute(TIME_ADD, Long.valueOf(graphContent.get(3)));
						e.addAttribute(VOTE_NOTE, Integer.valueOf(graphContent.get(4)));
						e.addAttribute(IDEA_TARGET, PREFIX_IDEA + graphContent.get(6));
						e.addAttribute(PLAYER_SOURCE, PREFIX_PLAYER + graphContent.get(7));
						e.addAttribute(VOTE_HIST, new ArrayList<long[]>());
						attHist = e.getAttribute(VOTE_HIST);
						hist = stringToTab(graphContent.get(8));
						for (String h : hist)
						{
							ArrayList<String> tabh = stringToTab(h);
							attHist.add(new long[]{Long.valueOf(tabh.get(0)),Long.valueOf(tabh.get(1)),Long.valueOf(tabh.get(2))});
						}						
						String label = "vote ";
						if (attHist.get(attHist.size()-1)[2] == -1)
						{
							label += "NEG ";
						}
						else if (attHist.get(attHist.size()-1)[2] == 1)
						{
							label += "POS ";
						}
						else if (attHist.get(attHist.size()-1)[2] == 0)
						{
							label += "NEU ";
						}
						
						long totalVote = 0;
						for (long[] h : attHist)
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
						break;
					default :
						break;
				}
			}
			catch (IdAlreadyInUseException ex)
			{
				System.err.println("Warning : ID already in use");
				ex.printStackTrace();
				System.exit(-1);
			}
		}
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