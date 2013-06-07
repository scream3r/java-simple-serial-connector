package jssc;

import java.io.IOException;
import java.io.InputStream;

public class SerialInputStream extends InputStream {
	
	private SerialPort serialPort;
	private int timeout = 0;

	public SerialInputStream(SerialPort sp) {
		serialPort = sp;
	}
	
	/** Set the timeout (ms) for subsequent calls to {@link #read()}, 
	 * {@link #blockingRead(int[])}, and {@link #blockingRead(int[], int, int)}
	 * @param time The timeout in milliseconds.
	 */
	public void setTimeout(int time) {
		timeout = time;
	}

	/** Reads the next byte from the port.
	 * If the timeout of this stream has been set, then this method
	 * blocks until data is available or until the timeout has been hit.
	 * If the timeout is not set or has been set to 0, then this method
	 * blocks indefinitely.
	 */
	@Override
	public int read() throws IOException {
		byte[] buf = new byte[1];
		try {
			if (timeout > 0) {
				buf = serialPort.readBytes(1, timeout);
			} else {
				buf = serialPort.readBytes(1);
			}
			return buf[0];
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	/** Non-blocking read of up to buf.length bytes from the stream.
	 * This is a call as follows: read(buf, 0, buf.length).
	 * @param buf The buffer to fill.
	 * @return The number of bytes read, which can be 0.
	 * @throws IOException on error.
	 */
	@Override
	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}
	
	/** Non-blocking read of up to length bytes from the stream.
	 * 
	 * @param buf The buffer to fill.
	 * @param offset The offset into the buffer to start copying data.
	 * @param length The max number of bytes to read.
	 * @return The actual number of bytes read, which can be 0.
	 * @throws IOException on error.
	 */
	@Override
	public int read(byte[] buf, int offset, int length) throws IOException {
		
		if (buf.length < offset + length)
			length = buf.length - offset;
		
		int available = this.available();
		
		if (available > length)
			available = length;
		
		try {
			byte[] readBuf = serialPort.readBytes(available);
			System.arraycopy(readBuf, 0, buf, offset, length);
		} catch (Exception e) {
			throw new IOException(e);
		}
		throw new IOException("No data - serial port error");
	}
	
	/** Blocks until buf.length bytes are read, an error occurs, or the timeout is hit if specified.
	 * This is a call as follows: blockingRead(buf, 0, buf.length);
	 * @param buf The buffer to fill with data.
	 * @return The number of bytes read.
	 * @throws IOException On error.
	 */
	public int blockingRead(byte[] buf) throws IOException {
		return blockingRead(buf, 0, buf.length);
	}
	
	/** Blocks until length bytes are read, an error occurs, or the timeout is hit if specified.
	 * Saves the data into the given buffer at the specified offset
	 * If the timeout isn't set, behaves as {@link #read(byte[], int, int)} would.
	 * @param buf The buffer to fill.
	 * @param offset The offset in buffer to save the data.
	 * @param length The number of bytes to read.
	 * @return the number of bytes read.
	 * @throws IOException on error or timeout.
	 */
	public int blockingRead(byte[] buf, int offset, int length) throws IOException {

		if (buf.length < offset + length)
			throw new IOException("Not enough buffer space for serial data");
		
		if (timeout < 1)
			return read(buf, offset, length);
		
		try {
			byte[] readBuf = serialPort.readBytes(length, timeout);
			System.arraycopy(readBuf, 0, buf, offset, length);
		} catch (Exception e) {
			throw new IOException(e);
		}
		throw new IOException("No data - serial port error");
	}
	
	@Override
	public int available() throws IOException {
		int ret;
		try {
			ret = serialPort.getInputBufferBytesCount();
			if (ret >= 0) 
				return ret;
			throw new IOException("Error checking available bytes from the serial port.");
		} catch (Exception e) {
			throw new IOException("Error checking available bytes from the serial port.");
		} 
	}

}
