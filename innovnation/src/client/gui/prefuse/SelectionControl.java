package client.gui.prefuse;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import prefuse.controls.ControlAdapter;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import client.gui.ISelectionControl;
import client.gui.ISelectionListener;
import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;


/**
 * Enables the selection of one (or more) items / ideas / etc.
 * 
 * @author <a href="http://jheer.org">Samuel Thiriot</a>
 */
public class SelectionControl extends ControlAdapter implements ISelectionControl {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger("client.gui.selection");
	
    private int m_button1 = LEFT_MOUSE_BUTTON;
    private int m_button2 = RIGHT_MOUSE_BUTTON;
    
    /**
     * Visual items that were selected. Always access it in synchronized mode.
     */
    private Collection<VisualItem> selectedsItems = new ArrayList<VisualItem>(30);
 
    /**
     * Objects represented by the visual items that were selected.
     */
    private Collection<Object> selectedsGameObject = new ArrayList<Object>(30);
       
    private Collection<ISelectionListener> selectionListeners = new LinkedList<ISelectionListener>();
    
    
    /**
     * Create a new ZoomToFitControl.
     */
    public SelectionControl() {
    	
    }
    
    private void unselectAllSelected() {
    	
    	synchronized (selectedsItems) {
    		for (VisualItem vi : selectedsItems) {
        		vi.set(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_SELECTED, false);
        	}
        	selectedsItems.clear();	
        	selectedsGameObject.clear();
		}
    	fireEventSelectionChanged();
    	
    }
    
    public Collection<VisualItem> getSelected() {
    	synchronized (selectedsItems) {
    		return new LinkedList<VisualItem>(selectedsItems);
    	}
    }

    /* (non-Javadoc)
	 * @see client.gui.prefuse.ISelectionControl#getSelectedObjects()
	 */
    public Collection<Object> getSelectedObjects() {
    	synchronized (selectedsItems) {
    		return new LinkedList<Object>(selectedsGameObject);
    	}
    }
    
    @Override
	public void mouseClicked(MouseEvent e) {
    	unselectAllSelected();
    }


	/**
     * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    @Override
    public void itemClicked(VisualItem item, MouseEvent e) {
           
    	
    	if (UILib.isButtonPressed(e, m_button1) || UILib.isButtonPressed(e, m_button2)) {

    		boolean previous = item.getBoolean(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_SELECTED);
    	
    		boolean novelSelection = false;
    		
    		if (e.isControlDown() || e.isShiftDown()) {
    			
    			novelSelection = !previous;
    				
    		} else {
    			
    			novelSelection = true;
    			unselectAllSelected();
    		}
    		
    		// change the state of the object (for highlight during drawing)
    		item.set(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_SELECTED, novelSelection);
    		
    		synchronized (selectedsItems) {
	    		if (novelSelection && !selectedsItems.contains(item)) {
	    			selectedsItems.add(item);
	    			selectedsGameObject.add(
	    					item.get(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_OBJ)
	    					);
	    		}
    		}
    		
    		fireEventSelectionChanged();
    		
    	}    	
    	
    	
    }
    
    private void fireEventSelectionChanged() {
    	synchronized (selectionListeners) {
    		for (ISelectionListener list : selectionListeners) {
    			list.notifySelectionChanged();
    		}
		}
    	
    }

	@Override
	public void addListener(ISelectionListener list) {
		synchronized (selectionListeners) {
			if (!selectionListeners.contains(list))
				selectionListeners.add(list);
		}
	}

	@Override
	public Collection<IIdea> getSelectedIdeas() {
		
		Collection<IIdea> res = new LinkedList<IIdea>();
		
		synchronized (selectedsItems) {
			
			for (Object object : selectedsGameObject) {
				if (object instanceof IIdea)
					res.add((IIdea) object);	
			}
		}
		
		return res;
	}

	@Override
	public Collection<Integer> getSelectedIdeasIds() {

		Collection<Integer> res = new LinkedList<Integer>();
		
		synchronized (selectedsItems) {
			
			for (Object object : selectedsGameObject) {
				if (object instanceof IIdea)
					res.add(((IIdea) object).getUniqueId());	
			}
		}
		
		return res;
	}

	@Override
	public Collection<IItem> getSelectedItems() {

		Collection<IItem> res = new LinkedList<IItem>();
		
		synchronized (selectedsItems) {
			
			for (Object object : selectedsGameObject) {
				if (object instanceof IItem)
					res.add((IItem) object);	
			}
		}
		
		return res;
	}

	@Override
	public Collection<Integer> getSelectedItemsIds() {
		Collection<Integer> res = new LinkedList<Integer>();
		
		synchronized (selectedsItems) {
			
			for (Object object : selectedsGameObject) {
				if (object instanceof IItem)
					res.add(((IItem) object).getUniqueId());	
			}
		}
		
		return res;
	}

	@Override
	public Collection<IPlayer> getSelectedPlayers() {

		Collection<IPlayer> res = new LinkedList<IPlayer>();
		
		synchronized (selectedsItems) {
			
			for (Object object : selectedsGameObject) {
				if (object instanceof IPlayer)
					res.add((IPlayer) object);	
			}
		}
		
		return res;
	}

	@Override
	public Collection<Integer> getSelectedPlayersIds() {
		Collection<Integer> res = new LinkedList<Integer>();
		
		synchronized (selectedsItems) {
			
			for (Object object : selectedsGameObject) {
				if (object instanceof IPlayer)
					res.add(((IPlayer) object).getUniqueId());	
			}
		}
		
		return res;
	}

	@Override
	public Collection<IComment> getSelectedComments() {
		
		Collection<IComment> res = new LinkedList<IComment>();
		
		synchronized (selectedsItems) {
			
			for (Object object : selectedsGameObject) {
				if (object instanceof IComment)
					res.add((IComment) object);	
			}
		}
		
		return res;
	}

	@Override
	public Collection<Integer> getSelectedCommentsIds() {
		Collection<Integer> res = new LinkedList<Integer>();
		
		synchronized (selectedsItems) {
			
			for (Object object : selectedsGameObject) {
				if (object instanceof IComment)
					res.add(((IComment) object).getUniqueId());	
			}
		}
		
		return res;
	}
	

    
    
} 
