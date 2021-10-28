package client.ui;

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

	public void printSend(int sentCode, int seqNo, double sentTime, int errorStatus, long startOffset, long endOffset);

	public void printAck(int receiveCode, int ackNo);

	public void printTimeout(int seqNo);

	public void printBye();

}
