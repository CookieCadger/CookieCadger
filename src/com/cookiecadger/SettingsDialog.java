package com.cookiecadger;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Window.Type;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;

import com.cookiecadger.CookieCadgerUtils;

public class SettingsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JPanel generalPanel, databasePanel;
	private JTabbedPane tabbedPane;
	private JTextField txtTsharkPath;
	private JTextField txtDatabaseHost, txtDatabaseUser, txtDatabasePass, txtDatabaseName;
	private JLabel lblDatabaseHost, lblDatabaseUser, lblDatabasePass, lblDatabaseName;
	

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
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Save");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						SettingsDialog.this.dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SettingsDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		generalPanel = new JPanel();
		generalPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		generalPanel.setLayout(null);
		tabbedPane.addTab("General Settings", null, generalPanel, null);
		
		JLabel lblDefaultBrowser = new JLabel("Default browser:");
		lblDefaultBrowser.setBounds(12, 60, 140, 15);
		generalPanel.add(lblDefaultBrowser);
		
		JComboBox comboBrowser = new JComboBox();
		comboBrowser.setModel(new DefaultComboBoxModel(CookieCadgerUtils.browserChoices.values()));
		comboBrowser.setBounds(159, 56, 250, 24);
		generalPanel.add(comboBrowser);
		
		JLabel lblPathTothsark = new JLabel("Path to 'tshark' binary:");
		lblPathTothsark.setBounds(12, 6, 180, 15);
		generalPanel.add(lblPathTothsark);
		
		txtTsharkPath = new JTextField();
		txtTsharkPath.setBounds(12, 24, 400, 19);
		generalPanel.add(txtTsharkPath);
		txtTsharkPath.setColumns(10);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(0, 126, 434, 2);
		generalPanel.add(separator);
		
		JCheckBox chckbxAllowUpdates = new JCheckBox("Allow automatic checking for software updates?");
		chckbxAllowUpdates.setBounds(12, 136, 400, 23);
		generalPanel.add(chckbxAllowUpdates);
		
		JLabel lblSessionDetection = new JLabel("Session detection:");
		lblSessionDetection.setBounds(12, 96, 140, 15);
		generalPanel.add(lblSessionDetection);
		
		JComboBox comboSessionDetection = new JComboBox();
		comboSessionDetection.setModel(new DefaultComboBoxModel(CookieCadgerUtils.sessionDetectionChoices.values()));
		comboSessionDetection.setBounds(159, 92, 250, 24);
		generalPanel.add(comboSessionDetection);
		
		JCheckBox chckbxEnableDemoMode = new JCheckBox("Enable 'demo mode' (requires session detection)");
		chckbxEnableDemoMode.setBounds(12, 161, 400, 23);
		generalPanel.add(chckbxEnableDemoMode);
		
		databasePanel = new JPanel();
		databasePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		databasePanel.setLayout(null);
		tabbedPane.addTab("Database Settings", null, databasePanel, null);
		
		JLabel lblDatabase = new JLabel("Database engine:");
		lblDatabase.setBounds(12, 12, 140, 15);
		databasePanel.add(lblDatabase);
		
		JComboBox comboDatabaseEngine = new JComboBox();
		comboDatabaseEngine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox source = ((JComboBox)e.getSource());
				
				boolean bUsingExternal = !source.getSelectedItem().equals(CookieCadgerUtils.databaseEngineChoices.SQLITE3);
				
				ChangeDatabaseFields(bUsingExternal);
			}
		});
		comboDatabaseEngine.setModel(new DefaultComboBoxModel(CookieCadgerUtils.databaseEngineChoices.values()));
		comboDatabaseEngine.setBounds(159, 8, 250, 24);
		databasePanel.add(comboDatabaseEngine);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(0, 48, 434, 2);
		databasePanel.add(separator_1);
		
		lblDatabaseHost = new JLabel("Database Host:");
		lblDatabaseHost.setBounds(12, 62, 120, 15);
		databasePanel.add(lblDatabaseHost);
		
		lblDatabaseUser = new JLabel("Database User:");
		lblDatabaseUser.setBounds(12, 86, 120, 15);
		databasePanel.add(lblDatabaseUser);
		
		lblDatabasePass = new JLabel("Database Pass:");
		lblDatabasePass.setBounds(12, 110, 120, 15);
		databasePanel.add(lblDatabasePass);
		
		lblDatabaseName = new JLabel("Database Name:");
		lblDatabaseName.setBounds(12, 134, 120, 15);
		databasePanel.add(lblDatabaseName);
		
		txtDatabaseHost = new JTextField();
		txtDatabaseHost.setBounds(146, 60, 265, 19);
		databasePanel.add(txtDatabaseHost);
		txtDatabaseHost.setColumns(10);
		
		txtDatabaseUser = new JTextField();
		txtDatabaseUser.setColumns(10);
		txtDatabaseUser.setBounds(146, 84, 265, 19);
		databasePanel.add(txtDatabaseUser);
		
		txtDatabasePass = new JTextField();
		txtDatabasePass.setColumns(10);
		txtDatabasePass.setBounds(146, 108, 265, 19);
		databasePanel.add(txtDatabasePass);
		
		txtDatabaseName = new JTextField();
		txtDatabaseName.setColumns(10);
		txtDatabaseName.setBounds(146, 132, 265, 19);
		databasePanel.add(txtDatabaseName);
	}
	
	private void ChangeDatabaseFields(boolean bUsingExternal)
	{
		txtDatabaseHost.setEnabled(bUsingExternal);
		txtDatabaseUser.setEnabled(bUsingExternal);
		txtDatabasePass.setEnabled(bUsingExternal);
		txtDatabaseName.setEnabled(bUsingExternal);
		
		lblDatabaseHost.setEnabled(bUsingExternal);
		lblDatabaseUser.setEnabled(bUsingExternal);
		lblDatabasePass.setEnabled(bUsingExternal);
		lblDatabaseName.setEnabled(bUsingExternal);
	}
}
