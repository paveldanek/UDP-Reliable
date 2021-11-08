package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;

import client.states.ClientState;
import client.states.Packet0;
import client.ui.CLI;
import client.ui.InitSettings;
import client.ui.UI;
import events.NewDataRead;
import events.NewPacketReceived;
import events.TimerRanOut;
import events.TimerTicked;
import multiTimer.Notifiable;
import packets.DataPacket;
import packets.Packet;

/**
 * The client context is an observer for the client (sender) and stores
 * information and houses tools for the client states to make a use of.
 * 
 * @author starnet © 2021
 *
 */
public class Context implements Notifiable {
	private static Context instance;
	private ClientState currentState;
	// using command line interface
	static UI ui = new CLI();
	// stores settings derived from command line args
	private static InitSettings settings;
	// stores the newest ACK packet received
	private Packet currentPacketIn;
	// stores the newest datagram to be sent out
	private DataPacket currentPacketOut;
	// stores TRUE if the currentPacketOut has not been sent out yet
	private boolean outPacketNew = false;
	// A period for which the DatagramSocket will block while listening for incoming
	// traffic on port. After BLOCKING_PERIOD milliseconds, it will continue
	// executing code.
	private final int BLOCKING_PERIOD = 1;
	private DatagramSocket datagramSocket;
	private FileInputStream inputStream;
	private int currentSeqNo = 0;
	private long offset = 0;
	private boolean readyForNextState = false;
	private double sendTimeMillis = 0.0;

	// constants for static text rendering
	public final int PK_SEND = 0;
	public final int PK_RESEND = 1;
	public final int SENT = 0;
	public final int DROP = 1;
	public final int ERROR = 2;
	public final int AK_REC = 0;
	public final int AK_DUPL = 1;
	public final int AK_ERR = 2;
	public final int TIMEOUT = 0;

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
			} catch (SocketException e1) {
				e1.printStackTrace();
				return false;
			}
			File inputFile = new File(settings.getFileName());
			try {
				inputStream = new FileInputStream(inputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			}
			currentState = Packet0.instance();
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
			inputStream.close();
		} catch (IOException e) {
		}
		// In order to stop all running threads (including the clock) and ending the
		// execution of the program, we use System.exit().
		System.exit(0);
	}

	/**
	 * Takes care of transitions between states.
	 * 
	 * @param nextState - indicates the state switching into.
	 */
	public void changeState(ClientState nextState) {
		currentState.leave();
		currentState = nextState;
		currentState.enter();
	}

	public void setCurrentPacketOut(DataPacket newPacket) {
		currentPacketOut = newPacket;
	}

	public DataPacket getCurrentPacketOut() {
		return currentPacketOut;
	}

	public boolean isOutPacketNew() {
		return outPacketNew;
	}

	public void setCurrentPacketIn(Packet newPacket) {
		currentPacketIn = newPacket;
	}

	public Packet getCurrentPacketIn() {
		return currentPacketIn;
	}

	public void toggleCurrentSeqNo() {
		if (currentSeqNo == 1) {
			currentSeqNo = 0;
		} else {
			currentSeqNo = 1;
		}
	}

	public int getCurrentSeqNo() {
		return currentSeqNo;
	}

	public int getTimeoutValue() {
		return settings.getTimeout();
	}

	public double getSendTime() {
		return sendTimeMillis;
	}

	public void resetSendTime() {
		sendTimeMillis = 0.0;
	}

	public long getOffset() {
		return offset;
	}

	public void incrementOffset(int increment) {
		offset += increment;
	}

	public void setReadyForNextState(boolean ready) {
		readyForNextState = ready;
	}

	public int getPort() {
		return settings.getPort();
	}

	/**
	 * Reads a single batch of data (the size of default or preset packet size) from
	 * a binary file, creates a new datagram and places it in the currentPacketOut
	 * variable.
	 */
	public void readData() {
		int bytesRead = 0;
		byte[] dataBuffer = new byte[settings.getPacketSize()];
		try {
			bytesRead = inputStream.read(dataBuffer);
		} catch (Exception e) {
			e.printStackTrace();
			quit();
		}
		if (bytesRead == -1) {
			bytesRead = 0;
		}
		byte[] trueData = new byte[bytesRead];
		// this trims the array size to the exact length used up
		System.arraycopy(dataBuffer, 0, trueData, 0, bytesRead);
		setCurrentPacketOut(new DataPacket(currentSeqNo, trueData));
		outPacketNew = true;
		handleEvent(NewDataRead.instance());
	}

	/**
	 * This function helps simulating the unreliability of Internet by corrupting a
	 * datagram that's being sent out. The ratio corrupted/all packets is retrieved
	 * from a variable holding custom settings.
	 * 
	 * @param newPacket - the uncorrupted packet
	 * @return the corrupted/uncorrupted packet in compliance with corruption ratio
	 *         OR null, if the packet is destined to be lost (which is half of the
	 *         corrupted packets)
	 */
	public DataPacket corrupter(DataPacket newPacket) {
		DataPacket output = new DataPacket(newPacket.getSeqno(), new byte[newPacket.getLen() - 12]);
		byte[] payload = new byte[newPacket.getLen() - 12];
		output.setCksum(newPacket.getCksum());
		output.setLen(newPacket.getLen());
		output.setAckno(newPacket.getAckno());
		System.arraycopy(newPacket.getData(), 0, payload, 0, payload.length);
		output.setData(payload);
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
	 * Sends the new datagram out on the port the server (receiver) will be
	 * listening on.
	 * 
	 * @return confirmation code (SENT/ERROR/DROPPED)
	 */
	public int sendPacket() {
		int outcome;
		long timeBefore, timeAfter;
		DataPacket realPacketOut;
		// if it's the last packet (length of data = 0), then do not corrupt,
		// otherwise run the corrupter
		if (getCurrentPacketOut().getLen() - 12 == 0) {
			realPacketOut = getCurrentPacketOut();
		} else {
			realPacketOut = corrupter(getCurrentPacketOut());
		}
		resetSendTime();
		if (realPacketOut != null) {
			byte[] data = new byte[realPacketOut.getLen()];
			data = DataPacket.buildStream(realPacketOut);
			timeBefore = System.nanoTime();
			try {
				datagramSocket.send(new DatagramPacket(data, 0, data.length, settings.getIpAddress(),
						server.Context.instance().getPort()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			timeAfter = System.nanoTime();
			sendTimeMillis = (double) (timeAfter - timeBefore) / 1000000;
			if (realPacketOut.getCksum() == 0) {
				outcome = SENT;
			} else {
				outcome = ERROR;
			}
		} else {
			outcome = DROP;
		}
		outPacketNew = false;
		return outcome;
	}

	/**
	 * Listens for incoming ACK packets. Continues listening on a loop until the
	 * readyForNextState is flipped to TRUE by current state.
	 */
	public void listen() {
		DatagramPacket dpReceive = null;
		byte[] data = new byte[8]; // ACK packets are only 8 bytes long
		do {
			dpReceive = new DatagramPacket(data, 8);
			try {
				// This following receive() method blocks only for BLOCKING_PERIOD milliseconds.
				// After that it throws a SocketTimeoutException, during which a flag can be
				// set to permit exiting the loop or additional code could be executed.
				datagramSocket.receive(dpReceive);
				data = dpReceive.getData();
				setCurrentPacketIn(Packet.buildPacket(data));
				handleEvent(NewPacketReceived.instance());
			} catch (SocketTimeoutException e) {
				// this is a spot to insert any code needing to be executed when listening is
				// interrupted by expiration of the BLOCKING_PERIOD (in ms). The author of this
				// code left it blank.
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} while (!readyForNextState);
		setReadyForNextState(false);
	}

	// Event handlers. Most of them set to give way to the individual current state
	// equivalents.

	public void handleEvent(TimerTicked event) {
		// an individual tick (every millisecond) is ignored
	}

	public void handleEvent(TimerRanOut event) {
		currentState.handleEvent(event);
	}

	public void handleEvent(NewDataRead event) {
		currentState.handleEvent(event);
	}

	public void handleEvent(NewPacketReceived event) {
		currentState.handleEvent(event);
	}

	// Display methods. They call UI methods. The program uses CLI, but it's
	// GUI-ready, should GUI be implemented.

	/**
	 * Displays information about the data packet sent out.
	 */
	public void displaySend(int sentCode, int seqNo, double sentTime, int errorStatus, long startOffset,
			long endOffset) {
		ui.printSend(sentCode, seqNo, sentTime, errorStatus, startOffset, endOffset);
	}

	/**
	 * Displays ACKnoledgement received information.
	 */
	public void displayAck(int receiveCode, int ackNo) {
		ui.printAck(receiveCode, ackNo);
	}

	/**
	 * Displays timeout triggered info.
	 */
	public void displayTimeout(int seqNo) {
		ui.printTimeout(seqNo);
	}

	/**
	 * Displays a good-bye.
	 */
	public void displayBye() {
		ui.printBye();
	}

}
