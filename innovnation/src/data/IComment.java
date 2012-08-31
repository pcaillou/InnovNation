package data;

/**
 * 
 * @param <T> the kind of thing this is comment made for.
 */
public interface IComment extends IGameObject {
	
	/** valeur maximale d'une idee (servant d'heuristique pour les bots pour les bots */
	public static final int COMMENT_MAX_VALUE = 100;

	/**
	 * Retourne la valeur d'un commentaire (servant au bot pour savoir si une idee est bonne ou non)
	 * @return int
	 */
	public int getCommentValue();
	
	/**
	 * Met a jour la valeur d'un commentaire (servant au bot pour savoir si une idee est bonne ou non)
	 * @param v
	 */
	public void setCommentValue(int v);
	
	/**
	 * @return the commented element id
	 */
	public int get();

	/**
	 * @return
	 */
	public String getText();

	/**
	 * Tells how much is this comment approving what it comments. 
	 * @return a CommentValence telling the strength of this comment.
	 */
	public CommentValence getValence();

	/**
	 * Get the count of tokens
	 * @return
	 */
	public int getTokensCount();
	
	public void setIndexSource(Integer indexSource);
	
	public Integer getIndexSource();
	
	public Integer getIndex();
	
	public void setIdea(IIdea idea);
	
	public IIdea getIdea();
	
}
