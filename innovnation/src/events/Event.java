/**
 * 
 */
package events;

import java.io.Serializable;

/**
 * @author Pierre Marques
 *
 */
public interface Event extends Serializable {
	int getPlayerId();
}
