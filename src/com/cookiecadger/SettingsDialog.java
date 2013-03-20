package com.cookiecadger;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;

import com.cookiecadger.Utils;

public class SettingsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JPanel generalPanel, databasePanel;
	private JTabbedPane tabbedPane;
	private EnhancedJTextField txtTsharkPath;
	private JTextField txtDatabaseHost, txtDatabaseUser, txtDatabaseName, txtDatabaseRefreshRate;
	private JComboBox comboSessionDetection, comboDatabaseEngine;
	private JCheckBox chckbxAllowUpdates, chckbxEnableDemoMode;
	private JPasswordField txtDatabasePass;
	private JLabel lblDatabaseHost, lblDatabaseUser, lblDatabasePass, lblDatabaseName, lblDatabaseRefreshRate;

	public SettingsDialog()
	{
		setTitle("Program Settings");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 450, 300);
		setModal(true);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(1, 0, 0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPanel.add(tabbedPane);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("Save");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				String dbEngine = ((Utils.databaseEngineChoices)comboDatabaseEngine.getSelectedItem()).name().toLowerCase();
				Utils.savePreference("dbEngine", dbEngine);
				Utils.savePreference("databaseHost", txtDatabaseHost.getText());
				Utils.savePreference("databaseUser", txtDatabaseUser.getText());
				Utils.savePreference("databasePass", new String(txtDatabasePass.getPassword()));
				Utils.savePreference("databaseName", txtDatabaseName.getText());
				Utils.savePreference("databaseRefreshRate", txtDatabaseRefreshRate.getText());
				
				Utils.savePreference("tsharkPath", txtTsharkPath.getText());
				
				String bSessionDetection = ((Utils.sessionDetectionChoices)comboSessionDetection.getSelectedItem()).name().toLowerCase();
				if(bSessionDetection.equals("prompt"))
					Utils.savePreference("bSessionDetection", -1);
				else if(bSessionDetection.equals("yes"))
					Utils.savePreference("bSessionDetection", 1);
				else
					Utils.savePreference("bSessionDetection", 0);
				
				Utils.savePreference("bUseDemoMode", chckbxEnableDemoMode.isSelected());
				Utils.savePreference("bCheckForUpdates", chckbxAllowUpdates.isSelected());

				JOptionPane.showMessageDialog(null, "You must restart Cookie Cadger for changes to take effect.");
				
				SettingsDialog.this.dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SettingsDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		generalPanel = new JPanel();
		generalPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		generalPanel.setLayout(null);
		tabbedPane.addTab("General Settings", null, generalPanel, null);
		
		JLabel lblPathTothsark = new JLabel("Path to 'tshark' binary:");
		lblPathTothsark.setBounds(12, 6, 180, 15);
		generalPanel.add(lblPathTothsark);
		
		txtTsharkPath = new EnhancedJTextField();
		txtTsharkPath.setBounds(12, 24, 400, 19);
		generalPanel.add(txtTsharkPath);
		txtTsharkPath.setColumns(10);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(0, 100, 434, 2);
		generalPanel.add(separator);
		
		JLabel lblSessionDetection = new JLabel("Session detection:");
		lblSessionDetection.setBounds(12, 60, 140, 15);
		generalPanel.add(lblSessionDetection);
		
		comboSessionDetection = new JComboBox();
		comboSessionDetection.setModel(new DefaultComboBoxModel(Utils.sessionDetectionChoices.values()));
		comboSessionDetection.setBounds(159, 56, 250, 24);
		generalPanel.add(comboSessionDetection);
		
		chckbxAllowUpdates = new JCheckBox("Allow automatic checking for software updates?");
		chckbxAllowUpdates.setBounds(12, 120, 400, 23);
		generalPanel.add(chckbxAllowUpdates);
		
		chckbxEnableDemoMode = new JCheckBox("Enable 'demo mode' (requires session detection)");
		chckbxEnableDemoMode.setBounds(12, 150, 400, 23);
		generalPanel.add(chckbxEnableDemoMode);
		
		databasePanel = new JPanel();
		databasePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		databasePanel.setLayout(null);
		tabbedPane.addTab("Database Settings", null, databasePanel, null);
		
		JLabel lblDatabase = new JLabel("Database:");
		lblDatabase.setBounds(12, 12, 80, 15);
		databasePanel.add(lblDatabase);
		
		comboDatabaseEngine = new JComboBox();
		comboDatabaseEngine.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JComboBox source = ((JComboBox)e.getSource());
				boolean bUsingExternal = !source.getSelectedItem().equals(Utils.databaseEngineChoices.SQLITE);
				
				changeDatabaseFields(bUsingExternal);
			}
		});
		comboDatabaseEngine.setModel(new DefaultComboBoxModel(Utils.databaseEngineChoices.values()));
		comboDatabaseEngine.setBounds(95, 8, 316, 24);
		databasePanel.add(comboDatabaseEngine);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(0, 48, 434, 2);
		databasePanel.add(separator_1);
		
		lblDatabaseHost = new JLabel("Database Host:");
		lblDatabaseHost.setBounds(12, 62, 120, 15);
		lblDatabaseHost.setEnabled(false);
		databasePanel.add(lblDatabaseHost);
		
		lblDatabaseUser = new JLabel("Database User:");
		lblDatabaseUser.setBounds(12, 86, 120, 15);
		lblDatabaseUser.setEnabled(false);
		databasePanel.add(lblDatabaseUser);
		
		lblDatabasePass = new JLabel("Database Pass:");
		lblDatabasePass.setBounds(12, 110, 120, 15);
		lblDatabasePass.setEnabled(false);
		databasePanel.add(lblDatabasePass);
		
		lblDatabaseName = new JLabel("Database Name:");
		lblDatabaseName.setBounds(12, 134, 120, 15);
		lblDatabaseName.setEnabled(false);
		databasePanel.add(lblDatabaseName);
		
		lblDatabaseRefreshRate = new JLabel("Database Refresh Rate (in seconds):");
		lblDatabaseRefreshRate.setEnabled(false);
		lblDatabaseRefreshRate.setBounds(12, 158, 264, 15);
		databasePanel.add(lblDatabaseRefreshRate);
		
		txtDatabaseHost = new JTextField();
		txtDatabaseHost.setBounds(146, 60, 265, 19);
		txtDatabaseHost.setEnabled(false);
		databasePanel.add(txtDatabaseHost);
		txtDatabaseHost.setColumns(10);
		
		txtDatabaseUser = new JTextField();
		txtDatabaseUser.setColumns(10);
		txtDatabaseUser.setBounds(146, 84, 265, 19);
		txtDatabaseUser.setEnabled(false);
		databasePanel.add(txtDatabaseUser);
		
		txtDatabasePass = new JPasswordField();
		txtDatabasePass.setColumns(10);
		txtDatabasePass.setBounds(146, 108, 265, 19);
		txtDatabasePass.setEnabled(false);
		databasePanel.add(txtDatabasePass);
		
		txtDatabaseName = new JTextField();
		txtDatabaseName.setColumns(10);
		txtDatabaseName.setBounds(146, 132, 265, 19);
		txtDatabaseName.setEnabled(false);
		databasePanel.add(txtDatabaseName);
		
		txtDatabaseRefreshRate = new JTextField();
		txtDatabaseRefreshRate.setEnabled(false);
		txtDatabaseRefreshRate.setColumns(10);
		txtDatabaseRefreshRate.setBounds(290, 156, 64, 19);
		databasePanel.add(txtDatabaseRefreshRate);
		
		// Initialize settings
		// =================================
		
		// Look up database engine preference and change combo
		String dbEngine = Utils.getPreference("dbEngine", "sqlite");
		for (Utils.databaseEngineChoices enum_option : Utils.databaseEngineChoices.values())
		{
			if(enum_option.name().toLowerCase().equals(dbEngine.toLowerCase()))
			{
				comboDatabaseEngine.setSelectedItem(enum_option);
				if(enum_option.equals(Utils.databaseEngineChoices.SQLITE))
				{
					changeDatabaseFields(false);
				}
				break;
			}
		}
		
		// Set up all other DB fields
		txtDatabaseHost.setText(Utils.getPreference("databaseHost", ""));
		txtDatabaseUser.setText(Utils.getPreference("databaseUser", ""));
		txtDatabasePass.setText(Utils.getPreference("databasePass", ""));
		txtDatabaseName.setText(Utils.getPreference("databaseName", ""));
		txtDatabaseRefreshRate.setText((Utils.getPreference("databaseRefreshRate", 15)).toString());

		
		// Look up session detection and change combo
		int bSessionDetection = Utils.getPreference("bSessionDetection", -1);
		
		if(bSessionDetection == -1)
			comboSessionDetection.setSelectedItem(Utils.sessionDetectionChoices.PROMPT);
		else if(bSessionDetection == 1)
			comboSessionDetection.setSelectedItem(Utils.sessionDetectionChoices.YES);
		else
			comboSessionDetection.setSelectedItem(Utils.sessionDetectionChoices.NO);
		
		// Set up display of tshark path
		String tsharkPathSaved = Utils.getPreference("tsharkPath", "");
		txtTsharkPath.setText(tsharkPathSaved);
		
		if(tsharkPathSaved.length() == 0)
			txtTsharkPath.setPlaceholder((String) Utils.programSettings.get("tsharkPath"), new Font("SansSerif", Font.BOLD, 12));
		
		// Automatic update checking?
		chckbxAllowUpdates.setSelected((Boolean) Utils.getPreference("bCheckForUpdates", true));
		
		// Demo mode?
		chckbxEnableDemoMode.setSelected((Boolean) Utils.getPreference("bUseDemoMode", false));
	}
	
	private void changeDatabaseFields(boolean bUsingExternal)
	{
		txtDatabaseHost.setEnabled(bUsingExternal);
		txtDatabaseUser.setEnabled(bUsingExternal);
		txtDatabasePass.setEnabled(bUsingExternal);
		txtDatabaseName.setEnabled(bUsingExternal);
		txtDatabaseRefreshRate.setEnabled(bUsingExternal);
		
		lblDatabaseHost.setEnabled(bUsingExternal);
		lblDatabaseUser.setEnabled(bUsingExternal);
		lblDatabasePass.setEnabled(bUsingExternal);
		lblDatabaseName.setEnabled(bUsingExternal);
		lblDatabaseRefreshRate.setEnabled(bUsingExternal);
	}
}
