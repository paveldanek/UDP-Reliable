package server.ui;

import java.net.UnknownHostException;

/**
 * A UI interface any UI used has to comply with.
 * 
 * @author starnet © 2021
 *
 */
public interface UI {

	public InitSettings getSettings(String args[]) throws UnknownHostException;

	public void print(String string);

	public void println(String string);

	public void printReceive(int receiveCode, int seqNo, double receiveTime, int condition);

	public void printAck(int sentCode, int ackNo, int errorStatus);

	public void printBye();

}
