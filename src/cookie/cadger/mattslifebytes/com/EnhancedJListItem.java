package cookie.cadger.mattslifebytes.com;

import java.awt.Image;

public class EnhancedJListItem
{
	private int recordNumber;
	private String name;
	private String description;
	private Image thumbnailImage;
	
	public EnhancedJListItem(int rn, String nm, String desc)  
	{
		recordNumber = rn;
		description = desc;
		name = nm;
	}
	
	public String toString()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String desc)
	{
		description = desc;
	}
	
	public void setThumbnail(Image img)
	{
		thumbnailImage = img;
	}
	
	public boolean hasThumbnail()
	{
		if(thumbnailImage == null)
			return false;
					
		return true;
	}
	
	public Image getThumbnail()
	{
		return thumbnailImage;
	}
	
	public int getID()
	{
		return recordNumber;
	}
}