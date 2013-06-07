package jssc;

import java.io.IOException;
import java.io.OutputStream;

public class SerialOutputStream extends OutputStream {
	
	SerialPort serialPort;

	public SerialOutputStream(SerialPort sp) {
		serialPort = sp;
	}

	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
