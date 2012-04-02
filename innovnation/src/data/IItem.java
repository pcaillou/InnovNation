package data;

import java.util.LinkedList;

/**
 * An item has to be <b>hosted</b> by one or more ideas. 
 * 
 * @author Samuel Thiriot
 *
 */
public interface IItem 
extends IGameObject {

	String getDescription();

	LinkedList<Integer> getAllHosts();

}
