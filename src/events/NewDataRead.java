package events;

/**
 * The class NewDataRead is invoked when new piece of data has been read from
 * file by the client (sender).
 * 
 * @author starnet © 2021
 *
 */
public class NewDataRead extends Event {
	private static NewDataRead instance;

	/**
	 * Singleton pattern.
	 */
	private NewDataRead() {

	}

	/**
	 * Creates a new instance of the NewDataRead object.
	 * 
	 * @return instance returns the instance of NewDataRead
	 */
	public static NewDataRead instance() {
		if (instance == null) {
			instance = new NewDataRead();
		}
		return instance;
	}
}
