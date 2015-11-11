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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author scream3r, vogt31337@googlemail.com
 */

public class SerialNativeAccess {
    private int osType = -1;
    private static SerialNativeInterface sni = new SerialNativeInterface();
    private static SerialNativeAccess instance = null;
    
    public static SerialNativeAccess getInstance() {
        if (SerialNativeAccess.instance == null) {
            SerialNativeAccess.instance = new SerialNativeAccess();
        } 
        return SerialNativeAccess.instance;
    }
    
    public SerialNativeAccess() {
        String libFolderPath;
        String libName;

        String osName = System.getProperty("os.name");
        String architecture = System.getProperty("os.arch");
        String userHome = System.getProperty("user.home");
        String fileSeparator = System.getProperty("file.separator");
        String tmpFolder = System.getProperty("java.io.tmpdir");

        //since 2.3.0 ->
        String libRootFolder = new File(userHome).canWrite() ? userHome : tmpFolder;
        //<- since 2.3.0

        String javaLibPath = System.getProperty("java.library.path");//since 2.1.0
        
        if(osName.equals("Linux")){
            osName = "linux";
            osType = SerialNativeInterface.OS_LINUX;
        }
        else if(osName.startsWith("Win")){
            osName = "windows";
            osType = SerialNativeInterface.OS_WINDOWS;
        }//since 0.9.0 ->
        else if(osName.equals("SunOS")){
            osName = "solaris";
            osType = SerialNativeInterface.OS_SOLARIS;
        }
        else if(osName.equals("Mac OS X") || osName.equals("Darwin")){//os.name "Darwin" since 2.6.0
            osName = "mac_os_x";
            osType = SerialNativeInterface.OS_MAC_OS_X;
        }//<- since 0.9.0
        else if(osName.equals("FreeBSD")){
            osName = "freebsd";
            osType = SerialNativeInterface.OS_FREEBSD;
        }

        if(architecture.equals("i386") || architecture.equals("i686")){
            architecture = "x86";
        }
        else if(architecture.equals("amd64") || architecture.equals("universal")){//os.arch "universal" since 2.6.0
            architecture = "x86_64";
        }
        else if(architecture.equals("arm")) {//since 2.1.0
            if(osName.equals("Linux")){
                String floatStr = "sf";
                if(javaLibPath.toLowerCase().contains("gnueabihf") || javaLibPath.toLowerCase().contains("armhf")){
                    floatStr = "hf";
                }
                else {
                    try {
                        Process readelfProcess =  Runtime.getRuntime().exec("readelf -A /proc/self/exe");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(readelfProcess.getInputStream()));
                        String buffer = "";
                        while((buffer = reader.readLine()) != null && !buffer.isEmpty()){
                            if(buffer.toLowerCase().contains("Tag_ABI_VFP_args".toLowerCase())){
                                floatStr = "hf";
                                break;
                            }
                        }
                        reader.close();
                    }
                    catch (Exception ex) {
                        //Do nothing
                    }
                }
                architecture = "arm" + floatStr;
            }
            else if(osName.equals("FreeBSD")){
                String floatStr = "";
                if(javaLibPath.toLowerCase().contains("armhf")){
                    floatStr = "hf";
                }
                architecture = "arm" + floatStr;
            }
        }
        
        libFolderPath = libRootFolder + fileSeparator + ".jssc" + fileSeparator + osName;
        libName = "jSSC-" + SerialNativeInterface.libVersion + "_" + architecture;
        libName = System.mapLibraryName(libName);

        if(libName.endsWith(".dylib")){//Since 2.1.0 MacOSX 10.8 fix
            libName = libName.replace(".dylib", ".jnilib");
        }
        
        boolean loadLib = false;

        if(isLibFolderExist(libFolderPath)){
            if(isLibFileExist(libFolderPath + fileSeparator + libName)){
                loadLib = true;
            }
            else {
                if(extractLib((libFolderPath + fileSeparator + libName), osName, libName)){
                    loadLib = true;
                }
            }
        }
        else {
            if(new File(libFolderPath).mkdirs()){
                if(extractLib((libFolderPath + fileSeparator + libName), osName, libName)){
                    loadLib = true;
                }
            }
        }

        if (loadLib) {
            System.load(libFolderPath + fileSeparator + libName);
            String versionBase = SerialNativeInterface.libVersion;
            String versionNative = sni.getNativeLibraryVersion();
            if (!versionBase.equals(versionNative)) {
                System.err.println("Warning! jSSC Java and Native versions mismatch (Java: " + versionBase + ", Native: " + versionNative + ")");
            }
        }
    }

    /**
     * Is library folder exists
     *
     * @param libFolderPath
     *
     * @since 0.8
     */
    private static boolean isLibFolderExist(String libFolderPath) {
        boolean returnValue = false;
        File folder = new File(libFolderPath);
        if(folder.exists() && folder.isDirectory()){
            returnValue = true;
        }
        return returnValue;
    }

    /**
     * Is library file exists
     * 
     * @param libFilePath
     *
     * @since 0.8
     */
    private static boolean isLibFileExist(String libFilePath) {
        boolean returnValue = false;
        File folder = new File(libFilePath);
        if(folder.exists() && folder.isFile()){
            returnValue = true;
        }
        return returnValue;
    }

    /**
     * Extract lib to lib folder
     *
     * @param libFilePath
     * @param osName
     * @param libName
     *
     * @since 0.8
     */
    private static boolean extractLib(String libFilePath, String osName, String libName) {
        boolean returnValue = false;
        File libFile = new File(libFilePath);
        InputStream input = null;
        FileOutputStream output = null;
        input = SerialNativeInterface.class.getResourceAsStream("/libs/" + osName + "/" + libName);
        if(input != null){
            int read;
            byte[] buffer = new byte[4096];
            try {
                output = new FileOutputStream(libFilePath);
                while((read = input.read(buffer)) != -1){
                    output.write(buffer, 0, read);
                }
                output.close();
                input.close();
                returnValue = true;
            }
            catch (Exception ex) {
                try {
                    output.close();
                    if(libFile.exists()){
                        libFile.delete();
                    }
                }
                catch (Exception ex_out) {
                    //Do nothing
                }
                try {
                    input.close();
                }
                catch (Exception ex_in) {
                    //Do nothing
                }
            }
        }
        return returnValue;
    }

    /**
     * Get OS type (OS_LINUX || OS_WINDOWS || OS_SOLARIS)
     * 
     * @since 0.8
     */
    public int getOsType() {
        return osType;
    }

    public SerialNativeInterface getInterface() {
        return sni;
    }

    /**
     * Get jSSC version. The version of library is <b>Base Version</b> + <b>Minor Suffix</b>
     *
     * @since 0.8
     */
//    public String getLibraryVersion() {
//        return libVersion + "." + libMinorSuffix;
//    }

    /**
     * Get jSSC Base Version
     *
     * @since 0.9.0
     */
//    public String getLibraryBaseVersion() {
//        return libVersion;
//    }

    /**
     * Get jSSC minor suffix. For example in version 0.8.1 - <b>1</b> is a minor suffix
     *
     * @since 0.9.0
     */
//    public String getLibraryMinorSuffix() {
//        return libMinorSuffix;
//    }
}
