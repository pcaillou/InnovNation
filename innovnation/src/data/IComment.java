package data;

/**
 * 
 * @param <T> the kind of thing this is comment made for.
 */
public interface IComment extends IGameObject {


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
}
