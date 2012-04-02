package client.gui.prefuse;

import java.awt.geom.Rectangle2D;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.ZoomToFitControl;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;

/**
 * A small change in the zoom to fit prefuse component, which enables the explicit call 
 * of the "zoom to fit" action via a method rather than by a mouse event.
 * 
 * @author Samuel Thiriot
 *
 */
public class MyZoomToFitControl extends ZoomToFitControl {

	private final Display display;
	
	/**
	 * Sadly, we have to declare this one again, as the inherited one isn't published :-(
	 */
    private String m_group = Visualization.ALL_ITEMS;

    /**
     * Idem
     */
    private long m_duration = 2000;

    
	public MyZoomToFitControl(Display display) {
		this.display = display;
	}

	public MyZoomToFitControl(Display display, String group) {
		super(group);
		this.display = display;
		this.m_group = group;
	}

	public MyZoomToFitControl(Display display, int button) {
		super(button);
		this.display = display;

	}

	public MyZoomToFitControl(Display display, String group, int button) {
		super(group, button);
		this.display = display;
		this.m_group = group;
	}

	public MyZoomToFitControl(Display display, String group, int margin, long duration,
			int button) {
		super(group, margin, duration, button);
		this.display = display;
		this.m_group = group;
		this.m_duration = duration;
	}


	public void zommToFitNow() {
		if ( !display.isTranformInProgress() )
	        {
	            Visualization vis = display.getVisualization();
	            Rectangle2D bounds = vis.getBounds(m_group);
	            GraphicsLib.expand(bounds, getMargin() + (int)(1/display.getScale()));
	            DisplayLib.fitViewToBounds(display, bounds, m_duration);
	        }

	}
	
}
