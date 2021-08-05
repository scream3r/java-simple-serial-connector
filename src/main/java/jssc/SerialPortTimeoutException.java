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
public class SerialPortTimeoutException extends Exception {
    final private static long serialVersionUID = 1L;

    /** Serial port object **/
    private SerialPort port;
    /** Method name **/
    private String methodName;
    /** Timeout value **/
    private int timeoutValue;

    /** Port name **/
    @Deprecated
    private String portName;

    /**
     * Constructs a new <code>SerialPortTimeoutException</code>
     *
     * @param port Port which the exception occurred on
     * @param methodName Method name which the exception occurred on
     * @param timeoutValue Timeout value which the exception occurred on
     */
    public SerialPortTimeoutException(SerialPort port, String methodName, int timeoutValue) {
        super("Port name - " + port.getPortName() + "; Method name - " + methodName + "; Serial port operation timeout (" + timeoutValue + " ms).");
        this.port = port;
        this.methodName = methodName;
        this.timeoutValue = timeoutValue;
    }

    /**
     * Constructs a new <code>SerialPortTimeoutException</code>
     * Deprecated: Use <code>SerialPortTimeoutException(SerialPort, String, int)</code> instead.
     *
     * @param portName Port name which the exception occurred on
     * @param methodName Method name which the exception occurred on
     * @param timeoutValue Timeout value which the exception occurred on
     *
     * @see #SerialPortTimeoutException(SerialPort, String, int)
     */
    @Deprecated
    public SerialPortTimeoutException(String portName, String methodName, int timeoutValue) {
        super("Port name - " + portName + "; Method name - " + methodName + "; Serial port operation timeout (" + timeoutValue + " ms).");
        this.portName = portName;
        this.methodName = methodName;
        this.timeoutValue = timeoutValue;
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
     * Gets timeout value of which the exception was called
     *
     * @return Calling method name
     */
    @SuppressWarnings("unused")
    public int getTimeoutValue() {
        return timeoutValue;
    }
}
