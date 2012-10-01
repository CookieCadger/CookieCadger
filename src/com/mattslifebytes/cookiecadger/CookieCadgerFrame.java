package com.mattslifebytes.cookiecadger;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class CookieCadgerFrame extends JFrame {
	
	private HashMap<String, Component> componentMap;
	private JPanel contentPanel;
	
	
	public CookieCadgerFrame() {
		initSettings();
	}
	
	private void initSettings() {
		this.setResizable(false);
		createComponentMap();	    
	}
	
	/**
	 * Creates a component map which matches the component's name as a key against their reference as a value. 
	 */
	private void createComponentMap() {
		HashMap<String, Component> componentMap = new HashMap<String,Component>();
        Component[] components = contentPanel.getComponents();
        for (int i=0; i < components.length; i++) {
        	if(components[i] instanceof JTabbedPane)
        		// Find the tabbed panel and iterate its components as well
        		for (Component cmp : ((JTabbedPane) components[i]).getComponents())
        			if(cmp instanceof JPanel)
        				for (Component x : ((JPanel) cmp).getComponents())
        		        	componentMap.put(x.getName(), x);
       		componentMap.put(components[i].getName(), components[i]);
        }
	}
	
	/**
	 * Returns a swing component by its name.
	 * @param name the name of the swing component
	 * @return the swing component named 'name'
	 */
	private Component getComponentByName(String name) {
        if (componentMap.containsKey(name)) {
                return (Component) componentMap.get(name);
        }
        else
        	return null;
	}
	
}
