package client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import data.CommentValence;
import data.IIdea;

import errors.AlreadyExistsException;
import errors.TooLateException;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;

public class DelegatingBotCore extends ClientCore {

	public final static String BOT_AVATAR = "usertile11.jpg";
	public final static String BOT_NAME = "Wall-e";
	private static Integer botCount = 0;

	private String name;
	private String avatar;
	
	private int nbIdeas;
	private int nbComments;
	
	private long timeCreation;
	private long nextAction;
	
	private IEventListener listener;
	
	/* liste des parametres */
	private int reactivity;
	private int creativity;
	private int relevance;
	private int adaptation;
	private int persuation;
	
	/**
	 * Cree un bot avec le listener fourni
	 * @param ui
	 */
	public DelegatingBotCore(IEventListener ui) {
		super();
		if(ui==null) throw new NullPointerException();
		listener = ui;
		
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
		long time = System.currentTimeMillis();
		if (time > nextAction)
		{
			LinkedList<IIdea> ideas = getGame().getAllIdeas();
			
			if (time%10 == 0)
			{ /* on ajoute une idee au hazard */
				Integer nbSources = (int) ((Math.random()*100));
				if (nbSources <= 95)
				{
					nbSources = 1;
				}
				else
				{
					nbSources = 2;
				}
				
				Collection<Integer> sources = new ArrayList<Integer>();
				
				for (int i = 0 ; i < nbSources ; i++)
				{
					Integer id = ideas.get((int)(Math.random()*ideas.size())).getUniqueId();
					if (!sources.contains(id))
					{
						sources.add(id);
					}
				}
				
				createIdea(name + " : Super idee " + time, "super description", sources);
				
			}
			else
			{ /* on ajoute un commentaire au hazard */
				Integer id = ideas.get((int)(Math.random()*ideas.size())).getUniqueId();
				Integer tokensGiven = getCurrentIdeasTokens().get(id);
				
				if (getRemainingTokens() == 0 && tokensGiven == 0)
				{
					createComment(id,name + " : super description",0,CommentValence.NEUTRAL);
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
						createComment(id,"super description",tokensToGive,CommentValence.NEUTRAL);
					}
					else if(tokensToGive > 0)
					{
						createComment(id,"super description",tokensToGive,CommentValence.POSITIVE);
					}
					else if(tokensToGive < 0)
					{
						createComment(id,"super description",tokensToGive,CommentValence.NEGATIVE);
					}
				}
				
				
				
			}
			
			
			nextAction = getNextAction(time);
		}
	}
	
	/**
	 * Recupere le temps auquel le bot fera sa prochaine action (donne par System.currentTimeMillis())
	 * @param time
	 * @return
	 */
	private long getNextAction(long time)
	{
		return (long) (time + ((10000 + (Math.random() * 50000)) / reactivity));
	}
	
	/**
	 * Reset le temps auquel le bot fera sa prochaine action
	 */
	public void resetNextAction()
	{
		nextAction = getNextAction(System.currentTimeMillis());
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
	public void createIdea(String name, String description, Collection<Integer> sources) throws AlreadyExistsException, TooLateException, RemoteException
	{
		
		getGame().addIdea(getPlayerId(),name, description, new ArrayList<Integer>(), sources);
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
	public void createComment(Integer idea, String description, int tokens, CommentValence valence) throws RemoteException
	{
		if (getRemainingTokens() < tokens)
		{
			System.err.println("Bot error : trying to use more than 10 tokens");
		}
		else
		{
			getGame().commentIdea(getPlayerId(), idea, description, tokens, valence);
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
