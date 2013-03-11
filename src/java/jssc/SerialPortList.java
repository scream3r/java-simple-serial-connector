/* jSSC (Java Simple Serial Connector) - serial port communication library.
 * © Alexey Sokolov (scream3r), 2010-2011.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

/**
 *
 * @author scream3r
 */
public class SerialPortList {

    private static SerialNativeInterface serialInterface;
    private static Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare(String valueA, String valueB) {
            int result = 0;
            if(valueA.toLowerCase().contains("com") && valueB.toLowerCase().contains("com")){
                try {
                    int index1 = Integer.valueOf(valueA.toLowerCase().replace("com", ""));
                    int index2 = Integer.valueOf(valueB.toLowerCase().replace("com", ""));
                    result = index1 - index2;
                }
                catch (Exception ex) {
                    result = valueA.compareToIgnoreCase(valueB);
                }
            } 
            else {
                result = valueA.compareToIgnoreCase(valueB);
            }
            return result;
        }
    };

    static {
        serialInterface = new SerialNativeInterface();
    }

    /**
     * Get sorted array of serial ports in the system
     *
     * @return String array. If there is no ports in the system String[]
     * with <b>zero</b> length will be returned (since jSSC-0.8 in previous versions null will be returned)
     */
    public static String[] getPortNames() {
        if(SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX){
            return getLinuxPortNames();
        }
        else if(SerialNativeInterface.getOsType() == SerialNativeInterface.OS_SOLARIS){//since 0.9.0 ->
            return getSolarisPortNames();
        }
        else if(SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X){
            return getMacOSXPortNames();
        }//<-since 0.9.0
        String[] portNames = serialInterface.getSerialPortNames();
        if(portNames == null){
            return new String[]{};
        }
        TreeSet<String> ports = new TreeSet<String>(comparator);
        ports.addAll(Arrays.asList(portNames));
        return ports.toArray(new String[ports.size()]);
    }

    /**
     * Get serial port names in Linux OS (This method was completely rewrited in 0.8-tb4)
     * 
     * @return
     */
    private static String[] getLinuxPortNames() {
        String[] returnArray = new String[]{};
        try {
            Process dmesgProcess =  Runtime.getRuntime().exec("dmesg");
            BufferedReader reader = new BufferedReader(new InputStreamReader(dmesgProcess.getInputStream()));
            TreeSet<String> portsTree = new TreeSet<String>();
            ArrayList<String> portsList = new ArrayList<String>();
            String buffer = "";
            while((buffer = reader.readLine()) != null && !buffer.isEmpty()){
                if(buffer.matches(".*(ttyS|ttyUSB)[0-9]{1,3}.*")){
                    String[] tmp = buffer.split(" ");
                    for(String value : tmp){
                        if(value.matches("(ttyS|ttyUSB)[0-9]{1,3}")){
                            portsTree.add("/dev/" + value);
                        }
                    }
                }
            }
            for(String portName : portsTree){
                SerialPort serialPort = new SerialPort(portName);
                try {
                    if(serialPort.openPort()){
                        portsList.add(portName);
                        serialPort.closePort();
                    }
                }
                catch (SerialPortException ex) {
                    //since 0.9.0 ->
                    if(ex.getExceptionType().equals(SerialPortException.TYPE_PORT_BUSY)){
                        portsList.add(portName);
                    }
                    //<- since 0.9.0
                }
            }
            returnArray = portsList.toArray(returnArray);
            reader.close();
        }
        catch (IOException ex) {
            //Do nothing
        }
        return returnArray;
    }

    /**
     * Get serial port names in Solaris OS
     *
     * @since 0.9.0
     */
    private static String[] getSolarisPortNames() {
        String[] returnArray = new String[]{};
        File dir = new File("/dev/term");
        if(dir.exists() && dir.isDirectory()){
            File[] files = dir.listFiles();
            if(files.length > 0){
                TreeSet<String> portsTree = new TreeSet<String>();
                ArrayList<String> portsList = new ArrayList<String>();
                for(File file : files){
                    if(!file.isDirectory() && !file.isFile() && file.getName().matches("[0-9]*|[a-z]*")){
                        portsTree.add("/dev/term/" + file.getName());
                    }
                }
                for(String portName : portsTree){
                    portsList.add(portName);
                }
                returnArray = portsList.toArray(returnArray);
            }
        }
        return returnArray;
    }

    /**
     * Get serial port names in Mac OS X
     *
     * @since 0.9.0
     */
    private static String[] getMacOSXPortNames() {
        String[] returnArray = new String[]{};
        File dir = new File("/dev");
        if(dir.exists() && dir.isDirectory()){
            File[] files = dir.listFiles();
            if(files.length > 0){
                TreeSet<String> portsTree = new TreeSet<String>();
                ArrayList<String> portsList = new ArrayList<String>();
                for(File file : files){
                    if(!file.isDirectory() && !file.isFile() && file.getName().matches("tty.(serial.*|usbserial.*)")){
                        portsTree.add("/dev/" + file.getName());
                    }
                }
                for(String portName : portsTree){
                    portsList.add(portName);
                }
                returnArray = portsList.toArray(returnArray);
            }
        }
        return returnArray;
    }
}
