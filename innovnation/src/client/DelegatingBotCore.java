package client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
//import java.util.concurrent.Semaphore;

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

public class DelegatingBotCore extends ClientCore // */
{

	public final static int BOT_BASE_SPEED = 2500;
	public final static String BOT_NAME = "Wall-e ";
	
	public final static int PARAM_MAX_VALUE = 10;
	
	private static Integer botCount = 1;
	public static int ideaCount = 1;
	
	//private static Semaphore semaphore = new Semaphore(1, true);

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
	private int reactivity; /* vitesse du bot */
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
		creativity = (int) (Math.random()*PARAM_MAX_VALUE +1);
		relevance = (int) (Math.random()*PARAM_MAX_VALUE +1);
		adaptation = (int) (Math.random()*PARAM_MAX_VALUE +1);
		persuation = (int) (Math.random()*PARAM_MAX_VALUE +1);
		
		timeCreation = System.currentTimeMillis();
		nextAction = getNextAction(timeCreation) ;
		
		usingSemaphore = false;
		
		name = BOT_NAME + " " + botCount;
		avatar = Avatars.getOneAvatarRandomly();
		paused = true;
		
		botCount++;
		
		//TypeScore.adaptation.calculer(g, playerId)
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
			/* on calcule les chances d'obtenir une idee (entre 1 et 30) reduit par le nombre d'idees deja creees */
			double nbIdeas = getAllIdeas().size()-1;
			double nbPlayerIdeas = getNbIdeas();
			double nbPlayers = getAllPlayers().size();
			double ideaCreationChance = (20 * (1-Math.sqrt((nbIdeas+nbPlayerIdeas) / ((nbPlayers) + nbIdeas+nbPlayerIdeas))));
			/* un bot creatif va plus chercher a rajouter des idees */
			ideaCreationChance = ideaCreationChance + 5 * creativity / 5 ;
			/* un bot qui s'adapte va plus chercher à commenter */
			double commentCreationChance = 25 * adaptation / 5 + 25 * relevance / 5;
			
			
			if (Math.random()*100 <= ideaCreationChance)
			{ 
				/* on ajoute une idee */
				
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
					int id = getBestIdeaForIdea();
					if (!sources.contains(id))
					{
						sources.add(id);
					}
				}
				
				/* on cree l'idee */
				createIdea(sources);
				
			}
			else if (Math.random()*100 <= commentCreationChance)
			{
				/* on ajoute un commentaire */
				int id = getBestIdeaForVote();
				int tokensToGive = 0;
				int tokensGiven = getCurrentIdeasTokensL().get(id);
				double tokensIdea = getIdea(id).getTotalBids();
				
				/* on choisit si le commentaire doit etre positif ou negatif */
				double chancePositiveComments = 30f + 7f * adaptation * tokensIdea / (getAllPlayersIds().size()*getMaxTokensByPlayer());
				
				double rand = Math.random()*100;
										
				if (chancePositiveComments >= rand)
				{
					/* on fait un commentaire positif */					
					/* on donne un nombre de tokens variable */
					tokensToGive = (int) ((Math.random()*getMaxTokensByPlayer()) - (getMaxTokensByPlayer()-getRemainingTokensL()-tokensGiven)/2) - tokensGiven;
					
					/* si on a deja donne plus de tokens a cette idee, on ne donne aucun token */
					if (tokensToGive > 0)
					{
						/* si on n'a pas assez de tokens, on en retire aux autres idees */
						if (tokensToGive > getRemainingTokensL())
						{
							int tokensNeeded = tokensToGive - getRemainingTokensL();
							Set<Entry<Integer, Integer>> tokens = getCurrentIdeasTokensL().entrySet();
							ArrayList<Integer> alreadySeenIdeas = new ArrayList<Integer>();
							
							System.out.println("--------------------------------------------------------------");
							System.out.println("Je dois trouver " + tokensToGive + " tokens et il m'en reste " + getRemainingTokensL());
							System.out.print("Idees ou j'ai vote : ");
							for (Entry<Integer, Integer> e : tokens)
							{
								if (e.getValue() > 0)
								{
									System.out.print("[" + e.getKey() + "," + e.getValue() + "] ");
								}
							}
							System.out.println();
							System.out.print("Idees ou j'ai deja retire des tokens : ");
							for (Integer i : alreadySeenIdeas)
							{
								System.out.print("[" + i + "] ");
							}
							System.out.println();
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
									int removed = Math.min(getCurrentIdeasTokensL().get(worstIdea),tokensNeeded);
									alreadySeenIdeas.add(worstIdea);
									tokensNeeded -= removed;
									System.out.println("je recupere " + removed + " sur l'idee " + worstIdea);
									createComment(worstIdea, -removed, CommentValence.NEUTRAL);
								}
							}
							System.out.println("--------------------------------------------------------------");
						}

						createComment(id,tokensToGive,CommentValence.POSITIVE);
					}
				}
				else if (chancePositiveComments / rand < 0.8)
				{
					/* on fait un commentaire neutre */
					
					createComment(id, 0, CommentValence.NEUTRAL);
				}
				else
				{
					/* on fait un commentaire negatif */
					
					/* on retire une partie des tokens donnes */
					tokensToGive = (int) -(Math.random()*tokensGiven/2 + tokensGiven/2);
					createComment(id,tokensToGive,CommentValence.NEGATIVE);
					
				}
			}
			nextAction = getNextAction(time);
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
		
		for (Entry<Integer, Long> h : lastHeuristics.entrySet())
		{
			totalHeuristic += Math.pow(h.getValue(), 2);
		}
		
		/* on recupere une idee au hazard, les chances d'obtenir une idee sont augmentee si son heuristique est grande */
		long rand = (int)(Math.random()*totalHeuristic);
		for (IIdea i : getAllIdeas())
		{
			long h = (long) Math.pow(lastHeuristics.get(i.getUniqueId()), 2);
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
		
		for (Entry<Integer, Long> h : lastHeuristics.entrySet())
		{
			totalHeuristic += h.getValue();
		}
		
		/* on recupere une idee au hazard, les chances d'obtenir une idee sont augmentee si son heuristique est grande */
		long rand = (int)(Math.random()*totalHeuristic);
		for (IIdea i : getAllIdeas())
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
		
		/* on recupere la valeur d'une idee, multipliee par la pertinence du bot (pour qu'il la repere plus facilement) */
		long ideaValue = idea.getIdeaValue() * relevance;
		
		/* on recupere les commentaires de l'idee, chaque commentaire etant valué par la persuasion de la source et l'adaptation du bot */
		long commentValue = 0;
		for (IComment c : getAllComments())
		{
			if (c.getIdea().getUniqueId() == id)
			{
				int actualCommentValue = (c.getCommentValue() + c.getTokensCount() * 5)* adaptation;
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
				double timeElapsed = time - c.getCreationDate();
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
		return (long) (time + ((BOT_BASE_SPEED + (Math.random() * BOT_BASE_SPEED*49)) / reactivity));
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
				long h = heuristicIdea(i.getUniqueId());
				lastHeuristics.put(i.getUniqueId(),h);
			}
			/* on ajoute de nouveaux noeuds si on peut */
			newChilds = false;
			boolean addChild;
			for (IIdea c : getAllIdeas())
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
		//semaphore.acquire();
	}
	
	/**
	 * Le bot informe les autres qu'il quitte une partie "sensible", les autres pourront maintenant lock à leurs tour
	 */
	public void unlock()
	{
		//semaphore.release();
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
		Integer value = (int)(Math.random() * (IIdea.IDEA_MAX_VALUE*creativity/10));
		getGame().addIdea(getPlayerId(),"Idea " + ideaCount, "this idea has a value of " + value, new ArrayList<Integer>(), sources);
		ideaCount++;
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
			Integer value = (int)(Math.random() * (IComment.COMMENT_MAX_VALUE * creativity / 10 ));
			getGame().commentIdea(getPlayerId(), idea, "this comment has a value of " + value, tokens, valence);
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
