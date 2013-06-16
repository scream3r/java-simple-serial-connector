package jssc;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class that wraps a {@link SerialPort} to provide
 * {@link OutputStream} functionality.
 * 
 * @author Charles Hache <chalz@member.fsf.org>
 *
 */
public class SerialOutputStream extends OutputStream {
	
	SerialPort serialPort;

	/** Instantiates a SerialOutputStream for the given {@link SerialPort}
	 * @param sp The serial port to stream.
	 */
	public SerialOutputStream(SerialPort sp) {
		serialPort = sp;
	}

	@Override
	public void write(int b) throws IOException {
		try {
			serialPort.writeInt(b);
		} catch (SerialPortException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
		
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		byte[] buffer = new byte[len];
		System.arraycopy(b, off, buffer, 0, len);
		try {
			serialPort.writeBytes(buffer);
		} catch (SerialPortException e) {
			throw new IOException(e);
		}
	}
}
