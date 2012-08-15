package cookie.cadger.mattslifebytes.com;

public class EnhancedJListItem
{
	private int recordNumber;
	private String name;
	private String description;
	
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
	
	public int getID()
	{
		return recordNumber;
	}
}