package client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import data.CommentValence;
import data.IComment;
import data.IIdea;

import errors.AlreadyExistsException;
import errors.TooLateException;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;

public class DelegatingBotCore extends ClientCore {

	public final static int BOT_BASE_SPEED = 5000;
	public final static String BOT_AVATAR = "usertile11.jpg";
	public final static String BOT_NAME = "Wall-e ";
	
	private static Integer botCount = 1;
	private static int ideaCount = 1;

	private String name;
	private String avatar;
	
	private int nbIdeas;
	private int nbComments;
	
	private long timeCreation;
	private long nextAction;
	
	private IEventListener listener;
	
	private HashMap<Integer, Long> lastHeuristics;
	
	/* liste des parametres */
	private int reactivity;
	private int creativity; /* capacite a avoir de bonne idees */
	private int relevance;  /* capacite a suivre les bonnes idees */
	private int adaptation; /* capacite a suivre les autres commentaires */
	private int persuation; /* capacite a convaincre les autres de suivre ses commentaires */
	
	/**
	 * Cree un bot avec le listener fourni
	 * @param ui
	 */
	public DelegatingBotCore(IEventListener ui) {
		super();
		if(ui==null) throw new NullPointerException();
		listener = ui;
		lastHeuristics = new HashMap<Integer, Long>();
		
		reactivity = 5;
		creativity = 5;
		relevance = 5;
		adaptation = 5;
		persuation = 5;
		
		timeCreation = System.currentTimeMillis();
		nextAction = getNextAction(timeCreation) ;
		
		name = BOT_NAME + " " + botCount;
		avatar = BOT_AVATAR;
		
		botCount++;
		
		//TypeScore.adaptation.calculer(g, playerId)
	}

	/**
	 * Raffraichit le bot, et effectue une action si celui-ci a prevu d'en faire une
	 * @throws RemoteException 
	 * @throws AlreadyExistsException
	 * @throws TooLateException
	 */
	public void refresh() throws AlreadyExistsException, TooLateException, RemoteException
	{
		updateHeuristics();
		long time = System.currentTimeMillis();
		if (time > nextAction)
		{
			/* on calcule les chances d'obtenir une idee (entre 5 et 20) */
			double nbIdeas = getGame().getAllIdeas().size()-1;
			double nbPlayerIdeas = getNbIdeas();
			double nbPlayers = getGame().getAllPlayers().size();
			double ideaCreationChance = 4 + (46 * (1-Math.sqrt((nbIdeas+nbPlayerIdeas) / ((nbPlayers) + nbIdeas+nbPlayerIdeas))));
							
			if (Math.random()*100 <= ideaCreationChance)
			{ /* on ajoute une idee */
				
				/* on calcule le nombre d'idees source */
				Integer nbSources;
				if (Math.random()*100 <= 95)
				{
					nbSources = 1;
				}
				else
				{
					nbSources = 2;
				}
				
				/* on recupere les idees sources */
				Collection<Integer> sources = new ArrayList<Integer>();
				for (int i = 0 ; i < nbSources ; i++)
				{
					int id = getBestIdea();
					if (!sources.contains(id))
					{
						sources.add(id);
					}
				}
				
				/* on cree l'idee */
				createIdea(sources);
				
			}
			else
			{ /* on ajoute un commentaire */
				Integer id = getBestIdea();
				Integer tokensGiven = getCurrentIdeasTokens().get(id);
				
				/* on cree un commentaire */
				if (getRemainingTokens() == 0 && tokensGiven == 0)
				{
					createComment(id,0,CommentValence.NEUTRAL);
				}
				else
				{
					Integer tokensToGive = 0;

					if ((time/10)%2 == 0)
					{
						tokensToGive = (int) ( 0 - tokensGiven * Math.random());
					}
					else
					{
						tokensToGive = (int) ( getRemainingTokens() * Math.random());
					}
					
					if (tokensToGive == 0)
					{
						createComment(id,tokensToGive,CommentValence.NEUTRAL);
					}
					else if(tokensToGive > 0)
					{
						createComment(id,tokensToGive,CommentValence.POSITIVE);
					}
					else if(tokensToGive < 0)
					{
						createComment(id,tokensToGive,CommentValence.NEGATIVE);
					}
				}
			}
			nextAction = getNextAction(time);
		}
	}
	
	/**
	 * Donne l'id de la meilleur idee possible, chaque idee ayant plus de chance d'etre obtenue selon ses caracteristiques et celles du bot
	 * @return int : l'id de l'idee obtenue
	 * @throws RemoteException 
	 */
	public int getBestIdea() throws RemoteException
	{
		int id = 0;
		long totalHeuristic = 0;
		
		for (Entry<Integer, Long> h : lastHeuristics.entrySet())
		{
			totalHeuristic += h.getValue();
		}
		
		/* on recupere une idee au hazard, les chances d'obtenir une idee sont augmentee si son heuristique est grande */
		long rand = (int)(Math.random()*totalHeuristic);
		for (IIdea i : getGame().getAllIdeas())
		{
			if (rand < lastHeuristics.get(i.getUniqueId()))
			{
				id = i.getUniqueId();
				break;
			}
			rand -= lastHeuristics.get(i.getUniqueId());
		}
		
		return id;
	}
	
	/**
	 * Calcule l'heuristique d'une idee
	 * @param id : id de l'idee
	 * @param heuristics : heuristique des autres idees
	 * @return int : l'heuristique calculee
	 * @throws RemoteException 
	 */
	public long heuristicIdea(int id) throws RemoteException
	{
		IIdea idea;
		try {
			idea = getGame().getIdea(id);
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
		
		long time = System.currentTimeMillis();
		
		/* on recupere la valeur d'une idee, multipliee par la pertinence du bot (pour qu'il la repere plus facilement) */
		long ideaValue = idea.getIdeaValue() * relevance;
		
		/* on recupere les commentaires de l'idee, chaque commentaire etant valué par la persuasion de la source et l'adaptation du bot */
		long commentValue = 0;
		for (IComment c : getGame().getAllComments())
		{
			if (c.getIdea().getUniqueId() == id)
			{
				int actualCommentValue = (c.getCommentValue() + c.getTokensCount() * 5)* adaptation;
				if (c.getValence() == CommentValence.POSITIVE)
				{
					actualCommentValue *= 2;
				}
				else if (c.getValence() == CommentValence.NEGATIVE)
				{
					actualCommentValue /= 2;
				}
				commentValue += actualCommentValue;

				//long timeElapsed = time - c.getCreationDate();
				//commentValue = commentValue * (1-(timeElapsed / (BOT_BASE_SPEED + timeElapsed))) ;
			}
		}
		
		/* on recupere l'heuristique des parents pour en heriter d'une partie */
		long parentsValue = 0;
		if (idea.getParents().size() != 0)
		{
			for (IIdea i : idea.getParents())
			{
				parentsValue += lastHeuristics.get(i.getUniqueId());
			}
			parentsValue /= 3*idea.getParents().size();
		}
		
		/* on calcule l'heuristique totale */
		long total = ideaValue + commentValue;
		
		/* on reduit l'heuristique en fonction du temps ecoule (1 min = /2)*/
		//double timeElapsed = time - idea.getCreationDate();
		//total = (long) (total * (1-timeElapsed / (BOT_BASE_SPEED*6 + timeElapsed)));
		//System.out.println("Heuristique reduite de : " + timeElapsed / (BOT_BASE_SPEED*6 + timeElapsed));
		
		return 1 + (total + parentsValue) / 100;
	}
	
	/**
	 * Recupere le temps auquel le bot fera sa prochaine action (donne par System.currentTimeMillis())
	 * @param time
	 * @return
	 */
 	private long getNextAction(long time)
	{
		return (long) (time + ((BOT_BASE_SPEED + (Math.random() * BOT_BASE_SPEED*49)) / reactivity));
	}
	
	/**
	 * Reset le temps auquel le bot fera sa prochaine action
	 */
	public void resetNextAction()
	{
		nextAction = getNextAction(System.currentTimeMillis());
	}
	
	/**
	 * Met a jour les heuristiques
	 * @throws RemoteException
	 */
	private void updateHeuristics() throws RemoteException
	{
		lastHeuristics.clear();
		ArrayList<IIdea> ideasTodo = new ArrayList<IIdea>();
		
		ideasTodo.add(getGame().getRootIdea());
		
		/* on parcourt les idees en commencant par la racine puis en remontant dans les branches */
		boolean newChilds = true;
		while(newChilds)
		{
			while (!ideasTodo.isEmpty())
			{
				IIdea i = ideasTodo.get(0);
				ideasTodo.remove(0);
				
				/* on calcule l'heuristique de l'idee puis on l'ajoute a la map */
				long h = heuristicIdea(i.getUniqueId());
				lastHeuristics.put(i.getUniqueId(),h);
			}
			/* on ajoute de nouveaux noeuds si on peut */
			newChilds = false;
			boolean addChild;
			for (IIdea c : getGame().getAllIdeas())
			{
				/* on ignore les idees qu'on a deja calcule */
				if (lastHeuristics.containsKey(c.getUniqueId()))
				{
					continue;
				}
				
				addChild = true;
				for (IIdea p : c.getParents())
				{
					if (!lastHeuristics.containsKey(p.getUniqueId()))
					{
						addChild = false;
						break;
					}
				}
				if (addChild)
				{
					ideasTodo.add(c);
					newChilds = true;
				}
			}
		}
	}
	
	/**
	 * Retourne l'heuristique du bot
	 * @return HashMap<Integer,Long>
	 */
	public HashMap<Integer,Long> getHeuristics()
	{
		return lastHeuristics;
	}
	
	/**
	 * Setter pour reactivity
	 * @param _reactivity
	 */
	public void setReactivity(int _reactivity)
	{
		reactivity = _reactivity;
	}
	
	/**
	 * Getter pour reactivity
	 * @return int
	 */
	public int getReactivity()
	{
		return reactivity;
	}
	
	/**
	 * Setter pour creativity
	 * @param _creativity
	 */
	public void setCreativity(int _creativity)
	{
		creativity = _creativity;
	}
	
	/**
	 * Getter pour creativity
	 * @return int
	 */
	public int getCreativity()
	{
		return creativity;
	}
	
	/**
	 * Setter pour relevance
	 * @param _relevance
	 */
	public void setRelevance(int _relevance)
	{
		relevance = _relevance;
	}
	
	/**
	 * Getter pour relevance
	 * @return int
	 */
	public int getRelevance()
	{
		return relevance;
	}
	
	/**
	 * Setter pour adaptation
	 * @param _adaptation
	 */
	public void setAdaptation(int _adaptation)
	{
		adaptation = _adaptation;
	}
	
	/**
	 * Getter pour adaptation
	 * @return int
	 */
	public int getAdaptation()
	{
		return adaptation;
	}
	
	/**
	 * Setter pour persuation
	 * @param _persuation
	 */
	public void setPersuation(int _persuation)
	{
		persuation = _persuation;
	}
	
	/**
	 * Getter pour persuation
	 * @return int
	 */
	public int getPersuation()
	{
		return persuation;
	}
	
	/**
	 * Retourne l'uptime du bot
	 * @return long : le temps de vie en ms
	 */
	public long getUpTime()
	{
		return System.currentTimeMillis() - timeCreation;
	}
	
	/**
	 * Retourne le nom du bot
	 * @return String
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Retourne l'avatar du bot
	 * @return String
	 */
	public String getAvatar()
	{
		return avatar;
	}
	
	/**
	 * Retourne le nombre d'idees du bot
	 * @return int
	 */
	public int getNbIdeas()
	{
		return nbIdeas;
	}
	
	/**
	 * Retourne le nombre de commentaires du bot
	 * @return int
	 */
	public int getNbComments()
	{
		return nbComments;
	}
	
	/**
	 * Cree une idee
	 * @param name : nom de l'idee
	 * @param description : description de l'idee
	 * @param sources : id des idees source
	 * @throws AlreadyExistsException
	 * @throws TooLateException
	 * @throws RemoteException
	 */
	public void createIdea(Collection<Integer> sources) throws AlreadyExistsException, TooLateException, RemoteException
	{
		Integer preValue = IIdea.IDEA_MAX_VALUE * creativity / 20 ;
		Integer value = preValue + (int)(Math.random() * (IIdea.IDEA_MAX_VALUE-preValue) / (11-creativity));
		getGame().addIdea(getPlayerId(),"Idea " + ideaCount, "this idea has a value of " + value, new ArrayList<Integer>(), sources);
		ideaCount++;
		nbIdeas++;

	}
	
	/**
	 * Cree un commentaire
	 * @param idea : idee cible du commentaire
	 * @param description : description du commentaire
	 * @param tokens : nombre de tokens ajoute
	 * @param valence : valence du commentaire
	 * @throws AlreadyExistsException
	 * @throws TooLateException
	 * @throws RemoteException
	 */
	public void createComment(Integer idea, int tokens, CommentValence valence) throws RemoteException
	{
		if (getRemainingTokens() < tokens)
		{
			System.err.println("Bot error : trying to use more than 10 tokens");
		}
		else
		{
			Integer preValue = IComment.COMMENT_MAX_VALUE * creativity / 20 ;
			Integer value = preValue + (int)(Math.random() * (IComment.COMMENT_MAX_VALUE-preValue) / (11-creativity));
			getGame().commentIdea(getPlayerId(), idea, "this comment has a value of " + value, tokens, valence);
			spendTokens(tokens);
			nbComments++;
		}

	}	
		
	/**
	 * Listener pour l'evenement player left
	 * @param e
	 */
	public void playerLeft(PlayerEvent e) throws RemoteException {
		listener.playerLeft(e);
	}

	/**
	 * Listener pour l'evenement player join
	 * @param e
	 */
	public void playerJoined(PlayerEvent e) throws RemoteException {
		listener.playerJoined(e);
	}

	/**
	 * Listener pour l'evenement item cree
	 * @param e
	 */
	public void ItemCreated(GameObjectEvent e) throws RemoteException {
		listener.ItemCreated(e);
	}

	/**
	 * Listener pour l'evenement idee creee
	 * @param e
	 */
	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
		listener.IdeaCreated(e);
	}

	/**
	 * Listener pour l'evenement lien d'idee cree
	 * @param e
	 */
	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
		listener.IdeaLinkCreated(e);
	}

	/**
	 * Listener pour l'evenement commentaire cree
	 * @param e
	 */
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {
		listener.ideaCommentCreated(e);
	}

	/**
	 * Listener pour l'evenement fin de partie
	 * @param e
	 */
	public void endOfGame() throws RemoteException {
		listener.endOfGame();
	}

}
