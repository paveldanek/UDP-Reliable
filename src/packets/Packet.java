package packets;

/**
 * This class represents a standard packet used by the server and client. As is
 * here, it will be used for ACK packets (for DATA PACKETS, an extended class
 * must be used).
 * 
 * @author starnet © 2021
 *
 */
public class Packet {

	private short cksum; // 16-bit (2-byte)
	private short len; // 16-bit (2-byte)
	private int ackno; // 32-bit (4-byte)

	public Packet(int ackno) {
		this.cksum = 0; // 0 = packet is not corrupted
		this.len = 8; // always 8 bytes long
		this.ackno = ackno;
	}

	public short getCksum() {
		return cksum;
	}

	public void setCksum(short cksum) {
		this.cksum = cksum;
	}

	public short getLen() {
		return len;
	}

	public void setLen(short len) {
		this.len = len;
	}

	public int getAckno() {
		return ackno;
	}

	public void setAckno(int ackno) {
		this.ackno = ackno;
	}

	/**
	 * A static method which converts a Packet to an array of bytes
	 * 
	 * @param packet - Packet to be converted
	 * @return an array of bytes ready to be sent
	 */
	public static byte[] buildStream(Packet packet) {
		byte[] output = new byte[packet.getLen()];
		output[0] = (byte) ((packet.getCksum() >> 8) & 0xFF);
		output[1] = (byte) (packet.getCksum() & 0xFF);
		output[2] = (byte) ((packet.getLen() >> 8) & 0xFF);
		output[3] = (byte) (packet.getLen() & 0xFF);
		output[4] = (byte) ((packet.getAckno() >> 24) & 0xFF);
		output[5] = (byte) ((packet.getAckno() >> 16) & 0xFF);
		output[6] = (byte) ((packet.getAckno() >> 8) & 0xFF);
		output[7] = (byte) (packet.getAckno() & 0xFF);
		return output;
	}

	/**
	 * A static method which converts an array of bytes to a Packet structure.
	 * 
	 * @param data - the array of bytes to be converted
	 * @return - a Packet with all its fields
	 */
	public static Packet buildPacket(byte[] data) {
		Packet output = new Packet(0);
		output.setCksum((short) ((data[0] & 0xFF) << 8 | data[1] & 0xFF));
		output.setLen((short) ((data[2] & 0xFF) << 8 | data[3] & 0xFF));
		output.setAckno(
				(int) ((data[4] & 0xFF) << 24 | (data[5] & 0xFF) << 16 | (data[6] & 0xFF) << 8 | data[7] & 0xFF));
		return output;
	}
}
