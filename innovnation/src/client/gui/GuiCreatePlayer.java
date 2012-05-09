package client.gui;

import java.rmi.RemoteException;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
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
import org.jfree.experimental.swt.SWTUtils;


import client.IClientCore;
import data.Avatars;

/**
 * Displays the window which enables the selection of a game.
 * s
 * 
 * @author Samuel Thiriot
 *
 */
public class GuiCreatePlayer {
	
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger("client.gui.createPlayer");
	
	private Shell shell = null;
	private Composite compositeHost = null;
	
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Display display = null;
	

	private final static int LOOK_MIN_WIDTH = 300;
	private final static int LOOK_MIN_HEIGHT = 400;

	private final static int AVATAR_MAX_X = 60;
	private final static int AVATAR_MAX_Y = 60;
	
	private final static String TXT_NAME = "Mon nom: ";
	
	private Text fieldPlayerName = null;
	
	private final static String TXT_AVATAR = "Mon avatar: ";
	
	private Table fieldAvailableAvatars = null;
	private final static String TXT_BUTTON_CREATE_PLAYER = "Créer et rejoindre";
	private Button buttonConnectGame = null;
	
	private final static String TXT_BUTTON_JOIN_PLAYER = "Reutiliser un joueur ou joindre une equipe";
	private Button buttonReuseGame = null;

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
	
	
	private String fileAvatar, name = null;
	
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiCreatePlayer(IClientCore clientCore, Composite compositeHost) {
		
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
	public GuiCreatePlayer(IClientCore clientCore, Display display) {
		
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
		
		buttonConnectGame.setEnabled( !fieldPlayerName.getText().trim().isEmpty() && fieldAvailableAvatars.getSelectionCount() > 0);
		
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
			shell.setText ("Joueur");
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
		fieldPlayerName = new Text(compositeHost, SWT.BORDER);
		fieldPlayerName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));

		if (GuiTestMain.TEST_MODE) {
			Integer size = 0;
			try {
				size = clientCore.getGame().getAllPlayers().size();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			fieldPlayerName.setText("playerName"+size);
		}
		fieldPlayerName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				
				updateButtonsStates();
			}
		});
		
		
		// idea's desc
		// the label
		Label labelAvatar = new Label(compositeHost, SWT.READ_ONLY);
		labelAvatar.setText(TXT_AVATAR);
		labelAvatar.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
		GridData gdlabel = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		labelAvatar.setLayoutData(gdlabel);
		
		// the field
		fieldAvailableAvatars = new Table(compositeHost, SWT.BORDER);
		GridData gdGames = new GridData(
				GridData.FILL_HORIZONTAL | 
				GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		fieldAvailableAvatars.setLayoutData(gdGames);
		fieldAvailableAvatars.setHeaderVisible (true);
		fieldAvailableAvatars.setLinesVisible (true);

		{
			TableColumn column = new TableColumn (fieldAvailableAvatars, SWT.NONE);
			column.setText("image");
			column = new TableColumn (fieldAvailableAvatars, SWT.NONE);
			column.setText("nom");
			
		}
		packTableColumns();

		fieldAvailableAvatars.addSelectionListener(new SelectionListener() {
			
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
		GC gc = new GC (labelAvatar);
		FontMetrics fm = gc.getFontMetrics ();
		int height = fm.getHeight ();
		gc.dispose ();
		gdGames.minimumHeight = height*LOOK_NB_LINES_GAMES;
		
			
		
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
			buttonConnectGame.setText(TXT_BUTTON_CREATE_PLAYER);
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
			buttonReuseGame = new Button(compositeBottom, SWT.PUSH);
			buttonReuseGame.setText(TXT_BUTTON_JOIN_PLAYER);
			buttonReuseGame.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickOnReuseGame();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickOnReuseGame();

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
		
		populateAvatars();
	
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

		
		TableItem item = fieldAvailableAvatars.getSelection()[0]; // only 1 possible 

		fileAvatar = (String)item.getData();
		name = fieldPlayerName.getText().trim();
		
		close();
	}
	
	private void clickOnReuseGame() {

		
		TableItem item = fieldAvailableAvatars.getSelection()[0]; // only 1 possible 

		fileAvatar = (String)item.getData();
		name = fieldPlayerName.getText().trim();
		
		try {
			if (this.clientCore.getGame().testExistingPlayer(name))
			{
				close();			
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("unused")
	private void displayError(String title, String msg) {
		MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		mb.setText(title);
		mb.setMessage(msg);
		mb.open();
	}
	
	private void packTableColumns() {
		for (TableColumn col : fieldAvailableAvatars.getColumns()) {
			col.pack();
		}
	}
	
	
	private Image resize(Image image, int maxwidth, int maxheight) {
		
		double scalex = (double)image.getBounds().width/(double)maxwidth;
		double scaley = (double)image.getBounds().height/(double)maxheight;
		
		int width, height;
		
		if (scalex > scaley) {
			width = (int)Math.round((double)image.getBounds().width/scalex);
			height = (int)Math.round((double)image.getBounds().height/scalex);
		} else {
			width = (int)Math.round((double)image.getBounds().width/scaley);
			height = (int)Math.round((double)image.getBounds().height/scaley);
		}
		
		//System.out.println("Yoooooo "+width+" "+height);
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(
				image, 
				0, 
				0, 
				image.getBounds().width, 
				image.getBounds().height,
				0, 
				0, 
				width, 
				height
				);
		gc.dispose();
		//image.dispose(); // don't forget about me!
		return scaled;
	}
	
	private void populateAvatars() {
		
		// clear list
		fieldAvailableAvatars.removeAll();
		
		
		@SuppressWarnings("unused")
		String serverURL = fieldPlayerName.getText().trim();
		
		try {
			
			for (String currentAvatar : Avatars.getAvailableAvatars()) {
				TableItem item = new TableItem(fieldAvailableAvatars, SWT.NONE);
				
//				Image image = new Image(display,  Avatars.getPathForAvatar(currentAvatar));
				logger.debug("avatar ressource : "+"/"+currentAvatar);		
				ImageIcon im=new ImageIcon(getClass().getResource("/"+currentAvatar));
				logger.debug("avatar ressourceb : "+"/"+currentAvatar);		
				Image image=new Image(display,SWTUtils.convertAWTImageToSWT(im.getImage()));
				logger.debug("avatar ressourcec : "+"/"+currentAvatar);		
				
				Image rescaled = resize(image, AVATAR_MAX_X, AVATAR_MAX_Y);
				
				item.setImage(0, rescaled);
				item.setText(1, currentAvatar);
				item.setData(currentAvatar);
			}
						
			packTableColumns();
			
			if (fieldAvailableAvatars.getItemCount() > 0) {
				fieldAvailableAvatars.setSelection(0);
				fieldAvailableAvatars.forceFocus();
				updateButtonsStates();
			}
			
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public String getAvatarFile ()  {
		return fileAvatar;
	}
	
	public String getName()  {
		return name;
	}
	
}
