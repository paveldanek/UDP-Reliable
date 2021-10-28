package server.states;

import events.NewAckToSend;
import events.NewPacketReceived;

/**
 * Abstract class ServerState is a superclass to any state of Server FSM.
 * 
 * @author starnet © 2021
 *
 */
public abstract class ServerState {

	/**
	 * Initializes the state
	 */
	public abstract void enter();

	/**
	 * Performs any necessary clean up while leaving the state
	 */
	public abstract void leave();

	/**
	 * Processes new packet received
	 */
	public void handleEvent(NewPacketReceived event) {

	}

	/**
	 * Processes new acknowledgement to be sent event
	 */
	public void handleEvent(NewAckToSend event) {

	}
}