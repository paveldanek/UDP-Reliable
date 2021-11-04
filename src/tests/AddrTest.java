package tests;

import java.net.UnknownHostException;

import client.ui.CLI;
import client.ui.InitSettings;

/**
 * TEST of command line args read.
 * 
 * Run from bin folder as follows: java tests/AddrTest [arguments]
 * 
 * @author starnet © 2021
 *
 */
public class AddrTest {

	static CLI cli = new CLI();

	public static void main(String[] args) throws UnknownHostException {
		InitSettings settings = cli.getSettings(args);
		if (settings != null) {
			cli.println("File=" + settings.getFileName() + "\nSize=" + settings.getPacketSize() + "\nTimeout="
					+ settings.getTimeout() + "\nCorrupt %=" + settings.getDataCorruptPercentage() + "\nIP Address="
					+ settings.getIpAddress() + "\nPort=" + settings.getPort());
		}
	}

}
