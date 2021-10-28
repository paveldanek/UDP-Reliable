package tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import events.TimerRanOut;
import events.TimerTicked;
import multiTimer.Notifiable;
import multiTimer.Timer;

/**
 * Stand-alone TEST Program. Please ignore.
 * 
 * This program is a modified version of a UDP Server listening for datagrams
 * sent from a UDP Client. It utilizes an advanced form of timer (which can be
 * modified as multi-timer). It serves the purpose of interruption of blocking,
 * typical for DatagramSocket receive() method, so that program will support
 * other actions, while listening on a port.
 * 
 * @author starnet © 2021
 *
 */
public class ServerWTimerTest implements Notifiable {
	static boolean eventTrigger = false;

	public void handleEvent(TimerTicked event) {
		// This event is handled every 1/10 of a second.
		if (event.getTimeLeft() % 100 == 0) {
			System.out.println("Timer #" + event.getTimerID() + " = " + (event.getTimeLeft() / 100) + "/10 sec left.");
			// eventTrigger is a static flag that ensures that the event triggered will not
			// only execute this method, but also will break in the normal block of program
			// and cause a fork in execution.
			eventTrigger = true;
		}
	};

	public void handleEvent(TimerRanOut event) {
		// This event is handled immediately when it happens.
		System.out.println("TIMER #" + event.getTimerID() + " TIMED OUT.");
	};

	public static void main(String[] args) throws IOException, SocketTimeoutException {
		final int PORT = 1100;
		final int BUFFER_SIZE = 65000;
		// timeout for DatagramSocket to stop blocking after TIMEOUT milliseconds, for
		// other actions to possibly take place
		final int TIMEOUT = 1;
		DatagramSocket ds = new DatagramSocket(PORT);
		ds.setSoTimeout(TIMEOUT);
		boolean tOut;
		// this array stores the incoming file in memory
		ArrayList<byte[]> buf = new ArrayList<byte[]>();
		byte[] read = new byte[BUFFER_SIZE]; // stores a single incoming datagram
		// SWITCH BACK TO: if (args.length < 1) IF YOU WANT TO RUN THIS FROM COMMAND
		// LINE.
		if (args.length < 0) {
			System.out.println("Run this server program with the name of the NEW output file as an argument.");
		} else {
			// SWITCH THE FOLLOWING LINE TO: String outputFileName = args[0]; IF INTENDED TO
			// RUN FROM COMMAND LINE.
			// For testing purposes, the outputFileName can be set to any path/file that
			// suits the testing.
			String outputFileName = "/Users/starnet/picOUT.jpg";
			File outputFile = new File(outputFileName);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			int packetNumber = 0;
			int offset = 0;
			DatagramPacket dpReceive = null;
			int dataLength = 0;
			// Starting a timer which demonstrates that anything can be done during the loop
			// when waiting for incoming datagrams.
			Timer timer = new Timer(new ServerWTimerTest(), 1, 10000);
			System.out.println(
					"--- Timer " + timer.getTimerID() + " set to " + (timer.getTimeValue() / 1000) + " seconds.");
			do {
				tOut = false;
				dpReceive = new DatagramPacket(read, BUFFER_SIZE);
				try {
					// This receive() method blocks only for TIMEOUT milliseconds, after which time
					// it throws a SocketTimeoutException, during which a flag can be set to permit
					// exiting the loop.
					ds.receive(dpReceive);
					dataLength = dpReceive.getLength();
					buf.add(dpReceive.getData());
					packetNumber++;
					System.out.println("Packet #" + packetNumber + ": Bytes " + offset + " to "
							+ (offset + dataLength - 1) + " received.");
					offset += dataLength;
					read = new byte[BUFFER_SIZE];
				} catch (SocketTimeoutException e) {
					if (eventTrigger) {
						// If the eventTrigger flag was raised in any of the handleEvent methods, the
						// following will be executed.
						System.out.println("BREAK!");
						eventTrigger = false;
					}
					tOut = true;
				}
			} while (tOut || dataLength == BUFFER_SIZE);
			ds.close();
			int bufLength = buf.size();
			// Writing all but last datagram, which is shorter.
			for (int i = 0; i < bufLength - 1; i++) {
				outputStream.write(buf.get(i), 0, buf.get(i).length);
			}
			// Writing the last datagram.
			outputStream.write(buf.get(bufLength - 1), 0, dataLength);
			outputStream.close();
			// Stopping timer manually, as a fail safe. Normally, the timer stops when it
			// times out.
			timer.stop();
		}
		// In order to stop all running threads (including the clock) and ending the
		// execution of the program, we use System.exit.
		System.exit(0);
	}

}
