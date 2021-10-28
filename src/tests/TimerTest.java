package tests;

import events.TimerRanOut;
import events.TimerTicked;
import multiTimer.Notifiable;
import multiTimer.Timer;

/**
 * TEST of built-in multi-timer.
 * 
 * @author starnet © 2021
 *
 */
public class TimerTest implements Notifiable {

	public TimerTest() {
	}

	public void handleEvent(TimerTicked event) {
		if (event.getTimeLeft() % 1000 == 0) {
			System.out.println("Timer #" + event.getTimerID() + " = " + (event.getTimeLeft() / 1000) + "sec left.");
		}
	};

	public void handleEvent(TimerRanOut event) {
		System.out.println("TIMER #" + event.getTimerID() + " TIMED OUT.");
	};

	public static void main(String[] args) throws InterruptedException {
		// We're using timer1 for all three timers, which is possible, unless we need to
		// stop an individual timer before it runs out. In that case it's recommended we
		// use an array of timers, length of which is the amount of timers needed.
		Timer timer1 = new Timer(new TimerTest(), 1, 15000);
		System.out
				.println("--- Timer " + timer1.getTimerID() + "set to " + (timer1.getTimeValue() / 1000) + " seconds.");
		Thread.sleep(2000);
		timer1 = new Timer(new TimerTest(), 2, 7000);
		System.out
				.println("--- Timer " + timer1.getTimerID() + "set to " + (timer1.getTimeValue() / 1000) + " seconds.");
		Thread.sleep(1500);
		timer1 = new Timer(new TimerTest(), 3, 3000);
		System.out
				.println("--- Timer " + timer1.getTimerID() + "set to " + (timer1.getTimeValue() / 1000) + " seconds.");
		Thread.sleep(13000);
		timer1.stop();
		System.exit(0);
	}

}
