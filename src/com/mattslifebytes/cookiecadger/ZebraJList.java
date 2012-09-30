package com.mattslifebytes.cookiecadger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

/**
 * A JList that supports zebra-style highlighting and tool-tip hovering.
 */
@SuppressWarnings("serial")
public class ZebraJList extends JList<SortedListModel> {
	
    private Color rowColors[] = new Color[2];
    private boolean drawStripes = false;
    private TreeSet<String> highlightedStrings = new TreeSet<String>();
    private RendererWrapper wrapper = null;
    private LinkedList<CookieCadgerException> debugList;
    
	/**
	 * Default constructor which attaches a mouse motion adapter to support the tool-tip hovering.
	 */
	public ZebraJList()
    {
    	ToolTipManager.sharedInstance().setInitialDelay(0);
    	// Attach a mouse motion adapter to let us know the mouse is over an item and to show the tip.
    	addMouseMotionListener( new MouseMotionAdapter()
    	{
    		public void mouseMoved(MouseEvent e)
    		{
    			ZebraJList list = (ZebraJList) e.getSource();
    			ListModel<SortedListModel> model = list.getModel();
    			Point mousePosition = e.getPoint();
    			int index = list.locationToIndex(mousePosition);
    			// Is there an item close by?
    			if (index > -1)
    			{
        			Rectangle cellRect = list.getCellBounds(index, index);
        			// If point inside rectangle
        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
        			{
        				Object item = model.getElementAt(index);
        				ListItem listItem = (ListItem) item;
        				list.setToolTipText(listItem.getDescription());
        				return;
        			}
    			}	
    			// If we got to this point it's because no match was found, disable
    			list.setToolTipText(null);
    		}
    	});
    }
	
	/**
	 * Returns the tooltip text to be used for the given event. This overrides JComponent's getToolTipText to first check the cell renderer component for the cell over which the event occurred, returning its tooltip text, if any. This implementation allows you to specify tooltip text on the cell level, by using setToolTipText on your cell renderer component. Note: For ZebraJList to properly display the tooltips of its renderers in this manner, ZebraJList must be a registered component with the ToolTipManager. This registration is done automatically in the constructor. However, if at a later point ZebraJList is unregistered, by way of a call to setToolTipText(null), tips from the renderers will no longer display.
	 * @param e the MouseEvent to fetch the tooltip text for
	 */
	public String getToolTipText(MouseEvent e)
    {
    	return super.getToolTipText();
    }
	
	/**
	 * Highlights a matching String with the given color.
	 * @param textToMatch the text to match
	 * @param color
	 */
	public void performHighlight(final String textToMatch)
    {
    	// Ignore repeat calls for the same item
        if(highlightedStrings.contains(textToMatch))
        	return;
    	highlightedStrings.add(textToMatch);
    	// Wait 10 seconds and then turn off the highlighting
    	SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {            
        	@Override            
            public Object doInBackground() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
            		debugList.add(new CookieCadgerException("ZebraJList: in performHighlight(final String): we weren't able to sleep the thread for 10 seconds: " + e.getLocalizedMessage()));
                }
                return null;
            }
            @Override
            public void done()
            {
                highlightedStrings.remove(textToMatch);
                repaint();
            }
        };
        worker.execute();
    	this.repaint();
    }
	
	/**
	 * Add zebra stripes to the background.
	 */
    public void paintComponent(Graphics g)
    {
    	if (g == null) {
    		debugList.add(new CookieCadgerException("ZebraJList: in paintComponent(Graphics): the passed Graphics argument was null."));
    	}
    	drawStripes = (getLayoutOrientation() == VERTICAL) && isOpaque();
    	if (!drawStripes)
    	{
    		super.paintComponent(g);
    		return;
    	}
    	// Paint zebra background stripes
    	updateZebraColors();
    	Insets insets = getInsets();
    	int w = getWidth() - insets.left - insets.right;
    	int h = getHeight() - insets.top - insets.bottom;
    	int x = insets.left;
	    int y = insets.top;
	    int nRows = 0;
	    int startRow = 0;
	    int rowHeight = getFixedCellHeight();
	    if (rowHeight > 0)
	    	nRows = h / rowHeight;
	    else
	    {
	    	// Paint non-uniform height rows first
	    	int nItems = getModel().getSize();
	    	rowHeight = 17; // A default for empty lists
	    	for (int i = 0; i < nItems; i++, y += rowHeight)
	    	{
	    		rowHeight = getCellBounds(i, i).height;
	    		g.setColor(rowColors[i&1]);
	    		g.fillRect(x, y, w, rowHeight);
	    	}
	    	// Use last row height for remainder of list area
	    	nRows = nItems + (insets.top + h - y) / rowHeight;
	    	startRow = nItems;
	    }
	    for (int i = startRow; i < nRows; i++, y+=rowHeight)
	    {
	    	g.setColor(rowColors[i&1]);
	    	g.fillRect(x, y, w, rowHeight);
	    }
	    int remainder = insets.top + h - y;
	    if (remainder > 0)
	    {
	    	g.setColor(rowColors[nRows&1]);
	    	g.fillRect(x, y, w, remainder);
	    }
	    // Paint component
	    setOpaque(false);
	    super.paintComponent(g);
	    setOpaque(true);	    
    }
    	
    /**
     * Wrapper to wrap a cell renderer to add zebra stripes behind list cells.
     */
    private class RendererWrapper implements ListCellRenderer<ListItem>
    {
        public ListCellRenderer<ListItem> renderer = null;

		@Override
		public Component getListCellRendererComponent(JList<? extends ListItem> list, ListItem item, int index, boolean isSelected, boolean cellHasFocus) {
			final Component c = renderer.getListCellRendererComponent(list, item, index, isSelected, cellHasFocus);
        	// Set our own selection color to support Mac OS X
			if (isSelected) {
            	c.setBackground(Color.LIGHT_GRAY);
            } else if (drawStripes) {
                c.setBackground(rowColors[index&1]);
            }
            String stringToHighlight = item.toString();
            if(highlightedStrings.contains(stringToHighlight)) {
            	c.setForeground(Color.BLUE);
        	} else {
            	c.setForeground(Color.BLACK);
            }
            if(item.hasThumbnail())
            {
            	javax.swing.JLabel itemLabel = (JLabel) c;
            	ImageIcon icon = new ImageIcon(item.getThumbnail());
            	itemLabel.setIcon(icon);
            }
			return c;
		}
    }
    
    /**
     * Returns the wrapped cell renderer.
     * @return the wrapped cell renderer.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public ListCellRenderer getCellRenderer()
    {
        ListCellRenderer<ListItem> renderer = getCellRenderer();
        if (renderer == null)
            return null;
        if (wrapper == null)
            wrapper = new RendererWrapper();
        wrapper.renderer = renderer;
        return wrapper;
    }
    
	/**
	 * Computes the background stripe colors.
	 */
    private void updateZebraColors()
    {
        if ((rowColors[0] = getBackground()) == null)
        {
            rowColors[0] = rowColors[1] = Color.white;
            return;
        }
        Color selector = getSelectionBackground();
        if (selector == null)
        {
            rowColors[1] = rowColors[0];
            return;
        }
        float[] bgHSB = Color.RGBtoHSB(
            rowColors[0].getRed(), rowColors[0].getGreen(),
            rowColors[0].getBlue(), null);
        float[] selHSB  = Color.RGBtoHSB(
        		selector.getRed(), selector.getGreen(),
        		selector.getBlue(), null);
        rowColors[1] = Color.getHSBColor(
            (selHSB[1]==0.0||selHSB[2]==0.0) ? bgHSB[0] : selHSB[0],
            0.1f * selHSB[1] + 0.9f * bgHSB[1],
            bgHSB[2] + ((bgHSB[2]<0.5f) ? 0.05f : -0.05f) );
    }
	
}
