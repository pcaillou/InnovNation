package client.gui.prefuse;


//AD import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;

import prefuse.Constants;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.ImageFactory;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.StringLib;
import prefuse.visual.VisualItem;
import client.LocalCopyOfGame;
import client.gui.prefuse.ClientWhiteboardSWT.TypeNode;
import data.IComment;
import data.IIdea;
import data.IItem;
import data.IPlayer;


/**
 * Renderer that draws a label, which consists of a text string,
 * an image, or both.
 * 
 * <p>When created using the default constructor, the renderer attempts
 * to use text from the "label" field. To use a different field, use the
 * appropriate constructor or use the {@link #setTextField(String)} method.
 * To perform custom String selection, subclass this Renderer and override the 
 * {@link #getText(VisualItem)} method. When the text field is
 * <code>null</code>, no text label will be shown. Labels can span multiple
 * lines of text, determined by the presence of newline characters ('\n')
 * within the text string.</p>
 * 
 * <p>By default, no image is shown. To show an image, the image field needs
 * to be set, either using the appropriate constructor or the
 * {@link #setImageField(String)} method. The value of the image field should
 * be a text string indicating the location of the image file to use. The
 * string should be either a URL, a file located on the current classpath,
 * or a file on the local filesystem. If found, the image will be managed
 * internally by an {@link ImageFactory} instance, which maintains a
 * cache of loaded images.</p>
 * 
 * <p>The position of the image relative to text can be set using the
 * {@link #setImagePosition(int)} method. Images can be placed to the
 * left, right, above, or below the text. The horizontal and vertical
 * alignments of either the text or the image can be set explicitly
 * using the appropriate methods of this class (e.g.,
 * {@link #setHorizontalTextAlignment(int)}). By default, both the
 * text and images are centered along both the horizontal and
 * vertical directions.</p>
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class MyBasicLabelRenderer extends AbstractShapeRenderer {

    protected ImageFactory m_images = null;
    protected String m_delim = "\n";
    
    protected String m_labelName = "label";
    protected String m_imageName = null;
    
    protected int m_xAlign = Constants.CENTER;
    protected int m_yAlign = Constants.CENTER;
    protected int m_hTextAlign = Constants.CENTER;
    protected int m_vTextAlign = Constants.CENTER;
    
    protected int m_horizBorder = 2;
    protected int m_vertBorder  = 3;
    protected int m_imageMargin = 0;
    protected int m_arcWidth    = 0;
    protected int m_arcHeight   = 0;

    protected int maxx   = 10;
    protected int maxy   = 10;

	protected String fieldForObject = "obj";

	
	/**
	 * Ajoute the circle 
	 * 
	 */
	private final static int rgbFillCircle = ColorLib.rgb(250, 213, 6);
	private final static int rgbShapeCircle = ColorLib.rgb(250, 172, 6);
	private final static int circle_width = 20;

    /**
     * Space between the top of the shape and the actual start of the rect. 
     */
    protected final double ideas_marginLeft = 0;
    protected final double ideas_marginRight = 10;
    protected final double ideas_marginTop = 20;
    protected final double ideas_marginBottom = 0;

    protected final int ideas_baseFirstLine = 4;

    private final int ideas_marginWidth = (int)(ideas_marginLeft + ideas_marginRight);
    private final int ideas_marginHeight = (int)(ideas_marginTop + ideas_marginBottom);
    
    /**
     * Used only for default (not idea)
     */
    protected int m_hImageAlign = Constants.CENTER;
    protected int m_vImageAlign = Constants.CENTER;
    protected int m_imagePos = Constants.LEFT;
    
    
    protected int m_maxTextWidth = -1;
    
    /** Transform used to scale and position images */
    AffineTransform m_transform = new AffineTransform();
    
    /** The holder for the currently computed bounding box */
    protected RectangularShape m_bbox  = new Rectangle2D.Double();
    protected Point2D m_pt = new Point2D.Double(); // temp point
    protected Font    m_font; // temp font holder
    protected String    m_text; // label text
    protected Dimension m_textDim = new Dimension(); // text width / height

    public Integer currentPlayerID = 0;
    
    /**
     * Create a new LabelRenderer. By default the field "label" is used
     * as the field name for looking up text, and no image is used.
     */
    public MyBasicLabelRenderer(Integer currentPlayerID) {
    	this.currentPlayerID = currentPlayerID;
    }
    
    /**
     * Create a new LabelRenderer. Draws a text label using the given
     * text data field and does not draw an image.
     * @param textField the data field for the text label.
     */
    public MyBasicLabelRenderer(String textField, Integer currentPlayerID) {
        this.setTextField(textField);
    	this.currentPlayerID = currentPlayerID;
    }
    
    /**
     * Create a new LabelRenderer. Draws a text label using the given text
     * data field, and draws the image at the location reported by the
     * given image data field.
     * @param textField the data field for the text label
     * @param imageField the data field for the image location. This value
     * in the data field should be a URL, a file within the current classpath,
     * a file on the filesystem, or null for no image. If the
     * <code>imageField</code> parameter is null, no images at all will be
     * drawn.
     */
    public MyBasicLabelRenderer(String textField, String imageField, Integer currentPlayerID) {
        setTextField(textField);
        setImageField(imageField);
    	this.currentPlayerID = currentPlayerID;

    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Rounds the corners of the bounding rectangle in which the text
     * string is rendered. This will only be seen if either the stroke
     * or fill color is non-transparent.
     * @param arcWidth the width of the curved corner
     * @param arcHeight the height of the curved corner
     */
    public void setRoundedCorner(int arcWidth, int arcHeight) {
        if ( (arcWidth == 0 || arcHeight == 0) && 
            !(m_bbox instanceof Rectangle2D) ) {
            m_bbox = new Rectangle2D.Double();
        } else {
            if ( !(m_bbox instanceof RoundRectangle2D) )
                m_bbox = new RoundRectangle2D.Double();
            ((RoundRectangle2D)m_bbox)
                .setRoundRect(0,0,10,10,arcWidth,arcHeight);
            m_arcWidth = arcWidth;
            m_arcHeight = arcHeight;
        }
    }

    /**
     * Get the field name to use for text labels.
     * @return the data field for text labels, or null for no text
     */
    public String getTextField() {
        return m_labelName;
    }
    
    /**
     * Set the field name to use for text labels.
     * @param textField the data field for text labels, or null for no text
     */
    public void setTextField(String textField) {
        m_labelName = textField;
    }
    
    /**
     * Sets the maximum width that should be allowed of the text label.
     * A value of -1 specifies no limit (this is the default).
     * @param maxWidth the maximum width of the text or -1 for no limit
     */
    public void setMaxTextWidth(int maxWidth) {
        m_maxTextWidth = maxWidth;
    }
    
    /**
     * Returns the text to draw. Subclasses can override this class to
     * perform custom text selection.
     * @param item the item to represent as a <code>String</code>
     * @return a <code>String</code> to draw
     */
    protected String getText(VisualItem item) {
        String s = null;
        if ( item.canGetString(m_labelName) ) {
            return item.getString(m_labelName);            
        }
        return s;
    }

    // ------------------------------------------------------------------------
    // Image Handling
    
    /**
     * Get the data field for image locations. The value stored
     * in the data field should be a URL, a file within the current classpath,
     * a file on the filesystem, or null for no image.
     * @return the data field for image locations, or null for no images
     */
    public String getImageField() {
        return m_imageName;
    }
    
    /**
     * Set the data field for image locations. The value stored
     * in the data field should be a URL, a file within the current classpath,
     * a file on the filesystem, or null for no image. If the
     * <code>imageField</code> parameter is null, no images at all will be
     * drawn.
     * @param imageField the data field for image locations, or null for
     * no images
     */
    public void setImageField(String imageField) {
        if ( imageField != null ) m_images = new ImageFactory();
        m_imageName = imageField;
    }
    
    /**
     * Sets the maximum image dimensions, used to control scaling of loaded
     * images. This scaling is enforced immediately upon loading of the image.
     * @param width the maximum width of images (-1 for no limit)
     * @param height the maximum height of images (-1 for no limit)
     */
    public void setMaxImageDimensions(int width, int height) {
        if ( m_images == null ) m_images = new ImageFactory();
        maxx=width;
        maxy=height;
        m_images.setMaxImageDimensions(width, height);
    }
    
    /**
     * Returns a location string for the image to draw. Subclasses can override 
     * this class to perform custom image selection beyond looking up the value
     * from a data field.
     * @param item the item for which to select an image to draw
     * @return the location string for the image to use, or null for no image
     */
    protected String getImageLocation(VisualItem item) {
        return item.canGetString(m_imageName)
                ? item.getString(m_imageName)
                : null;
    }
    
    /**
     * Get the image to include in the label for the given VisualItem.
     * @param item the item to get an image for
     * @return the image for the item, or null for no image
     */
    protected Image getImage(VisualItem item) {
        String imageLoc = getImageLocation(item);
		if (imageLoc==null) return null;
//		System.out.println("try to find : "+imageLoc+" from "+item.toString());		
//		ImageIcon im=new ImageIcon(getClass().getResource("/"+imageLoc));
//		Image imag=im.getImage();
/*		int imx=im.getIconWidth();
		int imy=im.getIconHeight();
		if ((imx>maxx)|(imy>maxy))
		{
			double sc=Math.max(((double)imx)/maxx,((double)imy)/maxy);
			System.out.println("sc "+(int)(imx/sc)+"/"+(int)(imx/sc)+"/"+sc);
			imag=im.getImage().getScaledInstance((int)(imx/sc), (int)(imy/sc), Image.SCALE_DEFAULT);
		}*/
//		m_images.addImage(imageLoc, imag);
		
//        return ( imageLoc == null ? null : imag );
        return ( imageLoc == null ? null : m_images.getImage("/"+imageLoc) );
    }
    
    
    // ------------------------------------------------------------------------
    // Rendering
    
    private String computeTextDimensions(VisualItem item, String text,
                                         double size)
    {
        // put item font in temp member variable
        m_font = item.getFont();
        // scale the font as needed
        if ( size != 1 ) {
            m_font = FontLib.getFont(m_font.getName(), m_font.getStyle(),
                                     size*m_font.getSize());
        }
        
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);
        StringBuffer str = null;
        
        // compute the number of lines and the maximum width
        int nlines = 1, w = 0, start = 0, end = text.indexOf(m_delim);
        m_textDim.width = 0;
        String line;
        for ( ; end >= 0; ++nlines ) {
            w = fm.stringWidth(line=text.substring(start,end));
            // abbreviate line as needed
            if ( m_maxTextWidth > -1 && w > m_maxTextWidth ) {
                if ( str == null )
                    str = new StringBuffer(text.substring(0,start));
                str.append(StringLib.abbreviate(line, fm, m_maxTextWidth));
                str.append(m_delim);
                w = m_maxTextWidth;
            } else if ( str != null ) {
                str.append(line).append(m_delim);
            }
            // update maximum width and substring indices
            m_textDim.width = Math.max(m_textDim.width, w);
            start = end+1;
            end = text.indexOf(m_delim, start);
        }
        w = fm.stringWidth(line=text.substring(start));
        // abbreviate line as needed
        if ( m_maxTextWidth > -1 && w > m_maxTextWidth ) {
            if ( str == null )
                str = new StringBuffer(text.substring(0,start));
            str.append(StringLib.abbreviate(line, fm, m_maxTextWidth));
            w = m_maxTextWidth;
        } else if ( str != null ) {
            str.append(line);
        }
        // update maximum width
        m_textDim.width = Math.max(m_textDim.width, w);
        
        // compute the text height
        m_textDim.height = fm.getHeight() * nlines;
        
        return str==null ? text : str.toString();
    }
   
    protected RectangularShape getRawShapeItem(VisualItem item) {
        
    	m_text = getText(item);
        
        Image  img  = getImage(item);
        double size = item.getSize();
        
        // get image dimensions
        double iw=0, ih=0;
        if ( img != null ) {
            ih = img.getHeight(null);
            iw = img.getWidth(null);    
        }
        
        // get text dimensions
        int tw=0, th=0;
        if ( m_text != null ) {
            m_text = computeTextDimensions(item, m_text, size);
            th = m_textDim.height + ideas_marginHeight;
            tw = m_textDim.width + ideas_marginWidth;   
        }
        
        // get bounding box dimensions
        double w=0, h=0;
            w = tw + size*(iw +2*m_horizBorder
                   + (tw>0 && iw>0 ? m_imageMargin : 0));
            h = Math.max(th, size*ih) + size*2*m_vertBorder;
        
        // get the top-left point, using the current alignment settings
        getAlignedPoint(m_pt, item, w, h, m_xAlign, m_yAlign);
        
        if ( m_bbox instanceof RoundRectangle2D ) {
            RoundRectangle2D rr = (RoundRectangle2D)m_bbox;
            rr.setRoundRect(m_pt.getX(), m_pt.getY(), w, h,
                            size*m_arcWidth, size*m_arcHeight);
        } else {
            m_bbox.setFrame(m_pt.getX(), m_pt.getY(), w, h);
        }
        return m_bbox;
    }
    
    protected RectangularShape getRawShapeComment(VisualItem item, IComment comment) {

        int count = comment.getTokensCount();
        if (count>0) {
        	m_text = "+"+count;
        } else if (count<0) {
        	m_text = Integer.toString(count);
        } else {
        	m_text = "";
        }
        m_text=m_text+" "+comment.getText().substring(0, 20);
        if (comment.getText().length()>20) m_text=m_text+"..";
        Image  img  = getImage(item);
        double size = item.getSize();
        
        // get image dimensions
        double iw=0, ih=0;
        if ( img != null ) {
            ih = img.getHeight(null);
            iw = img.getWidth(null);    
        }
        
        // get text dimensions
        int tw=0, th=0;
        if ( m_text != null ) {
            m_text = computeTextDimensions(item, m_text, size);
            th = m_textDim.height;
            tw = m_textDim.width;   
        }
        
        // get bounding box dimensions
        double w=0, h=0;
            w = tw + size*(iw +2*m_horizBorder
                   + (tw>0 && iw>0 ? m_imageMargin : 0));
            h = Math.max(th, size*ih) + size*2*m_vertBorder;
        
        // get the top-left point, using the current alignment settings
        getAlignedPoint(m_pt, item, w, h, m_xAlign, m_yAlign);
        
        if ( m_bbox instanceof RoundRectangle2D ) {
            RoundRectangle2D rr = (RoundRectangle2D)m_bbox;
            rr.setRoundRect(m_pt.getX(), m_pt.getY(), w, h,
                            size*m_arcWidth, size*m_arcHeight);
        } else {
            m_bbox.setFrame(m_pt.getX(), m_pt.getY(), w, h);
        }
        return m_bbox;
    }
    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    protected RectangularShape getRawShapeIdea(VisualItem item, IIdea idea) {
    	    	
    	if (idea != null)
    		m_text = getText(item)+"\n"+idea.getDesc();
    	else 	
    		m_text = getText(item);
    	

        Image  img  = getImage(item);
        double size = item.getSize();
        
        // get image dimensions
        double iw=0, ih=0;
        if ( img != null ) {
            ih = img.getHeight(null);
            iw = img.getWidth(null);    
        }
        
        // get text dimensions
        int tw=0, th=0;
        if ( m_text != null ) {
        	if ((idea == null) || (m_text.length() > idea.getDesc().length())) {
        		m_text = computeTextDimensions(item, m_text, size);	
        	} else {
        		computeTextDimensions(item, idea.getDesc(), size);
        	}
            th = m_textDim.height+ ideas_marginHeight;
            tw = m_textDim.width + ideas_marginWidth;   
        }
        
        // get bounding box dimensions
        double w=0, h=0;
            w = tw + size*(iw +2*m_horizBorder
                   + (tw>0 && iw>0 ? m_imageMargin : 0));
            h = Math.max(th, size*ih) + size*2*m_vertBorder;
        
        // get the top-left point, using the current alignment settings
        getAlignedPoint(m_pt, item, w, h, m_xAlign, m_yAlign);
        
        if ( m_bbox instanceof RoundRectangle2D ) {
            RoundRectangle2D rr = (RoundRectangle2D)m_bbox;
            rr.setRoundRect(m_pt.getX(), m_pt.getY(), w, h,
                            size*m_arcWidth, size*m_arcHeight);
        } else {
            m_bbox.setFrame(m_pt.getX(), m_pt.getY(), w, h);
        }
        m_text = getText(item);
        return m_bbox;
    }
    
    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    protected Shape getRawShape(VisualItem item) {
    	
    	Integer idTypeItem = (Integer)item.get(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_TYPE);
    	
    	if (idTypeItem == null) // quick return
    		return getRawShapeDefault(item);
    	
    	TypeNode typeNode = TypeNode.values()[idTypeItem];
    	
    	
    	switch (typeNode) {
    	
    	case IDEA:
    		Object obj = item.get(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_OBJ);
    		return getRawShapeIdea(item, (IIdea)obj);
    		
    	case ITEM:
    		return getRawShapeItem(item);
    		
    	case COMMENT:
    		Object obj2 = item.get(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_OBJ);
    		return getRawShapeComment(item, (IComment)obj2);

    	case POSITIVECOMMENT:
    		Object obj3 = item.get(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_OBJ);
    		return getRawShapeComment(item, (IComment)obj3);
    		
    	case NEGATIVECOMMENT:
    		Object obj4 = item.get(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_OBJ);
    		return getRawShapeComment(item, (IComment)obj4);

    	case PLAYER:
    		return getRawShapeDefault(item);
    	
    	default:
    		throw new RuntimeException("Node type not managed: "+typeNode);
    	}
    	
    	
    }
    
    protected Shape getRawShapeDefault(VisualItem item) {
    	
    
        m_text = getText(item);
        Image  img  = getImage(item);
        double size = item.getSize();
        
        // get image dimensions
        double iw=0, ih=0;
        if ( img != null ) {
            ih = img.getHeight(null);
            iw = img.getWidth(null);    
        }
        
        // get text dimensions
        int tw=0, th=0;
        if ( m_text != null ) {
            m_text = computeTextDimensions(item, m_text, size);
            th = m_textDim.height;
            tw = m_textDim.width;   
        }
        
        // get bounding box dimensions
        double w=0, h=0;
            w = tw + size*(iw +2*m_horizBorder
                   + (tw>0 && iw>0 ? m_imageMargin : 0));
            h = Math.max(th, size*ih) + size*2*m_vertBorder;
        
        // get the top-left point, using the current alignment settings
        getAlignedPoint(m_pt, item, w, h, m_xAlign, m_yAlign);
        
        if ( m_bbox instanceof RoundRectangle2D ) {
            RoundRectangle2D rr = (RoundRectangle2D)m_bbox;
            rr.setRoundRect(m_pt.getX(), m_pt.getY(), w, h,
                            size*m_arcWidth, size*m_arcHeight);
        } else {
            m_bbox.setFrame(m_pt.getX(), m_pt.getY(), w, h);
        }
        return m_bbox;
    }
    
    /**
     * Helper method, which calculates the top-left co-ordinate of an item
     * given the item's alignment.
     */
    protected static void getAlignedPoint(Point2D p, VisualItem item, 
            double w, double h, int xAlign, int yAlign)
    {
        double x = item.getX(), y = item.getY();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0; // safety check
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0; // safety check
        
        if ( xAlign == Constants.CENTER ) {
            x = x-(w/2);
        } else if ( xAlign == Constants.RIGHT ) {
            x = x-w;
        }
        if ( yAlign == Constants.CENTER ) {
            y = y-(h/2);
        } else if ( yAlign == Constants.BOTTOM ) {
            y = y-h;
        }
        p.setLocation(x,y);
    }
    
   public RectangularShape getIdeaShapeInside(VisualItem item,  final RectangularShape shape) {
       
	   AffineTransform at = getTransform(item);
	   
       RectangularShape shape2  = new Rectangle2D.Double(
       		shape.getX()+ideas_marginLeft,	// x
       		shape.getY()+ideas_marginTop ,// y
       		shape.getWidth()-ideas_marginWidth, // width
       		shape.getHeight()-ideas_marginHeight // height
       		);
       
       
       return (at==null || shape2==null ? shape2
                : (RectangularShape)at.createTransformedShape(shape2));
   }
   
   protected Shape getRawShapePlayer(VisualItem item) {
   
       m_text = getText(item);
       Image  img  = getImage(item);
       double size = item.getSize();
       
       // get image dimensions
       double iw=0, ih=0;
       if ( img != null ) {
           ih = img.getHeight(null);
           iw = img.getWidth(null);    
       }
       
       // get text dimensions
       int tw=0, th=0;
       if ( m_text != null ) {
           m_text = computeTextDimensions(item, m_text, size);
           th = m_textDim.height;
           tw = m_textDim.width;   
       }
       
       // get bounding box dimensions
       double w=0, h=0;
           w = tw + size*(iw +2*m_horizBorder
                  + (tw>0 && iw>0 ? m_imageMargin : 0));
           h = Math.max(th, size*ih) + size*2*m_vertBorder;
       
       // get the top-left point, using the current alignment settings
       getAlignedPoint(m_pt, item, w, h, m_xAlign, m_yAlign);
       
       if ( m_bbox instanceof RoundRectangle2D ) {
           RoundRectangle2D rr = (RoundRectangle2D)m_bbox;
           rr.setRoundRect(m_pt.getX(), m_pt.getY(), w, h,
                           size*m_arcWidth, size*m_arcHeight);
       } else {
           m_bbox.setFrame(m_pt.getX(), m_pt.getY(), w, h);
       }
       return m_bbox;
   }

   private Shape getShapePlayer(VisualItem item) {
       AffineTransform at = getTransform(item);
       Shape rawShape = getRawShapePlayer(item);
       return (at==null || rawShape==null ? rawShape 
                : at.createTransformedShape(rawShape));
   }
   
   private final void renderPlayer(Graphics2D g, VisualItem item, IPlayer player) {
	   
	   RectangularShape shape = (RectangularShape)getShapePlayer(item);
       if ( shape == null ) return;
       
       // fill the shape, if requested
       int type = getRenderType(item);
       if ( type==RENDER_TYPE_FILL || type==RENDER_TYPE_DRAW_AND_FILL )
           GraphicsLib.paint(g, item, shape, getStroke(item), RENDER_TYPE_FILL);

       // now render the image and text
       String text = m_text;
       Image  img  = getImage(item);
       if ( text == null && img == null )
           return;
                       
       double size = item.getSize();
       boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(),
                                       g.getTransform().getScaleY());
       double x = shape.getMinX() + size*m_horizBorder;
       double y = shape.getMinY() + size*m_vertBorder;
       
       // render image
       if ( img != null ) {            
           double w = size * img.getWidth(null);
           double h = size * img.getHeight(null);
           double ix=x, iy=y;
           
           // determine one co-ordinate based on the image position
           switch ( m_imagePos ) {
           case Constants.LEFT:
               x += w + size*m_imageMargin;
               break;
           case Constants.RIGHT:
               ix = shape.getMaxX() - size*m_horizBorder - w;
               break;
           case Constants.TOP:
               y += h + size*m_imageMargin;
               break;
           case Constants.BOTTOM:
               iy = shape.getMaxY() - size*m_vertBorder - h;
               break;
           default:
               throw new IllegalStateException(
                       "Unrecognized image alignment setting.");
           }
           
           // determine the other coordinate based on image alignment
           switch ( m_imagePos ) {
           case Constants.LEFT:
           case Constants.RIGHT:
               // need to set image y-coordinate
               switch ( m_vImageAlign ) {
               case Constants.TOP:
                   break;
               case Constants.BOTTOM:
                   iy = shape.getMaxY() - size*m_vertBorder - h;
                   break;
               case Constants.CENTER:
                   iy = shape.getCenterY() - h/2;
                   break;
               }
               break;
           case Constants.TOP:
           case Constants.BOTTOM:
               // need to set image x-coordinate
               switch ( m_hImageAlign ) {
               case Constants.LEFT:
                   break;
               case Constants.RIGHT:
                   ix = shape.getMaxX() - size*m_horizBorder - w;
                   break;
               case Constants.CENTER:
                   ix = shape.getCenterX() - w/2;
                   break;
               }
               break;
           }
           
           if ( useInt && size == 1.0 ) {
               // if possible, use integer precision
               // results in faster, flicker-free image rendering
               g.drawImage(img, (int)ix, (int)iy, null);
       
           } else {
               m_transform.setTransform(size,0,0,size,ix,iy);
               g.drawImage(img, m_transform, null);
           }
       }
       
       // render text
       int textColor = item.getTextColor();
       if ( text != null && ColorLib.alpha(textColor) > 0 ) {
           g.setPaint(ColorLib.getColor(textColor));
           g.setFont(m_font);
           FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);

           // compute available width
           double tw;
           switch ( m_imagePos ) {
           case Constants.TOP:
           case Constants.BOTTOM:
               tw = shape.getWidth() - 2*size*m_horizBorder;
               break;
           default:
               tw = m_textDim.width;
           }
           
           // compute available height
           double th;
           switch ( m_imagePos ) {
           case Constants.LEFT:
           case Constants.RIGHT:
               th = shape.getHeight() - 2*size*m_vertBorder;
               break;
           default:
               th = m_textDim.height;
           }
           
           // compute starting y-coordinate
           y += fm.getAscent();
           switch ( m_vTextAlign ) {
           case Constants.TOP:
               break;
           case Constants.BOTTOM:
               y += th - m_textDim.height;
               break;
           case Constants.CENTER:
               y += (th - m_textDim.height)/2;
           }
           
           // render each line of text
           int lh = fm.getHeight(); // the line height
           int start = 0, end = text.indexOf(m_delim);
           for ( ; end >= 0; y += lh ) {
               drawString(g, fm, text.substring(start, end), useInt, x, y, tw);
               start = end+1;
               end = text.indexOf(m_delim, start);   
           }
           drawString(g, fm, text.substring(start), useInt, x, y, tw);
       }
   
       // draw border
       if (type==RENDER_TYPE_DRAW || type==RENDER_TYPE_DRAW_AND_FILL) {
           MyGraphicsLib.paint(g,item,shape,getStroke(item),RENDER_TYPE_DRAW);
       }
   }

	   
   /**
    * Returns the shape describing the boundary of an item. The shape's
    * coordinates should be in abolute (item-space) coordinates.
    * @param item the item for which to get the Shape
    */
   public Shape getIdeaShape(VisualItem item, Shape rawShape) {
       AffineTransform at = getTransform(item);
       return (at==null || rawShape==null ? rawShape 
                : at.createTransformedShape(rawShape));
   }
   
   private final void renderItem(Graphics2D g, VisualItem item, IItem it) {

	   RectangularShape rawShape = getRawShapeIdea(item, null);
       
       RectangularShape shape = (RectangularShape)getIdeaShape(item, rawShape);
       
       if ( shape == null ) return;
       
       RectangularShape shape2 = getIdeaShapeInside(item, rawShape);
       
       // fill the shape, if requested
       int type = getRenderType(item);
       if ( type==RENDER_TYPE_FILL || type==RENDER_TYPE_DRAW_AND_FILL )
           MyGraphicsLib.paint(g, item, shape2, getStroke(item), RENDER_TYPE_FILL);

       
       // now render the text
       String text = m_text;
       //String textJetons = textJetons = it.getTotalBids().toString(); // TODO afficher différentiel !
    	   
       Image  img  = getImage(item);
       
       if ( text == null && img == null )
           return;
                       
       double size = item.getSize();
       boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(),
                                       g.getTransform().getScaleY());
       double x = shape.getMinX();
       double y = shape.getMinY();
       
       final double img_ix;
       final double img_iy;
       // compute future rendering of image
       if ( img != null ) {            
           double w = size * img.getWidth(null);
           @SuppressWarnings("unused")
           double h = size * img.getHeight(null);
           double ix=x, iy=y;
           
           // determine one co-ordinate based on the image position
           ix = shape.getMaxX() - w;
           
           img_ix = ix;
           img_iy = iy;
       
       } else { // useless, but the compiler is unable to understand such a thing !
           img_ix = 0;
           img_iy = 0;
       	
       }
       
       x = shape2.getMinX() + size*m_horizBorder;
       y = shape2.getMinY() + size*m_vertBorder;
       
       int textColor = item.getTextColor();
      
       /*
       // render the first line of text
       if ( textJetons != null && ColorLib.alpha(textColor) > 0 ) {
    	   g.setPaint(ColorLib.getColor(textColor));
           g.setFont(m_font);
           FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);

           // compute available width
           double tw = shape.getWidth(); // TODO moins img width !
               
           // compute starting y-coordinate
           double yJetons = 	shape.getMinY() + ideas_marginTop  
           						- ideas_baseFirstLine;
    
           // render this line of text
           drawString(
        		   g, 
        		   fm, 
        		   textJetons, 
        		   useInt, 
        		   shape.getMinX(), 
        		   yJetons, 
        		   tw
        		   );
           
       }*/
           
       // render text
       if ( text != null && ColorLib.alpha(textColor) > 0 ) {
           g.setPaint(ColorLib.getColor(textColor));
           g.setFont(m_font);
           FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);

           // compute available width
           double tw = shape2.getWidth() - 2*size*m_horizBorder;
               
           // compute available height
           double th = shape2.getHeight() - 2*size*m_vertBorder;
           
           // compute starting y-coordinate
           y += fm.getAscent();
           switch ( m_vTextAlign ) {
           case Constants.TOP:
               break;
           case Constants.BOTTOM:
               y += th - m_textDim.height;
               break;
           case Constants.CENTER:
               y += (th - m_textDim.height)/2;
           }
           
           // render each line of text
           int lh = fm.getHeight(); // the line height
           int start = 0, end = text.indexOf(m_delim);
           for ( ; end >= 0; y += lh ) {
               drawString(g, fm, text.substring(start, end), useInt, x, y, tw);
               start = end+1;
               end = text.indexOf(m_delim, start);   
           }
           drawString(g, fm, text.substring(start), useInt, x, y, tw);
       }
   
       // draw border
       if (type==RENDER_TYPE_DRAW || type==RENDER_TYPE_DRAW_AND_FILL) {
           MyGraphicsLib.paint(g,item,shape2,getStroke(item),RENDER_TYPE_DRAW);
       }
       
       // actually render the image
       if ( img != null ) {            
           
           if ( useInt && size == 1.0 ) {
               // if possible, use integer precision
               // results in faster, flicker-free image rendering
               g.drawImage(img, (int)img_ix, (int)img_iy, null);
           } else {
               m_transform.setTransform(size,0,0,size,img_ix,img_iy);
               g.drawImage(img, m_transform, null);
           }
       }
   }
		
  
   
   private final void renderIdea(Graphics2D g, VisualItem item, IIdea idea) {
		
	   RectangularShape rawShape = getRawShapeIdea(item, idea);
       
       RectangularShape shape = (RectangularShape)getIdeaShape(item, rawShape);
       
       if ( shape == null ) return;
       
       RectangularShape shape2 = getIdeaShapeInside(item, rawShape);
       
       // fill the shape, if requested
       int type = getRenderType(item);
       if ( type==RENDER_TYPE_FILL || type==RENDER_TYPE_DRAW_AND_FILL )
    	   MyGraphicsLib.paint(g, item, shape2, getStroke(item), RENDER_TYPE_FILL);

       
       // now render the text
       String title = m_text;
     
      
       Image  img  = getImage(item);
       
       if ( title == null && img == null )
           return;
                       
       double size = item.getSize();
       boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(),
                                       g.getTransform().getScaleY());
       double x = shape.getMinX();
       double y = shape.getMinY();
       
       final double img_ix;
       final double img_iy;
       // compute future rendering of image
       if ( img != null ) {            
           double w = size * img.getWidth(null);
           @SuppressWarnings("unused")
           double h = size * img.getHeight(null);
           double ix=x, iy=y;
           
           // determine one co-ordinate based on the image position
           ix = shape.getMaxX() - w;
           
           img_ix = ix;
           img_iy = iy;
       
       } else { // useless, but the compiler is unable to understand such a thing !
           img_ix = 0;
           img_iy = 0;
       	
       }
       
       x = shape2.getMinX() + size*m_horizBorder;
       y = shape2.getMinY() + size*m_vertBorder;
       
       int textColor = item.getTextColor();
      
	   Font fontBold = m_font.deriveFont(Font.BOLD);

	   double xEndOfTokens = 0;
	   // first display the tokens' count
       {
    	   // about tokens, the font size is adapted given the number of votes
           double fontSize = (double)m_font.getSize()+Math.min(
        		   										(double)idea.getTotalBids(),
        		   										(double)20
        		   										)/2d;
           Font fontJetons = m_font.deriveFont((float)fontSize);
           //System.out.println("size: "+fontSize+" ("+idea.getTotalBids()+")");
              
           String textJetons = idea.getTotalBids().toString(); 
           
           g.setPaint(MyGraphicsLib.getColorForItem(textColor, item));
           //g.setPaint(ColorLib.getColor(textColor));
           g.setFont(fontJetons);

           FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(fontJetons);

           // compute available width
           double tw = shape.getWidth(); // TODO moins img width !
               
           xEndOfTokens = fm.stringWidth(textJetons);
           
           // compute starting y-coordinate
           double yJetons = 	shape.getMinY() + ideas_marginTop  - ideas_baseFirstLine;
    
           // render this line of text
           drawString(
        		   g, 
        		   fm, 
        		   textJetons, 
        		   useInt, 
        		   shape.getMinX(), 
        		   yJetons, 
        		   tw
        		   );
               
       }
       
       // then the age of the idea
       {
    	  	long difference = 0;
			try {
				difference = idea.getCreationDate() - LocalCopyOfGame.getLocalCopy().getDateCreation();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	   long differenceMinutes = difference/60000;
    	   String minutes = " ("+ differenceMinutes+" min)";
           
    	   g.setPaint(MyGraphicsLib.getColorForItem(textColor, item));
           //g.setPaint(ColorLib.getColor(textColor));
           g.setFont(m_font);

           FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);

           // compute available width
           double tw = shape.getWidth(); // TODO moins img width !
               
           // compute starting y-coordinate
           double yJetons = shape.getMinY() + ideas_marginTop  - ideas_baseFirstLine;
    
           // render this line of text
           drawString(
        		   g, 
        		   fm, 
        		   minutes, 
        		   useInt, 
        		   shape.getMinX()+xEndOfTokens, 
        		   yJetons, 
        		   tw
        		   );
               
       }
       
           
       // render text
       if ( title != null && ColorLib.alpha(textColor) > 0 ) {
    	   
    	   g.setPaint(MyGraphicsLib.getColorForItem(textColor, item));
           
           //g.setPaint(ColorLib.getColor(textColor));
           g.setFont(fontBold);
           FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(fontBold);

           // compute available width
           double tw = shape2.getWidth() - 2*size*m_horizBorder;
               
           // compute available height
           double th = shape2.getHeight() - 2*size*m_vertBorder;
           
           // compute starting y-coordinate
           y += fm.getAscent();
           switch ( m_vTextAlign ) {
           case Constants.TOP:
               break;
           case Constants.BOTTOM:
               y += th - m_textDim.height;
               break;
           case Constants.CENTER:
               y += (th - m_textDim.height)/2;
           }
           
           // render each line of text
           int lh = fm.getHeight(); // the line height
           int start = 0, end = title.indexOf(m_delim);
           for ( ; end >= 0; y += lh ) {
               drawString(g, fm, title.substring(start, end), useInt, x, y, tw);
               start = end+1;
               end = title.indexOf(m_delim, start);   
           }
           drawString(g, fm, title.substring(start), useInt, x, y, tw);

           
           // and the desc 
           g.setFont(m_font);
           fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);

           // compute available width
           tw = shape2.getWidth() - 2*size*m_horizBorder;
               
           // compute available height
           th = shape2.getHeight() - 2*size*m_vertBorder;
           
           // compute starting y-coordinate
           //y += fm.getAscent();
           switch ( m_vTextAlign ) {
           case Constants.TOP:
               break;
           case Constants.BOTTOM:
               y += th - m_textDim.height;
               break;
           case Constants.CENTER:
               y += (th - m_textDim.height)/2;
           }
           
           // render each line of text
           lh = fm.getHeight(); // the line height
           start = 0;
           String desc = idea.getDesc();
           end = desc.indexOf(m_delim);
           for ( ; end >= 0; y += lh ) {
               drawString(g, fm, desc.substring(start, end), useInt, x, y, tw);
               start = end+1;
               end = desc.indexOf(m_delim, start);   
           }
           drawString(g, fm, desc.substring(start), useInt, x, y, tw);

           
       }
       

       // draw border
       if (type==RENDER_TYPE_DRAW || type==RENDER_TYPE_DRAW_AND_FILL) {
           MyGraphicsLib.paint(g,item,shape2,getStroke(item),RENDER_TYPE_DRAW);
       }
       
       // actually render the image
       if ( img != null ) {            
           
           if ( useInt && size == 1.0 ) {
               // if possible, use integer precision
               // results in faster, flicker-free image rendering
               g.drawImage(img, (int)img_ix, (int)img_iy, null);
           } else {
               m_transform.setTransform(size,0,0,size,img_ix,img_iy);
               g.drawImage(img, m_transform, null);
           }
       }
       
       // dessin rond jaune 
       // TODO ajouter condition
       LocalCopyOfGame game = LocalCopyOfGame.getLocalCopy();
       if (game.currentBids(idea.getUniqueId(), currentPlayerID) > 0) {	
    	   int circle_x = 0 + (int)shape.getMaxX() - circle_width;
    	   int circle_y = 0 + (int)shape.getMinY();

    	   // filling
    	   
    	   g.setPaint(MyGraphicsLib.getColorForItem(rgbFillCircle,item));
    	   g.fillOval(circle_x, circle_y, circle_width, circle_width);

    	   // shape
    	   g.setPaint(MyGraphicsLib.getColorForItem(rgbShapeCircle,item));
    	   g.drawOval(circle_x, circle_y, circle_width, circle_width);
    	   
       }
   
   
   }
   
   private void renderDefault(Graphics2D g, VisualItem item) {
	   
       RectangularShape shape = (RectangularShape)getShape(item);
       if ( shape == null ) return;
       
       // fill the shape, if requested
       int type = getRenderType(item);
       if ( type==RENDER_TYPE_FILL || type==RENDER_TYPE_DRAW_AND_FILL )
           MyGraphicsLib.paint(g, item, shape, getStroke(item), RENDER_TYPE_FILL);

       // now render the image and text
       String text = m_text;
       Image  img  = getImage(item);
       
       if ( text == null && img == null )
           return;
                       
       double size = item.getSize();
       boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(),
                                       g.getTransform().getScaleY());
       double x = shape.getMinX() + size*m_horizBorder;
       double y = shape.getMinY() + size*m_vertBorder;
       
       // render image
       if ( img != null ) {            
           double w = size * img.getWidth(null);
           double h = size * img.getHeight(null);
           double ix=x, iy=y;
           
           // determine one co-ordinate based on the image position
           switch ( m_imagePos ) {
           case Constants.LEFT:
               x += w + size*m_imageMargin;
               break;
           case Constants.RIGHT:
               ix = shape.getMaxX() - size*m_horizBorder - w;
               break;
           case Constants.TOP:
               y += h + size*m_imageMargin;
               break;
           case Constants.BOTTOM:
               iy = shape.getMaxY() - size*m_vertBorder - h;
               break;
           default:
               throw new IllegalStateException(
                       "Unrecognized image alignment setting.");
           }
           
           // determine the other coordinate based on image alignment
           switch ( m_imagePos ) {
           case Constants.LEFT:
           case Constants.RIGHT:
               // need to set image y-coordinate
               switch ( m_vImageAlign ) {
               case Constants.TOP:
                   break;
               case Constants.BOTTOM:
                   iy = shape.getMaxY() - size*m_vertBorder - h;
                   break;
               case Constants.CENTER:
                   iy = shape.getCenterY() - h/2;
                   break;
               }
               break;
           case Constants.TOP:
           case Constants.BOTTOM:
               // need to set image x-coordinate
               switch ( m_hImageAlign ) {
               case Constants.LEFT:
                   break;
               case Constants.RIGHT:
                   ix = shape.getMaxX() - size*m_horizBorder - w;
                   break;
               case Constants.CENTER:
                   ix = shape.getCenterX() - w/2;
                   break;
               }
               break;
           }
           
           if ( useInt && size == 1.0 ) {
               // if possible, use integer precision
               // results in faster, flicker-free image rendering
               g.drawImage(img, (int)ix, (int)iy, null);
           } else {
               m_transform.setTransform(size,0,0,size,ix,iy);
               g.drawImage(img, m_transform, null);
           }
       }
       
       // render text
       int textColor = item.getTextColor();
       if ( text != null && ColorLib.alpha(textColor) > 0 ) {
           g.setPaint(ColorLib.getColor(textColor));
           g.setFont(m_font);
           FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);

           // compute available width
           double tw;
           switch ( m_imagePos ) {
           case Constants.TOP:
           case Constants.BOTTOM:
               tw = shape.getWidth() - 2*size*m_horizBorder;
               break;
           default:
               tw = m_textDim.width;
           }
           
           // compute available height
           double th;
           switch ( m_imagePos ) {
           case Constants.LEFT:
           case Constants.RIGHT:
               th = shape.getHeight() - 2*size*m_vertBorder;
               break;
           default:
               th = m_textDim.height;
           }
           
           // compute starting y-coordinate
           y += fm.getAscent();
           switch ( m_vTextAlign ) {
           case Constants.TOP:
               break;
           case Constants.BOTTOM:
               y += th - m_textDim.height;
               break;
           case Constants.CENTER:
               y += (th - m_textDim.height)/2;
           }
           
           // render each line of text
           int lh = fm.getHeight(); // the line height
           int start = 0, end = text.indexOf(m_delim);
           for ( ; end >= 0; y += lh ) {
               drawString(g, fm, text.substring(start, end), useInt, x, y, tw);
               start = end+1;
               end = text.indexOf(m_delim, start);   
           }
           drawString(g, fm, text.substring(start), useInt, x, y, tw);
       }
   
       // draw border
       if (type==RENDER_TYPE_DRAW || type==RENDER_TYPE_DRAW_AND_FILL) {
           MyGraphicsLib.paint(g,item,shape,getStroke(item),RENDER_TYPE_DRAW);
       }
   }
   
    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
     */
    public void render(Graphics2D g, VisualItem item) {
    	
    	Integer idTypeNode = (Integer)item.get(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_TYPE);
    	
    	if (idTypeNode == null) {
    		// quick exec & return
    		renderDefault(g, item);
    		return;
    	}
    	
    	TypeNode typeNode = TypeNode.values()[idTypeNode];
    	
    	Object obj = item.get(ClientWhiteboardSWT.PREFUSE_NODE_FIELD_OBJ);
    	
    	switch (typeNode) {
    	case IDEA:
    		renderIdea(g, item, (IIdea)obj);
    		break;
    	case PLAYER:
    		renderPlayer(g, item, (IPlayer)obj);
    		break;
    	case ITEM:
    		renderItem(g, item, (IItem)obj);
    		break;
    	case COMMENT:
    		renderDefault(g, item);
    		break;
    	case POSITIVECOMMENT:
    		renderDefault(g, item);
    		break;
    	case NEGATIVECOMMENT:
    		renderDefault(g, item);
    		break;
    	default:
    			throw new RuntimeException("Type of node is not managed: "+typeNode);
    	}
    	
                
    }
    
    private final void drawString(Graphics2D g, FontMetrics fm, String text,
            boolean useInt, double x, double y, double w)
    {
        // compute the x-coordinate
        double tx;
        switch ( m_hTextAlign ) {
        case Constants.LEFT:
            tx = x;
            break;
        case Constants.RIGHT:
            tx = x + w - fm.stringWidth(text);
            break;
        case Constants.CENTER:
            tx = x + (w - fm.stringWidth(text)) / 2;
            break;
        default:
            throw new IllegalStateException(
                    "Unrecognized text alignment setting.");
        }
        // use integer precision unless zoomed-in
        // results in more stable drawing
        if ( useInt ) {
            g.drawString(text, (int)tx, (int)y);
        } else {
            g.drawString(text, (float)tx, (float)y);
        }
    }
    
    /**
     * Returns the image factory used by this renderer.
     * @return the image factory
     */
    public ImageFactory getImageFactory() {
        if ( m_images == null ) m_images = new ImageFactory();
        return m_images;
    }
    
    /**
     * Sets the image factory used by this renderer.
     * @param ifact the image factory
     */
    public void setImageFactory(ImageFactory ifact) {
        m_images = ifact;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Get the horizontal text alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     * @return the horizontal text alignment
     */
    public int getHorizontalTextAlignment() {
        return m_hTextAlign;
    }
    
    /**
     * Set the horizontal text alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     * @param halign the desired horizontal text alignment
     */
    public void setHorizontalTextAlignment(int halign) {
        if ( halign != Constants.LEFT &&
             halign != Constants.RIGHT &&
             halign != Constants.CENTER )
           throw new IllegalArgumentException(
                   "Illegal horizontal text alignment value.");
        m_hTextAlign = halign;
    }
    
    /**
     * Get the vertical text alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     * @return the vertical text alignment
     */
    public int getVerticalTextAlignment() {
        return m_vTextAlign;
    }
    
    /**
     * Set the vertical text alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     * @param valign the desired vertical text alignment
     */
    public void setVerticalTextAlignment(int valign) {
        if ( valign != Constants.TOP &&
             valign != Constants.BOTTOM &&
             valign != Constants.CENTER )
            throw new IllegalArgumentException(
                    "Illegal vertical text alignment value.");
        m_vTextAlign = valign;
    }
    
    
    
    // ------------------------------------------------------------------------
    
    /**
     * Get the horizontal alignment of this node with respect to its
     * x, y coordinates.
     * @return the horizontal alignment, one of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment() {
        return m_xAlign;
    }
    
    /**
     * Get the vertical alignment of this node with respect to its
     * x, y coordinates.
     * @return the vertical alignment, one of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment() {
        return m_yAlign;
    }
    
    /**
     * Set the horizontal alignment of this node with respect to its
     * x, y coordinates.
     * @param align the horizontal alignment, one of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}.
     */ 
    public void setHorizontalAlignment(int align) {
        m_xAlign = align;
    }
    
    /**
     * Set the vertical alignment of this node with respect to its
     * x, y coordinates.
     * @param align the vertical alignment, one of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}.
     */ 
    public void setVerticalAlignment(int align) {
        m_yAlign = align;
    }
    
    /**
     * Returns the amount of padding in pixels between the content 
     * and the border of this item along the horizontal dimension.
     * @return the horizontal padding
     */
    public int getHorizontalPadding() {
        return m_horizBorder;
    }
    
    /**
     * Sets the amount of padding in pixels between the content 
     * and the border of this item along the horizontal dimension.
     * @param xpad the horizontal padding to set
     */
    public void setHorizontalPadding(int xpad) {
        m_horizBorder = xpad;
    }
    
    /**
     * Returns the amount of padding in pixels between the content 
     * and the border of this item along the vertical dimension.
     * @return the vertical padding
     */
    public int getVerticalPadding() {
        return m_vertBorder;
    }
    
    /**
     * Sets the amount of padding in pixels between the content 
     * and the border of this item along the vertical dimension.
     * @param ypad the vertical padding
     */
    public void setVerticalPadding(int ypad) {
        m_vertBorder = ypad;
    }
    
    /**
     * Get the padding, in pixels, between an image and text.
     * @return the padding between an image and text
     */
    public int getImageTextPadding() {
        return m_imageMargin;
    }
    
    /**
     * Set the padding, in pixels, between an image and text.
     * @param pad the padding to use between an image and text
     */
    public void setImageTextPadding(int pad) {
        m_imageMargin = pad;
    }
    
    
    
    
    
    
    

	public String getFieldForObject() {
		return fieldForObject;
	}

	public void setFieldForObject(String fieldForObject) {
		this.fieldForObject = fieldForObject;
	}
	
	
} // end of class LabelRenderer
