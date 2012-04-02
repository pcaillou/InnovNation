package client.gui;

import java.net.UnknownHostException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import util.RMIUtils;
import client.IClientCore;
import functions.GameServer;
import functions.IGameDescription;

/**
 * Displays the window which enables the selection of a game.
 * s
 * 
 * @author Samuel Thiriot
 *
 */
public class GuiConnect {
	
	private Logger logger = Logger.getLogger("client.gui.connect");
	
	private Shell shell = null;
	private Composite compositeHost = null;
	
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Display display = null;
	

	private final static int LOOK_MIN_WIDTH = 300;
	private final static int LOOK_MIN_HEIGHT = 300;

	private final static String TXT_HOST = "Serveur: ";
	
	private Text fieldServerHost = null;
	private final static String TXT_BUTTON_CONNECT_HOST = "Lister";
	private Button buttonConnectHost = null;
	
	private final static String TXT_GAMES = "Parties: ";
	
	private Table fieldAvailableGames = null;
	private final static String TXT_BUTTON_CONNECT_GAME = "Connecter";
	private Button buttonConnectGame = null;
	
	private final static String TXT_BUTTON_CANCEL = "Annuler";

	private Button buttonCancel = null;
	
		
	private Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;
	
	/**
	 * The parameter style passed as a paraemter for all the composites (enables to add border, as example)
	 */
	private final int LOOK_COMPOSITE_STYLE_SUBSPACES = SWT.NONE; // SWT.BORDER;
	
	private final int LOOK_NB_LINES_GAMES = 8;
			
	//private Map<Integer,TableItem> availableItemId2tableItem = new HashMap<Integer, TableItem>();	

	private GuiEmbbedType embbedType;
	
	private final IClientCore clientCore;
	
	
	private String gameToFetch = null;
	
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiConnect(IClientCore clientCore, Composite compositeHost) {
		
		this.clientCore = clientCore;
		embbedType = GuiEmbbedType.EMBEDDED;
		this.compositeHost = compositeHost;
		this.display = compositeHost.getDisplay();
		initColors();
		
	}
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiConnect(IClientCore clientCore, Display display) {
		
		this.clientCore = clientCore;
		embbedType = GuiEmbbedType.STANDALONE;
		this.compositeHost = null;
		this.display = display;
		initColors();
				
		
	}
	
	private final void initColors() {
		if (display == null)
			throw new RuntimeException("unable to init colors prior to display assignment");
		
		LOOK_COLOR_BACKGROUND_MAINSPACE = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		LOOK_COLOR_BACKGROUND_SUBSPACES = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}
	
	protected void updateButtonsStates() {
		
		buttonConnectHost.setEnabled(!fieldServerHost.getText().trim().isEmpty());
		
		// TODO : check URL valid ?
		buttonConnectGame.setEnabled(fieldAvailableGames.getSelectionCount() > 0);
		
		if (buttonConnectGame.isEnabled()) {
			shell.setDefaultButton(buttonConnectGame);
		}
	}
	
	
	
	/**
	 * Actually creates the GUI into the host composite. 
	 * 
	 */
	public void init() {

		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell = new Shell (display, SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
			shell.setText ("Connection");
			shell.setSize (LOOK_MIN_WIDTH, LOOK_MIN_HEIGHT);
			shell.setLayout(new FillLayout());
	
			compositeHost = new Composite(shell, SWT.NONE);
	
		
		}

	
		// manage the layout of the host composite
		{
			GridLayout layoutHost = new GridLayout(1, true);
			layoutHost.horizontalSpacing = SWT.FILL;
			
			compositeHost.setLayout(layoutHost);
			
			compositeHost.setBackground(LOOK_COLOR_BACKGROUND_MAINSPACE);
			
			
		}
		
		
		// top top composite: name
		{
			// create the top composite for name and desc
			GridLayout layoutServer = new GridLayout(1, false);
			
			Composite compositeServer = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeServer.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeServer.setLayout(layoutServer);
			compositeServer.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
			
			// idea's name
			// the label
			Label labelHost = new Label(compositeServer, SWT.READ_ONLY);
			labelHost.setText(TXT_HOST);
			labelHost.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelHost.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			
			// the field
			fieldServerHost = new Text(compositeServer, SWT.BORDER);
			fieldServerHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
			try {
				fieldServerHost.setText("rmi://"+RMIUtils.getLocalHost().getHostAddress()+"/"+GameServer.SERVER_NAME);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			fieldServerHost.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent arg0) {
					
					if (fieldAvailableGames.getItemCount() > 0)
						fieldAvailableGames.removeAll();
					
					updateButtonsStates();
				}
			});
			
			buttonConnectHost = new Button(compositeServer, SWT.PUSH);
			buttonConnectHost.setText(TXT_BUTTON_CONNECT_HOST);
			GridData gdButton = new GridData(GridData.HORIZONTAL_ALIGN_END);
			gdButton.horizontalSpan = 2;
			buttonConnectHost.setLayoutData(gdButton);
				
			buttonConnectHost.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					clickConnectHost(false);
				}
				
			
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					clickConnectHost(false);
				}
			});
				
					
		}
		
		// top composite: desc
		{
			// create the top composite for name and desc
			GridLayout layoutGames = new GridLayout(1, false);
			
			Composite compositeGames = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeGames.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeGames.setLayout(layoutGames);
			compositeGames.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
		
			// idea's desc
			// the label
			Label labelGames = new Label(compositeGames, SWT.READ_ONLY);
			labelGames.setText(TXT_GAMES);
			labelGames.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			GridData gdlabel = new GridData(GridData.VERTICAL_ALIGN_CENTER);
			labelGames.setLayoutData(gdlabel);
			
			// the field
			fieldAvailableGames = new Table(compositeGames, SWT.BORDER);
			GridData gdGames = new GridData(
					GridData.FILL_HORIZONTAL | 
					GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
			fieldAvailableGames.setLayoutData(gdGames);
			fieldAvailableGames.setHeaderVisible (true);
			fieldAvailableGames.setLinesVisible (true);

			{
				TableColumn column = new TableColumn (fieldAvailableGames, SWT.NONE);
				column.setText("jeu");
				column = new TableColumn (fieldAvailableGames, SWT.NONE);
				column.setText("thème");
				
			}
			packTableColumns();

			fieldAvailableGames.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					updateButtonsStates();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					updateButtonsStates();
				}
			});

			// (define its height !)
			GC gc = new GC (labelGames);
			FontMetrics fm = gc.getFontMetrics ();
			int height = fm.getHeight ();
			gc.dispose ();
			gdGames.minimumHeight = height*LOOK_NB_LINES_GAMES;
			
				
		}
		
		
		// bottom composite
		{

			RowLayout layoutBottom = new RowLayout(SWT.HORIZONTAL);
						
			Composite compositeBottom = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeBottom.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeBottom.setLayout(layoutBottom);
			compositeBottom.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
						
			buttonCancel = new Button(compositeBottom, SWT.PUSH);
			buttonCancel.setText(TXT_BUTTON_CANCEL);
			buttonCancel.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickCancel();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickCancel();
				}
			});
			
			buttonConnectGame = new Button(compositeBottom, SWT.PUSH);
			buttonConnectGame.setText(TXT_BUTTON_CONNECT_GAME);
			buttonConnectGame.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickOnConnectGame();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickOnConnectGame();
				}
			});
			
		}
		updateButtonsStates();

		clickConnectHost(true);

		
		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell.pack();
			System.out.println(shell.getSize());
			shell.setMinimumSize(
					Math.max(shell.getSize().x, LOOK_MIN_WIDTH),
					Math.max(shell.getSize().y , LOOK_MIN_HEIGHT)
					);
			//shell.setSize(shell.getMinimumSize());

			shell.open ();
			
		}
	}
	
	/**
	 * Only makes sense if opened in standalone mode
	 */
	public void run() {

		if (embbedType == GuiEmbbedType.STANDALONE) {

			while (!shell.isDisposed ()) {
				if (!display.readAndDispatch ()) 
					display.sleep ();
			}
			
		}
		
	}
	
	private void close() {
		
		if (shell != null) {
			shell.close();
			shell.dispose();
		}
		
	}
	
	
	private void clickCancel() {
	
		close();
	}
	
	private void clickOnConnectGame() {

		TableItem item = fieldAvailableGames.getSelection()[0]; // only 1 possible 

		gameToFetch = (String)item.getData();
	
		close();
	}
	
	
	private void displayError(String title, String msg) {
		MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		mb.setText(title);
		mb.setMessage(msg);
		mb.open();
		
	
	}
	
	private void packTableColumns() {
		for (TableColumn col : fieldAvailableGames.getColumns()) {
			col.pack();
		}
	}
	
	private void clickConnectHost(boolean silent) {
		
		// clear list
		fieldAvailableGames.removeAll();
		
		
		String serverURL = fieldServerHost.getText().trim();
		
		try {
			
			Collection<IGameDescription> games = clientCore.fetchServer(serverURL);
			
			for (IGameDescription currentGame : games) {

				TableItem item = new TableItem(fieldAvailableGames, SWT.NONE);
				item.setText(0, currentGame.getName());
				item.setText(1, currentGame.getTheme());
				item.setData(currentGame.getBindName());

			}
			
			packTableColumns();
			
			if (fieldAvailableGames.getItemCount() > 0) {
				fieldAvailableGames.setSelection(0);
				fieldAvailableGames.forceFocus();
				updateButtonsStates();
			}
			
		} catch (RuntimeException e) {
		//	e.printStackTrace();
			if (!silent) 
				displayError("error", e.getMessage());
			e.printStackTrace();
		}
	}

	public String getGameToFetch ()  {
		return gameToFetch;
	}
	
}
