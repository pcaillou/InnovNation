package client.gui;

import java.util.Collection;

import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;

/**
 * Some component in charge of the selection of objects in a GUI, which enable the retriving of 
 * the list of selected objects.
 * 
 * @author Samuel Thiriot
 *
 */
public interface ISelectionControl {

	public Collection<Object> getSelectedObjects();

	public Collection<IIdea> getSelectedIdeas();
	public Collection<Integer> getSelectedIdeasIds();
	
	public Collection<IItem> getSelectedItems();
	public Collection<Integer> getSelectedItemsIds();
	

	public Collection<IComment> getSelectedComments();
	public Collection<Integer> getSelectedCommentsIds();
	
	public Collection<IPlayer> getSelectedPlayers();
	public Collection<Integer> getSelectedPlayersIds();

	
	public void addListener(ISelectionListener list);
	
}