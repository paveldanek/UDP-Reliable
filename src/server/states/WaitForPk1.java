package server.states;

import events.NewAckToSend;
import events.NewPacketReceived;
import server.Context;

/**
 * The Server's FSM state in which it receives, handles and acknowledges the
 * Packet1 of the two alternating packets (Packet0/Packet1).
 * 
 * @author starnet © 2021
 *
 */
public class WaitForPk1 extends ServerState {
	private static WaitForPk1 instance;

	/**
	 * Private constructor for the singleton pattern
	 */
	private WaitForPk1() {
	}

	public static WaitForPk1 instance() {
		if (instance == null) {
			instance = new WaitForPk1();
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
			Context.instance().handleEvent(NewAckToSend.instance()); // send an ACK for previous
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
			Context.instance().setLastGoodSeqNo();
			Context.instance().handleEvent(NewAckToSend.instance()); // send an ACK
		}
	}

	/**
	 * Sends out a new acknowledgement and reports on it.
	 */
	public void handleEvent(NewAckToSend event) {
		int errorOption = Context.instance().sendAck();
		if (Context.instance().getCurrentPacketIn().getCksum() != 0 || !Context.instance().isInPacketNew()) {
			Context.instance().displayAck(Context.instance().AK_RESEND, errorOption);
		} else {
			Context.instance().displayAck(Context.instance().AK_SEND, errorOption);
		}
	}
}
