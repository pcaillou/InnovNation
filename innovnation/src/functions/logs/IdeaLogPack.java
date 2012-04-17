/**
 * 
 */
package functions.logs;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import data.CommentValence;
import data.IComment;
import data.IIdea;
import data.IItem;
// AD import data.IPlayer;
import functions.IGame;

/**
 * @author Pierre Marques
 *
 */
public class IdeaLogPack implements LogPack {
	
	private IGame game;
	private int myId;
	
	//personnal data
	private int ownerId, creationTime,
	items, comments, PositiveComments,NegativeComments, votes, tokens, tokensMax,
	shortLength, longLength;
	private double PositiveProportion,NegativeProportion,NulProportion;
	private int depthMin;//distance à la racine par le plus court chemin possible
	private int depthMax;//distance à la racine par le plus long chemin possible

	//parents' data
	private String parentsId;
	private int parents,
	parentItems, parentItemsMin, parentItemsMax,
	parentComments, parentCommentsMin, parentCommentsMax,
	parentVotes, parentVotesMin, parentVotesMax,
	parentCreationTimeMin, parentCreationTimeMax;
	
	private int parentCreationTimes;//local data to fasten computation
	
	private double parentItemMean, parentCommentsMean, parentVotesMean, parentCreationTimeMean;
	
	private boolean sameOwnerParent;
	private int sameOwnerParents;

	
	
	/*
	//children's data
	public int children;
	//*/
	private int childrens;

	
	private void considerNewChild(int playerId,IIdea newChild){
		childrens++;
	}

	/**
	 * considers a new parent to be added;
	 * @param newParent
	 */
	private void considerParent(IIdea newParent){
		parents++;

		//depthMin
		//depthMax
		
		int items = newParent.getItemsIds().size();

		if(newParent.getPlayerId() == ownerId){
			sameOwnerParent = true;
			sameOwnerParents++;
		}

		parentItems += items;
		if( parentItemsMin == -1 || items < parentItemsMin) parentItemsMin = items;
		parentItemsMax = Math.max(parentItemsMax, items);

		try {
			DefaultMutableTreeNode f = game.getIdeaComments(newParent.getUniqueId());
			Enumeration<?> sons = f.breadthFirstEnumeration();
			int parentalComments=0;
			int parentalVotes = 0;
			sons.nextElement();//remove the idea itself
			while(sons.hasMoreElements()){
				IComment c = (IComment) ((DefaultMutableTreeNode) sons.nextElement()).getUserObject();
				parentalComments++;
				if(c.getTokensCount()!=0) parentalVotes++;
			}

			parentComments+= parentalComments;
			if( parentCommentsMin == -1 || parentalComments < parentCommentsMin) parentCommentsMin = parentalComments;
			parentCommentsMax = Math.max(parentCommentsMax, parentalComments);

			parentVotes+= parentalVotes;
			if( parentVotesMin == -1 || parentalVotes < parentVotesMin) parentVotesMin = parentalVotes;
			parentVotesMax = Math.max(parentVotesMax, parentalVotes);

		} catch (RemoteException e) {
			System.err.println("remote exception disallowed to count comments from idea "+newParent.getUniqueId());
		}

		int creationTime = (int) newParent.getCreationDate();
		
		parentCreationTimes += creationTime;
		if( parentCreationTimeMin == -1 || creationTime < parentCreationTimeMin) parentCreationTimeMin = creationTime;
		parentCreationTimeMax = Math.max(parentCreationTimeMax, creationTime);
	}
	
	private void checkParentMeans(){
		if(parents!=0) {
			this.parentItemMean = parentItems / (double) parents;
			this.parentCommentsMean = parentComments / (double) parents;
			this.parentVotesMean = parentVotes / (double) parents;
			this.parentCreationTimeMean = parentCreationTimes / (double) parents;
	
		} else {
			this.parentItemMean = 0;
			this.parentCommentsMean = 0;
			this.parentVotesMean = 0;
			this.parentCreationTimeMean = 0;
		}
	}

	/**
	 * @param idea
	 * @param game //should contains idea, it is requested to access to comments
	 * @param time
	 */
	public int getParentDepthMin(IIdea idea)
	{
		int res=-1;
		int temp=0;
		try {
			if (idea.getUniqueId()==game.getRootIdea().getUniqueId())
			{
				return 0;
			}
			else
			{
				for (IIdea i : idea.getParents())
				{
					temp=getParentDepthMin(i);
					if (res==-1) res=temp+1;
					else if (temp<res) res=temp+1;
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	public int getParentDepthMax(IIdea idea)
	{
		int res=-1;
		int temp=0;
		try {
			if (idea.getUniqueId()==game.getRootIdea().getUniqueId())
			{
				return 0;
			}
			else
			{
				for (IIdea i : idea.getParents())
				{
					temp=getParentDepthMax(i);
					if (temp>=res) res=temp+1;
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	public IdeaLogPack(IGame game, IIdea idea, int time) {
		this.game = game;
		this.myId = idea.getUniqueId();
		
		this.creationTime = time;
		try {
			if (myId==game.getRootIdea().getUniqueId()) creationTime=0;
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.ownerId = idea.getPlayerId();
		this.items = idea.getItemsIds().size();
		
		//some strange computation
		this.depthMin = 0;//TODO depthMin computing
		this.depthMax = 0;//TODO depthMax computing
		try {
			if (this.myId!=game.getRootIdea().getUniqueId())
			{
				depthMin=getParentDepthMin(game.getIdea(myId));
				depthMax=getParentDepthMax(game.getIdea(myId));
				
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		this.comments = 0;
		this.votes = 0;
		this.tokens = 0;
		this.tokensMax = 0;

		String s = idea.getShortName();
		this.shortLength = s.codePointCount(0, s.length());//consider unicode
		this.longLength = idea.getDesc().codePointCount(0, idea.getDesc().length());
		
		Collection<IIdea> myParents = idea.getParents();
		this.parents = myParents.size();
		this.parentsId = "[";
		
		for (IIdea parent : idea.getParents())
		{
			if (this.parentsId.equals("["))
			{
				this.parentsId += parent.getUniqueId();
			}
			else
			{
				this.parentsId += "," + parent.getUniqueId();
			}
		}
		this.parentsId +="]";
		
		this.childrens=0;
		this.sameOwnerParent = false;
		this.sameOwnerParents = 0;
		
		this.parentItems = 0;
		this.parentItemsMin = -1;
		this.parentItemsMax = 0;
		
		this.parentComments = 0;
		this.parentCommentsMin = -1;
		this.parentCommentsMax = 0;
		
		this.parentVotes = 0;
		this.parentVotesMin = -1;
		this.parentVotesMax = 0;
		
		this.parentCreationTimes = 0;
		this.parentCreationTimeMin = -1;
		this.parentCreationTimeMax = 0;
		
		for (IIdea i : myParents) considerParent(i);
		checkParentMeans();
	}
	
	static public String titles() {
		return "ideaId;ideaOwnerId;ideaCreationTime;ideaDepthMin;ideaDepthMax;"+
	           "ideaItems;ideaComments;PositiveComments;NegativeComments;PositiveProportion;NegativeProportion;NulProportion;ideaVotes;ideaTokens;ideaTokensMax;"+
			   "ideaShortLength;ideaLongLength;"+
	           "ideaParentsId;ideaParents;ideaParentItems;ideaParentItemsMin;ideaParentItemsMax;"+
			   "ideaParentComments;ideaParentCommentsMin;ideaParentCommentsMax;"+
	           "ideaParentVotes;ideaParentVotesMin;ideaParentVotesMax;"+
			   "ideaParentCreationTimeMin;ideaParentCreationTimeMax;"+
	           "ideaParentItemMean;ideaParentCommentsMean;ideaParentVotesMean;ideaParentCreationTimeMean;"+
			   "ideaHasSameOwnerParent;ideaSameOwnerParents;"+
	           "childrens;";
	}

	static public String zeros() {
		return "0;0;0;0;0;"+
			   "0;0;0;0;0;0;0;0;0;0;"+
			   "0;0;"+
			   "[];0;0;0;0;"+
			   "0;0.0;0.0;"+
			   "0.0;0.0;0.0"+
			   "0;0;0;0;"+
			   "0;0;"+
			   "0;";
	}
	
	/* (non-Javadoc)
	 * @see functions.logs.LogPack#log()
	 */
	@Override
	public String log(int time) {
		StringBuilder sb = new StringBuilder();
		sb.append(myId).append(';');
		sb.append(ownerId).append(';');
		sb.append(creationTime).append(';');
		sb.append(depthMin).append(';');
		sb.append(depthMax).append(';');
		
		sb.append(items).append(';');
		sb.append(comments).append(';');
		sb.append(PositiveComments).append(';');
		sb.append(NegativeComments).append(';');
		sb.append(PositiveProportion).append(';');
		sb.append(NegativeProportion).append(';');
		sb.append(NulProportion).append(';');
		sb.append(votes).append(';');
		sb.append(tokens).append(';');
		sb.append(tokensMax).append(';');
		
		sb.append(shortLength).append(';');
		sb.append(longLength).append(';');

		sb.append(parentsId).append(';');
		sb.append(parents).append(';');
		sb.append(parentItems).append(';');
		sb.append(parentItemsMin).append(';');
		sb.append(parentItemsMax).append(';');
		
		sb.append(parentComments).append(';');
		sb.append(parentCommentsMin).append(';');
		sb.append(parentCommentsMax).append(';');
		
		sb.append(parentVotes).append(';');
		sb.append(parentVotesMin).append(';');
		sb.append(parentVotesMax).append(';');
		
		sb.append(parentCreationTimeMin).append(';');
		sb.append(parentCreationTimeMax).append(';');
		
		sb.append(parentItemMean).append(';');
		sb.append(parentCommentsMean).append(';');
		sb.append(parentVotesMean).append(';');
		sb.append(parentCreationTimeMean).append(';');
		
		sb.append(sameOwnerParent).append(';');
		sb.append(sameOwnerParents).append(';');

		sb.append(childrens).append(';');

		return sb.toString();
	}

	public void updateOnItem(int playerId, IItem createdItem) {
		if(createdItem==null) throw new NullPointerException();
		//nothing, really, this is not a missing feature
	}
	
	@Override
	public void updateOnIdea(int playerId, IIdea createdIdea) {
		if(createdIdea==null) throw new NullPointerException();
		//if( <this is parents of that> ) considerNewChild( <that> );
		if (createdIdea.getParents().size()>0)
		{
			for (int i:createdIdea.getParentsIds())
				if (i==myId) considerNewChild(playerId,createdIdea);
		}
		
	}


	public void updateOnLinkToParent(int playerId, IIdea newParent) {
		considerParent(newParent);
	}

	public void updateOnLinkToChild(int playerId, IIdea newChild) {
		//considerNewChild(newChild);
	}
	
	@Override
	public void updateOnComment(int playerId, IComment createdComment) {
		if(createdComment==null) throw new NullPointerException();
		//check what is commented:
		if(myId == createdComment.get()) {
			//this is one of my comments
			comments++;
			if (createdComment.getValence().equals(CommentValence.POSITIVE))
			{
				PositiveComments++;
			}
			if (createdComment.getValence().equals(CommentValence.NEGATIVE))
			{
				NegativeComments++;
			}
			PositiveProportion=((double)PositiveComments)/comments;
			NegativeProportion=((double)NegativeComments)/comments;
			NulProportion=1.0-PositiveProportion-NegativeProportion;
			int tokenCount = createdComment.getTokensCount();
			if(tokenCount !=0) {
				votes++;
				tokens+=tokenCount;
				if(tokens>tokensMax) tokensMax = tokens;
			}
		} else {
			try {
				if(game.getIdeaParentIds(myId).contains(createdComment.get())) {
					//this one of my parents' comments
					parentComments++;
					if(createdComment.getTokensCount() !=0) parentVotes++;

					int parentalComments = 0;
					int parentalVotes = 0;
					Enumeration<?> sons = game.getIdeaComments(createdComment.get()).breadthFirstEnumeration();
					sons.nextElement();//remove the idea itself
					while(sons.hasMoreElements()){
						IComment c = (IComment) ((DefaultMutableTreeNode) sons.nextElement()).getUserObject();
						parentalComments++;
						if(c.getTokensCount()!=0) parentalVotes++;
					}

					if( parentCommentsMin == -1 || parentalComments < parentCommentsMin) {
						parentCommentsMin = parentalComments;
					}
					parentCommentsMax = Math.max(parentCommentsMax, parentalComments);

					if( parentVotesMin == -1 || parentalVotes < parentVotesMin) parentVotesMin = parentalVotes;
					parentVotesMax = Math.max(parentVotesMax, parentalVotes);

					checkParentMeans();
				}
			} catch (RemoteException e) {
				//RemoteException while not using RMI
				e.printStackTrace();
			}
		}
	}

}
