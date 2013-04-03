package com.cookiecadger;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Color;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

import java.util.ArrayList;

/**
 * A JList that supports a zebra stripe background.
 */
public class EnhancedJList extends JList
{
    private Color rowColors[] = new Color[2];
    private boolean drawStripes = false;
    private ArrayList<String> highlightStrings = new ArrayList<String>();
    private ArrayList<Color> highlightColors = new ArrayList<Color>();
    
    public EnhancedJList( )
    {
    	ToolTipManager.sharedInstance().setInitialDelay(0);
    	ToolTipManager.sharedInstance().setDismissDelay(60000); // Don't allow to disappear for a minute
    	
    	// Attach a mouse motion adapter to let us know the mouse is over an item and to show the tip.
    	addMouseMotionListener( new MouseMotionAdapter()
    	{
    		public void mouseMoved( MouseEvent e)
    		{
    			EnhancedJList theList = (EnhancedJList) e.getSource();
    			ListModel model = theList.getModel();
    			
    			Point mousePosition = e.getPoint();
    			int index = theList.locationToIndex(mousePosition);
    			
    			// Is there an item close by?
    			if (index > -1)
    			{
        			Rectangle cellRect = theList.getCellBounds(index, index);
        			
        			// If point inside rectangle
        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
        			{
        				theList.setToolTipText(((EnhancedJListItem)model.getElementAt(index)).getDescription());
        				return;
        			}
    			}
    			
    			// If we got to this point it's because no match was found, disable
    			theList.setToolTipText(null);
    		}
    	});
    }

    public EnhancedJList( ListModel dataModel )
    {
        super( dataModel );
    }
    public EnhancedJList( Object[] listData )
    {
        super( listData );
    }
    public EnhancedJList( java.util.Vector<?> listData )
    {
        super( listData );
    }

    // Expose the getToolTipText event of our JList
    public String getToolTipText(MouseEvent e)
    {
    	return super.getToolTipText();
    }
    
    public void performHighlight(final String textToMatchAgainst, Color color)
    {
        if(highlightStrings.contains(textToMatchAgainst))
        {
        	// Ignore repeat calls for the same item
        	return;
        }
        
    	highlightStrings.add(textToMatchAgainst);
    	highlightColors.add(color);
    	    	
    	SwingWorker worker = new SwingWorker() {            
        	@Override            
            public Object doInBackground() {
                try {
                    Thread.sleep(10000); // 10 seconds of highlighting
                } catch (InterruptedException e) { /*Who cares*/ }
                return null;
            }
            @Override
            public void done()
            {
            	highlightColors.remove(highlightStrings.indexOf(textToMatchAgainst));
                highlightStrings.remove(highlightStrings.indexOf(textToMatchAgainst));
                repaint();
            }
        };
        worker.execute();
    	
    	this.repaint();
    }
    
    /** Add zebra stripes to the background. */
    public void paintComponent( Graphics g )
    {
        try
	    {
	        drawStripes = (getLayoutOrientation( )==VERTICAL) && isOpaque( );
	        //drawStripes = true;
	        if ( !drawStripes )
	        {
	            super.paintComponent( g );
	            return;
	        }

	        // Paint zebra background stripes
	        updateZebraColors( );
	        final Insets insets = getInsets( );
	        final int w   = getWidth( )  - insets.left - insets.right;
	        final int h   = getHeight( ) - insets.top  - insets.bottom;
	        final int x   = insets.left;
	        int y         = insets.top;
	        int nRows     = 0;
	        int startRow  = 0;
	        int rowHeight = getFixedCellHeight( );
	        if ( rowHeight > 0 )
	            nRows = h / rowHeight;
	        else
	        {
	            // Paint non-uniform height rows first
	            final int nItems = getModel( ).getSize( );
	            rowHeight = 17; // A default for empty lists
	            for ( int i = 0; i < nItems; i++, y+=rowHeight )
	            {
	                rowHeight = getCellBounds( i, i ).height;
	                g.setColor( rowColors[i&1] );
	                g.fillRect( x, y, w, rowHeight );
	            }
	            
	            if(rowHeight > 0)
	            {
	            	// Use last row height for remainder of list area
	            	nRows    = nItems + (insets.top + h - y) / rowHeight;
	            	startRow = nItems;
	            }
	        }
	        for ( int i = startRow; i < nRows; i++, y+=rowHeight )
	        {
	            g.setColor( rowColors[i&1] );
	            g.fillRect( x, y, w, rowHeight );
	        }
	        final int remainder = insets.top + h - y;
	        if ( remainder > 0 )
	        {
	            g.setColor( rowColors[nRows&1] );
	            g.fillRect( x, y, w, remainder );
	        }
	    }
        catch (Exception e)
        {
        	e.printStackTrace();
        }

        // Paint component
        setOpaque( false );
        super.paintComponent( g );
        setOpaque( true );
    }
 
    /** Wrap a cell renderer to add zebra stripes behind list cells. */
    private class RendererWrapper implements ListCellRenderer
    {
        public ListCellRenderer ren = null;
 
        public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus )
        {
            final Component c = ren.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus );
            
            if (isSelected)
            {
            	// Set our own selection color to keep this from
            	// looking like crap on OS X
            	c.setBackground(Color.LIGHT_GRAY);
            }
            else if ( drawStripes )
            {
                c.setBackground( rowColors[index&1] );
            }
            
            // Yes, we must highlight this
            if(value != null)
            {
            	try
            	{
		            String strVal = ((EnhancedJListItem)value).toString();
		
		            if(highlightStrings.contains(strVal))
		            {
		            	int colorIndex = highlightStrings.indexOf(strVal);
		            	c.setForeground(highlightColors.get(colorIndex));
		        	}
		            else
		            {
		            	c.setForeground(Color.BLACK);
		            }
		            
		            if(((EnhancedJListItem)value).hasThumbnail())
		            {
		            	JLabel itemLabel = (JLabel)c;
		            	ImageIcon icon = new ImageIcon(((EnhancedJListItem)value).getThumbnail());
		            	itemLabel.setIcon(icon);
		            }
            	}
            	catch (Exception ex)
            	{
            		ex.printStackTrace();
            	}
            }
            
            return c;
        }
    }
    private RendererWrapper wrapper = null;
 
    /** Return the wrapped cell renderer. */
    public ListCellRenderer getCellRenderer( )
    {
        final ListCellRenderer ren = super.getCellRenderer( );
        if ( ren == null )
            return null;
        if ( wrapper == null )
            wrapper = new RendererWrapper( );
        wrapper.ren = ren;
        return wrapper;
    }
 
    /** Compute zebra background stripe colors. */
    private void updateZebraColors( )
    {
        if ( (rowColors[0] = getBackground( )) == null )
        {
            rowColors[0] = rowColors[1] = Color.white;
            return;
        }
        final Color sel = getSelectionBackground( );
        if ( sel == null )
        {
            rowColors[1] = rowColors[0];
            return;
        }
        final float[] bgHSB = Color.RGBtoHSB(
            rowColors[0].getRed( ), rowColors[0].getGreen( ),
            rowColors[0].getBlue( ), null );
        final float[] selHSB  = Color.RGBtoHSB(
            sel.getRed( ), sel.getGreen( ), sel.getBlue( ), null );
        rowColors[1] = Color.getHSBColor(
            (selHSB[1]==0.0||selHSB[2]==0.0) ? bgHSB[0] : selHSB[0],
            0.1f * selHSB[1] + 0.9f * bgHSB[1],
            bgHSB[2] + ((bgHSB[2]<0.5f) ? 0.05f : -0.05f) );
    }
}