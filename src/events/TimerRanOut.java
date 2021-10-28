package events;

/**
 * The class TimerRanOut is invoked when the Time runs out.
 * 
 * @author Ben Hines, Carter Clark, Chris Lara-Batencourt, Pavel Danek, Ricky
 *         Nguyen, modified by starnet © 2021
 *
 */
public class TimerRanOut extends Event {
	private int timerID;

	/**
	 * Represents the event Timer Ran Out.
	 */
	public TimerRanOut(int id) {
		timerID = id;
	}

	/**
	 * Identifies the timer that ticked
	 */
	public int getTimerID() {
		return timerID;
	}
}
