package client.gui;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import data.IPlayer;

public class GuiPlayerInfos {

	private Logger logger = Logger.getLogger("client.gui.playerinfo");
	

	private Composite compositeHost = null;
	
	/**
	 * Convenience access to the display retrieved from the compositeHost. 
	 */
	private Display display = null;
	
	/**
	 * Stores the avatar image (if any)
	 */
	private Image sourceImage = null;
		
	private Canvas imageDisplay = null;
	
	/**
	 * the player to display
	 */
	private IPlayer player = null;
	

	public GuiPlayerInfos(Composite compositeHost) {
		
		this.compositeHost = compositeHost;
	}
	
	/**
	 * Removes any info related to the player
	 */
	private void clearDataPlayer() {
		// TODO
	}
	
	/**
	 * Displays or update data from player
	 */
	private void populateDataPlayer() {
		// TODO
	}
	
	public void setPlayer(IPlayer player) {
		
		if (player == this.player)
			return; // quick exit when no change
		
		if (this.player != null) // clear data from previous player
			clearDataPlayer();
		
		this.player = player;
		
		if (this.player != null)
			populateDataPlayer();
		
	}
	
	private Image loadImage(String filename) { 
		
		// free image 
        if(sourceImage!=null && !sourceImage.isDisposed()){
            sourceImage.dispose();
            sourceImage=null;
        }
     
        // loads the image
        sourceImage = new Image(display, filename);
        
        // displays it into the Canvas
        
        GC newGC = new GC(sourceImage);

      //  newGC.drawImage( sourceImage);

        
        return sourceImage;
}
	
	public void init() {
		
	}
	
}
