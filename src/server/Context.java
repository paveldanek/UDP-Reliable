package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;

import events.NewAckToSend;
import events.NewPacketReceived;
import packets.DataPacket;
import packets.Packet;
import server.states.ServerState;
import server.states.WaitForPk1;
import server.ui.CLI;
import server.ui.InitSettings;
import server.ui.UI;

/**
 * The server context is an observer for the server (receiver) and stores
 * information and houses tools for the server states to make a use of.
 * 
 * @author starnet © 2021
 *
 */
public class Context {
	private static Context instance;
	private ServerState currentState;
	// using command line interface
	static UI ui = new CLI();
	// stores settings derived from command line args
	private static InitSettings settings;
	// stores the newest ACK packet to be sent out
	private Packet currentPacketOut;
	// stores the newest datagram received
	private DataPacket currentPacketIn;
	// stores TRUE if the currentPacketIn has not been acknowledged yet
	private boolean inPacketNew;
	// A period for which the DatagramSocket will block while listening for incoming
	// traffic on port. After BLOCKING_PERIOD milliseconds, it will continue
	// executing code.
	private final int BLOCKING_PERIOD = 1;
	private DatagramSocket datagramSocket;
	private FileOutputStream outputStream;
	private int expectedSeqNo = 0;
	private boolean dataWritten = false;
	private double receiveTimeMillis = 0.0;

	// constants for static text rendering
	public final int AK_SEND = 0;
	public final int AK_RESEND = 1;
	public final int PK_RCVD = 0;
	public final int PK_DUPL = 1;
	public final int SENT = 0;
	public final int DROP = 1;
	public final int ERROR = 2;
	public final int CON_RCVD = 0;
	public final int CON_OOS = 1;
	public final int CON_CORR = 2;

	/**
	 * Singleton pattern.
	 */
	private Context() {
		instance = this;
		try {
			settings = new InitSettings();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static Context instance() {
		if (instance == null) {
			instance = new Context();
		}
		return instance;
	}

	/**
	 * Extracts custom settings from user input. May or may not use the command line
	 * arguments depending on whether CLI or GUI is used.
	 * 
	 * @param args - command line arguments passed to the method
	 * @return TRUE if setup was successful, FALSE if there was an error or missing
	 *         information
	 */
	public boolean startUp(String[] args) {
		try {
			settings = ui.getSettings(args);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return false;
		}
		if (settings != null) {
			try {
				datagramSocket = new DatagramSocket(settings.getPort());
				datagramSocket.setSoTimeout(BLOCKING_PERIOD);
			} catch (SocketException e) {
				e.printStackTrace();
				return false;
			}
			File outputFile = new File(settings.getFileName());
			try {
				outputStream = new FileOutputStream(outputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			}
			currentState = WaitForPk1.instance();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Closes all open connections and streams and brings program to halt
	 */
	public void quit() {
		datagramSocket.close();
		try {
			outputStream.close();
		} catch (IOException e) {
		}
		// In order to stop all running threads and ending the execution of the program,
		// we use System.exit().
		System.exit(0);
	}

	/**
	 * Takes care of transitions between states.
	 * 
	 * @param nextState - indicates the state switching into.
	 */
	public void changeState(ServerState nextState) {
		currentState.leave();
		currentState = nextState;
		currentState.enter();
	}

	public void setCurrentPacketOut(Packet newPacket) {
		currentPacketOut = newPacket;
	}

	public Packet getCurrentPacketOut() {
		return currentPacketOut;
	}

	public boolean isInPacketNew() {
		return inPacketNew;
	}

	public void setCurrentPacketIn(DataPacket newPacket) {
		currentPacketIn = newPacket;
	}

	public DataPacket getCurrentPacketIn() {
		return currentPacketIn;
	}

	public void toggleExpectedSeqNo() {
		if (expectedSeqNo == 1) {
			expectedSeqNo = 0;
		} else {
			expectedSeqNo = 1;
		}
	}

	public int getExpectedSeqNo() {
		return expectedSeqNo;
	}

	public double getReceiveTime() {
		return receiveTimeMillis;
	}

	public void resetReceiveTime() {
		receiveTimeMillis = 0.0;
	}

	public int getPort() {
		return settings.getPort();
	}

	/**
	 * Writes the currentPacketIn into the output binary file.
	 */
	public void writeData() {
		// creates a new array of bytes of the exact length of the payload and copies
		// the payload there
		byte[] dataBuffer = new byte[getCurrentPacketIn().getLen() - 12];
		System.arraycopy(getCurrentPacketIn().getData(), 0, dataBuffer, 0, dataBuffer.length);
		try {
			outputStream.write(dataBuffer, 0, dataBuffer.length);
		} catch (IOException e) {
			e.printStackTrace();
			quit();
		}
		dataWritten = true;
	}

	/**
	 * This function helps simulating the unreliability of Internet by corrupting a
	 * ACK packet that's being sent out. The ratio corrupted/all packets is
	 * retrieved from a variable holding custom settings.
	 * 
	 * @param newPacket - the uncorrupted packet
	 * @return the corrupted/uncorrupted packet in compliance with corruption ratio
	 *         OR null, if the packet is destined to be lost (which is half of the
	 *         corrupted packets)
	 */
	public Packet corrupter(Packet newPacket) {
		Packet output = new Packet(newPacket.getAckno());
		output.setCksum(newPacket.getCksum());
		output.setLen(newPacket.getLen());
		Random rand = new Random();
		double rn = rand.nextDouble();
		if (rn <= settings.getDataCorruptPercentage()) {
			output.setCksum((short) 1);
			rn = rand.nextDouble();
			if (rn <= 0.50) {
				return null;
			} else {
				return output;
			}
		}
		return output;
	}

	/**
	 * Sends the new ACK packet out on the port the client (sender) will be
	 * listening on.
	 * 
	 * @return confirmation code (SENT/ERROR/DROPPED)
	 */
	public int sendAck() {
		int outcome;
		setCurrentPacketOut(new Packet(getExpectedSeqNo()));
		Packet realPacketOut = corrupter(getCurrentPacketOut());
		if (realPacketOut != null) {
			byte[] data = new byte[8];
			data = Packet.buildStream(realPacketOut);
			try {
				datagramSocket.send(new DatagramPacket(data, 0, data.length, settings.getIpAddress(),
						client.Context.instance().getPort()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (realPacketOut.getCksum() == 0) {
				outcome = SENT;
			} else {
				outcome = ERROR;
			}
		} else {
			outcome = DROP;
		}
		return outcome;
	}

	/**
	 * Listens for incoming data packets. Continues listening on a loop until
	 * dataWritten is flipped to TRUE by writeData() method.
	 */
	public void listen() {
		long timeBefore, timeAfter;
		resetReceiveTime();
		DatagramPacket dpReceive = null;
		byte[] data = new byte[DataPacket.MAX_DATA_LENGTH]; // Data packets are at max. 32000 bytes long
		do {
			dpReceive = new DatagramPacket(data, data.length);
			try {
				// This following receive() method blocks only for BLOCKING_PERIOD milliseconds.
				// After that it throws a SocketTimeoutException, during which a flag can be
				// set to permit exiting the loop or additional code could be executed.
				timeBefore = System.nanoTime();
				datagramSocket.receive(dpReceive);
				timeAfter = System.nanoTime();
				receiveTimeMillis = (double) (timeAfter - timeBefore) / 1000000;
				data = dpReceive.getData();
				setCurrentPacketIn(DataPacket.buildPacket(data));
				// if received DataPacket is out of sequence (duplicate of previous) then
				// inPacketNew is set to false
				inPacketNew = getCurrentPacketIn().getSeqno() == getExpectedSeqNo();
				handleEvent(NewPacketReceived.instance());
			} catch (SocketTimeoutException e) {
				// this is a spot to insert any code needing to be executed when listening is
				// interrupted by expiration of the BLOCKING_PERIOD (in ms). The author of this
				// code left it blank.
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} while (!dataWritten);
		dataWritten = false;
	}

	// Event handlers. The methods give way to the individual current state
	// equivalents.

	public void handleEvent(NewPacketReceived event) {
		currentState.handleEvent(event);
	}

	public void handleEvent(NewAckToSend event) {
		currentState.handleEvent(event);
	}

	// Display methods. They call UI methods. The program uses CLI, but it's
	// GUI-ready, should GUI be implemented.

	/**
	 * Displays information about the data packet received.
	 */
	public void displayReceive(int receiveCode, int seqNo, double receiveTime, int condition) {
		ui.printReceive(receiveCode, seqNo, receiveTime, condition);
	}

	/**
	 * Displays ACKnoledgement sent out information.
	 */
	public void displayAck(int sentCode, int ackNo, int errorStatus) {
		ui.printAck(sentCode, ackNo, errorStatus);
	}

	/**
	 * Displays a good-bye.
	 */
	public void displayBye() {
		ui.printBye();
	}
}
