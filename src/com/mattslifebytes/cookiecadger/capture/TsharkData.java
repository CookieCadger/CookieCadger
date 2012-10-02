package com.mattslifebytes.cookiecadger.capture;

public class TsharkData {

	
	private String macAddressWired;
	private String macAddressWireless;
	private String macAddress;
	private String ipv4Address;
	private String ipv6Address;
	private String tcpDestination;
	private String udpSource;
	private String netbiosCommand;
	private String netbiosName;
	private String mdnsName;
	private String requestHost;
	private String requestUri;
	private String accept;
	private String acceptEncoding;
	private String userAgent;
	private String refererUri;
	private String cookieData;
	private String authorization;
	private String authBasic;

	public TsharkData(String[] values) {
		macAddressWired = values[0];
		macAddressWireless = values[1];
		macAddress = "Unknown";
		if(!macAddressWired.isEmpty())
			macAddress = macAddressWired;
		else if(!macAddressWireless.isEmpty())
			macAddress = macAddressWireless;
		ipv4Address = values[2];
		ipv6Address = values[3];
		tcpDestination = values[5];
		udpSource = values[6];
		netbiosCommand = values[8];
		netbiosName = values[9];
		mdnsName = values[10];
		requestHost = values[11];
		requestUri = values[12];
		accept = values[13];
		acceptEncoding = values[14];
		userAgent = values[15];
		refererUri = values[16];
		cookieData = values[17];
		authorization = values[18];
		authBasic = values[19];
	}

	public String getMacAddressWired() {
		return macAddressWired;
	}

	public void setMacAddressWired(String macAddressWired) {
		this.macAddressWired = macAddressWired;
	}

	public String getMacAddressWireless() {
		return macAddressWireless;
	}

	public void setMacAddressWireless(String macAddressWireless) {
		this.macAddressWireless = macAddressWireless;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getIpv4Address() {
		return ipv4Address;
	}

	public void setIpv4Address(String ipv4Address) {
		this.ipv4Address = ipv4Address;
	}

	public String getIpv6Address() {
		return ipv6Address;
	}

	public void setIpv6Address(String ipv6Address) {
		this.ipv6Address = ipv6Address;
	}

	public String getTcpDestination() {
		return tcpDestination;
	}

	public void setTcpDestination(String tcpDestination) {
		this.tcpDestination = tcpDestination;
	}

	public String getUdpSource() {
		return udpSource;
	}

	public void setUdpSource(String udpSource) {
		this.udpSource = udpSource;
	}

	public String getNetbiosCommand() {
		return netbiosCommand;
	}

	public void setNetbiosCommand(String netbiosCommand) {
		this.netbiosCommand = netbiosCommand;
	}

	public String getNetbiosName() {
		return netbiosName;
	}

	public void setNetbiosName(String netbiosName) {
		this.netbiosName = netbiosName;
	}

	public String getMdnsName() {
		return mdnsName;
	}

	public void setMdnsName(String mdnsName) {
		this.mdnsName = mdnsName;
	}

	public String getRequestHost() {
		return requestHost;
	}

	public void setRequestHost(String requestHost) {
		this.requestHost = requestHost;
	}

	public String getRequestUri() {
		return requestUri;
	}

	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}

	public String getAcceptEncoding() {
		return acceptEncoding;
	}

	public void setAcceptEncoding(String acceptEncoding) {
		this.acceptEncoding = acceptEncoding;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getRefererUri() {
		return refererUri;
	}

	public void setRefererUri(String refererUri) {
		this.refererUri = refererUri;
	}

	public String getCookieData() {
		return cookieData;
	}

	public void setCookieData(String cookieData) {
		this.cookieData = cookieData;
	}

	public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public String getAuthBasic() {
		return authBasic;
	}

	public void setAuthBasic(String authBasic) {
		this.authBasic = authBasic;
	}
}
