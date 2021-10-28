package tests;

import packets.DataPacket;

/**
 * TEST of byte array creation for datagrams.
 * 
 * @author starnet © 2021
 *
 */
public class ByteStreamTest {

	public static void main(String[] args) {
		byte[] data = { 0, 1, 2, 3, 4, 5, 6, 7, -124, 69, 97, 0, -24, 101 };
		byte[] byteStream;
		DataPacket DP = new DataPacket(25487651, data);
		DP.setCksum((short) 12470);
		DP.setAckno(-5214806);
		System.out.println(DP.getCksum() + " " + DP.getLen() + " " + DP.getAckno() + " " + DP.getSeqno());
		for (int i = 0; i < DP.getLen() - 12; i++) {
			System.out.print(DP.getData()[i] + " ");
		}
		System.out.println(".\n\n");
		byteStream = DataPacket.buildStream(DP);
		for (int j = 0; j < byteStream.length; j++) {
			System.out.print(byteStream[j] + " ");
		}
		System.out.println(".\n\n");
		DataPacket RECEIVE = null;
		RECEIVE = DataPacket.buildPacket(byteStream);
		System.out.println(
				RECEIVE.getCksum() + " " + RECEIVE.getLen() + " " + RECEIVE.getAckno() + " " + RECEIVE.getSeqno());
		for (int k = 0; k < RECEIVE.getLen() - 12; k++) {
			System.out.print(RECEIVE.getData()[k] + " ");
		}
		System.out.println(".\n\n");
	}

}
