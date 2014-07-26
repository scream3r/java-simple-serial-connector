/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scream3r.jssc;

import junit.framework.TestCase;
import org.junit.Assert;

/**
 *
 * @author mvogt
 */
public class TestSerialPortWrite extends TestCase {

    private String findASerialPort() {
        String[] out = SerialPortList.getPortNames();

        if (out.length > 0) {
            return out[0];
        } else {
            return null;
        }
    }

    public void testSerialPortWrite() {
        String port = findASerialPort(); // could also be "COM1" under windows, or /dev/ttyS0 under linux

        if (port != null) {
            SerialPort serialPort = new SerialPort(port);
            try {
                serialPort.openPort();//Open serial port
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                serialPort.writeBytes("This is a test string".getBytes());//Write data to port
                serialPort.close();//Close serial port
            } catch (SerialPortException ex) {
                System.out.println(ex);
            }
        } else {
            Assert.fail("No comport found. Maybe running on a laptop? Or cables aren't attached?");
        }
        assertTrue(true);
    }
}
