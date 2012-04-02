package client.gui;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.Text;

/**
 * An easy access to the creation of both a server and a game. 
 * Creates the server AND the game in one unique window. 
 * May be a good idea - or not ?! 
 * 
 * @author Samuel Thiriot
 *
 */
public class GuiCreateGame {
	
	private Logger logger = Logger.getLogger("client.gui.createGame");
	
	private Shell shell = null;
	private Composite compositeHost = null;
	
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Display display = null;
	

	private final static int LOOK_MIN_WIDTH = 300;
	private final static int LOOK_MIN_HEIGHT = 100;

	private final static String TXT_NAME = "Nom de la partie: ";
	private Text fieldGameName = null;
	
	private final static String TXT_THEME = "Thème: ";
	private Text fieldTheme = null;
	
	private final static String TXT_CREATE_GAME = "Créer";
	private Button buttonCreateGame = null;
	
	private final static String TXT_BUTTON_CANCEL = "Annuler";

	private Button buttonCancel = null;
	
		
	private Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;
	
	/**
	 * The parameter style passed as a paraemter for all the composites (enables to add border, as example)
	 */
	private final int LOOK_COMPOSITE_STYLE_SUBSPACES = SWT.NONE; // SWT.BORDER;
	
			
	//private Map<Integer,TableItem> availableItemId2tableItem = new HashMap<Integer, TableItem>();	

	private GuiEmbbedType embbedType;
	
	
	
	private String resultGameName = null;
	private String resultGameTheme = null;
	
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiCreateGame(Composite compositeHost) {
		
		embbedType = GuiEmbbedType.EMBEDDED;
		this.compositeHost = compositeHost;
		this.display = compositeHost.getDisplay();
		initColors();
		
	}
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiCreateGame(Display display) {
		
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
		
		buttonCreateGame.setEnabled(
				!fieldGameName.getText().trim().isEmpty()
				&&
				!fieldTheme.getText().trim().isEmpty()
				);
		
		if (buttonCreateGame.isEnabled()) {
			shell.setDefaultButton(buttonCreateGame);
		}
	}
	
	
	
	/**
	 * Actually creates the GUI into the host composite. 
	 * 
	 */
	public void init() {

		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell = new Shell (display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
			shell.setText ("Créer une partie");
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
		
	
		// idea's name
		// the label
		Label labelName = new Label(compositeHost, SWT.READ_ONLY);
		labelName.setText(TXT_NAME);
		labelName.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
		labelName.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		
		// the field
		fieldGameName = new Text(compositeHost, SWT.BORDER);
		fieldGameName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
		
		if (GuiTestMain.TEST_MODE) 
			fieldGameName.setText("a_game");
		
		fieldGameName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				
				updateButtonsStates();
			}
		});
		
			
	
		// idea's desc
		// the label
		Label labelTheme = new Label(compositeHost, SWT.READ_ONLY);
		labelTheme.setText(TXT_THEME);
		labelTheme.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
		GridData gdlabel = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		labelTheme.setLayoutData(gdlabel);
		
		// the field
		fieldTheme = new Text(compositeHost, SWT.BORDER);
		fieldTheme.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
		
		if (GuiTestMain.TEST_MODE) 
			fieldTheme.setText("nice theme");
		
		fieldTheme.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				
				updateButtonsStates();
			}
		});
		
		
			
			
		
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
			
			buttonCreateGame = new Button(compositeBottom, SWT.PUSH);
			buttonCreateGame.setText(TXT_CREATE_GAME);
			buttonCreateGame.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickOnCreateGame();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickOnCreateGame();
				}
			});
			
		}
		updateButtonsStates();

		
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
	
	private void clickOnCreateGame() {

		resultGameName = fieldGameName.getText().trim();
		resultGameTheme = fieldTheme.getText().trim();
			
		close();
	}
	
	
	private void displayError(String title, String msg) {
		MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		mb.setText(title);
		mb.setMessage(msg);
		mb.open();
	}
	

	public String getGameName()  {
		return resultGameName;
	}

	public String getGameTheme()  {
		return resultGameTheme;
	}
}
