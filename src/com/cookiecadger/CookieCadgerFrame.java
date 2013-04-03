package com.cookiecadger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import com.cookiecadger.SortedListModel.SortOrder;

public class CookieCadgerFrame extends JFrame
{	
	private JPanel contentPane, requestsPanel, sessionsPanel;
	public EnhancedListModel clientsListModel, domainsListModel, requestsListModel, sessionsListModel;
	private DefaultComboBoxModel<String> interfacesListModel;
	public EnhancedJList clientsList, domainsList, requestsList, sessionsList;
	public EnhancedJTextField txtClientSearch, txtDomainSearch, txtRequestSearch;
	private JTextArea txtConsole;
	public JScrollPane consoleScrollPane;
	private JTabbedPane tabbedPane;
	public JProgressBar loadingRequestProgressBar;
	private HashMap<String, Component> componentMap; // Cheers to Jesse Strickland (stackoverflow.com/questions/4958600/get-a-swing-component-by-name)
	private JPopupMenu clientsPopup, domainsPopup, requestsPopup, sessionsPopup;
	private CaptureHandler captureHandler;

	public void addConsoleText(String text)
	{
		if(txtConsole.getText().isEmpty())
			txtConsole.setText(text);
		else
			txtConsole.append("\n" + text);
		
		txtConsole.setCaretPosition(txtConsole.getDocument().getLength());
	}
	
	private void clearGUI()
	{
		requestsListModel.clear();
		domainsListModel.clear();
		clientsListModel.clear();
		sessionsListModel.clear();
		
		txtClientSearch.setText("");
		txtDomainSearch.setText("");
		txtRequestSearch.setText("");
	}
	
	private boolean resetData()
	{
		// If sqlite, and this user session is not empty set
		if(Utils.dbEngine.equals("sqlite"))
		{
		    // Ask the user to save
		    int saveDatasetResult = JOptionPane.showConfirmDialog(
		            contentPane,
		            		"Would you like to save the current dataset?",
		            "Save?",
		            JOptionPane.YES_NO_CANCEL_OPTION);
		    
		    if (saveDatasetResult == JOptionPane.CANCEL_OPTION)
	    	{
		    	return false;
	    	}
		    else if (saveDatasetResult == JOptionPane.YES_OPTION)
		    {
		    	Utils.dbInstance.saveDatabase();
		    }
		}
		else
		{
		    // Ask the user if they want to reset external DBs
		    int saveDatasetResult = JOptionPane.showConfirmDialog(
		            contentPane,
		            		"Would you like to clear all data from the external database?\nTHIS CANNOT BE UNDONE!",
		            "Clear all data?",
		            JOptionPane.YES_NO_OPTION);
		    
		    if (saveDatasetResult == JOptionPane.NO_OPTION)
		    {
		    	return false;
		    }
		}

		try
		{
			Utils.dbInstance.clearTables();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		// Made it to here? That means we've committed to deletion, let the caller know
		return true;
	}
	
	public void stopCapture(int ethDevNumber)
	{
		interfacesListModel.removeElementAt(ethDevNumber);
		interfacesListModel.insertElementAt(captureHandler.deviceName.get(ethDevNumber) + " [" + captureHandler.deviceDescription.get(ethDevNumber) + "]", ethDevNumber);
		((JComboBox<?>)getComponentByName("interfaceListComboBox")).setSelectedIndex(ethDevNumber);

		if (captureHandler.deviceCaptureProcess.get(ethDevNumber) != null)
		{
			captureHandler.deviceCaptureProcess.get(ethDevNumber).destroy();
		}
		
		captureHandler.bCapturing.set(ethDevNumber, false);
		
		setCaptureButtonText();
	}
	
	private void prepCapture(int ethDevNumber)
	{
		interfacesListModel.removeElementAt(ethDevNumber);
		interfacesListModel.insertElementAt(captureHandler.deviceName.get(ethDevNumber) + " [" + captureHandler.deviceDescription.get(ethDevNumber) + "] (CURRENTLY CAPTURING)", ethDevNumber);
		((JComboBox<?>)getComponentByName("interfaceListComboBox")).setSelectedIndex(ethDevNumber);
		
		captureHandler.bCapturing.set(ethDevNumber, true);
		
		setCaptureButtonText();
	}
	
	private void setCaptureButtonText()
	{
		int selection = ((JComboBox<?>)getComponentByName("interfaceListComboBox")).getSelectedIndex();
		if(selection == -1)
		{
			((JButton)getComponentByName("btnMonitorOnSelected")).setEnabled(false);
			((JButton)getComponentByName("btnMonitorOnSelected")).setText("Select An Interface");
		}
		else
		{
			((JButton)getComponentByName("btnMonitorOnSelected")).setEnabled(true);
			
			if(captureHandler.bCapturing.get(selection))
			{
				((JButton)getComponentByName("btnMonitorOnSelected")).setText("Stop Capture on " + captureHandler.deviceName.get(selection));
			}
			else
			{
				((JButton)getComponentByName("btnMonitorOnSelected")).setText("Start Capture on " + captureHandler.deviceName.get(selection));
			}
		}
	}
	
	private void changeClientsList(boolean bClearFirst)
	{
		int clients = 0;
		final int clientCount;
		final boolean bClearListFirst;
		
		try {
			clients = Utils.dbInstance.getClientCount();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		clientCount = clients;
		bClearListFirst = bClearFirst;
		
		consoleScrollPane.setVisible(false);
		loadingRequestProgressBar.setString("Loading client list, please wait... [0 / " + clientCount + "]");
		loadingRequestProgressBar.setVisible(true);
        
		Thread changeClientsListThread = new Thread(new Runnable()
		{			 
		     public void run()
		     {		    	
				boolean bPreviousSelection = false;
				String previousSelection = null;
				int recordCount = 0;
				
				if(!clientsList.isSelectionEmpty())
				{
					bPreviousSelection = true;
					previousSelection = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
				}
				
				if(bClearListFirst)
				{
					clientsListModel.clear();
				}
				
				try
				{
					String[] macAddresses = Utils.dbInstance.getMacs(((EnhancedJTextField)getComponentByName("txtClientSearch")).getText());
					for (String s : macAddresses)
					{
						// Update the GUI with progress
					    if (recordCount % 10 == 0)
					    {
					    	final int currentRecord = recordCount;
				
					    	SwingUtilities.invokeLater(new Runnable()
							{
							    public void run()
							    {        			    	
							    	loadingRequestProgressBar.setString("Loading client list, please wait... (" + currentRecord + " / " + clientCount + ")");
							    }
							});
					    }
					    recordCount++;
				
					    if(!clientsListModel.contains(s))
					    {
					    	int clientID = Utils.dbInstance.getIntegerValue("clients", "id", "mac_address", s);
					    	clientsListModel.addElement(new EnhancedJListItem(clientID, s, null));
					    }
				
					    updateDescriptionForMac(s);
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				
				if(bClearListFirst && bPreviousSelection)
				{
					// If the newly generated list still contains the previously selected value, show it
					if (clientsListModel.contains(previousSelection))
					{
						int index = clientsListModel.indexOf(previousSelection);
						clientsList.setSelectedValue(clientsListModel.getElementAt(index), true);
					}
				}
				
				loadingRequestProgressBar.setVisible(false);
				consoleScrollPane.setVisible(true);
            }
    	});
		
		changeClientsListThread.start();
	}
	
	private void changeDomainsList(String macAddress)
	{		
		boolean bPreviousSelection = false;
		String previousSelection = null;
		
		if(!domainsList.isSelectionEmpty() && ((EnhancedJListItem)domainsList.getSelectedValue()).getID() != -1) // Not empty and not all domains
		{
			bPreviousSelection = true;
			previousSelection = ((EnhancedJListItem)domainsList.getSelectedValue()).toString();
		}
		
		domainsListModel.clear();
		requestsListModel.clear();
		
		domainsListModel.addElement(new EnhancedJListItem(-1, "[ All Domains ]", null));
		try
		{
			for (String s : Utils.dbInstance.getDomains(macAddress, ((EnhancedJTextField)getComponentByName("txtDomainSearch")).getText()))
			{
				int domainID = Utils.dbInstance.getIntegerValue("domains", "id", "name", s);
				domainsListModel.addElement(new EnhancedJListItem(domainID, s, null));
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if(bPreviousSelection)
		{
			// If the newly generated list still contains the previously selected value, show it
			if (domainsListModel.contains(previousSelection))
			{
				int index = domainsListModel.indexOf(previousSelection);
				domainsList.setSelectedValue(domainsListModel.getElementAt(index), true);
			}
		}
	}
	
	private void changeRequestsList(String macAddress, String uriHost)
	{		
		boolean bPreviousSelection = false;
		String previousSelection = null;

		if(!requestsList.isSelectionEmpty())
		{
			bPreviousSelection = true;
			previousSelection = ((EnhancedJListItem)requestsList.getSelectedValue()).toString();
		}
		
		boolean bShowAllDomains = false;
		requestsListModel.clear();
		
		if(uriHost.equals("[ All Domains ]"))
		{
			bShowAllDomains = true;
			uriHost = "%";
		}
		
		try {
			ArrayList<ArrayList> requests = Utils.dbInstance.getRequests(macAddress, uriHost, ((EnhancedJTextField)getComponentByName("txtRequestSearch")).getText());
			
			ArrayList<String> ids = requests.get(0);
			ArrayList<String> timerecordeds = requests.get(1);
			ArrayList<String> uris = requests.get(2);
			ArrayList<String> descriptions = requests.get(3);
			
			for (int i = 0; i < ids.size(); i++)
			{
				long timeStamp = new Long(timerecordeds.get(i));
				Date then = new Date((long)timeStamp * 1000);
				Date now = new Date();
				
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
				boolean sameDay = fmt.format(then).equals(fmt.format(now));
				
				String dateString;
				if(sameDay)
				{
					dateString = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(then);
				}
				else
				{
					dateString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(then);
				}

				int requestID = new Integer(ids.get(i));
				String uri = uris.get(i);
				String description = descriptions.get(i);
				
				// If showing all domains in the request list, make sure the full request Url gets displayed
				if(bShowAllDomains)
				{
					int domainID = Utils.dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
					String domain = Utils.dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
					uri = domain + uri;
				}

		    	EnhancedJListItem requestItem = new EnhancedJListItem(requestID, dateString + ": " + uri, description);
		    	requestsListModel.addElement(requestItem);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if(bPreviousSelection)
		{			
			// If the newly generated list still contains the previously selected value, show it
			if (requestsListModel.contains(previousSelection))
			{
				int index = requestsListModel.indexOf(previousSelection);
				requestsList.setSelectedValue(requestsListModel.getElementAt(index), true);
			}
		}
	}

	public void changeSessionsList(boolean bClearFirst)
	{
		final boolean bClearListFirst;
		bClearListFirst = bClearFirst;
		
		Thread changeSessionsListThread = new Thread(new Runnable()
		{
		     public void run()
		     {
				Image photo = null;
				
				if(bClearListFirst)
					sessionsListModel.clear();
				
				EnhancedJListItem[] sessions;
				try
				{
					sessions = Utils.dbInstance.getSessions();
					
					for (EnhancedJListItem item : sessions)
					{
						if(sessionsListModel.contains(item.getID()))
							continue;

						String profilePhotoUrl = item.getProfileImageUrl();
						boolean bHaveImage = false;
						
						// Make image
						if(profilePhotoUrl != null && !profilePhotoUrl.isEmpty())
						{
							try
							{
								URL url = new URL(profilePhotoUrl);
								photo = ImageIO.read(url);
								
								bHaveImage = true;
							}
							catch (Exception e)
							{
								bHaveImage = false;
							}
						}
						else
						{
							try
							{
								int sessionID = item.getID();
								int requestID = Utils.dbInstance.getIntegerValue("sessions", "request_id", "id", Integer.toString(sessionID));
								int domainID = Utils.dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
								String domain = Utils.dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
								
								URL url = new URL("https://www.google.com/s2/favicons?domain=" + domain);
								photo = ImageIO.read(url);
								photo = Utils.createResizedCopy(photo, 48, 48, true);
								
								bHaveImage = true;
							}
							catch (Exception e)
							{
								bHaveImage = false;
							}
						}
						
						if(bHaveImage)
						{
							item.setThumbnail(photo);
						}
						
						sessionsListModel.addElement(item);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		     }
		});
		
		changeSessionsListThread.start();
	}
	
	public void updateDescriptionForMac(String macAddress)
	{
		if(clientsListModel.contains(macAddress))
		{
			int location = clientsListModel.indexOf(macAddress);
			((EnhancedJListItem)clientsListModel.getElementAt(location)).setDescription(generateDescriptionForClient(macAddress, true));
		}
	}
	
	private String generateDescriptionForClient(String macAddress, boolean bUseHTML)
	{
		String htmlOpen = "";
		String boldOpen = "";
		String fontOpen = "";
		String fontClose = "";
		String boldClose = "";
		String htmlClose = "";
		String newLine = "\r\n";
		
		if(bUseHTML)
		{
			htmlOpen = "<html>";
			boldOpen = "<b>";
			fontOpen = "<font size=4>";
			fontClose = "</font>";
			boldClose = "</b>";
			htmlClose = "</html>";
			newLine = "<br>";
		}
		
		try
		{
			String[] fields = new String[] { "netbios_hostname", "mdns_hostname", "ipv4_address", "ipv6_address" };
			HashMap<String,String> resultMap = Utils.dbInstance.getStringValue("clients", fields, "mac_address", macAddress);
			
			String notesTxt = htmlOpen + fontOpen + boldOpen + "MAC Address: " + boldClose + macAddress;
			
			if(!resultMap.get("ipv4_address").isEmpty())
			{
				notesTxt = notesTxt + newLine + boldOpen + "IPv4 Address: " + boldClose + resultMap.get("ipv4_address");
			}
			
			if(!resultMap.get("ipv6_address").isEmpty())
			{				
				notesTxt = notesTxt + newLine + boldOpen + "IPv6 Address: " + boldClose + resultMap.get("ipv6_address");
			}
			
			if(!resultMap.get("netbios_hostname").isEmpty())
			{
				notesTxt = notesTxt + newLine + boldOpen + "Host Name (NetBIOS): " + boldClose + resultMap.get("netbios_hostname");
			}
			
			if(!resultMap.get("mdns_hostname").isEmpty())
			{
				notesTxt = notesTxt + newLine + boldOpen + "Host Name (mDNS/Bonjour): " + boldClose + resultMap.get("mdns_hostname");
			}

			String[] userAgents = Utils.dbInstance.getUserAgents(macAddress);
			ArrayList<String> displayUserAgents = new ArrayList<String>();
			
			for (String userAgent : userAgents)
			{
				if(userAgent.trim().length() > 0)
					displayUserAgents.add(userAgent.trim());
			}
			
			if(displayUserAgents.size() > 0)
			{
				notesTxt = notesTxt + newLine + boldOpen + "Detected User-Agents: " + boldClose;
				
				for (String userAgent : displayUserAgents)
				{
					notesTxt = notesTxt + newLine + "  - " + userAgent;
				}
			}
			
			notesTxt = notesTxt + fontClose + htmlClose;
			
			return notesTxt;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void prepareToCloseApplication()
	{
		if(Utils.dbEngine.equals("sqlite"))
		{
			// Try to get the database unloaded
			try
			{
				// Reset DB, which will ask user to save
				boolean bResetCommit = resetData();
				
				if(!bResetCommit)
					return;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		BrowserHandler.closeConnections();
		
		try
		{
			for (Process p : captureHandler.deviceCaptureProcess)
			{
				if(p != null)
					p.destroy();
			}
			
			if(Utils.dbInstance != null)
				Utils.dbInstance.closeDatabase();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		// All done, goodbye
		dispose();
		System.exit(0);
	}
	
	public CookieCadgerFrame() throws Exception
	{		
		// Tell Utils that we're in GUI mode
		Utils.cookieCadgerFrame = this;
		Utils.initializeDatabase();
		
		// Prepare capture handler
		captureHandler = new CaptureHandler();

		createGUI();
	}
	
	public void createGUI() throws Exception
	{		
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				prepareToCloseApplication();
			}
		});
		
		setTitle("Cookie Cadger");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 950, 680);
		
		URL url = this.getClass().getResource("/resource/cookiecadger.png");
		BufferedImage img = ImageIO.read(url);
		this.setIconImage(img);
		
		if((Integer)Utils.programSettings.get("bSessionDetection") == -1)
		{
	        // Ask the user about session detection
	        int sessionDetection = JOptionPane.showConfirmDialog(
	                contentPane,
	                		"Session detection replays web requests in the background and analyzes\n" +
	                		"them for evidence that a user is logged in. Enabling session detection\n" +
	                		"will cause Cookie Cadger to utilize a larger amount of available\n" +
	                		"system resources.\n\n" +
	                		"By enabling this feature you also understand that:\n\n" +
	                		"1) Cookie Cadger will (potentially) automatically impersonate any\n" +
	                		"network user without their explicit permission or interaction on your part.\n\n" +
	                		"2) The legality of doing so varies between jurisdictions. It is your\n" +
	                		"responsibility to understand and comply with any applicable laws.\n\n" +
	                		"Would you like to enable session detection?\n ",
	                "Enable Session Detection?",
	                JOptionPane.YES_NO_OPTION);
	        
	        if (sessionDetection == JOptionPane.YES_OPTION)
	        {
	        	Utils.programSettings.put("bSessionDetection", 1);
	        }
		}
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmStartNewSession = new JMenuItem("Start New Dataset");
		mnFile.add(mntmStartNewSession);

		JMenuItem mntmSettings = new JMenuItem("Program Settings");
		mnFile.add(mntmSettings);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmSaveSession = null;
		JMenuItem mntmLoadSession = null;
		
		if(!Utils.bUsingExternalDatabase)
		{
			mntmSaveSession = new JMenuItem("Save Dataset");
			mnFile.add(mntmSaveSession);
			
			mntmLoadSession = new JMenuItem("Load Dataset");
			mnFile.add(mntmLoadSession);

			mnFile.add(new JSeparator());
		}
		
		JMenuItem mntmOpenACapture = new JMenuItem("Open a Capture File (*.pcap)");
		mnFile.add(mntmOpenACapture);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmCopySelectedRequest = new JMenuItem("Copy Selected Request to Clipboard");
		mnEdit.add(mntmCopySelectedRequest);
		
		JMenuItem mntmCopyAllRequests = new JMenuItem("Copy All Requests to Clipboard");
		mnEdit.add(mntmCopyAllRequests);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About Cookie Cadger");
		mnHelp.add(mntmAbout);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		clientsListModel = new EnhancedListModel();
		domainsListModel = new EnhancedListModel();
		requestsListModel = new EnhancedListModel();
		sessionsListModel = new EnhancedListModel();
		interfacesListModel = new DefaultComboBoxModel<String>();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(28, 48, 894, 416);

		requestsPanel = new JPanel();
		requestsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		requestsPanel.setLayout(null);
		
		tabbedPane.addTab("Requests", null, requestsPanel, null);
		
		if((Integer)Utils.programSettings.get("bSessionDetection") == 1)
		{
			sessionsPanel = new JPanel();
			sessionsPanel.setLayout(null);

			tabbedPane.addTab("Recognized Sessions", null, sessionsPanel, null);
		
			JScrollPane sessionsScrollPane = new JScrollPane();
			sessionsScrollPane.setBounds(2, 2, 886, 320);
			sessionsPanel.add(sessionsScrollPane);
			
			sessionsList = new EnhancedJList();
			sessionsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			sessionsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			sessionsList.setVisibleRowCount(-1);
			sessionsList.setModel(sessionsListModel);
			
			sessionsScrollPane.setViewportView(sessionsList);
			
			JCheckBox chckbxAutomaticallyLoadSessions = new JCheckBox("Automatically Load Sessions Into Browser (Demo Mode)");
			chckbxAutomaticallyLoadSessions.setBounds(12, 332, 420, 25);
			chckbxAutomaticallyLoadSessions.setName("chckbxAutomaticallyLoadSessions");
			chckbxAutomaticallyLoadSessions.setSelected((Boolean) Utils.programSettings.get("bUseDemoMode"));
			sessionsPanel.add(chckbxAutomaticallyLoadSessions);
			
			JButton btnLoadSelectedSession = new JButton("Load Selected Session");
			btnLoadSelectedSession.setBounds(657, 332, 220, 25);
			sessionsPanel.add(btnLoadSelectedSession);
			
			btnLoadSelectedSession.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
			    	SwingWorker<?, ?> loadSessionWorker = new SwingWorker<Object, Object>() {            
			        	@Override
			            public Object doInBackground()
			        	{
							EnhancedJListItem listItem = (EnhancedJListItem)sessionsList.getSelectedValue();
							int sessionID = listItem.getID();
							
							try
							{
								int requestID = Utils.dbInstance.getIntegerValue("sessions", "request_id", "id", Integer.toString(sessionID));
								int domainID = Utils.dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
								String sessionUri = Utils.dbInstance.getStringValue("sessions", "session_uri", "id", Integer.toString(sessionID));
								String domain = Utils.dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
								
								String[] fields = new String[] { "useragent", "referer", "authorization", "cookies", "uri" };
								HashMap<String,String> resultMap = Utils.dbInstance.getStringValue("requests", fields, "id", Integer.toString(requestID));
								
								String useragent = resultMap.get("useragent");
								String referer = resultMap.get("referer");
								String authorization = resultMap.get("authorization");
								String cookies = resultMap.get("cookies");

								String uri;
								
								if(sessionUri != null && !sessionUri.isEmpty())
								{
									uri = sessionUri;
								}
								else
								{
									uri = resultMap.get("uri");
								}

								BrowserHandler.loadRequestIntoBrowser(domain, uri, useragent, referer, cookies, authorization);
							} catch (Exception e)
							{
								e.printStackTrace();
							}
			        		
			                return null;
			            }
			        };
			        loadSessionWorker.execute();
				}
			});
			
			sessionsPopup = new JPopupMenu();
			JMenuItem sessionsMenuItem = new JMenuItem("View Associated Request");
			sessionsMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!sessionsList.isSelectionEmpty())
					{
						int sessionID = ((EnhancedJListItem)sessionsList.getSelectedValue()).getID();

						try
						{
							final int requestID = Utils.dbInstance.getIntegerValue("sessions", "request_id", "id", Integer.toString(sessionID));
							final int domainID = Utils.dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
							final int clientID = Utils.dbInstance.getIntegerValue("requests", "client_id", "id", Integer.toString(requestID));
							
							SwingUtilities.invokeLater(new Runnable()
							{
							    public void run()
							    {
							    	// Select the tab
							        tabbedPane.setSelectedIndex(0);

									SwingUtilities.invokeLater(new Runnable()
									{
									    public void run()
									    {
									    	// Select the client
									    	if(clientsListModel.contains(clientID))
									    	{
									    		clientsList.setSelectedValue(clientsListModel.getElementById(clientID), true);
									    		
												SwingUtilities.invokeLater(new Runnable()
												{
												    public void run()
												    {												    	
												    	// Select the domain												    	
												    	if(domainsListModel.contains(domainID))
												    	{
												    		domainsList.setSelectedValue(domainsListModel.getElementById(domainID), true);
												    		
															SwingUtilities.invokeLater(new Runnable()
															{
															    public void run()
															    {
															    	// Select the request
															    	if(requestsListModel.contains(requestID))
															    	{
															    		requestsList.setSelectedValue(requestsListModel.getElementById(requestID), true);
															    	}
															    }
															});
												    	}
												    }
												});
									    	}
									    }
									});
							    }
							});
						} catch (SQLException e1) {
							e1.printStackTrace();
						}						
					}
				}
			});
		    sessionsPopup.add(sessionsMenuItem);
		    
		    sessionsList.addMouseListener(new MouseAdapter()
		    {
		    	public void mouseClicked(MouseEvent me)
		    	{
		            if (SwingUtilities.isRightMouseButton(me))
		            {	                
		                Point mousePosition = me.getPoint();
		                int index = sessionsList.locationToIndex(me.getPoint());
	        			Rectangle cellRect = sessionsList.getCellBounds(index, index);
	        			
	        			// If point inside rectangle
	        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
	        			{
	    	            	// If right-clicked item is not currently selected, do that
	    	            	if( index != sessionsList.getSelectedIndex())
	    	            		sessionsList.setSelectedIndex(index);
	    	            	
	    	            	sessionsPopup.show(sessionsList, me.getX(), me.getY());
	        			}
		            }
		    	}
		    });
		}
		contentPane.add(tabbedPane);
		
		txtConsole = new JTextArea();
		txtConsole.setBackground(UIManager.getColor("Panel.background"));
		txtConsole.setFont(new Font("Verdana", Font.BOLD, 12));
		txtConsole.setEditable(false);
		
		consoleScrollPane = new JScrollPane();
		consoleScrollPane.setBounds(28, 475, 895, 130);
		contentPane.add(consoleScrollPane);
		consoleScrollPane.setViewportView(txtConsole);
		
		loadingRequestProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
	    loadingRequestProgressBar.setStringPainted(true);
	    loadingRequestProgressBar.setFont(new Font("Verdana", Font.BOLD, 22));
	    loadingRequestProgressBar.setIndeterminate(true);
	    loadingRequestProgressBar.setVisible(false);
	    loadingRequestProgressBar.setBounds(consoleScrollPane.getBounds());
	    contentPane.add(loadingRequestProgressBar);
		
		JButton btnMonitorOnSelected = new JButton("Select An Interface");
		btnMonitorOnSelected.setEnabled(false);
		btnMonitorOnSelected.setName("btnMonitorOnSelected");
		btnMonitorOnSelected.setBounds(681, 14, 240, 25);
		contentPane.add(btnMonitorOnSelected);
		
		JComboBox<String> interfaceListComboBox = new JComboBox<String>();
		interfaceListComboBox.setName("interfaceListComboBox");
		interfaceListComboBox.setBounds(28, 14, 626, 24);
		contentPane.add(interfaceListComboBox);
		
		interfaceListComboBox.setModel(interfacesListModel);
		
		JScrollPane clientsListScrollPanel = new JScrollPane();
		clientsListScrollPanel.setBounds(22, 32, 152, 336);
		requestsPanel.add(clientsListScrollPanel);
		
		clientsList = new EnhancedJList();
		
		clientsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientsList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting() && !clientsList.isSelectionEmpty())
				{
					JList<?> list = (JList<?>)e.getSource();
					String item = ((EnhancedJListItem)list.getSelectedValue()).toString();
					changeDomainsList(item);
				}
			}
		});

		clientsListScrollPanel.setViewportView(clientsList);
		SortedListModel clientsListModelSorted = new SortedListModel(clientsListModel);
		clientsListModelSorted.setSortOrder(SortOrder.ASCENDING);
		clientsList.setModel(clientsListModelSorted);
		
		JScrollPane domainListScrollPane = new JScrollPane();
		domainListScrollPane.setBounds(180, 32, 200, 336);
		requestsPanel.add(domainListScrollPane);
		
		domainsList = new EnhancedJList();
		
		domainsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		domainsList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting() && !clientsList.isSelectionEmpty() && !domainsList.isSelectionEmpty())
				{
					JList<?> list = (JList<?>)e.getSource();
					String item = ((EnhancedJListItem)list.getSelectedValue()).toString();
					String macAddress = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
					changeRequestsList(macAddress, item);
				}
			}
		});
		domainsList.addMouseListener(new MouseAdapter()
	    {
	    	public void mouseClicked(MouseEvent me)
	    	{
	            if (SwingUtilities.isRightMouseButton(me))
	            {	                
	                Point mousePosition = me.getPoint();
	                int index = domainsList.locationToIndex(me.getPoint());
        			Rectangle cellRect = domainsList.getCellBounds(index, index);
        			
        			// If point inside rectangle
        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
        			{
    	            	// If right-clicked item is not currently selected, do that
    	            	if( index != domainsList.getSelectedIndex())
    	            		domainsList.setSelectedIndex(index);
    	            	
    	            	domainsPopup.show(domainsList, me.getX(), me.getY());
        			}
	            }
	    	}
	    });
		
		domainListScrollPane.setViewportView(domainsList);
		SortedListModel domainListModelSorted = new SortedListModel(domainsListModel);
		domainListModelSorted.setSortOrder(SortOrder.ASCENDING);
		domainsList.setModel(domainListModelSorted);
		
		domainsPopup = new JPopupMenu();
		
		JMenuItem copyDomainInfoMenuItem = new JMenuItem("Copy Domain Information");
		copyDomainInfoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!domainsList.isSelectionEmpty())
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();
					String domain = ((EnhancedJListItem)domainsList.getSelectedValue()).toString();
					StringSelection strSel = new StringSelection(domain);
					clipboard.setContents(strSel, null);
				}
			}
		});
		domainsPopup.add(copyDomainInfoMenuItem);
		
		JScrollPane requestListScrollPanel = new JScrollPane();
		requestListScrollPanel.setBounds(386, 32, 482, 306);
		requestsPanel.add(requestListScrollPanel);

		requestsList = new EnhancedJList();
		
		Font fontMonospace = new Font( "Monospaced", Font.BOLD, 12 ); 
		requestsList.setFont(fontMonospace);
		requestsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		requestsList.setModel(requestsListModel);
		requestListScrollPanel.setViewportView(requestsList);
		
		clientsPopup = new JPopupMenu();
		
		JMenuItem copyHostInfoMenuItem = new JMenuItem("Copy Host Information");
		copyHostInfoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!clientsList.isSelectionEmpty())
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();
					String macAddress = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
					StringSelection strSel = new StringSelection(generateDescriptionForClient(macAddress, false));
					clipboard.setContents(strSel, null);
				}
			}
		});
	    clientsPopup.add(copyHostInfoMenuItem);
	    
		JMenuItem copyMacAddressMenuItem = new JMenuItem("Copy MAC Address");
		copyMacAddressMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!clientsList.isSelectionEmpty())
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();
					String macAddress = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
					StringSelection strSel = new StringSelection(macAddress);
					clipboard.setContents(strSel, null);
				}
			}
		});
	    clientsPopup.add(copyMacAddressMenuItem);
	    
	    clientsList.addMouseListener(new MouseAdapter()
	    {
	    	public void mouseClicked(MouseEvent me)
	    	{
	            if (SwingUtilities.isRightMouseButton(me))
	            {
	            	// Disable ToolTip for 3 seconds
	            	SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() {            
	                	@Override            
	                    public Object doInBackground() {
	                        try {
	                        	ToolTipManager.sharedInstance().setEnabled(false);
	                            Thread.sleep(3000);
	                        } catch (InterruptedException e) { /*Who cares*/ }
	                        return null;
	                    }
	                    @Override
	                    public void done()
	                    {
	                    	ToolTipManager.sharedInstance().setEnabled(true);
	                    }
	                };
	                worker.execute();
	            	
	                Point mousePosition = me.getPoint();
	                int index = clientsList.locationToIndex(me.getPoint());
        			Rectangle cellRect = clientsList.getCellBounds(index, index);
        			
        			// If point inside rectangle
        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
        			{
    	            	// If right-clicked item is not currently selected, do that
    	            	if( index != clientsList.getSelectedIndex())
    	            		clientsList.setSelectedIndex(index);
    	            	
    	            	clientsPopup.show(clientsList, me.getX(), me.getY());
        			}
	            }
	    	}
	    });
	    
		requestsPopup = new JPopupMenu();
		JMenuItem requestMenuItem = new JMenuItem("Copy Request Information");
		requestMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!requestsList.isSelectionEmpty())
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();
					int request = ((EnhancedJListItem)requestsList.getSelectedValue()).getID();
					StringSelection strSel = new StringSelection(Utils.generateDescriptionForRequest(request, false, false));				
					clipboard.setContents(strSel, null);
				}
			}
		});
	    requestsPopup.add(requestMenuItem);
	    
	    requestsList.addMouseListener(new MouseAdapter()
	    {
	    	public void mouseClicked(MouseEvent me)
	    	{
	            if (SwingUtilities.isRightMouseButton(me))
	            {
	            	// Disable ToolTip for 3 seconds
	            	SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() {            
	                	@Override            
	                    public Object doInBackground() {
	                        try {
	                        	ToolTipManager.sharedInstance().setEnabled(false);
	                            Thread.sleep(3000);
	                        } catch (InterruptedException e) { /*Who cares*/ }
	                        return null;
	                    }
	                    @Override
	                    public void done()
	                    {
	                    	ToolTipManager.sharedInstance().setEnabled(true);
	                    }
	                };
	                worker.execute();
	                
	                Point mousePosition = me.getPoint();
	                int index = requestsList.locationToIndex(me.getPoint());
        			Rectangle cellRect = requestsList.getCellBounds(index, index);
        			
        			// If point inside rectangle
        			if(mousePosition.x >= cellRect.getMinX() && mousePosition.x < cellRect.getMaxX() && mousePosition.y >= cellRect.getMinY() && mousePosition.y < cellRect.getMaxY())
        			{
    	            	// If right-clicked item is not currently selected, do that
    	            	if( index != requestsList.getSelectedIndex())
    	            		requestsList.setSelectedIndex(index);
    	            	
    	            	requestsPopup.show(requestsList, me.getX(), me.getY());
        			}
	            }
	    	}
	    });
	    
		JButton btnLoadDomainCookies = new JButton("Load Domain Cookies");
		btnLoadDomainCookies.setFont(new Font("Dialog", Font.BOLD, 10));
		btnLoadDomainCookies.setBounds(386, 342, 155, 25);
		requestsPanel.add(btnLoadDomainCookies);
		
		JButton btnReplayRequest = new JButton("Replay Request");
		btnReplayRequest.setFont(new Font("Dialog", Font.BOLD, 10));
		btnReplayRequest.setBounds(553, 342, 126, 25);
		requestsPanel.add(btnReplayRequest);
		
		JButton btnModifyReplay = new JButton("Modify & Replay Request");
		btnModifyReplay.setFont(new Font("Dialog", Font.BOLD, 10));
		btnModifyReplay.setBounds(691, 342, 176, 25);
		requestsPanel.add(btnModifyReplay);
		
		txtClientSearch = new EnhancedJTextField();
		txtClientSearch.setName("txtClientSearch");
		txtClientSearch.setPlaceholder("Filter MACs", new Font("SansSerif", Font.BOLD, 10));
		txtClientSearch.setBounds(22, 8, 152, 19);
		requestsPanel.add(txtClientSearch);
		txtClientSearch.setColumns(10);
		
		txtDomainSearch = new EnhancedJTextField();
		txtDomainSearch.setName("txtDomainSearch");
		txtDomainSearch.setPlaceholder("Filter Domains", new Font("SansSerif", Font.BOLD, 10));
		txtDomainSearch.setBounds(179, 8, 201, 19);
		requestsPanel.add(txtDomainSearch);
		txtDomainSearch.setColumns(10);
		
		txtRequestSearch = new EnhancedJTextField();
		txtRequestSearch.setName("txtRequestSearch");
		txtRequestSearch.setPlaceholder("Filter Requests", new Font("SansSerif", Font.BOLD, 10));
		txtRequestSearch.setBounds(386, 8, 482, 19);
		requestsPanel.add(txtRequestSearch);
		txtRequestSearch.setColumns(10);
		
		JLabel lblSoftwareUpdateAvailable = new JLabel("");
		lblSoftwareUpdateAvailable.setName("lblSoftwareUpdateAvailable");
		lblSoftwareUpdateAvailable.setBounds(28, 606, 895, 15);
		contentPane.add(lblSoftwareUpdateAvailable);
		
		/*
		 * Create and associate the ActionListeners for all objects
		 */
		
		mntmStartNewSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				boolean resetCommit = resetData();
				
				if(resetCommit)
				{
					clearGUI();
					try {
						Utils.dbInstance.initTables();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		mntmSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{				
				SettingsDialog settingsInterface = new SettingsDialog();
				settingsInterface.setVisible(true);
			}
		});
		
		mntmOpenACapture.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				JFileChooser fc = new JFileChooser();
				FileFilter pcapFilter = new FileNameExtensionFilter("*.pcap | *.cap | *.pcapdump", "pcap", "cap", "pcapdump");
				fc.addChoosableFileFilter(pcapFilter);
				fc.setFileFilter(pcapFilter);
				int returnVal = fc.showOpenDialog(null);

		        if (returnVal == JFileChooser.APPROVE_OPTION)
		        {
		        	File file = fc.getSelectedFile();
		        	final String pcapFile = file.getAbsolutePath();
		        	JOptionPane.showMessageDialog(null, "This process could take some time, please be patient.");
		        	
			    	SwingWorker<?, ?> openCaptureWorker = new SwingWorker<Object, Object>() {            
			        	@Override            
			            public Object doInBackground()
			        	{
			        		try
							{
								consoleScrollPane.setVisible(false);
								loadingRequestProgressBar.setString("Processing capture file, please wait...");
								loadingRequestProgressBar.setVisible(true);

								captureHandler.startCapture(-1, pcapFile);
								
				                loadingRequestProgressBar.setVisible(false);
				                consoleScrollPane.setVisible(true);
					                
							} catch (IOException e) {
								e.printStackTrace();
							}
			        		
			                return null;
			            }
			        };
			        
			        openCaptureWorker.execute();
				}
			}
		});

		if(!Utils.bUsingExternalDatabase)
		{
			mntmSaveSession.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Utils.dbInstance.saveDatabase();
				}
			});

			mntmLoadSession.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					boolean resetCommit = resetData();
					
					if(resetCommit)
					{
						clearGUI();
						
						try {
							Utils.dbInstance.initTables();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						
						Utils.dbInstance.openDatabase();
				        
				    	changeClientsList(true);
				    	
				    	if((Integer) Utils.programSettings.get("bSessionDetection") == 1)
				    		changeSessionsList(true);
					}
				}
			});
		}

		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				prepareToCloseApplication();
			}
		});


		mntmCopySelectedRequest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(requestsList.isSelectionEmpty())
				{
					JOptionPane.showMessageDialog(null, "You must first select a request.");
				}
				else
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();				
					int request = ((EnhancedJListItem)requestsList.getSelectedValue()).getID();
					StringSelection strSel = new StringSelection(Utils.generateDescriptionForRequest(request, false, false));
					clipboard.setContents(strSel, null);
				}
			}
		});


		mntmCopyAllRequests.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String allRequests = "";
				
				for (int i = 0; i < requestsListModel.getSize(); i++)
				{
					int request = ((EnhancedJListItem)requestsListModel.getElementAt(i)).getID();
					allRequests = allRequests + Utils.generateDescriptionForRequest(request, false, false);
					
					if( i < requestsListModel.getSize() - 1)
						allRequests = allRequests + "\r\n\r\n---\r\n\r\n";
				}
				
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = toolkit.getSystemClipboard();
				StringSelection strSel = new StringSelection(allRequests);
				clipboard.setContents(strSel, null);	
			}
		});


		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Utils.displayAboutWindow();
			}
		});
		
		// Listen for changes in the client filter textbox
		txtClientSearch.getDocument().addDocumentListener(new DocumentListener() {
		  public void changedUpdate(DocumentEvent e) {
			  changeClientsList(true);
		  }
		  public void removeUpdate(DocumentEvent e) {
			  changeClientsList(true);
		  }
		  public void insertUpdate(DocumentEvent e) {
			  changeClientsList(true);
		  }
		});
		
		// Listen for changes in the domain filter textbox
		txtDomainSearch.getDocument().addDocumentListener(new DocumentListener() {
		  public void changedUpdate(DocumentEvent e) {
			  ChangeDomains();
		  }
		  public void removeUpdate(DocumentEvent e) {
			  ChangeDomains();
		  }
		  public void insertUpdate(DocumentEvent e) {
			  ChangeDomains();
		  }
		  
		  private void ChangeDomains()
		  {
				if(!clientsList.getValueIsAdjusting() && !clientsList.isSelectionEmpty())
				{
					String item = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
					changeDomainsList(item);
				}
		  }
		});
		
		// Listen for changes in the request filter textbox
		txtRequestSearch.getDocument().addDocumentListener(new DocumentListener() {
		  public void changedUpdate(DocumentEvent e) {
			  ChangeRequests();
		  }
		  public void removeUpdate(DocumentEvent e) {
			  ChangeRequests();
		  }
		  public void insertUpdate(DocumentEvent e) {
			  ChangeRequests();
		  }
		  
		  private void ChangeRequests()
		  {
				if( (!clientsList.getValueIsAdjusting() && !clientsList.isSelectionEmpty()) &&
						(!domainsList.getValueIsAdjusting() && !domainsList.isSelectionEmpty()) )
				{
					String client = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
					String domain = ((EnhancedJListItem)domainsList.getSelectedValue()).toString();
					changeRequestsList(client, domain);
				}
		  }
		});

		btnLoadDomainCookies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				if(((EnhancedJListItem)domainsList.getSelectedValue()).getID() == -1) // "All Domains"
				{
					JOptionPane.showMessageDialog(null, "This option is not available when \"[ All Domains ]\" is selected.");
				}
				else if(!domainsList.isSelectionEmpty())
				{
			    	SwingWorker<?, ?> loadRequestWorker = new SwingWorker<Object, Object>() {            
			        	@Override            
			            public Object doInBackground()
			        	{
							try {
								int clientID = Utils.dbInstance.getIntegerValue("clients", "id", "mac_address", ((EnhancedJListItem)clientsList.getSelectedValue()).toString());
								int domainID = Utils.dbInstance.getIntegerValue("domains", "id", "name", ((EnhancedJListItem)domainsList.getSelectedValue()).toString());
								int requestID = Utils.dbInstance.getNewestRequestID(clientID, domainID);
								
								String domain = ((EnhancedJListItem)domainsList.getSelectedValue()).toString();
								String uri = "/";
								
								String[] fields = new String[] { "useragent", "referer", "authorization", "cookies" };
								HashMap<String,String> resultMap = Utils.dbInstance.getStringValue("requests", fields, "id", Integer.toString(requestID));
								
								String useragent = resultMap.get("useragent");
								String referer = resultMap.get("referer");
								String authorization = resultMap.get("authorization");
								String cookies = resultMap.get("cookies");
								
								BrowserHandler.loadRequestIntoBrowser(domain, uri, useragent, referer, cookies, authorization);
							} catch (SQLException e) {
								e.printStackTrace();
							}
							
							return null;
			            }
			        };
			        
			        loadRequestWorker.execute();
				}
			}
		});

		btnReplayRequest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				if(!requestsList.isSelectionEmpty())
				{
			    	SwingWorker<?, ?> loadRequestWorker = new SwingWorker<Object, Object>() {            
			        	@Override            
			            public Object doInBackground()
			        	{
							EnhancedJListItem listItem = (EnhancedJListItem)requestsList.getSelectedValue();
							int requestID = listItem.getID();
							
							try {
								int domainID = Utils.dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
								String domain = Utils.dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
								
								String[] fields = new String[] { "uri", "useragent", "referer", "authorization", "cookies" };
								HashMap<String,String> resultMap = Utils.dbInstance.getStringValue("requests", fields, "id", Integer.toString(requestID));
								
								String uri = resultMap.get("uri");
								String useragent = resultMap.get("useragent");
								String referer = resultMap.get("referer");
								String authorization = resultMap.get("authorization");
								String cookies = resultMap.get("cookies");
								
								BrowserHandler.loadRequestIntoBrowser(domain, uri, useragent, referer, cookies, authorization);
							} catch (SQLException e) {
								e.printStackTrace();
							}
			        		
			                return null;
			            }
			        };
			        
			        loadRequestWorker.execute();
				}
			}
		});

		btnModifyReplay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				if(!requestsList.isSelectionEmpty())
				{
			    	SwingWorker<?, ?> loadRequestWorker = new SwingWorker<Object, Object>() {            
			        	@Override            
			            public Object doInBackground()
			        	{
							EnhancedJListItem listItem = (EnhancedJListItem)requestsList.getSelectedValue();
							int requestID = listItem.getID();
							
							try {
								int domainID = Utils.dbInstance.getIntegerValue("requests", "domain_id", "id", Integer.toString(requestID));
								String domain = Utils.dbInstance.getStringValue("domains", "name", "id", Integer.toString(domainID));
								
								String[] fields = new String[] { "uri", "useragent", "referer", "authorization", "cookies" };
								HashMap<String,String> resultMap = Utils.dbInstance.getStringValue("requests", fields, "id", Integer.toString(requestID));
								
								String uri = resultMap.get("uri");
								String useragent = resultMap.get("useragent");
								String referer = resultMap.get("referer");
								String authorization = resultMap.get("authorization");
								String cookies = resultMap.get("cookies");
								
								ReplayDialog replayInterface = new ReplayDialog(domain, uri, useragent, referer, cookies, authorization);
								replayInterface.setVisible(true);
							} catch (SQLException e) {
								e.printStackTrace();
							}
			        		
			                return null;
			            }
			        };
			        
			        loadRequestWorker.execute();
				}
			}
		});
		
		interfaceListComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				setCaptureButtonText();
			}
		});


		btnMonitorOnSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int selectedInterface = ((JComboBox<?>)getComponentByName("interfaceListComboBox")).getSelectedIndex();
				if(selectedInterface == -1)
					return;
					
				boolean bInterfaceIsCapturing = captureHandler.bCapturing.get(selectedInterface); // Make a copy because this value will be changing a lot...
							
				if(bInterfaceIsCapturing)
				{
					stopCapture(selectedInterface);
				}
				else
				{
					prepCapture(selectedInterface);
					
			    	SwingWorker<?, ?> captureWorker = new SwingWorker<Object, Object>() {            
			        	@Override
			            public Object doInBackground()
			        	{
							try {
								int selectedInterface = ((JComboBox<?>)getComponentByName("interfaceListComboBox")).getSelectedIndex();
								captureHandler.startCapture(selectedInterface, null);
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							return null;
			            }
			        };
			        
			        captureWorker.execute();
				}
			}
		});
		
		// Associate all components with the HashMap
		componentMap = createComponentMap(contentPane);
				
		// Get capture devices
		captureHandler.initializeDeviceList();
		
		// Name and license
		Utils.consoleMessage("\n\nCookie Cadger (v"+ Utils.version +", https://cookiecadger.com)\nCreated by Matthew Sullivan - mattslifebytes.com\nThis software is freely distributed under the terms of the FreeBSD license.\n");
		
		// Populate the ComboBox	
		for (int i = 0; i < captureHandler.deviceName.size(); i++)
		{
			String interfaceText;
			
			interfaceText = captureHandler.deviceName.get(i) + " [" + captureHandler.deviceDescription.get(i) + "]";
			interfacesListModel.addElement(interfaceText);
		}
		
		// Select the best available interface (for Mac/Linux systems)
		int itemToSelect = -1;
		boolean bFinished = false;
		
		String[] interfaceNames = { "mon", "wlan", "en", "eth" };
		for (String interfaceName : interfaceNames)
		{
			for (int i = 0; i < captureHandler.deviceName.size(); i++)
			{
				if(captureHandler.deviceName.get(i).contains(interfaceName))
				{
					itemToSelect = i;
					bFinished = true;
					break;
				}
			}
			
			if(bFinished)
				break;
		}
		
		interfaceListComboBox.setSelectedIndex(itemToSelect);
		
		if((Boolean) Utils.programSettings.get("bCheckForUpdates"))
		{
			// Check for software update
	    	SwingWorker<?, ?> updateWorker = new SwingWorker<Object, Object>() {            
	        	@Override            
	            public Object doInBackground()
	        	{
	        		try {
	        			JLabel lblSoftwareUpdateAvailable = ((JLabel)getComponentByName("lblSoftwareUpdateAvailable"));

	        			String macAddressHashString;
	        			try
	        			{
		        			InetAddress ip = InetAddress.getLocalHost(); // Get an active IP	        			
		        	        NetworkInterface network = NetworkInterface.getByInetAddress(ip); // and match to Mac Address
		        	        byte[] mac = network.getHardwareAddress();
		        	        
			        	    MessageDigest shaOfMacAddress = MessageDigest.getInstance("SHA-512");
			        	    byte[] macAddressHash = shaOfMacAddress.digest(mac);
	
		        	        StringBuffer macAddressHashStringBuilder = new StringBuffer();
		        	        for (int i = 0; i < macAddressHash.length; i++)
		        	        {
		        	        	macAddressHashStringBuilder.append(Integer.toString((macAddressHash[i] & 0xff) + 0x100, 16).substring(1));
		        	        }
		        	        macAddressHashString = macAddressHashStringBuilder.toString();
	        			}
	        			catch (Exception ex)
	        			{
	        				// Something weird happened, just reset the MAC hash to blank and move on
	        				macAddressHashString = "";
	        			}
	        			
						String releasedVersion = Utils.readUrl("https://www.cookiecadger.com/update/?update=" + Utils.version + "; " + System.getProperty("os.name") + "; " + System.getProperty("os.version") + "; " + System.getProperty("os.arch") + "; " + macAddressHashString, "Cookie Cadger, " + Utils.version, "text/html", null);
						
						if(releasedVersion.length() > 0 && releasedVersion.contains("Cookie Cadger")) // String has stuff in it? Display.
						{
							lblSoftwareUpdateAvailable.setText(releasedVersion);
							lblSoftwareUpdateAvailable.setForeground(Color.BLUE);
							lblSoftwareUpdateAvailable.addMouseListener(new MouseAdapter()
							{
								@Override
								public void mouseClicked(MouseEvent e) {
									try
									{
										Desktop.getDesktop().browse(new URI("http://www.cookiecadger.com/update/"));
									}
									catch (Exception ex) {
										// Do nothing
									}
								}
							});
						}
					}
	        		catch (Exception e)
	        		{
	        			e.printStackTrace();
						// Do nothing
					}
	        		
	                return null;
	            }
	        };
	        updateWorker.execute();
		}
        
        // All components loaded, start refresh timer if using remote DB
        if(!Utils.dbEngine.equals("sqlite"))
        {
        	try
         	{
        		// Bring in the initial GUI data from the DB
        		changeClientsList(false);
        		
		    	if((Integer) Utils.programSettings.get("bSessionDetection") == 1)
		    		changeSessionsList(false);
        	}
        	catch (Exception ex)
        	{
        		ex.printStackTrace();
        	}
			
			// Refresh every X seconds
	        Timer timer = new Timer( (1000 * (Integer)Utils.programSettings.get("databaseRefreshRate")) , new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
		        	try
		        	{
		        		// If a request is highlighted we need to remember it
		        		boolean bPreviousSelection = false;
		        		String previousSelection = null;
		        		
		        		if(!clientsList.isSelectionEmpty() && requestsList.getSelectedValue() != null)
		        		{
		        			bPreviousSelection = true;
		        			previousSelection = ((EnhancedJListItem)requestsList.getSelectedValue()).toString();
		        		}
		        		
		        		// Now refresh the clients list, don't blank it first
		        		changeClientsList(false);
		        		
						if(!clientsList.isSelectionEmpty())
						{
							String selectedClient = ((EnhancedJListItem)clientsList.getSelectedValue()).toString();
							changeDomainsList(selectedClient);
						}
		        		
		        		if((Integer) Utils.programSettings.get("bSessionDetection") == 1)
				    		changeSessionsList(false);
		        		
		        		// And give our previously highlighted requests back
	            		if(bPreviousSelection)
	            		{
	            			// If the newly generated list still contains the previously selected value, show it
	            			if (requestsListModel.contains(previousSelection))
	            			{
	            				int index = requestsListModel.indexOf(previousSelection);
	            				requestsList.setSelectedValue(requestsListModel.getElementAt(index), true);
	            			}
	            		}
		        	}
		        	catch (Exception ex)
		        	{
		        		ex.printStackTrace();
		        	}
				}
			});
	        timer.setRepeats(true);
	        timer.start();
        }
	}
	
	private HashMap<String, Component> createComponentMap(JPanel panel)
	{
		HashMap<String, Component> componentMap = new HashMap<String,Component>();
        Component[] components = panel.getComponents();
        for (int i=0; i < components.length; i++)
        {
        	if(components[i] instanceof JTabbedPane)
        	{
        		// Find the tabbed panel and iterate its components as well
        		for (Component cmp : ((JTabbedPane) components[i]).getComponents())
        		{
        			if(cmp instanceof JPanel)
        			{
        				for (Component x : ((JPanel) cmp).getComponents())
        				{
        		        	componentMap.put(x.getName(), x);
        				}
        			}
        		}
        	}
        	
       		componentMap.put(components[i].getName(), components[i]);
        }
        
        return componentMap;
	}

	public Component getComponentByName(String name) {
        if (componentMap.containsKey(name)) {
                return (Component) componentMap.get(name);
        }
        else return null;
	}
}