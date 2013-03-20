package com.cookiecadger;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

public class ReplayDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextArea txtUri, txtUserAgent, txtReferer, txtCookies, txtAuthorization;
	private String domain = null;

	public ReplayDialog(String domain, String uri, String userAgent, String referer, String cookies, String authorization)
	{
		this.domain = domain;
		
		setTitle("Modify & Replay Request");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 650, 520);
		setModal(true);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblDomain = new JLabel("");
		lblDomain.setFont(new Font("Dialog", Font.BOLD, 16));
		lblDomain.setBounds(12, 6, 623, 24);
		lblDomain.setText(domain);
		contentPanel.add(lblDomain);
		
		JLabel lblUri = new JLabel("URI:");
		lblUri.setBounds(12, 40, 120, 15);
		contentPanel.add(lblUri);
		
		JScrollPane scrollPaneUri = new JScrollPane();
		scrollPaneUri.setBounds(12, 58, 623, 40);
		contentPanel.add(scrollPaneUri);
		
		txtUri = new JTextArea();
		txtUri.setText(uri);
		txtUri.setLineWrap(true);
		scrollPaneUri.setViewportView(txtUri);
		
		JLabel lblUserAgent = new JLabel("User Agent:");
		lblUserAgent.setBounds(12, 110, 120, 15);
		contentPanel.add(lblUserAgent);
		
		JScrollPane scrollPaneUserAgent = new JScrollPane();
		scrollPaneUserAgent.setBounds(12, 128, 623, 40);
		contentPanel.add(scrollPaneUserAgent);
		
		txtUserAgent = new JTextArea();
		txtUserAgent.setText(userAgent);
		txtUserAgent.setLineWrap(true);
		scrollPaneUserAgent.setViewportView(txtUserAgent);
		
		JLabel lblReferer = new JLabel("Referer:");
		lblReferer.setBounds(12, 180, 120, 15);
		contentPanel.add(lblReferer);
		
		JScrollPane scrollPaneReferer = new JScrollPane();
		scrollPaneReferer.setBounds(12, 198, 623, 40);
		contentPanel.add(scrollPaneReferer);
		
		txtReferer = new JTextArea();
		txtReferer.setText(referer);
		txtReferer.setLineWrap(true);
		scrollPaneReferer.setViewportView(txtReferer);
		
		JLabel lblAuthorization = new JLabel("Authorization:");
		lblAuthorization.setBounds(12, 250, 120, 15);
		contentPanel.add(lblAuthorization);
		
		JScrollPane scrollPaneAuthorization = new JScrollPane();
		scrollPaneAuthorization.setBounds(12, 268, 623, 40);
		contentPanel.add(scrollPaneAuthorization);
		
		txtAuthorization = new JTextArea();
		txtAuthorization.setText(authorization);
		txtAuthorization.setLineWrap(true);
		scrollPaneAuthorization.setViewportView(txtAuthorization);
		
		JLabel lblCookies = new JLabel("Cookies:");
		lblCookies.setBounds(12, 320, 120, 15);
		contentPanel.add(lblCookies);
		
		JScrollPane scrollPaneCookies = new JScrollPane();
		scrollPaneCookies.setBounds(12, 338, 623, 100);
		contentPanel.add(scrollPaneCookies);
		
		txtCookies = new JTextArea();
		txtCookies.setText(cookies);
		txtCookies.setLineWrap(true);
		scrollPaneCookies.setViewportView(txtCookies);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("Replay");

		final String loadDomain = this.domain;
		okButton.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0)
			{
				final String loadUri = txtUri.getText();
				final String loadUserAgent = txtUserAgent.getText();
				final String loadReferer = txtReferer.getText();
				final String loadCookies = txtCookies.getText();
				final String loadAuthorization = txtAuthorization.getText();
				
		    	SwingWorker<?, ?> loadRequestWorker = new SwingWorker<Object, Object>() {            
		        	@Override            
		            public Object doInBackground()
		        	{
						BrowserHandler.loadRequestIntoBrowser(loadDomain, loadUri, loadUserAgent, loadReferer, loadCookies, loadAuthorization);
						
						return null;
		            }
		        };
		        
		        loadRequestWorker.run();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				ReplayDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}
}
