package client.gui.prefuse;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates the workaround for displaying a SWT popup menu 
 * over swing components under GTK (menu not displayed / visible bug).
 * 
 * TODO: remove from the Innovnation project.
 * 
 * @author Samuel Thiriot, INRIA
 *
 */
public class PopupOverAwt {

	private Display swtDisplay;
	private Shell swtShell ;
	
	private Menu swtPopupMenu;
	
	private final static int MAX_RETRIES = 10;
	
	public PopupOverAwt() {
		
				
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
    protected void retryVisible(final int retriesRemaining) {
    	
	

    	swtDisplay.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				
				if (swtPopupMenu.isVisible()) {
					System.out.println("made visible after "+(MAX_RETRIES-retriesRemaining)+" attempts");
					
				} else if (retriesRemaining > 0) {
					
					System.out.println("retrying (remains "+(retriesRemaining-1)+")");
					
					//swtHost.getShell().forceFocus();
					//swtHost.getShell().forceActive();
					//menu.setVisible(false);
					swtPopupMenu.setVisible(false);
					
					{
						Shell shell = new Shell(swtDisplay, 
								SWT.APPLICATION_MODAL | // should lead the window manager to switch another window to the front
								SWT.DIALOG_TRIM	// not displayed into taskbars nor in task managers 
								);
						shell.setSize(10, 10); // big enough to avoid errors from the gtk layer
						shell.setBackground(swtDisplay.getSystemColor(SWT.COLOR_RED));
						shell.setText("Not visible");
						shell.setVisible(false);
						shell.open();
						shell.dispose();
					}
					swtPopupMenu.getShell().forceActive();
					
					//forceFocus();
					//forceActive();
					swtPopupMenu.setVisible(true);
					
					retryVisible(retriesRemaining-1);
					
				} else {
					System.err.println("unable to display the menu, sorry :-(");
					
				}
					
			}
		});
    }
    
    protected void swtDirectShowMenu(int x, int y) {
    	
		if (swtDisplay.isDisposed())	 // possible quick exit
    		return;
		
		swtPopupMenu.setLocation(new Point(x, y));
    	
		System.out.println("Displaying the menu at coordinates "+x+","+y);
		swtPopupMenu.setVisible(true);
		
		// if GUI not based on GTK, the menu should already be displayed. 
		
		retryVisible(MAX_RETRIES); // but just in case, we ensure this is the case :-)
	
    }
    

	/**
	 * May be called from the AWT thread. Just called swtDirectShowMenu with the very same parameters, 
	 * but from the right thread.
	 * @param x
	 * @param y
	 * 
	 */
    protected void swtIndirectShowMenu(final int x, final int y) {
    	
    	swtDisplay.asyncExec(new Runnable() {
			
			@Override
			public void run() {
			
				swtDirectShowMenu(x, y);
			}
		});
    	
    }

    
	public void display() {
	
		// creates a SWT Shell
		swtDisplay = new Display();
		swtShell = new Shell(swtDisplay);
		swtShell.setText("click somewhere !");
		
		Composite swtComposite = new Composite(swtShell, SWT.BORDER|SWT.EMBEDDED);
		
		swtShell.setLayout(new FillLayout());
	
		
		// create AWT embedded components into the SWT shell
		Frame awtFrame = SWT_AWT.new_Frame(swtComposite);
		Panel awtPanel = new Panel(new BorderLayout());
		awtFrame.add(awtPanel);
		

		// create the popup menu to display
		swtPopupMenu = new Menu(swtComposite);
		
		MenuItem item1 = new MenuItem (swtPopupMenu, SWT.PUSH);
    	item1.setText ("useless item for test");
    	item1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("The useless popup menu was clicked !");
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		swtPopupMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuShown(MenuEvent arg0) {
				
			}
			
			@Override
			public void menuHidden(MenuEvent arg0) {
				
				System.out.println("the SWT menu was hidden (by itself)");
			}
		});
	    

		// management of events from awt to swt
		
		JPanel jPanel = new JPanel(new BorderLayout());
		awtPanel.add(jPanel);
		jPanel.addMouseListener(new MouseAdapter() {	// maps AWT mouse events to the display of the popup menu
			
			public void mousePressed(final MouseEvent e) {
				System.out.println("AWT click detected");
				swtDisplay.asyncExec(new Runnable() {
					public void run() {
						System.out.println("SWT calling menu");
						swtIndirectShowMenu(e.getXOnScreen(), e.getYOnScreen());					
					}
				});				
			}
			
		});

		
		// loop for SWT events
		swtShell.setBounds(10, 10, 300, 300);
		swtShell.open();
		while (!swtShell.isDisposed()) {
			if (!swtDisplay.readAndDispatch()) 
				swtDisplay.sleep();
		}
		
		swtDisplay.dispose();
		
	}
	
	public static void main(String [] args) {

		PopupOverAwt test = new PopupOverAwt();
		
		test.display();
		

		

	}
	
}
