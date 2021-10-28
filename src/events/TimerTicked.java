package events;

/**
 * The class TimerTicked is invoked when the Timer ticks.
 * 
 * @author Ben Hines, Carter Clark, Chris Lara-Batencourt, Pavel Danek, Ricky
 *         Nguyen, modified by starnet © 2021
 *
 */
public class TimerTicked extends Event {
	private int timeLeft;
	private int timerID;

	/**
	 * Stores the amount of time left in the Timer.
	 * 
	 * @param value the amount of time left
	 */
	public TimerTicked(int id, int value) {
		timerID = id;
		timeLeft = value;
	}

	/**
	 * Needed for display purposes
	 */
	public int getTimeLeft() {
		return timeLeft;
	}

	/**
	 * Identifies the timer that ticked
	 */
	public int getTimerID() {
		return timerID;
	}
}
