package multiTimer;

import events.TimerRanOut;
import events.TimerTicked;

/**
 * An entity that can be notified of timing events
 * 
 * @author Brahma Dathan
 *
 */
public interface Notifiable {
	/**
	 * Process timer ticks
	 */
	public void handleEvent(TimerTicked event);

	/**
	 * Process timer runs out event
	 */
	public void handleEvent(TimerRanOut event);
}
