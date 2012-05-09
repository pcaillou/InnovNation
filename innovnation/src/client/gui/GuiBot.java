package client.gui;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import client.DelegatingBotCore;
import events.IEventListener;


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
	
	
	private final static int LOOK_MIN_WIDTH = 400;
	private final static int LOOK_MIN_HEIGHT = 450;

	/* constantes de la description du bot */
	private final static String TXT_STATUS = "Status";
	private final static String TXT_HOST = "  Serveur :  ";
	private final static String TXT_BOT = "  Bot :  ";
	private final static String TXT_UPTIME = "  UpTime :  ";
	private final static String TXT_NBIDEAS = "  Idees :  ";
	private final static String TXT_NBCOMMENTS = "  Commentaires :  ";
	private final static String TXT_TOKENS = "  Tokens :  ";
	
	private final static String TOOLTIP_HOST = "Serveur de la partie";
	private final static String TOOLTIP_BOT = "Nom du bot";
	private final static String TOOLTIP_UPTIME = "Temps ecoule depuis la creation";
	private final static String TOOLTIP_NBIDEAS = "Nombre d'idees crees";
	private final static String TOOLTIP_NBCOMMENTS = "Nombre de commentaires crees";
	private final static String TOOLTIP_TOKENS = "Nombre de tokens restant";
	
	/* constantes des parametres */
	private final static String TXT_PARAMS = "Parametres";
	private final static String TXT_REACTIVITY = "Reactivite : ";
	private final static String TXT_CREATIVITY = "Creativite : ";
	private final static String TXT_RELEVANCE = "Pertinence : ";
	private final static String TXT_ADAPTATION = "Adaptation : ";
	private final static String TXT_PERSUATION = "Persuation : ";

	private final static String TOOLTIP_REACTIVITY = "Rapidite avec laquelle le bot agira et reagira\n"
													+"1 : lent - 10 : rapide";
	private final static String TOOLTIP_CREATIVITY = "Taux de creativite des idees\n"
			+"1 : peu creatif - 10 : tres creatif";
	private final static String TOOLTIP_RELEVANCE = "Taux de pertinence des idees et commentaires\n"
			+"1 : peu pertinent - 10 : tres pertinent";
	private final static String TOOLTIP_ADAPTATION = "Taux d'adaptation face aux idees et commentaires\n"
			+"1 : peu adaptatif - 10 : tres adaptatif";
	private final static String TOOLTIP_PERSUATION = "Taux de persuation des idees et commentaires\n"
			+"1 : peu persuasif - 10 : tres persuasif";
	
	/* constantes des boutons */
	private final static String TXT_UPDATE = "Appliquer parametres";
	private final static String TXT_CANCEL = "Annuler parametres";
	private final static String TXT_REMOVE = "Supprimer bot";
	private final static String TXT_START = "Start bot";
	private final static String TXT_PAUSE = "Pause bot";
		
	private final static String FIELD_DEFAULT_TEXT = "                                                          ";
	private final static String LABEL_DEFAULT_TEXT = "                         ";
	
	private Color LOOK_COLOR_BACKGROUND_MAINSPACE = null;
	private Color LOOK_COLOR_BACKGROUND_SUBSPACES = null;
	
	/* Style des composants */
	private final int LOOK_COMPOSITE_STYLE_SUBSPACES = SWT.BORDER; // SWT.NONE; // 
	
	/* EmbbedType des composants */
	private GuiEmbbedType embbedType;
	
	/* Client */
	private final DelegatingBotCore clientCore;
	
	/* Liste des labels */
	private Label labelHost;
	private Label labelName;
	private Label labelUpTime;
	private Label labelNbIdeas;
	private Label labelNbComments;
	private Label labelNbTokens;
	
	/* list des textbox */
	private Combo textReactivity;
	private Combo textCreativity;
	private Combo textRelevance;
	private Combo textAdaptation;
	private Combo textPersuation;
	
	/* Liste des boutons */
	private Button buttonCancel;
	private Button buttonUpdate;
	private Button buttonRemove;
	private Button buttonPause;
	
	/* Runnable servant a refresh la fenetre */
	private Runnable refresh;
	
	/* Adresse du serveur */
	private String server = null;
	
	/* Indique si des parametres ont ete modifies */
	private boolean paramsChanged;
	
	/* indique si le bot est en pause ou non */
	private boolean botPaused;
	
	/**
	 * Cree le Gui du bot
	 * @param IEventListener : le listener du bot
	 * @param host : adresse du serveur
	 * @param _display : displayeur
	 * @param main : main listener
	 */
	public GuiBot(IEventListener listener, String host,  Display _display, IEventListener main) {
		
		clientCore = new DelegatingBotCore(listener);
		embbedType = GuiEmbbedType.STANDALONE;
		compositeHost = null;
		display = _display;
		server = host;
		paramsChanged = false;
		botPaused = true;
		initColors();

		try {
			clientCore.connectToGame(host);
			clientCore.getGame().addListener(main);
			
		} catch (RuntimeException e) {
			throw e;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
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
		
		if (botPaused)
		{
			buttonPause.setText(TXT_START);
		}
		else
		{
			buttonPause.setText(TXT_PAUSE);
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
		
		Label 	labelReactivity,
				labelCreativity,
				labelRelevance,
				labelAdaptation,
				labelPersuation;
		
		
		/* partie description du bot */
		{
			/* on cree le layout */
			GridLayout layoutDescr = new GridLayout(1, false);
			Composite compositeDescr = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeDescr.setBackground(LOOK_COLOR_BACKGROUND_MAINSPACE);
			compositeDescr.setLayout(layoutDescr);
			compositeDescr.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

			/* on ajoute le nom de la partie status */
			Label labelStatus = new Label(compositeDescr, SWT.READ_ONLY);
			labelStatus.setText(TXT_STATUS);
			labelStatus.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelStatus.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			
			/* on ajoute la ligne description du serveur */
			labelHost = new Label(compositeDescr, SWT.READ_ONLY);
			labelHost.setText(TXT_HOST + server);
			labelHost.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelHost.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelHost.setToolTipText(TOOLTIP_HOST);
			
			/* on ajoute le nom du bot */
			labelName = new Label(compositeDescr, SWT.READ_ONLY);
			labelName.setText(TXT_BOT + clientCore.getName());
			labelName.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelName.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			labelName.setToolTipText(TOOLTIP_BOT);
			
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
		}
		
		/* partie parametres */
		{

			/* on cree le layout */
			GridLayout layoutParams = new GridLayout(1, false);
			Composite compositeParams = new Composite(compositeHost, LOOK_COMPOSITE_STYLE_SUBSPACES);
			compositeParams.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			compositeParams.setLayout(layoutParams);
			compositeParams.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

			/* on cree le layout des lignes */
			RowLayout layoutRow = new RowLayout(SWT.HORIZONTAL);
			
			/* on ajoute le nom de la partie status */
			Label labelStatus = new Label(compositeParams, SWT.READ_ONLY);
			labelStatus.setText(TXT_PARAMS);
			labelStatus.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
			labelStatus.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

			/* on ajoute la ligne du parametre reactivity */
			{
				Composite rowExemple = new Composite(compositeParams, SWT.NONE);
				rowExemple.setBackground(LOOK_COLOR_BACKGROUND_SUBSPACES);
				rowExemple.setLayout(layoutRow);
				rowExemple.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				
				/* on ajoute le label */
				labelReactivity = new Label(rowExemple, SWT.READ_ONLY);
				labelReactivity.setText(LABEL_DEFAULT_TEXT);
				labelReactivity.setToolTipText(TOOLTIP_REACTIVITY);
				
				/* on ajoute le champ */
				textReactivity = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textReactivity.add(i.toString());
				textReactivity.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						updateButtonsStates();
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
				labelCreativity.setText(LABEL_DEFAULT_TEXT);
				labelCreativity.setToolTipText(TOOLTIP_CREATIVITY);
				
				/* on ajoute le champ */
				textCreativity = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textCreativity.add(i.toString());
				textCreativity.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						updateButtonsStates();
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
				labelRelevance.setText(LABEL_DEFAULT_TEXT);
				labelRelevance.setToolTipText(TOOLTIP_RELEVANCE);
				
				/* on ajoute le champ */
				textRelevance = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textRelevance.add(i.toString());
				textRelevance.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						updateButtonsStates();
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
				labelAdaptation.setText(LABEL_DEFAULT_TEXT);
				labelAdaptation.setToolTipText(TOOLTIP_ADAPTATION);
				
				/* on ajoute le champ */
				textAdaptation = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textAdaptation.add(i.toString());
				textAdaptation.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						updateButtonsStates();
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
				labelPersuation.setText(LABEL_DEFAULT_TEXT);
				labelPersuation.setToolTipText(TOOLTIP_PERSUATION);
				
				/* on ajoute le champ */
				textPersuation = new Combo(rowExemple, SWT.BORDER | SWT.READ_ONLY);
				for (Integer i = 1 ; i <= 10 ; i++)textPersuation.add(i.toString());
				textPersuation.addListener(SWT.Selection,new Listener() {
					public void handleEvent (Event e) {
						paramsChanged = true;
						updateButtonsStates();
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
		labelAdaptation.setText(TXT_ADAPTATION);
		labelReactivity.setText(TXT_REACTIVITY);
		labelCreativity.setText(TXT_CREATIVITY);
		labelRelevance.setText(TXT_RELEVANCE);
		labelPersuation.setText(TXT_PERSUATION);

		updateButtonsStates();
		
		getParams();
		paramsChanged = false;
		
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
	 * Rafraichit les informations a l'ecran ainsi que le bot
	 */
	private void refresh()
	{
		/* on raffraichit les labels */
		labelUpTime.setText(TXT_UPTIME + clientCore.getUpTime()/1000 + " s");
		labelNbIdeas.setText(TXT_NBIDEAS + clientCore.getNbIdeas());
		labelNbComments.setText(TXT_NBCOMMENTS + clientCore.getNbComments());
		labelNbTokens.setText(TXT_TOKENS + clientCore.getRemainingTokens());
	}
	
	/**
	 * Demarre la fenetre du but
	 */
	public void run() {
		if (embbedType == GuiEmbbedType.STANDALONE) {

			while (!shell.isDisposed ()) {

				Display.getDefault().asyncExec(refresh);
				
				/* on raffraichit le bot */
				if (!botPaused)
				{
					try {
						clientCore.refresh();
					} catch (RemoteException e) {
						System.err.println("Bot error : impossible de refresh");
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.err.println("SLeep failed");
					e.printStackTrace();
				}
			}
			
		}
		
		close();
		
	}
	
	/**
	 * Ferme la fenetre du bot, et le deconnecte
	 */
	private void close() {
				
		/* on deconnecte le bot */
		clientCore.disconnectFromGame();
		
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
		botPaused = !botPaused;
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
	 * Retire le bot du jeu et le supprime 
	 */
	private void clickRemove()
	{
		close();
	}
	
	/**
	 * Modifie les parametres du bot par ceux donne
	 */
	private void setParams()
	{
		int param;
		
		param = textReactivity.getSelectionIndex()+1;
		clientCore.setReactivity(param);
		
		param = textCreativity.getSelectionIndex()+1;
		clientCore.setCreativity(param);
		
		param = textAdaptation.getSelectionIndex()+1;
		clientCore.setAdaptation(param);
		
		param = textPersuation.getSelectionIndex()+1;
		clientCore.setPersuation(param);
		
		param = textRelevance.getSelectionIndex()+1;
		clientCore.setRelevance(param);
	}
	
	/**
	 * Recupere les parametres du bot poru les afficher
	 */
	private void getParams()
	{
		int param;
		
		param = clientCore.getReactivity();
		textReactivity.select(param-1);
		
		param = clientCore.getCreativity();
		textCreativity.select(param-1);
		
		param = clientCore.getAdaptation();
		textAdaptation.select(param-1);
		
		param = clientCore.getPersuation();
		textPersuation.select(param-1);
		
		param = clientCore.getRelevance();
		textRelevance.select(param-1);
	}
	
	@SuppressWarnings("unused")
	private void displayError(String title, String msg) {
		MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		mb.setText(title);
		mb.setMessage(msg);
		mb.open();
	}
	
}
