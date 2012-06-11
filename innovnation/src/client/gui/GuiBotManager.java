package client.gui;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import errors.AlreadyExistsException;
import errors.TooLateException;
import events.IEventListener;

public class GuiBotManager extends Thread{
	
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger("client.gui.connect"); // => unused
	
	private Shell shell = null;
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Composite compositeHost = null;
	private Display display = null;
	
	private int selectedBot;
	
	/* liste des bots */
	private static ArrayList<DelegatingBotCore> bots = new ArrayList<DelegatingBotCore>();
	
	/* liste des fenetres bot separee */
	private ArrayList<GuiBot> botsWindows;
	
	/* Adresse du serveur */
	private String server = null;
	
	/* Indique si des parametres ont ete modifies */
	private boolean paramsChanged;
	
	/* Listener global */
	private IEventListener listener;
	private IEventListener main;
	
	private final static int LOOK_MIN_WIDTH = 100;
	private final static int LOOK_MIN_HEIGHT = 100;

	public static boolean ALLOW_IDEA_CREATION = true;
	
	/* constantes de la description du bot */
	private final static String TXT_BOT_QUANTITY = "Nombre de bots :  ";
	private final static String TXT_BOT_IDEAS = "Nombre d'idees :  ";
	private final static String TXT_BOT_VOTES = "Nombre de commentaires :  ";
	private final static String TXT_ROLE = "Role : \t\t";
	private final static String TXT_BOT = "Bot :\t\t";
	private final static String TXT_UPTIME = "UpTime :\t";
	private final static String TXT_NBIDEAS = "Idees :\t\t";
	private final static String TXT_NBCOMMENTS = "Commentaires :\t";
	private final static String TXT_TOKENS = "Tokens :\t\t";
	private final static String TXT_ADAPTATION = "Adaptation :\t"; 
	private final static String TXT_CREATIVITY = "Creativite :\t";  
	private final static String TXT_PERSUATION = "Persuation :\t";  
	private final static String TXT_RELEVANCE = "Pertinence :\t";   
	
	private final static String TOOLTIP_BOT_QUANTITY = "Nombre de bots actuellement controles par le manager";
	private final static String TOOLTIP_BOT_IDEAS = "Nombre d'idees creees par les bots";
	private final static String TOOLTIP_BOT_VOTES = "Nombre de votes effectues par les bots";
	private final static String TOOLTIP_ROLE = "Serveur de la partie";
	private final static String TOOLTIP_BOT = "Nom du bot";
	private final static String TOOLTIP_UPTIME = "Temps ecoule depuis la creation";
	private final static String TOOLTIP_NBIDEAS = "Nombre d'idees crees";
	private final static String TOOLTIP_NBCOMMENTS = "Nombre de commentaires crees";
	private final static String TOOLTIP_TOKENS = "Nombre de tokens restant";
	private final static String TOOLTIP_ADAPTATION = "Taux d'adaptation calcule"; 
	private final static String TOOLTIP_CREATIVITY = "Taux de creativite calcule";  
	private final static String TOOLTIP_PERSUATION = "Taux de persuation calcule";  
	private final static String TOOLTIP_RELEVANCE = "Taux de pertinence calcule"; 
	
	/* constantes des parametres */
	private final static String TEXT_ROLE = "Role :\t\t";
	private final static String TEXT_CREATIVITY = "Creativite :\t";
	private final static String TEXT_RELEVANCE =  "Pertinence :\t";
	private final static String TEXT_ADAPTATION = "Adaptation :\t";
	private final static String TEXT_PERSUATION = "Persuation :\t";

	private final static String TOOLTIP_PROLE = "Role predefinis du bot";
	private final static String TOOLTIP_PCREATIVITY = "Taux de creativite des idees\n"
			+"1 : peu creatif - 10 : tres creatif";
	private final static String TOOLTIP_PRELEVANCE = "Taux de pertinence des idees et commentaires\n"
			+"1 : peu pertinent - 10 : tres pertinent";
	private final static String TOOLTIP_PADAPTATION = "Taux d'adaptation face aux idees et commentaires\n"
			+"1 : peu adaptatif - 10 : tres adaptatif";
	private final static String TOOLTIP_PPERSUATION = "Taux de persuation des idees et commentaires\n"
			+"1 : peu persuasif - 10 : tres persuasif";
	
	/* constantes des boutons */
	private final static String TXT_UPDATE = "Appliquer parametres";
	private final static String TXT_CANCEL = "Annuler parametres";
	private final static String TXT_REMOVE = "Supprimer bot";
	private final static String TXT_SPLIT = "Separer fenetre";
	private final static String TXT_ADD = "Ajouter bot";
	private final static String TXT_START = "Demarrer bot";
	private final static String TXT_PAUSE = "Stopper bot";
	private final static String TXT_START_ALL = "Demarrer tous";
	private final static String TXT_PAUSE_ALL = "Stopper tous";
	private final static String TXT_REMOVE_ALL = "Supprimer tous";
	private final static String TXT_ALLOW_IDEA = "Autoriser idees";
	private final static String TXT_FORBID_IDEA = "Interdire idees";
	private final static String TXT_ADD_TEN = "Ajouter 10 bots";
		
	private final static String FIELD_DEFAULT_TEXT = "                                                                                         ";
	
	private Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;
	
	/* EmbbedType des composants */
	private GuiEmbbedType embbedType;
	
	/* onglet principal */
	TabFolder tab;
	
	/* Liste des labels */
	private Label labelDRole;
	private Label labelName;
	private Label labelUpTime;
	private Label labelNbIdeas;
	private Label labelNbComments;
	private Label labelNbTokens;
	private Label labelRole;
	private Label labelAdaptation;
	private Label labelCreativity;
	private Label labelRelevance;
	private Label labelPersuation;
	private Label labelBotQuantity;
	private Label labelBotIdeas;
	private Label labelBotVotes;
	
	/* tableau d'affichage des heuristiques */
	private Table heuristicsTable;
	
	/* list des textbox */
	private Combo textRole;
	private Combo textCreativity;
	private Combo textRelevance;
	private Combo textAdaptation;
	private Combo textPersuasion;
	private Combo textBotChoice;
	
	/* liste des "bloqueurs" pour les combobox (pour empêcher leurs changement pendant qu'on les utilise)*/
	private boolean focusRole;
	private boolean focusCreativity;
	private boolean focusRelevance;
	private boolean focusAdaptation;
	private boolean focusPersuasion;
	
	/* Liste des boutons */
	private Button buttonCancel;
	private Button buttonUpdate;
	private Button buttonRemove;
	private Button buttonAdd;
	private Button buttonAddTen;
	private Button buttonAllowIdeas;
	private Button buttonPause;
	private Button buttonSplit;
	private Button buttonPauseAll;
	private Button buttonStartAll;
	private Button buttonRemoveAll;
	
	/* Runnable servant a refresh la fenetre */
	private Runnable refresh;
	
	/**
	 * Cree le Gui du bot
	 * @param IEventListener : le listener du bot
	 * @param host : adresse du serveur
	 * @param _display : displayeur
	 * @param main : main listener
	 */
	public GuiBotManager(IEventListener _listener, String host,  Display _display, IEventListener _main) {
		
		//clientCore = new DelegatingBotCore(listener);
		listener = _listener;
		main = _main;
		embbedType = GuiEmbbedType.STANDALONE;
		compositeHost = null;
		display = _display;
		server = host;
		paramsChanged = false;
		botsWindows = new ArrayList<GuiBot>();
		selectedBot = -1;
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
		buttonCancel.setEnabled(paramsChanged);
		buttonUpdate.setEnabled(paramsChanged);
		
		if (ALLOW_IDEA_CREATION)
		{
			buttonAllowIdeas.setText(TXT_FORBID_IDEA);
		}
		else
		{
			buttonAllowIdeas.setText(TXT_ALLOW_IDEA);
		}
		
		if (selectedBot == -1)
		{
			buttonPause.setEnabled(false);
			buttonRemove.setEnabled(false);
			buttonSplit.setEnabled(false);
			buttonPause.setText(TXT_START);
		}
		else
		{
			buttonPause.setEnabled(true);
			buttonRemove.setEnabled(true);
			buttonSplit.setEnabled(true);		
			if (bots.get(selectedBot).isPaused())
			{
				buttonPause.setText(TXT_START);
			}
			else
			{
				buttonPause.setText(TXT_PAUSE);
			}
		}
		
		textBotChoice.setEnabled(bots.size() != 0);
	}
	
	/**
	 * Initialise la fenetre du bot
	 */
	public void init() {

		if (embbedType == GuiEmbbedType.STANDALONE) 
		{
			shell = new Shell (display, SWT.SHELL_TRIM);
			shell.setText ("Bot Manager");
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

		/* partie nombre de bots */
		{
			RowLayout layoutTop = new RowLayout(SWT.HORIZONTAL);
			
			Composite compositeBotChoice = new Composite(compositeHost, SWT.NONE);
			compositeBotChoice.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeBotChoice.setLayout(layoutTop);
			compositeBotChoice.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			
			labelBotQuantity = new Label(compositeBotChoice, SWT.PUSH);
			labelBotQuantity.setText(TXT_BOT_QUANTITY + FIELD_DEFAULT_TEXT.substring(70));
			labelBotQuantity.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelBotQuantity.setToolTipText(TOOLTIP_BOT_QUANTITY);
			
		}

		/* partie nombre d'idees */
		{
			RowLayout layoutTop = new RowLayout(SWT.HORIZONTAL);
			
			Composite compositeBotChoice = new Composite(compositeHost, SWT.NONE);
			compositeBotChoice.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeBotChoice.setLayout(layoutTop);
			compositeBotChoice.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			
			
			labelBotIdeas = new Label(compositeBotChoice, SWT.PUSH);
			labelBotIdeas.setText(TXT_BOT_IDEAS + FIELD_DEFAULT_TEXT.substring(70));
			labelBotIdeas.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelBotIdeas.setToolTipText(TOOLTIP_BOT_IDEAS);
			
		}
		
		/* partie nombre de commentaires */
		{
			RowLayout layoutTop = new RowLayout(SWT.HORIZONTAL);
			
			Composite compositeBotChoice = new Composite(compositeHost, SWT.NONE);
			compositeBotChoice.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeBotChoice.setLayout(layoutTop);
			compositeBotChoice.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			
			labelBotVotes = new Label(compositeBotChoice, SWT.PUSH);
			labelBotVotes.setText(TXT_BOT_VOTES + FIELD_DEFAULT_TEXT.substring(70));
			labelBotVotes.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelBotVotes.setToolTipText(TOOLTIP_BOT_VOTES);
			
		}
		/* on ajoute les boutons globaux */
		{

			RowLayout layoutBottom = new RowLayout(SWT.HORIZONTAL);
						
			Composite compositeButtons = new Composite(compositeHost, SWT.NONE);
			compositeButtons.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeButtons.setLayout(layoutBottom);
			compositeButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

			buttonAdd = new Button(compositeButtons, SWT.PUSH);
			buttonAdd.setText(TXT_ADD);
			buttonAdd.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent e) {
					clickAddBot();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					clickAddBot();
				}
			});

			buttonAddTen = new Button(compositeButtons, SWT.PUSH);
			buttonAddTen.setText(TXT_ADD_TEN);
			buttonAddTen.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent e) {
					for (int i = 0 ; i < 10 ; i++)
					{
						clickAddBot();
					}
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					for (int i = 0 ; i < 10 ; i++)
					{
						clickAddBot();
					}
				}
			});
			
			buttonRemoveAll = new Button(compositeButtons, SWT.PUSH);
			buttonRemoveAll.setText(TXT_REMOVE_ALL);
			buttonRemoveAll.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					clickRemoveAll();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					clickRemoveAll();
				}
			});
			
		}
		
		/* on ajoute la suite des boutons globaux */
		{

			RowLayout layoutBottom = new RowLayout(SWT.HORIZONTAL);
						
			Composite compositeButtons = new Composite(compositeHost, SWT.NONE);
			compositeButtons.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeButtons.setLayout(layoutBottom);
			compositeButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

			
			buttonStartAll = new Button(compositeButtons, SWT.PUSH);
			buttonStartAll.setText(TXT_START_ALL);
			buttonStartAll.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent e) {
					clickStartAll();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					clickStartAll();
				}
			});
			
			buttonPauseAll = new Button(compositeButtons, SWT.PUSH);
			buttonPauseAll.setText(TXT_PAUSE_ALL);
			buttonPauseAll.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent e) {
					clickPauseAll();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					clickPauseAll();
				}
			});

			buttonAllowIdeas = new Button(compositeButtons, SWT.PUSH);
			buttonAllowIdeas.setText(TXT_ALLOW_IDEA);
			buttonAllowIdeas.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent e) {
					ALLOW_IDEA_CREATION = !ALLOW_IDEA_CREATION;
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					ALLOW_IDEA_CREATION = !ALLOW_IDEA_CREATION;
				}
			});
			
			buttonRemoveAll = new Button(compositeButtons, SWT.PUSH);
			buttonRemoveAll.setText(TXT_REMOVE_ALL);
			buttonRemoveAll.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					clickRemoveAll();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					clickRemoveAll();
				}
			});
			
		}
		
		/* partie choix du bot */
		{
			RowLayout layoutTop = new RowLayout(SWT.HORIZONTAL);
			
			Composite compositeBotChoice = new Composite(compositeHost, SWT.NONE);
			compositeBotChoice.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeBotChoice.setLayout(layoutTop);
			compositeBotChoice.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			
			textBotChoice = new Combo(compositeBotChoice, SWT.PUSH);
			for (DelegatingBotCore bot : bots)
			{
				textBotChoice.add(bot.getName());
			}
			textBotChoice.addListener(SWT.Selection,new Listener() {
				public void handleEvent (Event e) {
					changeBotSelection();
				}
			});
		}	
		
		addBotMenu(compositeHost);
		
		/* on ajoute les boutons */
		{

			RowLayout layoutBottom = new RowLayout(SWT.HORIZONTAL);
						
			Composite compositeButtons = new Composite(compositeHost, SWT.NONE);
			compositeButtons.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeButtons.setLayout(layoutBottom);
			compositeButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			
			buttonPause = new Button(compositeButtons, SWT.PUSH);
			buttonPause.setText(TXT_PAUSE);
			buttonPause.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent e) {
					clickPause();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					clickPause();
				}
			});
			
			buttonRemove = new Button(compositeButtons, SWT.PUSH);
			buttonRemove.setText(TXT_REMOVE);
			buttonRemove.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					clickRemove();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					clickRemove();
				}
			});
			
			buttonSplit = new Button(compositeButtons, SWT.PUSH);
			buttonSplit.setText(TXT_SPLIT);
			buttonSplit.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					clickSplit();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					clickSplit();
				}
			});
			
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
		getParams();
		paramsChanged = false;
		changeBotSelection();
	}
	
	/**
	 * Ajoute les onglets du menu du bot
	 * @param compositeHost
	 */
	public void addBotMenu(Composite compositeHost)
	{
		/* partie onglets */
		tab = new TabFolder(compositeHost, SWT.NONE);
		TabItem itemStatus = new TabItem (tab, SWT.NONE);
		itemStatus.setText ("Status");
		TabItem itemParams = new TabItem (tab, SWT.NONE);
		itemParams.setText ("Parametres");
		TabItem itemHeuristics = new TabItem (tab, SWT.NONE);
		itemHeuristics.setText ("Heuristiques");
		
		/* partie description du bot */
		{
			/* on cree le layout */
			GridLayout layoutDescr = new GridLayout(1, false);
			Composite compositeDescr = new Composite(tab,  SWT.NONE);
			itemStatus.setControl(compositeDescr);
			compositeDescr.setBackground(LOOK_COLOR_BACKGROUND_MAINSPACE);
			compositeDescr.setLayout(layoutDescr);
			compositeDescr.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
			
			
			/* on ajoute le nom du bot */
			labelName = new Label(compositeDescr, SWT.READ_ONLY);
			labelName.setText(TXT_BOT +  FIELD_DEFAULT_TEXT);
			labelName.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelName.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelName.setToolTipText(TOOLTIP_BOT);
			
			/* on ajoute la ligne description du serveur */
			labelDRole = new Label(compositeDescr, SWT.READ_ONLY);
			labelDRole.setText(TXT_ROLE + FIELD_DEFAULT_TEXT);
			labelDRole.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelDRole.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelDRole.setToolTipText(TOOLTIP_ROLE);
			
			/* on ajoute l'uptime du bot */
			labelUpTime = new Label(compositeDescr, SWT.READ_ONLY);
			labelUpTime.setText(TXT_UPTIME + FIELD_DEFAULT_TEXT);
			labelUpTime.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelUpTime.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelUpTime.setToolTipText(TOOLTIP_UPTIME);
			
			/* on ajoute le nombre d'idees crees */
			labelNbIdeas = new Label(compositeDescr, SWT.READ_ONLY);
			labelNbIdeas.setText(TXT_NBIDEAS + FIELD_DEFAULT_TEXT);
			labelNbIdeas.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelNbIdeas.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelNbIdeas.setToolTipText(TOOLTIP_NBIDEAS);
			
			/* on ajoute le nombre de commentaires crees */
			labelNbComments = new Label(compositeDescr, SWT.READ_ONLY);
			labelNbComments.setText(TXT_NBCOMMENTS + FIELD_DEFAULT_TEXT);
			labelNbComments.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelNbComments.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelNbComments.setToolTipText(TOOLTIP_NBCOMMENTS);
			
			/* on ajoute le nombre de tokens restant */
			labelNbTokens = new Label(compositeDescr, SWT.READ_ONLY);
			labelNbTokens.setText(TXT_TOKENS + FIELD_DEFAULT_TEXT);
			labelNbTokens.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelNbTokens.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelNbTokens.setToolTipText(TOOLTIP_TOKENS);
			
			/* on ajoute la creativite */
			labelCreativity = new Label(compositeDescr, SWT.READ_ONLY);
			labelCreativity.setText(TXT_CREATIVITY + FIELD_DEFAULT_TEXT);
			labelCreativity.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelCreativity.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelCreativity.setToolTipText(TOOLTIP_CREATIVITY);
			
			/* on ajoute l'adaptation */
			labelAdaptation = new Label(compositeDescr, SWT.READ_ONLY);
			labelAdaptation.setText(TXT_ADAPTATION + FIELD_DEFAULT_TEXT);
			labelAdaptation.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelAdaptation.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelAdaptation.setToolTipText(TOOLTIP_ADAPTATION);
			
			/* on ajoute pertinence */
			labelRelevance = new Label(compositeDescr, SWT.READ_ONLY);
			labelRelevance.setText(TXT_RELEVANCE + FIELD_DEFAULT_TEXT);
			labelRelevance.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelRelevance.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelRelevance.setToolTipText(TOOLTIP_RELEVANCE);
			
			/* on ajoute la persuation */
			labelPersuation = new Label(compositeDescr, SWT.READ_ONLY);
			labelPersuation.setText(TXT_PERSUATION + FIELD_DEFAULT_TEXT);
			labelPersuation.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelPersuation.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelPersuation.setToolTipText(TOOLTIP_PERSUATION);
		}
		
		/* partie parametres */
		Label 	labelCreativity,
				labelRelevance,
				labelAdaptation,
				labelPersuation;
		{

			/* on cree le layout */
			GridLayout layoutParams = new GridLayout(1, false);
			Composite compositeParams = new Composite(tab, SWT.NONE);
			itemParams.setControl(compositeParams);
			compositeParams.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeParams.setLayout(layoutParams);
			compositeParams.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

			/* on cree le layout des lignes */
			RowLayout layoutRow = new RowLayout(SWT.HORIZONTAL);

			/* on ajoute la ligne du choix du role */
			{
				Composite rowRole = new Composite(compositeParams, SWT.NONE);
				rowRole.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rowRole.setLayout(layoutRow);
				rowRole.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				
				/* on ajoute le label */
				labelRole = new Label(rowRole, SWT.READ_ONLY);
				labelRole.setText(TEXT_ROLE);
				labelRole.setToolTipText(TOOLTIP_PROLE);
				
				/* on ajoute le champ */
				textRole = new Combo(rowRole, SWT.BORDER | SWT.READ_ONLY);
				textRole.setItems(DelegatingBotCore.roles);
				textRole.select(0);
				textRole.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						if (textRole.getSelectionIndex() == 0)
						{
							// aucun changement
						}
						else if (textRole.getSelectionIndex() == 1)
						{
							textPersuasion.select((int) (Math.random()*10));
							textCreativity.select((int) (Math.random()*10));
							textAdaptation.select((int) (Math.random()*10));
							textRelevance.select((int) (Math.random()*10));
						}
						else
						{
							textCreativity.select(DelegatingBotCore.rolesParams[textRole.getSelectionIndex()][0]-1);
							textRelevance.select(DelegatingBotCore.rolesParams[textRole.getSelectionIndex()][1]-1);
							textAdaptation.select(DelegatingBotCore.rolesParams[textRole.getSelectionIndex()][2]-1);
							textPersuasion.select(DelegatingBotCore.rolesParams[textRole.getSelectionIndex()][3]-1);
						}
						
						updateButtonsStates();
					}
				});
				textRole.addFocusListener(new FocusListener() {
					public void focusLost(FocusEvent arg0) {
						focusRole = false;
					}
					public void focusGained(FocusEvent arg0) {
						focusRole = true;
						
					}
				});
			}
			
			/* on ajoute la ligne du parametre creativity */
			{
				Composite rowExemple = new Composite(compositeParams, SWT.NONE);
				rowExemple.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rowExemple.setLayout(layoutRow);
				rowExemple.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				
				/* on ajoute le label */
				labelCreativity = new Label(rowExemple, SWT.READ_ONLY);
				labelCreativity.setText(TEXT_CREATIVITY);
				labelCreativity.setToolTipText(TOOLTIP_PCREATIVITY);
				
				/* on ajoute le champ */
				textCreativity = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textCreativity.add(i.toString());
				textCreativity.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						textRole.select(0);
						updateButtonsStates();
					}
				});
				textCreativity.addFocusListener(new FocusListener() {
					public void focusLost(FocusEvent arg0) {
						focusCreativity = false;
					}
					public void focusGained(FocusEvent arg0) {
						focusCreativity = true;
						
					}
				});
			}

			/* on ajoute la ligne du parametre relevance */
			{
				Composite rowExemple = new Composite(compositeParams, SWT.NONE);
				rowExemple.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rowExemple.setLayout(layoutRow);
				rowExemple.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				
				/* on ajoute le label */
				labelRelevance = new Label(rowExemple, SWT.READ_ONLY);
				labelRelevance.setText(TEXT_RELEVANCE);
				labelRelevance.setToolTipText(TOOLTIP_PRELEVANCE);
				
				/* on ajoute le champ */
				textRelevance = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textRelevance.add(i.toString());
				textRelevance.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						textRole.select(0);
						updateButtonsStates();
					}
				});
				textRelevance.addFocusListener(new FocusListener() {
					public void focusLost(FocusEvent arg0) {
						focusRelevance = false;
					}
					public void focusGained(FocusEvent arg0) {
						focusRelevance = true;
						
					}
				});
			}

			/* on ajoute la ligne du parametre adaptation */
			{
				Composite rowExemple = new Composite(compositeParams, SWT.NONE);
				rowExemple.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rowExemple.setLayout(layoutRow);
				rowExemple.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				
				/* on ajoute le label */
				labelAdaptation = new Label(rowExemple, SWT.READ_ONLY);
				labelAdaptation.setText(TEXT_ADAPTATION);
				labelAdaptation.setToolTipText(TOOLTIP_PADAPTATION);
				
				/* on ajoute le champ */
				textAdaptation = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textAdaptation.add(i.toString());
				textAdaptation.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						textRole.select(0);
						updateButtonsStates();
					}
				});
				textAdaptation.addFocusListener(new FocusListener() {
					public void focusLost(FocusEvent arg0) {
						focusAdaptation = false;
					}
					public void focusGained(FocusEvent arg0) {
						focusAdaptation = true;
						
					}
				});
			}

			/* on ajoute la ligne du parametre persuation */
			{
				Composite rowExemple = new Composite(compositeParams, SWT.NONE);
				rowExemple.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rowExemple.setLayout(layoutRow);
				rowExemple.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				
				/* on ajoute le label */
				labelPersuation = new Label(rowExemple, SWT.READ_ONLY);
				labelPersuation.setText(TEXT_PERSUATION);
				labelPersuation.setToolTipText(TOOLTIP_PPERSUATION);
				
				/* on ajoute le champ */
				textPersuasion = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textPersuasion.add(i.toString());
				textPersuasion.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						textRole.select(0);
						updateButtonsStates();
					}
				});
				textPersuasion.addFocusListener(new FocusListener() {
					public void focusLost(FocusEvent arg0) {
						focusPersuasion = false;
					}
					public void focusGained(FocusEvent arg0) {
						focusPersuasion = true;
						
					}
				});
			}
			
			/* on ajoute les boutons update et cancel */
			{
				Composite rowButton = new Composite(compositeParams, SWT.NONE);
				rowButton.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rowButton.setLayout(layoutRow);
				rowButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				
				buttonUpdate = new Button(rowButton, SWT.PUSH);
				buttonUpdate.setText(TXT_UPDATE);
				buttonUpdate.addSelectionListener(new SelectionListener() {
					
					public void widgetSelected(SelectionEvent e) {
						clickUpdate();
					}
					
					public void widgetDefaultSelected(SelectionEvent e) {
						clickUpdate();
					}
				});
				
				buttonCancel = new Button(rowButton, SWT.PUSH);
				buttonCancel.setText(TXT_CANCEL);
				buttonCancel.addSelectionListener(new SelectionListener() {
			
					public void widgetSelected(SelectionEvent e) {
						clickCancel();
					}
					
					public void widgetDefaultSelected(SelectionEvent e) {
						clickCancel();
					}
				});
			}
		}
		
		{

			/* on cree le layout */
			GridLayout layoutHeuristics = new GridLayout(1, false);
			Composite compositeHeuristics = new Composite(tab, SWT.NONE);
			itemHeuristics.setControl(compositeHeuristics);
			compositeHeuristics.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeHeuristics.setLayout(layoutHeuristics);
			compositeHeuristics.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
			
			heuristicsTable = new Table(compositeHeuristics, SWT.NONE);
			GridData gdGames = new GridData(
					GridData.FILL_HORIZONTAL | 
					GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
			heuristicsTable.setLayoutData(gdGames);
			heuristicsTable.setHeaderVisible (true);
			heuristicsTable.setLinesVisible (true);
			
			TableColumn column = new TableColumn (heuristicsTable, SWT.NONE);
			column.setText("Idee");
			column.setWidth(75);
			column = new TableColumn (heuristicsTable, SWT.NONE);
			column.setText("Heuristique");
			column.setWidth(175);
			column = new TableColumn (heuristicsTable, SWT.NONE);
			column.setText("Opinion");
			column.setWidth(100);
		}
	}
	
	/**
	 * Rafraichit les informations a l'ecran
	 */
	private void refresh()
	{
		labelBotQuantity.setText(TXT_BOT_QUANTITY + bots.size());
		labelBotIdeas.setText(TXT_BOT_IDEAS + DelegatingBotCore.getIdeaCount());
		labelBotVotes.setText(TXT_BOT_VOTES + DelegatingBotCore.getCommentCount());
		
		if (selectedBot == -1)
		{
			return;
		}
		
		/* on rafraichit les boutons */
		updateButtonsStates();

		/* on rafraichit les paramètres si on ne les change pas */
		if (!paramsChanged)
		{
			getParams();
		}
		
		/* on raffraichit les labels */
		labelDRole.setText(TXT_ROLE + DelegatingBotCore.roles[bots.get(selectedBot).getRole()]);
		labelUpTime.setText(TXT_UPTIME + bots.get(selectedBot).getUpTime()/1000 + " s");		
		labelNbIdeas.setText(TXT_NBIDEAS + bots.get(selectedBot).getNbIdeas());
		labelNbComments.setText(TXT_NBCOMMENTS + bots.get(selectedBot).getNbComments());
		labelNbTokens.setText(TXT_TOKENS + bots.get(selectedBot).getRemainingTokens());
		
		try 
		{
			labelAdaptation.setText(TXT_ADAPTATION + String.valueOf(bots.get(selectedBot).computeAdaptation()) + " (" + bots.get(selectedBot).computeAdaptationRank() + "/" + bots.get(selectedBot).getAllPlayersIds().size() + ")");
			labelCreativity.setText(TXT_CREATIVITY + String.valueOf(bots.get(selectedBot).computeCreativity()) + " (" + bots.get(selectedBot).computeCreativityRank() + "/" + bots.get(selectedBot).getAllPlayersIds().size() + ")");
			labelRelevance.setText(TXT_RELEVANCE + String.valueOf(bots.get(selectedBot).computeRelevance()) + " (" + bots.get(selectedBot).computeRelevanceRank() + "/" + bots.get(selectedBot).getAllPlayersIds().size() + ")");
			labelPersuation.setText(TXT_PERSUATION + String.valueOf(bots.get(selectedBot).computePersuasion()) + " (" + bots.get(selectedBot).computePersuasionRank() + "/" + bots.get(selectedBot).getAllPlayersIds().size() + ")");
			updateHeuristicTable();

		} catch (Exception e) {
			System.err.println("Error bot : error while computing bot stats (" + e.getMessage() + ")");
			//labelAdaptation.setText(TXT_ADAPTATION + "compute error");
			//labelCreativity.setText(TXT_CREATIVITY + "compute error");
			//labelRelevance.setText(TXT_RELEVANCE + "compute error");
			//labelPersuation.setText(TXT_PERSUATION + "compute error");
		}

		if (bots.get(selectedBot).isUsingSemaphore())
		{
			bots.get(selectedBot).unlock();
		}
	}
	
	/**
	 * Met a jour le tableau des heuristiques
	 * @throws RemoteException
	 */
	public void updateHeuristicTable() throws RemoteException
	{
		int selection = heuristicsTable.getVerticalBar().getSelection();
		HashMap<Integer,Long> heuristics = bots.get(selectedBot).getHeuristics();
		HashMap<Integer,Double> opinions = bots.get(selectedBot).getOpinions();
		
		/* on trie la map pour obtenir les idees ayant les plus hautes valeurs en haut */
		List<Integer> mapKeys = new ArrayList<Integer>(heuristics.keySet());
		List<Long> mapValues = new ArrayList<Long>(heuristics.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);
		  
		Collections.reverse(mapValues);
		  
		LinkedHashMap<Integer,Long> sortedHeuristics = new LinkedHashMap<Integer,Long>();
		for (Long val : mapValues) 
		{
			for (Integer key : mapKeys)
			{
				if (heuristics.get(key).equals(val)) 
				{
					//heuristics.remove(key);
					mapKeys.remove(key);
					sortedHeuristics.put(key, val);
					break;
				}
			}
		}
		
		/* si des idees ont ete ajoutees, on agrandit le tableau */
		while (sortedHeuristics.size() > heuristicsTable.getItemCount())
		{
			new TableItem(heuristicsTable, SWT.NONE);
		}
		
		int cursor = 0;
		for (Entry<Integer, Long> h : sortedHeuristics.entrySet())
		{
			heuristicsTable.getItem(cursor).setText(0, bots.get(selectedBot).getGame().getIdea(h.getKey()).getShortName());					
			heuristicsTable.getItem(cursor).setText(1, h.getValue().toString());
			heuristicsTable.getItem(cursor).setText(2, opinions.get(h.getKey()).toString());
			if (bots.get(selectedBot).getGame().getIdea(h.getKey()).getPlayerId() == bots.get(selectedBot).getPlayerId())
			{
				heuristicsTable.getItem(cursor).setBackground(new Color(Display.getCurrent(), 64,64,255));
			}
			else
			{
				heuristicsTable.getItem(cursor).setBackground(new Color(Display.getCurrent(),(int) (255*(1-opinions.get(h.getKey()))),(int) (255 * opinions.get(h.getKey())),0));
			}
			cursor++;
		}
		
		/* on redeplace la scrollBar au même niveau */
		heuristicsTable.getVerticalBar().setSelection(selection);
	}
	
	/**
	 * Change le bot actuellement selectionne
	 * @param newSelection
	 */
	public void changeBotSelection()
	{
		if (textBotChoice.getItemCount() == 0)
		{
			labelName.setText(TXT_BOT);	
			labelDRole.setText(TXT_ROLE);	
			labelUpTime.setText(TXT_UPTIME + 0 + " s");		
			labelNbIdeas.setText(TXT_NBIDEAS + 0);
			labelNbComments.setText(TXT_NBCOMMENTS + 0);
			labelNbTokens.setText(TXT_TOKENS + 0);
			labelAdaptation.setText(TXT_ADAPTATION + 0);
			labelCreativity.setText(TXT_CREATIVITY + 0);
			labelRelevance.setText(TXT_RELEVANCE + 0);
			labelPersuation.setText(TXT_PERSUATION + 0);
			heuristicsTable.clearAll();
			selectedBot = -1;
			tab.setSelection(0);
			tab.setEnabled(false);
		}
		else
		{
			if (textBotChoice.getSelectionIndex() == -1)
			{
				textBotChoice.select(0);
			}
			tab.setEnabled(true);
			selectedBot = textBotChoice.getSelectionIndex();
			labelName.setText(TXT_BOT + bots.get(selectedBot).getName());	
		}
		updateButtonsStates();
	}
	
	/**
	 * Demarre la fenetre du but
	 */
	public void run() {
		if (embbedType == GuiEmbbedType.STANDALONE) {

			while (!shell.isDisposed ()) {
				/* on raffraichit le bot */
				try{
					for (DelegatingBotCore bot : bots)
					{
						try {
							bot.refresh();
						} 
						catch (RemoteException e) 
						{
							System.err.println(bot.getName() + " : impossible de refresh (remote exception)");
							//e.printStackTrace();
						} 
						catch (TooLateException e) 
						{
							System.err.println(bot.getName() + " : impossible de refresh (too late exception)");
							//e.printStackTrace();
						} 
						catch (AlreadyExistsException e) 
						{
							System.err.println(bot.getName() + " : impossible de refresh (already exists exception)");
							DelegatingBotCore.ideaCount++;
						} 
						catch(ConcurrentModificationException e)
						{
							System.err.println(bot.getName() + " : impossible de rajouter le commentaire (concurent modification exception)");
						}
						catch(InterruptedException e)
						{
							System.err.println(bot.getName() + " : probleme de semaphore lors du lock");
						}
						catch(Exception e)
						{
							System.err.println(bot.getName() + " : erreur inconnue " + e.getClass().toString());
							e.printStackTrace();
						}
						if (bot.isUsingSemaphore())
						{
							bot.unlock();
						}
					}
				}
				catch (ConcurrentModificationException e)
				{
					System.err.println("BotManager error : impossible de lister les bots " + e.getClass().toString());
				}
				
				/* on raffraichir l'affichage */
				Display.getDefault().asyncExec(refresh);
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.err.println("BotManager error : impossible d'executer la fonction sleep");
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
		for (DelegatingBotCore bot : bots)
		{
			bot.disconnectFromGame();
		}
		
		for (GuiBot bot : botsWindows)
		{
			bot.close();
		}
		
		bots.clear();
	}
		
	/**
	 * Ajoute un nouveau bot
	 */
	private void clickAddBot()
	{
		try {
			DelegatingBotCore b = new DelegatingBotCore(listener);        
			
			/* on connecte le bot a la partie */
			try {
				b.connectToGame(server);
				b.getGame().addListener(main);
				
			} catch (RuntimeException e) {
				throw e;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			
			/* on ajoute un id au bot */
			Integer id;
			id = b.getGame().addPlayer(b.getName(), b.getAvatar());
			b.setPlayerId(id);
			bots.add(b);
			
			textBotChoice.add(b.getName());
			textBotChoice.select(textBotChoice.getItemCount()-1);
			changeBotSelection();
			
		} catch (RemoteException e) {
			System.err.println("Error while adding the bot to the game :");
			e.printStackTrace();
		}
	}
	
	/**
	 * Met en pause tous les bots
	 */
	private void clickPauseAll()
	{
		for (DelegatingBotCore bot : bots)
		{
			bot.setPaused(true);
		}
	}
	
	/**
	 * Demarre tous les bots
	 */
	private void clickStartAll()
	{
		for (DelegatingBotCore bot : bots)
		{
			bot.setPaused(false);
		}
	}
	
	/**
	 * Supprime tous les bots
	 */
	private void clickRemoveAll()
	{
		for (DelegatingBotCore bot : bots)
		{
			bot.disconnectFromGame();
		}
		
		for (GuiBot bot : botsWindows)
		{
			bot.close();
		}
		
		bots.clear();
		botsWindows.clear();
		
		textBotChoice.removeAll();
		
		selectedBot = -1;
		textBotChoice.select(selectedBot);
		
		changeBotSelection();
	}
	
	/**
	 * Met le bot en pause ou le relance si celui-ci etait deja en pause
	 */
	private void clickPause()
	{
		bots.get(selectedBot).setPaused(!bots.get(selectedBot).isPaused());
		updateButtonsStates();
	}
	
	/**
	 * Cree une fenetre pour gerer le bot separement
	 */
	private void clickSplit()
	{
		GuiBot bot = new GuiBot(bots.get(selectedBot),server,display);
		botsWindows.add(bot);
		bot.init();
		bot.start();
	}
	
	/** 
	 * Annule les modifications apportees aux parametres 
	 */
	private void clickCancel() {
		getParams();
		paramsChanged = false;
		updateButtonsStates();
	}
	
	/** 
	 * Applique les modifications apportees aux parametres 
	 */
	private void clickUpdate() {
		setParams();
		paramsChanged = false;
		updateButtonsStates();
	}
	
	/** 
	 * Retire le bot du jeu et le supprime 
	 */
	private void clickRemove()
	{
		
		ArrayList<GuiBot> toRemove = new ArrayList<GuiBot>();
		for (GuiBot bot : botsWindows)
		{
			if (bot.getBotCore().getName().equals(bots.get(selectedBot).getName()))
			{
				System.out.println("gui trouve");
				bot.close();
				toRemove.add(bot);
			}
		}
		botsWindows.removeAll(toRemove);
		
		bots.get(selectedBot).disconnectFromGame();
		
		bots.remove(selectedBot);
		
		textBotChoice.removeAll();
		
		for (DelegatingBotCore b : bots)
		{
			textBotChoice.add(b.getName());
		}
		
		if (selectedBot != 0)
		{
			selectedBot--;
		}
		textBotChoice.select(selectedBot);
		
		changeBotSelection();
		
	}
	
	/**
	 * Modifie les parametres du bot par ceux donne
	 */
	private void setParams()
	{
		int param;
		
		param = textRole.getSelectionIndex();
		bots.get(selectedBot).setRole(param);
		
		param = textCreativity.getSelectionIndex()+1;
		bots.get(selectedBot).setCreativity(param);
		
		param = textAdaptation.getSelectionIndex()+1;
		bots.get(selectedBot).setAdaptation(param);
		
		param = textPersuasion.getSelectionIndex()+1;
		bots.get(selectedBot).setPersuation(param);
		
		param = textRelevance.getSelectionIndex()+1;
		bots.get(selectedBot).setRelevance(param);
	}
	
	/**
	 * Recupere les parametres du bot poru les afficher
	 */
	private void getParams()
	{
		if (selectedBot == -1)
		{
			textRole.select(0);
			textCreativity.select(0);
			textAdaptation.select(0);
			textPersuasion.select(0);
			textRelevance.select(0);
		}
		else
		{
			int param;

			if (!focusRole)
			{
				param = bots.get(selectedBot).getRole();
				textRole.select(param);
			}

			if (!focusCreativity)
			{
				param = bots.get(selectedBot).getCreativity();
				textCreativity.select(param-1);
			}

			if (!focusAdaptation)
			{
				param = bots.get(selectedBot).getAdaptation();
				textAdaptation.select(param-1);
			}

			if (!focusPersuasion)
			{
				param = bots.get(selectedBot).getPersuasion();
				textPersuasion.select(param-1);
			}

			if (!focusRelevance)
			{
				param = bots.get(selectedBot).getRelevance();
				textRelevance.select(param-1);
			}
		}
	}
	
	/**
	 * Ajoute le nombre de bots demande au manager
	 * @param count
	 */
	public void addBots(int count)
	{
		for (int i = 0 ; i < count ; i++)
		{
			clickAddBot();
		}
	}
}
