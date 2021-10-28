package packets;

/**
 * This class represents a standard data packet used by the server and client.
 * As is here, it will be used for DATA packets only (for ACK PACKETS, the
 * parent class must be used.
 * 
 * @author starnet © 2021
 *
 */
public class DataPacket extends Packet {

	public static final int DEFAULT_DATA_LENGTH = 500; // NOT to be set over 32000 (computer allows 65535, but variable
														// holding length of the DataPacket is of type short (-32768 to
														// 32767))
	public static final int MAX_DATA_LENGTH = 32000;

	// contains all fields and methods from parent class, plus those below

	private int seqno; // 32-bit (4-byte)
	private byte[] data = new byte[MAX_DATA_LENGTH]; // 0 to MAX_DATA_LENGTH-byte variable size.

	public DataPacket(int seqno, byte[] data) {
		super(0);
		if (data == null) {
			data = new byte[0];
		}
		this.data = data;
		super.setLen((short) (12 + data.length)); // 12 bytes plus payload length
		this.seqno = seqno;
	}

	public int getSeqno() {
		return seqno;
	}

	public void setSeqno(int seqno) {
		this.seqno = seqno;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * A static method which converts a DataPacket to an array of bytes
	 * 
	 * @param packet - DataPacket to be converted
	 * @return an array of bytes ready to be sent
	 */
	public static byte[] buildStream(DataPacket packet) {
		byte[] output = new byte[packet.getLen()];
		output[0] = (byte) ((packet.getCksum() >> 8) & 0xFF);
		output[1] = (byte) (packet.getCksum() & 0xFF);
		output[2] = (byte) ((packet.getLen() >> 8) & 0xFF);
		output[3] = (byte) (packet.getLen() & 0xFF);
		output[4] = (byte) ((packet.getAckno() >> 24) & 0xFF);
		output[5] = (byte) ((packet.getAckno() >> 16) & 0xFF);
		output[6] = (byte) ((packet.getAckno() >> 8) & 0xFF);
		output[7] = (byte) (packet.getAckno() & 0xFF);
		output[8] = (byte) ((packet.getSeqno() >> 24) & 0xFF);
		output[9] = (byte) ((packet.getSeqno() >> 16) & 0xFF);
		output[10] = (byte) ((packet.getSeqno() >> 8) & 0xFF);
		output[11] = (byte) (packet.getSeqno() & 0xFF);
		System.arraycopy(packet.getData(), 0, output, 12, output.length - 12);
		return output;
	}

	/**
	 * A static method which converts an array of bytes to a DataPacket structure.
	 * 
	 * @param data - the array of bytes to be converted
	 * @return - a DataPacket with all its fields
	 */
	public static DataPacket buildPacket(byte[] data) {
		DataPacket output = new DataPacket(0, new byte[data.length - 12]);
		byte[] payload = new byte[data.length - 12];
		output.setCksum((short) ((data[0] & 0xFF) << 8 | data[1] & 0xFF));
		output.setLen((short) ((data[2] & 0xFF) << 8 | data[3] & 0xFF));
		output.setAckno(
				(int) ((data[4] & 0xFF) << 24 | (data[5] & 0xFF) << 16 | (data[6] & 0xFF) << 8 | data[7] & 0xFF));
		output.setSeqno(
				(int) ((data[8] & 0xFF) << 24 | (data[9] & 0xFF) << 16 | (data[10] & 0xFF) << 8 | data[11] & 0xFF));
		System.arraycopy(data, 12, payload, 0, data.length - 12);
		output.setData(payload);
		return output;
	}
}
