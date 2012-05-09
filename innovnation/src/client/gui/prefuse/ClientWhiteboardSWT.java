package client.gui.prefuse;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JInternalFrame;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;

// AD import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
//AD import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.Layout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
//AD import prefuse.data.Table;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import util.Pair;
import client.gui.GuiTestMain;
import client.gui.ISelectionControl;
import client.gui.prefuse.ThreadMonitorPrefusePerformance.IMonitorPrefusePerformanceListener;
import data.Avatars;
import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;
import events.GameObjectEvent;
import events.IEventListener;
import events.LinkEvent;
import events.PlayerEvent;
import fr.research.samthiriot.commons.parameters.EventParameterDispatcher;
import fr.research.samthiriot.commons.parameters.IParametersObserver;
import fr.research.samthiriot.commons.parameters.events.EventParameterAbstract;
import fr.research.samthiriot.commons.parameters.events.EventParameterValueChanged;
import fr.research.samthiriot.gui.prefuse.MyPrefuseDisplay;
import functions.IGame;


/**
 * TODO ajouter la possibilité d'écouter les idées sélectionnées, afin de simplifier l'ajout / suppression d'idées et items
 * 
 * TODO desactiver le layout quand la fenetre est cachée
 * 
 * @author Samuel Thiriot
 *
 */
public final class ClientWhiteboardSWT 
								implements 	client.gui.IWhiteboard, 
											IParametersObserver, // listens changes in the display's parameters
											IMonitorPrefusePerformanceListener, // listens for low perf alerts from the monitor
											IEventListener // and for events from the game
											{

	
	private Logger logger = Logger.getLogger("innovnation.gui.whiteboard");

	/**
	 * True if the dynamic (online) layout is active.
	 */
	private boolean layoutActive = false;
	
	
	private boolean displayItemsAsNodes = false;
	
	private boolean displayPlayersAsNodes = false;
	
	/*
	 * ===================================================
	 * "Dynamic" settings
	 * ===================================================
	 */
	
	/**
	 * When true, an "autofit" is ran after each change.
	 * (good idea, but not implemented so good till now)
	 */
	private boolean autoFit = false;
	
	/*
	 * ===================================================
	 * Elements of windows
	 * ===================================================
	 */
	
	/**
	 * The SWT composite that contains the Prefuse AWT display 
	 */
	private Composite compositeNet = null;
	
	/**
	 * The AWT Frame that actually contains the prefuse display (into compositeNet)
	 */
	private Frame frameAwt = null;
	
	/**
	 * Another AWT Frame is required for prefuse (with specific properties). Displayed into frameAwt.
	 */
	private JInternalFrame frameAwt2 = null;
	
	

	/*
	 * ===================================================
	 * Prefuse Objects
	 * ===================================================
	 */
	
	/**
	 * The prefuse Visualisation 
	 */
	private Visualization vis = null;
	
	/**
	 * The prefuse Display 
	 */	
	private MyPrefuseDisplay prefuseDisplay;

	protected Layout theLayout = null;

	private Graph prefusegraph = null;
	
	private MyZoomToFitControl zoomToFitControl = null;
	
	private ContextualMenuControl contextualMenuControl = null;
	
	private SelectionControl selectionControl = null;
	
	private MyBasicLabelRenderer labelRenderer;
	
	protected final static int PREFUSE_WHITEBOARD_WIDTH = 800;
	protected final static int PREFUSE_WHITEBOARD_HEIGHT = 800;
	
	
	/**
	 * The field which contains the label of the node
	 */
	protected final static String PREFUSE_NODE_FIELD_LABEL = "label";
	
	protected final static String PREFUSE_NODE_FIELD_AGE = "age";

	protected final static String PREFUSE_NODE_FIELD_IMG = "img";
	protected final static int PREFUSE_NODE_IMG_MAX_WIDTH = 50;
	protected final static int PREFUSE_NODE_IMG_MAX_HEIGHT = PREFUSE_NODE_IMG_MAX_WIDTH;
	
	protected final static int PREFUSE_NODE_TEXT_MAXWIDTH = 50;


	/**
	 * Stores the kind of node type (idea, item and so on)
	 * Actually stores the ordinal value of one #{@link #TypeNode}
	 */
	protected final static String PREFUSE_NODE_FIELD_TYPE = "type";
	
	/**
	 * Name of the field which stores the kind of edge type (idea to idea, idea to item, and so on).
	 * Actually stores the ordinal value of one #{@link #TypeEdge}
	 */
	protected final static String PREFUSE_EDGE_FIELD_TYPE = "type";

	protected final static String PREFUSE_NODE_FIELD_SELECTED = "selected";

	/**
	 * Duration of animations for zoom
	 */
	protected final static long PREFUSE_ZOOM_DURATION = 500;
	protected final static long PREFUSE_PAN_DURATION = 1000;

	/**
	 * The possible types of nodes
	 */
	protected enum TypeNode {
		
		//		 FILL COLOR			BORDER COLOR	BORDER WIDTH	TEXT COLOR
		ITEM	(Color.WHITE, 		Color.BLACK,	1,				Color.BLACK		), 
		IDEA	(Color.LIGHT_GRAY, 	Color.BLACK,	1,				Color.BLACK		),
		PLAYER	(Color.WHITE,		Color.WHITE,	0,				Color.BLUE		),
		COMMENT	(Color.LIGHT_GRAY,	Color.LIGHT_GRAY, 0,			Color.BLACK)
		;
		//COMMENT, 
		//PLAYER;
		
		public final Color 	nodeFillColor;
		public final Color 	nodeBorderColor;
		public final int	nodeBorderWidth;
		public final Color 	nodeTextColor;
		
		private TypeNode(
					Color nodeFillColor, 
					Color nodeBorderColor, 
					int nodeBorderWidth, 
					Color nodeTextColor
					) {
			
			this.nodeFillColor = nodeFillColor;
			this.nodeBorderColor = nodeBorderColor;
			this.nodeBorderWidth = nodeBorderWidth;
			this.nodeTextColor = nodeTextColor;
		}
	}

	/**
	 * 
	 */
	protected enum TypeEdge {
		
		//				directed		color					width		springcoef	springlength
		IDEA2IDEA	(		true,		Color.BLACK,			3,			-1.f,			400f	),
		IDEA2ITEM	(		false,		Color.GRAY,				2,			-1.f,			300f	),
		PLAYER2IDEA	(		false,		Color.BLUE,				1,			0.0005f,		300f	),
		PLAYER2ITEM	(		false,		Color.BLUE,				1,			0.0005f,		300f	),
		COMMENT2IDEA(		false,		Color.LIGHT_GRAY,		1,			0.0005f,		200f	),
		COMMENT2ITEM(		false,		Color.LIGHT_GRAY,		1,			0.0005f,		100f	),
		COMMENT2COMMENT(	false,		Color.LIGHT_GRAY,		1,			0.0005f,		40f		)
		;
		
		public final boolean directed;
		public final Color 	edgeColor;
		public final int	edgeWidth;
		
		/**
		 * Note that a spring coef of -1 means an automatic one 
		 */
		public final float	edgeSpringCoef;
		
		/**
		 * Note that a length of -1 means an automatic one 
		 */
		public final float	edgeSpringLength;
		
		
		private TypeEdge(boolean directed, Color edgeColor, int edgeWidth, float	edgeSpringCoef, float	edgeSpringLength) {
			this.directed = directed;
			this.edgeColor = edgeColor;
			this.edgeWidth = edgeWidth;
			this.edgeSpringCoef = edgeSpringCoef;
			this.edgeSpringLength = edgeSpringLength;
		}
		
	}

	/**
	 * If true, the force direct layout will enforce bounds into the window (no infinite horizon)
	 */
	protected final static boolean PREFUSE_LAYOUT_ENFORCE_BOUND = false;

	protected final static int PREFUSE_NODE_PADDING_HORIZ = 10;
	
	/**
	 * Forbidds Prefuse to be so verbose. Should be true for production, may be false for debugging.
	 */
	protected final static boolean PREFUSE_DISABLE_LOGGING = false;
	/**
	 * The field that stores the innovnation object represented by this object
	 */
	protected final static String PREFUSE_NODE_FIELD_OBJ = "object";
	
//	protected final static Color PREFUSE_COLOR_IDEA_TXT = Color.BLACK;
//	protected final static Color PREFUSE_COLOR_IDEA_FILL = Color.LIGHT_GRAY;
	
	/*
	 * ===================================================
	 * Maps InnovNation objects to a prefuse network
	 * ===================================================
	 */
	
	protected Map<Integer, Node> ideasIds2nodes = new HashMap<Integer, Node>();
	protected Map<Integer, Node> itemsIds2nodes = new HashMap<Integer, Node>();
	protected Map<Integer, Node> playersIds2nodes = new HashMap<Integer, Node>();
	
	protected Map<Integer, Node> commentIds2nodes = new HashMap<Integer, Node>();

	private WhiteboardParameters parameters = null;
	
	
	private ThreadMonitorPrefusePerformance threadMonitorPrefusePerformance;
	
	
	/**
	 * Access to the game, that is the distant server !
	 */
	private IGame game = null;
	
	private GuiTestMain guiTestMain = null;
	
	/**
	 * Creates a whiteboard into the proposed composite. 
	 * NB: composite <b>has to be created with special parameters SWT.EMBEDDED | SWT.NO_BACKGROUND</b> (thanks !)
	 * @param composite 
	 */
	public ClientWhiteboardSWT(Composite composite) {
		
		this.compositeNet = composite;
		
		// prefuse uses the standart Java Logger framework. It is verbose, however. This asks it politely to shut up.
		if (PREFUSE_DISABLE_LOGGING) {
			java.util.logging.Logger.getLogger("prefuse").setLevel(java.util.logging.Level.SEVERE);
			java.util.logging.Logger.getLogger("prefuse.data.expression.parser.ExpressionParser").setLevel(java.util.logging.Level.SEVERE);
		}

		parameters = new WhiteboardParameters();
		EventParameterDispatcher.getDispatcher().addParametersListener(this);
		
	}
	
	/**
	 * Inits the display: 
	 */
	public void init(GuiTestMain guiTestMain) {
		
		logger.debug("init of the whiteboard");
		
		this.guiTestMain = guiTestMain;
		
		if (frameAwt == null) {
			frameAwt = SWT_AWT.new_Frame(compositeNet);
			frameAwt.setBackground(Color.WHITE);
		}
		
		if (frameAwt2 == null) {
			frameAwt2 = new JInternalFrame("", false, false, false,
					false);
			frameAwt2.setBorder(null);
			((javax.swing.plaf.basic.BasicInternalFrameUI) frameAwt2.getUI())
					.setNorthPane(null);
			frameAwt2.setResizable(false);
			frameAwt2.setVisible(true);
			frameAwt.add(frameAwt2);
				
		}
		
		
		if (vis == null) {
			vis = new Visualization();
			
			labelRenderer = new MyBasicLabelRenderer(PREFUSE_NODE_FIELD_LABEL, -1);
			labelRenderer.setImageField(PREFUSE_NODE_FIELD_IMG);
			labelRenderer.setMaxImageDimensions(PREFUSE_NODE_IMG_MAX_WIDTH, PREFUSE_NODE_IMG_MAX_HEIGHT);
			// USELESS, as the number of chars is managed by buildLabelForNode()
			// labelRenderer.setMaxTextWidth(PREFUSE_NODE_TEXT_MAXWIDTH);
			labelRenderer.setHorizontalPadding(PREFUSE_NODE_PADDING_HORIZ);
			labelRenderer.setVerticalTextAlignment(prefuse.Constants.TOP);
			labelRenderer.setHorizontalTextAlignment(prefuse.Constants.LEFT);
			
			labelRenderer.setFieldForObject(PREFUSE_NODE_FIELD_OBJ);
			
			MyEdgeRenderer edgeRenderer = new MyEdgeRenderer();
			
			DefaultRendererFactory df = new DefaultRendererFactory(labelRenderer, edgeRenderer);
			
			vis.setRendererFactory(df);
			
			
		}

		if (prefuseDisplay == null) {
			
			prefuseDisplay = new MyPrefuseDisplay(vis);

			prefuseDisplay.setDoubleBuffered(true);
			prefuseDisplay.setHighQuality(true);
			
			// configure the prefuse display
			prefuseDisplay.setSize(PREFUSE_WHITEBOARD_WIDTH, PREFUSE_WHITEBOARD_HEIGHT); // set display size
			prefuseDisplay.setDoubleBuffered(true);
			// TODO ??? prefuseDisplay.setIgnoreRepaint(true);

			prefuseDisplay.addControlListener(new DragControl()); // drag items
																	// around

			selectionControl = new SelectionControl();
			prefuseDisplay.addControlListener(selectionControl);
			
			prefuseDisplay.addControlListener(new PanControl()); // pan with
																	// background
																	// left-drag
			prefuseDisplay.addControlListener(new WheelZoomControl()); // zoom with
																		// wheel
			
			zoomToFitControl = new MyZoomToFitControl(prefuseDisplay);
			//zoomToFitControl.setZoomOverItem(false);
			//prefuseDisplay.addControlListener(new ZoomToFitControl());
			//prefuseDisplay.addControlListener(zoomToFitControl);
			
			contextualMenuControl = new ContextualMenuControl(compositeNet, this, guiTestMain);
			prefuseDisplay.addControlListener(contextualMenuControl);
			
			
			//prefuseDisplay.addControlListener(new NeighborHighlightControl());
			
			frameAwt2.add(prefuseDisplay);

			threadMonitorPrefusePerformance = new ThreadMonitorPrefusePerformance(prefuseDisplay);
			threadMonitorPrefusePerformance.count = 20;
			threadMonitorPrefusePerformance.thresholdFrameRateMin = 10;
			threadMonitorPrefusePerformance.addListener(this);
			/*
			// add the legend to post-paint events
			prefuseDisplay.addPaintListener(new PaintListener() {
				
				@Override
				public void prePaint(prefuse.Display d, Graphics2D g) {
					//Graphics2D gd2 = (Graphics2D)prefuseDisplay.getGraphics();
					//gd2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					
					g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
//					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
					g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

					
				}
				
				@Override
				public void postPaint(prefuse.Display d, Graphics2D g) {
					// to be used later (for adding buttons, and so on)
				}

				
			});
		*/
		}
		

		
		if (prefusegraph == null) {
			
			// debug : prefusegraph = GraphLib.getGrid(10, 10);
			
			
			prefusegraph = new Graph();
			
			prefusegraph.getNodeTable().addColumn(PREFUSE_NODE_FIELD_LABEL, String.class, "");
			prefusegraph.getNodeTable().addColumn(PREFUSE_NODE_FIELD_IMG, String.class, "");
			prefusegraph.getNodeTable().addColumn(PREFUSE_NODE_FIELD_TYPE, Integer.class, 0);
			prefusegraph.getNodeTable().addColumn(PREFUSE_NODE_FIELD_OBJ, Object.class, null);
			prefusegraph.getNodeTable().addColumn(PREFUSE_NODE_FIELD_SELECTED, boolean.class, Boolean.FALSE);
			prefusegraph.getNodeTable().addColumn(PREFUSE_NODE_FIELD_AGE, double.class, 0d);

			prefusegraph.getNodeTable().index(PREFUSE_NODE_FIELD_TYPE);


			prefusegraph.getEdgeTable().addColumn(PREFUSE_EDGE_FIELD_TYPE, Integer.class, 0);
			prefusegraph.getEdgeTable().index(PREFUSE_EDGE_FIELD_TYPE);
			
			vis.add("graph", prefusegraph);
		}

		compositeNet.setVisible(true);
		frameAwt.setVisible(true);
		frameAwt.setIgnoreRepaint(true);

		
		readParameters();
		
		// center the display
		centerDisplayImmediat();
		
		initLayout();
	}
	

	
	@Override
	public boolean isLayoutActive() {
		return layoutActive;
	}

	protected void initLayout() {
		
		if (theLayout == null) {
			
			theLayout = new MyMultiplexForceDirectedLayout("graph", PREFUSE_LAYOUT_ENFORCE_BOUND);

			//0
			ActionList ageDatas = new ActionList(Activity.INFINITY, 100 );
			ageDatas.add(new ItemAction("graph.nodes") {
				
				@Override
				public void process(VisualItem item, double frac) {
					try {
						Double precedent = (Double)item.get(PREFUSE_NODE_FIELD_AGE);
						item.set(PREFUSE_NODE_FIELD_AGE, Math.min(precedent+0.01,1));
						
						/*if (precedent < 1) {
							System.out.print(precedent+" ");
						}*/
						/*
						int value = (Integer)item.get(PREFUSE_NODE_FIELD_AGE);
						if (value<254) {
							System.out.print(item.get(PREFUSE_NODE_FIELD_AGE));
							System.out.print(" ");
						}
						*/
						//item.getVisualization().invalidate("graph.nodes");
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("oops (array out of bound)");
						System.out.println(item.getClass().getCanonicalName());
						e.printStackTrace();
					}
				}
			});
			
			ActionList layout = null;

			layout = new ActionList(Activity.INFINITY);
			layout.add(theLayout);
			
			vis.removeAction("layout");
			vis.putAction("layout", layout);
			
			ActionList color = new ActionList( Activity.INFINITY);

			// stroke for nodes
			StrokeAction strokeNodes = new StrokeAction("graph.nodes", new BasicStroke(2));
			strokeNodes.add(VisualItem.FIXED, new BasicStroke(3));
			strokeNodes.add(VisualItem.HIGHLIGHT, new BasicStroke(2));
			strokeNodes.add(PREFUSE_NODE_FIELD_SELECTED, new BasicStroke(5));

			color.add(strokeNodes);
			
			
			ColorAction caStroke = new ColorAction("graph.nodes", VisualItem.STROKECOLOR, ColorLib.rgba(0, 0, 0, 0));

			caStroke.add(VisualItem.FIXED, new ColorAction("graph.nodes",
					VisualItem.STROKECOLOR, ColorLib.rgb(0, 0, 0) ));
			caStroke.add(PREFUSE_NODE_FIELD_SELECTED, new ColorAction("graph.nodes",
					VisualItem.STROKECOLOR, ColorLib.rgb(0, 0, 0) ));
			caStroke.add(VisualItem.HIGHLIGHT, new ColorAction("graph.nodes",
					VisualItem.STROKECOLOR, ColorLib.rgb(90, 90, 90))); // 255,200,125
			
			/*
			DataColorAction dcaNodes2 = new DataColorAction(
					"TRUE", 
					PREFUSE_NODE_FIELD_AGE,
					Constants.NOMINAL,
					VisualItem.FILLCOLOR,
					new int[] {122}
					//ColorLib.getCoolPalette()
					);
			*/
			
			color.add(caStroke);

			
/*
			DataColorAction dcaNodes = new DataColorAction(
					"graph.nodes", 
					PREFUSE_NODE_FIELD_AGE,
					Constants.NUMERICAL,
					VisualItem.STROKECOLOR,
					ColorLib.getHotPalette()
					);
			color.add(dcaNodes);
*/
			// use black for node text
			ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
			color.add(text);
			
			ColorAction nodeFillColor = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.color(Color.GREEN));
			color.add(nodeFillColor);
			
			for (TypeNode type : TypeNode.values()) {
				
				Predicate p = ExpressionParser.predicate("["+PREFUSE_NODE_FIELD_TYPE+"]="+type.ordinal());
				// fill color
				nodeFillColor.add(p, ColorLib.color(type.nodeFillColor));
				// text color
				text.add(p, ColorLib.color(type.nodeTextColor));
			
				// basic stroke
				strokeNodes.add(p, new BasicStroke(type.nodeBorderWidth));
				
				// TODO... stroke when fixed and highlighted ? 
				//Predicate p2forFixed = ExpressionParser.predicate("["+PREFUSE_NODE_FIELD_TYPE+"]="+type.ordinal()+" AND "+VisualItem.FIXED);
				//strokeNodes.add(p2forFixed, new BasicStroke(type.nodeBorderWidth));
				
				//caStroke.add(p, ColorLib.color(type.nodeBorderColor));
			}
			

		
			//nodeFillColor.add("TRUE", dcaNodes2);

			
			// color for edges (both the line and the arrow)
			ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(100));
			ColorAction edges2 = new ColorAction("graph.edges", VisualItem.FILLCOLOR, ColorLib.gray(100));
			color.add(edges);
			color.add(edges2);
		
			StrokeAction edgesStroke = new StrokeAction("graph.edges");

		
			for (TypeEdge type : TypeEdge.values()) {
				
				Predicate p = ExpressionParser.predicate("["+PREFUSE_EDGE_FIELD_TYPE+"]="+type.ordinal());
				// fill color
				int i = ColorLib.color(type.edgeColor);
				edges.add(p, i);
				edges2.add(p, i);
			
				// stroke edges
				edgesStroke.add(p, new BasicStroke(type.edgeWidth));
			
			}
			
			color.add(edgesStroke);

			color.add(new RepaintAction());
			vis.removeAction("color");
			vis.removeAction("aging");
			vis.putAction("aging", ageDatas);
			vis.putAction("color", color);
			
			theLayout.setEnabled(true);
		}
	
	}
	
	
	public void startLayout() {

		
		vis.run("layout");
		vis.run("color");
		vis.run("aging");

	}
	
	public void stopLayout() {

		vis.cancel("layout");
		vis.cancel("color");
		vis.cancel("aging"); // TODO: really ?

	}
	
	@Override
	public void setLayoutActive(boolean active) {
		
		if (layoutActive == active) // if nothing to change => quick exit :-) 
			return; 
		
		layoutActive = active;
		
		if (layoutActive)
			startLayout();
		else 
			stopLayout();
		
	}
	
	protected Node findOrCreateNodeForComment(IComment comment, TypeEdge typeEdge) {
		return findOrCreateNodeForComment(comment, typeEdge, false);	
	}
	
	protected Node findOrCreateNodeForComment(IComment comment, TypeEdge typeEdge, boolean refresh) {
		
		Node node = commentIds2nodes.get(comment.getUniqueId());
		
		if (node == null) {
			logger.debug("no node for comment "+comment+"; creating one");
			
			node = createNodeForComment(comment, typeEdge);
			commentIds2nodes.put(comment.getUniqueId(), node);
		} else if (refresh) {
			updateDataForComment(comment, node, typeEdge);
		}
		
		
		return node;
	}

	/**
	 * Updates the data fields of the internal Node representation 
	 * of this idea (i.e. image, object, type), including links.
	 * @param idea
	 * @param node
	 */
	private void updateDataForIdea(IIdea idea, Node node) {
		
		try {
			
			final IPlayer author = game.getPlayer(idea.getPlayerId());
			
			// (!displayPlayersAsNodes) && 
			if ((author != null)) { // set the image only if the player is not displayed into the idea node 
				node.setString(PREFUSE_NODE_FIELD_IMG, Avatars.getPathForAvatar(author.getPicturePath()));
			} else {
				node.setString(PREFUSE_NODE_FIELD_IMG, null);
			}
			
			node.set(PREFUSE_NODE_FIELD_OBJ, idea);
			node.set(PREFUSE_NODE_FIELD_TYPE, TypeNode.IDEA.ordinal());
			
			node.set(PREFUSE_NODE_FIELD_AGE, 0d);
			
			// display links between ideas
			if (idea.hasParents()) {
				
				for (IIdea currentIdea : idea.getParents()) {
					findOrCreateEdgeBetween(
							findOrCreateNodeForIdea(currentIdea),
							node,
							TypeEdge.IDEA2IDEA
							);
				}
			}
			
			if (idea.hasChildren()) {
				for (IIdea currentIdea : idea.getChildren()) {
					findOrCreateEdgeBetween(
							node,
							findOrCreateNodeForIdea(currentIdea),
							TypeEdge.IDEA2IDEA
							);
				}
			}
			
			if ( (displayPlayersAsNodes) && (author != null) ) {
				findOrCreateEdgeBetween(
						findOrCreateNodeForPlayer(author),
						node,
						TypeEdge.IDEA2IDEA
						);
			}
			
			// display items...
			if (displayItemsAsNodes) {
				// ... either as nodes...
				//node.setString(PREFUSE_NODE_FIELD_LABEL, idea.getShortName());
				for (Integer itemId : idea.getItemsIds()) {
					IItem item = game.getItem(itemId);
					Node nodeForItem = findOrCreateNodeForItem(item);
					findOrCreateEdgeBetween(
							node, 
							nodeForItem,
							TypeEdge.IDEA2ITEM
							);
				}
			} 
			
			//else {
				// ... or as a longer label !
				StringBuffer sb = new StringBuffer();
				sb.append(idea.getShortName());
				sb.append("\n");
				for (Integer itemId : idea.getItemsIds()) {
					IItem item = game.getItem(itemId);
					sb.append("- ");
					sb.append(buildLabelForNode(item.getShortName()));
					sb.append("\n");
				}
				
				node.setString(PREFUSE_NODE_FIELD_LABEL, sb.toString());
			//}
		
			
		} catch (RemoteException e) {
			synchroProblem(e);
		}
	}
	
	private void updateDataForIdea(IIdea idea) {
		
		updateDataForIdea(
				idea, 
				findOrCreateNodeForIdea(idea)
				);
		
	}
	
	/**
	 * If the node does not exists, create its; else, if refresh is true,
	 * updates its data.
	 * @param idea
	 * @param refresh
	 * @return
	 */
	protected Node findOrCreateNodeForIdea(IIdea idea, boolean refresh) {
		
		Node node = ideasIds2nodes.get(idea.getUniqueId());
		
		if (node == null) {
			logger.debug("no node for idea "+idea+"; creating one");
			
			node = createNodeForIdea(idea);
			ideasIds2nodes.put(idea.getUniqueId(), node);
		} else if (refresh) {
			updateDataForIdea(idea, node);
		}
		
		return node;
	}
	
	
	protected Node findOrCreateNodeForIdea(IIdea idea) {
		return findOrCreateNodeForIdea(idea, false);
	}
	
	protected Node findOrCreateNodeForPlayer(IPlayer player) {
		return findOrCreateNodeForPlayer(player, false);
	}
	
	protected Node findOrCreateNodeForPlayer(IPlayer player, boolean refresh) {
		
		
		Node node = playersIds2nodes.get(player.getUniqueId());
		
		if (node == null) {
			logger.debug("no node for player "+player+"; creating one");
			
			node = createNodeForPlayer(player);
			playersIds2nodes.put(player.getUniqueId(), node);
		} else if (refresh) {
			updateDataForPlayer(player, node);
		}
		
		return node;
	}
	
	protected Node findOrCreateNodeForItem(IItem item) {
		return findOrCreateNodeForItem(item, false);
	}
	
	protected Node findOrCreateNodeForItem(IItem item, boolean refresh) {
		
		Node node = itemsIds2nodes.get(item.getUniqueId());
		
		if (node == null) {
			node = createNodeForItem(item);
			itemsIds2nodes.put(item.getUniqueId(), node);
		} else if (refresh) {
			updateDataForItem(item);
		}
		
		
		return node;
	}
	
	/**
	 * Get the parameters used to tune this display
	 * @return
	 */
	public WhiteboardParameters getParameters() {
		
		return parameters;
		
	}
	
	/**
	 * Updates the internal representation of the IItem 
	 * (label, type, object and so on), including links.
	 * @param item
	 * @param node
	 */
	private void updateDataForItem(IItem item, Node node) {
	
		try {
			node.setString(PREFUSE_NODE_FIELD_LABEL, item.getShortName());
			node.set(PREFUSE_NODE_FIELD_TYPE, TypeNode.ITEM.ordinal());
			node.set(PREFUSE_NODE_FIELD_OBJ, item);
			
			node.set(PREFUSE_NODE_FIELD_AGE, 0d);
			
			IPlayer author = game.getPlayer(item.getPlayerId());
			
			
			if ((author != null)) { // set the image only if the player is not displayed into the idea node 
				node.setString(PREFUSE_NODE_FIELD_IMG, Avatars.getPathForAvatar(author.getPicturePath()));
			} else {
				node.setString(PREFUSE_NODE_FIELD_IMG, null);
			}
			
			if ( (displayPlayersAsNodes) && (author != null) ) {
				findOrCreateEdgeBetween(
						findOrCreateNodeForPlayer(author),
						node,
						TypeEdge.PLAYER2ITEM
						);
			}
		
		} catch (RemoteException e) {
			synchroProblem(e);
		}
		
		eventDataChanged();
	}
	
	private void updateDataForItem(IItem item) {
		
		// TODO : not really efficient: data is updated two time when the node is created on such an event 
		// (however, this should never happen, as the creation should be done BEFORE data update)
		
		updateDataForItem(
				item, 
				findOrCreateNodeForItem(item)
				);
	}

	private void updateDataForComment(IComment comment, Node node, TypeEdge typeEdge) {

		assert comment != null;
		assert node != null;
	
		try {
			
			node.setString(PREFUSE_NODE_FIELD_LABEL, "");
			node.set(PREFUSE_NODE_FIELD_TYPE, TypeNode.COMMENT.ordinal());
			node.set(PREFUSE_NODE_FIELD_OBJ, comment);
			node.set(PREFUSE_NODE_FIELD_IMG, null);
			node.set(PREFUSE_NODE_FIELD_AGE, 0d);
			
			{
				Node nodeCommented = null;
				switch (typeEdge) {
				case COMMENT2ITEM: 
				
						nodeCommented = findOrCreateNodeForItem(
												game.getItem(
														comment.get()
														)
										);
					
					break;
				case COMMENT2IDEA: 
					nodeCommented = findOrCreateNodeForIdea(
											game.getIdea(
													comment.get()
													)
									);
					break;
				case COMMENT2COMMENT: 
					nodeCommented = findOrCreateNodeForComment(
											game.getComment(
													comment.get()
													),
											typeEdge
									);
					break;		
				default: 
					throw new RuntimeException("Unable to process case "+typeEdge+"; error in program logics");
				}
			
				findOrCreateEdgeBetween(
						node, 
						nodeCommented,
						typeEdge
						);
				
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		eventDataChanged();
		
		
	}
	
	protected Node createNodeForComment(IComment comment, TypeEdge typeEdge) {
		
		Node novelNode = prefusegraph.addNode();

		updateDataForComment(comment, novelNode, typeEdge);
		
		// reset age of related IIdea
		Node item = ideasIds2nodes.get(comment.get());
		if (item != null) {
			item.set(PREFUSE_NODE_FIELD_AGE, 0d);// TODO age
			//System.out.println("age to 0 for "+item);
		}
		
		return novelNode;
	}

	protected Node createNodeForItem(IItem item) {
		
		Node novelNode = prefusegraph.addNode();

		updateDataForItem(item, novelNode);
		
		return novelNode;
	}
	
	private void updateDataForPlayer(IPlayer player) {
	
		updateDataForPlayer(
				player, 
				findOrCreateNodeForPlayer(player)
				);
	}
	
	private void updateDataForPlayer(IPlayer player, Node node) {
		assert player != null;
		assert node != null;
	
		try {
			node.setString(PREFUSE_NODE_FIELD_LABEL, player.getShortName());
			node.set(PREFUSE_NODE_FIELD_TYPE, TypeNode.PLAYER.ordinal());
			node.set(PREFUSE_NODE_FIELD_OBJ, player);
			node.set(PREFUSE_NODE_FIELD_IMG, Avatars.getPathForAvatar(player.getPicturePath()));
			node.set(PREFUSE_NODE_FIELD_AGE, 0d);
		} catch (RemoteException e) {
			synchroProblem(e);
		}

		eventDataChanged();
		
	}
	
	protected Node createNodeForPlayer(IPlayer player) {
		assert player != null;
		
		Node novelNode = prefusegraph.addNode();

		updateDataForPlayer(player, novelNode);
		
		return novelNode;
	}
	
	protected Edge findOrCreateEdgeBetween(Node node1, Node node2, TypeEdge typeEdge) {
		
		assert node1 != null;
		assert node2 != null;
		
		Edge edge = prefusegraph.getEdge(node1, node2);
		
		if (edge == null) {
			logger.debug("edge between "+node1+" and "+node2+" does not exists; will create one");
			edge = prefusegraph.addEdge(node1, node2);
			edge.set(PREFUSE_EDGE_FIELD_TYPE, typeEdge.ordinal());
		}
		
		return edge;
		
	}

	
	protected Node createNodeForIdea(IIdea idea) {
		assert idea != null;
		
		Node novelNode = prefusegraph.addNode();

		updateDataForIdea(idea, novelNode);
		
		
		return novelNode;
	}
	
	
	public void eventIdeaAdded(IIdea idea) {
		assert idea != null;
		
		findOrCreateNodeForIdea(idea);
			
	}
	
	public void eventItemAdded(IItem item) {
		assert item != null;
		
		findOrCreateNodeForItem(item);
			
	}
	
	public void eventIdeaChanged(IIdea idea) {
		assert idea != null;
		
		updateDataForIdea(idea);
		
	}
	
	public void eventPlayerChanged(IPlayer player) {
		assert player != null;
		
		updateDataForPlayer(player);
	}

	public void eventPlayerRemoved(IPlayer player) {
		// TODO !!!
	}
	
	public void eventPlayerAdded(IPlayer player) {
	
		assert player != null;
		
		try {
			labelRenderer.currentPlayerID = guiTestMain.getPlayerId();
		} catch (RuntimeException e) {
		}
		
		if (!displayPlayersAsNodes)
			return;
		
		findOrCreateNodeForPlayer(player);
	}
	
	/**
	 * Should be called after any change in prefuse data; 
	 * for instance, manages the "autofit" feature.
	 */
	protected void eventDataChanged() {
		if (autoFit)
			fitToScreen();
		
	}
	
	public void addIdea(IIdea idea) {

		findOrCreateNodeForIdea(idea);
		
	}
	
	/**
	 * Has to be called at each RemoteException.
	 * Should display the synchro problem into the GUI, in order to warn the user; 
	 * moreover, should attempt to re-synchro.
	 */
	public void synchroProblem(RemoteException e) {
		logger.warn("synchro problem", e);
		
	}

	public void setGame(IGame game) {
		
		// TODO remove listener from game if not null !!!
		try {
			this.game = game;
			game.addListener(this);
			
			refreshAllIndirect();
			
		} catch (RemoteException e) {
			synchroProblem(e);
		}
		
			
	}
	
	
	/**
	 * Builds a representation of this label in which all the lines are shorter than 
	 * {@link #PREFUSE_NODE_TEXT_MAXWIDTH} chars. The function inserts "\n" chars 
	 * in order to achieve this. 
	 * @param label
	 * @return
	 */
	public String buildLabelForNode(String label) {

		
		StringBuffer sb = new StringBuffer(); // stores the result
		
		// for each line ended by a "\n"...
		StringTokenizer st1 = new StringTokenizer(label, "\n");
		
		while (st1.hasMoreElements()) {
			String currentLine = st1.nextToken();
			
			
			while (currentLine.length() > PREFUSE_NODE_TEXT_MAXWIDTH) { 
				// shorten this line
			
				// debug: logger.debug("shorting: "+currentLine);
				
				int lastSpace = -1;
				for (int i=PREFUSE_NODE_TEXT_MAXWIDTH; i>0; i--) {
					
					if (currentLine.charAt(i) == ' ') {
						lastSpace = i;
						// debug: logger.debug("found space at "+lastSpace);
						break;
					}
					
				}
				if (lastSpace == -1)
					lastSpace = PREFUSE_NODE_TEXT_MAXWIDTH;
				
				// find position for breaking the line
				sb.append(currentLine.substring(0, lastSpace));
				sb.append("\n");
				
				currentLine = currentLine.substring(lastSpace);
				
			}
			
			sb.append(currentLine);
			
		}
		
		return sb.toString();
		
	}
	
	
	public void centerOnRootIdea() {
		
		try {
			Node rootNode = ideasIds2nodes.get(game.getRootIdea().getUniqueId());
		
			VisualItem visualItem = vis.getVisualItem("graph", rootNode);
			
			prefuseDisplay.animatePanToAbs(
					new Point(
							(int)visualItem.getX(), 
							(int)visualItem.getY()
							), 
					PREFUSE_PAN_DURATION
					);
			
			
			//rootNode.get
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		// TODO
		
		// first find this item ands its position
	}
	
	public void fitToScreen() {
		zoomToFitControl.zommToFitNow();
	}
	
	public void zoomOne() {
		
		Rectangle2D r = prefuseDisplay.getItemBounds();
		
		prefuseDisplay.animateZoomAbs(
				new Point((int)r.getCenterX(), (int)r.getCenterY()), 
				1d/prefuseDisplay.getScale(), 
				PREFUSE_ZOOM_DURATION
				);
		// TODO
	}
	
	public void zoomOut() {
		
		Rectangle2D r = prefuseDisplay.getItemBounds();
		
		prefuseDisplay.animateZoomAbs(
				new Point((int)r.getCenterX(), (int)r.getCenterY()), 
				0.8, 
				PREFUSE_ZOOM_DURATION
				);
		
	}
	
	public void zoomIn() {
		
		
		Rectangle2D r = prefuseDisplay.getItemBounds();
		
		prefuseDisplay.animateZoomAbs(
				new Point((int)r.getCenterX(), (int)r.getCenterY()), 
				1.2, 
				PREFUSE_ZOOM_DURATION
				);
		
	}
	

	/**
	 * especially relevant at init
	 */
	public void centerDisplayImmediat() {
	
		// TODO not item bounds !!!
		@SuppressWarnings("unused")
		Rectangle2D r = prefuseDisplay.getItemBounds();
		
		Point p = new Point(
				-PREFUSE_WHITEBOARD_WIDTH/2, 
				-PREFUSE_WHITEBOARD_HEIGHT/2
				); 
		
		prefuseDisplay.panToAbs(p);
		prefuseDisplay.zoomAbs(p, 1);
	}
	
	private void refreshPlayersDirect() throws RemoteException {
		
		Set<Integer> originalPlayersIds = new HashSet<Integer>(playersIds2nodes.keySet());
		
		if (displayPlayersAsNodes) {
			// iterate accross all the players retrieved from the server
			
			for (IPlayer player : game.getAllPlayers()) {
				
				// create or update 
				findOrCreateNodeForPlayer(player, true);
				
				// remove this player from the original ones
				originalPlayersIds.remove(player.getUniqueId());
				
			}
			
		}
		
		// remove all other players
		for (Integer playerToRemove : originalPlayersIds) {
			// remove from node 
			Node nodeToRemove = playersIds2nodes.get(playerToRemove);
			
			prefusegraph.removeNode(nodeToRemove);
			
			playersIds2nodes.remove(playerToRemove);
		}
	
	}
	
	public void refreshIdeasDirect() throws RemoteException {

		Set<Integer> originalIdeas = new HashSet<Integer>(ideasIds2nodes.keySet());
		
		// iterate accross all the players retrieved from the server
		
		for (IIdea idea : game.getAllIdeas()) {
			
			// create or update 
			findOrCreateNodeForIdea(idea, true);
			
			// remove this player from the original ones
			originalIdeas.remove(idea.getUniqueId());
			
		}
		
	
		// remove all other players
		for (Integer ideaToRemove : originalIdeas) {
			// remove from node 
			Node nodeToRemove = ideasIds2nodes.get(ideaToRemove);
			
			prefusegraph.removeNode(nodeToRemove);
			
			ideasIds2nodes.remove(ideaToRemove);
		}
	
	}
	
	public void refreshItemsDirect() throws RemoteException {

		Set<Integer> originalItems = new HashSet<Integer>(itemsIds2nodes.keySet());
		
		// iterate accross all the players retrieved from the server
		
		if (displayItemsAsNodes) {
			for (IItem item : game.getAllItems()) {
				
				// create or update 
				findOrCreateNodeForItem(item, true);
				
				// remove this player from the original ones
				originalItems.remove(item.getUniqueId());
				
			}
		}		
	
		// remove all other players
		for (Integer itemToRemove : originalItems) {
			// remove from node 
			Node nodeToRemove = itemsIds2nodes.get(itemToRemove);
			
			prefusegraph.removeNode(nodeToRemove);
			
			itemsIds2nodes.remove(itemToRemove);
		}
	
	}
	public void refreshCommentDirect() throws RemoteException {

		Set<Integer> originalComments = new HashSet<Integer>(commentIds2nodes.keySet());
		
		// iterate accross all the players retrieved from the server
		
			for (IComment com : game.getAllComments()) {
				
				// create or update 
				findOrCreateNodeForComment(com, TypeEdge.COMMENT2IDEA, true);
				
				// remove this player from the original ones
				originalComments.remove(com.getUniqueId());
				
			}
	
		// remove all other players
		for (Integer itemToRemove : originalComments) {
			// remove from node 
			Node nodeToRemove = commentIds2nodes.get(itemToRemove);
			
			prefusegraph.removeNode(nodeToRemove);
			
			commentIds2nodes.remove(itemToRemove);
		}
	
	}
	
	/**
	 * Refresh everything from data (kind of a re-syncing !).
	 * Adds the links, nodes and everything required; 
	 * also removes the entities that shouldn't be there.
	 */
	public void refreshAllDirect() {
	
		try {
			refreshIdeasDirect();
			refreshPlayersDirect();
			refreshItemsDirect();
			refreshCommentDirect();
			
		} catch (RemoteException e) {
			synchroProblem(e);
		}
			
	}
	
	public void refreshAllIndirect() {
		
		compositeNet.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				refreshAllDirect();
			}
		});
	}

	protected void readParameters() {
		this.displayItemsAsNodes = (Boolean)parameters.getValue(parameters.prefuseDisplayItemsAsNodes);
		this.displayPlayersAsNodes = (Boolean)parameters.getValue(parameters.prefuseDisplayPlayersAsNodes);
		prefuseDisplay.setHighQuality((Boolean)parameters.getValue(parameters.prefuseDisplayHighQuality));


	}
	@Override
	public void notifyParameterEvent(EventParameterAbstract parameterEvent) {
		
		if (!(parameterEvent instanceof EventParameterValueChanged))
			return;
		
		if (!parameters.containsParameter(parameterEvent.parameter))
			return;
		
		
		if (parameterEvent.parameter == parameters.prefuseDisplayItemsAsNodes) {
			this.displayItemsAsNodes = (Boolean)parameters.getValue(parameters.prefuseDisplayItemsAsNodes);
			refreshAllDirect();
			return;
		}
		
		if (parameterEvent.parameter == parameters.prefuseDisplayPlayersAsNodes) {
			this.displayPlayersAsNodes = (Boolean)parameters.getValue(parameters.prefuseDisplayPlayersAsNodes);
			refreshAllDirect();
			return;
		}
		
		if (parameterEvent.parameter == parameters.prefuseDisplayHighQuality) {
			prefuseDisplay.setHighQuality((Boolean)parameters.getValue(parameters.prefuseDisplayHighQuality));
			return;
		}
		
		
		
	}
	
	/**
	 * True when already switched to a low perf mode (or user asked for not being more disturbed)
	 */
	
	
	private boolean lowPerformanceProcessed = false;
	
	@Override
	public void notifyLowPerformance(final String value) {
	
		// one unique event is enough; deregister
	
		if (lowPerformanceProcessed)
			return;
		
		lowPerformanceProcessed = true;
		
		
		final ClientWhiteboardSWT thise = this;
		
		
		compositeNet.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {

				threadMonitorPrefusePerformance.removeListener(thise);

				MessageBox mb = new MessageBox(compositeNet.getShell(), SWT.YES | SWT.NO);
				mb.setMessage(
						"L'affichage du réseau semble lent ("+value+"). " +
						"Désirez-vous diminuer la qualité d'affichage afin d'améliorer sa fluidité ? " +
						"Vous pourrez ensuite modifier ce réglage dans le panneau de paramètres.");
				mb.setText("Affichage lent");
				if (mb.open() == SWT.YES) {
					parameters.setParameterValue(parameters.prefuseDisplayHighQuality, Boolean.FALSE);
				}
				
			}
		});
		
	}

	@Override
	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
				
		final int objectid = e.getObjectId();
		IIdea idea = game.getIdea(objectid);
		eventIdeaAdded(idea);
		
	}

	@Override
	public void ItemCreated(GameObjectEvent e) throws RemoteException {

		// TODO en faire qqchose ? 
		
		// in this version, items are only added when they appear in an idea.
		
		final int objectid = e.getObjectId();
		IItem item = game.getItem(objectid);
		
		if (item == null)
			logger.warn("unable to find item "+objectid+" in the local copy of data ?!");
		else 
			eventItemAdded(item);
		
	}

	@Override
	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {

		final int objectid = e.getObjectId();
		IComment comment = game.getComment(objectid);
		
		if (comment == null)
			logger.warn("unable to find item "+objectid+" in the local copy of data ?!");
		else 
			findOrCreateNodeForComment(comment, TypeEdge.COMMENT2IDEA);
		
	}

	@Override
	public void playerJoined(PlayerEvent e) throws RemoteException {
		
		final int playerId = e.getPlayerId();
		IPlayer player = game.getPlayer(playerId);
		eventPlayerAdded(player);
		
	}

	@Override
	public void playerLeft(PlayerEvent e) throws RemoteException {
		
		final int playerId = e.getPlayerId();
		IPlayer player = game.getPlayer(playerId);
		eventPlayerRemoved(player);
		
	}

	

	
	/* (non-Javadoc)
	 * @see events.IEventListener#IdeaLinkCreated(events.LinkEvent)
	 */
	@Override
	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
		
		for (Pair<Integer,Integer> currentPair : e) {
			
			Integer from = currentPair.a;
			Integer to = currentPair.b;
		
			IIdea ideaFrom = game.getIdea(from);
			IIdea ideaTo = game.getIdea(to);
			
			findOrCreateEdgeBetween(
					findOrCreateNodeForIdea(ideaFrom),
					findOrCreateNodeForIdea(ideaTo),
					TypeEdge.IDEA2IDEA
					);
		}
		
	}

	/* (non-Javadoc)
	 * @see events.IEventListener#endOfGame()
	 */
	@Override
	public void endOfGame() throws RemoteException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public ISelectionControl getSelectionControl() {
		return selectionControl;
	}

	public void setActionsEnabled(boolean enabled) {
		
		contextualMenuControl.setActionsEnabled(enabled);
	}

    
	
}
