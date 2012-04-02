package client.gui.prefuse;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.ForceSimulator;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import client.gui.prefuse.ClientWhiteboardSWT.TypeEdge;

/**
 * An adaptation of the standard ForceDirectedLayout.
 * <ul>
 * <li>enables the definition of various spring parameters depending to link types</li>
 * <li>TODO: eviter que "tout bouge trop" => par exemple en ajoutant une des forces prefuse type viscosité</li>
 * </ul>
 * 
 * @author Samuel THiriot
 *
 */
public final class MyMultiplexForceDirectedLayout extends ForceDirectedLayout {

	public MyMultiplexForceDirectedLayout(String graph) {
		super(graph);
	}

	public MyMultiplexForceDirectedLayout(String group, boolean enforceBounds) {
		super(group, enforceBounds);
	}

	public MyMultiplexForceDirectedLayout(String group, boolean enforceBounds,
			boolean runonce) {
		super(group, enforceBounds, runonce);
	}

	public MyMultiplexForceDirectedLayout(String group, ForceSimulator fsim,
			boolean enforceBounds) {
		super(group, fsim, enforceBounds);
	}

	public MyMultiplexForceDirectedLayout(String group, ForceSimulator fsim,
			boolean enforceBounds, boolean runonce) {
		super(group, fsim, enforceBounds, runonce);
	}

	@Override
	protected float getSpringCoefficient(EdgeItem e) {
		

		final int typeId = (Integer)e.get(ClientWhiteboardSWT.PREFUSE_EDGE_FIELD_TYPE);		
		
		final TypeEdge type = TypeEdge.values()[typeId];
		
		return type.edgeSpringCoef;
		
	
	}

	@Override
	protected float getSpringLength(EdgeItem e) {
		
		final int typeId = (Integer)e.get(ClientWhiteboardSWT.PREFUSE_EDGE_FIELD_TYPE);
		final TypeEdge type = TypeEdge.values()[typeId];
		
		return type.edgeSpringLength;
	}
	
	@Override
    protected float getMassValue(VisualItem n) {
		
		return super.getMassValue(n);
		
		/*
		final Object theItem = n.get(WhiteboardSWT.PREFUSE_NODE_FIELD_OBJ);
		
		if (theItem == null) {	// if this happens often, has to be in this first position
			return super.getMassValue(n);
		} else if (theItem instanceof IIdea) {
			return 1;
		} else if (theItem instanceof IItem) {
			return 0.3f;
		} else {
			return super.getMassValue(n);
		}
		*/
		
    }
	

}
