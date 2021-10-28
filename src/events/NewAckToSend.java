package events;

/**
 * The class NewAckToSend is invoked when new ACKnoledgment has been generated
 * by the server (receiver).
 * 
 * @author starnet © 2021
 *
 */
public class NewAckToSend extends Event {
	private static NewAckToSend instance;

	/**
	 * Singleton pattern.
	 */
	private NewAckToSend() {

	}

	/**
	 * Creates a new instance of the NewAckToSend object.
	 * 
	 * @return instance returns the instance of NewAckToSend
	 */
	public static NewAckToSend instance() {
		if (instance == null) {
			instance = new NewAckToSend();
		}
		return instance;
	}
}
