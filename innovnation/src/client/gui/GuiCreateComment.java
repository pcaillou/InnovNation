package client.gui;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import client.ClientCore;
import data.CommentValence;
import data.IComment;
import data.IIdea;
// AD import data.IItem;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;
import functions.Game;
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
public class GuiCreateComment implements IEventListener {
	
	private Logger logger = Logger.getLogger("client.gui.createcomment");
	
	private Shell shell = null;
	private Composite compositeHost = null;
	
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Display display = null;
	
	private ClientCore core=null;

	private Text fieldTokenNb = null;
	private Label fieldTokenText = null;
	private Text fieldCommentText = null;
	private Text fieldTokenRemain = null;

	private IIdea selectedidea=null;
	
	private Table fieldOtherIdeas = null;

	private Button buttonPlus = null;
	private Button buttonMoins = null;
	private Button buttonPositive = null;
	private Button buttonNegative = null;
	private Button buttonNeutral = null;
	
//	private Button buttonCreateItem = null;

	private Button buttonValidate = null;
	private Button buttonCancel = null;
	

	private final static String TXT_IDEA_NAME = "Nombre de mises:  ";
	private final static String TXT_TOOLTIP_IDEA_NAME = "Combien voulez vous miser?";
	private final static String TXT_IDEA_DESC = "Commentaire :";
	private final static String TXT_TOOLTIP_IDEA_DESC = "Expliquer pourquoi vous aimez / N'aimez pas";
	
	private final static String TXT_PARENT_IDEAS = "Mises en cours";
	
	private final static String TXT_BUTTON_VALIDATE = "Valider Mise/ Commentaire";
	private final static String TXT_BUTTON_CANCEL = "Annuler";

	private final static String TXT_BUTTON_PLUS = "++";
	private final static String TXT_BUTTON_MOINS = "--";

	
	private Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;
	
	/**
	 * The parameter style passed as a paraemter for all the composites (enables to add border, as example)
	 */
	private final int LOOK_COMPOSITE_STYLE_SUBSPACES = SWT.NONE; // SWT.BORDER;
	
	private final int LOOK_NB_LINES_DESC = 3;	
	@SuppressWarnings("unused")
	private final int LOOK_NB_LINES_ITEMDESC = 2;	
	@SuppressWarnings("unused")
	private final int LOOK_NB_LINES_ITEMS_AVAILABLE = 10;
	private final int LOOK_NB_LINES_ITEMS_SELECTED = 8;

	public int nbvote=0;	
	public int nbvotetotal=0;	
	

	private Map<Integer,TableItem> ideaId2tableItem = new HashMap<Integer, TableItem>();
	private Map<Integer,Integer> ideaTable = new HashMap<Integer, Integer>();
	public int nbtokprec=0;


	private GuiEmbbedType embbedType;
	
	private IGame localGame;
	
	private Collection<Integer> resultParentIds = null;
	private String resultName = null;
	private String resultDesc = null;
	private Collection<Integer> resultItemIds = null;

	private Integer playerId;
	
	/**
	 * CompositeHost is supposed to be free of use (we will change its layout and so on.)
	 * @param compositeHost
	 */
	public GuiCreateComment(Integer playerId, Composite compositeHost, IGame game, ClientCore co) {
		
		this.playerId = playerId;
		this.core=co;
		embbedType = GuiEmbbedType.EMBEDDED;
		this.compositeHost = compositeHost;
		this.display = compositeHost.getDisplay();
		initColors();
		nbvote=0;
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
	public GuiCreateComment(Integer playerId, Display display, IGame game, ClientCore co) {
		
		this.playerId = playerId;
		this.core=co;
		embbedType = GuiEmbbedType.STANDALONE;
		this.compositeHost = null;
		this.display = display;
		initColors();
		nbvote=0;		
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
		
//		buttonPlus.setEnabled(fieldAllAvailableItems.getSelectionCount() > 0);
//		buttonMoins.setEnabled(fieldSelectedItems.getSelectionCount() > 0);

		buttonValidate.setEnabled(
				(fieldTokenNb.getText().trim().length() > 0) 
				//&&
			//	(fieldIdeaDesc.getText().trim().length() > 0) &&
			//	(fieldSelectedItems.getItemCount() > 0)
				);
		
		
	}
	
	
	public void init(Collection<IIdea> selectedIdeas, Collection<IComment> selectedComments) {

		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell = new Shell (display, SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
			shell.setText ("Miser / Commenter");
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
			GridLayout layoutName = new GridLayout(5, false);
			
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

						
			buttonMoins = new Button(compositeName, SWT.PUSH);
			buttonMoins.setText(TXT_BUTTON_MOINS);
			buttonMoins.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickMoins();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickMoins();
				}
			});
			
			fieldTokenNb = new Text(compositeName,  SWT.READ_ONLY);
			fieldTokenNb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
			fieldTokenNb.setToolTipText(TXT_TOOLTIP_IDEA_NAME);
			fieldTokenNb.setEditable(false);

			buttonPlus = new Button(compositeName, SWT.PUSH);
			buttonPlus.setText(TXT_BUTTON_PLUS);
			buttonPlus.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickPlus();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickPlus();
				}
			});
			// the field
			
			fieldTokenText = new Label(compositeName, SWT.READ_ONLY);
			fieldTokenText.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			fieldTokenText.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			fieldTokenText.setToolTipText(TXT_TOOLTIP_IDEA_NAME);

			fieldTokenNb.addModifyListener(updateStatesModifyListener);

			fieldTokenRemain = new Text(compositeName, SWT.BORDER);
			fieldTokenRemain.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
			fieldTokenRemain.setToolTipText(TXT_TOOLTIP_IDEA_NAME);
			fieldTokenRemain.setEditable(false);
			
			GridLayout layoutName2 = new GridLayout(3, false);
			
			Composite compositeName2 = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeName2.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeName2.setLayout(layoutName2);
			compositeName2.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL));

//			shell.setLayout (new FillLayout ());
/*			Listener listener = new Listener () {
				public void handleEvent (Event e) {
					Control [] children = shell.getChildren ();
					for (int i=0; i<children.length; i++) {
						Control child = children [i];
						if (e.widget != child && child instanceof Button && (child.getStyle () & SWT.TOGGLE) != 0) {
							((Button) child).setSelection (false);
						}
					}
					((Button) e.widget).setSelection (true);
				}
			};*/
			buttonNegative = new Button (compositeName2, SWT.RADIO);
			buttonNegative.setText("Negatif");
			buttonNegative.setSelection (false);
			buttonNeutral = new Button (compositeName2, SWT.RADIO);
			buttonNeutral.setSelection (true);
			buttonNeutral.setText("Neutre");
			buttonPositive = new Button (compositeName2, SWT.RADIO);
			buttonPositive.setSelection (false);
			buttonPositive.setText("Positif");

		
					
		}
		
		// bottom composite
		{

//			RowLayout layoutPlusMoins = new RowLayout(SWT.HORIZONTAL);
/*			Composite compositePlusMoins = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositePlusMoins.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositePlusMoins.setLayout(layoutPlusMoins);
			compositePlusMoins.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	*/					
			
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
			fieldCommentText = new Text(compositeDesc, SWT.BORDER | SWT.MULTI);
			GridData gdDesc = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
			fieldCommentText.setLayoutData(gdDesc);
			fieldCommentText.setToolTipText(TXT_TOOLTIP_IDEA_DESC);

			fieldCommentText.addModifyListener(updateStatesModifyListener);

			// (define its height !)
			GC gc = new GC (fieldCommentText);
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
		
			fieldOtherIdeas = new Table(compositeHost, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL);
			
			GridData gdParents = new GridData(GridData.FILL_BOTH);
		
			fieldOtherIdeas.addSelectionListener(updateStatesSelectionListener);

			GC gc = new GC (fieldOtherIdeas);
			FontMetrics fm = gc.getFontMetrics ();
			int height = fm.getHeight ();
			gc.dispose ();
			gdParents.minimumHeight = height*LOOK_NB_LINES_ITEMS_SELECTED;

			fieldOtherIdeas.setLayoutData(gdParents);
			
			new TableColumn(fieldOtherIdeas, SWT.LEFT);
			fieldOtherIdeas.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					clickidea();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					clickidea();
				}
			});
			
			
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

		if (!selectedIdeas.isEmpty()) {
			// select the last idea of the selection
			for (IIdea idea: selectedIdeas) {
				selectedidea=idea;
			}	
		} else if (!selectedComments.isEmpty()) {
			// it is currently not possible to select a comment; 
			// then comment the idea commented by this comment  (got it ?)
			IComment firstComment = selectedComments.iterator().next();
			try {
				selectedidea = localGame.getIdea(firstComment.get());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				close();	
			}
		}
		
		// security 
		if (selectedidea == null)
			close();
		
		this.fieldTokenNb.setText(""+nbvote);				
		this.fieldTokenText.setText(" pour "+selectedidea.getShortName());				
		this.fieldTokenRemain.setText(""+(Game.INITIAL_TOKEN_COUNT_BY_PLAYER-nbtokprec-nbvote)+" restants");
		
		
		populateIdeas();
		

		selectIdeas(selectedIdeas);
		clickidea();
		
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
		
		if ((shell != null) && (!shell.isDisposed()) ) {
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

	private void clickidea() {
		
		nbtokprec=0;
		for (int i=0; i<ideaId2tableItem.size(); i++)
		{
			TableItem tabId=ideaId2tableItem.get(i);
			if (tabId.getChecked())
			{
				nbtokprec++;
			}
		}
		if ((nbvote+nbtokprec)>Game.INITIAL_TOKEN_COUNT_BY_PLAYER)
		{
			nbvote=nbtokprec;
		}
		this.fieldTokenNb.setText(""+nbvote);				
		this.fieldTokenRemain.setText(""+(Game.INITIAL_TOKEN_COUNT_BY_PLAYER-nbtokprec-nbvote)+" restants");

	}
	
	private void clickPlus() {
		if (nbvote<(Game.INITIAL_TOKEN_COUNT_BY_PLAYER-nbtokprec))
		nbvote++;
		this.fieldTokenNb.setText(""+nbvote+" pour "+selectedidea.getShortName());	
		this.buttonPositive.setSelection(true);
		this.buttonNegative.setSelection(false);
		this.buttonNeutral.setSelection(false);
		clickidea();
	}
	
	private void clickMoins() {
		if (nbvote>0)
		nbvote--;
		this.fieldTokenNb.setText(""+nbvote+" pour "+selectedidea.getShortName());				
		clickidea();
		
	}
	
	private void clickValidate() {
		
		resultParentIds = new LinkedList<Integer>();
		resultName = fieldTokenNb.getText().trim();
		resultDesc = fieldCommentText.getText().trim();
		resultItemIds = new LinkedList<Integer>();
		CommentValence valence=CommentValence.NEUTRAL;
//		logger.debug("Valence "+playerId+"  "+ valence);
		if (buttonPositive.getSelection()&(resultDesc.length()>0)) valence=CommentValence.POSITIVE;
//		logger.debug("Valence "+playerId+"  "+ valence);
		if (buttonNegative.getSelection()&(resultDesc.length()>0)) valence=CommentValence.NEGATIVE;
//		logger.debug("Valence "+playerId+"  "+ valence);
		try {
//			Map<Integer, Integer> res = new HashMap<Integer, Integer>(localGame.getAllIdeas().size(),1.0f);
			Map<Integer, Integer> res = new HashMap<Integer, Integer>();
		
		
			for (int i=0; i<ideaId2tableItem.size(); i++)
			{
				TableItem tabId=ideaId2tableItem.get(i);
				if (!tabId.getChecked())
				{
					int idid=ideaTable.get(i);
					resultDesc = "";
					if (res.containsKey(idid))
					{
						res.put(idid, res.get(idid)-1);
					}
					else
					{
						res.put(idid,-1);
						
					}
				}
			}

			Iterator<Integer> itid=res.keySet().iterator();
			while (itid.hasNext())
			{
				Integer idid=itid.next();
				localGame.commentIdea(this.playerId.intValue(), idid.intValue(), "", res.get(idid).intValue(),CommentValence.NEUTRAL);
			
			}
			try {
//				logger.debug("Valence "+playerId+"  "+ valence);
				if ( (nbvote > 0) || (!resultDesc.isEmpty()) )
					localGame.commentIdea(this.playerId.intValue(), selectedidea.getUniqueId(), resultDesc, nbvote,valence);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			

			/*			for (TableItem item : fieldOtherIdeas.getItems()) {
			
			if (item.getChecked()) 
				resultParentIds.add((Integer) item.getData());
			
			
		}
	*/	
		
		close();
		
	}
	
	private void clickCancel() {
	
		close();
	}
	
	
	private Map<Integer, String> getIdeas() {
		
		Map<Integer, String> res = new HashMap<Integer, String>();
		Map<Integer, Integer> idtok = core.getCurrentIdeasTokens() ;

		nbtokprec=0;
		try {
			for (IIdea idea: localGame.getAllIdeas()) {
				if (idtok.get(idea.getUniqueId())>0)
				{
					for (int nbt=0; nbt<idtok.get(idea.getUniqueId()); nbt++)
					{
						res.put(nbtokprec,idea.getShortName());
						ideaTable.put(nbtokprec, idea.getUniqueId());
						nbtokprec++;
					}
					
				}
				
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
		fieldOtherIdeas.setRedraw(false);
		
		// builds the set of all the ids that were stored before update (we may have to remove something !)
		Set<Integer> storedIdeasIds = new HashSet<Integer>(ideaId2tableItem.keySet());
		
		for (Integer ideaId : ideas.keySet()) {
			
			if (storedIdeasIds.remove(ideaId)) {
				// right, this item was already displayed...
				
			} else {
				
				// look ! seems like this one is a novel one !
				TableItem novelItem = new TableItem(fieldOtherIdeas, 0);
				novelItem.setText(ideas.get(ideaId));
				novelItem.setData(ideaId);
				ideaId2tableItem.put(ideaId, novelItem);
				
			}
					
		}
		
		// now, remove the items that disappeared
		for (Integer idToRemove :  storedIdeasIds) {
			
			if (ideaId2tableItem.containsKey(idToRemove)) {
				
				TableItem it = ideaId2tableItem.get(idToRemove);
				
				final int idx = fieldOtherIdeas.indexOf(it);
				fieldOtherIdeas.remove(idx);
				
			} else {
				logger.error("??? inconsistency detected: unable to find item "+idToRemove);
			}
		}
		
		for (TableColumn col : fieldOtherIdeas.getColumns()) {
			col.setWidth(200);
		}
		
		
		// update displays
		fieldOtherIdeas.setRedraw(true);

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
		
		for (Integer tabId: ideaId2tableItem.keySet()) {
			TableItem tableItem = ideaId2tableItem.get(tabId);
			tableItem.setChecked(true);
		}
		
	}

	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	

	
}
