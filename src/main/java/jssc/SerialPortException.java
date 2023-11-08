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

/**
 *
 * @author scream3r
 */
public class SerialPortException extends Exception {
    final private static long serialVersionUID = 1L;
    /** Port already opened **/
    final public static String TYPE_PORT_ALREADY_OPENED = "Port already opened";
    /** Port not opened **/
    final public static String TYPE_PORT_NOT_OPENED = "Port not opened";
    /** Can't set mask **/
    final public static String TYPE_CANT_SET_MASK = "Can't set mask";
    /** Event listener already added **/
    final public static String TYPE_LISTENER_ALREADY_ADDED = "Event listener already added";
    /** Event listener thread interrupted **/
    final public static String TYPE_LISTENER_THREAD_INTERRUPTED = "Event listener thread interrupted";
    /** Can't remove event listener **/
    final public static String TYPE_CANT_REMOVE_LISTENER = "Can't remove event listener, because listener not added";
    /**
     * @since 0.8
     */
    final public static String TYPE_PARAMETER_IS_NOT_CORRECT = "Parameter is not correct";
    /**
     * @since 0.8
     */
    final public static String TYPE_NULL_NOT_PERMITTED = "Null not permitted";
    /**
     * @since 0.9.0
     */
    final public static String TYPE_PORT_BUSY = "Port busy";
    /**
     * @since 0.9.0
     */
    final public static String TYPE_PORT_NOT_FOUND = "Port not found";
    /**
     * @since 2.2.0
     */
    final public static String TYPE_PERMISSION_DENIED = "Permission denied";
    /**
     * @since 2.3.0
     */
    final public static String TYPE_INCORRECT_SERIAL_PORT = "Incorrect serial port";

    /** Exception occurred in native code */
    final public static String TYPE_NATIVE_EXCEPTION = "Native exception occurred: %s";

    /** Serial port object **/
    private SerialPort port;
    /** Method name **/
    private String methodName;
    /** Exception type **/
    private String exceptionType;

    /** Port name **/
    @Deprecated
    private String portName;

    /**
     * Constructs a new <code>SerialPortException</code>
     *
     * @param port Port which the exception occurred on
     * @param methodName Method name which the exception occurred on
     * @param exceptionType Any <code>SerialPortException.TYPE_*</code>
     */
    public SerialPortException(SerialPort port, String methodName, String exceptionType) {
        super("Port name - " + port.getPortName() + "; Method name - " + methodName + "; Exception type - " + exceptionType + ".");
        this.port = port;
        this.methodName = methodName;
        this.exceptionType = exceptionType;
    }

    /**
     * Constructs a new <code>SerialPortException</code>
     * Deprecated: Use <code>SerialPortTimeoutException(SerialPort, String, String)</code> instead.
     *
     * @param portName Port which the exception occurred on
     * @param methodName Method name which the exception occurred on
     * @param exceptionType Any <code>SerialPortException.TYPE_*</code>
     *
     * @see #SerialPortException(SerialPort, String, String)
     */
    @Deprecated
    public SerialPortException(String portName, String methodName, String exceptionType) {
        super("Port name - " + portName + "; Method name - " + methodName + "; Exception type - " + exceptionType + ".");
        this.portName = portName;
        this.methodName = methodName;
        this.exceptionType = exceptionType;
    }

    public static SerialPortException wrapNativeException(Exception ex, SerialPort port, String methodName) {
        return new SerialPortException(port, methodName, String.format(TYPE_NATIVE_EXCEPTION, ex.getLocalizedMessage()));
    }

    /**
     * Getting port name during operation with which the exception was called
     * Deprecated: Use <code>getPort().getName()</code> instead.
     *
     * @return Port name
     */
    @Deprecated
    public String getPortName() {
        return port != null ? port.getPortName() : portName;
    }

    /**
     * Gets the <code>SerialPort</code> which threw the exception
     *
     * @return <code>SerialPort</code> object
     */
    @SuppressWarnings("unused")
    public SerialPort getPort() {
        return port;
    }

    /**
     * Gets the method name during execution of which the exception was called
     *
     * @return Calling method name
     */
    @SuppressWarnings("unused")
    public String getMethodName() {
        return methodName;
    }

    /**
     * Getting exception type
     *
     * @return a value from <code>SerialPortException.TYPE_*</code>
     * or a custom <code>String</code> value if provided
     */
    @SuppressWarnings("unused")
    public String getExceptionType() {
        return exceptionType;
    }
}
