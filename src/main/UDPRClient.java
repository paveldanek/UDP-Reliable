package main;

import client.Context;
import client.states.Packet0;
import client.states.Packet1;

/**
 * This program uses Finite State Machine (FSM) and unreliable UDP datagrams to
 * read a binary file from disk and send it through the network to destination.
 * It's to be paired with UDPRServer and run in Terminal (for Windows folks
 * Command Line) from bin folder of the project as follows: java main/UDPRClient
 * <args>. Start running UDPRServer first, then, in a new window of Terminal,
 * run UDPRClient the same way.
 * 
 * UDPR stands for UDP Reliable.
 * 
 * DISCLAIMER: The program has not been tested on a Windows computer.
 * 
 * 
 * @author starnet © 2021
 *
 */
public class UDPRClient {
	public static void main(String[] args) {
		if (Context.instance().startUp(args)) {
			while (true) {
				Context.instance().changeState(Packet1.instance());
				Context.instance().changeState(Packet0.instance());
			}
		}
	}
}
