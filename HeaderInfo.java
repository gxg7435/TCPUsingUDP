import java.io.Serializable;

/**
 * This class is implemented to add header values to packet like sequence
 * number,acknowledgement number, and various other flags and payload array in
 * byte[] format.
 * 
 * @author gaurav gaur
 *
 */

public class HeaderInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	public boolean ackFlag = false;
	public boolean synFlag = false;
	public boolean finishFlag = false;

	public int seqNo = 0, ackNo = 0;
	public byte[] data;

	/**
	 * getter for ack flag
	 * 
	 * @return : ack flag in boolean
	 */
	public boolean isAckFlag() {
		return ackFlag;
	}

	/**
	 * Setter for ack flag
	 * 
	 * @param ackFlag:
	 *            set ack flag with value passed
	 */
	public void setAckFlag(boolean ackFlag) {
		this.ackFlag = ackFlag;
	}

	/**
	 * setter for syn flag
	 * 
	 * @return : syn flag in boolean
	 */
	public boolean isSynFlag() {
		return synFlag;
	}

	/**
	 * setter for syn flag.
	 * 
	 * @param synFlag:
	 *            set syn flag with this parameter.
	 */
	public void setSynFlag(boolean synFlag) {
		this.synFlag = synFlag;
	}

	/**
	 * getter for finish flag
	 * 
	 * @return : finish flag in boolean format
	 */
	public boolean isFinishFlag() {
		return finishFlag;
	}

	/**
	 * Setter for finish flag
	 * 
	 * @param finishFlag:
	 *            set finish flag with this value.
	 */
	public void setFinishFlag(boolean finishFlag) {
		this.finishFlag = finishFlag;
	}

	/**
	 * getter for sequence number
	 * 
	 * @return: sequence number in integer format
	 */
	public int getSeqNo() {
		return seqNo;
	}

	/**
	 * setter for sequence number
	 * 
	 * @param seqNo:
	 *            set sequence number with this value
	 */
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	/**
	 * getter for acknowledgement number
	 */
	public int getAckNo() {
		return ackNo;
	}

	/**
	 * setter for acknowledgement number
	 * 
	 * @param ackNo:
	 *            set ackno with this value
	 */
	public void setAckNo(int ackNo) {
		this.ackNo = ackNo;
	}

	/**
	 * getter for payload
	 * 
	 * @return: payload in byte[] format
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * setter for payload
	 * 
	 * @param data;
	 *            set the payload with this value.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

}
