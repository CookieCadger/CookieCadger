package com.mattslifebytes.cookiecadger;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.LinkedList;

/**
 * A structure containing a record number, name, description, and thumbnail image.
 */
public class ListItem implements Comparable<ListItem> {
    private int recordNumber;
    private String name;
    private String description;
    private Image thumbnailImage;
    private LinkedList<CookieCadgerException> debugList;

    /**
     * Default constructor for this item.
     * @param recordNumber the record number of this item
     * @param name the name of this item
     * @param description the description of this item
     */
    public ListItem(int recordNumber, String name, String description) {
    	this.recordNumber = recordNumber;
    	this.name = name;
    	this.description = description;
    	this.debugList = new LinkedList<CookieCadgerException>();
    }
    
    /**
     * Returns the record number of this item.
     * @return recordNumber the record number of this item.
     */
    public int getRecordNumber() {
    	return recordNumber;
    }

    /**
     * Returns a string representation of this item.
     * @return name a string representation of this item.
     */
    public String toString() {
    	return name;
    }
    
    /**
     * Sets a description of the item.
     * @param description a description of the item.
     */
    public void setDescription(String description) {
    	if (description == null) {
    		this.description = new String();
    		this.debugList.add(new CookieCadgerException("EnhancedJListItem: in setDescription(String): the argument String was null so set a blank String as the description."));
    	} else {
    		this.description = description;
    	}
    }

    /**
     * Returns a description of the item.
     * @return description a description of the item.
     */
    public String getDescription() {
    	return description;
    }

    /**
     * Checks to see if there is a thumbnail associated with this item.
     * @return returns true if there is a thumbnail associated with this item, false otherwise.
     */
    public boolean hasThumbnail() {
    	if (thumbnailImage == null)
    		return false;
    	return true;
    }
    
    /**
     * Sets a thumbnail picture for this item.
     * @param url the URL for the profile picture to be associated with this item.
     */
    public void setThumbnailImage(URL url) {
    	try {
    		thumbnailImage = Toolkit.getDefaultToolkit().createImage(url);
    	} catch (Exception e) {
    		debugList.add(new CookieCadgerException("EnhancedJListItem: in setThumbNailImage(URL url): " + e.getClass() + ": " + e.getLocalizedMessage()));
    	}
    }
    
    /**
     * Gets the thumbnail associated with this item.
     * @return thumbnailImage the thumbnailImage associated with this item.
     */
    public Image getThumbnail() {
    	return thumbnailImage;
    }

    /**
     * Compares a ListItem to this one lexicographically based on 'name'.
     * @param list the list to be compared to.
     * @return -1 if this list comes before the argument list, 0 if they are sortably equivalent, and 1 if this list comes after the argument list.
     */
	@Override
	public int compareTo(ListItem list) {
		if (name == null) {
    		this.debugList.add(new CookieCadgerException("ListItem: in compareTo(ListItem): the instance variable 'name' is null ('name' is used for the comparison)."));
		}
		if (list == null) {
    		this.debugList.add(new CookieCadgerException("ListItem: in compareTo(ListItem): the ListItem to compare to is null."));
		}
		if (list.name == null) {
    		this.debugList.add(new CookieCadgerException("ListItem: in compareTo(ListItem): the argument ListItem's instance variable 'name' is null ('name' is used for the comparison)."));
		}
		return this.name.compareTo(list.name);
	}
}