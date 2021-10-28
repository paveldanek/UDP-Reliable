package main;

import server.Context;
import server.states.WaitForPk0;
import server.states.WaitForPk1;

/**
 * This program uses Finite State Machine (FSM) and unreliable UDP datagrams to
 * receive a binary file from source on the network and write it on disk. It's
 * to be paired with UDPRClient and run in Terminal (for Windows folks Command
 * Line) from bin folder of the project as follows: java main/UDPRServer <args>.
 * Start running UDPRServer first, then, in a new window of Terminal, run
 * UDPRClient the same way.
 * 
 * WARNING: UDPRServer WILL OVERWRITE THE DESTINATION FILE WITHOUT WARNING, IF
 * IT ALREADY EXISTS!
 * 
 * UDPR stands for UDP Reliable.
 * 
 * DISCLAIMER: The program has not been tested on a Windows computer.
 * 
 * 
 * @author starnet © 2021
 *
 */
public class UDPRServer {
	public static void main(String[] args) {
		if (Context.instance().startUp(args)) {
			while (true) {
				Context.instance().changeState(WaitForPk1.instance());
				Context.instance().changeState(WaitForPk0.instance());
			}
		}
	}
}
