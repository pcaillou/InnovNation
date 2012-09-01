package client.gui;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
// AD import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import util.RMIUtils;
//import org.graphstream.ui.swingViewer.Viewer;

import client.DelegatingClientCore;
import client.IClientCore.TreeExplorator;
import client.gui.prefuse.ClientWhiteboardSWT;
import client.gui.prefuse.GuiClientMode;
import client.gui.prefuse.GuiServerMode;
import data.Avatars;
import data.IComment;
import data.IIdea;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;
import fr.research.samthiriot.commons.parameters.gui.swt.GUIParameters;
import fr.research.samthiriot.commons.parameters.gui.swt.GUIParameters.GUIParametersMode;
import functions.GameServer;
import functions.TypeScore;
import functions.logs.SimAnalyzerLog;
/**
 * TODO déconnecter si connecté !
 * 
 * @author sammy
 *
 * un message inutile
 */
public class GuiTestMain implements IEventListener // and for events from the game 
{

	private Display display = null;
	private Shell shell = null;

	private GuiScores scoredial;

	public GuiClientMode modeClient = GuiClientMode.DISCONNECTED;
	public GuiServerMode modeServer = GuiServerMode.NO_SERVER;
	
	private GuiBotManager botManager;
	
	public static final boolean TEST_MODE = true;
	
	public static final int PERIOD_REFRESH_SCORES = 1000; // ms

	private static ArrayList<String> TXT_TOOLTIP_HIGH_SCORE;	
	
	public static String lastGameName = null;
	
	/**
	 * Only relevant if mode != DISCONNECTED.
	 */	
	private String gameBindName = null;
	
	
	private ClientWhiteboardSWT clientWhiteboard = null;

//	private IGame localGame ;

	public final String TXT_WINDOW_TITLE = "InnovNation - proto 0.1";


	/**
	 * Client used to access to a game as an observer or player
	 */
	public DelegatingClientCore clientCore = null;
	
	/**
	 * The game server that may be used for creating a game.
	 */
	private GameServer gameServer = null;
	
	
	/**
	 * The ID of the player who is currently playing (only relevant if connected and playing)
	 */
	private Integer playerId = null;
	
	private MenuItem itemConnectAndJoin, itemCreateJoinAndPlay, itemCreateBotGame, itemCreateJoin, itemObserve, itemJoin, itemDisconnect, itemCreateServerLocal, itemCreateServer, itemCreateGame, itemShutdownServer, itemAddBotManager;
	
	@SuppressWarnings("unused")
	private Button buttonAddIdea, buttonAddItem, buttonComment, buttonCleanCommentInput, buttonAddComment, buttonTest,buttonShowScores;
	
	@SuppressWarnings("unused")
	private Text commentText;

	private Label[] scores,scoreslab,rg,rglab,scoreevo,rgevo;
	
	@SuppressWarnings("unused")
	private Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;

	private Tree ideaCommentTree = null;
	private Map<Integer, Double>  scvalprec;
	private Map<Integer, Double>  scrgprec;
	
	private Collection<IIdea> selectedIdeas = null;
	
	
	private Runnable ideaCommentTreeFillerRunnable = null;

	private final TreeExplorator ideaCommentTreeFiller = new TreeExplorator() {
		private Stack<TreeItem> treeItems = new Stack<TreeItem>();

		@Override
		public void start(String startMessage) {
			if(ideaCommentTree == null) return;
			TreeItem item = new TreeItem(ideaCommentTree, SWT.NONE);
			item.setText(startMessage);
			treeItems.push(item);
		}

		@Override
		public void end(String endMessage) {
			//nothing for the moment
		}

		@Override
		public void work(DefaultMutableTreeNode node, int depth) {
			if( depth!=0 ){
				IComment com = (IComment) node.getUserObject();
				
				StringBuilder sb = new StringBuilder();
				try {
					sb.append(clientCore.getGame().getPlayer(com.getPlayerId()).getShortName());
					sb.append(": ");
				} catch (RemoteException e) {
					sb.append("???: ");
				}
				
				int tokens = com.getTokensCount();
				if (tokens>0) {
					sb.append(" [+");
					sb.append(tokens);
					sb.append("] ");
				} else if (tokens<0) {
					sb.append(" [");
					sb.append(tokens);
					sb.append("] ");
				}
				sb.append(com.getText());
				
				TreeItem item =new TreeItem(treeItems.peek(), SWT.NONE);
				item.setText(sb.toString());
				//System.out.println("build tree item: "+sb.toString());
				treeItems.push(item);
			}
			
			@SuppressWarnings("unchecked")
			Enumeration<DefaultMutableTreeNode> children = node.children();
			while(children.hasMoreElements()) {
				work(children.nextElement(), depth+1);
			}
			
			treeItems.pop().setExpanded(true);
		}
		
	};

	
	public GuiTestMain() {
	}
	
	private void updateStates() {
		
		updateMenuStates();
		updateButtonStates();
		majScores();
		
	}

	private void updateMenuStates() {
		
		itemConnectAndJoin.setEnabled(modeClient == GuiClientMode.DISCONNECTED);
		itemCreateJoinAndPlay.setEnabled(modeClient == GuiClientMode.DISCONNECTED && modeServer == GuiServerMode.NO_SERVER);
		itemCreateBotGame.setEnabled(modeClient == GuiClientMode.DISCONNECTED && modeServer == GuiServerMode.NO_SERVER);
		itemCreateJoin.setEnabled(modeClient == GuiClientMode.DISCONNECTED && modeServer == GuiServerMode.NO_SERVER);
		
		itemObserve.setEnabled(modeClient == GuiClientMode.DISCONNECTED);
		itemJoin.setEnabled(modeClient == GuiClientMode.MONITOR);
		itemDisconnect.setEnabled(modeClient != GuiClientMode.DISCONNECTED);
		
		itemCreateGame.setEnabled(modeClient == GuiClientMode.DISCONNECTED);
		
		itemCreateServerLocal.setEnabled(modeServer == GuiServerMode.NO_SERVER);
		itemCreateServer.setEnabled(modeServer == GuiServerMode.NO_SERVER);
		itemShutdownServer.setEnabled(modeServer != GuiServerMode.NO_SERVER);
		itemAddBotManager.setEnabled(modeServer != GuiServerMode.NO_SERVER);
		
		
		itemCreateGame.setEnabled(modeServer != GuiServerMode.NO_SERVER);
		
		clientWhiteboard.setActionsEnabled(modeClient == GuiClientMode.PLAYING);
		
	}
	
	private void updateButtonStates() {
		
		buttonAddIdea.setEnabled(modeClient == GuiClientMode.PLAYING);
		buttonAddItem.setEnabled(modeClient == GuiClientMode.PLAYING);
		
		
	}

	private void initMenus() {
		
		Menu menuBar = new Menu (shell, SWT.BAR);
		shell.setMenuBar (menuBar);
		
		// Jeu
		
		{
			MenuItem itemFile = new MenuItem (menuBar, SWT.CASCADE);
			itemFile.setText ("&Jeu");
			
			Menu menuFile = new Menu (shell, SWT.DROP_DOWN);
			itemFile.setMenu (menuFile);
			
			{
				itemConnectAndJoin = new MenuItem (menuFile, SWT.PUSH);
				itemConnectAndJoin.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clickConnectAndJoin();					}
				});
				itemConnectAndJoin.setText ("Re&joindre une partie...\tCtrl+J");

				itemConnectAndJoin.setAccelerator (SWT.CTRL + 'J');
			}
			
			{
				itemCreateJoinAndPlay = new MenuItem (menuFile, SWT.PUSH);
				itemCreateJoinAndPlay.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clickCreateJoinAndPlay();
					}
				});
				itemCreateJoinAndPlay.setText ("&Créer une partie et jouer...\tCtrl+N");

				itemCreateJoinAndPlay.setAccelerator (SWT.CTRL + 'N');
			}
			
			{
				itemCreateBotGame = new MenuItem (menuFile, SWT.PUSH);
				itemCreateBotGame.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clickCreateBotGame();
					}
				});
				itemCreateBotGame.setText ("Creer une partie rapide avec des bots...");

			}
			

			{
				itemCreateJoin = new MenuItem (menuFile, SWT.PUSH);
				itemCreateJoin.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clickCreateAndJoin();					}
				});
				itemCreateJoin.setText ("&Créer une partie et l'observer");

				
			}
			
			
			new MenuItem(menuFile, SWT.SEPARATOR);
			{
				MenuItem itemClient = new MenuItem(menuFile, SWT.CASCADE);
				itemClient.setText("&Client...");
				

				Menu menuClient = new Menu(shell, SWT.DROP_DOWN);
				itemClient.setMenu(menuClient);
				

				{
					itemObserve = new MenuItem (menuClient, SWT.PUSH);
					itemObserve.addListener (SWT.Selection, new Listener() {
						public void handleEvent (Event e) {
							clickConnect();
						}
					});
					itemObserve.setText ("Observer...");
				}
				
				{
					itemJoin = new MenuItem (menuClient, SWT.PUSH);
					itemJoin.addListener (SWT.Selection, new Listener() {
						public void handleEvent (Event e) {
							clickJoin();
						}
					});
					itemJoin.setText ("Participer...");
				}
				
				{
					itemDisconnect = new MenuItem (menuClient, SWT.PUSH);
					itemDisconnect.addListener (SWT.Selection, new Listener() {
						public void handleEvent (Event e) {
							clickDisconnect();
						}
					});
					itemDisconnect.setText ("&Déconnecter...\tCtrl+D");
					itemDisconnect.setAccelerator (SWT.CTRL + 'D');
					
				}
			}
			
			new MenuItem(menuFile, SWT.SEPARATOR);

			{
				MenuItem itemServer = new MenuItem(menuFile, SWT.CASCADE);
				itemServer.setText("&Serveur...");
				

				Menu menuServer = new Menu(shell, SWT.DROP_DOWN);
				itemServer.setMenu(menuServer);

				{
					itemCreateServerLocal = new MenuItem (menuServer, SWT.PUSH);
					itemCreateServerLocal.addListener (SWT.Selection, new Listener() {
						public void handleEvent (Event e) {
							clickCreateServerLocal();
						}
					});
					itemCreateServerLocal.setText ("Créer un serveur &local");
					
					
				}
				
				{
					itemCreateServer = new MenuItem (menuServer, SWT.PUSH);
					itemCreateServer.addListener (SWT.Selection, new Listener() {
						public void handleEvent (Event e) {
							clickCreateServer();
						}
					});
					itemCreateServer.setText ("Créer un serveur...");
					
					
				}
				{
					itemShutdownServer= new MenuItem (menuServer, SWT.PUSH);
					itemShutdownServer.addListener (SWT.Selection, new Listener() {
						public void handleEvent (Event e) {
							clickShutdownServer();
						}
					});
					itemShutdownServer.setText ("Eteindre le serveur");
					
					
				}
				{
					itemAddBotManager= new MenuItem (menuServer, SWT.PUSH);
					itemAddBotManager.addListener (SWT.Selection, new Listener() {
						public void handleEvent (Event e) {
							clickAddBotManager();
						}
					});
					itemAddBotManager.setText ("Ouvrir le gestionaire de bots...");
					itemAddBotManager.setAccelerator (SWT.CTRL + 'B');
					
					
				}
				{
					itemCreateGame = new MenuItem (menuServer, SWT.PUSH);
					itemCreateGame.addListener (SWT.Selection, new Listener() {
						public void handleEvent (Event e) {
							clickCreateGame();
						}
					});
					itemCreateGame.setText ("&Créer une partie...\tCtrl+C");
					itemCreateGame.setAccelerator (SWT.CTRL + 'C');
					
				}
				
			}
			
			
			
			new MenuItem(menuFile, SWT.SEPARATOR);
			
			{
				MenuItem itemQuit = new MenuItem (menuFile, SWT.PUSH);
				itemQuit.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clickQuit();
					}
				});
				itemQuit.setText ("&Quitter...\tCtrl+Q");
				itemQuit.setAccelerator (SWT.CTRL + 'Q');
			}
			//item.setAccelerator (SWT.MOD1 + 'A');
		}	
		
		{
			MenuItem itemView = new MenuItem (menuBar, SWT.CASCADE);
			itemView.setText ("&Vue");
			
			Menu menuView = new Menu (shell, SWT.DROP_DOWN);
			itemView.setMenu (menuView);
			
			
			{
				MenuItem itemZoomIn = new MenuItem (menuView, SWT.PUSH);
				itemZoomIn.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clientWhiteboard.zoomIn();
					}
				});
				itemZoomIn.setText ("Zoom &in\tCtrl+'+'");
				itemZoomIn.setAccelerator (SWT.CTRL + '+');

			}
			
			{
				MenuItem itemZoomOut = new MenuItem (menuView, SWT.PUSH);
				itemZoomOut.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clientWhiteboard.zoomOut();
					}
				});
				itemZoomOut.setText ("Zoom &out\tCtrl+'-'");
				itemZoomOut.setAccelerator (SWT.CTRL + '-');

			}
			
			{
				MenuItem itemZoomReset = new MenuItem (menuView, SWT.PUSH);
				itemZoomReset.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clientWhiteboard.zoomOne();
					}
				});
				itemZoomReset.setText ("Zoom &1:1\tCtrl+0");
				itemZoomReset.setAccelerator (SWT.CTRL + '0');

			}
			
			{
				MenuItem itemZoomToFit = new MenuItem (menuView, SWT.PUSH);
				itemZoomToFit.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clientWhiteboard.fitToScreen();
					}
				});
				itemZoomToFit.setText ("Zoom to &fit...\tCtrl+F");
				itemZoomToFit.setAccelerator (SWT.CTRL + 'F');
				
			}

			new MenuItem(menuView, SWT.SEPARATOR);

			{
				MenuItem itemShowRoot = new MenuItem (menuView, SWT.PUSH);
				itemShowRoot.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clientWhiteboard.centerOnRootIdea();
					}
				});
				itemShowRoot.setText ("Voir idée &racine\tCtrl+R");
				itemShowRoot.setAccelerator (SWT.CTRL + 'R');
				
			}
						
			new MenuItem(menuView, SWT.SEPARATOR);

			{
				MenuItem itemParams = new MenuItem (menuView, SWT.PUSH);
				itemParams.addListener (SWT.Selection, new Listener() {
					public void handleEvent (Event e) {
						clickEditVisuParams();			}
				});
				itemParams.setText ("&Parameters");

			}
			
		}	
		
		
	}
	
	
	public void initFrameScores()
	{
		this.scoredial  = new GuiScores(clientWhiteboard, gameBindName, display, this);
		
		scoredial.init();
		
		scoredial.start();
		
	}
	
	public void init() {

		display = new Display ();
		shell = new Shell (display, SWT.SHELL_TRIM);
		shell.setText(TXT_WINDOW_TITLE);
		
		botManager = null;

		//shell.setLayout(new GridLayout(2, false));
		shell.setLayout(new GridLayout(1, false));

		initMenus();
		
		initFrameScores();
		
		// left pane: whiteboard
		{
			Composite compositeWhiteboard = new Composite(shell, SWT.EMBEDDED | SWT.NO_BACKGROUND | SWT.BORDER);

			GridData gdWhiteboard = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);

			compositeWhiteboard.setLayoutData(gdWhiteboard);

			clientWhiteboard = new ClientWhiteboardSWT(compositeWhiteboard);
			clientWhiteboard.init(this);

			clientWhiteboard.setLayoutActive(true);

		}

		// right pane: buttons + comments
		{
			/*
			Composite compositeRight = new Composite(shell, SWT.NONE);
			GridData gdRight = new GridData(GridData.FILL_VERTICAL);

			compositeRight.setLayoutData(gdRight);

			compositeRight.setLayout(new GridLayout());
			 */
			Composite compositeBottom = new Composite(shell, SWT.NONE);
			GridData gdBottom= new GridData(GridData.FILL_HORIZONTAL);

			compositeBottom.setLayoutData(gdBottom);

			compositeBottom.setLayout(new GridLayout(3, false));
						
			

			//scores
			{
				
				Composite compositeScores = new Composite(compositeBottom, SWT.NONE);
				compositeScores.setLayout(new GridLayout(1,false));
				compositeScores.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
				
				scores=new Label[TypeScore.values().length];
				scoreslab=new Label[TypeScore.values().length];
				rg=new Label[TypeScore.values().length];
				rglab=new Label[TypeScore.values().length];
				scoreevo=new Label[TypeScore.values().length];
				rgevo=new Label[TypeScore.values().length];
				int i=0;
				scvalprec = new HashMap<Integer, Double>(TypeScore.values().length, 1.0f);
				scrgprec = new HashMap<Integer, Double>(TypeScore.values().length, 1.0f);
				// AD TXT_TOOLTIP_HIGH_SCORE=new ArrayList(TypeScore.values().length); /*
				TXT_TOOLTIP_HIGH_SCORE=new ArrayList<String>(TypeScore.values().length);// */
				
	
				for (TypeScore sc: TypeScore.values())
				{
					scvalprec.put(i, (double)0);
					scrgprec.put(i, (double)1);
//					compositeSc.setLayout(new RowLayout(SWT.HORIZONTAL));
					Composite compositeSc = new Composite(compositeScores, SWT.NONE);
					GridLayout layoutName = new GridLayout(6, false);
					GridData gdSc = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
					compositeSc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					compositeSc.setLayout(layoutName);
					compositeSc.setLayoutData(gdSc);
					
					scoreslab[i] = new Label(compositeSc, SWT.READ_ONLY);
					scoreslab[i].setText(sc.nom+":");
					scoreslab[i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					scoreslab[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
					scoreslab[i].setToolTipText(sc.tooltip);
					scores[i] = new Label(compositeSc, SWT.READ_ONLY);
					scores[i].setText(" 0000");
					scores[i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					scores[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
					TXT_TOOLTIP_HIGH_SCORE.add(i, "High Scores");
					scores[i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(i));
					scoreevo[i] = new Label(compositeSc, SWT.READ_ONLY);
					scoreevo[i].setText(" (==)");
					scoreevo[i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					scoreevo[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
					rglab[i] = new Label(compositeSc, SWT.READ_ONLY);
					rglab[i].setText(" Rg:");
					rglab[i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					rglab[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
					rg[i] = new Label(compositeSc, SWT.READ_ONLY);
					rg[i].setText(" 00");
					rg[i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					rg[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
					rgevo[i] = new Label(compositeSc, SWT.READ_ONLY);
					rgevo[i].setText(" (==)");
					rgevo[i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
					rgevo[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
					i++;
					
				}

				{
					// init the timer for refreshing this
					display.timerExec(
							PERIOD_REFRESH_SCORES, 
							new Runnable() {
							      public void run() {
									majScores();
							        display.timerExec(PERIOD_REFRESH_SCORES, this);
							      }
							    }
						    );
					
				}
			
			}
			
			//comments
			{
				Composite compositeComments = new Composite(compositeBottom, SWT.NONE);
				
				GridData gdComments = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
				compositeComments.setLayoutData(gdComments);
	
				compositeComments.setLayout(new GridLayout());

				//it is initialy empty, as no idea is selected
				ideaCommentTree = new Tree(compositeComments, SWT.VIRTUAL | SWT.BORDER);
				
				ideaCommentTree.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL));

				// this Rennable is used to refresh comments displayed in the bottom part.
				// what ? this should be somewhere else ? hum... that's called anarchy. Or collaborative work ?
				ideaCommentTreeFillerRunnable = new Runnable() {
					@Override
					public void run() {
						selectedIdeas = clientWhiteboard.getSelectionControl().getSelectedIdeas();
						if(!selectedIdeas.isEmpty()){
							ideaCommentTree.removeAll();
							for(IIdea i : selectedIdeas){
								try{
									clientCore.displayIdeaComments(i.getUniqueId(), ideaCommentTreeFiller);
								} catch (RemoteException e){
									displayError("an anormal remote exception happened.", e);
								}
							}						}
					}
				};

				
				//make the tree resets its data when a new idea is selected
				clientWhiteboard.getSelectionControl().addListener(new ISelectionListener() {
					@Override
					public void notifySelectionChanged() {
						display.asyncExec(ideaCommentTreeFillerRunnable);
					}
				}
				);
				
			}
			
			
			//buttons
			{
				Composite compositeButtons = new Composite(compositeBottom, SWT.NONE);
	
				GridData gdButtons = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
	
				compositeButtons.setLayoutData(gdButtons);
	
				compositeButtons.setLayout(new RowLayout(SWT.VERTICAL));
	
	
				buttonAddIdea = new Button(compositeButtons, SWT.PUSH);
				buttonAddIdea.setText("Ajouter une idée");
				buttonAddIdea.addSelectionListener(new SelectionListener() {
	
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						clickCreateIdea();
					}
	
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						clickCreateIdea();
					}
				});
				
				buttonComment = new Button(compositeButtons, SWT.PUSH);
				buttonComment.setText("Commenter / Miser");
				buttonComment.addSelectionListener(new SelectionListener() {
	
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						clickVoteOnIdea();
					}
	
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						clickVoteOnIdea();
					}
				});
				
	
				buttonAddItem = new Button(compositeButtons, SWT.PUSH);
				buttonAddItem.setText("Ajouter un item");
				buttonAddItem.addSelectionListener(new SelectionListener() {
	
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						clickCreateItem();
					}
	
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						clickCreateItem();
					}
				});
				buttonShowScores = new Button(compositeButtons, SWT.PUSH);
				buttonShowScores.setText("Afficher les scores");
				buttonShowScores.addSelectionListener(new SelectionListener() {
	
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						initFrameScores();
					}
	
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						initFrameScores();
					}
				});
					
			}
		}

		
		clientCore = new DelegatingClientCore(clientWhiteboard);
		
		updateStates();
		
		updateShellTitle();
		

	}

	/**
	 * Cree un bot manager avec un GUI separe s'il n'existe pas
	 */
	private void clickAddBotManager()
	{
		if (botManager == null || !botManager.isAlive())
		{
			botManager = new GuiBotManager(clientWhiteboard, gameBindName, display, this);
			
			botManager.init();
			
			botManager.start();
		}

	}
	
	private void clickDisconnect() {
		
		clientCore.disconnectFromGame();
		
		setClientMode(GuiClientMode.DISCONNECTED);
		
	}
	
	private void clickShutdownServer() {
		
		if (gameServer != null) {
			gameServer.shutDown();
			gameServer = null;
			setServerMode(GuiServerMode.NO_SERVER);
		}
		
	}
	
	private void clickQuit() {
		
		// TODO disconnect and so on if relevant
		
		// TODO confirmation
		
		shell.setVisible(false);
		
		shell.dispose();
		
	}
	
	private void clickCreateServerLocal() {

		if (gameServer == null) {
			try {
				gameServer = new GameServer(GameServer.SERVER_NAME);
				lastGameName = GuiCreateGame.GAME_NAME;
				
				setServerMode(GuiServerMode.SERVER_WITHOUT_GAME);
			} catch (Exception e) {
				displayError("Error while creating the server, sorry", e);
			} 
		}
		
		
	}
	
	private void clickCreateServer() {
		

		
		if (display.isDisposed())
			return;

		GuiCreateServer guiCreateServer = new GuiCreateServer(display);
		guiCreateServer.init();
		guiCreateServer.run();
		
		String serverHost = guiCreateServer.getHost();
		
		if (serverHost == null)
			return;	// cancelled !
		
		if (gameServer == null) {
			try {
				gameServer = new GameServer(GameServer.SERVER_NAME, serverHost);
				
				lastGameName = GuiCreateGame.GAME_NAME;
				
				setServerMode(GuiServerMode.SERVER_WITHOUT_GAME);
			} catch (Exception e) {
				displayError("Error while creating the server, sorry", e);
			} 

		}
		
	}
		
	private void clickConnect() {
		
		GuiConnect guiConnect = new GuiConnect(this.clientCore, display);
		guiConnect.init();
		guiConnect.run();
		
		String toFetch = guiConnect.getGameToFetch();
		
		if (toFetch == null)
			return;	// user cancelled
		
		try {
			connectToGame(toFetch);
			
		} catch (Exception e) {
			displayError("Error while connecting to game, sorry", e);
		} 
		
		
	}

	private void clickConnectAndJoin() {

		clickConnect();
		
		// check that connection did work !
		if (modeClient != GuiClientMode.MONITOR)
			return;
		
		clickJoin();
		
	}
	
	private void clickCreateAndJoin() {

		clickCreateServerLocal();
		
		if (modeServer != GuiServerMode.SERVER_WITHOUT_GAME)
			return;
		
		clickCreateGame();
		
		if (modeServer != GuiServerMode.SERVER_WITH_GAME)
			return;
		
		clickConnect();
		
	}
	
	private void clickCreateJoinAndPlay() {

		clickCreateAndJoin();
		
		if (modeClient != GuiClientMode.MONITOR)
			return;
		
		clickJoin();
		
	}
	
	private void clickCreateBotGame()
	{

		String serverName = "Bot_game", serverTheme = "nice theme";
		
		clickCreateServerLocal();
		
		if (modeServer != GuiServerMode.SERVER_WITHOUT_GAME)
		{
			return;
		}
		
		try {
			gameServer.createGame(serverName, serverTheme);
			lastGameName = serverName;
		} catch (Exception e) {
			displayError("Error while creating this game, sorry", e);
		} 
		
		setServerMode(GuiServerMode.SERVER_WITH_GAME);
		
		if (modeServer != GuiServerMode.SERVER_WITH_GAME)
		{
			return;
		}
		
		try {
			connectToGame("rmi://"+RMIUtils.getLocalHost().getHostAddress()+"/GAME_testServer_"+serverName);
			
		} catch (Exception e) {
			displayError("Error while connecting to game, sorry", e);
		} 
		
		if (modeClient != GuiClientMode.MONITOR)
			return;
		
		try {
			Integer id = clientCore.getGame().addPlayer("Observer", Avatars.getAvailableAvatars().get(0));
			setPlayerId(id);
			setClientMode(GuiClientMode.PLAYING);

		} catch (Exception e) {
			displayError("Error while adding this player, sorry", e);
		} 
		
		clickAddBotManager();
	}
	
	private void clickJoin() {
		
		GuiCreatePlayer guiConnect = new GuiCreatePlayer(this.clientCore, display);
		guiConnect.init();
		guiConnect.run();
	
		String name = guiConnect.getName();
		String avatar = guiConnect.getAvatarFile();
		
		if ( (name == null) || (avatar == null) )
			return;
		
		try {
			Integer id = clientCore.getGame().addPlayer(name, avatar);
			setPlayerId(id);
			setClientMode(GuiClientMode.PLAYING);

		} catch (Exception e) {
			displayError("Error while adding this player, sorry", e);
		} 
	
		
	}

	private void clickEditVisuParams() {

		if (display.isDisposed())
			return;

		GUIParameters guiParams = new GUIParameters(display, "Visu Parameters", GUIParametersMode.STANDALONE);
		guiParams.init(clientWhiteboard.getParameters());
		guiParams.refreshDirect();	
		guiParams.openDirect();		

	}

	public void clickCreateIdea() {

		if (display.isDisposed())
			return;

		GuiCreateIdea guiCreateIdea = new GuiCreateIdea(
											getPlayerId(),
											display, 
											clientCore.getGame()
											);
		
		guiCreateIdea.init(
				clientWhiteboard.getSelectionControl().getSelectedIdeas(),
				clientWhiteboard.getSelectionControl().getSelectedItems()
				);
		
		guiCreateIdea.run();

		if (guiCreateIdea.getResultName() == null) 
			return; // user probably cancelled
		
		try {
			clientCore.getGame().addIdea(
					getPlayerId(), 
					guiCreateIdea.getResultName(), 
					guiCreateIdea.getResultDesc(),
					guiCreateIdea.getResultItemIds(), 
					guiCreateIdea.getResultParentIds()
					);
		} catch (Exception e) {
			displayError("error while creating the idea", e);
		}

	}
	
	public void clickVoteOnIdea() {

		if (display.isDisposed())
			return;

		if ((clientWhiteboard.getSelectionControl().getSelectedIdeas().size()+
				clientWhiteboard.getSelectionControl().getSelectedIdeas().size())==0)
			return;
		
		GuiCreateComment guicomment = new GuiCreateComment(
											getPlayerId(),
											display, 
											clientCore.getGame(),
											clientCore
											);
		
		guicomment.init(
				clientWhiteboard.getSelectionControl().getSelectedIdeas(),
				clientWhiteboard.getSelectionControl().getSelectedComments()
				);
		
		guicomment.run();

		if (guicomment.getResultName() == null) 
			return; // user cancelled
		
/*		try {
			clientCore.getGame().addIdea(
					getPlayerId(), 
					guiCreateIdea.getResultName(), 
					guiCreateIdea.getResultItemIds(), 
					guiCreateIdea.getResultParentIds()
					);
		} catch (Exception e) {
			displayError("error while creating the idea", e);
		}*/

	}
	
	private void clickCreateItem() {

		if (display.isDisposed())
			return;
		
		GuiCreateItem.manageCreateItem(display, clientCore.getGame(), getPlayerId());
		
	}
	
	protected void displayError(String msg, Exception e) {
		
		e.printStackTrace();
		
		MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		
		mb.setText("Error");
		mb.setMessage(msg+"\n"+e.getMessage());
		
		mb.open();
		
		
	}
	
	private void clickCreateGame() {

		if (display.isDisposed())
			return;

		GuiCreateGame guiCreateGame = new GuiCreateGame(display);
		guiCreateGame.init();
		guiCreateGame.run();
		
		String gameName = guiCreateGame.getGameName(); 
//		.replaceAll("![a-zA-Z0-9_]", "_");
		// TODO constrain the format 
		String gameTheme = guiCreateGame.getGameTheme();
		
		if (gameName == null) 
			return;
		
		
		try {
			System.out.println("create game : " + gameName + "," + gameTheme);
			gameServer.createGame(gameName, gameTheme);
		} catch (Exception e) {
			displayError("Error while creating this game, sorry", e);
		} 
		
		setServerMode(GuiServerMode.SERVER_WITH_GAME);

		//GuiCreateIdea guiCreateIdea = new GuiCreateIdea(compositeHost);
	}

	public void run() {


		shell.open ();
		//ClientWhiteboardSWT.simulateData(clientWhiteboard, game);

		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) 
				display.sleep ();
		}
		display.dispose ();
		
		if (botManager != null)
		{
			botManager.close();
		}

	}

	/**
	 * Connects to this game.
	 * 
	 * @param gameBindName
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws NotBoundException
	 * @throws UnknownHostException
	 */
	public void connectToGame(String gameBindName) throws MalformedURLException, RemoteException, NotBoundException, UnknownHostException {
		
		try {
			clientCore.connectToGame(gameBindName);
			
			clientWhiteboard.setGame(clientCore.getGame());
			
			clientCore.getGame().addListener(this);
			
			setClientMode(GuiClientMode.MONITOR);
			this.gameBindName = gameBindName;
					
		} catch (RuntimeException e) {
		
			// update info relative to the game.
			setClientMode(GuiClientMode.DISCONNECTED);
			this.gameBindName = null;
		
			throw e;
		}
	}

	private void updateShellTitle() {
		
		if (shell.isDisposed())
			return;	
		
		String post = "";
		
		switch (modeServer) {
			case SERVER_WITH_GAME:
			case SERVER_WITHOUT_GAME:
				post += " [serveur]";
				break;
			case NO_SERVER:
				break;
			default:
				throw new RuntimeException("Not managed :" + modeServer);
		}
		
		

		switch (modeClient) {
			case DISCONNECTED:
				post += " [non connecté]";
				break;
			case MONITOR:
				post += " [observation]";
				break;
			case PLAYING:
				post += " [joueur]";
				break;
			default:
				throw new RuntimeException("Not managed :" + modeClient);
		}
		
		shell.setText(TXT_WINDOW_TITLE+post);
		
		
	}
	
	private void setClientMode(GuiClientMode novelMode) {
		this.modeClient = novelMode;
		
		if (display.isDisposed())
			return;
		
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				updateShellTitle();
				updateStates();	
			}
		});	
	}
	
	private void setServerMode(GuiServerMode novelMode) {
		this.modeServer = novelMode;
		updateShellTitle();
		updateStates();
	}
	
	public void setPlayerId(Integer id) {
		this.playerId = id;
		this.clientCore.setPlayerId(id);
		
	}
	
	public Integer getPlayerId() {
		
		if (modeClient != GuiClientMode.PLAYING)
			throw new RuntimeException("Cannot get player ID when player not connected.");
		
		return playerId;
	}

	/**
	 * NB: should always be called from the SWT thread. 
	 */
	public void majScores()
	{
		if (modeClient != GuiClientMode.PLAYING)	// quick exit
			return;
		
		try {
		if (clientCore.getGame().getPlayer(getPlayerId())!=null)
	{
		int i=0;
		@SuppressWarnings("unused")
		Map<Integer, Double>  sctab,sccop;
		for (TypeScore sc: TypeScore.values())
		{
				sctab=sc.calculer(clientCore.getGame());
				
				double scval=sctab.get(getPlayerId());
				//ArrayList val=new ArrayList(sctab.values());
				//Collections.sort(val);
				
				SortedSet<PlayersScores> sortedScores = PlayersScores.calculer(sctab);
	
				int rgval=PlayersScores.computeRank(sortedScores, getPlayerId());
				StringBuilder sb = new StringBuilder(sc.nom+'\n');
				@SuppressWarnings("unused")
				int idj;
				int rankCurrent = 1;
				for (PlayersScores currentScore : sortedScores) {
					sb.append(rankCurrent);
					sb.append(": ");
					sb.append(clientCore.getGame().getPlayer(currentScore.idplayer).getShortName());
					sb.append(" (");
					sb.append(currentScore.score);
					sb.append(")\n");
					rankCurrent++;
				}
				TXT_TOOLTIP_HIGH_SCORE.set(i, sb.toString());
				
				scoreslab[i].setText(sc.nom+"");
				scores[i].setText(" "+(int)scval);
//				scoreevo[i].setText(" (=)");
				if (scvalprec.get(i)>scval)
				{
					scoreevo[i].setText("("+((int)(scval-scvalprec.get(i)))+")");
					scoreevo[i].setForeground(display.getSystemColor(SWT.COLOR_RED));
					
				}
/*				if (scvalprec.get(i)==scval)
				{
					scoreevo[i].setText(" (=)");
					scoreevo[i].setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
					
				}*/
				if (scvalprec.get(i)<scval)
				{
					scoreevo[i].setText("(+"+((int)(scval-scvalprec.get(i)))+")");
					scoreevo[i].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
					
				}
				rglab[i].setText(" Rg ");
				rg[i].setText(""+rgval);
				if (scrgprec.get(i)>rgval)
				{
					rgevo[i].setText("("+((int)(rgval-scrgprec.get(i)))+")");
					rgevo[i].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
					
				}
/*				if (scrgprec.get(i)==rgval)
				{
					rgevo[i].setText(" (=)");
					rgevo[i].setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
					
				}*/
				if (scrgprec.get(i)<rgval)
				{
					rgevo[i].setText("(+"+((int)(rgval-scrgprec.get(i)))+")");
					rgevo[i].setForeground(display.getSystemColor(SWT.COLOR_RED));
					
				}
				scvalprec.put(i, scval);
				scrgprec.put(i, (double)rgval);
				scores[i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(i));
				scoreslab[i].setToolTipText(sc.tooltip);
			i++;
			
		}
		
		
	}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		//System.setSecurityManager(new RMISecurityManager());
		try {
			GuiTestMain t = new GuiTestMain();

			t.init();

			t.run();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SimAnalyzerLog.generateSimAnalyzerLog();
			//to ensure that whatever happens, system shutdowns in the proper way
			Runtime.getRuntime().exit(0);
		}
		
		
	}

	@Override
	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
		
	}

	@Override
	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
		
	}

	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException {
		
	}

	@Override
	public void endOfGame() throws RemoteException {
		setClientMode(GuiClientMode.DISCONNECTED);
	}

	@Override
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {
		
		// we should also update the tree of comments
		display.asyncExec(ideaCommentTreeFillerRunnable);
		
	}

	@Override
	public void playerJoined(PlayerEvent e) throws RemoteException {
	}

	@Override
	public void playerLeft(PlayerEvent e) throws RemoteException {
	}
	
	
}
