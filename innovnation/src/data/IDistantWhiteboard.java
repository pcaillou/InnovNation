package data;

import java.rmi.Remote;

/**
 * A distant whiteboard is remote, thus being reached through RMI
 * @author Samuel Thiriot
 *
 */
public interface IDistantWhiteboard extends IWhiteboard,  Remote {

}
