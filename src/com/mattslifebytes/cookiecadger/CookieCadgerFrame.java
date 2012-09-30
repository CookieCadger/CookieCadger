package com.mattslifebytes.cookiecadger;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class CookieCadgerFrame extends JFrame {
	
	public CookieCadgerFrame() {
		initSettings();
	}
	
	private void initSettings() {
		this.setResizable(false);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				PrepareToCloseApplication();
			}
		});	    
	}
	
}
