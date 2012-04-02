/**
 * 
 */
package functions.logs;

import data.IComment;
import data.IIdea;
import data.IItem;

/**
 * @author Pierre Marques
 *
 */
public interface LogPack {
	
	//chaque implémenteur doit avoir cette fonction
	//qui renvoie la ligne de titres correspondant à ce pack
	//static String titles();

	//chaque implémenteur doit avoir cette fonction
	//qui renvoie la ligne de 0 correspondant à ce pack
	//static String zeros();
	
	/**
	 * renvoie la ligne de données correspondant à ce pack
	 * @param time temps en seconde depuis le début de la partie
	 * @return
	 */
	String log(int time);
	

	/**
	 * @param playerId
	 * @param createdItem
	 * @throws NullPointerException si createdItem est null;
	 */
	void updateOnItem(int playerId, IItem createdItem);
	
	/**
	 * @param playerId
	 * @param createdIdea
	 * @throws NullPointerException si createdIdea est null;
	 */
	void updateOnIdea(int playerId, IIdea createdIdea);
	
	/**
	 * @param playerId
	 * @param createdComment
	 * @throws NullPointerException si createdComment est null;
	 */
	void updateOnComment(int playerId, IComment createdComment);

}
