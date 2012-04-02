package client.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import data.IIdea;
import data.IItem;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;
import functions.IGame;

/**
 * Pane (later put into a window OR a panel) that enables the creation of an idea from scratch.
 * 
 * TODO listen for whiteboard events, for adding items if relevant (replace calls to the game itself)
 * TODO dispose ???
 * TODO quand on recadre, limiter la taille min.
 * 
 * TODO comment on ajoute les parents ?
 * 
 * TODO id du joueur ?
 * 
 * @author Samuel Thiriot
 *
 */
public class GuiCreateIdea implements IEventListener {
	
	private Logger logger = Logger.getLogger("client.gui.createidea");
	
	private Shell shell = null;
	private Composite compositeHost = null;
	
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Display display = null;
	

	private Text fieldIdeaName = null;
	private Text fieldIdeaDesc = null;
	
	private Table fieldParentIdeas = null;

	private Table fieldAllAvailableItems = null;
	private Table fieldSelectedItems = null;
	
	private Text fieldNewItem = null;

	private Button buttonAdd = null;
	private Button buttonRemove = null;
	
	private Button buttonCreateItem = null;

	private Button buttonValidate = null;
	private Button buttonCancel = null;
	

	private final static String TXT_IDEA_NAME = "Nom : ";
	private final static String TXT_TOOLTIP_IDEA_NAME = "Un nom clair qui distinguera votre idée";
	private final static String TXT_IDEA_DESC = "Description :";
	private final static String TXT_TOOLTIP_IDEA_DESC = "Une description concise de votre idée";
	
	private final static String TXT_PARENT_IDEAS = "Idées parentes:";
	
	private final static String TXT_ITEMS_AVAILABLE = "Items réutilisables:";
	private final static String TXT_ITEMS_SELECTED = "Items de votre idée:";
	private final static String TXT_TOOLTIP_AVAILABLE = "Vous pouvez réutiliser tous les items déjà proposés";
	private final static String TXT_TOOLTIP_SELECTED = "Ces items seront intégrés à votre idée";

	private final static String TXT_BUTTON_VALIDATE = "Créer l'idée";
	private final static String TXT_BUTTON_CANCEL = "Annuler";

	private final static String TXT_TOOLTIP_NEWITEM_NAME = "Un nom clair qui distinguera votre idée";
	private final static String TXT_TOOLTIP_NEWITEM_DESC = "Description de l'Item";

	private final static String TXT_BUTTON_CREATENEWITEM = "Créer un item";

	
	private Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;
	
	/**
	 * The parameter style passed as a paraemter for all the composites (enables to add border, as example)
	 */
	private final int LOOK_COMPOSITE_STYLE_SUBSPACES = SWT.NONE; // SWT.BORDER;
	
	private final int LOOK_NB_LINES_DESC = 3;
	private final int LOOK_NB_LINES_ITEMDESC = 2;
	private final int LOOK_NB_LINES_ITEMS_AVAILABLE = 10;
	private final int LOOK_NB_LINES_ITEMS_SELECTED = 8;

		
	private Map<Integer,TableItem> availableItemId2tableItem = new HashMap<Integer, TableItem>();
	private Map<Integer,TableItem> selectedItemId2tableItem = new HashMap<Integer, TableItem>();

	private Map<Integer,TableItem> ideaId2tableItem = new HashMap<Integer, TableItem>();


	private GuiEmbbedType embbedType;
	
	private IGame localGame;
	
	private Collection<Integer> resultParentIds = null;
	private String resultName = null;
	private String resultDesc = null;
	private Collection<Integer> resultItemIds = null;

	private Integer playerId;
	
	/**
	 * Stores the ids of the items created via this GUI. 
	 * This way we can detect that a novel item should be automatically added to the idea.
	 */
	private Collection<Integer> createdItems = new ArrayList<Integer>();
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiCreateIdea(Integer playerId, Composite compositeHost, IGame game) {
		
		this.playerId = playerId;
		embbedType = GuiEmbbedType.EMBEDDED;
		this.compositeHost = compositeHost;
		this.display = compositeHost.getDisplay();
		initColors();
		
		this.localGame = game;
		
		try {
			localGame.addListener(this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiCreateIdea(Integer playerId, Display display, IGame game) {
		
		this.playerId = playerId;
		embbedType = GuiEmbbedType.STANDALONE;
		this.compositeHost = null;
		this.display = display;
		initColors();
		
		this.localGame = game;
		

		try {
			localGame.addListener(this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private final void initColors() {
		if (display == null)
			throw new RuntimeException("unable to init colors prior to display assignment");
		
		LOOK_COLOR_BACKGROUND_MAINSPACE = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		LOOK_COLOR_BACKGROUND_SUBSPACES = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}
	
	protected void updateButtonsStates() {
		
		buttonAdd.setEnabled(fieldAllAvailableItems.getSelectionCount() > 0);
		buttonRemove.setEnabled(fieldSelectedItems.getSelectionCount() > 0);

		buttonValidate.setEnabled(
				(fieldIdeaName.getText().trim().length() > 0) 
				//&&
			//	(fieldIdeaDesc.getText().trim().length() > 0) &&
			//	(fieldSelectedItems.getItemCount() > 0)
				);
		
		
	}
	
	
	protected void clickOnAdd() {
		
		if (compositeHost.isDisposed())
			return;
		
		if (fieldAllAvailableItems.getSelectionCount() == 0)
			return;
		
		
		// freeze display
		fieldAllAvailableItems.setEnabled(false);
		fieldAllAvailableItems.setRedraw(false);
		fieldSelectedItems.setEnabled(false);
		fieldSelectedItems.setRedraw(false);
		buttonAdd.setEnabled(false);
		
		// add selection to the other table
		TableItem lastItemAdded = null;
		for (TableItem itemToAdd : fieldAllAvailableItems.getSelection()) {
			
			Integer itemId = (Integer)itemToAdd.getData();
			
			// add to the other table
			TableItem novelItem = new TableItem(fieldSelectedItems, 0);
			novelItem.setText(getItems().get(itemId));
			novelItem.setData(itemId);
			selectedItemId2tableItem.put(itemId, novelItem);
		
			lastItemAdded = novelItem;
		}
		
		// show the last item added
		if (lastItemAdded != null)
			fieldSelectedItems.showItem(lastItemAdded);
		
		// remove from the previous table
		fieldAllAvailableItems.remove(fieldAllAvailableItems.getSelectionIndices());
		
		// draw
		fieldAllAvailableItems.setEnabled(true);
		fieldAllAvailableItems.setRedraw(true);
		fieldSelectedItems.setEnabled(true);
		fieldSelectedItems.setRedraw(true);
		
		updateButtonsStates(); // selection may have changed !
			
	}
	
	protected void clickOnRemove() {
		
		if (compositeHost.isDisposed())
			return;
		
		if (fieldSelectedItems.getSelectionCount() == 0)
			return;
		
		
		// freeze display
		fieldAllAvailableItems.setEnabled(false);
		fieldAllAvailableItems.setRedraw(false);
		fieldSelectedItems.setEnabled(false);
		fieldSelectedItems.setRedraw(false);
		buttonAdd.setEnabled(false);
		
		// add selection to the other table
		TableItem lastItemAdded = null;
		for (TableItem itemToAdd : fieldSelectedItems.getSelection()) {
			
			Integer itemId = (Integer)itemToAdd.getData();
			
			// add to the other table
			TableItem novelItem = new TableItem(fieldAllAvailableItems, 0);
			novelItem.setText(getItems().get(itemId));
			novelItem.setData(itemId);
			availableItemId2tableItem.put(itemId, novelItem);
		
			lastItemAdded = novelItem;
		}
		
		// show the last item added
		if (lastItemAdded != null)
			fieldAllAvailableItems.showItem(lastItemAdded);
		
		// remove from the previous table
		fieldSelectedItems.remove(fieldSelectedItems.getSelectionIndices());
		
		// draw
		fieldAllAvailableItems.setEnabled(true);
		fieldAllAvailableItems.setRedraw(true);
		fieldSelectedItems.setEnabled(true);
		fieldSelectedItems.setRedraw(true);
		
		updateButtonsStates(); // selection may have changed !
			
	}
	
	/**
	 * Actually creates the GUI into the host composite. 
	 * 
	 */
	public void init(Collection<IIdea> selectedIdeas, Collection<IItem> selectedItems) {

		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell = new Shell (display, SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
			shell.setText ("Test creation idee");
			shell.setSize (400, 300);
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
		
		// a listener that is of use for so many components....
		SelectionListener updateStatesSelectionListener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateButtonsStates();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				updateButtonsStates();
			}
		};
		
		// a listener that is of use for so many components....
		ModifyListener updateStatesModifyListener = new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateButtonsStates();
				
			}
		};
		
		// top top composite: name
		{
			// create the top composite for name and desc
			GridLayout layoutName = new GridLayout(2, false);
			
			Composite compositeName = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeName.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeName.setLayout(layoutName);
			compositeName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
			
			// idea's name
			// the label
			Label labelIdeaName = new Label(compositeName, SWT.READ_ONLY);
			labelIdeaName.setText(TXT_IDEA_NAME);
			labelIdeaName.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelIdeaName.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelIdeaName.setToolTipText(TXT_TOOLTIP_IDEA_NAME);

			// the field
			fieldIdeaName = new Text(compositeName, SWT.BORDER);
			fieldIdeaName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
			fieldIdeaName.setToolTipText(TXT_TOOLTIP_IDEA_NAME);
			

			if (GuiTestMain.TEST_MODE) {
				Integer size = 0;
				try {
					size = localGame.getAllIdeas().size();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				fieldIdeaName.setText("idea "+size);
			}
			
			fieldIdeaName.addModifyListener(updateStatesModifyListener);
		
		
					
		}
		
		
		
		// top composite: desc
		{
			// create the top composite for name and desc
			GridLayout layoutDesc = new GridLayout(1, false);
			
			Composite compositeDesc = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeDesc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeDesc.setLayout(layoutDesc);
			compositeDesc.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		
			// idea's desc
			// the label
			Label labelIdeaDesc = new Label(compositeDesc, SWT.READ_ONLY);
			labelIdeaDesc.setText(TXT_IDEA_DESC);
			labelIdeaDesc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			GridData gdlabel = new GridData(GridData.VERTICAL_ALIGN_CENTER);
			labelIdeaDesc.setLayoutData(gdlabel);
			labelIdeaDesc.setToolTipText(TXT_TOOLTIP_IDEA_DESC);

			// the field
			fieldIdeaDesc = new Text(compositeDesc, SWT.BORDER | SWT.MULTI);
			GridData gdDesc = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
			fieldIdeaDesc.setLayoutData(gdDesc);
			fieldIdeaDesc.setToolTipText(TXT_TOOLTIP_IDEA_DESC);

			fieldIdeaDesc.addModifyListener(updateStatesModifyListener);

			// (define its height !)
			GC gc = new GC (fieldIdeaDesc);
			FontMetrics fm = gc.getFontMetrics ();
			int height = fm.getHeight ();
			gc.dispose ();
			gdDesc.minimumHeight = height*LOOK_NB_LINES_DESC;
			
				
		}
		
		// parent ideas
		{
			Label labelIdeaDesc = new Label(compositeHost, SWT.READ_ONLY);
			labelIdeaDesc.setText(TXT_PARENT_IDEAS);
			labelIdeaDesc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			GridData gdlabel = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			labelIdeaDesc.setLayoutData(gdlabel);
		
			fieldParentIdeas = new Table(compositeHost, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL);
			
			GridData gdParents = new GridData(GridData.FILL_BOTH);
		
			fieldParentIdeas.addSelectionListener(updateStatesSelectionListener);

			GC gc = new GC (fieldParentIdeas);
			FontMetrics fm = gc.getFontMetrics ();
			int height = fm.getHeight ();
			gc.dispose ();
			gdParents.minimumHeight = height*LOOK_NB_LINES_ITEMS_SELECTED;

			fieldParentIdeas.setLayoutData(gdParents);
			
			new TableColumn(fieldParentIdeas, SWT.LEFT);
			
			
		}
		
		// middle composite: the items
		{
			GridLayout layoutItems = new GridLayout(3, false);
			
			Composite compositeItems = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeItems.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeItems.setLayout(layoutItems);
			compositeItems.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
			
			// labels (kind of "headers")
			{
				{
					// label 1
					Label labelAvailableItems = new Label(compositeItems, SWT.READ_ONLY);
					labelAvailableItems.setText(TXT_ITEMS_AVAILABLE);
					labelAvailableItems.setToolTipText(TXT_TOOLTIP_AVAILABLE);
					labelAvailableItems.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					
					GridData gdlabel1 = new GridData(GridData.VERTICAL_ALIGN_CENTER);
					gdlabel1.horizontalSpan = 2;
					labelAvailableItems.setLayoutData(gdlabel1);
				}
				
				{
					// label 2
					Label labelSelectedItems = new Label(compositeItems, SWT.READ_ONLY);
					labelSelectedItems.setText(TXT_ITEMS_SELECTED);
					labelSelectedItems.setToolTipText(TXT_TOOLTIP_SELECTED);
					labelSelectedItems.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					
					GridData gdlabel2 = new GridData(GridData.VERTICAL_ALIGN_CENTER);
					labelSelectedItems.setLayoutData(gdlabel2);
				}
				
			}
			
			// the list of available items
			{
				fieldAllAvailableItems = new Table(compositeItems, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
				
				
				// ... its layout
				GridData gdAvailableItems = new GridData(
															GridData.FILL_HORIZONTAL | 
															GridData.VERTICAL_ALIGN_BEGINNING | 
															GridData.GRAB_HORIZONTAL |
															GridData.FILL_VERTICAL |
															GridData.GRAB_VERTICAL
															);
				// (define its height !)
				GC gc = new GC (fieldIdeaDesc);
				FontMetrics fm = gc.getFontMetrics ();
				int height = fm.getHeight ();
				gc.dispose ();
				gdAvailableItems.minimumHeight = height*LOOK_NB_LINES_ITEMS_AVAILABLE;
				fieldAllAvailableItems.setLayoutData(gdAvailableItems);
				
				
				fieldAllAvailableItems.addSelectionListener(updateStatesSelectionListener);
				
			}
			
			// buttons
			{
				Composite compositeButtons = new Composite(compositeItems, SWT.NONE);
				compositeButtons.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				compositeButtons.setLayout(new RowLayout(SWT.VERTICAL));
				
				GridData gdButtons = new GridData(GridData.VERTICAL_ALIGN_CENTER);	
				compositeButtons.setLayoutData(gdButtons);
				
				buttonAdd = new Button(compositeButtons, SWT.ARROW | SWT.RIGHT);
				buttonAdd.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						clickOnAdd();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						clickOnAdd();
					}
				});
				
				buttonRemove = new Button(compositeButtons, SWT.ARROW | SWT.LEFT);
				buttonRemove.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						clickOnRemove();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						clickOnRemove();
					}
				});
			}
			
			// the list of selected items
			{
				fieldSelectedItems = new Table(compositeItems, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
				
				
				// ... its layout
				GridData gdSelectedItems = new GridData(
											GridData.FILL_HORIZONTAL | 
											GridData.VERTICAL_ALIGN_BEGINNING | 
											GridData.GRAB_HORIZONTAL |
											GridData.GRAB_VERTICAL |
											GridData.FILL_VERTICAL
											
											);	
				
				fieldSelectedItems.addSelectionListener(updateStatesSelectionListener);

				GC gc = new GC (fieldIdeaDesc);
				FontMetrics fm = gc.getFontMetrics ();
				int height = fm.getHeight ();
				gc.dispose ();
				gdSelectedItems.minimumHeight = height*LOOK_NB_LINES_ITEMS_SELECTED;
				fieldSelectedItems.setLayoutData(gdSelectedItems);
	
				
				// TODO (define its height !) => nop, this should be automatic !
			}
			
			// the field for adding a new item
			/*{
				fieldNewItem = new Text(compositeItems, SWT.BORDER);
				
				// ... its layout
				GridData gdSelectedItems = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);	
				fieldSelectedItems.setLayoutData(gdSelectedItems);
				
			}*/
		
			// add item composite: name
			{
				
				
				buttonCreateItem = new Button(compositeItems, SWT.PUSH);
				buttonCreateItem.setText(TXT_BUTTON_CREATENEWITEM);
				GridData gdButton = new GridData(GridData.HORIZONTAL_ALIGN_END);
				gdButton.horizontalSpan = 3;
				buttonCreateItem.setLayoutData(gdButton);
					
				buttonCreateItem.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						clickCreateItem();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						clickCreateItem();
					}
				});
					
			}
			
			
	
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
			
			buttonValidate = new Button(compositeBottom, SWT.PUSH);
			buttonValidate.setText(TXT_BUTTON_VALIDATE);
			buttonValidate.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickValidate();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickValidate();
				}
			});
			
		}
		updateButtonsStates();

		populateIdeas();
		populateItems();
		

		selectItems(selectedItems);
		selectIdeas(selectedIdeas);
		
		
		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell.pack();
			shell.setMinimumSize(shell.getSize());
			
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
	
	
	
	public Collection<Integer> getResultParentIds() {
		return resultParentIds;
	}

	public String getResultName() {
		return resultName;
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public Collection<Integer> getResultItemIds() {
		return resultItemIds;
	}

	private void clickValidate() {
		
		resultParentIds = new LinkedList<Integer>();
		resultName = fieldIdeaName.getText().trim();
		resultDesc = fieldIdeaDesc.getText().trim();
		resultItemIds = new LinkedList<Integer>();
		for (TableItem item : fieldSelectedItems.getItems()) {
		
			Integer itemId = (Integer)item.getData();
			resultItemIds.add(itemId);
			
		}
		
		
		for (TableItem item : fieldParentIdeas.getItems()) {
			
			if (item.getChecked()) 
				resultParentIds.add((Integer) item.getData());
			
			
		}
		
/*
		try {
			buttonAdd.setEnabled(false);
		
			// retrieve data from ihm
			final String name = fieldIdeaName.getText().trim();
			final String desc = fieldIdeaDesc.getText().trim();
			
			final Collection<Integer> itemsIds = new LinkedList<Integer>();
			for (TableItem item : fieldSelectedItems.getItems()) {
			
				Integer itemId = (Integer)item.getData();
				itemsIds.add(itemId);
				
			}
			
			final Collection<Integer> parentIds = new LinkedList<Integer>();
			
			final int playerId = 1; // TODO !!!
			
			// TODO parents ?
			
			// check !
			// TODO !!!
			
			// TODO id of the player !
			try {
				localGame.addIdea(playerId, name, itemsIds, parentIds);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AlreadyExistsException e) {
				
				logger.warn("this idea already exists (according to the server)");
				// TODO !!!
				//MessageBox mb = new MessageBox(arg0)
				return;
			}
			
			// TODO manage errors, and display them.
			

		} finally {
			updateButtonsStates();
		}
		
		*/
		
		close();
		
	}
	
	private void clickCancel() {
	
		close();
	}
	
	private void clickCreateItem() {
	
		buttonCreateItem.setEnabled(false);
		
		int res = GuiCreateItem.manageCreateItem(display, localGame, playerId);

		if (res > -1)
			createdItems.add(res);
		
		buttonCreateItem.setEnabled(true);

		
		
	}
	
	private Map<Integer, String> getItems() {
		
		Map<Integer, String> res = new HashMap<Integer, String>();
		

		try {
			for (IItem item : localGame.getAllItems()) {
				res.put(item.getUniqueId(), item.getShortName());
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
		
	}
	
	private Map<Integer, String> getIdeas() {
		
		Map<Integer, String> res = new HashMap<Integer, String>();
		

		try {
			for (IIdea idea: localGame.getAllIdeas()) {
				res.put(idea.getUniqueId(), idea.getShortName());
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
		
	}
	
	private void populateIdeas() {
		
		if (shell.isDisposed())
			return;	// quick & safe exit
		
		Map<Integer, String>  ideas = getIdeas();
		
		// freeze display
		fieldParentIdeas.setRedraw(false);
		
		// builds the set of all the ids that were stored before update (we may have to remove something !)
		Set<Integer> storedIdeasIds = new HashSet<Integer>(ideaId2tableItem.keySet());
		
		for (Integer ideaId : ideas.keySet()) {
			
			if (storedIdeasIds.remove(ideaId)) {
				// right, this item was already displayed...
				
			} else {
				
				// look ! seems like this one is a novel one !
				TableItem novelItem = new TableItem(fieldParentIdeas, 0);
				novelItem.setText(ideas.get(ideaId));
				novelItem.setData(ideaId);
				ideaId2tableItem.put(ideaId, novelItem);
				
			}
					
		}
		
		// now, remove the items that disappeared
		for (Integer idToRemove :  storedIdeasIds) {
			
			if (ideaId2tableItem.containsKey(idToRemove)) {
				
				TableItem it = ideaId2tableItem.get(idToRemove);
				
				final int idx = fieldParentIdeas.indexOf(it);
				fieldParentIdeas.remove(idx);
				
			} else {
				logger.error("??? inconsistency detected: unable to find item "+idToRemove);
			}
		}
		
		for (TableColumn col : fieldParentIdeas.getColumns()) {
			col.setWidth(200);
		}
		
		
		// update displays
		fieldParentIdeas.setRedraw(true);

	}

	
	private void populateItems() {
		
		if (shell.isDisposed())
			return;	// quick & safe exit
		
		Map<Integer, String>  items = getItems();
		
		// freeze display
		fieldAllAvailableItems.setRedraw(false);
		fieldSelectedItems.setRedraw(false);
		
		// builds the set of all the ids that were stored before update (we may have to remove something !)
		Set<Integer> storedItemsIds = new HashSet<Integer>(availableItemId2tableItem.keySet());
		storedItemsIds.addAll(selectedItemId2tableItem.keySet());
		
		for (Integer itemId : items.keySet()) {
			
			if (storedItemsIds.remove(itemId)) {
				// right, this item was already displayed...
				
			} else {
				// look ! seems like this one is a novel one !
				
				if (createdItems.contains(itemId)) {
					// well, user just created this item; add it automatically to this idea
					TableItem novelItem = new TableItem(fieldSelectedItems, 0);
					novelItem.setText(items.get(itemId));
					novelItem.setData(itemId);
					selectedItemId2tableItem.put(itemId, novelItem);
				} else {
					// someone else created this item, it is just available, but not added to the idea
					TableItem novelItem = new TableItem(fieldAllAvailableItems, 0);
					novelItem.setText(items.get(itemId));
					novelItem.setData(itemId);
					availableItemId2tableItem.put(itemId, novelItem);
				}
				
			}
					
		}
		
		// now, remove the items that disappeared
		for (Integer idToRemove :  storedItemsIds) {
			
			if (availableItemId2tableItem.containsKey(idToRemove)) {
				
				TableItem it = availableItemId2tableItem.get(idToRemove);
				
				final int idx = fieldAllAvailableItems.indexOf(it);
				fieldAllAvailableItems.remove(idx);
				
			} else if (selectedItemId2tableItem.containsKey(idToRemove)) {
				
				TableItem it = selectedItemId2tableItem.get(idToRemove);
				
				final int idx = fieldSelectedItems.indexOf(it);
				fieldSelectedItems.remove(idx);
				
			} else {
				logger.error("??? inconsistency detected: unable to find item "+idToRemove);
			}
		}
		
		// fill 
		
		/*
		for (int i=0; i<12; i++) {
			TableItem item = new TableItem(fieldSelectedItems, 0);
			item.setText ("Item " + i);
		}
		*/
		
		// TODO fill with empty lines ? 
		// LOOK_NB_LINES_ITEMS_SELECTED
		
		// update displays
		fieldAllAvailableItems.setRedraw(true);
		fieldSelectedItems.setRedraw(true);
	}
	
	
	public void eventItemsChanged() {
		populateItems();
	}
	
	/*
	/**
	 * 
	 * @param display
	 * @param whiteBoard 	the whiteboard use to load data
	 * @param game			the game used for creating objects
	 */
	/*
	public static void openInAWindow(Display display, IGame game) {
		
		if(game==null)return;
		
		GuiCreateIdea guiCreateIdea = new GuiCreateIdea(display, game);
		
		guiCreateIdea.init();
		
		guiCreateIdea.run();

	
	}
	*/
	
	@Override
	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				populateIdeas();
			}
		});
	}

	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException {

		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				populateItems();
			}
		});
		
	}

	@Override
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {
	}

	@Override
	public void playerJoined(PlayerEvent e) throws RemoteException {		
	}

	@Override
	public void playerLeft(PlayerEvent e) throws RemoteException {
	}

	/* (non-Javadoc)
	 * @see events.IEventListener#IdeaLinkCreated(events.LinkEvent)
	 */
	@Override
	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see events.IEventListener#endOfGame()
	 */
	@Override
	public void endOfGame() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	protected void selectIdeas(Collection<IIdea> ideasToSelect) {
		
		for (IIdea idea: ideasToSelect) {
			TableItem tableItem = ideaId2tableItem.get(idea.getUniqueId());
			if (tableItem != null) {
				tableItem.setChecked(true);
			}
		}
		
	}
	

	protected void selectItems(Collection<IItem> itemsToSelect) {

		

		// freeze display
		fieldAllAvailableItems.setEnabled(false);
		fieldAllAvailableItems.setRedraw(false);
		fieldSelectedItems.setEnabled(false);
		fieldSelectedItems.setRedraw(false);
		buttonAdd.setEnabled(false);
		
		for (IItem item: itemsToSelect) {
			TableItem itemToAdd = availableItemId2tableItem.get(item.getUniqueId());
			if (itemToAdd != null) {
				
				Integer itemId = (Integer)itemToAdd.getData();
				
				// add to the other table
				TableItem novelItem = new TableItem(fieldSelectedItems, 0);
				novelItem.setText(getItems().get(itemId));
				novelItem.setData(itemId);
				selectedItemId2tableItem.put(itemId, novelItem);
			
				itemToAdd.dispose();
			}
		}
		
	
		// remove from the previous table
		
		// draw
		fieldAllAvailableItems.setEnabled(true);
		fieldAllAvailableItems.setRedraw(true);
		fieldSelectedItems.setEnabled(true);
		fieldSelectedItems.setRedraw(true);
		
		updateButtonsStates(); // selection may have changed !
			
		
	}
	
}
