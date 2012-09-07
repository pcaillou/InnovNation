package client.gui;

import java.awt.Font; 
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import client.DelegatingBotCore;
import client.gui.prefuse.GuiClientMode;
import data.IComment;
import data.IIdea;
import data.IPlayer;
import errors.AlreadyExistsException;
import errors.TooLateException;
import events.IEventListener;
import functions.IGame;
import functions.TypeScore;

public class GuiScores extends Thread{
	
	public static final int PERIOD_REFRESH_SCORES = 1000; // ms
	private static ArrayList<String> TXT_TOOLTIP_HIGH_SCORE;	

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger("client.gui.connect"); // => unused
	
	private Shell shell = null;
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Composite compositeHost = null;
	private Display display = null;
		
	/* labels */
	private Label[] tabtitle,tabdesc1,tabdesc2;
	private Label[][] scores,scoreslab,rg,rglab,scoreevo,rgevo;
	private Map<Integer, Double>  scvalprec[];
	private Map<Integer, Double>  scrgprec[];
	
	static final int NBTAB=8;
	static final int NBPERTAB=6;
	
	/* Adresse du serveur */
	private String server = null;
	
	/* Listener global */
	private IEventListener listener;
	private GuiTestMain main;
	
	private final static int LOOK_MIN_WIDTH = 100;
	private final static int LOOK_MIN_HEIGHT = 100;
	
	private Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;
	
	/* EmbbedType des composants */
	private GuiEmbbedType embbedType;
	
	/* onglet principal */
	TabFolder tab;
	
	/* Runnable servant a refresh la fenetre */
	private Runnable refresh;
	
	/**
	 * Cree le Gui du bot
	 * @param IEventListener : le listener du bot
	 * @param host : adresse du serveur
	 * @param _display : displayeur
	 * @param main : main listener
	 */
	public GuiScores(IEventListener _listener, String host,  Display _display, GuiTestMain _main) {
		
		//clientCore = new DelegatingBotCore(listener);
		listener = _listener;
		main = _main;
		embbedType = GuiEmbbedType.STANDALONE;
		compositeHost = null;
		display = _display;
		server = host;
		initColors();

		/* initialisation de la fonction refresh */
		refresh = new Runnable() {
            public void run() {
            	if (!shell.isDisposed ()) {
            		refresh();
            	}
            }
         };

	}

	/**
	 * Initialise la couleur des fenetres
	 */
	private final void initColors() {
		if (display == null)
			throw new RuntimeException("unable to init colors prior to display assignment");
		
		LOOK_COLOR_BACKGROUND_MAINSPACE = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		LOOK_COLOR_BACKGROUND_SUBSPACES = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}
	
	/** 
	 * Met a jour l'etat des boutons 
	 */
	protected void updateButtonsStates() {
	}
	
	/**
	 * Initialise la fenetre du bot
	 */
	public void init() {

		
		
		
		if (embbedType == GuiEmbbedType.STANDALONE) 
		{
			shell = new Shell (display, SWT.SHELL_TRIM);
			shell.setText ("Classements");
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

		Composite compositeScoresTabs = new Composite(compositeHost, SWT.HORIZONTAL);
		compositeScoresTabs.setLayout(new GridLayout(4,false));
		compositeScoresTabs.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
		
		//scores
		tabtitle=new Label[NBTAB];
		tabdesc1=new Label[NBTAB];
		tabdesc2=new Label[NBTAB];
		scores=new Label[NBTAB][NBPERTAB+1];
		scoreslab=new Label[NBTAB][NBPERTAB+1];
		rg=new Label[NBTAB][NBPERTAB+1];
		rglab=new Label[NBTAB][NBPERTAB+1];
		scoreevo=new Label[NBTAB][NBPERTAB+1];
		rgevo=new Label[NBTAB][NBPERTAB+1];
		int t=0;
		scvalprec=new HashMap[NBTAB];
		scrgprec=new HashMap[NBTAB];
		for (int i=0; i<NBTAB; i++)
		{
			scvalprec[i] = new HashMap<Integer, Double>(TypeScore.values().length, 1.0f);
			scrgprec[i] = new HashMap<Integer, Double>(TypeScore.values().length, 1.0f);				
		}
		// AD TXT_TOOLTIP_HIGH_SCORE=new ArrayList(TypeScore.values().length); /*
		TXT_TOOLTIP_HIGH_SCORE=new ArrayList<String>(TypeScore.values().length);// */
		
		for (TypeScore sc: TypeScore.values())
		{
			
			Composite compositeScores = new Composite(compositeScoresTabs, SWT.NONE);
			compositeScores.setLayout(new GridLayout(1,false));
			compositeScores.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
			
			

			tabtitle[t] = new Label(compositeScores, SWT.READ_ONLY);
			tabtitle[t].setText(sc.nom.toUpperCase());
			tabtitle[t].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			tabtitle[t].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
//			tabtitle[t].setSize(30, 10);
//			tabdesc1[t].setText("");
//			tabdesc2[t].setText("");

			//				compositeSc.setLayout(new RowLayout(SWT.HORIZONTAL));
				Composite compositeSc = new Composite(compositeScores, SWT.NONE);
				GridLayout layoutName = new GridLayout(6, false);
				GridData gdSc = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
				compositeSc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				compositeSc.setLayout(layoutName);
				compositeSc.setLayoutData(gdSc);

							
				
				for (int i=0; i<NBPERTAB;i++)
				{
//					System.out.println("t"+t+"i"+i);
				scoreslab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scoreslab[t][i].setText(sc.nom+":");
				scoreslab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scoreslab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				scoreslab[t][i].setToolTipText(sc.tooltip);
				scores[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scores[t][i].setText(" 0000");
				scores[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scores[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				TXT_TOOLTIP_HIGH_SCORE.add(i, "High Scores");
				scores[t][i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(t));
				scoreevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scoreevo[t][i].setText(" (==)");
				scoreevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scoreevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rglab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rglab[t][i].setText("");
				rglab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rglab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rg[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rg[t][i].setText("");
				rg[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rg[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rgevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rgevo[t][i].setText("");
				rgevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rgevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				}
				t++;
		}
			
		Composite compositeScores;
			Composite compositeSc;
			GridLayout layoutName;
			GridData gdSc;
			
		 compositeScores = new Composite(compositeScoresTabs, SWT.NONE);
		compositeScores.setLayout(new GridLayout(1,false));
		compositeScores.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
		
		

		tabtitle[t] = new Label(compositeScores, SWT.READ_ONLY);
		tabtitle[t].setText("MES IDEE");
		tabtitle[t].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
		tabtitle[t].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
//		tabtitle[t].setSize(30, 10);
//		tabdesc1[t].setText("");
//		tabdesc2[t].setText("");

		//				compositeSc.setLayout(new RowLayout(SWT.HORIZONTAL));
			 compositeSc = new Composite(compositeScores, SWT.NONE);
			 layoutName = new GridLayout(6, false);
			 gdSc = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
			compositeSc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeSc.setLayout(layoutName);
			compositeSc.setLayoutData(gdSc);

						
			
			for (int i=0; i<NBPERTAB;i++)
			{
//				System.out.println("t"+t+"i"+i);
			scoreslab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			scoreslab[t][i].setText("---------------------------");
			scoreslab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			scoreslab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			scoreslab[t][i].setToolTipText("");
			scores[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			scores[t][i].setText(" 0000");
			scores[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			scores[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			TXT_TOOLTIP_HIGH_SCORE.add(i, "High Scores");
			scores[t][i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(t));
			scoreevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			scoreevo[t][i].setText(" (==)");
			scoreevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			scoreevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			rglab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			rglab[t][i].setText("");
			rglab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			rglab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			rg[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			rg[t][i].setText("");
			rg[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			rg[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			rgevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			rgevo[t][i].setText("");
			rgevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			rgevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			}
			t++;

		
			 compositeScores = new Composite(compositeScoresTabs, SWT.NONE);
			compositeScores.setLayout(new GridLayout(1,false));
			compositeScores.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
			
			

			tabtitle[t] = new Label(compositeScores, SWT.READ_ONLY);
			tabtitle[t].setText("MES VOTES PERTINENTS");
			tabtitle[t].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			tabtitle[t].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
//			tabtitle[t].setSize(30, 10);
//			tabdesc1[t].setText("");
//			tabdesc2[t].setText("");

			//				compositeSc.setLayout(new RowLayout(SWT.HORIZONTAL));
				 compositeSc = new Composite(compositeScores, SWT.NONE);
				 layoutName = new GridLayout(6, false);
				 gdSc = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
				compositeSc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				compositeSc.setLayout(layoutName);
				compositeSc.setLayoutData(gdSc);

							
				
				for (int i=0; i<NBPERTAB;i++)
				{
//					System.out.println("t"+t+"i"+i);
				scoreslab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scoreslab[t][i].setText("---------------------------");
				scoreslab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scoreslab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				scoreslab[t][i].setToolTipText("");
				scores[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scores[t][i].setText(" 0000");
				scores[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scores[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				TXT_TOOLTIP_HIGH_SCORE.add(i, "High Scores");
				scores[t][i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(t));
				scoreevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scoreevo[t][i].setText(" (==)");
				scoreevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scoreevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rglab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rglab[t][i].setText("");
				rglab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rglab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rg[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rg[t][i].setText("");
				rg[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rg[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rgevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rgevo[t][i].setText("");
				rgevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rgevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				}
				t++;

		
		
		 compositeScores = new Composite(compositeScoresTabs, SWT.NONE);
		compositeScores.setLayout(new GridLayout(1,false));
		compositeScores.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
		
		

		tabtitle[t] = new Label(compositeScores, SWT.READ_ONLY);
		tabtitle[t].setText("MEILLEURS IDEES");
		tabtitle[t].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
		tabtitle[t].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
//		tabtitle[t].setSize(30, 10);
//		tabdesc1[t].setText("");
//		tabdesc2[t].setText("");

		//				compositeSc.setLayout(new RowLayout(SWT.HORIZONTAL));
			 compositeSc = new Composite(compositeScores, SWT.NONE);
			 layoutName = new GridLayout(6, false);
			 gdSc = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
			compositeSc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeSc.setLayout(layoutName);
			compositeSc.setLayoutData(gdSc);

						
			
			for (int i=0; i<NBPERTAB;i++)
			{
//				System.out.println("t"+t+"i"+i);
			scoreslab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			scoreslab[t][i].setText("---------------------------");
			scoreslab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			scoreslab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			scoreslab[t][i].setToolTipText("");
			scores[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			scores[t][i].setText(" 0000");
			scores[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			scores[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			TXT_TOOLTIP_HIGH_SCORE.add(i, "High Scores");
			scores[t][i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(t));
			scoreevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			scoreevo[t][i].setText(" (==)");
			scoreevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			scoreevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			rglab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			rglab[t][i].setText("");
			rglab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			rglab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			rg[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			rg[t][i].setText("");
			rg[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			rg[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			rgevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
			rgevo[t][i].setText("");
			rgevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			rgevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
			}
			t++;

			 compositeScores = new Composite(compositeScoresTabs, SWT.NONE);
			compositeScores.setLayout(new GridLayout(1,false));
			compositeScores.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
			
			

			tabtitle[t] = new Label(compositeScores, SWT.READ_ONLY);
			tabtitle[t].setText("LES PLUS ACTIVES");
			tabtitle[t].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			tabtitle[t].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
//			tabtitle[t].setSize(30, 10);
//			tabdesc1[t].setText("");
//			tabdesc2[t].setText("");

			//				compositeSc.setLayout(new RowLayout(SWT.HORIZONTAL));
				 compositeSc = new Composite(compositeScores, SWT.NONE);
				 layoutName = new GridLayout(6, false);
				 gdSc = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
				compositeSc.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				compositeSc.setLayout(layoutName);
				compositeSc.setLayoutData(gdSc);

							
				
				for (int i=0; i<NBPERTAB;i++)
				{
//					System.out.println("t"+t+"i"+i);
				scoreslab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scoreslab[t][i].setText("---------------------------");
				scoreslab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scoreslab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				scoreslab[t][i].setToolTipText("");
				scores[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scores[t][i].setText(" 0000");
				scores[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scores[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				TXT_TOOLTIP_HIGH_SCORE.add(i, "High Scores");
				scores[t][i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(t));
				scoreevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				scoreevo[t][i].setText(" (==)");
				scoreevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				scoreevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rglab[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rglab[t][i].setText("");
				rglab[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rglab[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rg[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rg[t][i].setText("");
				rg[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rg[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				rgevo[t][i] = new Label(compositeSc, SWT.READ_ONLY);
				rgevo[t][i].setText("");
				rgevo[t][i].setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rgevo[t][i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END));
				}
				t++;

			
				
		
		
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
		
		
	
		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell.pack();
			shell.setMinimumSize(
					Math.max(shell.getSize().x, LOOK_MIN_WIDTH),
					Math.max(shell.getSize().y , LOOK_MIN_HEIGHT)
					);
			shell.open ();
			
		}
		
		/* on initialise les parametres et boutons */
		updateButtonsStates();
	}
	
	public void majScores()
	{
		if (shell == null || shell.isDisposed())
			return;
		if (main.modeClient != GuiClientMode.PLAYING)	// quick exit
			return;
		
		try {
		if (main.clientCore.getGame().getPlayer(main.getPlayerId())!=null)
	{
		int t=0;
		@SuppressWarnings("unused")
		Map<Integer, Double>  sctab[]=new HashMap[NBTAB];
		Map<Integer, Double>  sccop[]=new HashMap[NBTAB];
		for (TypeScore sc: TypeScore.values())
		{
			sctab[t]=sc.calculer(main.clientCore.getGame());
			SortedSet<PlayersScores> sortedScores = PlayersScores.calculer(sctab[t]);
		for (data.IPlayer p:main.clientCore.getGame().getAllPlayers())
		{
				
				double scval=sctab[t].get(p.getUniqueId());
				//ArrayList val=new ArrayList(sctab.values());
				//Collections.sort(val);
				
	
				int rgval=PlayersScores.computeRank(sortedScores, p.getUniqueId());
				StringBuilder sb = new StringBuilder(sc.nom+'\n');
				@SuppressWarnings("unused")
				int idj;
				int rankCurrent = 1;
					if (rgval<NBPERTAB)
					{
						scoreslab[t][rgval-1].setText(p.getShortName()+"");
						scoreslab[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_BLACK));
						scores[t][rgval-1].setText(" "+(int)scval);
//						scoreevo[i].setText(" (=)");
						if (scvalprec[t].get(p.getUniqueId())!=null)
						if (scvalprec[t].get(p.getUniqueId())>scval)
						{
							scoreevo[t][rgval-1].setText("("+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
							scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_RED));
							
						}
						if (scvalprec[t].get(p.getUniqueId())!=null)
						if (scvalprec[t].get(p.getUniqueId())<scval)
						{
							scoreevo[t][rgval-1].setText("(+"+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
							scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
							
						}
						
					}
					if ((rgval<NBPERTAB)&(p.getUniqueId()==main.getPlayerId()))
					{
						scoreslab[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_RED));
						scoreslab[t][NBPERTAB-1].setText("");
						scores[t][NBPERTAB-1].setText("");
						scoreevo[t][NBPERTAB-1].setText("");
						
					}
					
					if ((rgval>=NBPERTAB)&(p.getUniqueId()==main.getPlayerId()))
					{
						scoreslab[t][NBPERTAB-1].setForeground(display.getSystemColor(SWT.COLOR_RED));
						scoreslab[t][NBPERTAB-1].setText(p.getShortName()+"");
						scores[t][NBPERTAB-1].setText(" "+(int)scval);
//						scoreevo[i].setText(" (=)");
						if (scvalprec[t].get(p.getUniqueId())!=null)
						if (scvalprec[t].get(p.getUniqueId())>scval)
						{
							scoreevo[t][NBPERTAB-1].setText("("+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
							scoreevo[t][NBPERTAB-1].setForeground(display.getSystemColor(SWT.COLOR_RED));
							
						}
						if (scvalprec[t].get(p.getUniqueId())!=null)
						if (scvalprec[t].get(p.getUniqueId())<scval)
						{
							scoreevo[t][NBPERTAB-1].setText("(+"+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
							scoreevo[t][NBPERTAB-1].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
							
						}
						
					}
				
					scvalprec[t].put(p.getUniqueId(), scval);
					scrgprec[t].put(p.getUniqueId(), (double)rgval);
	//				scores[t][i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(i));
//					scoreslab[t][i].setToolTipText(sc.tooltip);
		}
				t++;
		}

		int nbi;
		SortedSet<PlayersScores> sortedScores; 
		 //Mes Idees
		 nbi=main.clientCore.getGame().getAllIdeas().size();
		sctab[t]=new HashMap<Integer,Double>();
		if (nbi>0)
		{
			for (IIdea p:main.clientCore.getGame().getAllIdeas())
				if (p.getPlayerId()==main.getPlayerId())
			{
				sctab[t].put(p.getUniqueId(), new Double(p.getTotalBids()));
			}
			
		}
		sortedScores = PlayersScores.calculer(sctab[t]);
		for (IIdea p:main.clientCore.getGame().getAllIdeas())
			if (p.getPlayerId()==main.getPlayerId())
	{
			
			double scval=sctab[t].get(p.getUniqueId());
			//ArrayList val=new ArrayList(sctab.values());
			//Collections.sort(val);
			

			int rgval=PlayersScores.computeRank(sortedScores, p.getUniqueId());
			@SuppressWarnings("unused")
			int idj;
			int rankCurrent = 1;
				if (rgval<=NBPERTAB)
				{
					scoreslab[t][rgval-1].setText(p.getShortName()+"");
					scores[t][rgval-1].setText(" "+(int)scval);
//					scoreevo[i].setText(" (=)");
					if (scvalprec[t].get(p.getUniqueId())!=null)
					if (scvalprec[t].get(p.getUniqueId())>scval)
					{
						scoreevo[t][rgval-1].setText("("+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
						scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_RED));
						
					}
					if (scvalprec[t].get(p.getUniqueId())!=null)
					if (scvalprec[t].get(p.getUniqueId())<scval)
					{
						scoreevo[t][rgval-1].setText("(+"+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
						scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
						
					}
					
				}
				
			
				scvalprec[t].put(p.getUniqueId(), scval);
				scrgprec[t].put(p.getUniqueId(), (double)rgval);
	}
				
  t++;	
  
	 //Pertinence des votes
	 nbi=main.clientCore.getGame().getAllIdeas().size();
	sctab[t]=new HashMap<Integer,Double>();
	if (nbi>0)
	{
		IGame g=main.clientCore.getGame();
		int cumulatedTotal=0;
		for (IIdea currentIdea:main.clientCore.getGame().getAllIdeas())
		{
				
			cumulatedTotal=0;
					
					final LinkedList<IComment> allComments = g.getAllIdeasComments(currentIdea.getUniqueId()); // this is a COPY of the comments, it will not change because of changes by another player
					
					// init the mapping between comments and a timeline based on local ids
					Map<IComment,Integer> comment2id = new HashMap<IComment, Integer>();
					{
						int previousId = 0;
						for (IComment currentComment : allComments) {
							comment2id.put(currentComment, previousId++);
					}
					}
					
					// init the various lists for players
					Map<IPlayer,ArrayList<Integer>> player2scores = new HashMap<IPlayer, ArrayList<Integer>>();
					for (IPlayer currentPlayer : g.getAllPlayers()) {
						ArrayList<Integer> list = new ArrayList<Integer>(allComments.size());
						for (int i=0; i<allComments.size();i++)
							list.add(0);
						player2scores.put(
								currentPlayer, 
								list
								);
					}
					
					// populates the lists of scores for each player
					for (IComment currentComment : allComments) {
						if (currentComment.getTokensCount() != 0) {
							// right, this one deserves some processing
							Integer localID = comment2id.get(currentComment);
							IPlayer author = g.getPlayer(currentComment.getPlayerId());
							player2scores.get(author).set(localID, currentComment.getTokensCount());
						}
					}
					
					// iterates through each list of scores and removes the negative values
					for (IPlayer currentPlayer : player2scores.keySet()) {
						LinkedList<Integer> indexesPositivesValues = new LinkedList<Integer>();
						ArrayList<Integer> playerVotes = player2scores.get(currentPlayer);
//						logger.debug("Player "+currentPlayer.getShortName()+" bids (with negative): \t "+playerVotes.toString());
						for (int i=0; i<playerVotes.size(); i++) {
							Integer currentValue = playerVotes.get(i);
							if (currentValue < 0) {
								
								// for each negative value, removes the tokens from the previous positive values
								int tokensToRemove = -currentValue;
								while (tokensToRemove > 0) {
									Integer idxLastPositiveValue = 	indexesPositivesValues.getLast();
									int valueLastVote = playerVotes.get(idxLastPositiveValue);
									int novelValue = Math.max(0, valueLastVote - tokensToRemove); 
									
									if (novelValue == 0) {
										indexesPositivesValues.removeLast(); // remove this index, nothing more there
										tokensToRemove -= valueLastVote;
									} else {
										tokensToRemove = 0;
									}
								
									playerVotes.set(idxLastPositiveValue, novelValue);
									//logger.debug("bid "+idxLastPositiveValue+"("+valueLastVote+"): now "+novelValue);
										
								}
								
								// actually remove this negative value that was already processed (removed from previous positive bids)
								playerVotes.set(i, 0);
								
								
							} else if (currentValue > 0) {
								// this is a positive vote that could be used later to remove tokens
								indexesPositivesValues.add(i);
							} // as default, nothing happens if 0
						}
						
		//				logger.debug("Player "+currentPlayer.getShortName()+" bids (removing negative): \t "+playerVotes.toString());
						
					}
					

					// well, we now have the list of positive votes, without any negative votes. 
					// we can now compute the score
					// dixit Philippe: "Maintenant, c'est facile"
					IPlayer currentPlayer = g.getPlayer(main.getPlayerId());		
					ArrayList<Integer> playerVotes = player2scores.get(currentPlayer);
					for (int i=0; i<playerVotes.size(); i++) {
						Integer currentValue = playerVotes.get(i);
						if (currentValue > 0) {
							// we have to compute the tokens from other players 
							for (IPlayer currentPlayerOther : player2scores.keySet()) {
								if (currentPlayerOther != currentPlayer) {
									ArrayList<Integer> playerVotesOther = player2scores.get(currentPlayerOther);
									for (int j=i+1; j<playerVotesOther.size(); j++) {
										cumulatedTotal += playerVotesOther.get(j)*currentValue;
									}		
								}
							}
							
						}
					}
					
		//			logger.debug("Player "+currentPlayer.getShortName()+"'s score: \t "+cumulatedTotal);
				
				
			sctab[t].put(currentIdea.getUniqueId(), new Double(cumulatedTotal));
		}
		
	}
	sortedScores = PlayersScores.calculer(sctab[t]);
	for (IIdea p:main.clientCore.getGame().getAllIdeas())
{
		
		double scval=sctab[t].get(p.getUniqueId());
		//ArrayList val=new ArrayList(sctab.values());
		//Collections.sort(val);
		

		int rgval=PlayersScores.computeRank(sortedScores, p.getUniqueId());
		@SuppressWarnings("unused")
		int idj;
		int rankCurrent = 1;
			if (rgval<=NBPERTAB)
			{
				scoreslab[t][rgval-1].setText(p.getShortName()+"");
				scores[t][rgval-1].setText(" "+(int)scval);
//				scoreevo[i].setText(" (=)");
				if (scvalprec[t].get(p.getUniqueId())!=null)
				if (scvalprec[t].get(p.getUniqueId())>scval)
				{
					scoreevo[t][rgval-1].setText("("+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
					scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_RED));
					
				}
				if (scvalprec[t].get(p.getUniqueId())!=null)
				if (scvalprec[t].get(p.getUniqueId())<scval)
				{
					scoreevo[t][rgval-1].setText("(+"+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
					scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
					
				}
				
			}
			
		
			scvalprec[t].put(p.getUniqueId(), scval);
			scrgprec[t].put(p.getUniqueId(), (double)rgval);
}
			
t++;	

		
//Scores idées		
				 nbi=main.clientCore.getGame().getAllIdeas().size();
				sctab[t]=new HashMap<Integer,Double>();
				if (nbi>0)
				{
					for (IIdea p:main.clientCore.getGame().getAllIdeas())
					{
						sctab[t].put(p.getUniqueId(), new Double(p.getTotalBids()));
					}
					
				}
				sortedScores = PlayersScores.calculer(sctab[t]);
				for (IIdea p:main.clientCore.getGame().getAllIdeas())
			{
					
					double scval=sctab[t].get(p.getUniqueId());
					//ArrayList val=new ArrayList(sctab.values());
					//Collections.sort(val);
					
		
					int rgval=PlayersScores.computeRank(sortedScores, p.getUniqueId());
					@SuppressWarnings("unused")
					int idj;
					int rankCurrent = 1;
						if (rgval<=NBPERTAB)
						{
							scoreslab[t][rgval-1].setText(p.getShortName()+"");
							scores[t][rgval-1].setText(" "+(int)scval);
//							scoreevo[i].setText(" (=)");
							if (scvalprec[t].get(p.getUniqueId())!=null)
							if (scvalprec[t].get(p.getUniqueId())>scval)
							{
								scoreevo[t][rgval-1].setText("("+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
								scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_RED));
								
							}
							if (scvalprec[t].get(p.getUniqueId())!=null)
							if (scvalprec[t].get(p.getUniqueId())<scval)
							{
								scoreevo[t][rgval-1].setText("(+"+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
								scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
								
							}
							scoreslab[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_BLACK));
							if ((p.getPlayerId()==main.getPlayerId()))
							{
								scoreslab[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_RED));								
							}
							
						}
						
					
						scvalprec[t].put(p.getUniqueId(), scval);
						scrgprec[t].put(p.getUniqueId(), (double)rgval);
			}
						
		   t++;

		   
		 //Idees actives
			 nbi=main.clientCore.getGame().getAllIdeas().size();
			sctab[t]=new HashMap<Integer,Double>();
			if (nbi>0)
			{
				for (IIdea p:main.clientCore.getGame().getAllIdeas())
				{
					sctab[t].put(p.getUniqueId(), new Double(main.clientCore.getGame().getIdeaMainComments(p.getUniqueId()).size()));
				}
				
			}
			sortedScores = PlayersScores.calculer(sctab[t]);
			for (IIdea p:main.clientCore.getGame().getAllIdeas())
		{
				
				double scval=sctab[t].get(p.getUniqueId());
				//ArrayList val=new ArrayList(sctab.values());
				//Collections.sort(val);
				
	
				int rgval=PlayersScores.computeRank(sortedScores, p.getUniqueId());
				@SuppressWarnings("unused")
				int idj;
				int rankCurrent = 1;
					if (rgval<=NBPERTAB)
					{
						scoreslab[t][rgval-1].setText(p.getShortName()+"");
						scores[t][rgval-1].setText(" "+(int)scval);
//						scoreevo[i].setText(" (=)");
						if (scvalprec[t].get(p.getUniqueId())!=null)
						if (scvalprec[t].get(p.getUniqueId())>scval)
						{
							scoreevo[t][rgval-1].setText("("+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
							scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_RED));
							
						}
						if (scvalprec[t].get(p.getUniqueId())!=null)
						if (scvalprec[t].get(p.getUniqueId())<scval)
						{
							scoreevo[t][rgval-1].setText("(+"+((int)(scval-scvalprec[t].get(p.getUniqueId())))+")");
							scoreevo[t][rgval-1].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
							
						}
						
					}
					
				
					scvalprec[t].put(p.getUniqueId(), scval);
					scrgprec[t].put(p.getUniqueId(), (double)rgval);
		}
					
	   t++;
		   
	   
   
   //				scores[t][i].setToolTipText(TXT_TOOLTIP_HIGH_SCORE.get(i));
//						scoreslab[t][i].setToolTipText(sc.tooltip);
		
		

				/*				if (scvalprec.get(i)==scval)
				{
					scoreevo[i].setText(" (=)");
					scoreevo[i].setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
					
				}*/
/*				rglab[i].setText(" Rg ");
				rg[i].setText(""+rgval);
				if (scrgprec.get(i)>rgval)
				{
					rgevo[i].setText("("+((int)(rgval-scrgprec.get(i)))+")");
					rgevo[i].setForeground(display.getSystemColor(SWT.COLOR_BLUE));
					
				}*/
/*				if (scrgprec.get(i)==rgval)
				{
					rgevo[i].setText(" (=)");
					rgevo[i].setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
					
				}*/
/*				if (scrgprec.get(i)<rgval)
				{
					rgevo[i].setText("(+"+((int)(rgval-scrgprec.get(i)))+")");
					rgevo[i].setForeground(display.getSystemColor(SWT.COLOR_RED));
					
				}*/
			
		
		
	}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void refresh()
	{
	}
	
	/**
	 * Demarre la fenetre du but
	 */
	public void run() {
		if (embbedType == GuiEmbbedType.STANDALONE) {

			while (!shell.isDisposed ()) {
				/* on raffraichit le bot */
				
				/* on raffraichir l'affichage */
				Display.getDefault().asyncExec(refresh);
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.err.println("ScoreManager error : impossible d'executer la fonction sleep");
					e.printStackTrace();
				}
			}
		}
		
		close();
		
	}
	
	
	/**
	 * Ferme la fenetre du bot, et le deconnecte
	 */
	public void close() {
		/* on ferme le shell */
		if (shell != null && !shell.isDisposed()) {
			shell.close();
			shell.dispose();
		}
		
		/* on deconnecte le bot */
	}
	
	
}
