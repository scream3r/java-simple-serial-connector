/* jSSC (Java Simple Serial Connector) - serial port communication library.
 * © Alexey Sokolov (scream3r), 2010-2014.
 *
 * This file is part of jSSC.
 *
 * jSSC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSSC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSSC.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you use jSSC in public project you can inform me about this by e-mail,
 * of course if you want it.
 *
 * e-mail: scream3r.org@gmail.com
 * web-site: http://scream3r.org | http://code.google.com/p/java-simple-serial-connector/
 */
package jssc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

/**
 *
 * @author scream3r
 */
@SuppressWarnings("unused")
public class SerialPort {

    private final SerialNativeInterface serialInterface;
    private SerialPortEventListener eventListener;
    private volatile long portHandle;
    private final String portName;
    private volatile boolean portOpened = false;
    private boolean maskAssigned = false;
    private boolean eventListenerAdded = false;

    //since 2.2.0 ->
    private volatile Method methodErrorOccurred = null;
    //<- since 2.2.0

    /** Baud rate 110 symbols/second **/
    public static final int BAUDRATE_110 = 110;
    /** Baud rate 300 symbols/second **/
    public static final int BAUDRATE_300 = 300;
    /** Baud rate 600 symbols/second **/
    public static final int BAUDRATE_600 = 600;
    /** Baud rate 1200 symbols/second **/
    public static final int BAUDRATE_1200 = 1200;
    /** Baud rate 2400 symbols/second **/
    public static final int BAUDRATE_2400 = 2400;
    /** Baud rate 4800 symbols/second **/
    public static final int BAUDRATE_4800 = 4800;
    /** Baud rate 9600 symbols/second **/
    public static final int BAUDRATE_9600 = 9600;
    /** Baud rate 14400 symbols/second **/
    public static final int BAUDRATE_14400 = 14400;
    /** Baud rate 19200 symbols/second **/
    public static final int BAUDRATE_19200 = 19200;
    /** Baud rate 38400 symbols/second **/
    public static final int BAUDRATE_38400 = 38400;
    /** Baud rate 57600 symbols/second **/
    public static final int BAUDRATE_57600 = 57600;
    /** Baud rate 115200 symbols/second **/
    public static final int BAUDRATE_115200 = 115200;
    /** Baud rate 128000 symbols/second **/
    public static final int BAUDRATE_128000 = 128000;
    /** Baud rate 256000 symbols/second **/
    public static final int BAUDRATE_256000 = 256000;

    /** Five (5) data bits per byte **/
    public static final int DATABITS_5 = 5;
    /** Six (6) data bits per byte **/
    public static final int DATABITS_6 = 6;
    /** Seven (7) data bits per byte **/
    public static final int DATABITS_7 = 7;
    /** Eight (8) data bits per byte **/
    public static final int DATABITS_8 = 8;

    /** One (1) stop bits per byte **/
    public static final int STOPBITS_1 = 1;
    /** Two (2) stop bits per byte **/
    public static final int STOPBITS_2 = 2;
    /** One and a half (1.5) stop bits per byte **/
    public static final int STOPBITS_1_5 = 3;

    /** Do not send a parity bit **/
    public static final int PARITY_NONE = 0;
    /** Use a very basic checksum: Send an additional bit to report if odd number of data bits **/
    public static final int PARITY_ODD = 1;
    /** Use a very basic checksum: Send an additional bit to report if even number of data bits **/
    public static final int PARITY_EVEN = 2;
    /** Use a very basic checksum: Send a parity bit to indicate "mark signal" condition (rarely used) **/
    public static final int PARITY_MARK = 3;
    /** Use a very basic checksum: Send a parity bit to indicate "space signal" condition (rarely used) **/
    public static final int PARITY_SPACE = 4;


    /** Receive "abort" status code **/
    public static final int PURGE_RXABORT = 0x0002;
    /** Receive "clear" status code **/
    public static final int PURGE_RXCLEAR = 0x0008;
    /** Transfer "abort" status code **/
    public static final int PURGE_TXABORT = 0x0001;
    /** Transfer "clear" status code **/
    public static final int PURGE_TXCLEAR = 0x0004;


    /** Receive character flag **/
    public static final int MASK_RXCHAR = 1;
    /** Receive flag **/
    public static final int MASK_RXFLAG = 2;
    /** Receive empty flag **/
    public static final int MASK_TXEMPTY = 4;
    /** CTS (clear to send) mask **/
    public static final int MASK_CTS = 8;
    /** DSR (data set to ready) flag **/
    public static final int MASK_DSR = 16;
    /** RLSD (Receive Line Signal Detected) flag **/
    public static final int MASK_RLSD = 32;
    /** Break (reset a communications line or change the operating mode) flag **/
    public static final int MASK_BREAK = 64;
    /** Error flag **/
    public static final int MASK_ERR = 128;
    /** State of RING line (0 - OFF, 1 - ON) **/
     public static final int MASK_RING = 256;


    //since 0.8 ->
    /** Disable flow control **/
    public static final int FLOWCONTROL_NONE = 0;
    /** Legacy hardware control: RTS/CTS (request to send/clear to send) flow control on input **/
    public static final int FLOWCONTROL_RTSCTS_IN = 1;
    /** Legacy hardware control: RTS/CTS (request to send/clear to send) flow control on output **/
    public static final int FLOWCONTROL_RTSCTS_OUT = 2;
    /** Software flow control: XON/XOFF flow control on input **/
    public static final int FLOWCONTROL_XONXOFF_IN = 4;
    /** Software flow control: XON/XOFF flow control on output **/
    public static final int FLOWCONTROL_XONXOFF_OUT = 8;
    //<- since 0.8

    //since 0.8 ->
    /** Indicates faulty data or frame **/
    public static final int ERROR_FRAME = 0x0008;
    /** Overrun error; receiver hardware unable to handle receive data **/
    public static final int ERROR_OVERRUN = 0x0002;
    /** Parity/check bit error **/
    public static final int ERROR_PARITY = 0x0004;
    //<- since 0.8

    //since 2.6.0 ->
    /** Ignore bytes with framing error or parity error **/
    private static final int PARAMS_FLAG_IGNPAR = 1;
    /** Mark bytes with parity error or framing error **/
    private static final int PARAMS_FLAG_PARMRK = 2;
    //<- since 2.6.0

    /**
     * Construct a serial port object with the specified <code>portName</code>
     *
     * @param portName Name of the port, e.g. <code>COM1</code>, <code>/dev/tty.FOO</code>, etc.
     */
    public SerialPort(String portName) {
        this.portName = portName;
        serialInterface = new SerialNativeInterface();
    }

    /**
     * Getting port name under operation
     *
     * @return Method returns port name under operation as a String
     */
    public String getPortName(){
        return portName;
    }

    /**
     * Getting port state
     * 
     * @return Method returns true if port is open, otherwise false
     */
    public boolean isOpened() {
        return portOpened;
    }

    /**
     * Port opening
     * <br><br>
     * <b>Note: </b>If port busy <b>TYPE_PORT_BUSY</b> exception will be thrown.
     * If port not found <b>TYPE_PORT_NOT_FOUND</b> exception will be thrown.
     *
     * @return If the operation is successfully completed, the method returns true
     *
     * @throws SerialPortException if exception occurred
     */
    public synchronized boolean openPort() throws SerialPortException {
        if(portOpened){
            throw new SerialPortException(this, "openPort()", SerialPortException.TYPE_PORT_ALREADY_OPENED);
        }
        if(portName != null){
            boolean useTIOCEXCL = (System.getProperty(SerialNativeInterface.PROPERTY_JSSC_NO_TIOCEXCL) == null &&
                                   System.getProperty(SerialNativeInterface.PROPERTY_JSSC_NO_TIOCEXCL.toLowerCase()) == null);
            portHandle = serialInterface.openPort(portName, useTIOCEXCL);//since 2.3.0 -> (if JSSC_NO_TIOCEXCL defined, exclusive lock for serial port will be disabled)
        }
        else {
            throw new SerialPortException(this, "openPort()", SerialPortException.TYPE_NULL_NOT_PERMITTED);//since 2.1.0 -> NULL port name fix
        }
        if(portHandle == SerialNativeInterface.ERR_PORT_BUSY){
            throw new SerialPortException(this, "openPort()", SerialPortException.TYPE_PORT_BUSY);
        }
        else if(portHandle == SerialNativeInterface.ERR_PORT_NOT_FOUND){
            throw new SerialPortException(this, "openPort()", SerialPortException.TYPE_PORT_NOT_FOUND);
        }
        else if(portHandle == SerialNativeInterface.ERR_PERMISSION_DENIED){
            throw new SerialPortException(this, "openPort()", SerialPortException.TYPE_PERMISSION_DENIED);
        }
        else if(portHandle == SerialNativeInterface.ERR_INCORRECT_SERIAL_PORT){
            throw new SerialPortException(this, "openPort()", SerialPortException.TYPE_INCORRECT_SERIAL_PORT);
        }
        portOpened = true;
        return true;
    }

    /**
     * Setting the parameters of port. RTS and DTR lines are enabled by default
     * 
     * @param baudRate data transfer rate
     * @param dataBits number of data bits
     * @param stopBits number of stop bits
     * @param parity parity
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     */
    public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException {
        return setParams(baudRate, dataBits, stopBits, parity, true, true);
    }

    /**
     * Setting the parameters of port
     *
     * @param baudRate data transfer rate
     * @param dataBits number of data bits
     * @param stopBits number of stop bits
     * @param parity parity
     * @param setRTS initial state of RTS line(ON/OFF)
     * @param setDTR initial state of DTR line(ON/OFF)
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public synchronized boolean setParams(int baudRate, int dataBits, int stopBits, int parity, boolean setRTS, boolean setDTR) throws SerialPortException {
        checkPortOpened("setParams()");
        if(stopBits == 1){
            stopBits = 0;
        }
        else if(stopBits == 3){
            stopBits = 1;
        }
        int flags = 0;
        if(System.getProperty(SerialNativeInterface.PROPERTY_JSSC_IGNPAR) != null || System.getProperty(SerialNativeInterface.PROPERTY_JSSC_IGNPAR.toLowerCase()) != null){
            flags |= PARAMS_FLAG_IGNPAR;
        }
        if(System.getProperty(SerialNativeInterface.PROPERTY_JSSC_PARMRK) != null || System.getProperty(SerialNativeInterface.PROPERTY_JSSC_PARMRK.toLowerCase()) != null){
            flags |= PARAMS_FLAG_PARMRK;
        }
        return serialInterface.setParams(portHandle, baudRate, dataBits, stopBits, parity, setRTS, setDTR, flags);
    }

    /**
     * Purge of input and output buffer. Required flags shall be sent to the input. Variables with prefix 
     * <b>"PURGE_"</b>, for example <b>"PURGE_RXCLEAR"</b>. Sent parameter "flags" is additive value,
     * so addition of flags is allowed. For example, if input or output buffer shall be purged, 
     * parameter <b>"PURGE_RXCLEAR | PURGE_TXCLEAR"</b>.
     * <br><b>Note: </b>some devices or drivers may not support this function
     *
     * @param flags One or more <code>SerialPort.PURGE_*</code> flag
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false. 
     *
     * @throws SerialPortException if exception occurred
     */
    public synchronized boolean purgePort(int flags) throws SerialPortException {
        checkPortOpened("purgePort()");
        return serialInterface.purgePort(portHandle, flags);
    }

    /**
     * Events mask for Linux OS
     *
     * @since 0.8
     */
    private int linuxMask;

    /**
     * Set events mask. Required flags shall be sent to the input. Variables with prefix 
     * <b>"MASK_"</b>, shall be used as flags, for example <b>"MASK_RXCHAR"</b>. 
     * Sent parameter "mask" is additive value, so addition of flags is allowed.
     * For example if messages about data receipt and CTS and DSR status changing
     * shall be received, it is required to set the mask - <b>"MASK_RXCHAR | MASK_CTS | MASK_DSR"</b>
     *
     * @param mask One or more <code>SerialPort.MASK_*</code> value
     * 
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     */
    public synchronized boolean setEventsMask(int mask) throws SerialPortException {
        checkPortOpened("setEventsMask()");
        if(SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_SOLARIS ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X){//since 0.9.0
            linuxMask = mask;
            if(mask > 0){
                maskAssigned = true;
            }
            else {
                maskAssigned = false;
            }
            return true;
        }
        boolean returnValue = serialInterface.setEventsMask(portHandle, mask);
        if(!returnValue){
            throw new SerialPortException(this, "setEventsMask()", SerialPortException.TYPE_CANT_SET_MASK);
        }
        if(mask > 0){
            maskAssigned = true;
        }
        else {
            maskAssigned = false;
        }
        return returnValue;
    }

    /**
     * Getting events mask for the port
     * 
     * @return Method returns events mask as int type variable. This variable is an additive value
     *
     * @throws SerialPortException if exception occurred
     */
    public int getEventsMask() throws SerialPortException {
        checkPortOpened("getEventsMask()");
        if(SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_SOLARIS ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X){//since 0.9.0
            return linuxMask;
        }
        return serialInterface.getEventsMask(portHandle);
    }

    /**
     * Getting events mask for the port is Linux OS (for internal use)
     * 
     * @since 0.8
     */
    private int getLinuxMask() {
        return linuxMask;
    }

    /**
     * Change RTS line state. Set "true" for switching ON and "false" for switching OFF RTS line
     *
     * @param enabled on/off flag
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     */
    public boolean setRTS(boolean enabled) throws SerialPortException {
        checkPortOpened("setRTS()");
        return serialInterface.setRTS(portHandle, enabled);
    }

    /**
     * Change DTR line state. Set "true" for switching ON and "false" for switching OFF DTR line
     *
     * @param enabled on/off flag
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     */
    public boolean setDTR(boolean enabled) throws SerialPortException {
        checkPortOpened("setDTR()");
        return serialInterface.setDTR(portHandle, enabled);
    }

    /**
     * Write byte array to port
     *
     * @param buffer <code>byte[]</code> array to write
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     * 
     * @throws SerialPortException if exception occurred
     */
    public boolean writeBytes(byte[] buffer) throws SerialPortException {
        checkPortOpened("writeBytes()");
        try {
            return serialInterface.writeBytes(portHandle, buffer);
        } catch(IOException ex) {
            throw SerialPortException.wrapNativeException(ex, this, "writeBytes");
        }
    }

    /**
     * Write single byte to port
     *
     * @param singleByte single <code>byte</code> value to write
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public boolean writeByte(byte singleByte) throws SerialPortException {
        checkPortOpened("writeByte()");
        return writeBytes(new byte[]{singleByte});
    }

    /**
     * Write String to port
     *
     * @param string <code>String</code> value to write
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public boolean writeString(String string) throws SerialPortException {
        checkPortOpened("writeString()");
        return writeBytes(string.getBytes());
    }

    /**
     * Write String to port
     *
     * @param string <code>String</code> value to write
     * @param charsetName valid <code>Charset</code> name, e.g. <code>"UTF-8"</code>
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     * @throws UnsupportedEncodingException if encoding exception occurred
     *
     * @since 2.8.0
     */
    public boolean writeString(String string, String charsetName) throws SerialPortException, UnsupportedEncodingException {
        checkPortOpened("writeString()");
        return writeBytes(string.getBytes(charsetName));
    }

    /**
     * Write int value (in range from 0 to 255 (0x00 - 0xFF)) to port
     *
     * @param singleInt single <code>int</code> value to write
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public boolean writeInt(int singleInt) throws SerialPortException {
        checkPortOpened("writeInt()");
        return writeBytes(new byte[]{(byte)singleInt});
    }

    /**
     * Write int array (in range from 0 to 255 (0x00 - 0xFF)) to port
     *
     * @param buffer <code>int[]</code> array to write
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public boolean writeIntArray(int[] buffer) throws SerialPortException {
        checkPortOpened("writeIntArray()");
        byte[] byteArray = new byte[buffer.length];
        for(int i = 0; i < buffer.length; i++){
            byteArray[i] = (byte)buffer[i];
        }
        return writeBytes(byteArray);
    }

    /**
     * Read byte array from port
     *
     * @param byteCount count of bytes for reading
     * 
     * @return byte array with "byteCount" length
     *
     * @throws SerialPortException if exception occurred
     */
    public byte[] readBytes(int byteCount) throws SerialPortException {
        checkPortOpened("readBytes()");
        return serialInterface.readBytes(portHandle, byteCount);
    }

    /**
     * Read string from port
     *
     * @param byteCount count of bytes for reading
     *
     * @return byte array with "byteCount" length converted to String
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public String readString(int byteCount) throws SerialPortException {
        checkPortOpened("readString()");
        return new String(readBytes(byteCount));
    }

    /**
     * Read Hex string from port (example: FF 0A FF). Separator by default is a space
     *
     * @param byteCount count of bytes for reading
     *
     * @return byte array with "byteCount" length converted to Hexadecimal String
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public String readHexString(int byteCount) throws SerialPortException {
        checkPortOpened("readHexString()");
        return readHexString(byteCount, " ");
    }

    /**
     * Read Hex string from port with set separator (example if separator is "::": FF::0A::FF)
     *
     * @param byteCount count of bytes for reading
     * @param separator the hex string separator, e.g. "::", etc
     *
     * @return byte array with "byteCount" length converted to Hexadecimal String
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public String readHexString(int byteCount, String separator) throws SerialPortException {
        checkPortOpened("readHexString()");
        String[] strBuffer = readHexStringArray(byteCount);
        StringBuilder returnString = new StringBuilder();
        boolean insertSeparator = false;
        for(String value : strBuffer){
            if(insertSeparator){
                returnString.append(separator);
            }
            returnString.append(value);
            insertSeparator = true;
        }
        return returnString.toString();
    }

    /**
     * Read Hex String array from port
     *
     * @param byteCount count of bytes for reading
     * 
     * @return String array with "byteCount" length and Hexadecimal String values
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public String[] readHexStringArray(int byteCount) throws SerialPortException {
        checkPortOpened("readHexStringArray()");
        int[] intBuffer = readIntArray(byteCount);
        String[] strBuffer = new String[intBuffer.length];
        for(int i = 0; i < intBuffer.length; i++){
            String value = Integer.toHexString(intBuffer[i]).toUpperCase();
            if(value.length() == 1) {
                value = "0" + value;
            }
            strBuffer[i] = value;
        }
        return strBuffer;
    }

    /**
     * Read int array from port
     *
     * @param byteCount count of bytes for reading
     *
     * @return int array with values in range from 0 to 255
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public int[] readIntArray(int byteCount) throws SerialPortException {
        checkPortOpened("readIntArray()");
        byte[] buffer = readBytes(byteCount);
        int[] intBuffer = new int[buffer.length];
        for(int i = 0; i < buffer.length; i++){
            if(buffer[i] < 0){
                intBuffer[i] = 256 + buffer[i];
            }
            else {
                intBuffer[i] = buffer[i];
            }
        }
        return intBuffer;
    }

    private void waitBytesWithTimeout(String methodName, int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        checkPortOpened("waitBytesWithTimeout()");
        boolean timeIsOut = true;
        long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < timeout){
            if(getInputBufferBytesCount() >= byteCount){
                timeIsOut = false;
                break;
            }
        }
        if(timeIsOut){
            throw new SerialPortTimeoutException(this, methodName, timeout);
        }
    }

    /**
     * Read byte array from port
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return byte array with "byteCount" length
     *
     * @throws SerialPortException if exception occurred
     * @throws SerialPortTimeoutException if timeout exception occurred
     *
     * @since 2.0
     */
    public byte[] readBytes(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        checkPortOpened("readBytes()");
        waitBytesWithTimeout("readBytes()", byteCount, timeout);
        return readBytes(byteCount);
    }

    /**
     * Read string from port
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return byte array with "byteCount" length converted to String
     *
     * @throws SerialPortException if exception occurred
     * @throws SerialPortTimeoutException if timeout exception occurred
     *
     * @since 2.0
     */
    public String readString(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        checkPortOpened("readString()");
        waitBytesWithTimeout("readString()", byteCount, timeout);
        return readString(byteCount);
    }

    /**
     * Read Hex string from port (example: FF 0A FF). Separator by default is a space
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return byte array with "byteCount" length converted to Hexadecimal String
     *
     * @throws SerialPortException if exception occurred
     * @throws SerialPortTimeoutException if timeout exception occurred
     *
     * @since 2.0
     */
    public String readHexString(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        checkPortOpened("readHexString()");
        waitBytesWithTimeout("readHexString()", byteCount, timeout);
        return readHexString(byteCount);
    }

    /**
     * Read Hex string from port with set separator (example if separator is "::": FF::0A::FF)
     *
     * @param byteCount count of bytes for reading
     * @param separator the hex string separator, e.g. "::", etc
     * @param timeout timeout in milliseconds
     *
     * @return byte array with "byteCount" length converted to Hexadecimal String
     *
     * @throws SerialPortException if exception occurred
     * @throws SerialPortTimeoutException if timeout exception occurred
     *
     * @since 2.0
     */
    public String readHexString(int byteCount, String separator, int timeout) throws SerialPortException, SerialPortTimeoutException {
        checkPortOpened("readHexString()");
        waitBytesWithTimeout("readHexString()", byteCount, timeout);
        return readHexString(byteCount, separator);
    }

    /**
     * Read Hex String array from port
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return String array with "byteCount" length and Hexadecimal String values
     *
     * @throws SerialPortException if exception occurred
     * @throws SerialPortTimeoutException if timeout exception occurred
     *
     * @since 2.0
     */
    public String[] readHexStringArray(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        checkPortOpened("readHexStringArray()");
        waitBytesWithTimeout("readHexStringArray()", byteCount, timeout);
        return readHexStringArray(byteCount);
    }

    /**
     * Read int array from port
     *
     * @param byteCount count of bytes for reading
     * @param timeout timeout in milliseconds
     *
     * @return int array with values in range from 0 to 255
     *
     * @throws SerialPortException if exception occurred
     * @throws SerialPortTimeoutException if timeout exception occurred
     *
     * @since 2.0
     */
    public int[] readIntArray(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        checkPortOpened("readIntArray()");
        waitBytesWithTimeout("readIntArray()", byteCount, timeout);
        return readIntArray(byteCount);
    }

    /**
     * Read all available bytes from port like a byte array
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public byte[] readBytes() throws SerialPortException {
        checkPortOpened("readBytes()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readBytes(byteCount);
    }

    /**
     * Read all available bytes from port like a String
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to String
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public String readString() throws SerialPortException {
        checkPortOpened("readString()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readString(byteCount);
    }

    /**
     * Read all available bytes from port like a Hex String
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public String readHexString() throws SerialPortException {
        checkPortOpened("readHexString()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readHexString(byteCount);
    }

    /**
     * Read all available bytes from port like a Hex String with set separator
     *
     * @param separator the hex string separator, e.g. "::", etc
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public String readHexString(String separator) throws SerialPortException {
        checkPortOpened("readHexString()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readHexString(byteCount, separator);
    }

    /**
     * Read all available bytes from port like a Hex String array
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to Hex String array
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public String[] readHexStringArray() throws SerialPortException {
        checkPortOpened("readHexStringArray()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readHexStringArray(byteCount);
    }

    /**
     * Read all available bytes from port like a int array (values in range from 0 to 255)
     *
     * @return If input buffer is empty <b>null</b> will be returned, else byte array with all data from port converted to int array
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public int[] readIntArray() throws SerialPortException {
        checkPortOpened("readIntArray()");
        int byteCount = getInputBufferBytesCount();
        if(byteCount <= 0){
            return null;
        }
        return readIntArray(byteCount);
    }

    /**
     * Get count of bytes in input buffer
     *
     * @return Count of bytes in input buffer or -1 if error occured
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public int getInputBufferBytesCount() throws SerialPortException {
        checkPortOpened("getInputBufferBytesCount()");
        return serialInterface.getBuffersBytesCount(portHandle)[0];
    }

    /**
     * Get count of bytes in output buffer
     *
     * @return Count of bytes in output buffer or -1 if error occured
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public int getOutputBufferBytesCount() throws SerialPortException {
        checkPortOpened("getOutputBufferBytesCount()");
        return serialInterface.getBuffersBytesCount(portHandle)[1];
    }

    /**
     * Set flow control mode. For required mode use variables with prefix <b>"FLOWCONTROL_"</b>.
     * Example of hardware flow control mode(RTS/CTS): setFlowControlMode(FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT);
     *
     * @param mask mask of flow control mode
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public boolean setFlowControlMode(int mask) throws SerialPortException {
        checkPortOpened("setFlowControlMode()");
        return serialInterface.setFlowControlMode(portHandle, mask);
    }

    /**
     * Get flow control mode
     *
     * @return Mask of set flow control mode
     *
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public int getFlowControlMode() throws SerialPortException {
        checkPortOpened("getFlowControlMode()");
        return serialInterface.getFlowControlMode(portHandle);
    }

    /**
     * Send Break singnal for set duration
     *
     * @param duration duration of Break signal, in milliseconds
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     * 
     * @throws SerialPortException if exception occurred
     *
     * @since 0.8
     */
    public boolean sendBreak(int duration)throws SerialPortException {
        checkPortOpened("sendBreak()");
        return serialInterface.sendBreak(portHandle, duration);
    }

    private int[][] waitEvents() {
        return serialInterface.waitEvents(portHandle);
    }

    /**
     * Check port opened (since jSSC-0.8 String "EMPTY" was replaced with "portName" variable)
     *
     * @param methodName method name
     *
     * @throws SerialPortException if exception occurred
     */
    private void checkPortOpened(String methodName) throws SerialPortException {
        if(!portOpened){
            throw new SerialPortException(this, methodName, SerialPortException.TYPE_PORT_NOT_OPENED);
        }
    }

    /**
     * Getting lines status. Lines status is sent as 0 – OFF and 1 - ON
     *
     * @return Method returns the array containing information about lines in following order:
     * <br><b>element 0</b> - <b>CTS</b> line state
     * <br><b>element 1</b> - <b>DSR</b> line state
     * <br><b>element 2</b> - <b>RING</b> line state
     * <br><b>element 3</b> - <b>RLSD</b> line state
     *
     * @throws SerialPortException if exception occurred
     */
    public int[] getLinesStatus() throws SerialPortException {
        checkPortOpened("getLinesStatus()");
        return serialInterface.getLinesStatus(portHandle);
    }

    /**
     * Get state of CTS line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     */
    public boolean isCTS() throws SerialPortException {
        checkPortOpened("isCTS()");
        if(serialInterface.getLinesStatus(portHandle)[0] == 1){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Get state of DSR line
     *
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     */
    public boolean isDSR() throws SerialPortException {
        checkPortOpened("isDSR()");
        if(serialInterface.getLinesStatus(portHandle)[1] == 1){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Get state of RING line
     * 
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     */
    public boolean isRING() throws SerialPortException {
        checkPortOpened("isRING()");
        if(serialInterface.getLinesStatus(portHandle)[2] == 1){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Get state of RLSD line
     * 
     * @return If line is active, method returns true, otherwise false
     *
     * @throws SerialPortException if exception occurred
     */
    public boolean isRLSD() throws SerialPortException {
        checkPortOpened("isRLSD()");
        if(serialInterface.getLinesStatus(portHandle)[3] == 1){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Add event listener. Object of <b>"SerialPortEventListener"</b> type shall
     * be sent to the method. This object shall be properly described, as it will
     * be in charge for handling of occurred events. This method will independently
     * set the mask in <b>"MASK_RXCHAR"</b> state if it was not set beforehand
     *
     * @param listener Event Listener of type <code>SerialPortListener</code>
     *
     * @throws SerialPortException if exception occurred
     */
    public void addEventListener(SerialPortEventListener listener) throws SerialPortException {
        addEventListener(listener, MASK_RXCHAR, false);
    }

    /**
     * Add event listener. Object of <b>"SerialPortEventListener"</b> type shall be sent
     * to the method. This object shall be properly described, as it will be in
     * charge for handling of occurred events. Also events mask shall be sent to
     * this method, to do it use variables with prefix <b>"MASK_"</b> for example <b>"MASK_RXCHAR"</b>
     *
     * @param listener Event Listener of type <code>SerialPortListener</code>
     * @param mask <code>SerialPort.MASK_*</code> representing the event mask to listen upon
     *
     * @see #setEventsMask(int) setEventsMask(int mask)
     *
     * @throws SerialPortException if exception occurred
     */
    public void addEventListener(SerialPortEventListener listener, int mask) throws SerialPortException {
        addEventListener(listener, mask, true);
    }

    /**
     * Internal method. Add event listener. Object of <b>"SerialPortEventListener"</b> type shall be sent
     * to the method. This object shall be properly described, as it will be in
     * charge for handling of occurred events. Also events mask shall be sent to
     * this method, to do it use variables with prefix <b>"MASK_"</b> for example <b>"MASK_RXCHAR"</b>. If
     * <b>overwriteMask == true</b> and mask has been already assigned it value will be rewritten by <b>mask</b>
     * value, if <b>overwriteMask == false</b> and mask has been already assigned the new <b>mask</b> value will be ignored,
     * if there is no assigned mask to this serial port the <b>mask</b> value will be used for setting it up in spite of
     * <b>overwriteMask</b> value
     *
     * @param listener Event Listener of type <code>SerialPortListener</code>
     * @param mask <code>SerialPort.MASK_*</code> representing the event mask to listen upon
     * @param overwriteMask overwrite <code>mask</code> if already assigned
     *
     * @see #setEventsMask(int) setEventsMask(int mask)
     *
     * @throws SerialPortException if exception occurred
     */
    private synchronized void addEventListener(SerialPortEventListener listener, int mask, boolean overwriteMask) throws SerialPortException {
        checkPortOpened("addEventListener()");
        if(!eventListenerAdded){
            if((maskAssigned && overwriteMask) || !maskAssigned) {
                setEventsMask(mask);
            }
            eventListener = listener;
            eventThread = getNewEventThread();
            eventThread.setName("EventThread " + portName);
            //since 2.2.0 ->
            try {
                Method method = eventListener.getClass().getMethod("errorOccurred", new Class<?>[]{SerialPortException.class});
                method.setAccessible(true);
                methodErrorOccurred = method;
            }
            catch (SecurityException ex) {
                //Do nothing
            }
            catch (NoSuchMethodException ex) {
                //Do nothing
            }
            //<- since 2.2.0
            eventThread.start();
            eventListenerAdded = true;
        }
        else {
            throw new SerialPortException(this, "addEventListener()", SerialPortException.TYPE_LISTENER_ALREADY_ADDED);
        }
    }

    /**
     * Create new EventListener Thread depending on the type of operating system
     *
     * @since 0.8
     */
    private EventThread getNewEventThread() {
        if(SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_SOLARIS ||
           SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X){//since 0.9.0
            return new LinuxEventThread();
        }
        return new EventThread();
    }

    /**
     * Delete event listener. Mask is set to 0. So at the next addition of event
     * handler you shall set required event mask again
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     * 
     * @throws SerialPortException if exception occurred
     */
    public synchronized boolean removeEventListener() throws SerialPortException {
        checkPortOpened("removeEventListener()");
        if(!eventListenerAdded){
            throw new SerialPortException(this, "removeEventListener()", SerialPortException.TYPE_CANT_REMOVE_LISTENER);
        }
        eventThread.terminateThread();
        setEventsMask(0);
        if(Thread.currentThread().getId() != eventThread.getId()){
            if(eventThread.isAlive()){
                try {
                    eventThread.join(5000);
                }
                catch (InterruptedException ex) {
                    throw new SerialPortException(this, "removeEventListener()", SerialPortException.TYPE_LISTENER_THREAD_INTERRUPTED);
                }
            }
        }
        methodErrorOccurred = null;
        eventListenerAdded = false;
        return true;
    }

    /**
     * Close port. This method deletes event listener first, then closes the port
     *
     * @return If the operation is successfully completed, the method returns true, otherwise false
     * 
     * @throws SerialPortException if exception occurred
     */
    public synchronized boolean closePort() throws SerialPortException {
        checkPortOpened("closePort()");
        if(eventListenerAdded){
            removeEventListener();
        }
        boolean returnValue = serialInterface.closePort(portHandle);
        if(returnValue){
            maskAssigned = false;
            portOpened = false;
        }
        return returnValue;
    }

    private EventThread eventThread;

    private class EventThread extends Thread {

        private boolean threadTerminated = false;
        
        @Override
        public void run() {
            while(!threadTerminated){
                int[][] eventArray = waitEvents();
                for(int[] event : eventArray){
                    if(event[0] > 0 && !threadTerminated){
                        eventListener.serialEvent(new SerialPortEvent(SerialPort.this, event[0], event[1]));
                        //FIXME
                        /*if(methodErrorOccurred != null){
                            try {
                                methodErrorOccurred.invoke(eventListener, new Object[]{new SerialPortException(SerialPort.this, "method", "exception")});
                            }
                            catch (Exception ex) {
                                System.out.println(ex);
                            }
                        }*/
                    }
                }
            }
        }

        private void terminateThread(){
            threadTerminated = true;
        }
    }

    /**
     * EventListener for Linux OS
     *
     * @since 0.8
     */
    private class LinuxEventThread extends EventThread {

        //Essential interruptions for events: BREAK, ERR, TXEMPTY
        private final int INTERRUPT_BREAK = 512;
        private final int INTERRUPT_TX = 1024;
        private final int INTERRUPT_FRAME = 2048;
        private final int INTERRUPT_OVERRUN = 4096;
        private final int INTERRUPT_PARITY = 8192;

        //Count of interruptions
        private int interruptBreak;
        private int interruptTX;
        private int interruptFrame;
        private int interruptOverrun;
        private int interruptParity;

        //Previous states if lines (then state change event will be generated)
        private int preCTS;
        private int preDSR;
        private int preRLSD;
        private int preRING;

        //Need to get initial states
        public LinuxEventThread(){
            int[][] eventArray = waitEvents();
            for(int[] event : eventArray){
                int eventType = event[0];
                int eventValue = event[1];
                switch(eventType){
                    case INTERRUPT_BREAK:
                        interruptBreak = eventValue;
                        break;
                    case INTERRUPT_TX:
                        interruptTX = eventValue;
                        break;
                    case INTERRUPT_FRAME:
                        interruptFrame = eventValue;
                        break;
                    case INTERRUPT_OVERRUN:
                        interruptOverrun = eventValue;
                        break;
                    case INTERRUPT_PARITY:
                        interruptParity = eventValue;
                        break;
                    case MASK_CTS:
                        preCTS = eventValue;
                        break;
                    case MASK_DSR:
                        preDSR = eventValue;
                        break;
                    case MASK_RING:
                        preRING = eventValue;
                        break;
                    case MASK_RLSD:
                        preRLSD = eventValue;
                        break;
                }
            }
        }

        @Override
        public void run() {
            while(!super.threadTerminated){
                int[][] eventArray = waitEvents();
                int mask = getLinuxMask();
                boolean interruptTxChanged = false;
                int errorMask = 0;
                for(int[] event : eventArray){
                    boolean sendEvent = false;
                    int eventType = event[0];
                    int eventValue = event[1];
                    if(eventType > 0 && !super.threadTerminated){
                        switch(eventType){
                            case INTERRUPT_BREAK:
                                if(eventValue != interruptBreak){
                                    interruptBreak = eventValue;
                                    if((mask & MASK_BREAK) == MASK_BREAK){
                                        eventType = MASK_BREAK;
                                        eventValue = 0;
                                        sendEvent = true;
                                    }
                                }
                                break;
                            case INTERRUPT_TX:
                                if(eventValue != interruptTX){
                                    interruptTX = eventValue;
                                    interruptTxChanged = true;
                                }
                                break;
                            case INTERRUPT_FRAME:
                                if(eventValue != interruptFrame){
                                    interruptFrame = eventValue;
                                    errorMask |= ERROR_FRAME;
                                }
                                break;
                            case INTERRUPT_OVERRUN:
                                if(eventValue != interruptOverrun){
                                    interruptOverrun = eventValue;
                                    errorMask |= ERROR_OVERRUN;
                                }
                                break;
                            case INTERRUPT_PARITY:
                                if(eventValue != interruptParity){
                                    interruptParity = eventValue;
                                    errorMask |= ERROR_PARITY;
                                }
                                if((mask & MASK_ERR) == MASK_ERR && errorMask != 0){
                                    eventType = MASK_ERR;
                                    eventValue = errorMask;
                                    sendEvent = true;
                                }
                                break;
                            case MASK_CTS:
                                if(eventValue != preCTS){
                                    preCTS = eventValue;
                                    if((mask & MASK_CTS) == MASK_CTS){
                                        sendEvent = true;
                                    }
                                }
                                break;
                            case MASK_DSR:
                                if(eventValue != preDSR){
                                    preDSR = eventValue;
                                    if((mask & MASK_DSR) == MASK_DSR){
                                        sendEvent = true;
                                    }
                                }
                                break;
                            case MASK_RING:
                                if(eventValue != preRING){
                                    preRING = eventValue;
                                    if((mask & MASK_RING) == MASK_RING){
                                        sendEvent = true;
                                    }
                                }
                                break;
                            case MASK_RLSD: /*DCD*/
                                if(eventValue != preRLSD){
                                    preRLSD = eventValue;
                                    if((mask & MASK_RLSD) == MASK_RLSD){
                                        sendEvent = true;
                                    }
                                }
                                break;
                            case MASK_RXCHAR:
                                if(((mask & MASK_RXCHAR) == MASK_RXCHAR) && (eventValue > 0)){
                                    sendEvent = true;
                                }
                                break;
                            /*case MASK_RXFLAG:
                                //Do nothing at this moment
                                if(((mask & MASK_RXFLAG) == MASK_RXFLAG) && (eventValue > 0)){
                                    sendEvent = true;
                                }
                                break;*/
                            case MASK_TXEMPTY:
                                if(((mask & MASK_TXEMPTY) == MASK_TXEMPTY) && (eventValue == 0) && interruptTxChanged){
                                    sendEvent = true;
                                }
                                break;
                        }
                        if(sendEvent){
                            eventListener.serialEvent(new SerialPortEvent(SerialPort.this, eventType, eventValue));
                        }
                    }
                }
                //Need to sleep some time
                try {
                    Thread.sleep(0, 100);
                }
                catch (Exception ex) {
                    //Do nothing
                }
            }
        }
    }
}
