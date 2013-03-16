package com.cookiecadger;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.FocusManager;
import javax.swing.JTextField;

public class EnhancedJTextField extends JTextField
{
	private String placeholderText = null;
	
	@Override
	protected void paintComponent(java.awt.Graphics g)
	{
		super.paintComponent(g);
		
		if(getText().isEmpty() && getPlaceholder() != null)
		{
			Graphics2D g2 = (Graphics2D)g.create();
			setForeground(Color.GRAY);
			
			Font font1 = new Font("SansSerif", Font.BOLD, 10);
			g2.setFont(font1);
			
			g2.drawString(getPlaceholder(), 5, 10);
			g2.dispose();
		}
		else
		{
			setForeground(Color.BLACK);
		}
	}
	
	public void setPlaceholder(String placeholderText)
	{
		this.placeholderText = placeholderText; 
	}
	
	public String getPlaceholder()
	{
		return this.placeholderText;
	}
}