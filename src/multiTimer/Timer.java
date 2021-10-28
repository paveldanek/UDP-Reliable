package multiTimer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import events.TimerRanOut;
import events.TimerTicked;

/**
 *
 * @author Brahma Dathan and Sarnath Ramnath
 * @Copyright (c) 2010

 * Redistribution and use with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - the use is for academic purpose only
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Neither the name of Brahma Dathan or Sarnath Ramnath
 *     may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * The authors do not make any claims regarding the correctness of the code in this module
 * and are not responsible for any loss or damage resulting from its use.
 */

// modified by starnet © 2021 to permit multiple timers

/**
 * The timer allows a certain time period to be set when created. It sends
 * signals back to its creator every millisecond and a timer runs out message
 * when the time period has elapsed.
 *
 * @author Brahma Dathan
 *
 */
public class Timer implements PropertyChangeListener {
	private int timeValue;
	private Notifiable client;
	private int timerID;

	/**
	 * Sets up the timer for a certain client with an initial time value
	 *
	 * @param client    the client, a Notifiable object
	 * @param id        uniquely identifies this timer
	 * @param timeValue the initial time value (in ms) after which the timer runs
	 *                  out of time.
	 */
	public Timer(Notifiable client, int id, int timeValue) {
		this.client = client;
		this.timerID = id;
		this.timeValue = timeValue;
		Clock.instance().addPropertyChangeListener(this);
	}

	/**
	 * Stops the timer by deleting itself from the list of observers
	 */
	public void stop() {
		Clock.instance().removePropertyChangeListener(this);
	}

	/**
	 * Returns the time value left
	 *
	 * @return the time value left in the timer
	 */
	public int getTimeValue() {
		return timeValue;
	}

	/**
	 * Returns this timer's unique identifier.
	 * 
	 * @return timer ID.
	 */
	public int getTimerID() {
		return timerID;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (--timeValue <= 0) {
			client.handleEvent(new TimerRanOut(timerID));
			Clock.instance().removePropertyChangeListener(this);
		} else {
			client.handleEvent(new TimerTicked(timerID, timeValue));
		}
	}
}