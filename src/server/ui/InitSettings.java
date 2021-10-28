package server.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Specifies and stores all necessary initial settings values for the client to
 * proceed; includes defaults.
 * 
 * @author starnet © 2021
 *
 */
public class InitSettings {

	private String fileName;
	private double dataCorruptPercentage;
	private InetAddress ipAddress;
	private int port;

	/**
	 * Constructor sets all variables to their default values, except for packetSize
	 * to avoid class dependency.
	 * 
	 * @throws UnknownHostException
	 */
	public InitSettings() throws UnknownHostException {
		fileName = "";
		dataCorruptPercentage = 0.25;
		try {
			ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			ipAddress = InetAddress.getByName("127.0.0.1");
		}
		port = 1200;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public double getDataCorruptPercentage() {
		return dataCorruptPercentage;
	}

	public void setDataCorruptPercentage(double dataCorruptPercent) {
		this.dataCorruptPercentage = dataCorruptPercent;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
