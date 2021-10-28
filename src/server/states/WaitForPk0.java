package server.states;

import events.NewAckToSend;
import events.NewPacketReceived;
import server.Context;

/**
 * The Server's FSM state in which it receives, handles and acknowledges the
 * Packet0 of the two alternating packets (Packet0/Packet1).
 * 
 * @author starnet © 2021
 *
 */
public class WaitForPk0 extends ServerState {
	private static WaitForPk0 instance;

	/**
	 * Private constructor for the singleton pattern
	 */
	private WaitForPk0() {
	}

	public static WaitForPk0 instance() {
		if (instance == null) {
			instance = new WaitForPk0();
		}
		return instance;
	}

	/**
	 * Initializes the state
	 */
	@Override
	public void enter() {
		Context.instance().listen();
	}

	/**
	 * Performs any necessary clean up while leaving the state
	 */
	@Override
	public void leave() {
		Context.instance().toggleExpectedSeqNo();
	}

	/**
	 * Processes new packet received by displaying the appropriate status info.
	 */
	public void handleEvent(NewPacketReceived event) {
		if (Context.instance().getCurrentPacketIn().getCksum() != 0) { // if data packet is corrupted
			Context.instance().displayReceive(Context.instance().PK_RCVD,
					Context.instance().getCurrentPacketIn().getSeqno(), Context.instance().getReceiveTime(),
					Context.instance().CON_CORR);
		} else if (!Context.instance().isInPacketNew()) { // if it's a duplicate
			Context.instance().displayReceive(Context.instance().PK_DUPL,
					Context.instance().getCurrentPacketIn().getSeqno(), Context.instance().getReceiveTime(),
					Context.instance().CON_OOS);
			Context.instance().handleEvent(NewAckToSend.instance()); // send an ACK
		} else if (Context.instance().getCurrentPacketIn().getLen() - 12 == 0) { // if it's empty (end of transmission)
			Context.instance().displayReceive(Context.instance().PK_RCVD,
					Context.instance().getCurrentPacketIn().getSeqno(), Context.instance().getReceiveTime(),
					Context.instance().CON_RCVD);
			Context.instance().displayBye();
			Context.instance().quit();
		} else { // if it's a new uncorrupted, unempty data packet
			Context.instance().displayReceive(Context.instance().PK_RCVD,
					Context.instance().getCurrentPacketIn().getSeqno(), Context.instance().getReceiveTime(),
					Context.instance().CON_RCVD);
			Context.instance().writeData(); // write it into output binary file
			Context.instance().handleEvent(NewAckToSend.instance()); // send an ACK
		}
	}

	/**
	 * Sends out a new acknowledgement and reports on it.
	 */
	public void handleEvent(NewAckToSend event) {
		if (Context.instance().isInPacketNew()) {
			int errorOption = Context.instance().sendAck(); // acknowledge Packet0
			Context.instance().displayAck(Context.instance().AK_SEND, 0, errorOption);
		} else {
			Context.instance().toggleExpectedSeqNo();
			int errorOption = Context.instance().sendAck(); // acknowledge Packet1
			Context.instance().toggleExpectedSeqNo();
			Context.instance().displayAck(Context.instance().AK_RESEND, 1, errorOption);
		}
	}
}
