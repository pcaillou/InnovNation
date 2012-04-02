package client.gui.prefuse;

import java.awt.event.MouseEvent;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import prefuse.controls.ControlAdapter;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import client.gui.GuiTestMain;


/**
 * 
 * <p>
 * On right click (by default), displays a contextual menu.
 * </p>
 * 
 * <ul>
 * <li>the menu may be hidden either by itself (a button was clicked, for instance), or by the controladapter (user clicked somewhere, for instance)</li>
 * <li>TODO: freeze display one clicked ?</li>
 * </ul>
 * 
 * 
 * 
 * TODO
 * 
 * @author <a href="http://jheer.org">Samuel Thiriot</a>
 */
public class ContextualMenuControl extends ControlAdapter {

	private Logger logger = Logger.getLogger("client.gui.contextmenu");
	
    private int m_button = RIGHT_MOUSE_BUTTON;
    
    private ClientWhiteboardSWT whiteboard;
    
    private GuiTestMain guiTestMain;
    
    private Control swtHost;
    
    private org.eclipse.swt.widgets.Display swtDisplay = null;

    private MenuItem itemExtendInIdea = null;
    private MenuItem itemVoteOnIdea = null;
    
    private Menu swtMenuItem = null;

    private Menu swtMenuBackground = null;

    /**
     * The item for which a contextual menu is currently displayed. 
     * Null if nothing displayed. 
     */
    private VisualItem itemDisplayed = null;
    
    /**
     * The menu which is currently displayed 
     * (or null)
     */
    private Menu menuDisplayed = null;

    
    
    public final static String TEXT_FIT = "Tout voir";
    public final static String TEXT_SHOW_ROOT = "Voir idée racine";

    
    /**
     * Create a new ZoomToFitControl.
     */
    public ContextualMenuControl(Control swtHost, ClientWhiteboardSWT whiteboard, GuiTestMain guiTestMain) {
    	this.swtHost = swtHost;
    	swtDisplay = swtHost.getDisplay();
    	
    	this.whiteboard = whiteboard;
    	this.guiTestMain = guiTestMain;
    	
    	initMenus();
    }
    

   
    private void initMenus() {
    	
    	logger.debug("init menus");
    	
    	MenuListener menuClosedListener = new MenuListener() {
			
			@Override
			public void menuShown(MenuEvent arg0) {
				
			}
			
			@Override
			public void menuHidden(MenuEvent arg0) {
				// the menu was hidden (by itelf); update the item field there...
				itemDisplayed = null;
				menuDisplayed = null;
				logger.debug("the menu was closed (by itself)");
			}
		};
		
    	// =================== for the background 
    	swtMenuBackground = new Menu(swtHost);
    	{
    		swtMenuBackground.addMenuListener(menuClosedListener);
    	    
	    	MenuItem item1 = new MenuItem (swtMenuBackground, SWT.PUSH);
	    	item1.setText (TEXT_FIT);
	    	item1.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					whiteboard.fitToScreen();
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});
	    	
	    	MenuItem item2 = new MenuItem (swtMenuBackground, SWT.PUSH);
	    	item2.setText (TEXT_SHOW_ROOT);
	    	item2.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					whiteboard.centerOnRootIdea();
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});
	    	
	    	
    	}    	

		
    	// =================== for an item
    	swtMenuItem = new Menu(swtHost);
    	{
	    	swtMenuItem.addMenuListener(menuClosedListener);

	    	itemVoteOnIdea = new MenuItem (swtMenuItem, SWT.PUSH);
	    	itemVoteOnIdea.setText ("Miser / Commenter");
	    	itemVoteOnIdea.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					guiTestMain.clickVoteOnIdea();	
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});
	    	itemExtendInIdea = new MenuItem (swtMenuItem, SWT.PUSH);
	    	itemExtendInIdea.setText ("Etendre avec une idée");
	    	itemExtendInIdea.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					guiTestMain.clickCreateIdea();	
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});
    	}    	


    }
 
    
    /**
     * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    @Override
    public void itemClicked(VisualItem item, MouseEvent e) {
           
    	//logger.debug("item clicked: "+item+", "+e);
    	
    	if (UILib.isButtonPressed(e, m_button)) {
  	  		// this is the button that is supposed to display the menu
  	  		

	  	  	if (itemDisplayed == item) {
	  	  		logger.debug("menu already displayed for this item");
	      		return;	// exit if this item is already currently displayed !
	  	  	} 
	  	  	
	  	  	/*else if (itemDisplayed != null) {
	  	  		// first hide menu
	  	  		logger.debug("menu displayed for another item; first hiding it");
	  	  		hideMenus();
	  	  	}*/
	  	  	logger.debug("showing the menu for item");
      	
	    	swtIndirectShowMenu(
	    			swtMenuItem,
	    			e.getXOnScreen(),
	    			e.getYOnScreen(),
	    			item
	    			);
	    	
  	  	} else {
  	  		
  	    	//logger.debug("should hide the menu (not the right button)");
  	      	
  	  		// not the expected button => hide menus
  	  		//hideMenus();
  	  	}
  	  	
    	
    	
    }

    
    @Override
    public void mousePressed(MouseEvent e) {
      	
    	if (UILib.isButtonPressed(e, m_button)) {
  	  		// this is the button that is supposed to display the menu
  	  		
    		if (menuDisplayed != null) {
	  	  		// first hide menu
	  	  		logger.debug("another menu displayed; first hiding it");
	  	  		hideMenus();
	  	  	}
	  	  	logger.debug("showing the background menu");
      
	  	  	swtIndirectShowMenu(
				swtMenuBackground,
				e.getXOnScreen(),
				e.getYOnScreen(),
				null
			);
	  	  	
  	  	} else {
  	      	logger.debug("should hide the menu (click anywere)");
  	      	hideMenus();
  	  	}
    	
    } 
    
    protected void swtDirectHideMenu(Menu menu) {
    	
    	if (swtDisplay.isDisposed())
    		return;
    	
    	if (menu.isVisible()) {
    		menu.setVisible(false);
    		swtHost.setMenu(null);
    	}

    }
    
    /**
     * Workaround: due to a GTK problem (Linux and other Unix), 
     * popup menus are not always displayed. This tries several 
     * times to display it. 
     * 
     * see
     * http://dev.eclipse.org/newslists/news.eclipse.platform.swt/msg33992.html
     * http://www.eclipsezone.com/eclipse/forums/t95687.html
     * @param menu
     * @param retriesRemaining
     */
    protected void retryVisible(final Menu menu, final int retriesRemaining) {
    	
    	swtDisplay.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				
				if (menu.isVisible()) {
					logger.debug("made visible, remaining "+retriesRemaining);
					
				} else if (retriesRemaining > 0) {
					
					logger.debug("retry visible, remaining "+(retriesRemaining-1));
					
					//swtHost.getShell().forceFocus();
					//swtHost.getShell().forceActive();
					//menu.setVisible(false);
					menu.setVisible(false);
					
					{
						Shell shell = new Shell(swtDisplay, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
						shell.setSize(10, 10);
						shell.setBackground(swtDisplay.getSystemColor(SWT.COLOR_RED));
						shell.setText("");
						shell.setVisible(false);
						shell.open();
						shell.dispose();
					}
					swtHost.getShell().forceActive();
					
					//forceFocus();
					//forceActive();
					menu.setVisible(true);
					
					retryVisible(menu, retriesRemaining-1);
					
				} else {
					logger.warn("unable to make it visible :-(");
					
				}
					
			}
		});
    }
    

    /**
     * Workaround: due to a GTK problem (Linux and other Unix), 
     * popup menus are not always displayed. This tries several 
     * times to display it. 
     * @param menu
     */
    protected void retryVisible(final Menu menu) {
    	retryVisible(menu, 10);
    }
    
    protected void swtDirectShowMenu(final Menu menu, int x, int y, VisualItem itemToDisplay) {
    	
		if (swtDisplay.isDisposed())	 // possible quick exit
    		return;

		menuDisplayed = menu;
		
		swtHost.setMenu(menu);
		
		menu.setLocation(new Point(x, y));
    	
		menu.setVisible(true);
		
		swtHost.setFocus();
		
		itemDisplayed = itemToDisplay;
		// TODO focus ???
		
		retryVisible(menu);
		
		// http://dev.eclipse.org/newslists/news.eclipse.platform.swt/msg33992.html
		
    }
    
    protected void swtIndirectShowMenu(final Menu menu, final int x, final int y, final VisualItem itemToDisplay) {
    	
    	swtDisplay.asyncExec(new Runnable() {
			
			@Override
			public void run() {
			
				swtDirectShowMenu(menu, x, y, itemToDisplay);
			}
		});
    	
    }

    
    protected void hideMenus() {

    	if (menuDisplayed == null) 
    		return; // nothing to hide, quick exit
    	
    	swtDisplay.asyncExec(new Runnable() {
			
			@Override
			public void run() {

				if (swtDisplay.isDisposed())	 // possible quick exit
		    		return;

			  	logger.debug("actually hiding the menu");
		      	
			  	//if (menuDisplayed != null)
//			  		menuDisplayed.setVisible(false);
			  	
			  	swtDirectHideMenu(swtMenuItem);
			  	swtDirectHideMenu(swtMenuBackground);
			  	
				// TODO à compléter
		    	
				itemDisplayed = null;
				menuDisplayed = null;
			}
		});

    }
        
    
	public void setActionsEnabled(boolean enabled) {
		
		itemExtendInIdea.setEnabled(enabled);
	}

    
    
    
} // end of class ZoomToFitControl
