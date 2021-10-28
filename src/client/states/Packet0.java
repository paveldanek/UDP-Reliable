package client.states;

import client.Context;
import events.NewDataRead;
import events.NewPacketReceived;
import events.TimerRanOut;
import multiTimer.Notifiable;
import multiTimer.Timer;

/**
 * The Client's FSM state in which it reads, creates and sends out the Packet0
 * of the two alternating packets (Packet0/Packet1).
 * 
 * @author starnet © 2021
 *
 */
public class Packet0 extends ClientState implements Notifiable {
	private static Packet0 instance;
	private Timer timer;

	/**
	 * Private constructor for the singleton pattern
	 */
	private Packet0() {
	}

	public static Packet0 instance() {
		if (instance == null) {
			instance = new Packet0();
		}
		return instance;
	}

	/**
	 * Initializes the state
	 */
	@Override
	public void enter() {
		Context.instance().readData();
		Context.instance().listen();
	}

	/**
	 * Performs any necessary clean up while leaving the state
	 */
	@Override
	public void leave() {
		Context.instance().toggleCurrentSeqNo();
	}

	/**
	 * Processes new data read event by sending it out and then reporting on it.
	 */
	public void handleEvent(NewDataRead event) {
		// sending the datagram
		int errorOption = Context.instance().sendPacket();
		int status;
		if (Context.instance().isOutPacketNew()) {
			status = Context.instance().PK_SEND;
		} else {
			status = Context.instance().PK_RESEND;
		}
		// starts the timer to measure timely response of the server (receiver)
		timer = new Timer(this, 0, Context.instance().getTimeoutValue());
		Context.instance().displaySend(status, Context.instance().getCurrentPacketOut().getSeqno(),
				Context.instance().getSendTime(), errorOption, Context.instance().getOffset(),
				Context.instance().getOffset() + Context.instance().getCurrentPacketOut().getLen() - 12);
		// if it was the last packet sent, quit
		if (Context.instance().getCurrentPacketOut().getLen() - 12 == 0) {
			Context.instance().displayBye();
			Context.instance().quit();
		}
	}

	/**
	 * Processes new packet received by displaying the appropriate status info.
	 */
	public void handleEvent(NewPacketReceived event) {
		if (Context.instance().getCurrentPacketIn().getCksum() != 0) {
			Context.instance().displayAck(Context.instance().AK_ERR,
					Context.instance().getCurrentPacketIn().getAckno());
			return;
		}
		if (Context.instance().getCurrentPacketIn().getAckno() == 1) {
			Context.instance().displayAck(Context.instance().AK_DUPL,
					Context.instance().getCurrentPacketIn().getAckno());
			return;
		}
		// if the recieved ACK packet was not out-of-sequence and uncorrupted, gives
		// permission to leave the listen loop of the context, which it it called from
		if (Context.instance().getCurrentPacketIn().getAckno() == 0
				&& Context.instance().getCurrentPacketIn().getCksum() == 0) {
			timer.stop();
			Context.instance().displayAck(Context.instance().AK_REC,
					Context.instance().getCurrentPacketIn().getAckno());
			Context.instance().incrementOffset(Context.instance().getCurrentPacketOut().getLen() - 12);
			Context.instance().setReadyForNextState(true);
		}
	}

	/**
	 * Processes a Timer Ran Out event by triggering New Data Read event which
	 * re-sends the current packet.
	 */
	public void handleEvent(TimerRanOut event) {
		Context.instance().displayTimeout(Context.instance().getCurrentPacketOut().getSeqno());
		Context.instance().handleEvent(NewDataRead.instance());
	}

}
