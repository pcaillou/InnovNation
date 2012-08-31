package client.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
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


// TODO gestion des parametres + ajouter creativite etc..

public class GuiBot extends Thread{
	
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger("client.gui.connect"); // => unused
	
	private Shell shell = null;
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Composite compositeHost = null;
	private Display display = null;
	
	
	private final static int LOOK_MIN_WIDTH = 100;
	private final static int LOOK_MIN_HEIGHT = 100;

	/* constantes de la description du bot */
	private final static String TXT_ROLE = "Role :\t\t";
	private final static String TXT_BOT = "Bot :\t\t";
	private final static String TXT_UPTIME = "UpTime :\t";
	private final static String TXT_NBIDEAS = "Idees :\t\t";
	private final static String TXT_NBCOMMENTS = "Commentaires :\t";
	private final static String TXT_TOKENS = "Tokens :\t\t";
	private final static String TXT_ADAPTATION = "Adaptation :\t"; 
	private final static String TXT_CREATIVITY = "Creativite :\t";  
	private final static String TXT_PERSUATION = "Persuation :\t";  
	private final static String TXT_RELEVANCE = "Pertinence :\t";   
	
	
	private final static String TOOLTIP_ROLE = "Role du bot";
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
	private final static String TXT_START = "Demarrer bot";
	private final static String TXT_PAUSE = "Stopper bot";
		
	private final static String FIELD_DEFAULT_TEXT = "                                                          ";
	
	private static Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private static Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;
	
	/* EmbbedType des composants */
	private GuiEmbbedType embbedType;
	
	/* Client */
	private DelegatingBotCore clientCore;
	
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
	
	/* tableau d'affichage des heuristiques */
	private Table heuristicsTable;
	
	/* list des textbox */
	private Combo textRole;
	private Combo textOpinion;
	private Combo textCreativity;
	private Combo textRelevance;
	private Combo textAdaptation;
	private Combo textPersuasion;
	
	/* liste des "bloqueurs" pour les combobox (pour empêcher leurs changement pendant qu'on les utilise)*/
	private boolean focusOpinion;
	private boolean focusRole;
	private boolean focusCreativity;
	private boolean focusRelevance;
	private boolean focusAdaptation;
	private boolean focusPersuasion;
	
	/* Liste des boutons */
	private Button buttonCancel;
	private Button buttonUpdate;
	private Button buttonPause;
	
	/* Runnable servant a refresh la fenetre */
	private Runnable refresh;
	
	/* Indique si des parametres ont ete modifies */
	private boolean paramsChanged;
	
	/**
	 * Cree le Gui du bot
	 * @param IEventListener : le listener du bot
	 * @param host : adresse du serveur
	 * @param _display : displayeur
	 * @param main : main listener
	 */
	public GuiBot(DelegatingBotCore core, String host,  Display _display) {
		
		clientCore = core;
		embbedType = GuiEmbbedType.STANDALONE;
		compositeHost = null;
		display = _display;
		paramsChanged = false;
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
	 * Retourne le bot cree
	 * @return DelegatingBotCore
	 */
	public DelegatingBotCore getBotCore()
	{
		return clientCore;
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
		
		if (clientCore.isPaused())
		{
			buttonPause.setText(TXT_START);
		}
		else
		{
			buttonPause.setText(TXT_PAUSE);
		}
	}
	
	/**
	 * Ajoute les informations du bots sous forme d'onglets
	 * @param compositeHost
	 */
	public void addBotMenu(Composite compositeHost)
	{
		/* partie onglets */
		TabFolder tab = new TabFolder(compositeHost, SWT.NONE);
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
			labelName.setText(TXT_BOT +  clientCore.getName());
			labelName.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelName.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelName.setToolTipText(TOOLTIP_BOT);
			
			/* on ajoute la ligne du role du bot */
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
						else if (textRole.getSelectionIndex() == 2)
						{
							textPersuasion.select((int) (Math.random()*10));
							textCreativity.select((int) (Math.random()*10));
							textAdaptation.select((int) (Math.random()*10));
							textRelevance.select((int) (Math.random()*10));
						}
						else
						{
							textCreativity.select(DelegatingBotCore.rolesParams[textRole.getSelectionIndex()][0]);
							textRelevance.select(DelegatingBotCore.rolesParams[textRole.getSelectionIndex()][1]);
							textAdaptation.select(DelegatingBotCore.rolesParams[textRole.getSelectionIndex()][2]);
							textPersuasion.select(DelegatingBotCore.rolesParams[textRole.getSelectionIndex()][3]);
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
				
				textOpinion = new Combo(rowRole, SWT.BORDER | SWT.READ_ONLY);
				textOpinion.setItems(new String[]{"Opinion","Sans opinion"});
				textOpinion.select(0);
				textOpinion.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						if (textOpinion.getSelectionIndex() == 0)
						{
							int[] opinion = new int[DelegatingBotCore.TOTAL_OPINION];
							for (int i = 0 ; i < opinion.length ; i++)
							{
								opinion[i] = (int) (Math.random() * 2);
							}
							
							clientCore.setOpinion(opinion);
							
						}
						else
						{
							clientCore.setOpinion(DelegatingBotCore.NO_OPINION);
						}
						
						updateButtonsStates();
					}
				});
				textOpinion.addFocusListener(new FocusListener() {
					public void focusLost(FocusEvent arg0) {
						focusOpinion = false;
					}
					public void focusGained(FocusEvent arg0) {
						focusOpinion = true;
						
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
				for (Integer i = 0 ; i <= 10 ; i++)textCreativity.add(i.toString());
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
				for (Integer i = 0 ; i <= 10 ; i++)textRelevance.add(i.toString());
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
				for (Integer i = 0 ; i <= 10 ; i++)textAdaptation.add(i.toString());
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
				for (Integer i = 0 ; i <= 10 ; i++)textPersuasion.add(i.toString());
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
	 * Initialise la fenetre du bot
	 */
	public void init() {

		if (embbedType == GuiEmbbedType.STANDALONE) {

			shell = new Shell (display, SWT.SHELL_TRIM);
			shell.setText (clientCore.getName());
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
		
		/* on ajoute les infos du bot */
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
	}
	
	/**
	 * Rafraichit les informations a l'ecran
	 */
	private void refresh()
	{
		
		/* on rafraichit les boutons */
		updateButtonsStates();
		
		/* on rafraichit les paramètres si on ne les change pas */
		if (!paramsChanged)
		{
			getParams();
		}
		
		/* on raffraichit les labels */
		labelDRole.setText(TXT_ROLE + DelegatingBotCore.roles[clientCore.getRole()]);
		labelUpTime.setText(TXT_UPTIME + clientCore.getUpTime()/1000 + " s");		
		labelNbIdeas.setText(TXT_NBIDEAS + clientCore.getNbIdeas());
		labelNbComments.setText(TXT_NBCOMMENTS + clientCore.getNbComments());
		labelNbTokens.setText(TXT_TOKENS + clientCore.getRemainingTokens());
		
		try {
			labelAdaptation.setText(TXT_ADAPTATION + String.valueOf(clientCore.computeAdaptation()) + " (" + clientCore.computeAdaptationRank() + "/" + clientCore.getAllPlayersIds().size() + ")");
			labelCreativity.setText(TXT_CREATIVITY + String.valueOf(clientCore.computeCreativity()) + " (" + clientCore.computeCreativityRank() + "/" + clientCore.getAllPlayersIds().size() + ")");
			labelRelevance.setText(TXT_RELEVANCE + String.valueOf(clientCore.computeRelevance()) + " (" + clientCore.computeRelevanceRank() + "/" + clientCore.getAllPlayersIds().size() + ")");
			labelPersuation.setText(TXT_PERSUATION + String.valueOf(clientCore.computePersuasion()) + " (" + clientCore.computePersuasionRank() + "/" + clientCore.getAllPlayersIds().size() + ")");
			updateHeuristicTable();

		} catch (Exception e) {
			System.err.println("Error bot : error while computing bot stats (" + e.getMessage() + ")");
			//labelAdaptation.setText(TXT_ADAPTATION + "compute error");
			//labelCreativity.setText(TXT_CREATIVITY + "compute error");
			//labelRelevance.setText(TXT_RELEVANCE + "compute error");
			//labelPersuation.setText(TXT_PERSUATION + "compute error");
		}
	}
	
	/**
	 * Met a jour le tableau des heuristiques
	 * @throws RemoteException
	 */
	public void updateHeuristicTable() throws RemoteException
	{
		int selection = heuristicsTable.getVerticalBar().getSelection();
		HashMap<Integer,Long> heuristics = clientCore.getHeuristics();
		HashMap<Integer,Double> opinions = clientCore.getOpinions();
		
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
			heuristicsTable.getItem(cursor).setText(0, clientCore.getGame().getIdea(h.getKey()).getShortName());					
			heuristicsTable.getItem(cursor).setText(1, h.getValue().toString());
			heuristicsTable.getItem(cursor).setText(2, opinions.get(h.getKey()).toString());
			heuristicsTable.getItem(cursor).setBackground(new Color(Display.getCurrent(), 128,128,128));
			if (clientCore.getGame().getIdea(h.getKey()).getPlayerId() == clientCore.getPlayerId())
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
	 * Demarre la fenetre du but
	 */
	public void run() {
		if (embbedType == GuiEmbbedType.STANDALONE) {

			if (!clientCore.isConnected())
			{
				System.out.println("bot deconnecte");
			}
			
			while (!shell.isDisposed () && clientCore.isConnected()) {
				
				/* on raffraichir l'affichage */
				Display.getDefault().asyncExec(refresh);
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.err.println(getName() + " error : sLeep failed");
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
	}
	
	/**
	 * Met le bot en pause ou le relance si celui-ci etait deja en pause
	 */
	private void clickPause()
	{
		clientCore.setPaused(!clientCore.isPaused());
		updateButtonsStates();
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
	 * Modifie les parametres du bot par ceux donne
	 */
	private void setParams()
	{
		int param;
		
		param = textRole.getSelectionIndex();
		clientCore.setRole(param);
		
		param = textCreativity.getSelectionIndex()+1;
		clientCore.setCreativity(param);
		
		param = textAdaptation.getSelectionIndex()+1;
		clientCore.setAdaptation(param);
		
		param = textPersuasion.getSelectionIndex()+1;
		clientCore.setPersuasion(param);
		
		param = textRelevance.getSelectionIndex()+1;
		clientCore.setRelevance(param);
	}
	
	/**
	 * Recupere les parametres du bot poru les afficher
	 */
	private void getParams()
	{
		int param;

		if (!focusOpinion)
		{
			if (clientCore.getOpinion() == DelegatingBotCore.NO_OPINION)
			{
				textOpinion.select(1);
			}
			else
			{
				textOpinion.select(0);
			}
		}
		
		if (!focusRole)
		{
			param = clientCore.getRole();
			textRole.select(param);
		}

		if (!focusCreativity)
		{
			param = clientCore.getCreativity();
		textCreativity.select(param-1);
		}

		if (!focusAdaptation)
		{
			param = clientCore.getAdaptation();
			textAdaptation.select(param-1);
		}

		if (!focusPersuasion)
		{
			param = clientCore.getPersuasion();
			textPersuasion.select(param-1);
		}

		if (!focusRelevance)
		{
			param = clientCore.getRelevance();
			textRelevance.select(param-1);
		}
	}
	
}
