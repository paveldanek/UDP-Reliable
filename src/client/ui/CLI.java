package client.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import packets.DataPacket;

/**
 * Command Line Interface. Uses command line to retrieve settings from execution
 * command arguments and outputs on screen in plain text.
 * 
 * @author starnet © 2021
 *
 */
public class CLI implements UI {
	// arrays of strings with values for static text rendering
	private String[] sentCodes = { "SENDing", "ReSend." };
	private String[] receiveCodes = { "AckRcvd", "DuplAck", "ErrAck." };
	private String[] errorStatuses = { "SENT", "DROP", "ERRR" };
	private String[] timeOuts = { "TimeOut" };

	/**
	 * Grabs args from command line and returns them them to the program.
	 */
	@Override
	public InitSettings getSettings(String[] args) throws UnknownHostException {
		InitSettings settings = new InitSettings();
		settings.setPacketSize(DataPacket.DEFAULT_DATA_LENGTH);
		// if no agrs (including the mandatory input file name) were specified
		if (args.length < 1 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("-h")) {
			println("Arguments expected.\nFirst (mandatory) argument: File (incl. path) to be sent. \n"
					+ "Following optional arguments:\n"
					+ "-s <number from 1 to 32000>: size of packets in bytes (default=" + settings.getPacketSize()
					+ "),\n-t <number larger than 0>: timeout for an ack in millisecs (default=" + settings.getTimeout()
					+ "),\n-d <decimal number from 0.0 to 1.0>: percentage of packets to be corrupt (default="
					+ settings.getDataCorruptPercentage()
					+ "),\n-a <string QQQ.XXX.YYY.ZZZ>: ip address of the Server (default=" + settings.getIpAddress()
					+ "),\n-p <number from 1 to 65535>: port number on which the Server is listening (default="
					+ settings.getPort() + ").");
			return null;
		}
		try {
			settings.setFileName(args[0]);
			int helpInt;
			double helpDouble;
			// for loop converts args into values and throws an exception in case of an
			// error
			for (int i = 1; i < args.length; i++) {
				switch (args[i]) {
				case "-s":
					helpInt = Integer.parseInt(args[++i]);
					if (helpInt < 1 || helpInt > 32000)
						throw new Exception();
					else
						settings.setPacketSize(helpInt);
					break;
				case "-t":
					helpInt = Integer.parseInt(args[++i]);
					if (helpInt < 1)
						throw new Exception();
					else
						settings.setTimeout(helpInt);
					break;
				case "-d":
					helpDouble = Double.parseDouble(args[++i]);
					if (helpDouble < 0.0 || helpDouble > 1.0)
						throw new Exception();
					else
						settings.setDataCorruptPercentage(helpDouble);
					break;
				case "-a":
					try {
						settings.setIpAddress(InetAddress.getByName(args[++i]));
					} catch (UnknownHostException e) {
						throw new Exception();
					}
					break;
				case "-p":
					helpInt = Integer.parseInt(args[++i]);
					if (helpInt < 1 || helpInt > 65535)
						throw new Exception();
					else
						settings.setPort(helpInt);
					break;
				}
			}
		} catch (Exception e) {
			println("There was an error in the arguments presented.");
			return null;
		}
		return settings;
	}

	// printing methods, called by corresponding Client Context methods

	@Override
	public void print(String string) {
		System.out.print(string);
	}

	@Override
	public void println(String string) {
		System.out.println(string);
	}

	@Override
	public void printSend(int sentCode, int seqNo, double sentTime, int errorStatus, long startOffset, long endOffset) {
		System.out.println("[" + sentCodes[sentCode] + "] " + seqNo + " " + String.format("%.3f", sentTime) + "ms ["
				+ errorStatuses[errorStatus] + "] (" + startOffset + ":" + endOffset + ")");
	}

	@Override
	public void printAck(int receiveCode, int ackNo) {
		System.out.println("[" + receiveCodes[receiveCode] + "] " + ackNo);
	}

	@Override
	public void printTimeout(int seqNo) {
		System.out.println("[" + timeOuts[0] + "] " + seqNo);
	}

	@Override
	public void printBye() {
		System.out.println("The job is DONE.\nThank you.\nBYE!");
	}

}
