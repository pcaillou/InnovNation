/**
 * 
 */
package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import util.graph.Dag;
import client.LocalCopyOfGame;
import functions.IGame;

/**
 * 
 * TODO mises et gérer bids.
 * 
 * 
 * @author Pierre Marques
 *
 */
public final class Idea extends GameObject implements IIdea {
	
	private static final long serialVersionUID = 1L;
	
	private static Integer indexCount = 0;
	
	private Integer index;
	
	private int value;
	
	private ArrayList<Integer> parentsIndexs;
	
	LinkedList<Integer> items = null;

	transient private Dag<Integer, IIdea> graphIdeas = null;
	
	transient int nbTokens = 0;

	transient int nbTokensOtherPlayers = 0;

	transient int maxTokens = 0;

	
	private String desc = null;
	
	/**
	 * Default constructor for deserialization
	 */
	protected Idea() {
	}

	/**
	 * 
	 */
	public Idea(int authorId, String name, String desc, Dag<Integer, IIdea> graphIdeas, Collection<Integer> itemsIds) {
		super(authorId, name);
		if(graphIdeas==null) throw new NullPointerException("new Idea can't have a null graph");
		if(itemsIds==null) this.items = new LinkedList<Integer>();
		else this.items = new LinkedList<Integer>(itemsIds);
		this.graphIdeas = graphIdeas;
		this.desc = createIdeaDescWithReturn(desc);
		value =(int)(Math.random() * IIdea.IDEA_MAX_VALUE);
		
		/* on cree un nouvel index pour l'idee */
		parentsIndexs = new ArrayList<Integer>();
		index = indexCount;
		Idea.indexCount++;
	}
	
	private String createIdeaDescWithReturn(String descInit) {
	
		int MAX_CHARS = 50;
	
		if (descInit == null)
			return "";
		
		if (descInit.length() <= MAX_CHARS)
			return descInit;
		
		StringBuffer sb = new StringBuffer();
		int lastSpace = 0;
		int lengthCurrent = 0;
		int beginCurrent = 0;
		int i;
		for (i=0; i<descInit.length(); i++) {
			
			if (descInit.charAt(i) == ' ') 
				lastSpace = i;
			
			lengthCurrent++;
			
			if (lengthCurrent > MAX_CHARS) {
				if (lastSpace > beginCurrent) {
					sb.append(descInit.substring(beginCurrent, lastSpace));
					i=lastSpace+1;
					lastSpace = i;
					beginCurrent = i;
					lengthCurrent = 0;
				} else {
					sb.append(descInit.substring(beginCurrent, i));
					lastSpace = i;
					beginCurrent = i;
					lengthCurrent = 0;
				}
				sb.append("\n");
			}
		}
		sb.append(descInit.substring(beginCurrent));
		
		return sb.toString();
	}
	
	/**
	 * Fonction permetant a la classe locale de retrouver le meme index que le classe distante en regardant l'index de chaque IIdea
	 * @param i : copie de l'idee en local
	 */
	public static void watchIndex(IIdea i)
	{
		if (indexCount <= i.getIndex())
		{
			indexCount = i.getIndex()+1;
		}
	}
	
	public void addParentIndex(Integer _index)
	{
		parentsIndexs.add(_index);
	}	
	
	public ArrayList<Integer> getParentsIndexs()
	{
		return parentsIndexs;
	}
	
	public Integer getIndex()
	{
		return index;
	}
	/* (non-Javadoc)
	 * @see data.IIdea#getItemsIds()
	 * 
	 * TODO not efficient, find another solution
	 */
	@Override
	public LinkedList<Integer> getItemsIds() {
		//TODO think about honestly referencing instead of copying
		return new LinkedList<Integer>(items);
	}
	
	/* (non-Javadoc)
	 * @see data.IIdea#getParentsIds()
	 * 
	 * TODO nota: c'est extremement couteux cette creation de liste à chaque appel !
	 * A changer !
	 */
	@Override
	public Collection<Integer> getParentsIds() {
		return graphIdeas.getParentsIds(this.id);
	}
	
	@Override
	public Collection<Integer> getChildrenIds() {
		
		return graphIdeas.getChildrenIds(this.id);
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString()+" items: {");
		for(int i : items) sb.append(' ').append(i);
		sb.append(" }, parents: {");
		for(int i : getParentsIds()) sb.append(' ').append(i);
		return sb.append(" }").toString();
	}

	@Override
	public boolean hasParents() {
		return graphIdeas.hasParents(this.id);
	}

	@Override
	public boolean hasChildren() {
		return graphIdeas.hasChildren(this.id);
	}

	@Override
	public Collection<IIdea> getChildren() {
		return graphIdeas.getChildren(this.id);
	}

	@Override
	public Collection<IIdea> getParents() {
		return graphIdeas.getParents(this.id);
	}

	
	
	
	@Override
	public Collection<IItem> getItems() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IComment> getComments() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer getTotalBids() {

		return nbTokens;
	}

	@Override
	public Integer getMaxBids() {

		return maxTokens;
	}

	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

		aInputStream.defaultReadObject();
		if (LocalCopyOfGame.getLocalCopy().ideas != null)
		{
			graphIdeas = LocalCopyOfGame.getLocalCopy().ideas;
		}
		else
		{
			System.out.println("POINTEUR NULL");
			graphIdeas = new Dag<Integer, IIdea>();
		}
		
 
	}
	
    /**
     * Test of serialisation of Idea !
     * @param args
     */
    public static void main(String[] args) {
    	
    	Logger logger = Logger.getLogger("testIdea");
    	
    	
		try {
			logger.info("Will test the serialisation of ideas");
	    	
	    	logger.info("Here is the test idea that will be serialized:");
	    
	    	// create a whiteboard
	    	Whiteboard whiteboard = new Whiteboard("rootIdea");
			
			logger.debug("created a whiteboard");
	    	
	    	LinkedList<Integer> allItemsIds = new LinkedList<Integer>();
	    	LinkedList<Integer> someItemsIds = new LinkedList<Integer>();

	    	for (int i=0; i<5; i++) {
	    		int createdItemId = whiteboard.addItem(1, "item "+i, "item desc"+i);
	    		allItemsIds.add(createdItemId);
	    		if (i%2 == 0)
	    			someItemsIds.add(createdItemId);
	    	}
	    	logger.debug("created some items");
	    	
	    	final int ideaId1 = whiteboard.addIdea(1, "nameOfIdea", "desc test", someItemsIds, Collections.<Integer>emptyList());
	    	IIdea ideaTest1 = whiteboard.getIdea(ideaId1);
	    	
	    	LinkedList<Integer> parentsIdea1 = new LinkedList<Integer>();
	    	parentsIdea1.add(ideaId1);
	    	final int ideaId2 = whiteboard.addIdea(1, "nameOfIdea2", "desc test", allItemsIds, parentsIdea1);
	    	IIdea ideaTest2 = whiteboard.getIdea(ideaId2);
	    	logger.debug("created test ideas");
	    	
	    	
	    	
	    	logger.info("Here are the ideas BEFORE serialization");
	    	
	    	logger.info("Idea1: "+ideaTest1.toString()+"\t, and its parents "+ideaTest1.getParents());
	    	logger.info("Idea2: "+ideaTest2.toString()+"\t, and its parents "+ideaTest2.getParents());
	    	
	    	File file  = null;
	    	
	    	logger.info("Serialization to file");
	    	{
	    		
		    	FileOutputStream fos = null;
		    	ObjectOutputStream out = null;
		    	try {
		    		file = File.createTempFile("myTmpFile", ".tmp");
					file.deleteOnExit();
					logger.debug("Data stored into "+file.getAbsolutePath());
					
					fos = new FileOutputStream(file);
		    		out = new ObjectOutputStream(fos);
		    		out.writeObject(ideaTest1);
		    		out.writeObject(ideaTest2);
		    		out.close();
		    	} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
	    	}
	    	
	    	logger.info("idea stored into the file");
	    	
	    	
	    	IIdea ideaRead1 = null;
	    	IIdea ideaRead2	 = null;
	    	{
		    	FileInputStream fis = null;
		    	ObjectInputStream in = null;
		    	try {
					
					fis = new FileInputStream(file);
					in = new ObjectInputStream(fis);
					ideaRead1 = (IIdea)in.readObject();
					ideaRead2 = (IIdea)in.readObject();
		    	} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
		    	
	    	}	
	    	
	    	logger.info("Here are the idea reloaded");

	    	logger.info("Idea1: "+ideaRead1.toString()+"\t, and its parents "+ideaRead1.getParents());
	    	logger.info("Idea2: "+ideaRead2.toString()+"\t, and its parents "+ideaRead2.getParents());
	    		    	
	    	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

	@Override
	public void betChanged(int nbTokens, IPlayer p, IGame g) throws RemoteException  {
		this.nbTokens += nbTokens;
		if (this.getPlayerId()!=p.getUniqueId())
		{
			maxbetChanged(nbTokens);
			p.majScores(g);
		}
	}
	@Override
	public void maxbetChanged(int nbTokens) {
		this.nbTokensOtherPlayers += nbTokens;
		if (this.nbTokensOtherPlayers>this.maxTokens) this.maxTokens=this.nbTokensOtherPlayers;
	}

	@Override
	public String getDesc() {
		return desc;
	}


	public int getIdeaValue() {
		return value;
	}
	
	public void setIdeaValue(int v) {
		value = v;
	}
}
