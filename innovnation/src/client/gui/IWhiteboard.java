package client.gui;


/**
 * 
 * The subpart of the GUI which displays all the ideas as a tree with a (supposely) nice layout. 
 * 
 * @author Samuel Thiriot
 *
 */
public interface IWhiteboard {

	/**
	 * Returns true when the layout is active (i.e. consuming CPU)
	 * @return
	 */
	public boolean isLayoutActive();
	
	/**
	 * Starts or stops (or does nothing) the layout.
	 * @param active
	 */
	public void setLayoutActive(boolean active);
	
	/**
	 * Returns the component which manages the selection of objects.
	 * @return
	 */
	public ISelectionControl getSelectionControl();
	
}
