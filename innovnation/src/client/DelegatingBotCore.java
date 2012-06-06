package client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.Set;

import client.gui.GuiBotManager;
import client.gui.PlayersScores;

import java.util.concurrent.Semaphore;

import data.Avatars;
import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IPlayer;

import errors.AlreadyExistsException;
import errors.TooLateException;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;
import functions.TypeScore;

public class DelegatingBotCore extends ClientCore
{

	public final static int ACTION_VOTE = 0;
	public final static int ACTION_IDEA = 1;
	public final static int ACTION_NOTHING = 2;
	
	public static final String[] roles = {"Custom","Random","Persuasif","Creatif","Adaptatif","Pertinent"};
	public static final int[][] rolesParams = 
		{{-1,-1,-1,-1},{-1,-1,-1,-1},{3,3,3,10},{10,3,3,3},{3,3,10,3},{3,10,3,3}};
	public final static int BOT_BASE_SPEED = 2500;
	public final static String BOT_NAME = "Wall-e ";
	
	public final static int PARAM_MAX_VALUE = 10;
	
	public static final double CHANCE_IDEA_CREATION_RATIO = 0.1;
	
	private static Integer botCount = 1;
	public static int ideaCount = 0;
	public static int commentCount = 0;
	
	private static Semaphore semaphore = new Semaphore(1, true);

	private String name;
	private String avatar;
	
	private int nbIdeas;
	private int nbComments;
	
	private boolean paused;
	
	private long timeCreation;
	private long nextAction;
	
	private boolean usingSemaphore;
	
	private IEventListener listener;
	
	private HashMap<Integer, Long> lastHeuristics;
	
	/* liste des parametres */
	private int role;		/* role choisis dans la liste des roles */
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
		
		timeCreation = System.currentTimeMillis();
		nextAction = getNextAction(timeCreation) ;
		
		usingSemaphore = false;
		
		name = BOT_NAME + " " + botCount;
		avatar = Avatars.getOneAvatarRandomly();
		paused = true;
		
		botCount++;
		
		/* on choisis un role au hazard */
		role = (int) (2 + Math.random() * (roles.length-2));
		
		creativity = rolesParams[role][0];
		relevance = rolesParams[role][1];
		adaptation = rolesParams[role][2];
		persuation = rolesParams[role][3];
		
		//TypeScore.adaptation.calculer(g, playerId)
	}

	/**
	 * Retourne le nombre total d'idees creees par les bots
	 * @return int
	 */
	public static int getIdeaCount()
	{
		return ideaCount;
	}

	/**
	 * Retourne le nombre total de commentaires creees par les bots
	 * @return int
	 */
	public static int getCommentCount()
	{
		return commentCount;
	}
	
	/**
	 * Raffraichit le bot, et effectue une action si celui-ci a prevu d'en faire une
	 * @throws RemoteException 
	 * @throws AlreadyExistsException
	 * @throws TooLateException
	 * @throws InterruptedException 
	 */
	public void refresh() throws AlreadyExistsException, TooLateException, RemoteException, InterruptedException
	{
		updateHeuristics();

		if (paused)
		{
			return;
		}
		
		long time = System.currentTimeMillis();
		
		if (time > nextAction)
		{
			switch(choseAction())
			{
				case ACTION_IDEA :
					actionIdea();
					break;
				case ACTION_VOTE :
					actionVote();
					break;
				case ACTION_NOTHING :
					break;
			}

			nextAction = getNextAction(time);
		}
	}
	
	/**
	 * Choisis une action a faire (vote, rajouter une idee, ne rien faire ..)
	 * @return int
	 * @throws InterruptedException 
	 * @throws RemoteException 
	 */
	public int choseAction() throws RemoteException, InterruptedException
	{
		if (!GuiBotManager.ALLOW_IDEA_CREATION)
		{
			return ACTION_VOTE;
		}
		
		double discount = CHANCE_IDEA_CREATION_RATIO * Math.sqrt(getGame().getAllPlayers().size());
		double strength = 0.5;
		double time = ideaCount + commentCount;
		
		double newIdeaChance = (strength + getAllIdeas().size() * discount) / (time + strength);
				
		if (Math.random() <= newIdeaChance)
		{ 
			return ACTION_IDEA;
		}
		else
		{
			return ACTION_VOTE;
		}
	}
	
	/**
	 * Choisi une idee et y fait heriter une autre
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	public void actionIdea() throws RemoteException, InterruptedException
	{
		/* on ajoute une idee */
		
		/* on calcule le nombre d'idees source */
		Integer nbSources = 1;
		/*if (Math.random()*100 <= 95)
		{
			nbSources = 1;
		}
		else
		{
			nbSources = 2;
		}*/
		
		/* on recupere les idees sources */
		Collection<Integer> sources = new ArrayList<Integer>();
		for (int i = 0 ; i < nbSources ; i++)
		{
			int id = getBestIdeaForIdea();
			if (!sources.contains(id))
			{
				sources.add(id);
			}
		}
		
		/* on cree l'idee */
		createIdea(sources);
	}
	
	/**
	 * Choisi une idee et vote dessus
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	public void actionVote() throws RemoteException, InterruptedException
	{
		/* on ajoute un commentaire */
		int id = getBestIdeaForVote();
		int tokensGiven = getCurrentIdeasTokensL().get(id);
		int tokensToGive = 0;
		double tokensIdea = getIdea(id).getTotalBids();
		
		/* on choisit si le commentaire doit etre positif ou negatif */
		int nbBest = 3;
		ArrayList<Integer> bests = new ArrayList<Integer>();
		HashMap<Integer,Integer> tokensMap = new HashMap<Integer, Integer>();
		long heuristicSum = 0;
		
		/* on recupere les nbBest meilleures idees */
		for (Entry<Integer, Long> s : lastHeuristics.entrySet())
		{
			if (bests.size() < nbBest)
			{
				bests.add(s.getKey());
			}
			else
			{
				int worst = 0;
				for (int i = 0 ; i < bests.size() ; i++)
				{
					if (lastHeuristics.get(bests.get(worst)) > lastHeuristics.get(bests.get(i)))
					{
						worst = i;
					}
				}
				if (lastHeuristics.get(bests.get(worst)) < s.getValue())
				{
					bests.remove(worst);
					bests.add(s.getKey());
				}
			}
		}
		
		for (Integer i : bests)
		{
			heuristicSum += lastHeuristics.get(i);
		}
		
		/* on calcune le nombre de tokens que doit avoir une idee */
		for (Integer i : bests)
		{
			tokensMap.put(i, 
					(int) Math.min(((10 * relevance * lastHeuristics.get(i) / heuristicSum) + (10 * adaptation * tokensIdea / getGame().getAllPlayers().size())) / (relevance + adaptation),10));
		}
		
		/* on calcule le nombre de tokens necessaire a l'idee */
		int tokensForIdea = 0;
		if (tokensMap.containsKey(id))
		{
			tokensForIdea = tokensMap.get(id);
		}
		tokensToGive = tokensForIdea - tokensGiven;
		if (id == getRootIdea().getUniqueId())
		{
			tokensToGive = 0;
		}
		
		/* plus le nombre de tokens est grand, plus les chances de voter sont grande */
		if ( Math.random() * (Math.abs(tokensToGive)+2) < 1)
		{
			tokensToGive = 0;
		}
		
		/* on effectue le vote */
		if (tokensToGive > 0)
		{
			/* si on n'a pas assez de tokens, on en retire aux autres idees */			
			if (tokensToGive > getRemainingTokensL())
			{
				removeTokens(tokensToGive + getRemainingTokensL());
			}			
			createComment(id,tokensToGive,CommentValence.POSITIVE);
		}
		else if (tokensToGive == 0)
		{
			/* on fait un commentaire neutre */
			createComment(id, 0, CommentValence.NEUTRAL);
		}
		else
		{
			/* on fait un commentaire negatif */
			
			/* on retire une partie des tokens donnes */
			createComment(id,tokensToGive,CommentValence.NEGATIVE);
			
		}
	}
	
	/**
	 * Recupere le nombre de tokens indiques sur les idees votees (si cela est possible)
	 * @param tokensCount
	 * @throws InterruptedException 
	 * @throws RemoteException 
	 */
	public void removeTokens(int tokensCount) throws RemoteException, InterruptedException
	{
		if (getRemainingTokens() + tokensCount > getMaxTokensByPlayer())
		{
			tokensCount = getMaxTokensByPlayer() - getRemainingTokens();
		}
		
		int tokensToGive = getRemainingTokensL() + tokensCount;
		Set<Entry<Integer, Integer>> tokens = getCurrentIdeasTokensL().entrySet();
		ArrayList<Integer> alreadySeenIdeas = new ArrayList<Integer>();

		while (getRemainingTokensL() < tokensToGive)
		{
			/* on recherche la plus mauvaise idee */
			int worstIdea = -1;
			for (Entry<Integer, Integer> e : tokens)
			{
				if (alreadySeenIdeas.contains(e.getKey()) || e.getValue() <= 0)
				{
					continue;
				}
				
				if (worstIdea == -1 || heuristicIdea(worstIdea) > heuristicIdea(e.getKey()))
				{
					worstIdea = e.getKey();
				}
			}
			
			/* on retire les tokens */
			if (worstIdea == -1)
			{
				System.err.println("Error : des tokens ont ete utilises mais aucune idee n'en contient pour ce bot");
				return;
			}
			else
			{
				int removed = Math.min(getCurrentIdeasTokensL().get(worstIdea),tokensCount);
				alreadySeenIdeas.add(worstIdea);
				tokensCount -= removed;
				createComment(worstIdea, -removed, CommentValence.NEUTRAL);
			}
		}
	}
	
	/**
	 * Donne l'id de la meilleur idee possible pour vote, chaque idee ayant beaucoup plus de chance d'apparaitre selon son heuristique
	 * @return int : l'id de l'idee obtenue
	 * @throws RemoteException 
	 * @throws InterruptedException 
	 */
	public int getBestIdeaForVote() throws RemoteException, InterruptedException
	{
		int id = 0;
		long totalHeuristic = 0;
		double pow = 2;
		
		for (Entry<Integer, Long> h : lastHeuristics.entrySet())
		{
			totalHeuristic += Math.pow(h.getValue(),pow);
		}
		
		/* on recupere une idee au hazard, les chances d'obtenir une idee sont augmentee si son heuristique est grande */
		long rand = (int)(Math.random()*totalHeuristic);
		for (IIdea i : getAllIdeas())
		{
			long h = (long) Math.pow(lastHeuristics.get(i.getUniqueId()),pow);
			if (rand < h)
			{
				id = i.getUniqueId();
				break;
			}
			rand -= h;
		}
		
		return id;
	}
	
	/**
	 * Donne l'id de la meilleur idee possible pour une idee, chaque idee ayant plus de chance d'apparaitre selon son heuristique
	 * @return int : l'id de l'idee obtenue
	 * @throws RemoteException 
	 * @throws InterruptedException 
	 */
	public int getBestIdeaForIdea() throws RemoteException, InterruptedException
	{
		int id = 0;
		long totalHeuristic = 0;
		
		/* vu qu'il n'y a aucun moyen de connaitre la distance avec la racine, on la calcule nous meme */
		HashMap<Integer, Integer> rootLevel = new HashMap<Integer, Integer>();
		ArrayList<Integer> ideas = new ArrayList<Integer>(),
				       nextIdeas = new ArrayList<Integer>();
		ideas.add(getRootIdea().getUniqueId());
		
		rootLevel.put(getRootIdea().getUniqueId(), 0);
		
		int level = 0;
		while (ideas.size() > 0)
		{
			for (Integer i : ideas)
			{
				rootLevel.put(i, level);
				for (Integer c : getIdea(i).getChildrenIds())
				{
					if (!rootLevel.containsKey(c))
					{
						nextIdeas.add(c);
					}
				}
			}
			
			level++;
			ideas = nextIdeas;
			nextIdeas = new ArrayList<Integer>();

		}
		
		
		for (Entry<Integer, Long> h : lastHeuristics.entrySet())
		{
			totalHeuristic += (long) (h.getValue() / ( 1 + rootLevel.get(h.getKey())));
		}
		
		/* on recupere une idee au hazard, les chances d'obtenir une idee sont augmentee si son heuristique est grande */
		long rand = (int)(Math.random()*totalHeuristic);
		long ideaHeuristic = 0;
		for (IIdea i : getAllIdeas())
		{
			ideaHeuristic = (long) (lastHeuristics.get(i.getUniqueId()) / ( 1 + rootLevel.get(i.getUniqueId())));
			if (rand < ideaHeuristic)
			{
				id = i.getUniqueId();
				break;
			}
			rand -= ideaHeuristic;
		}
		
		return id;
	}
	
	/**
	 * Calcule l'heuristique d'une idee
	 * @param id : id de l'idee
	 * @param heuristics : heuristique des autres idees
	 * @return int : l'heuristique calculee
	 * @throws RemoteException 
	 * @throws InterruptedException 
	 */
	public long heuristicIdea(int id) throws RemoteException, InterruptedException
	{
		IIdea idea;
		try {
			idea = getIdea(id);
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
		
		long time = System.currentTimeMillis();
		double timeElapsed = time - idea.getCreationDate();
		
		/* on recupere la valeur d'une idee, multipliee par la pertinence du bot (pour qu'il la repere plus facilement) */
		long ideaValue = (long) (idea.getIdeaValue() * relevance * (1-(timeElapsed / (BOT_BASE_SPEED*6 + timeElapsed))));
		
		/* on recupere les commentaires de l'idee, chaque commentaire etant valué par la persuasion de la source et l'adaptation du bot */
		long commentValue = 0;
		for (IComment c : getAllComments())
		{
			if (c.getIdea().getUniqueId() == id)
			{
				int actualCommentValue = (c.getCommentValue() + c.getTokensCount() * 5) * adaptation * adaptation;
				/* on augmente la valeur du commentaire ou le reduit s'il est positif ou negatif */
				if (c.getValence() == CommentValence.POSITIVE)
				{
					actualCommentValue *= 2;
				}
				else if (c.getValence() == CommentValence.NEGATIVE)
				{
					actualCommentValue *= -1;
				}
				commentValue += actualCommentValue;

				/* on modifie la valeur des commentaires en fonction de la valeur de l'idee et du temps */
				timeElapsed = time - c.getCreationDate();
				commentValue = (long) (commentValue * idea.getIdeaValue() / IIdea.IDEA_MAX_VALUE * (1-(timeElapsed / (BOT_BASE_SPEED*6 + timeElapsed))));
			}
		}
		
		/* on recupere l'heuristique des parents pour en heriter d'une partie */
		long parentsValue = 0;
		/*if (idea.getParents().size() != 0)
		{
			for (IIdea i : idea.getParents())
			{
				if (lastHeuristics.containsKey(i.getUniqueId()))
				{
					parentsValue += lastHeuristics.get(i.getUniqueId());
				}
			}
			parentsValue /= 3*idea.getParents().size();
		}*/
		
		/* on calcule l'heuristique totale */
		long total = ideaValue + commentValue;
		
		/* on reduit l'heuristique en fonction du temps ecoule (1 min = /2)*/
		//double timeElapsed = time - idea.getCreationDate();
		//total = (long) (total * (1-timeElapsed / (BOT_BASE_SPEED*6 + timeElapsed)));
		//System.out.println("Heuristique reduite de : " + timeElapsed / (BOT_BASE_SPEED*6 + timeElapsed));
		
		return (long) (1+ total + parentsValue);
	}
	
	/**
	 * Recupere le temps auquel le bot fera sa prochaine action (donne par System.currentTimeMillis())
	 * @param time
	 * @return
	 */
 	private long getNextAction(long time)
	{
 		/* plus le temps passe, moins le bot a d'idees */
		return (long) (time + (BOT_BASE_SPEED + (Math.random() * (BOT_BASE_SPEED*9 + BOT_BASE_SPEED*nbIdeas + BOT_BASE_SPEED*nbComments / 5))));
	}
	
 	/**
	 * Reset le temps auquel le bot fera sa prochaine action
	 */
	private void resetNextAction()
	{
		nextAction = getNextAction(System.currentTimeMillis());
	}
	
	/**
	 * Met a jour les heuristiques
	 * @throws RemoteException
	 * @throws InterruptedException 
	 */
	private void updateHeuristics() throws RemoteException, InterruptedException
	{
		lastHeuristics.clear();
		ArrayList<IIdea> ideasTodo = new ArrayList<IIdea>();
		LinkedList<IIdea> allIdeas = getAllIdeas();
		
		ideasTodo.add(getRootIdea());
		
		/* on parcourt les idees en commencant par la racine puis en remontant dans les branches */
		boolean newChilds = true;
		while(newChilds)
		{
			while (!ideasTodo.isEmpty())
			{
				IIdea i = ideasTodo.get(0);
				ideasTodo.remove(0);
				
				/* on calcule l'heuristique de l'idee puis on l'ajoute a la map */
				lastHeuristics.put(i.getUniqueId(),heuristicIdea(i.getUniqueId()));
			}
			/* on ajoute de nouveaux noeuds si on peut */
			newChilds = false;
			boolean addChild;
			for (IIdea c : allIdeas)
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
	public int getPersuasion()
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
	 * Retourne l'index du role dans la liste des roles
	 * @return int
	 */
	public int getRole()
	{
		return role;
	}
	
	/**
	 * Met a jour le role choisis
	 * @param _role
	 */
	public void setRole(int _role)
	{
		role = _role;
		// mise a jour des infos du role
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
	 * Indique si le bot est actuellement en train d'utiliser un semaphore
	 * @return
	 */
	public boolean isUsingSemaphore()
	{
		return usingSemaphore;
	}
	
	/**
	 * Indique si le bot est en pause ou non
	 * @return
	 */
	public boolean isPaused()
	{
		return paused;
	}
	
	/**
	 * Met le bot en pause, ou le relance
	 * @param _paused
	 */
	public void setPaused(boolean _paused)
	{
		paused = _paused;
		if (!paused)
		{
			resetNextAction();
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

	
	/* liste des fonctions bloquante */
	
	/**
	 * Le bot informe les autres qu'il rentre dans une partie "sensible", les autres bots attendront que celui ci unlock pour pouvoir lock à leurs tour
	 * @throws InterruptedException 
	 */
	public void lock() throws InterruptedException
	{
		usingSemaphore = true;
		semaphore.acquire();
	}
	
	/**
	 * Le bot informe les autres qu'il quitte une partie "sensible", les autres pourront maintenant lock à leurs tour
	 */
	public void unlock()
	{
		semaphore.release();
		usingSemaphore = false;
	}
	
	/**
	 * Cree une idee
	 * @param name : nom de l'idee
	 * @param description : description de l'idee
	 * @param sources : id des idees source
	 * @throws AlreadyExistsException
	 * @throws TooLateException
	 * @throws RemoteException
	 * @throws InterruptedException 
	 */
	public void createIdea(Collection<Integer> sources) throws AlreadyExistsException, TooLateException, RemoteException, InterruptedException
	{
		lock();		
		ideaCount++;
		Integer value = (int) ((creativity-1) * IIdea.IDEA_MAX_VALUE / 10 + (Math.random() * (IIdea.IDEA_MAX_VALUE/10)));
		getGame().addIdea(getPlayerId(),"Idea " + ideaCount, "this idea has a value of " + value, new ArrayList<Integer>(), sources);
		nbIdeas++;
		unlock();

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
	public void createComment(Integer idea, int tokens, CommentValence valence) throws RemoteException, InterruptedException
	{
		if (getRemainingTokensL() < tokens)
		{
			System.err.println("Bot error : trying to use more than 10 tokens");
		}
		else
		{
			lock();
			commentCount++;
			Integer value = (int)(Math.random() * (IComment.COMMENT_MAX_VALUE * (creativity*creativity) / 10 ));
			getGame().commentIdea(getPlayerId(), idea, "value of " + value, tokens, valence);
			spendTokens(tokens);
			nbComments++;
			unlock();
		}
	}	
	
	public double computePersuasion() throws RemoteException, InterruptedException
	{
		lock();
		double ret = TypeScore.persuasion.calculer(getGame(), getPlayerId());
		unlock();
		return ret;
	}
	
	public double computeRelevance() throws RemoteException, InterruptedException
	{
		lock();
		double ret = TypeScore.pertinence.calculer(getGame(), getPlayerId());
		unlock();
		return ret;
	}
	
	public double computeCreativity() throws RemoteException, InterruptedException
	{
		lock();
		double ret = TypeScore.creativite.calculer(getGame(), getPlayerId());
		unlock();
		return ret;
	}
	
	public double computeAdaptation() throws RemoteException, InterruptedException
	{
		lock();
		double ret = TypeScore.adaptation.calculer(getGame(), getPlayerId());
		unlock();
		return ret;
	}
	
	public int computePersuasionRank() throws RemoteException, InterruptedException
	{
		lock();
		Map<Integer, Double> sctab=TypeScore.persuasion.calculer(getGame());
		SortedSet<PlayersScores> sortedScores = PlayersScores.calculer(sctab);
		int ret = PlayersScores.computeRank(sortedScores, getPlayerId());
		unlock();
		return ret;
	}
	
	public int computeRelevanceRank() throws RemoteException, InterruptedException
	{
		lock();
		Map<Integer, Double> sctab=TypeScore.pertinence.calculer(getGame());
		SortedSet<PlayersScores> sortedScores = PlayersScores.calculer(sctab);
		int ret = PlayersScores.computeRank(sortedScores, getPlayerId());
		unlock();
		return ret;
	}
	
	public int computeCreativityRank() throws RemoteException, InterruptedException
	{
		lock();
		Map<Integer, Double> sctab=TypeScore.creativite.calculer(getGame());
		SortedSet<PlayersScores> sortedScores = PlayersScores.calculer(sctab);
		int ret = PlayersScores.computeRank(sortedScores, getPlayerId());
		unlock();
		return ret;
	}
	
	public int computeAdaptationRank() throws RemoteException, InterruptedException
	{
		lock();
		Map<Integer, Double> sctab=TypeScore.adaptation.calculer(getGame());
		SortedSet<PlayersScores> sortedScores = PlayersScores.calculer(sctab);
		int ret = PlayersScores.computeRank(sortedScores, getPlayerId());
		unlock();
		return ret;
	}
	
	public LinkedList<IIdea> getAllIdeas() throws RemoteException, InterruptedException
	{
		lock();
		LinkedList<IIdea> ret = getGame().getAllIdeas();
		unlock();
		return ret;
	}
	
	public LinkedList<IPlayer> getAllPlayers() throws RemoteException, InterruptedException
	{		
		lock();
		LinkedList<IPlayer> ret = getGame().getAllPlayers();
		unlock();
		return ret;
	}
	
	public LinkedList<Integer> getAllPlayersIds() throws RemoteException, InterruptedException
	{
		lock();
		LinkedList<Integer> ret = getGame().getAllPlayersIds();
		unlock();
		return ret;
	}
	
	public int getMaxTokensByPlayer() throws RemoteException, InterruptedException
	{
		lock();
		int ret = getGame().getMaxTokensByPlayer();
		unlock();
		return ret;
	}
	
	public Map<Integer, Integer> getCurrentIdeasTokensL() throws InterruptedException
	{
		lock();
		Map<Integer, Integer> ret = super.getCurrentIdeasTokens();
		unlock();
		return ret;
	}
	
	public IIdea getIdea(Integer id) throws RemoteException, InterruptedException
	{
		lock();
		IIdea ret = getGame().getIdea(id);
		unlock();
		return ret;
	}
	
	public int getRemainingTokensL() throws InterruptedException
	{
		lock();
		int ret = super.getRemainingTokens();
		unlock();
		return ret;
	}
	
	public LinkedList<IComment> getAllComments() throws RemoteException, InterruptedException
	{
		lock();
		LinkedList<IComment> ret = getGame().getAllComments();
		unlock();
		return ret;
	}
	
	public IIdea getRootIdea() throws RemoteException, InterruptedException
	{
		lock();
		IIdea ret = getGame().getRootIdea();
		unlock();
		return ret;
	}
	
}
