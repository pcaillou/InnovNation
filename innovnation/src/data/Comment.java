/**
 * 
 */
package data;

/**
 * @author Pierre Marques
 *
 */
public class Comment extends GameObject implements IComment {
	
	private static final long serialVersionUID = 1L;
	
	private int tokens;
	private final int commented;
	private final CommentValence valence;
	private String text;
	private Integer indexIdea;
	
	/**
	 * @param authorId
	 * @param commented
	 * @param shortName
	 * @param text
	 * @param valence
	 */
	public Comment(int authorId, int commented, String shortName, String text, CommentValence valence) {
		super(authorId, shortName);
		this.commented=commented;
		this.valence=valence;
		this.text=text;
		this.tokens = 0;
	}

	/**
	 * @param authorId
	 * @param commented
	 * @param shortName
	 * @param text
	 * @param valence
	 * @param tokens
	 */
	public Comment(int authorId, int commented, String shortName, String text, CommentValence valence, int tokens) {
		super(authorId, shortName);
		this.commented=commented;
		this.valence=valence;
		this.text=text;
		this.tokens = tokens;
		
	}

	public void setIndexSource(Integer indexSource)
	{
		indexIdea = indexSource;
	}
	
	public Integer getIndexSource()
	{
		return indexIdea;
	}
	
	/* (non-Javadoc)
	 * @see data.IComment#get()
	 */
	@Override
	public int get() {
		return commented;
	}

	/* (non-Javadoc)
	 * @see data.IComment#getText()
	 */
	@Override
	public String getText() {
		return text;
	}

	/* (non-Javadoc)
	 * @see data.IComment#getValence()
	 */
	@Override
	public CommentValence getValence() {
		return valence;
	}

	@Override
	public String toString() {
		return new StringBuilder(super.toString())
			.append(": \"").append(getText()).append("\"")
			.append(' ').append(valence==CommentValence.NEGATIVE?'-':'+').append(tokens)
			.toString();
	}

	@Override
	public int getTokensCount() {
		return tokens;
	}

}
