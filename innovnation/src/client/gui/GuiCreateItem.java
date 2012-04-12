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

import functions.IGame;

/**
 * An easy access to the creation of both a server and a game. 
 * Creates the server AND the game in one unique window. 
 * May be a good idea - or not ?! 
 * 
 * @author Samuel Thiriot
 *
 */
public class GuiCreateItem {
	
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger("client.gui.createGame");
	
	private Shell shell = null;
	private Composite compositeHost = null;
	
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Display display = null;
	

	private final static int LOOK_MIN_WIDTH = 300;
	private final static int LOOK_MIN_HEIGHT = 100;

	private final static String TXT_NAME = "Nom court: ";
	private Text fieldItemName = null;
	
	/*
	private final static String TXT_THEME = "Description: ";
	private Text fieldDesc = null;
	*/
	
	private final static String TXT_CREATE_ITEM = "Créer";
	private Button buttonCreateItem = null;
	
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
	
	
	
	private String resultItemName = null;
	private String resultItemDEsc = null;
	
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiCreateItem(Composite compositeHost) {
		
		embbedType = GuiEmbbedType.EMBEDDED;
		this.compositeHost = compositeHost;
		this.display = compositeHost.getDisplay();
		initColors();
		
	}
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiCreateItem(Display display) {
		
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
		
		buttonCreateItem.setEnabled(
				!fieldItemName.getText().trim().isEmpty()
				);
		
		if (buttonCreateItem.isEnabled()) {
			shell.setDefaultButton(buttonCreateItem);
		}
	}
	
	
	
	/**
	 * Actually creates the GUI into the host composite. 
	 * 
	 */
	public void init() {

		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell = new Shell (display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
			shell.setText ("Ajouter un item");
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
		fieldItemName = new Text(compositeHost, SWT.BORDER);
		fieldItemName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
		
		fieldItemName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				
				updateButtonsStates();
			}
		});
		
	
				
	
		// idea's desc
		// the label
		/*
		Label labelDesc = new Label(compositeHost, SWT.READ_ONLY);
		labelDesc.setText(TXT_THEME);
		labelDesc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
		GridData gdlabel = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		labelDesc.setLayoutData(gdlabel);
		
		// the field
		fieldDesc = new Text(compositeHost, SWT.BORDER);
		fieldDesc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
		
		
		fieldDesc.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				
				updateButtonsStates();
			}
		});
		
		*/
		
			
		
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
			
			buttonCreateItem = new Button(compositeBottom, SWT.PUSH);
			buttonCreateItem.setText(TXT_CREATE_ITEM);
			buttonCreateItem.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickOnCreateItem();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickOnCreateItem();
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
	
	private void clickOnCreateItem() {

		
		resultItemName = fieldItemName.getText().trim();
		//resultItemDEsc = fieldDesc.getText().trim();
			
		close();
	}
	
	
	@SuppressWarnings("unused")
	private void displayError(String title, String msg) {
		MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		mb.setText(title);
		mb.setMessage(msg);
		mb.open();
	}
	

	public String getItemName()  {
		return resultItemName;
	}

	public String getItemDesc()  {
		return resultItemDEsc;
	}
	
	/**
	 * Returns the id of the created item, or -1 in case of cancelling or error.
	 * @param display
	 * @param game
	 * @param playerId
	 * @return
	 */
	public static int manageCreateItem(Display display, IGame game, Integer playerId) {

		if (display.isDisposed())
			return -1;

		GuiCreateItem guiCreateItem = new GuiCreateItem(display);
		
		guiCreateItem.init();
		guiCreateItem.run();
		
		String itemName = guiCreateItem.getItemName();
		String itemDesc = guiCreateItem.getItemDesc();
		
		if (itemName == null)
			return -1;	// user cancelled
		
		try {
			return game.addItem(playerId, itemName, itemDesc);
		} catch (Exception e) {
			MessageBox mb = new MessageBox(display.getActiveShell(), SWT.ICON_ERROR | SWT.OK);
			
			mb.setText("Error");
			mb.setMessage("Error while adding item, sorry\n"+e.getMessage());
			
			mb.open();
			
		}
		
		return -1;
	}
	
}
