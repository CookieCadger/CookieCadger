package cookie.cadger.mattslifebytes.com;

import javax.swing.DefaultListModel;

public class EnhancedListModel extends DefaultListModel
{
	public boolean contains(String elem)
	{		
		for(int i = 0; i < getSize(); i++)
		{
			if(((EnhancedJListItem)getElementAt(i)).toString().equals(elem))
				return true;
		}
		
		return false;
	}
	
	public boolean contains(int elem)
	{		
		for(int i = 0; i < getSize(); i++)
		{
			if(((EnhancedJListItem)getElementAt(i)).getID() == elem)
				return true;
		}
		
		return false;
	}
	
	public int indexOf(String elem)
	{		
		for(int i = 0; i < getSize(); i++)
		{
			if(((EnhancedJListItem)getElementAt(i)).toString().equals((String)elem))
				return i;
		}
		
		return -1;
	}
	
	public int indexOf(int elem)
	{	
		for(int i = 0; i < getSize(); i++)
		{
			if(((EnhancedJListItem)getElementAt(i)).getID() == elem)
				return i;
		}
		
		return -1;
	}
}
