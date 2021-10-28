package events;

/**
 * The class NewPacketReceived is invoked when a new packet has been received by
 * either client (sender) or server (receiver).
 * 
 * @author starnet © 2021
 *
 */
public class NewPacketReceived extends Event {
	private static NewPacketReceived instance;

	/**
	 * Singleton pattern.
	 */
	private NewPacketReceived() {

	}

	/**
	 * Creates a new instance of the NewPacketReceived object.
	 * 
	 * @return instance returns the instance of NewPacketReceived
	 */
	public static NewPacketReceived instance() {
		if (instance == null) {
			instance = new NewPacketReceived();
		}
		return instance;
	}
}
