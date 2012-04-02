package client.gui.prefuse;

import fr.research.samthiriot.commons.parameters.GroupParameters;
import fr.research.samthiriot.commons.parameters.Parameters;
import fr.research.samthiriot.commons.parameters.basics.BooleanParameter;

public class WhiteboardParameters extends GroupParameters {

	public final Parameters prefuseParams;
	
	public final BooleanParameter prefuseDisplayPlayersAsNodes;
	public final BooleanParameter prefuseDisplayItemsAsNodes;
	
	public final BooleanParameter prefuseDisplayHighQuality;
	
	public WhiteboardParameters() {
		
		prefuseParams = new Parameters();
		
		prefuseDisplayPlayersAsNodes = new BooleanParameter("joueurs comme noeuds", "afficher les joueurs comme noeuds");
		prefuseParams.addParameterWithDefaultValue(prefuseDisplayPlayersAsNodes, true, Boolean.FALSE);
		
		prefuseDisplayItemsAsNodes = new BooleanParameter("items comme noeuds", "afficher les items comme noeuds");
		prefuseParams.addParameterWithDefaultValue(prefuseDisplayItemsAsNodes, true, Boolean.TRUE);
		
		prefuseDisplayHighQuality = new BooleanParameter("haute qualité", "utiliser un rendu de haute qualité (plus couteux)");
		prefuseParams.addParameterWithDefaultValue(prefuseDisplayHighQuality, true, Boolean.TRUE);
		
		addSubParameters("visu", "visu", prefuseParams);
		
	}
	
}
