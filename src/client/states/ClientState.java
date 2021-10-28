package client.states;

import events.NewDataRead;
import events.NewPacketReceived;
import events.TimerRanOut;
import events.TimerTicked;

/**
 * Abstract class ClientState is a superclass to any state of Client FSM.
 * 
 * @author starnet © 2021
 *
 */
public abstract class ClientState {

	/**
	 * Initializes the state
	 */
	public abstract void enter();

	/**
	 * Performs any necessary clean up while leaving the state
	 */
	public abstract void leave();

	/**
	 * Processes new data read event
	 */
	public void handleEvent(NewDataRead event) {

	}

	/**
	 * Processes new packet received
	 */
	public void handleEvent(NewPacketReceived event) {

	}

	/**
	 * Processes a timer tick, generates a Timer Ran Out event
	 */
	public void handleEvent(TimerRanOut event) {

	}

	/**
	 * Processes a timer tick, generates a Timer Ticked event
	 */
	public void handleEvent(TimerTicked event) {

	}
}