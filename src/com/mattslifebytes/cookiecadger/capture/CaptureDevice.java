package com.mattslifebytes.cookiecadger.capture;

public class CaptureDevice {
	private boolean isCapturing;
	private String deviceName;
	private String deviceDescription;
	public CaptureDevice(String deviceName, String deviceDescription) {
		this.deviceName = deviceName;
		this.deviceDescription = deviceDescription;
	}
	public boolean isCapturing() {
		return this.isCapturing;
	}
	public void setCapturing(boolean isCapturing) {
		this.isCapturing = isCapturing;
	}
	public String getDeviceName() {
		return this.deviceName;
	}
	public void setDeviceName(String name) {
		this.deviceName = name;
	}
	public String getDeviceDescription() {
		return this.deviceDescription;
	}
	public void setDeviceDescription(String description) {
		this.deviceDescription = description;
	}
}